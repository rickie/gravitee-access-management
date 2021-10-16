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
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.Email;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.management.api.EmailRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.EmailMongo;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
import javax.annotation.PostConstruct;
import org.bson.Document;
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
public class MongoEmailRepository extends AbstractManagementMongoRepository implements EmailRepository {

    private static final String FIELD_TEMPLATE = "template";
    private MongoCollection<EmailMongo> emailsCollection;

    @PostConstruct
    public void init() {
        emailsCollection = mongoOperations.getCollection("emails", EmailMongo.class);
        super.init(emailsCollection);
        super.createIndex(emailsCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1));
        super.createIndex(emailsCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1).append(FIELD_TEMPLATE, 1));
        super.createIndex(emailsCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1).append(FIELD_CLIENT, 1).append(FIELD_TEMPLATE, 1));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Email> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<Email> findAll_migrated() {
        return Flux.from(emailsCollection.find()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Email> findAll(ReferenceType referenceType, String referenceId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
@Override
    public Flux<Email> findAll_migrated(ReferenceType referenceType, String referenceId) {
        return Flux.from(emailsCollection.find(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId)))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByClient_migrated(referenceType, referenceId, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Email> findByClient(ReferenceType referenceType, String referenceId, String client) {
 return RxJava2Adapter.fluxToFlowable(findByClient_migrated(referenceType, referenceId, client));
}
@Override
    public Flux<Email> findByClient_migrated(ReferenceType referenceType, String referenceId, String client) {
        return Flux.from(emailsCollection.find(
                        and(
                                eq(FIELD_REFERENCE_TYPE, referenceType.name()),
                                eq(FIELD_REFERENCE_ID, referenceId),
                                eq(FIELD_CLIENT, client))
                )).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByTemplate_migrated(referenceType, referenceId, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findByTemplate(ReferenceType referenceType, String referenceId, String template) {
 return RxJava2Adapter.monoToMaybe(findByTemplate_migrated(referenceType, referenceId, template));
}
@Override
    public Mono<Email> findByTemplate_migrated(ReferenceType referenceType, String referenceId, String template) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(
                emailsCollection.find(
                        and(
                                eq(FIELD_REFERENCE_TYPE, referenceType.name()),
                                eq(FIELD_REFERENCE_ID, referenceId),
                                eq(FIELD_TEMPLATE, template),
                                exists(FIELD_CLIENT, false)))
                        .first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndTemplate_migrated(domain, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findByDomainAndTemplate(String domain, String template) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndTemplate_migrated(domain, template));
}
@Override
    public Mono<Email> findByDomainAndTemplate_migrated(String domain, String template) {
        return findByTemplate_migrated(ReferenceType.DOMAIN, domain, template);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByClientAndTemplate_migrated(referenceType, referenceId, client, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findByClientAndTemplate(ReferenceType referenceType, String referenceId, String client, String template) {
 return RxJava2Adapter.monoToMaybe(findByClientAndTemplate_migrated(referenceType, referenceId, client, template));
}
@Override
    public Mono<Email> findByClientAndTemplate_migrated(ReferenceType referenceType, String referenceId, String client, String template) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(
                emailsCollection.find(
                        and(
                                eq(FIELD_REFERENCE_TYPE, referenceType.name()),
                                eq(FIELD_REFERENCE_ID, referenceId),
                                eq(FIELD_CLIENT, client),
                                eq(FIELD_TEMPLATE, template)))
                        .first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientAndTemplate_migrated(domain, client, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findByDomainAndClientAndTemplate(String domain, String client, String template) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndClientAndTemplate_migrated(domain, client, template));
}
@Override
    public Mono<Email> findByDomainAndClientAndTemplate_migrated(String domain, String client, String template) {
        return findByClientAndTemplate_migrated(ReferenceType.DOMAIN, domain, client, template);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findById(ReferenceType referenceType, String referenceId, String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, id));
}
@Override
    public Mono<Email> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(emailsCollection.find(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId), eq(FIELD_ID, id))).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Email> findById_migrated(String id) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(emailsCollection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Email> create(Email item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Email> create_migrated(Email item) {
        EmailMongo email = convert(item);
        email.setId(email.getId() == null ? RandomString.generate() : email.getId());
        return RxJava2Adapter.singleToMono(Single.fromPublisher(emailsCollection.insertOne(email))).flatMap(success->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(email.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Email> update(Email item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Email> update_migrated(Email item) {
        EmailMongo email = convert(item);
        return RxJava2Adapter.singleToMono(Single.fromPublisher(emailsCollection.replaceOne(eq(FIELD_ID, email.getId()), email))).flatMap(updateResult->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(email.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return Mono.from(emailsCollection.deleteOne(eq(FIELD_ID, id))).then();
    }

    private Email convert(EmailMongo emailMongo) {
        if (emailMongo == null) {
            return null;
        }
        Email email = new Email();
        email.setId(emailMongo.getId());
        email.setEnabled(emailMongo.isEnabled());
        email.setReferenceType(emailMongo.getReferenceType());
        email.setReferenceId(emailMongo.getReferenceId());
        email.setClient(emailMongo.getClient());
        email.setTemplate(emailMongo.getTemplate());
        email.setFrom(emailMongo.getFrom());
        email.setFromName(emailMongo.getFromName());
        email.setSubject(emailMongo.getSubject());
        email.setContent(emailMongo.getContent());
        email.setExpiresAfter(emailMongo.getExpiresAfter());
        email.setCreatedAt(emailMongo.getCreatedAt());
        email.setUpdatedAt(emailMongo.getUpdatedAt());
        return email;
    }

    private EmailMongo convert(Email email) {
        if (email == null) {
            return null;
        }

        EmailMongo emailMongo = new EmailMongo();
        emailMongo.setId(email.getId());
        emailMongo.setEnabled(email.isEnabled());
        emailMongo.setReferenceType(email.getReferenceType());
        emailMongo.setReferenceId(email.getReferenceId());
        emailMongo.setClient(email.getClient());
        emailMongo.setTemplate(email.getTemplate());
        emailMongo.setFrom(email.getFrom());
        emailMongo.setFromName(email.getFromName());
        emailMongo.setSubject(email.getSubject());
        emailMongo.setContent(email.getContent());
        emailMongo.setExpiresAfter(email.getExpiresAfter());
        emailMongo.setCreatedAt(email.getCreatedAt());
        emailMongo.setUpdatedAt(email.getUpdatedAt());
        return emailMongo;
    }

}
