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
import io.gravitee.am.model.Application;
import io.gravitee.am.model.User;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.oauth2.Scope;
import io.gravitee.am.model.uma.Resource;
import io.gravitee.am.model.uma.policy.AccessPolicy;
import io.gravitee.am.model.uma.policy.AccessPolicyType;
import io.gravitee.am.repository.management.api.AccessPolicyRepository;
import io.gravitee.am.repository.management.api.ResourceRepository;
import io.gravitee.am.service.ApplicationService;
import io.gravitee.am.service.ResourceService;
import io.gravitee.am.service.ScopeService;
import io.gravitee.am.service.UserService;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.model.NewResource;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
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
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class ResourceServiceImpl implements ResourceService {

    private final Logger LOGGER = LoggerFactory.getLogger(ResourceServiceImpl.class);

    @Lazy
    @Autowired
    private ResourceRepository repository;

    @Lazy
    @Autowired
    private AccessPolicyRepository accessPolicyRepository;

    @Autowired
    private ScopeService scopeService;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationService applicationService;

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Resource>> findByDomain(String domain, int page, int size) {
 return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
@Override
    public Mono<Page<Resource>> findByDomain_migrated(String domain, int page, int size) {
        LOGGER.debug("Listing resource for domain {}", domain);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.findByDomain_migrated(domain, page, size))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find resources by domain {}", domain, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find resources by domain %s", domain), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomainAndClient_migrated(domain, client, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Resource>> findByDomainAndClient(String domain, String client, int page, int size) {
 return RxJava2Adapter.monoToSingle(findByDomainAndClient_migrated(domain, client, page, size));
}
@Override
    public Mono<Page<Resource>> findByDomainAndClient_migrated(String domain, String client, int page, int size) {
        LOGGER.debug("Listing resource set for domain {} and client {}", domain, client);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.findByDomainAndClient_migrated(domain, client, page, size))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find resources by domain {} and client {}", domain, client, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find resources by domain %s and client %s", domain, client), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByResources_migrated(resourceIds))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Resource> findByResources(List<String> resourceIds) {
 return RxJava2Adapter.fluxToFlowable(findByResources_migrated(resourceIds));
}
@Override
    public Flux<Resource> findByResources_migrated(List<String> resourceIds) {
        LOGGER.debug("Listing resources by ids {}", resourceIds);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(repository.findByResources_migrated(resourceIds)));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.listByDomainAndClientAndUser_migrated(domain, client, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Resource> listByDomainAndClientAndUser(String domain, String client, String userId) {
 return RxJava2Adapter.fluxToFlowable(listByDomainAndClientAndUser_migrated(domain, client, userId));
}
@Override
    public Flux<Resource> listByDomainAndClientAndUser_migrated(String domain, String client, String userId) {
        LOGGER.debug("Listing resource for resource owner {} and client {}", userId, client);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(repository.findByDomainAndClientAndUser_migrated(domain, client, userId)));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndClientAndResources_migrated(domain, client, resourceIds))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Resource> findByDomainAndClientAndResources(String domain, String client, List<String> resourceIds) {
 return RxJava2Adapter.fluxToFlowable(findByDomainAndClientAndResources_migrated(domain, client, resourceIds));
}
@Override
    public Flux<Resource> findByDomainAndClientAndResources_migrated(String domain, String client, List<String> resourceIds) {
        LOGGER.debug("Getting resource {} for  client {} and resources {}", resourceIds, client, resourceIds);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(repository.findByDomainAndClientAndResources_migrated(domain, client, resourceIds)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientAndUserAndResource_migrated(domain, client, userId, resourceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Resource> findByDomainAndClientAndUserAndResource(String domain, String client, String userId, String resourceId) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndClientAndUserAndResource_migrated(domain, client, userId, resourceId));
}
@Override
    public Mono<Resource> findByDomainAndClientAndUserAndResource_migrated(String domain, String client, String userId, String resourceId) {
        LOGGER.debug("Getting resource by resource owner {} and client {} and resource {}", userId, client, resourceId);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(repository.findByDomainAndClientAndUserAndResource_migrated(domain, client, userId, resourceId)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientResource_migrated(domain, client, resourceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Resource> findByDomainAndClientResource(String domain, String client, String resourceId) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndClientResource_migrated(domain, client, resourceId));
}
@Override
    public Mono<Resource> findByDomainAndClientResource_migrated(String domain, String client, String resourceId) {
        LOGGER.debug("Getting resource by domain {} client {} and resource {}", domain, client, resourceId);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.flowableToFlux(this.findByDomainAndClientAndResources(domain, client, Arrays.asList(resourceId))).next()));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getMetadata_migrated(resources))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Map<String, Map<String, Object>>> getMetadata(List<Resource> resources) {
 return RxJava2Adapter.monoToSingle(getMetadata_migrated(resources));
}
@Override
    public Mono<Map<String,Map<String,Object>>> getMetadata_migrated(List<Resource> resources) {
        if (resources == null || resources.isEmpty()) {
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(Collections.emptyMap())));
        }

        List<String> userIds = resources.stream().filter(resource -> resource.getUserId() != null).map(Resource::getUserId).distinct().collect(Collectors.toList());
        List<String> appIds = resources.stream().filter(resource -> resource.getClientId() != null).map(Resource::getClientId).distinct().collect(Collectors.toList());
        return RxJava2Adapter.singleToMono(Single.zip(RxJava2Adapter.fluxToFlowable(userService.findByIdIn_migrated(userIds)).toMap(User::getId, this::filter),
                RxJava2Adapter.fluxToFlowable(applicationService.findByIdIn_migrated(appIds)).toMap(Application::getId, this::filter), (users, apps) -> {
            Map<String, Map<String, Object>> metadata = new HashMap<>();
            metadata.put("users", (Map) users);
            metadata.put("applications", (Map) apps);
            return metadata;
        }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(newResource, domain, client, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Resource> create(NewResource newResource, String domain, String client, String userId) {
 return RxJava2Adapter.monoToSingle(create_migrated(newResource, domain, client, userId));
}
@Override
    public Mono<Resource> create_migrated(NewResource newResource, String domain, String client, String userId) {
        LOGGER.debug("Creating resource for resource owner {} and client {}", userId, client);
        Resource toCreate = new Resource();
        toCreate.setResourceScopes(newResource.getResourceScopes())
                .setDescription(newResource.getDescription())
                .setIconUri(newResource.getIconUri())
                .setName(newResource.getName())
                .setType(newResource.getType())
                .setDomain(domain)
                .setClientId(client)
                .setUserId(userId)
                .setCreatedAt(new Date())
                .setUpdatedAt(toCreate.getCreatedAt());

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(this.validateScopes_migrated(toCreate))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Resource, SingleSource<Resource>>toJdkFunction((io.gravitee.am.model.uma.Resource ident) -> RxJava2Adapter.monoToSingle(validateIconUri_migrated(ident))).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Resource, SingleSource<Resource>>toJdkFunction((Resource ident) -> RxJava2Adapter.monoToSingle(repository.create_migrated(ident))).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono((Single<Resource>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Resource, Single<Resource>>)r -> {
                    AccessPolicy accessPolicy = new AccessPolicy();
                    accessPolicy.setName("Deny all");
                    accessPolicy.setDescription("Default deny access policy. Created by Gravitee.io.");
                    accessPolicy.setType(AccessPolicyType.GROOVY);
                    accessPolicy.setCondition("{\"onRequestScript\":\"import io.gravitee.policy.groovy.PolicyResult.State\\nresult.state = State.FAILURE;\"}");
                    accessPolicy.setEnabled(true);
                    accessPolicy.setDomain(domain);
                    accessPolicy.setResource(r.getId());
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(accessPolicyRepository.create_migrated(accessPolicy))).map(RxJavaReactorMigrationUtil.toJdkFunction(__ -> r)));
                }).apply(v)))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(newResource, domain, client, userId, resourceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Resource> update(NewResource newResource, String domain, String client, String userId, String resourceId) {
 return RxJava2Adapter.monoToSingle(update_migrated(newResource, domain, client, userId, resourceId));
}
@Override
    public Mono<Resource> update_migrated(NewResource newResource, String domain, String client, String userId, String resourceId) {
        LOGGER.debug("Updating resource id {} for resource owner {} and client {}", resourceId, userId, client);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserAndResource(domain, client, userId, resourceId)).switchIfEmpty(Mono.error(new ResourceNotFoundException(resourceId))))
                .flatMapSingle(Single::just)).map(RxJavaReactorMigrationUtil.toJdkFunction(newResource::update)).map(RxJavaReactorMigrationUtil.toJdkFunction(toUpdate -> toUpdate.setUpdatedAt(new Date()))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Resource, SingleSource<Resource>>toJdkFunction((io.gravitee.am.model.uma.Resource ident) -> RxJava2Adapter.monoToSingle(validateScopes_migrated(ident))).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Resource, SingleSource<Resource>>toJdkFunction((io.gravitee.am.model.uma.Resource ident) -> RxJava2Adapter.monoToSingle(validateIconUri_migrated(ident))).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono((Single<Resource>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Resource, Single<Resource>>)(Resource ident) -> RxJava2Adapter.monoToSingle(repository.update_migrated(ident))).apply(v)))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Resource> update(Resource resource) {
 return RxJava2Adapter.monoToSingle(update_migrated(resource));
}
@Override
    public Mono<Resource> update_migrated(Resource resource) {
        LOGGER.debug("Updating resource id {}", resource.getId());
        resource.setUpdatedAt(new Date());
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.update_migrated(resource)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, client, userId, resourceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String domain, String client, String userId, String resourceId) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(domain, client, userId, resourceId));
}
@Override
    public Mono<Void> delete_migrated(String domain, String client, String userId, String resourceId) {
        LOGGER.debug("Deleting resource id {} for resource owner {} and client {}", resourceId, userId, client);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserAndResource(domain, client, userId, resourceId)).switchIfEmpty(Mono.error(new ResourceNotFoundException(resourceId))).flatMap(found->RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(repository.delete_migrated(resourceId)))).then()));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(Resource resource) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(resource));
}
@Override
    public Mono<Void> delete_migrated(Resource resource) {
        LOGGER.debug("Deleting resource id {} on domain {}", resource.getId(), resource.getDomain());
        // delete policies and then the resource
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.flowableToFlux(accessPolicyRepository.findByDomainAndResource(resource.getDomain(), resource.getId())).flatMap(y->RxJava2Adapter.completableToMono(RxJavaReactorMigrationUtil.toJdkFunction((Function<AccessPolicy, Completable>)accessPolicy -> RxJava2Adapter.monoToCompletable(accessPolicyRepository.delete_migrated(accessPolicy.getId()))).apply(y))).then().then(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(repository.delete_migrated(resource.getId()))))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAccessPolicies_migrated(domain, client, user, resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<AccessPolicy> findAccessPolicies(String domain, String client, String user, String resource) {
 return RxJava2Adapter.fluxToFlowable(findAccessPolicies_migrated(domain, client, user, resource));
}
@Override
    public Flux<AccessPolicy> findAccessPolicies_migrated(String domain, String client, String user, String resource) {
        LOGGER.debug("Find access policies by domain {}, client {}, resource owner {} and resource id {}", domain, client, user, resource);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserAndResource(domain, client, user, resource)).switchIfEmpty(Mono.error(new ResourceNotFoundException(resource))).flatMapMany(RxJavaReactorMigrationUtil.toJdkFunction(r -> RxJava2Adapter.fluxToFlowable(accessPolicyRepository.findByDomainAndResource_migrated(domain, r.getId())))).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.fluxToFlowable(Flux.error(ex));
                    }
                    LOGGER.error("An error has occurred while trying to find access policies by domain {}, client {}, resource owner {} and resource id {}", domain, client, user, resource, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(String.format("An error has occurred while trying to find access policies by domain %s, client %s, resource owner %s and resource id %s", domain, client, user, resource), ex)));
                }))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAccessPoliciesByResources_migrated(resourceIds))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<AccessPolicy> findAccessPoliciesByResources(List<String> resourceIds) {
 return RxJava2Adapter.fluxToFlowable(findAccessPoliciesByResources_migrated(resourceIds));
}
@Override
    public Flux<AccessPolicy> findAccessPoliciesByResources_migrated(List<String> resourceIds) {
        LOGGER.debug("Find access policies by resources {}", resourceIds);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(accessPolicyRepository.findByResources_migrated(resourceIds))).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error has occurred while trying to find access policies by resource ids {}", resourceIds, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(String.format("An error has occurred while trying to find access policies by resource ids %s", resourceIds), ex)));
                }))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countAccessPolicyByResource_migrated(resourceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Long> countAccessPolicyByResource(String resourceId) {
 return RxJava2Adapter.monoToSingle(countAccessPolicyByResource_migrated(resourceId));
}
@Override
    public Mono<Long> countAccessPolicyByResource_migrated(String resourceId) {
        LOGGER.debug("Count access policies by resource {}", resourceId);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(accessPolicyRepository.countByResource_migrated(resourceId))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error has occurred while trying to count access policies by resource id {}", resourceId, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error has occurred while trying to count access policies by resource id %s", resourceId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findAccessPolicy_migrated(domain, client, user, resource, accessPolicy))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<AccessPolicy> findAccessPolicy(String domain, String client, String user, String resource, String accessPolicy) {
 return RxJava2Adapter.monoToMaybe(findAccessPolicy_migrated(domain, client, user, resource, accessPolicy));
}
@Override
    public Mono<AccessPolicy> findAccessPolicy_migrated(String domain, String client, String user, String resource, String accessPolicy) {
        LOGGER.debug("Find access policy by domain {}, client {}, resource owner {}, resource id {} and policy id {}", domain, client, user, resource, accessPolicy);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserAndResource(domain, client, user, resource)).switchIfEmpty(Mono.error(new ResourceNotFoundException(resource))).flatMap(z->RxJava2Adapter.monoToMaybe(accessPolicyRepository.findById_migrated(accessPolicy)).as(RxJava2Adapter::maybeToMono)))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToMaybe(Mono.error(ex));
                    }
                    LOGGER.error("An error has occurred while trying to find access policies by domain {}, client {}, resource owner {} and resource id {} and policy id {}", domain, client, user, resource, accessPolicy, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(String.format("An error has occurred while trying to find access policies by domain %s, client %s, resource owner %s resource id %s and policy id %s", domain, client, user, resource, accessPolicy), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findAccessPolicy_migrated(accessPolicy))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<AccessPolicy> findAccessPolicy(String accessPolicy) {
 return RxJava2Adapter.monoToMaybe(findAccessPolicy_migrated(accessPolicy));
}
@Override
    public Mono<AccessPolicy> findAccessPolicy_migrated(String accessPolicy) {
        LOGGER.debug("Find access policy by id {}", accessPolicy);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(accessPolicyRepository.findById_migrated(accessPolicy))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error has occurred while trying to find access policy by id {}", accessPolicy, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(String.format("An error has occurred while trying to find access policy by id %s", accessPolicy), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createAccessPolicy_migrated(accessPolicy, domain, client, user, resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AccessPolicy> createAccessPolicy(AccessPolicy accessPolicy, String domain, String client, String user, String resource) {
 return RxJava2Adapter.monoToSingle(createAccessPolicy_migrated(accessPolicy, domain, client, user, resource));
}
@Override
    public Mono<AccessPolicy> createAccessPolicy_migrated(AccessPolicy accessPolicy, String domain, String client, String user, String resource) {
        LOGGER.debug("Creating access policy for domain {}, client {}, resource owner {} and resource id {}", domain, client, user, resource);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserAndResource(domain, client, user, resource)).switchIfEmpty(Mono.error(new ResourceNotFoundException(resource))).flatMap(v->RxJava2Adapter.singleToMono((Single<AccessPolicy>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Resource, Single<AccessPolicy>>)r -> {
                    accessPolicy.setDomain(domain);
                    accessPolicy.setResource(r.getId());
                    accessPolicy.setCreatedAt(new Date());
                    accessPolicy.setUpdatedAt(accessPolicy.getCreatedAt());
                    return RxJava2Adapter.monoToSingle(accessPolicyRepository.create_migrated(accessPolicy));
                }).apply(v))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error has occurred while trying to create an access policy for domain {}, client {}, resource owner {} and resource id {}", domain, client, user, resource, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error has occurred while trying to create an access policy for domain %s, client %s, resource owner %s and resource id %s", domain, client, user, resource), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.updateAccessPolicy_migrated(accessPolicy, domain, client, user, resource, accessPolicyId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AccessPolicy> updateAccessPolicy(AccessPolicy accessPolicy, String domain, String client, String user, String resource, String accessPolicyId) {
 return RxJava2Adapter.monoToSingle(updateAccessPolicy_migrated(accessPolicy, domain, client, user, resource, accessPolicyId));
}
@Override
    public Mono<AccessPolicy> updateAccessPolicy_migrated(AccessPolicy accessPolicy, String domain, String client, String user, String resource, String accessPolicyId) {
        LOGGER.debug("Updating access policy for domain {}, client {}, resource owner {}, resource id {} and policy id {}", domain, client, user, resource, accessPolicyId);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserAndResource(domain, client, user, resource)).switchIfEmpty(Mono.error(new ResourceNotFoundException(resource))).flatMap(z->RxJava2Adapter.monoToMaybe(accessPolicyRepository.findById_migrated(accessPolicyId)).as(RxJava2Adapter::maybeToMono)).switchIfEmpty(Mono.error(new AccessPolicyNotFoundException(resource))).flatMap(v->RxJava2Adapter.singleToMono((Single<AccessPolicy>)RxJavaReactorMigrationUtil.toJdkFunction((Function<AccessPolicy, Single<AccessPolicy>>)oldPolicy -> {
                    AccessPolicy policyToUpdate = new AccessPolicy();
                    policyToUpdate.setId(oldPolicy.getId());
                    policyToUpdate.setEnabled(accessPolicy.isEnabled());
                    policyToUpdate.setName(accessPolicy.getName());
                    policyToUpdate.setDescription(accessPolicy.getDescription());
                    policyToUpdate.setType(accessPolicy.getType());
                    policyToUpdate.setOrder(accessPolicy.getOrder());
                    policyToUpdate.setCondition(accessPolicy.getCondition());
                    policyToUpdate.setDomain(oldPolicy.getDomain());
                    policyToUpdate.setResource(oldPolicy.getResource());
                    policyToUpdate.setCreatedAt(oldPolicy.getCreatedAt());
                    policyToUpdate.setUpdatedAt(new Date());
                    return RxJava2Adapter.monoToSingle(accessPolicyRepository.update_migrated(policyToUpdate));
                }).apply(v))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error has occurred while trying to update access policy for domain {}, client {}, resource owner {}, resource id {} and policy id {}", domain, client, user, resource, accessPolicyId, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error has occurred while trying to update access policy for domain %s, client %s, resource owner %s, resource id %s and policy id %s", domain, client, user, resource, accessPolicyId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteAccessPolicy_migrated(domain, client, user, resource, accessPolicy))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteAccessPolicy(String domain, String client, String user, String resource, String accessPolicy) {
 return RxJava2Adapter.monoToCompletable(deleteAccessPolicy_migrated(domain, client, user, resource, accessPolicy));
}
@Override
    public Mono<Void> deleteAccessPolicy_migrated(String domain, String client, String user, String resource, String accessPolicy) {
        LOGGER.debug("Deleting access policy for domain {}, client {}, resource owner {}, resource id and policy id {}", domain, client, user, resource, accessPolicy);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserAndResource(domain, client, user, resource)).switchIfEmpty(Mono.error(new ResourceNotFoundException(resource))).flatMap(__->RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(accessPolicyRepository.delete_migrated(accessPolicy)))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }
                    LOGGER.error("An error has occurred while trying to delete access policy for domain {}, client {}, resource owner {}, resource id {} and policy id {}", domain, client, user, resource, accessPolicy, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(String.format("An error has occurred while trying to delete access policy for domain %s, client %s, resource owner %s, resource id %s and policy id %s", domain, client, user, resource, accessPolicy), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.validateScopes_migrated(toValidate))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Resource> validateScopes(Resource toValidate) {
 return RxJava2Adapter.monoToSingle(validateScopes_migrated(toValidate));
}
private Mono<Resource> validateScopes_migrated(Resource toValidate) {
        if(toValidate.getResourceScopes()==null || toValidate.getResourceScopes().isEmpty()) {
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.error(new MissingScopeException())));
        }
        //Make sure they are distinct
        toValidate.setResourceScopes(toValidate.getResourceScopes().stream().distinct().collect(Collectors.toList()));

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(scopeService.findByDomainAndKeys_migrated(toValidate.getDomain(), toValidate.getResourceScopes()))).flatMap(v->RxJava2Adapter.singleToMono((Single<Resource>)RxJavaReactorMigrationUtil.toJdkFunction((Function<List<Scope>, Single<Resource>>)scopes -> {
                    if(toValidate.getResourceScopes().size() != scopes.size()) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new ScopeNotFoundException(
                                toValidate.getResourceScopes().stream().filter(s -> !scopes.contains(s)).collect(Collectors.joining(","))
                        )));
                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(toValidate));
                }).apply(v)))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.validateIconUri_migrated(toValidate))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Resource> validateIconUri(Resource toValidate) {
 return RxJava2Adapter.monoToSingle(validateIconUri_migrated(toValidate));
}
private Mono<Resource> validateIconUri_migrated(Resource toValidate) {
        if(toValidate.getIconUri()!=null) {
            try {
                URI.create(toValidate.getIconUri()).toURL();
            } catch (MalformedURLException | IllegalArgumentException e) {
                return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.error(new MalformedIconUriException(toValidate.getIconUri()))));
            }
        }
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(toValidate)));
    }

    private User filter(User user) {
        User resourceOwner = new User();
        resourceOwner.setId(user.getId());
        resourceOwner.setDisplayName(user.getDisplayName());
        return resourceOwner;
    }

    private Application filter(Application application) {
        Application client = new Application();
        client.setId(application.getId());
        client.setName(application.getName());
        return client;
    }
}
