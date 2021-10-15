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
package io.gravitee.am.identityprovider.mongo.authentication;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.exception.authentication.BadCredentialsException;
import io.gravitee.am.common.exception.authentication.UsernameNotFoundException;
import io.gravitee.am.common.oidc.StandardClaims;
import io.gravitee.am.identityprovider.api.*;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.identityprovider.mongo.MongoIdentityProviderConfiguration;
import io.gravitee.am.identityprovider.mongo.authentication.spring.MongoAuthenticationProviderConfiguration;
import io.gravitee.am.service.authentication.crypto.password.PasswordEncoder;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Import({MongoAuthenticationProviderConfiguration.class})
public class MongoAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoAuthenticationProvider.class);
    private static final String FIELD_ID = "_id";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_UPDATED_AT = "updatedAt";

    @Autowired
    private IdentityProviderMapper mapper;

    @Autowired
    private IdentityProviderRoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MongoIdentityProviderConfiguration configuration;

    @Autowired
    private MongoClient mongoClient;

    public Maybe<User> loadUserByUsername(Authentication authentication) {
        String username = ((String) authentication.getPrincipal()).toLowerCase();
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.singleToMono(findUserByMultipleField(username)
                .toList()).flatMapMany(RxJavaReactorMigrationUtil.toJdkFunction(users -> {
                    if (users.isEmpty()) {
                        return Flowable.error(new UsernameNotFoundException(username));
                    }
                    return Flowable.fromIterable(users);
                })).filter(RxJavaReactorMigrationUtil.toJdkPredicate(user -> {
                    String password = user.getString(this.configuration.getPasswordField());
                    String presentedPassword = authentication.getCredentials().toString();

                    if (password == null) {
                        LOGGER.debug("Authentication failed: password is null");
                        return false;
                    }

                    if (configuration.isUseDedicatedSalt()) {
                        String hash = user.getString(configuration.getPasswordSaltAttribute());
                        if (!passwordEncoder.matches(presentedPassword, password, hash)) {
                            LOGGER.debug("Authentication failed: password does not match stored value");
                            return false;
                        }
                    } else {
                        if (!passwordEncoder.matches(presentedPassword, password)) {
                            LOGGER.debug("Authentication failed: password does not match stored value");
                            return false;
                        }
                    }

                    return true;
                })).collectList().flatMap(e->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<List<Document>, MaybeSource<User>>)users -> {
                    if (users.isEmpty()) {
                        return RxJava2Adapter.monoToMaybe(Mono.error(new BadCredentialsException("Bad credentials")));
                    }
                    if (users.size() > 1) {
                        return RxJava2Adapter.monoToMaybe(Mono.error(new BadCredentialsException("Bad credentials")));
                    }
                    return RxJava2Adapter.monoToMaybe(Mono.just(this.createUser(authentication.getContext(), users.get(0))));
                }).apply(e)))));
    }

    private Flowable<Document> findUserByMultipleField(String value) {
        MongoCollection<Document> usersCol = this.mongoClient.getDatabase(this.configuration.getDatabase()).getCollection(this.configuration.getUsersCollection());
        String findQuery = this.configuration.getFindUserByMultipleFieldsQuery() != null ? this.configuration.getFindUserByMultipleFieldsQuery() : this.configuration.getFindUserByUsernameQuery();
        String rawQuery = findQuery.replaceAll("\\?", value);
        String jsonQuery = convertToJsonString(rawQuery);
        BsonDocument query = BsonDocument.parse(jsonQuery);
        return RxJava2Adapter.fluxToFlowable(Flux.from(usersCol.find(query)));
    }

    public Maybe<User> loadUserByUsername(String username) {
        final String encodedUsername = username.toLowerCase();
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(findUserByUsername(encodedUsername)).map(RxJavaReactorMigrationUtil.toJdkFunction(document -> createUser(new SimpleAuthenticationContext(), document))));
    }

    private Maybe<Document> findUserByUsername(String username) {
        MongoCollection<Document> usersCol = this.mongoClient.getDatabase(this.configuration.getDatabase()).getCollection(this.configuration.getUsersCollection());
        String rawQuery = this.configuration.getFindUserByUsernameQuery().replaceAll("\\?", username);
        String jsonQuery = convertToJsonString(rawQuery);
        BsonDocument query = BsonDocument.parse(jsonQuery);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(usersCol.find(query).first()), BackpressureStrategy.BUFFER).next());
    }

    private User createUser(AuthenticationContext authContext, Document document) {
        String username = document.getString(FIELD_USERNAME);
        DefaultUser user = new DefaultUser(username);
        Map<String, Object> claims = new HashMap<>();

        String sub = document.containsKey(FIELD_ID) ?
                document.get(FIELD_ID) instanceof ObjectId ? ((ObjectId) document.get(FIELD_ID)).toString() : document.getString(FIELD_ID)
                : username;
        // set technical id
        user.setId(sub);

        // set user roles
        user.setRoles(getUserRoles(authContext, document));

        // set claims
        claims.put(StandardClaims.SUB, sub);
        claims.put(StandardClaims.PREFERRED_USERNAME, username);
        if (this.mapper != null
                && this.mapper.getMappers() != null
                && !this.mapper.getMappers().isEmpty()) {
            claims.putAll(this.mapper.apply(authContext, document));
        } else {
            // default claims
            // remove reserved claims
            document.remove(FIELD_ID);
            document.remove(FIELD_USERNAME);
            document.remove(configuration.getPasswordField());
            if (configuration.isUseDedicatedSalt()) {
                document.remove(configuration.getPasswordSaltAttribute());
            }
            document.remove(FIELD_CREATED_AT);
            if (document.containsKey(FIELD_UPDATED_AT)) {
                document.put(StandardClaims.UPDATED_AT, document.get(FIELD_UPDATED_AT));
                document.remove(FIELD_UPDATED_AT);
            }
            document.entrySet().forEach(entry -> claims.put(entry.getKey(), entry.getValue()));
        }
        user.setAdditionalInformation(claims);

        return user;
    }

    private String convertToJsonString(String rawString) {
        rawString = rawString.replaceAll("[^\\{\\}\\[\\],:\\s]+", "\"$0\"").replaceAll("\\s+","");
        return rawString;
    }

    private List<String> getUserRoles(AuthenticationContext context, Document document) {
        if (roleMapper != null) {
            Map<String, Object> profile = new HashMap<>();
            document.forEach(profile::put);
            return roleMapper.apply(context, profile);
        }
        return new ArrayList<>();
    }
}
