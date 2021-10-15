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
package io.gravitee.am.management.handlers.management.api.resources.organizations.groups;

import io.gravitee.am.management.handlers.management.api.resources.AbstractResource;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.GroupService;
import io.gravitee.am.service.IdentityProviderService;
import io.gravitee.common.http.MediaType;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Comparator;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class GroupMembersResource extends AbstractResource {

    private static final int MAX_MEMBERS_SIZE_PER_PAGE = 30;
    private static final String MAX_MEMBERS_SIZE_PER_PAGE_STRING = "30";

    @Context
    private ResourceContext resourceContext;

    @Autowired
    private GroupService groupService;

    @Autowired
    private IdentityProviderService identityProviderService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List group members",
            notes = "User must have the ORGANIZATION_GROUP[READ] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Group members successfully fetched", response = User.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void list(
            @PathParam("organizationId") String organizationId,
            @PathParam("group") String group,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue(MAX_MEMBERS_SIZE_PER_PAGE_STRING) int size,
            @Suspended final AsyncResponse response) {

        RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(checkPermission(ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_GROUP, Acl.READ)).then(RxJava2Adapter.singleToMono(groupService.findMembers(ReferenceType.ORGANIZATION, organizationId, group, page, Integer.min(size, MAX_MEMBERS_SIZE_PER_PAGE))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Page<io.gravitee.am.model.User>, SingleSource<Page<io.gravitee.am.model.User>>>toJdkFunction(pagedMembers -> {
                            if (pagedMembers.getData() == null) {
                                return RxJava2Adapter.monoToSingle(Mono.just(pagedMembers));
                            }
                            return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Observable.fromIterable(pagedMembers.getData())
                                    .flatMapSingle(member -> {
                                        if (member.getSource() != null) {
                                            return RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(identityProviderService.findById(member.getSource())
                                                    .map(idP -> {
                                                        member.setSource(idP.getName());
                                                        return member;
                                                    })
                                                    .defaultIfEmpty(member)).single());
                                        }
                                        return RxJava2Adapter.monoToSingle(Mono.just(member));
                                    })
                                    .toSortedList(Comparator.comparing(User::getUsername))).map(RxJavaReactorMigrationUtil.toJdkFunction(members -> new Page<>(members, pagedMembers.getCurrentPage(), pagedMembers.getTotalCount()))));
                        }).apply(v))))))
                .subscribe(response::resume, response::resume);
    }

    @Path("{member}")
    public GroupMemberResource groupMemberResource() {
        return resourceContext.getResource(GroupMemberResource.class);
    }
}