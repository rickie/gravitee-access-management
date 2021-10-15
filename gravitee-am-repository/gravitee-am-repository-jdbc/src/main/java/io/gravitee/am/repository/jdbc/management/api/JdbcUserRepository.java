/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.repository.jdbc.management.api;

import static io.gravitee.am.model.ReferenceType.DOMAIN;
import static java.time.ZoneOffset.UTC;
import static java.util.stream.Stream.concat;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.CriteriaDefinition.from;
import static reactor.adapter.rxjava.RxJava2Adapter.*;

import io.gravitee.am.common.analytics.Field;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.analytics.AnalyticsQuery;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.scim.Address;
import io.gravitee.am.model.scim.Attribute;
import io.gravitee.am.repository.jdbc.common.dialect.ScimUserSearch;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcUser;
import io.gravitee.am.repository.jdbc.management.api.spring.user.*;
import io.gravitee.am.repository.management.api.UserRepository;
import io.gravitee.am.repository.management.api.search.FilterCriteria;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.relational.core.query.Update;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.data.util.StreamUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class JdbcUserRepository extends AbstractJdbcRepository implements UserRepository {
    private static final String ATTRIBUTE_USER_FIELD_EMAIL = "email";
    private static final String ATTRIBUTE_USER_FIELD_PHOTO = "photo";
    private static final String ATTRIBUTE_USER_FIELD_IM = "im";
    private static final String ATTRIBUTE_USER_FIELD_PHONE = "phoneNumber";

    private static short CONCURRENT_FLATMAP = 1;

    @Autowired
    protected SpringUserRepository userRepository;

    @Autowired
    protected SpringUserRoleRepository roleRepository;

    @Autowired
    protected SpringUserAddressesRepository addressesRepository;

    @Autowired
    protected SpringUserAttributesRepository attributesRepository;

    @Autowired
    protected SpringUserEntitlementRepository entitlementRepository;

    protected User toEntity(JdbcUser entity) {
        return mapper.map(entity, User.class);
    }

    protected JdbcUser toJdbcEntity(User entity) {
        return mapper.map(entity, JdbcUser.class);
    }

    @Override
    public Flowable<User> findAll(ReferenceType referenceType, String referenceId) {
        LOGGER.debug("findByReference({})", referenceId);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(userRepository.findByReference(referenceType.name(), referenceId)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(user -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(completeUser(user)).flux()))));
    }

    @Override
    public Single<Page<User>> findAll(ReferenceType referenceType, String referenceId, int page, int size) {
        LOGGER.debug("findAll({}, {}, {}, {})", referenceType, referenceId, page, size);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(dbClient.select()
                .from(JdbcUser.class)
                .matching(from(where("reference_id").is(referenceId)
                        .and(where("reference_type").is(referenceType.name()))))
                .orderBy(Sort.Order.asc("id"))
                .page(PageRequest.of(page, size))
                .as(JdbcUser.class).all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)))
                .flatMap(user -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(completeUser(user)).flux()), CONCURRENT_FLATMAP)).collectList().flatMap(content->RxJava2Adapter.singleToMono(userRepository.countByReference(referenceType.name(), referenceId)).map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long count)->new Page<User>(content, page, count)))));
    }

    @Override
    public Single<Page<User>> search(ReferenceType referenceType, String referenceId, String query, int page, int size) {
        LOGGER.debug("search({}, {}, {}, {}, {})", referenceType, referenceId, query, page, size);

        boolean wildcardSearch = query.contains("*");
        String wildcardValue = query.replaceAll("\\*+", "%");

        String search = this.databaseDialectHelper.buildSearchUserQuery(wildcardSearch, page, size);
        String count = this.databaseDialectHelper.buildCountUserQuery(wildcardSearch);

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(dbClient.execute(search)
                .bind("value", wildcardSearch ? wildcardValue : query)
                .bind("refId", referenceId)
                .bind("refType", referenceType.name())
                .as(JdbcUser.class)
                .fetch().all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)))
                .flatMap(app -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(completeUser(app)).flux()), CONCURRENT_FLATMAP)).collectList().flatMap(data->dbClient.execute(count).bind("value", wildcardSearch ? wildcardValue : query).bind("refId", referenceId).bind("refType", referenceType.name()).as(Long.class).fetch().first().map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long total)->new Page<User>(data, page, total)))));
    }

    @Override
    public Single<Page<User>> search(ReferenceType referenceType, String referenceId, FilterCriteria criteria, int page, int size) {
        LOGGER.debug("search({}, {}, {}, {}, {})", referenceType, referenceId, criteria, page, size);

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(" FROM users WHERE reference_id = :refId AND reference_type = :refType AND ");
        ScimUserSearch search = this.databaseDialectHelper.prepareScimSearchUserQuery(queryBuilder, criteria, page, size);

        // execute query
        DatabaseClient.GenericExecuteSpec executeSelect = dbClient.execute(search.getSelectQuery());
        executeSelect = executeSelect.bind("refType", referenceType.name()).bind("refId", referenceId);
        for (Map.Entry<String, Object> entry : search.getBinding().entrySet()) {
            executeSelect = executeSelect.bind(entry.getKey(), entry.getValue());
        }
        Flux<JdbcUser> userFlux = executeSelect.as(JdbcUser.class).fetch().all();

        // execute count to provide total in the Page
        DatabaseClient.GenericExecuteSpec executeCount = dbClient.execute(search.getCountQuery());
        executeCount = executeCount.bind("refType", referenceType.name()).bind("refId", referenceId);
        for (Map.Entry<String, Object> entry : search.getBinding().entrySet()) {
            executeCount = executeCount.bind(entry.getKey(), entry.getValue());
        }
        Mono<Long> userCount = executeCount.as(Long.class).fetch().one();

        return RxJava2Adapter.monoToSingle(userFlux.map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(user -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(completeUser(user)).flux()))).collectList().flatMap(list->userCount.map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long total)->new Page<User>(list, page, total)))));
    }

    @Override
    public Flowable<User> findByDomainAndEmail(String domain, String email, boolean strict) {
        boolean ignoreCase = !strict;
        return RxJava2Adapter.fluxToFlowable(dbClient.execute(databaseDialectHelper.buildFindUserByReferenceAndEmail(DOMAIN, domain, email, strict))
                .bind("refId", domain)
                .bind("refType", DOMAIN.name())
                .bind("email", email)
                .as(JdbcUser.class)
                .fetch()
                .all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(user -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(completeUser(user)).flux()))));
    }

    @Override
    public Maybe<User> findByUsernameAndDomain(String domain, String username) {
        LOGGER.debug("findByUsernameAndDomain({},{},{})", domain, username);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(userRepository.findByUsername(ReferenceType.DOMAIN.name(), domain, username)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(z->RxJava2Adapter.singleToMono(completeUser(z))));
    }

    @Override
    public Maybe<User> findByUsernameAndSource(ReferenceType referenceType, String referenceId, String username, String source) {
        LOGGER.debug("findByUsernameAndSource({},{},{},{})", referenceType, referenceId, username, source);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(userRepository.findByUsernameAndSource(referenceType.name(), referenceId, username, source)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(z->RxJava2Adapter.singleToMono(completeUser(z))));
    }

    @Override
    public Maybe<User> findByExternalIdAndSource(ReferenceType referenceType, String referenceId, String externalId, String source) {
        LOGGER.debug("findByExternalIdAndSource({},{},{},{})", referenceType, referenceId, externalId, source);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(userRepository.findByExternalIdAndSource(referenceType.name(), referenceId, externalId, source)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(z->RxJava2Adapter.singleToMono(completeUser(z))));
    }

    @Override
    public Flowable<User> findByIdIn(List<String> ids) {
        LOGGER.debug("findByIdIn({})", ids);
        if (ids == null || ids.isEmpty()) {
            return RxJava2Adapter.fluxToFlowable(Flux.empty());
        }
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(userRepository.findByIdIn(ids)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)))
                .flatMap(user -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(completeUser(user)).flux()), CONCURRENT_FLATMAP);
    }

    @Override
    public Maybe<User> findById(ReferenceType referenceType, String referenceId, String userId) {
        LOGGER.debug("findById({},{},{})", referenceType, referenceId, userId);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(userRepository.findById(referenceType.name(), referenceId, userId)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(z->RxJava2Adapter.singleToMono(completeUser(z))));
    }

    @Override
    public Single<Long> countByReference(ReferenceType refType, String domain) {
        return userRepository.countByReference(refType.name(), domain);
    }

    @Override
    public Single<Long> countByApplication(String domain, String application) {
        return userRepository.countByClient(ReferenceType.DOMAIN.name(), domain, application);
    }

    @Override
    public Single<Map<Object, Object>> statistics(AnalyticsQuery query) {
        switch (query.getField()) {
            case Field.USER_STATUS:
                return usersStatusRepartition(query);
            case Field.USER_REGISTRATION:
                return registrationsStatusRepartition(query);
        }

        return RxJava2Adapter.monoToSingle(Mono.just(Collections.emptyMap()));
    }

    private Single<Map<Object, Object>> usersStatusRepartition(AnalyticsQuery query) {
        boolean filteringByApplication = query.getApplication() != null && !query.getApplication().isEmpty();

        Single<Long> total = filteringByApplication
                ? userRepository.countByClient(DOMAIN.name(), query.getDomain(), query.getApplication())
                : userRepository.countByReference(DOMAIN.name(), query.getDomain());
        Single<Long> disabled = filteringByApplication
                ? userRepository.countDisabledUserByClient(DOMAIN.name(), query.getDomain(), query.getApplication(), false)
                : userRepository.countDisabledUser(DOMAIN.name(), query.getDomain(), false);
        Single<Long> locked = filteringByApplication
                ? userRepository.countLockedUserByClient(DOMAIN.name(), query.getDomain(), query.getApplication(), false, LocalDateTime.now(UTC))
                : userRepository.countLockedUser(DOMAIN.name(), query.getDomain(), false, LocalDateTime.now(UTC));
        Single<Long> inactive = filteringByApplication
                ? userRepository.countInactiveUserByClient(DOMAIN.name(), query.getDomain(), query.getApplication(), LocalDateTime.now(UTC).minus(90, ChronoUnit.DAYS))
                : userRepository.countInactiveUser(DOMAIN.name(), query.getDomain(), LocalDateTime.now(UTC).minus(90, ChronoUnit.DAYS));

        return RxJava2Adapter.monoToSingle(Mono.just(new HashMap<>()).flatMap(stats->RxJava2Adapter.singleToMono(disabled).map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long count)->{
LOGGER.debug("usersStatusRepartition(disabled) = {}", count);
stats.put("disabled", count);
return stats;
}))).flatMap(stats->RxJava2Adapter.singleToMono(locked).map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long count)->{
LOGGER.debug("usersStatusRepartition(locked) = {}", count);
stats.put("locked", count);
return stats;
}))).flatMap(stats->RxJava2Adapter.singleToMono(inactive).map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long count)->{
LOGGER.debug("usersStatusRepartition(inactive) = {}", count);
stats.put("inactive", count);
return stats;
}))).flatMap(v->RxJava2Adapter.singleToMono((Single<Map<Object, Object>>)RxJavaReactorMigrationUtil.toJdkFunction((Function<HashMap<Object, Object>, Single<Map<Object, Object>>>)(stats) -> RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(total).map(RxJavaReactorMigrationUtil.toJdkFunction(count -> {
                    long value = count - (stats.values().stream().mapToLong(l -> (Long) l).sum());
                    stats.put("active", value);
                    LOGGER.debug("usersStatusRepartition(active) = {}", value);
                    return stats;
                })))).apply(v))));
    }

    private Single<Map<Object, Object>> registrationsStatusRepartition(AnalyticsQuery query) {
        LOGGER.debug("process statistic registrationsStatusRepartition({})", query);
        Single<Long> total = userRepository.countPreRegisteredUser(DOMAIN.name(), query.getDomain(), true);
        Single<Long> completed = userRepository.countRegistrationCompletedUser(DOMAIN.name(), query.getDomain(), true, true);
        return RxJava2Adapter.monoToSingle(Mono.just(new HashMap<>()).flatMap(stats->RxJava2Adapter.singleToMono(total).map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long count)->{
LOGGER.debug("registrationsStatusRepartition(total) = {}", count);
stats.put("total", count);
return stats;
}))).flatMap(v->RxJava2Adapter.singleToMono((Single<Map<Object, Object>>)RxJavaReactorMigrationUtil.toJdkFunction((Function<HashMap<Object, Object>, Single<Map<Object, Object>>>)(stats) -> RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(completed).map(RxJavaReactorMigrationUtil.toJdkFunction(count -> {
                    LOGGER.debug("registrationsStatusRepartition(completed) = {}", count);
                    stats.put("completed", count);
                    return stats;
                })))).apply(v))));
    }

    @Override
    public Maybe<User> findById(String id) {
        LOGGER.debug("findById({})", id);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(userRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(z->RxJava2Adapter.singleToMono(completeUser(z))));
    }

    @Override
    public Single<User> create(User item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("Create user with id {}", item.getId());

        TransactionalOperator trx = TransactionalOperator.create(tm);
        DatabaseClient.GenericInsertSpec<Map<String, Object>> insertSpec = dbClient.insert().into("users");

        // doesn't use the class introspection to handle json objects
        insertSpec = addQuotedField(insertSpec, "id", item.getId(), String.class);
        insertSpec = addQuotedField(insertSpec, "external_id", item.getExternalId(), String.class);
        insertSpec = addQuotedField(insertSpec, "username", item.getUsername(), String.class);
        insertSpec = addQuotedField(insertSpec, "email", item.getEmail(), String.class);
        insertSpec = addQuotedField(insertSpec, "display_name", item.getDisplayName(), String.class);
        insertSpec = addQuotedField(insertSpec, "nick_name", item.getNickName(), String.class);
        insertSpec = addQuotedField(insertSpec, "first_name", item.getFirstName(), String.class);
        insertSpec = addQuotedField(insertSpec, "last_name", item.getLastName(), String.class);
        insertSpec = addQuotedField(insertSpec, "title", item.getTitle(), String.class);
        insertSpec = addQuotedField(insertSpec, "type", item.getType(), String.class);
        insertSpec = addQuotedField(insertSpec, "preferred_language", item.getPreferredLanguage(), String.class);
        insertSpec = addQuotedField(insertSpec, "account_non_expired", item.isAccountNonExpired(), Boolean.class);
        insertSpec = addQuotedField(insertSpec, "account_locked_at", dateConverter.convertTo(item.getAccountLockedAt(), null), LocalDateTime.class);
        insertSpec = addQuotedField(insertSpec, "account_locked_until", dateConverter.convertTo(item.getAccountLockedUntil(), null), LocalDateTime.class);
        insertSpec = addQuotedField(insertSpec, "account_non_locked", item.isAccountNonLocked(), Boolean.class);
        insertSpec = addQuotedField(insertSpec, "credentials_non_expired", item.isCredentialsNonExpired(), Boolean.class);
        insertSpec = addQuotedField(insertSpec, "enabled", item.isEnabled(), Boolean.class);
        insertSpec = addQuotedField(insertSpec, "internal", item.isInternal(), Boolean.class);
        insertSpec = addQuotedField(insertSpec, "pre_registration", item.isPreRegistration(), Boolean.class);
        insertSpec = addQuotedField(insertSpec, "registration_completed", item.isRegistrationCompleted(), Boolean.class);
        insertSpec = addQuotedField(insertSpec, "newsletter", item.isNewsletter(), Boolean.class);
        insertSpec = addQuotedField(insertSpec, "registration_user_uri", item.getRegistrationUserUri(), String.class);
        insertSpec = addQuotedField(insertSpec, "registration_access_token", item.getRegistrationAccessToken(), String.class);
        insertSpec = addQuotedField(insertSpec, "reference_type", item.getReferenceType(), String.class);
        insertSpec = addQuotedField(insertSpec, "reference_id", item.getReferenceId(), String.class);
        insertSpec = addQuotedField(insertSpec, "source", item.getSource(), String.class);
        insertSpec = addQuotedField(insertSpec, "client", item.getClient(), String.class);
        insertSpec = addQuotedField(insertSpec, "logins_count", item.getLoginsCount(), Integer.class);
        insertSpec = addQuotedField(insertSpec, "logged_at", dateConverter.convertTo(item.getLoggedAt(), null), LocalDateTime.class);
        insertSpec = addQuotedField(insertSpec, "created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        insertSpec = addQuotedField(insertSpec, "updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);
        insertSpec = databaseDialectHelper.addJsonField(insertSpec, "x509_certificates", item.getX509Certificates());
        insertSpec = databaseDialectHelper.addJsonField(insertSpec, "factors", item.getFactors());
        insertSpec = databaseDialectHelper.addJsonField(insertSpec, "additional_information", item.getAdditionalInformation());

        Mono<Integer> insertAction = insertSpec.fetch().rowsUpdated();

        insertAction = persistChildEntities(insertAction, item);

        return RxJava2Adapter.monoToSingle(insertAction.as(trx::transactional).flatMap(i->RxJava2Adapter.maybeToMono(this.findById(item.getId())).single()));
    }

    @Override
    public Single<User> update(User item) {
        LOGGER.debug("Update User with id {}", item.getId());

        TransactionalOperator trx = TransactionalOperator.create(tm);
        final DatabaseClient.GenericUpdateSpec updateSpec = dbClient.update().table("users");
        // doesn't use the class introspection to handle json objects
        Map<SqlIdentifier, Object> updateFields = new HashMap<>();
        updateFields = addQuotedField(updateFields, "id", item.getId(), String.class);
        updateFields = addQuotedField(updateFields, "external_id", item.getExternalId(), String.class);
        updateFields = addQuotedField(updateFields, "username", item.getUsername(), String.class);
        updateFields = addQuotedField(updateFields, "email", item.getEmail(), String.class);
        updateFields = addQuotedField(updateFields, "display_name", item.getDisplayName(), String.class);
        updateFields = addQuotedField(updateFields, "nick_name", item.getNickName(), String.class);
        updateFields = addQuotedField(updateFields, "first_name", item.getFirstName(), String.class);
        updateFields = addQuotedField(updateFields, "last_name", item.getLastName(), String.class);
        updateFields = addQuotedField(updateFields, "title", item.getTitle(), String.class);
        updateFields = addQuotedField(updateFields, "type", item.getType(), String.class);
        updateFields = addQuotedField(updateFields, "preferred_language", item.getPreferredLanguage(), String.class);
        updateFields = addQuotedField(updateFields, "account_non_expired", item.isAccountNonExpired(), Boolean.class);
        updateFields = addQuotedField(updateFields, "account_locked_at", dateConverter.convertTo(item.getAccountLockedAt(), null), LocalDateTime.class);
        updateFields = addQuotedField(updateFields, "account_locked_until", dateConverter.convertTo(item.getAccountLockedUntil(), null), LocalDateTime.class);
        updateFields = addQuotedField(updateFields, "account_non_locked", item.isAccountNonLocked(), Boolean.class);
        updateFields = addQuotedField(updateFields, "credentials_non_expired", item.isCredentialsNonExpired(), Boolean.class);
        updateFields = addQuotedField(updateFields, "enabled", item.isEnabled(), Boolean.class);
        updateFields = addQuotedField(updateFields, "internal", item.isInternal(), Boolean.class);
        updateFields = addQuotedField(updateFields, "pre_registration", item.isPreRegistration(), Boolean.class);
        updateFields = addQuotedField(updateFields, "registration_completed", item.isRegistrationCompleted(), Boolean.class);
        updateFields = addQuotedField(updateFields, "newsletter", item.isNewsletter(), Boolean.class);
        updateFields = addQuotedField(updateFields, "registration_user_uri", item.getRegistrationUserUri(), String.class);
        updateFields = addQuotedField(updateFields, "registration_access_token", item.getRegistrationAccessToken(), String.class);
        updateFields = addQuotedField(updateFields, "reference_type", item.getReferenceType(), String.class);
        updateFields = addQuotedField(updateFields, "reference_id", item.getReferenceId(), String.class);
        updateFields = addQuotedField(updateFields, "source", item.getSource(), String.class);
        updateFields = addQuotedField(updateFields, "client", item.getClient(), String.class);
        updateFields = addQuotedField(updateFields, "logins_count", item.getLoginsCount(), Integer.class);
        updateFields = addQuotedField(updateFields, "logged_at", dateConverter.convertTo(item.getLoggedAt(), null), LocalDateTime.class);
        updateFields = addQuotedField(updateFields, "created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        updateFields = addQuotedField(updateFields, "updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);
        updateFields = databaseDialectHelper.addJsonField(updateFields, "x509_certificates", item.getX509Certificates());
        updateFields = databaseDialectHelper.addJsonField(updateFields, "factors", item.getFactors());
        updateFields = databaseDialectHelper.addJsonField(updateFields, "additional_information", item.getAdditionalInformation());

        Mono<Integer> updateAction = updateSpec.using(Update.from(updateFields)).matching(from(where("id").is(item.getId()))).fetch().rowsUpdated();

        updateAction = deleteChildEntities(item.getId()).then(updateAction);
        updateAction = persistChildEntities(updateAction, item);

        return RxJava2Adapter.monoToSingle(updateAction.as(trx::transactional).flatMap(i->RxJava2Adapter.maybeToMono(this.findById(item.getId())).single()));
    }

    @Override
    public Completable delete(String id) {
        LOGGER.debug("delete({})", id);
        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Integer> delete = dbClient.delete().from(JdbcUser.class).matching(from(where("id").is(id))).fetch().rowsUpdated();

        return monoToCompletable(delete.then(deleteChildEntities(id)).as(trx::transactional));
    }

    private Mono<Integer> persistChildEntities(Mono<Integer> actionFlow, User item) {
        final List<Address> addresses = item.getAddresses();
        if (addresses != null && !addresses.isEmpty()) {
            actionFlow = actionFlow.then(Flux.fromIterable(addresses).concatMap(address -> {
                JdbcUser.Address jdbcAddr = mapper.map(address, JdbcUser.Address.class);
                jdbcAddr.setUserId(item.getId());
                return dbClient.insert().into(JdbcUser.Address.class).using(jdbcAddr).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        final List<String> roles = item.getRoles();
        if (roles != null && !roles.isEmpty()) {
            actionFlow = actionFlow.then(Flux.fromIterable(roles).concatMap(role -> {
                JdbcUser.Role jdbcRole = new JdbcUser.Role();
                jdbcRole.setUserId(item.getId());
                jdbcRole.setRole(role);
                return dbClient.insert().into(JdbcUser.Role.class).using(jdbcRole).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        final List<String> entitlements = item.getEntitlements();
        if (entitlements != null && !entitlements.isEmpty()) {
            actionFlow = actionFlow.then(Flux.fromIterable(entitlements).concatMap(entitlement -> {
                JdbcUser.Entitlements jdbcEntitlement = new JdbcUser.Entitlements();
                jdbcEntitlement.setUserId(item.getId());
                jdbcEntitlement.setEntitlement(entitlement);
                return dbClient.insert().into(JdbcUser.Entitlements.class).using(jdbcEntitlement).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        Optional<Mono<Integer>> attributes = concat(concat(concat(convertAttributes(item, item.getEmails(), ATTRIBUTE_USER_FIELD_EMAIL),
                convertAttributes(item, item.getPhoneNumbers(), ATTRIBUTE_USER_FIELD_PHONE)),
                convertAttributes(item, item.getIms(), ATTRIBUTE_USER_FIELD_IM)),
                convertAttributes(item, item.getPhotos(), ATTRIBUTE_USER_FIELD_PHOTO))
                .map(jdbcAttr -> dbClient.insert().into(JdbcUser.Attribute.class).using(jdbcAttr).fetch().rowsUpdated())
                .reduce(Mono::then);
        if (attributes.isPresent()) {
            actionFlow = actionFlow.then(attributes.get());
        }

        return actionFlow;
    }

    private Stream<JdbcUser.Attribute> convertAttributes(User item, List<Attribute> attributes, String field) {
        if (attributes != null && !attributes.isEmpty()) {
            return attributes.stream().map(attr -> {
                JdbcUser.Attribute jdbcAttr = mapper.map(attr, JdbcUser.Attribute.class);
                jdbcAttr.setUserId(item.getId());
                jdbcAttr.setUserField(field);
                return jdbcAttr;
            });
        }
        return Stream.empty();
    }

    private Mono<Integer> deleteChildEntities(String userId) {
        Mono<Integer> deleteRoles = dbClient.delete().from(JdbcUser.Role.class).matching(from(where("user_id").is(userId))).fetch().rowsUpdated();
        Mono<Integer> deleteAddresses = dbClient.delete().from(JdbcUser.Address.class).matching(from(where("user_id").is(userId))).fetch().rowsUpdated();
        Mono<Integer> deleteAttributes = dbClient.delete().from(JdbcUser.Attribute.class).matching(from(where("user_id").is(userId))).fetch().rowsUpdated();
        Mono<Integer> deleteEntitlements = dbClient.delete().from(JdbcUser.Entitlements.class).matching(from(where("user_id").is(userId))).fetch().rowsUpdated();
        return deleteRoles.then(deleteAddresses).then(deleteAttributes).then(deleteEntitlements);
    }

    private Single<User> completeUser(User userToComplete) {
        return RxJava2Adapter.monoToSingle(Mono.just(userToComplete).flatMap(user->RxJava2Adapter.flowableToFlux(roleRepository.findByUserId(user.getId())).map(RxJavaReactorMigrationUtil.toJdkFunction(JdbcUser.Role::getRole)).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction((java.util.List<java.lang.String> roles)->{
user.setRoles(roles);
return user;
}))).flatMap(user->RxJava2Adapter.flowableToFlux(entitlementRepository.findByUserId(user.getId())).map(RxJavaReactorMigrationUtil.toJdkFunction(JdbcUser.Entitlements::getEntitlement)).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction((java.util.List<java.lang.String> entitlements)->{
user.setEntitlements(entitlements);
return user;
}))).flatMap(user->RxJava2Adapter.flowableToFlux(addressesRepository.findByUserId(user.getId())).map(RxJavaReactorMigrationUtil.toJdkFunction((io.gravitee.am.repository.jdbc.management.api.model.JdbcUser.Address jdbcAddr)->mapper.map(jdbcAddr, Address.class))).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction((java.util.List<io.gravitee.am.model.scim.Address> addresses)->{
user.setAddresses(addresses);
return user;
}))).flatMap(user->RxJava2Adapter.flowableToFlux(attributesRepository.findByUserId(user.getId())).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction((java.util.List<io.gravitee.am.repository.jdbc.management.api.model.JdbcUser.Attribute> attributes)->{
Map<String, List<Attribute>> map = attributes.stream().collect(StreamUtils.toMultiMap(JdbcUser.Attribute::getUserField, (io.gravitee.am.repository.jdbc.management.api.model.JdbcUser.Attribute attr)->mapper.map(attr, Attribute.class)));
if (map.containsKey(ATTRIBUTE_USER_FIELD_EMAIL)) {
user.setEmails(map.get(ATTRIBUTE_USER_FIELD_EMAIL));
}
if (map.containsKey(ATTRIBUTE_USER_FIELD_PHONE)) {
user.setPhoneNumbers(map.get(ATTRIBUTE_USER_FIELD_PHONE));
}
if (map.containsKey(ATTRIBUTE_USER_FIELD_PHOTO)) {
user.setPhotos(map.get(ATTRIBUTE_USER_FIELD_PHOTO));
}
if (map.containsKey(ATTRIBUTE_USER_FIELD_IM)) {
user.setIms(map.get(ATTRIBUTE_USER_FIELD_IM));
}
return user;
}))));
    }

}
