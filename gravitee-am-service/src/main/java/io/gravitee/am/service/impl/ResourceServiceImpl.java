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

    @Override
    public Single<Page<Resource>> findByDomain(String domain, int page, int size) {
        LOGGER.debug("Listing resource for domain {}", domain);
        return repository.findByDomain(domain, page, size)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find resources by domain {}", domain, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find resources by domain %s", domain), ex)));
                });
    }

    @Override
    public Single<Page<Resource>> findByDomainAndClient(String domain, String client, int page, int size) {
        LOGGER.debug("Listing resource set for domain {} and client {}", domain, client);
        return repository.findByDomainAndClient(domain, client, page, size)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find resources by domain {} and client {}", domain, client, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find resources by domain %s and client %s", domain, client), ex)));
                });
    }

    @Override
    public Flowable<Resource> findByResources(List<String> resourceIds) {
        LOGGER.debug("Listing resources by ids {}", resourceIds);
        return repository.findByResources(resourceIds);
    }

    @Override
    public Flowable<Resource> listByDomainAndClientAndUser(String domain, String client, String userId) {
        LOGGER.debug("Listing resource for resource owner {} and client {}", userId, client);
        return repository.findByDomainAndClientAndUser(domain, client, userId);
    }

    @Override
    public Flowable<Resource> findByDomainAndClientAndResources(String domain, String client, List<String> resourceIds) {
        LOGGER.debug("Getting resource {} for  client {} and resources {}", resourceIds, client, resourceIds);
        return repository.findByDomainAndClientAndResources(domain, client, resourceIds);
    }

    @Override
    public Maybe<Resource> findByDomainAndClientAndUserAndResource(String domain, String client, String userId, String resourceId) {
        LOGGER.debug("Getting resource by resource owner {} and client {} and resource {}", userId, client, resourceId);
        return repository.findByDomainAndClientAndUserAndResource(domain, client, userId, resourceId);
    }

    @Override
    public Maybe<Resource> findByDomainAndClientResource(String domain, String client, String resourceId) {
        LOGGER.debug("Getting resource by domain {} client {} and resource {}", domain, client, resourceId);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.flowableToFlux(this.findByDomainAndClientAndResources(domain, client, Arrays.asList(resourceId))).next());
    }

    @Override
    public Single<Map<String, Map<String, Object>>> getMetadata(List<Resource> resources) {
        if (resources == null || resources.isEmpty()) {
            return RxJava2Adapter.monoToSingle(Mono.just(Collections.emptyMap()));
        }

        List<String> userIds = resources.stream().filter(resource -> resource.getUserId() != null).map(Resource::getUserId).distinct().collect(Collectors.toList());
        List<String> appIds = resources.stream().filter(resource -> resource.getClientId() != null).map(Resource::getClientId).distinct().collect(Collectors.toList());
        return Single.zip(userService.findByIdIn(userIds).toMap(User::getId, this::filter),
                applicationService.findByIdIn(appIds).toMap(Application::getId, this::filter), (users, apps) -> {
            Map<String, Map<String, Object>> metadata = new HashMap<>();
            metadata.put("users", (Map) users);
            metadata.put("applications", (Map) apps);
            return metadata;
        });
    }

    @Override
    public Single<Resource> create(NewResource newResource, String domain, String client, String userId) {
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

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(this.validateScopes(toCreate)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Resource, SingleSource<Resource>>toJdkFunction(this::validateIconUri).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Resource, SingleSource<Resource>>toJdkFunction(repository::create).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono((Single<Resource>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Resource, Single<Resource>>)r -> {
                    AccessPolicy accessPolicy = new AccessPolicy();
                    accessPolicy.setName("Deny all");
                    accessPolicy.setDescription("Default deny access policy. Created by Gravitee.io.");
                    accessPolicy.setType(AccessPolicyType.GROOVY);
                    accessPolicy.setCondition("{\"onRequestScript\":\"import io.gravitee.policy.groovy.PolicyResult.State\\nresult.state = State.FAILURE;\"}");
                    accessPolicy.setEnabled(true);
                    accessPolicy.setDomain(domain);
                    accessPolicy.setResource(r.getId());
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(accessPolicyRepository.create(accessPolicy)).map(RxJavaReactorMigrationUtil.toJdkFunction(__ -> r)));
                }).apply(v))));
    }

    @Override
    public Single<Resource> update(NewResource newResource, String domain, String client, String userId, String resourceId) {
        LOGGER.debug("Updating resource id {} for resource owner {} and client {}", resourceId, userId, client);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(findByDomainAndClientAndUserAndResource(domain, client, userId, resourceId)
                .switchIfEmpty(Maybe.error(new ResourceNotFoundException(resourceId)))
                .flatMapSingle(Single::just)
                .map(newResource::update)).map(RxJavaReactorMigrationUtil.toJdkFunction(toUpdate -> toUpdate.setUpdatedAt(new Date()))))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Resource, SingleSource<Resource>>toJdkFunction(this::validateScopes).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Resource, SingleSource<Resource>>toJdkFunction(this::validateIconUri).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono((Single<Resource>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Resource, Single<Resource>>)repository::update).apply(v))));
    }

    @Override
    public Single<Resource> update(Resource resource) {
        LOGGER.debug("Updating resource id {}", resource.getId());
        resource.setUpdatedAt(new Date());
        return repository.update(resource);
    }

    @Override
    public Completable delete(String domain, String client, String userId, String resourceId) {
        LOGGER.debug("Deleting resource id {} for resource owner {} and client {}", resourceId, userId, client);
        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserAndResource(domain, client, userId, resourceId)).switchIfEmpty(Mono.error(new ResourceNotFoundException(resourceId))).flatMap(found->RxJava2Adapter.completableToMono(repository.delete(resourceId))).then());
    }

    @Override
    public Completable delete(Resource resource) {
        LOGGER.debug("Deleting resource id {} on domain {}", resource.getId(), resource.getDomain());
        // delete policies and then the resource
        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.flowableToFlux(accessPolicyRepository.findByDomainAndResource(resource.getDomain(), resource.getId())).flatMap(y->RxJava2Adapter.completableToMono(RxJavaReactorMigrationUtil.toJdkFunction((Function<AccessPolicy, Completable>)accessPolicy -> accessPolicyRepository.delete(accessPolicy.getId())).apply(y))).then().then(RxJava2Adapter.completableToMono(repository.delete(resource.getId()))));
    }

    @Override
    public Flowable<AccessPolicy> findAccessPolicies(String domain, String client, String user, String resource) {
        LOGGER.debug("Find access policies by domain {}, client {}, resource owner {} and resource id {}", domain, client, user, resource);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserAndResource(domain, client, user, resource)).switchIfEmpty(RxJava2Adapter.singleToMono(Single.error(new ResourceNotFoundException(resource)))).flatMapMany(RxJavaReactorMigrationUtil.toJdkFunction(r -> accessPolicyRepository.findByDomainAndResource(domain, r.getId()))).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.fluxToFlowable(Flux.error(ex));
                    }
                    LOGGER.error("An error has occurred while trying to find access policies by domain {}, client {}, resource owner {} and resource id {}", domain, client, user, resource, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(String.format("An error has occurred while trying to find access policies by domain %s, client %s, resource owner %s and resource id %s", domain, client, user, resource), ex)));
                })));
    }

    @Override
    public Flowable<AccessPolicy> findAccessPoliciesByResources(List<String> resourceIds) {
        LOGGER.debug("Find access policies by resources {}", resourceIds);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(accessPolicyRepository.findByResources(resourceIds)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error has occurred while trying to find access policies by resource ids {}", resourceIds, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(String.format("An error has occurred while trying to find access policies by resource ids %s", resourceIds), ex)));
                })));
    }

    @Override
    public Single<Long> countAccessPolicyByResource(String resourceId) {
        LOGGER.debug("Count access policies by resource {}", resourceId);
        return accessPolicyRepository.countByResource(resourceId)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error has occurred while trying to count access policies by resource id {}", resourceId, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error has occurred while trying to count access policies by resource id %s", resourceId), ex)));
                });
    }

    @Override
    public Maybe<AccessPolicy> findAccessPolicy(String domain, String client, String user, String resource, String accessPolicy) {
        LOGGER.debug("Find access policy by domain {}, client {}, resource owner {}, resource id {} and policy id {}", domain, client, user, resource, accessPolicy);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserAndResource(domain, client, user, resource)).switchIfEmpty(Mono.error(new ResourceNotFoundException(resource))).flatMap(z->accessPolicyRepository.findById(accessPolicy).as(RxJava2Adapter::maybeToMono)))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToMaybe(Mono.error(ex));
                    }
                    LOGGER.error("An error has occurred while trying to find access policies by domain {}, client {}, resource owner {} and resource id {} and policy id {}", domain, client, user, resource, accessPolicy, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(String.format("An error has occurred while trying to find access policies by domain %s, client %s, resource owner %s resource id %s and policy id %s", domain, client, user, resource, accessPolicy), ex)));
                });
    }

    @Override
    public Maybe<AccessPolicy> findAccessPolicy(String accessPolicy) {
        LOGGER.debug("Find access policy by id {}", accessPolicy);
        return accessPolicyRepository.findById(accessPolicy)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error has occurred while trying to find access policy by id {}", accessPolicy, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(String.format("An error has occurred while trying to find access policy by id %s", accessPolicy), ex)));
                });
    }

    @Override
    public Single<AccessPolicy> createAccessPolicy(AccessPolicy accessPolicy, String domain, String client, String user, String resource) {
        LOGGER.debug("Creating access policy for domain {}, client {}, resource owner {} and resource id {}", domain, client, user, resource);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserAndResource(domain, client, user, resource)).switchIfEmpty(Mono.error(new ResourceNotFoundException(resource))).flatMap(v->RxJava2Adapter.singleToMono((Single<AccessPolicy>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Resource, Single<AccessPolicy>>)r -> {
                    accessPolicy.setDomain(domain);
                    accessPolicy.setResource(r.getId());
                    accessPolicy.setCreatedAt(new Date());
                    accessPolicy.setUpdatedAt(accessPolicy.getCreatedAt());
                    return accessPolicyRepository.create(accessPolicy);
                }).apply(v))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error has occurred while trying to create an access policy for domain {}, client {}, resource owner {} and resource id {}", domain, client, user, resource, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error has occurred while trying to create an access policy for domain %s, client %s, resource owner %s and resource id %s", domain, client, user, resource), ex)));
                });
    }

    @Override
    public Single<AccessPolicy> updateAccessPolicy(AccessPolicy accessPolicy, String domain, String client, String user, String resource, String accessPolicyId) {
        LOGGER.debug("Updating access policy for domain {}, client {}, resource owner {}, resource id {} and policy id {}", domain, client, user, resource, accessPolicyId);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserAndResource(domain, client, user, resource)).switchIfEmpty(RxJava2Adapter.maybeToMono(Maybe.wrap(Maybe.error(new ResourceNotFoundException(resource))))))).flatMap(z->accessPolicyRepository.findById(accessPolicyId).as(RxJava2Adapter::maybeToMono)).switchIfEmpty(Mono.error(new AccessPolicyNotFoundException(resource))).flatMap(v->RxJava2Adapter.singleToMono((Single<AccessPolicy>)RxJavaReactorMigrationUtil.toJdkFunction((Function<AccessPolicy, Single<AccessPolicy>>)oldPolicy -> {
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
                    return accessPolicyRepository.update(policyToUpdate);
                }).apply(v))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error has occurred while trying to update access policy for domain {}, client {}, resource owner {}, resource id {} and policy id {}", domain, client, user, resource, accessPolicyId, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error has occurred while trying to update access policy for domain %s, client %s, resource owner %s, resource id %s and policy id %s", domain, client, user, resource, accessPolicyId), ex)));
                });
    }

    @Override
    public Completable deleteAccessPolicy(String domain, String client, String user, String resource, String accessPolicy) {
        LOGGER.debug("Deleting access policy for domain {}, client {}, resource owner {}, resource id and policy id {}", domain, client, user, resource, accessPolicy);
        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserAndResource(domain, client, user, resource)).switchIfEmpty(Mono.error(new ResourceNotFoundException(resource))).flatMap(__->RxJava2Adapter.completableToMono(accessPolicyRepository.delete(accessPolicy))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }
                    LOGGER.error("An error has occurred while trying to delete access policy for domain {}, client {}, resource owner {}, resource id {} and policy id {}", domain, client, user, resource, accessPolicy, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(String.format("An error has occurred while trying to delete access policy for domain %s, client %s, resource owner %s, resource id %s and policy id %s", domain, client, user, resource, accessPolicy), ex)));
                });
    }

    private Single<Resource> validateScopes(Resource toValidate) {
        if(toValidate.getResourceScopes()==null || toValidate.getResourceScopes().isEmpty()) {
            return RxJava2Adapter.monoToSingle(Mono.error(new MissingScopeException()));
        }
        //Make sure they are distinct
        toValidate.setResourceScopes(toValidate.getResourceScopes().stream().distinct().collect(Collectors.toList()));

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(scopeService.findByDomainAndKeys(toValidate.getDomain(), toValidate.getResourceScopes())).flatMap(v->RxJava2Adapter.singleToMono((Single<Resource>)RxJavaReactorMigrationUtil.toJdkFunction((Function<List<Scope>, Single<Resource>>)scopes -> {
                    if(toValidate.getResourceScopes().size() != scopes.size()) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new ScopeNotFoundException(
                                toValidate.getResourceScopes().stream().filter(s -> !scopes.contains(s)).collect(Collectors.joining(","))
                        )));
                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(toValidate));
                }).apply(v))));
    }

    private Single<Resource> validateIconUri(Resource toValidate) {
        if(toValidate.getIconUri()!=null) {
            try {
                URI.create(toValidate.getIconUri()).toURL();
            } catch (MalformedURLException | IllegalArgumentException e) {
                return RxJava2Adapter.monoToSingle(Mono.error(new MalformedIconUriException(toValidate.getIconUri())));
            }
        }
        return RxJava2Adapter.monoToSingle(Mono.just(toValidate));
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
