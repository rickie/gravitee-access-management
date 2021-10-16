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
package io.gravitee.am.management.service.impl.commands;

import static io.gravitee.am.management.service.impl.commands.UserCommandHandler.COCKPIT_SOURCE;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.Membership;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.User;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.model.permissions.DefaultRole;
import io.gravitee.am.model.permissions.SystemRole;
import io.gravitee.am.service.MembershipService;
import io.gravitee.am.service.OrganizationUserService;
import io.gravitee.am.service.RoleService;
import io.gravitee.am.service.UserService;
import io.gravitee.am.service.exception.InvalidRoleException;
import io.gravitee.cockpit.api.command.Command;
import io.gravitee.cockpit.api.command.CommandHandler;
import io.gravitee.cockpit.api.command.CommandStatus;
import io.gravitee.cockpit.api.command.membership.MembershipCommand;
import io.gravitee.cockpit.api.command.membership.MembershipPayload;
import io.gravitee.cockpit.api.command.membership.MembershipReply;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MembershipCommandHandler implements CommandHandler<MembershipCommand, MembershipReply> {

    private final Logger logger = LoggerFactory.getLogger(MembershipCommandHandler.class);

    private final OrganizationUserService userService;
    private final RoleService roleService;
    private final MembershipService membershipService;

    public MembershipCommandHandler(OrganizationUserService userService,
                                    RoleService roleService,
                                    MembershipService membershipService) {
        this.userService = userService;
        this.roleService = roleService;
        this.membershipService = membershipService;
    }

    @Override
    public Command.Type handleType() {
        return Command.Type.MEMBERSHIP_COMMAND;
    }

    @Override
    public Single<MembershipReply> handle(MembershipCommand command) {

        MembershipPayload membershipPayload = command.getPayload();
        ReferenceType assignableType;

        try {
            assignableType = ReferenceType.valueOf(membershipPayload.getReferenceType());
        } catch (Exception e) {
            logger.error("Invalid referenceType [{}].", membershipPayload.getReferenceType());
            return RxJava2Adapter.monoToSingle(Mono.just(new MembershipReply(command.getId(), CommandStatus.ERROR)));
        }

        Single<String> userObs = RxJava2Adapter.monoToSingle(userService.findByExternalIdAndSource_migrated(ReferenceType.ORGANIZATION, membershipPayload.getOrganizationId(), membershipPayload.getUserId(), COCKPIT_SOURCE).map(RxJavaReactorMigrationUtil.toJdkFunction(User::getId)).single());
        Single<Role> roleObs = RxJava2Adapter.monoToSingle(findRole_migrated(membershipPayload.getRole(), membershipPayload.getOrganizationId(), assignableType));


        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.zip(roleObs, userObs,
                (role, userId) -> {
                    Membership membership = new Membership();
                    membership.setMemberType(MemberType.USER);
                    membership.setMemberId(userId);
                    membership.setReferenceType(assignableType);
                    membership.setReferenceId(membershipPayload.getReferenceId());
                    membership.setRoleId(role.getId());

                    return membership;
                })).flatMap(membership->membershipService.addOrUpdate_migrated(membershipPayload.getOrganizationId(), membership)).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(membership -> logger.info("Role [{}] assigned on {} [{}] for user [{}] and organization [{}].", membershipPayload.getRole(), membershipPayload.getReferenceType(), membershipPayload.getReferenceId(), membership.getMemberId(), membershipPayload.getOrganizationId()))).map(RxJavaReactorMigrationUtil.toJdkFunction(user -> new MembershipReply(command.getId(), CommandStatus.SUCCEEDED))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> logger.error("Error occurred when trying to assign role [{}] on {} [{}] for cockpit user [{}] and organization [{}].", membershipPayload.getRole(), membershipPayload.getReferenceType(), membershipPayload.getReferenceId(), membershipPayload.getUserId(), membershipPayload.getOrganizationId(), error))))
                .onErrorReturn(throwable -> new MembershipReply(command.getId(), CommandStatus.ERROR));
    }


    
private Mono<Role> findRole_migrated(String roleName, String organizationId, ReferenceType assignableType) {

        SystemRole systemRole = SystemRole.fromName(roleName);

        // First try to map to a system role.
        if (systemRole != null) {
            return roleService.findSystemRole_migrated(systemRole, assignableType).single();
        } else {
            // Then try to find a default role.
            DefaultRole defaultRole = DefaultRole.fromName(roleName);

            if (defaultRole != null) {
                return roleService.findDefaultRole_migrated(organizationId, defaultRole, assignableType).single();
            }
        }

        return Mono.error(new InvalidRoleException(String.format("Unable to find role [%s] for organization [%s].", roleName, organizationId)));
    }
}