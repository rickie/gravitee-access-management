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
package io.gravitee.am.identityprovider.mongo.user;

import static com.mongodb.client.model.Filters.eq;

import com.google.errorprone.annotations.InlineMe;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.oidc.StandardClaims;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.identityprovider.api.UserProvider;
import io.gravitee.am.identityprovider.api.encoding.BinaryToTextEncoder;
import io.gravitee.am.identityprovider.mongo.MongoIdentityProviderConfiguration;
import io.gravitee.am.identityprovider.mongo.authentication.spring.MongoAuthenticationProviderConfiguration;
import io.gravitee.am.service.authentication.crypto.password.PasswordEncoder;
import io.gravitee.am.service.exception.UserAlreadyExistsException;
import io.gravitee.am.service.exception.UserNotFoundException;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.bson.BsonDocument;
import org.bson.Document;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Import({MongoAuthenticationProviderConfiguration.class})
public class MongoUserProvider implements UserProvider, InitializingBean {

    private static final String FIELD_ID = "_id";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_UPDATED_AT = "updatedAt";

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BinaryToTextEncoder binaryToTextEncoder;

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private MongoIdentityProviderConfiguration configuration;

    private MongoCollection<Document> usersCollection;

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByEmail_migrated(email))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<User> findByEmail(String email) {
 return RxJava2Adapter.monoToMaybe(findByEmail_migrated(email));
}
@Override
    public Mono<User> findByEmail_migrated(String email) {
        String rawQuery = this.configuration.getFindUserByEmailQuery().replaceAll("\\?", email);
        String jsonQuery = convertToJsonString(rawQuery);
        BsonDocument query = BsonDocument.parse(jsonQuery);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(usersCollection.find(query).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByUsername_migrated(username))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<User> findByUsername(String username) {
 return RxJava2Adapter.monoToMaybe(findByUsername_migrated(username));
}
@Override
    public Mono<User> findByUsername_migrated(String username) {
        // lowercase username since case-sensitivity feature
        final String encodedUsername = username.toLowerCase();

        String rawQuery = this.configuration.getFindUserByUsernameQuery().replaceAll("\\?", encodedUsername);
        String jsonQuery = convertToJsonString(rawQuery);
        BsonDocument query = BsonDocument.parse(jsonQuery);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(usersCollection.find(query).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> create(User user) {
 return RxJava2Adapter.monoToSingle(create_migrated(user));
}
@Override
    public Mono<User> create_migrated(User user) {
        // lowercase username to avoid duplicate account
        final String username = user.getUsername().toLowerCase();

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(findByUsername(username)).hasElement().flatMap(v->RxJava2Adapter.singleToMono((Single<User>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Boolean, Single<User>>)isEmpty -> {
                    if (!isEmpty) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new UserAlreadyExistsException(user.getUsername())));
                    } else {
                        Document document = new Document();
                        // set technical id
                        document.put(FIELD_ID, user.getId() != null ? user.getId() : RandomString.generate());
                        // set username
                        document.put(configuration.getUsernameField(), username);
                        // set password
                        if (user.getCredentials() != null) {
                            if (configuration.isUseDedicatedSalt()) {
                                byte[] salt = createSalt();
                                document.put(configuration.getPasswordField(), passwordEncoder.encode(user.getCredentials(), salt));
                                document.put(configuration.getPasswordSaltAttribute(), binaryToTextEncoder.encode(salt));
                            } else {
                                document.put(configuration.getPasswordField(), passwordEncoder.encode(user.getCredentials()));
                            }
                        }
                        // set additional information
                        if (user.getAdditionalInformation() != null) {
                            document.putAll(user.getAdditionalInformation());
                        }
                        // set date fields
                        document.put(FIELD_CREATED_AT, new Date());
                        document.put(FIELD_UPDATED_AT, document.get(FIELD_CREATED_AT));
                        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(usersCollection.insertOne(document))).flatMap(success->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(document.getString(FIELD_ID)))).single()));
                    }
                }).apply(v)))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(id, updateUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> update(String id, User updateUser) {
 return RxJava2Adapter.monoToSingle(update_migrated(id, updateUser));
}
@Override
    public Mono<User> update_migrated(String id, User updateUser) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(id))).switchIfEmpty(Mono.error(new UserNotFoundException(id))))
                .flatMapSingle(oldUser -> {
                    Document document = new Document();
                    // set username (keep the original value)
                    document.put(configuration.getUsernameField(), oldUser.getUsername());
                    // set password
                    if (updateUser.getCredentials() != null) {
                        if (configuration.isUseDedicatedSalt()) {
                            byte[] salt = createSalt();
                            document.put(configuration.getPasswordField(), passwordEncoder.encode(updateUser.getCredentials(), salt));
                            document.put(configuration.getPasswordSaltAttribute(), binaryToTextEncoder.encode(salt));
                        } else {
                            document.put(configuration.getPasswordField(), passwordEncoder.encode(updateUser.getCredentials()));
                        }
                    } else {
                        document.put(configuration.getPasswordField(), oldUser.getCredentials());
                    }
                    // set additional information
                    if (updateUser.getAdditionalInformation() != null) {
                        document.putAll(updateUser.getAdditionalInformation());
                    }
                    // set date fields
                    document.put(FIELD_CREATED_AT, oldUser.getCreatedAt());
                    document.put(FIELD_UPDATED_AT, new Date());
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(usersCollection.replaceOne(eq(FIELD_ID, oldUser.getId()), document))).flatMap(updateResult->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(oldUser.getId()))).single()));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(id))).switchIfEmpty(Mono.error(new UserNotFoundException(id))).flatMap(idpUser->Mono.from(usersCollection.deleteOne(eq(FIELD_ID, id)))).then()));
    }

    @Override
    public void afterPropertiesSet() {
        // init users collection
        usersCollection = this.mongoClient.getDatabase(this.configuration.getDatabase()).getCollection(this.configuration.getUsersCollection());
        // create index on username field
        Observable.fromPublisher(usersCollection.createIndex(new Document(configuration.getUsernameField(), 1))).subscribe();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Maybe<User> findById(String userId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(userId));
}
private Mono<User> findById_migrated(String userId) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(usersCollection.find(eq(FIELD_ID, userId)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    private User convert(Document document) {
        String username = document.getString(configuration.getUsernameField());
        DefaultUser user = new DefaultUser(username);
        user.setId(document.getString(FIELD_ID));
        user.setCredentials(document.getString(configuration.getPasswordField()));
        user.setCreatedAt(document.get(FIELD_CREATED_AT, Date.class));
        user.setUpdatedAt(document.get(FIELD_UPDATED_AT, Date.class));
        // additional claims
        Map<String, Object> claims = new HashMap<>();
        claims.put(StandardClaims.SUB, document.getString(FIELD_ID));
        claims.put(StandardClaims.PREFERRED_USERNAME, username);
        // remove reserved claims
        document.remove(FIELD_ID);
        document.remove(configuration.getUsernameField());
        document.remove(configuration.getPasswordField());
        document.entrySet().forEach(entry -> claims.put(entry.getKey(), entry.getValue()));
        user.setAdditionalInformation(claims);
        return user;
    }

    private String convertToJsonString(String rawString) {
        rawString = rawString.replaceAll("[^\\{\\}\\[\\],:]+", "\"$0\"").replaceAll("\\s+","");
        return rawString;
    }

    private byte[] createSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[configuration.getPasswordSaltLength()];
        random.nextBytes(salt);
        return salt;
    }
}
