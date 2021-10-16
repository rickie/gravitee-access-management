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
package io.gravitee.am.service.impl;

import static io.gravitee.am.service.utils.BackendConfigurationUtils.getMongoDatabaseName;

import com.google.common.io.BaseEncoding;
import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.audit.EventType;
import io.gravitee.am.common.event.Action;
import io.gravitee.am.common.event.Type;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Reporter;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.repository.management.api.ReporterRepository;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.DomainService;
import io.gravitee.am.service.EventService;
import io.gravitee.am.service.ReporterService;
import io.gravitee.am.service.exception.AbstractManagementException;
import io.gravitee.am.service.exception.ReporterConfigurationException;
import io.gravitee.am.service.exception.ReporterNotFoundException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.NewReporter;
import io.gravitee.am.service.model.UpdateReporter;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.ReporterAuditBuilder;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class ReporterServiceImpl implements ReporterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReporterServiceImpl.class);
    public static final int TABLE_SUFFIX_MAX_LENGTH = 30;
    public static final String REPORTER_AM_JDBC = "reporter-am-jdbc";
    public static final String REPORTER_AM_FILE= "reporter-am-file";
    public static final String REPORTER_CONFIG_FILENAME = "filename";
    public static final String ADMIN_DOMAIN = "admin";

    @Autowired
    private Environment environment;

    @Lazy
    @Autowired
    private ReporterRepository reporterRepository;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private DomainService domainService;

    @Deprecated
@Override
    public Flowable<Reporter> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<Reporter> findAll_migrated() {
        LOGGER.debug("Find all reporters");
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(reporterRepository.findAll()).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find all reporter", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find all reporters", ex)));
                }))));
    }

    @Deprecated
@Override
    public Flowable<Reporter> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<Reporter> findByDomain_migrated(String domain) {
        LOGGER.debug("Find reporters by domain: {}", domain);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(reporterRepository.findByDomain(domain)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find reporters by domain: {}", domain, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(String.format("An error occurs while trying to find reporters by domain: %s", domain), ex)));
                }))));
    }

    @Deprecated
@Override
    public Maybe<Reporter> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Reporter> findById_migrated(String id) {
        LOGGER.debug("Find reporter by id: {}", id);
        return RxJava2Adapter.maybeToMono(reporterRepository.findById(id)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find reporters by id: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(String.format("An error occurs while trying to find reporters by id: %s", id), ex)));
                }));
    }

    @Deprecated
@Override
    public Single<Reporter> createDefault(String domain) {
 return RxJava2Adapter.monoToSingle(createDefault_migrated(domain));
}
@Override
    public Mono<Reporter> createDefault_migrated(String domain) {
        LOGGER.debug("Create default reporter for domain {}", domain);
        NewReporter newReporter = createInternal(domain);
        if (newReporter == null) {
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.error(new ReporterNotFoundException("Reporter type " + this.environment.getProperty("management.type") + " not found"))));
        }
        return RxJava2Adapter.singleToMono(create(domain, newReporter));
    }

    @Override
    public NewReporter createInternal(String domain) {
        NewReporter newReporter = null;
        if (useMongoReporter()) {
            newReporter = createMongoReporter(domain);
        } else if (useJdbcReporter()) {
            newReporter = createJdbcReporter(domain);
        }
        return newReporter;
    }

    @Deprecated
