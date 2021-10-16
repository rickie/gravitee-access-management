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

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.audit.EventType;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.Tag;
import io.gravitee.am.repository.management.api.TagRepository;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.DomainService;
import io.gravitee.am.service.TagService;
import io.gravitee.am.service.exception.AbstractManagementException;
import io.gravitee.am.service.exception.TagAlreadyExistsException;
import io.gravitee.am.service.exception.TagNotFoundException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.NewTag;
import io.gravitee.am.service.model.UpdateTag;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.TagAuditBuilder;
import io.reactivex.*;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.text.Normalizer;
import java.util.Date;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class TagServiceImpl implements TagService {

    private final Logger LOGGER = LoggerFactory.getLogger(TagServiceImpl.class);

    @Lazy
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private DomainService domainService;

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id, organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Tag> findById(String id, String organizationId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id, organizationId));
}
@Override
    public Mono<Tag> findById_migrated(String id, String organizationId) {
        LOGGER.debug("Find tag by ID: {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(tagRepository.findById_migrated(id, organizationId))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a tag using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a tag using its ID: %s", id), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Tag> findAll(String organizationId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(organizationId));
}
@Override
    public Flux<Tag> findAll_migrated(String organizationId) {
        LOGGER.debug("Find all tags");
        return tagRepository.findAll_migrated(organizationId).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find all tags", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find all tags", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(newTag, organizationId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Tag> create(NewTag newTag, String organizationId, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(newTag, organizationId, principal));
}
@Override
    public Mono<Tag> create_migrated(NewTag newTag, String organizationId, User principal) {
        LOGGER.debug("Create a new tag: {}", newTag);
        String id = humanReadableId(newTag.getName());

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(tagRepository.findById_migrated(id, organizationId).hasElement().flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Boolean, SingleSource<Tag>>toJdkFunction(empty -> {
                    if (!empty) {
                        throw new TagAlreadyExistsException(newTag.getName());
                    } else {
                        Tag tag = new Tag();
                        tag.setId(id);
                        tag.setOrganizationId(organizationId);
                        tag.setName(newTag.getName());
                        tag.setDescription(newTag.getDescription());
                        tag.setCreatedAt(new Date());
                        tag.setUpdatedAt(tag.getCreatedAt());
                        return RxJava2Adapter.monoToSingle(tagRepository.create_migrated(tag));
                    }
                }).apply(v)))))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Tag>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to create a tag", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a tag", ex)));
                }).apply(err))))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(tag -> auditService.report(AuditBuilder.builder(TagAuditBuilder.class).tag(tag).principal(principal).type(EventType.TAG_CREATED)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(TagAuditBuilder.class).referenceId(organizationId).principal(principal).type(EventType.TAG_CREATED).throwable(throwable))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(tagId, organizationId, updateTag, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Tag> update(String tagId, String organizationId, UpdateTag updateTag, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(tagId, organizationId, updateTag, principal));
}
@Override
    public Mono<Tag> update_migrated(String tagId, String organizationId, UpdateTag updateTag, User principal) {
        LOGGER.debug("Update an existing tag: {}", updateTag);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(tagRepository.findById_migrated(tagId, organizationId).switchIfEmpty(Mono.error(new TagNotFoundException(tagId))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Tag, SingleSource<Tag>>toJdkFunction(oldTag -> {
                    Tag tag = new Tag();
                    tag.setId(tagId);
                    tag.setName(updateTag.getName());
                    tag.setDescription(updateTag.getDescription());
                    tag.setCreatedAt(oldTag.getCreatedAt());
                    tag.setUpdatedAt(new Date());

                    return RxJava2Adapter.monoToSingle(tagRepository.update_migrated(tag).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(tag1 -> auditService.report(AuditBuilder.builder(TagAuditBuilder.class).principal(principal).type(EventType.TAG_UPDATED).tag(tag1).oldValue(oldTag)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(TagAuditBuilder.class).principal(principal).type(EventType.TAG_UPDATED).throwable(throwable)))));
                }).apply(y)))))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Tag>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to update a tag", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a tag", ex)));
                }).apply(err)))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(tagId, orgaizationId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String tagId, String orgaizationId, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(tagId, orgaizationId, principal));
}
@Override
    public Mono<Void> delete_migrated(String tagId, String orgaizationId, User principal) {
        LOGGER.debug("Delete tag {}", tagId);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(tagRepository.findById_migrated(tagId, orgaizationId).switchIfEmpty(Mono.error(new TagNotFoundException(tagId))).flatMap(tag->RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(tagRepository.delete_migrated(tagId).then(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToSingle(domainService.findAll_migrated()).flatMapObservable(Observable::fromIterable).flatMapCompletable((io.gravitee.am.model.Domain domain)->{
if (domain.getTags() != null) {
domain.getTags().remove(tagId);
return RxJava2Adapter.monoToSingle(domainService.update_migrated(domain.getId(), domain)).toCompletable();
}
return RxJava2Adapter.monoToCompletable(Mono.empty());
})))).doOnComplete(()->auditService.report(AuditBuilder.builder(TagAuditBuilder.class).principal(principal).type(EventType.TAG_DELETED).tag(tag)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((java.lang.Throwable throwable)->auditService.report(AuditBuilder.builder(TagAuditBuilder.class).principal(principal).type(EventType.TAG_DELETED).throwable(throwable))))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to delete tag {}", tagId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException("An error occurs while trying to delete tag " + tagId, ex)));
                }));
    }

    private String humanReadableId(String domainName) {
        String nfdNormalizedString = Normalizer.normalize(domainName, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        domainName = pattern.matcher(nfdNormalizedString).replaceAll("");
        return domainName.toLowerCase().trim().replaceAll("\\s{1,}", "-");
    }
}
