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
package io.gravitee.am.repository.mongodb.management;

import static com.mongodb.client.model.Filters.*;

import com.google.errorprone.annotations.InlineMe;
import com.mongodb.BasicDBObject;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.repository.management.api.RoleRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.RoleMongo;
import io.reactivex.*;


import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoRoleRepository extends AbstractManagementMongoRepository implements RoleRepository {

    private static final String FIELD_SCOPE = "scope";
    private static final String FIELD_ASSIGNABLE_TYPE = "assignableType";
    private MongoCollection<RoleMongo> rolesCollection;

    @PostConstruct
    public void init() {
        rolesCollection = mongoOperations.getCollection("roles", RoleMongo.class);
        super.init(rolesCollection);
        super.createIndex(rolesCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1));
        super.createIndex(rolesCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1).append(FIELD_NAME, 1).append(FIELD_SCOPE, 1));
    }

    
@Override
    public Flux<Role> findAll_migrated(ReferenceType referenceType, String referenceId) {
        return Flux.from(rolesCollection.find(and(eq(FIELD_REFERENCE_TYPE, referenceType == null ? null : referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId)))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    
@Override
    public Mono<Page<Role>> findAll_migrated(ReferenceType referenceType, String referenceId, int page, int size) {
        Single<Long> countOperation = RxJava2Adapter.fluxToObservable(Flux.from(rolesCollection.countDocuments(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId))))).first(0l);
        Single<List<Role>> rolesOperation = RxJava2Adapter.fluxToObservable(Flux.from(rolesCollection.find(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId))).sort(new BasicDBObject(FIELD_NAME, 1)).skip(size * page).limit(size))).map(this::convert).toList();
        return RxJava2Adapter.singleToMono(Single.zip(countOperation, rolesOperation, (count, roles) -> new Page<>(roles, page, count)));
    }

    
@Override
    public Mono<Page<Role>> search_migrated(ReferenceType referenceType, String referenceId, String query, int page, int size) {
        Bson searchQuery = new BasicDBObject(FIELD_NAME, query);

        // if query contains wildcard, use the regex query
        if (query.contains("*")) {
            String compactQuery = query.replaceAll("\\*+", ".*");
            String regex = "^" + compactQuery;
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            searchQuery = new BasicDBObject(FIELD_NAME, pattern);
        }

        Bson mongoQuery = and(
                eq(FIELD_REFERENCE_TYPE, referenceType.name()),
                eq(FIELD_REFERENCE_ID, referenceId),
                searchQuery);

        Single<Long> countOperation = RxJava2Adapter.fluxToObservable(Flux.from(rolesCollection.countDocuments(mongoQuery))).first(0l);
        Single<List<Role>> rolesOperation = RxJava2Adapter.fluxToObservable(Flux.from(rolesCollection.find(mongoQuery).skip(size * page).limit(size))).map(this::convert).toList();
        return RxJava2Adapter.singleToMono(Single.zip(countOperation, rolesOperation, (count, roles) -> new Page<>(roles, 0, count)));
    }

    
@Override
    public Flux<Role> findByIdIn_migrated(List<String> ids) {
        return Flux.from(rolesCollection.find(in(FIELD_ID, ids))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, role))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Role> findById(ReferenceType referenceType, String referenceId, String role) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, role));
}
@Override
    public Mono<Role> findById_migrated(ReferenceType referenceType, String referenceId, String role) {
        return Flux.from(rolesCollection.find(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId), eq(FIELD_ID, role))).first()).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(role))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Role> findById(String role) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(role));
}
@Override
    public Mono<Role> findById_migrated(String role) {
        return Flux.from(rolesCollection.find(eq(FIELD_ID, role)).first()).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Role> create(Role item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Role> create_migrated(Role item) {
        RoleMongo role = convert(item);
        role.setId(role.getId() == null ? RandomString.generate() : role.getId());
        return Mono.from(rolesCollection.insertOne(role)).flatMap(success->findById_migrated(role.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Role> update(Role item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Role> update_migrated(Role item) {
        RoleMongo role = convert(item);
        return Mono.from(rolesCollection.replaceOne(eq(FIELD_ID, role.getId()), role)).flatMap(updateResult->findById_migrated(role.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return Mono.from(rolesCollection.deleteOne(eq(FIELD_ID, id))).then();
    }

    
@Override
    public Mono<Role> findByNameAndAssignableType_migrated(ReferenceType referenceType, String referenceId, String name, ReferenceType assignableType) {
        return Flux.from(rolesCollection.find(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId), eq(FIELD_NAME, name), eq(FIELD_ASSIGNABLE_TYPE, assignableType.name()))).first()).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    
@Override
    public Flux<Role> findByNamesAndAssignableType_migrated(ReferenceType referenceType, String referenceId, List<String> names, ReferenceType assignableType) {
        return Flux.from(rolesCollection.find(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId), in(FIELD_NAME, names), eq(FIELD_ASSIGNABLE_TYPE, assignableType.name())))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    private Role convert(RoleMongo roleMongo) {
        if (roleMongo == null) {
            return null;
        }

        Role role = new Role();
        role.setId(roleMongo.getId());
        role.setName(roleMongo.getName());
        role.setDescription(roleMongo.getDescription());
        role.setReferenceType(roleMongo.getReferenceType() == null ? null : ReferenceType.valueOf(roleMongo.getReferenceType()));
        role.setReferenceId(roleMongo.getReferenceId());
        role.setAssignableType(roleMongo.getAssignableType() == null ? null : ReferenceType.valueOf(roleMongo.getAssignableType()));
        role.setSystem(roleMongo.isSystem());
        role.setDefaultRole(roleMongo.isDefaultRole());

        if (roleMongo.getPermissionAcls() != null) {
            Map<Permission, Set<Acl>> permissions = new HashMap<>();
            roleMongo.getPermissionAcls().forEach((key, value) -> {
                try {
                    permissions.put(Permission.valueOf(key), new HashSet<>(value));
                } catch (IllegalArgumentException iae) {
                    // Ignore invalid Permission enum.
                }
            });

            role.setPermissionAcls(permissions);
        }

        role.setOauthScopes(roleMongo.getOauthScopes());
        role.setCreatedAt(roleMongo.getCreatedAt());
        role.setUpdatedAt(roleMongo.getUpdatedAt());
        return role;
    }

    private RoleMongo convert(Role role) {
        if (role == null) {
            return null;
        }

        RoleMongo roleMongo = new RoleMongo();
        roleMongo.setId(role.getId());
        roleMongo.setName(role.getName());
        roleMongo.setDescription(role.getDescription());
        roleMongo.setReferenceType(role.getReferenceType() == null ? null : role.getReferenceType().name());
        roleMongo.setReferenceId(role.getReferenceId());
        roleMongo.setAssignableType(role.getAssignableType() == null ? null : role.getAssignableType().name());
        roleMongo.setSystem(role.isSystem());
        roleMongo.setDefaultRole(role.isDefaultRole());
        roleMongo.setPermissionAcls(role.getPermissionAcls() == null ? null : role.getPermissionAcls().entrySet().stream().collect(Collectors.toMap(o -> o.getKey().name(), Map.Entry::getValue)));
        roleMongo.setOauthScopes(role.getOauthScopes());
        roleMongo.setCreatedAt(role.getCreatedAt());
        roleMongo.setUpdatedAt(role.getUpdatedAt());
        return roleMongo;
    }
}