@Override
    public Single<Reporter> create(String domain, NewReporter newReporter, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, newReporter, principal));
}
@Override
    public Mono<Reporter> create_migrated(String domain, NewReporter newReporter, User principal) {
        LOGGER.debug("Create a new reporter {} for domain {}", newReporter, domain);

        Reporter reporter = new Reporter();
        reporter.setId(newReporter.getId() == null ? RandomString.generate() : newReporter.getId());
        reporter.setEnabled(newReporter.isEnabled());
        reporter.setDomain(domain);
        reporter.setName(newReporter.getName());
        reporter.setType(newReporter.getType());
        // currently only audit logs
        reporter.setDataType("AUDIT");
        reporter.setConfiguration(newReporter.getConfiguration());
        reporter.setCreatedAt(new Date());
        reporter.setUpdatedAt(reporter.getCreatedAt());

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(checkReporterConfiguration(reporter)).flatMap(ignore->RxJava2Adapter.singleToMono(reporterRepository.create(reporter))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Reporter, SingleSource<Reporter>>toJdkFunction(reporter1 -> {
                    // create event for sync process
                    Event event = new Event(Type.REPORTER, new Payload(reporter1.getId(), ReferenceType.DOMAIN, reporter1.getDomain(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(__->Mono.just(reporter1)));
                }).apply(v)))))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to create a reporter", ex);
                    String message = "An error occurs while trying to create a reporter. ";
                    if (ex instanceof ReporterConfigurationException) {
                        message += ex.getMessage();
                    }
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(message, ex)));
                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(reporter1 -> auditService.report(AuditBuilder.builder(ReporterAuditBuilder.class).principal(principal).type(EventType.REPORTER_CREATED).reporter(reporter1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ReporterAuditBuilder.class).principal(principal).type(EventType.REPORTER_CREATED).throwable(throwable))))));
    }


    @Deprecated
@Override
    public Single<Reporter> update(String domain, String id, UpdateReporter updateReporter, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateReporter, principal));
}
@Override
    public Mono<Reporter> update_migrated(String domain, String id, UpdateReporter updateReporter, User principal) {
        LOGGER.debug("Update a reporter {} for domain {}", id, domain);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(reporterRepository.findById(id)).switchIfEmpty(Mono.error(new ReporterNotFoundException(id))))
                .flatMapSingle(oldReporter -> {
                    Reporter reporterToUpdate = new Reporter(oldReporter);
                    reporterToUpdate.setEnabled(updateReporter.isEnabled());
                    reporterToUpdate.setName(updateReporter.getName());
                    reporterToUpdate.setConfiguration(updateReporter.getConfiguration());
                    reporterToUpdate.setUpdatedAt(new Date());

                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(checkReporterConfiguration(reporterToUpdate)).flatMap(ignore->RxJava2Adapter.singleToMono(reporterRepository.update(reporterToUpdate)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Reporter, SingleSource<Reporter>>toJdkFunction((io.gravitee.am.model.Reporter reporter1)->{
if (!ADMIN_DOMAIN.equals(domain)) {
Event event = new Event(Type.REPORTER, new Payload(reporter1.getId(), ReferenceType.DOMAIN, reporter1.getDomain(), Action.UPDATE));
return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(__->Mono.just(reporter1)));
} else {
return RxJava2Adapter.monoToSingle(Mono.just(reporter1));
}
}).apply(v))))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(reporter1 -> auditService.report(AuditBuilder.builder(ReporterAuditBuilder.class).principal(principal).type(EventType.REPORTER_UPDATED).oldValue(oldReporter).reporter(reporter1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ReporterAuditBuilder.class).principal(principal).type(EventType.REPORTER_UPDATED).throwable(throwable)))));
                })
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to update a reporter", ex);
                    String message = "An error occurs while trying to update a reporter. ";
                    if (ex instanceof ReporterConfigurationException) {
                        message += ex.getMessage();
                    }
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(message, ex)));
                }));
    }

    @Deprecated
