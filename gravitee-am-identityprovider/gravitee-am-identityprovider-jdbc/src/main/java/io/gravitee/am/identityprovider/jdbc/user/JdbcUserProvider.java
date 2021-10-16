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
package io.gravitee.am.identityprovider.jdbc.user;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.oidc.StandardClaims;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.identityprovider.api.UserProvider;
import io.gravitee.am.identityprovider.api.encoding.BinaryToTextEncoder;
import io.gravitee.am.identityprovider.jdbc.JdbcAbstractProvider;
import io.gravitee.am.identityprovider.jdbc.user.spring.JdbcUserProviderConfiguration;
import io.gravitee.am.identityprovider.jdbc.utils.ColumnMapRowMapper;
import io.gravitee.am.identityprovider.jdbc.utils.ParametersUtils;
import io.gravitee.am.service.exception.UserAlreadyExistsException;
import io.gravitee.am.service.exception.UserNotFoundException;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Import(JdbcUserProviderConfiguration.class)
public class JdbcUserProvider extends JdbcAbstractProvider<UserProvider> implements UserProvider {

    private final Pattern pattern = Pattern.compile("idp_users___");

    @Autowired
    private BinaryToTextEncoder binaryToTextEncoder;

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (configuration.getAutoProvisioning()) {
            LOGGER.debug("Auto provisioning of identity provider table enabled");
            // for now simply get the file named <driver>.schema, more complex stuffs will be done if schema updates have to be done in the future
            final String sqlScript = "database/" + configuration.getProtocol() + ".schema";
            try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(sqlScript);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

                List<String> sqlStatements = reader.lines()
                        // remove empty line and comment
                        .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("--"))
                        .map(line -> {
                            // update table & index names
                            String finalLine = pattern.matcher(line).replaceAll(configuration.getUsersTable());
                            LOGGER.debug("Statement to execute: {}", finalLine);
                            return finalLine;
                        })
                        .distinct()
                        .collect(Collectors.toList());

                LOGGER.debug("Found {} statements to execute", sqlStatements.size());

                RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(tableExists(configuration.getProtocol(), configuration.getUsersTable())))
                        .flatMapSingle(statement -> RxJava2Adapter.fluxToFlowable(query_migrated(statement, new Object[0]).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(Result::getRowsUpdated)))
                                .first(0))).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(total -> {
                                    if (total == 0) {
                                        return RxJava2Adapter.fluxToFlowable(Flux.fromIterable(sqlStatements))
                                                .flatMapSingle(statement -> RxJava2Adapter.fluxToFlowable(query_migrated(statement, new Object[0]).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(Result::getRowsUpdated)))
                                                        .first(0));
                                    } else {
                                        return RxJava2Adapter.fluxToFlowable(Flux.empty());
                                    }
                                })))
                        .doOnError(error -> LOGGER.error("Unable to initialize Database", error))
                        .subscribe();

            } catch (Exception e) {
                LOGGER.error("Unable to initialize the identity provider schema", e);
            }
        }
    }

    private String tableExists(String protocol, String table) {
        if ("sqlserver".equalsIgnoreCase(protocol)) {
            return "SELECT 1 FROM sysobjects WHERE name = '"+table+"' AND xtype = 'U'";
        } else {
            return "SELECT 1 FROM information_schema.tables WHERE table_name = '"+table+"'";
        }
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByEmail_migrated(email))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<User> findByEmail(String email) {
 return RxJava2Adapter.monoToMaybe(findByEmail_migrated(email));
}
@Override
    public Mono<User> findByEmail_migrated(String email) {
        return selectUserByEmail_migrated(email).map(RxJavaReactorMigrationUtil.toJdkFunction(this::createUser));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.selectUserByEmail_migrated(email))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Maybe<Map<String, Object>> selectUserByEmail(String email) {
 return RxJava2Adapter.monoToMaybe(selectUserByEmail_migrated(email));
}
private Mono<Map<String,Object>> selectUserByEmail_migrated(String email) {
        final String sql = String.format(configuration.getSelectUserByEmailQuery(), getIndexParameter(1, configuration.getEmailAttribute()));
        return query_migrated(sql, email).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(result -> result.map(ColumnMapRowMapper::mapRow))).next();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByUsername_migrated(username))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<User> findByUsername(String username) {
 return RxJava2Adapter.monoToMaybe(findByUsername_migrated(username));
}
@Override
    public Mono<User> findByUsername_migrated(String username) {
        return selectUserByUsername_migrated(username).map(RxJavaReactorMigrationUtil.toJdkFunction(this::createUser));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> create(User user) {
 return RxJava2Adapter.monoToSingle(create_migrated(user));
}
@Override
    public Mono<User> create_migrated(User user) {
        // set technical id
        ((DefaultUser)user).setId(user.getId() != null ? user.getId() : RandomString.generate());

       return RxJava2Adapter.singleToMono(Single.fromPublisher(connectionPool.create())).flatMap(v->RxJava2Adapter.singleToMono((Single<User>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Connection, Single<User>>)cnx -> {
                    return RxJava2Adapter.monoToSingle(selectUserByUsername_migrated(cnx, user.getUsername()).hasElement().flatMap(x->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Boolean, SingleSource<io.gravitee.am.identityprovider.api.User>>toJdkFunction(isEmpty -> {
                                if (!isEmpty) {
                                    return RxJava2Adapter.monoToSingle(Mono.error(new UserAlreadyExistsException(user.getUsername())));
                                } else {
                                    String sql;
                                    Object[] args;
                                    if (configuration.isUseDedicatedSalt()) {
                                        sql = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (%s, %s, %s, %s, %s, %s)",
                                                configuration.getUsersTable(),
                                                configuration.getIdentifierAttribute(),
                                                configuration.getUsernameAttribute(),
                                                configuration.getPasswordAttribute(),
                                                configuration.getPasswordSaltAttribute(),
                                                configuration.getEmailAttribute(),
                                                configuration.getMetadataAttribute(),
                                                getIndexParameter(1, configuration.getIdentifierAttribute()),
                                                getIndexParameter(2, configuration.getUsernameAttribute()),
                                                getIndexParameter(3, configuration.getPasswordAttribute()),
                                                getIndexParameter(4, configuration.getPasswordSaltAttribute()),
                                                getIndexParameter(5, configuration.getEmailAttribute()),
                                                getIndexParameter(6, configuration.getMetadataAttribute()));

                                        args = new Object[6];
                                        byte[] salt = createSalt();
                                        args[0] = user.getId();
                                        args[1] = user.getUsername();
                                        args[2] = user.getCredentials() != null ? passwordEncoder.encode(user.getCredentials(), salt) : null;
                                        args[3] = user.getCredentials() != null ? binaryToTextEncoder.encode(salt) : null;
                                        args[4] = user.getEmail();
                                        args[5] = user.getAdditionalInformation() != null ? objectMapper.writeValueAsString(user.getAdditionalInformation()) : null;
                                    } else {
                                        sql = String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (%s, %s, %s, %s, %s)",
                                                configuration.getUsersTable(),
                                                configuration.getIdentifierAttribute(),
                                                configuration.getUsernameAttribute(),
                                                configuration.getPasswordAttribute(),
                                                configuration.getEmailAttribute(),
                                                configuration.getMetadataAttribute(),
                                                getIndexParameter(1, configuration.getIdentifierAttribute()),
                                                getIndexParameter(2, configuration.getUsernameAttribute()),
                                                getIndexParameter(3, configuration.getPasswordAttribute()),
                                                getIndexParameter(4, configuration.getEmailAttribute()),
                                                getIndexParameter(5, configuration.getMetadataAttribute()));

                                        args = new Object[5];
                                        args[0] = user.getId();
                                        args[1] = user.getUsername();
                                        args[2] = user.getCredentials() != null ? passwordEncoder.encode(user.getCredentials()) : null;
                                        args[3] = user.getEmail();
                                        args[4] = user.getAdditionalInformation() != null ? objectMapper.writeValueAsString(user.getAdditionalInformation()) : null;
                                    }

                                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToFlowable(query_migrated(cnx, sql, args).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(Result::getRowsUpdated)))
                                            .first(0)).map(RxJavaReactorMigrationUtil.toJdkFunction(result -> user)));
                                }
                            }).apply(x))))).doFinally(() -> RxJava2Adapter.monoToCompletable(Mono.from(cnx.close())).subscribe());
                }).apply(v)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.selectUserByUsername_migrated(cnx, username))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Maybe<Map<String, Object>> selectUserByUsername(Connection cnx, String username) {
 return RxJava2Adapter.monoToMaybe(selectUserByUsername_migrated(cnx, username));
}
private Mono<Map<String,Object>> selectUserByUsername_migrated(Connection cnx, String username) {
        final String sql = String.format(configuration.getSelectUserByUsernameQuery(), getIndexParameter(1, configuration.getUsernameAttribute()));
        return query_migrated(cnx, sql, username).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(result -> result.map(ColumnMapRowMapper::mapRow))).next();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(id, updateUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> update(String id, User updateUser) {
 return RxJava2Adapter.monoToSingle(update_migrated(id, updateUser));
}
@Override
    public Mono<User> update_migrated(String id, User updateUser) {
        final String sql;
        final Object[] args;
        final String metadata = convert(updateUser.getAdditionalInformation());

        if (updateUser.getCredentials() != null) {
            if (configuration.isUseDedicatedSalt()) {
                args = new Object[4];
                sql = String.format("UPDATE %s SET %s = %s, %s = %s, %s = %s WHERE %s = %s",
                        configuration.getUsersTable(),
                        configuration.getPasswordAttribute(),
                        getIndexParameter(1, configuration.getPasswordAttribute()),
                        configuration.getPasswordSaltAttribute(),
                        getIndexParameter(2, configuration.getPasswordSaltAttribute()),
                        configuration.getMetadataAttribute(),
                        getIndexParameter(3, configuration.getMetadataAttribute()),
                        configuration.getIdentifierAttribute(),
                        getIndexParameter(4, configuration.getIdentifierAttribute()));
                byte[] salt = createSalt();
                args[0] = passwordEncoder.encode(updateUser.getCredentials(), salt);
                args[1] = binaryToTextEncoder.encode(salt);
                args[2] = metadata;
                args[3] = id;
            } else {
                args = new Object[3];
                sql = String.format("UPDATE %s SET %s = %s, %s = %s WHERE %s = %s",
                        configuration.getUsersTable(),
                        configuration.getPasswordAttribute(),
                        getIndexParameter(1, configuration.getPasswordAttribute()),
                        configuration.getMetadataAttribute(),
                        getIndexParameter(2, configuration.getMetadataAttribute()),
                        configuration.getIdentifierAttribute(),
                        getIndexParameter(3, configuration.getIdentifierAttribute()));
                args[0] = passwordEncoder.encode(updateUser.getCredentials());
                args[1] = metadata;
                args[2] = id;
            }
        } else {
            args = new Object[2];
            sql = String.format("UPDATE %s SET %s = %s WHERE %s = %s",
                    configuration.getUsersTable(),
                    configuration.getMetadataAttribute(),
                    getIndexParameter(1, configuration.getMetadataAttribute()),
                    configuration.getIdentifierAttribute(),
                    getIndexParameter(2, configuration.getIdentifierAttribute()));
            args[0] = metadata;
            args[1] = id;
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToFlowable(query_migrated(sql, args).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(Result::getRowsUpdated)))
                .first(0)).flatMap(v->RxJava2Adapter.singleToMono((Single<User>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Integer, Single<User>>)rowsUpdated -> {
                    if (rowsUpdated == 0) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new UserNotFoundException(id)));
                    }
                    ((DefaultUser) updateUser).setId(id);
                    return RxJava2Adapter.monoToSingle(Mono.just(updateUser));
                }).apply(v)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        final String sql = String.format("DELETE FROM %s where %s = %s",
                configuration.getUsersTable(),
                configuration.getIdentifierAttribute(),
                getIndexParameter(1, configuration.getIdentifierAttribute()));

        return query_migrated(sql, id).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(Result::getRowsUpdated)).flatMap(y->RxJava2Adapter.completableToMono(RxJavaReactorMigrationUtil.toJdkFunction((Function<Integer, Completable>)rowsUpdated -> {
                    if (rowsUpdated == 0) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(new UserNotFoundException(id)));
                    }
                    return RxJava2Adapter.monoToCompletable(Mono.empty());
                }).apply(y))).then();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.selectUserByUsername_migrated(username))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Maybe<Map<String, Object>> selectUserByUsername(String username) {
 return RxJava2Adapter.monoToMaybe(selectUserByUsername_migrated(username));
}
private Mono<Map<String,Object>> selectUserByUsername_migrated(String username) {
        final String sql = String.format(configuration.getSelectUserByUsernameQuery(), getIndexParameter(1, configuration.getUsernameAttribute()));
        return query_migrated(sql, username).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(result -> result.map(ColumnMapRowMapper::mapRow))).next();
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.query_migrated(connection, sql, args))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Flowable<Result> query(Connection connection, String sql, Object... args) {
 return RxJava2Adapter.fluxToFlowable(query_migrated(connection, sql, args));
}
private Flux<Result> query_migrated(Connection connection, String sql, Object... args) {
        Statement statement = connection.createStatement(sql);
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            bind(statement, i, arg, arg != null ? arg.getClass() : String.class);
        }
        return Flux.from(statement.execute());
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.query_migrated(sql, args))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Flowable<Result> query(String sql, Object... args) {
 return RxJava2Adapter.fluxToFlowable(query_migrated(sql, args));
}
private Flux<Result> query_migrated(String sql, Object... args) {
        return RxJava2Adapter.singleToMono(Single.fromPublisher(connectionPool.create())).flux().flatMap(RxJavaReactorMigrationUtil.toJdkFunction(connection ->
                        RxJava2Adapter.fluxToFlowable(query_migrated(connection, sql, args))
                                .doFinally(() -> RxJava2Adapter.monoToCompletable(Mono.from(connection.close())).subscribe())));
    }

    private User createUser(Map<String, Object> claims) {
        // get username
        String username = claims.get(configuration.getUsernameAttribute()).toString();
        // get sub
        String id = claims.get(configuration.getIdentifierAttribute()).toString();
        // get encrypted password
        String password = claims.get(configuration.getPasswordAttribute()) != null ? claims.get(configuration.getPasswordAttribute()).toString() : null;
        // compute metadata
        computeMetadata(claims);

        // create the user
        DefaultUser user = new DefaultUser(username);
        user.setId(id);
        user.setCredentials(password);
        // additional claims
        Map<String, Object> additionalInformation = new HashMap<>(claims);
        claims.put(StandardClaims.SUB, id);
        claims.put(StandardClaims.PREFERRED_USERNAME, username);
        // remove reserved claims
        additionalInformation.remove(configuration.getIdentifierAttribute());
        additionalInformation.remove(configuration.getUsernameAttribute());
        additionalInformation.remove(configuration.getPasswordAttribute());
        additionalInformation.remove(configuration.getMetadataAttribute());
        if (configuration.isUseDedicatedSalt()) {
            additionalInformation.remove(configuration.getPasswordSaltAttribute());
        }
        user.setAdditionalInformation(additionalInformation);

        return user;
    }

    private void bind(Statement statement, int index, Object value, Class type) {
        if (value != null) {
            statement.bind(index, value);
        } else {
            statement.bindNull(index, type);
        }
    }

    private String getIndexParameter(int index, String field) {
        return ParametersUtils.getIndexParameter(configuration.getProtocol(), index, field);
    }

    private String convert(Map<String, Object> claims) {
        if (claims == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(claims);
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] createSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[configuration.getPasswordSaltLength()];
        random.nextBytes(salt);
        return salt;
    }
}