@Override
    public Completable delete(String reporterId, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(reporterId, principal));
}
@Override
    public Mono<Void> delete_migrated(String reporterId, User principal) {
        LOGGER.debug("Delete reporter {}", reporterId);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(reporterRepository.findById(reporterId)).switchIfEmpty(Mono.error(new ReporterNotFoundException(reporterId))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Reporter, CompletableSource>)reporter -> {
                    // create event for sync process
                    Event event = new Event(Type.REPORTER, new Payload(reporterId, ReferenceType.DOMAIN, reporter.getDomain(), Action.DELETE));
                    return RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(reporterRepository.delete(reporterId)).then(RxJava2Adapter.singleToMono(eventService.create(event))))
                            .toCompletable()
                            .doOnComplete(() -> auditService.report(AuditBuilder.builder(ReporterAuditBuilder.class).principal(principal).type(EventType.REPORTER_DELETED).reporter(reporter)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ReporterAuditBuilder.class).principal(principal).type(EventType.REPORTER_DELETED).throwable(throwable)))));
                }).apply(y)))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to delete reporter: {}", reporterId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete reporter: %s", reporterId), ex)));
                }));
    }

    /**
     * This method check if the configuration attribute of a Reporter is valid
     *
     * @param reporter to check
     * @return
     */
    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.checkReporterConfiguration_migrated(reporter))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Reporter> checkReporterConfiguration(Reporter reporter) {
 return RxJava2Adapter.monoToSingle(checkReporterConfiguration_migrated(reporter));
}
private Mono<Reporter> checkReporterConfiguration_migrated(Reporter reporter) {
        Single<Reporter> result = RxJava2Adapter.monoToSingle(Mono.just(reporter));

        if (REPORTER_AM_FILE.equalsIgnoreCase(reporter.getType())) {
            // for FileReporter we have to check if the filename isn't used by another reporter
            final JsonObject configuration = (JsonObject) Json.decodeValue(reporter.getConfiguration());
            final String reporterId = reporter.getId();

            result = RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(reporterRepository.findByDomain(reporter.getDomain())).filter(RxJavaReactorMigrationUtil.toJdkPredicate(r -> r.getType().equalsIgnoreCase(REPORTER_AM_FILE))).filter(RxJavaReactorMigrationUtil.toJdkPredicate(r -> reporterId == null || !r.getId().equals(reporterId))).map(RxJavaReactorMigrationUtil.toJdkFunction(r -> (JsonObject) Json.decodeValue(r.getConfiguration()))).filter(RxJavaReactorMigrationUtil.toJdkPredicate(cfg ->
                            cfg.containsKey(REPORTER_CONFIG_FILENAME) &&
                                    cfg.getString(REPORTER_CONFIG_FILENAME).equals(configuration.getString(REPORTER_CONFIG_FILENAME)))))
                    .count()).flatMap(v->RxJava2Adapter.singleToMono((Single<Reporter>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Long, Single<Reporter>>)reporters -> {
                        if (reporters > 0) {
                            // more than one reporter use the same filename
                            return RxJava2Adapter.monoToSingle(Mono.error(new ReporterConfigurationException("Filename already defined")));
                        } else {
                            return RxJava2Adapter.monoToSingle(Mono.just(reporter));
                        }
                    }).apply(v))));
        }

        return RxJava2Adapter.singleToMono(result);
    }

    private NewReporter createMongoReporter(String domain) {
        Optional<String> mongoServers = getMongoServers(environment);
        String mongoHost = null;
        String mongoPort = null;
        if (!mongoServers.isPresent()) {
            mongoHost = environment.getProperty("management.mongodb.host", "localhost");
            mongoPort = environment.getProperty("management.mongodb.port", "27017");
        }

        final String username = environment.getProperty("management.mongodb.username");
        final String password = environment.getProperty("management.mongodb.password");
        String mongoDBName = getMongoDatabaseName(environment);

        String defaultMongoUri = "mongodb://";
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            defaultMongoUri += username +":"+ password +"@";
        }
        defaultMongoUri += mongoServers.orElse(mongoHost+":"+mongoPort) + "/" + mongoDBName;
        String mongoUri = environment.getProperty("management.mongodb.uri", addOptionsToURI(environment, defaultMongoUri));

        NewReporter newReporter = new NewReporter();
        newReporter.setId(RandomString.generate());
        newReporter.setEnabled(true);
        newReporter.setName("MongoDB Reporter");
        newReporter.setType("mongodb");
        newReporter.setConfiguration("{\"uri\":\"" + mongoUri + ((mongoHost != null) ? "\",\"host\":\"" + mongoHost : "") + "\",\"port\":" + mongoPort + ",\"enableCredentials\":false,\"database\":\"" + mongoDBName + "\",\"reportableCollection\":\"reporter_audits" + (domain != null ? "_" + domain : "") + "\",\"bulkActions\":1000,\"flushInterval\":5}");

        return newReporter;
    }

    private NewReporter createJdbcReporter(String domain) {
        String jdbcHost = environment.getProperty("management.jdbc.host");
        String jdbcPort = environment.getProperty("management.jdbc.port");
        String jdbcDatabase = environment.getProperty("management.jdbc.database");
        String jdbcDriver = environment.getProperty("management.jdbc.driver");
        String jdbcUser = environment.getProperty("management.jdbc.username");
        String jdbcPwd = environment.getProperty("management.jdbc.password");

        // dash are forbidden in table name, replace them in domainName by underscore
        String tableSuffix = null;
        if (domain != null) {
            tableSuffix = domain.replaceAll("-", "_");
            if (tableSuffix.length() > TABLE_SUFFIX_MAX_LENGTH) {
                try {
                    LOGGER.info("Table name 'reporter_audits_access_points_{}' will be too long, compute shortest unique name", tableSuffix);
                    byte[] hash = MessageDigest.getInstance("sha-256").digest(tableSuffix.getBytes());
                    tableSuffix = BaseEncoding.base16().encode(hash).substring(0, 30).toLowerCase();
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalStateException("Unable to compute digest of '" + domain + "' due to unknown sha-256 algorithm", e);
                }
            }
        }

        NewReporter newReporter = new NewReporter();
        newReporter.setId(RandomString.generate());
        newReporter.setEnabled(true);
        newReporter.setName("JDBC Reporter");
        newReporter.setType(REPORTER_AM_JDBC);
        newReporter.setConfiguration("{\"host\":\"" + jdbcHost + "\"," +
                "\"port\":" + jdbcPort + "," +
                "\"database\":\"" + jdbcDatabase + "\"," +
                "\"driver\":\"" + jdbcDriver + "\"," +
                "\"username\":\"" + jdbcUser+ "\"," +
                "\"password\":"+ (jdbcPwd == null ? null : "\"" + jdbcPwd + "\"") + "," +
                "\"tableSuffix\":\"" + (tableSuffix != null ? tableSuffix : "") + "\"," +
                "\"initialSize\":0," +
                "\"maxSize\":10," +
                "\"maxIdleTime\":30000," +
                "\"maxLifeTime\":30000," +
                "\"bulkActions\":1000," +
                "\"flushInterval\":5}");

        return newReporter;
    }

    private String addOptionsToURI(Environment environment, String mongoUri) {
        Integer connectTimeout = environment.getProperty("management.mongodb.connectTimeout", Integer.class, 1000);
        Integer socketTimeout = environment.getProperty("management.mongodb.socketTimeout", Integer.class, 1000);
        Integer maxConnectionIdleTime = environment.getProperty("management.mongodb.maxConnectionIdleTime", Integer.class);
        Integer heartbeatFrequency = environment.getProperty("management.mongodb.heartbeatFrequency", Integer.class);
        Boolean sslEnabled = environment.getProperty("management.mongodb.sslEnabled", Boolean.class);
        String authSource = environment.getProperty("management.mongodb.authSource", String.class);

        mongoUri += "?connectTimeoutMS="+connectTimeout+"&socketTimeoutMS="+socketTimeout;
        if (authSource != null) {
            mongoUri += "&authSource="+authSource;
        }
        if (maxConnectionIdleTime != null) {
            mongoUri += "&maxIdleTimeMS="+maxConnectionIdleTime;
        }
        if (heartbeatFrequency != null) {
            mongoUri += "&heartbeatFrequencyMS="+heartbeatFrequency;
        }
        if (sslEnabled != null) {
            mongoUri += "&ssl="+sslEnabled;
        }

        return mongoUri;
    }

    private Optional<String> getMongoServers(Environment env) {
        LOGGER.debug("Looking for MongoDB server configuration...");
        boolean found = true;
        int idx = 0;
        List<String> endpoints = new ArrayList<>();

        while (found) {
            String serverHost = env.getProperty("management.mongodb.servers[" + (idx++) + "].host");
            int serverPort = env.getProperty("management.mongodb.servers[" + (idx++) + "].port", int.class, 27017);
            found = (serverHost != null);
            if (found) {
                endpoints.add(serverHost+":"+serverPort);
            }
        }
        return endpoints.isEmpty() ? Optional.empty() : Optional.of(endpoints.stream().collect(Collectors.joining(",")));
    }

    private boolean useMongoReporter() {
        String managementBackend = this.environment.getProperty("management.type", "mongodb");
        return "mongodb".equalsIgnoreCase(managementBackend);
    }

    private boolean useJdbcReporter() {
        String managementBackend = this.environment.getProperty("management.type", "mongodb");
        return "jdbc".equalsIgnoreCase(managementBackend);
    }
}
