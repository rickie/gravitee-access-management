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
package io.gravitee.am.repository.management.api;

import io.gravitee.am.model.Membership;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.repository.management.AbstractManagementTest;
import io.gravitee.am.repository.management.api.search.MembershipCriteria;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class MembershipRepositoryTest extends AbstractManagementTest {
    public static final String ORGANIZATION_ID = "ORGANIZATIONID123";
    @Autowired
    private MembershipRepository membershipRepository;

    @Test
    public void testFindById() {

        Membership membership = new Membership();
        membership.setRoleId("role#1");
        membership.setReferenceType(ReferenceType.ORGANIZATION);
        membership.setReferenceId(ORGANIZATION_ID);
        membership.setMemberType(MemberType.USER);
        membership.setMemberId("user#1");

        Membership createdMembership = membershipRepository.create_migrated(membership).block();

        TestObserver<Membership> obs = RxJava2Adapter.monoToMaybe(membershipRepository.findById_migrated(createdMembership.getId())).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(m -> m.getId().equals(createdMembership.getId())
                && m.getRoleId().equals(membership.getRoleId())
                && m.getReferenceType() == membership.getReferenceType()
                && m.getReferenceId().equals(membership.getReferenceId())
                && m.getMemberType() == membership.getMemberType()
                && m.getMemberId().equals(membership.getMemberId()));
    }

    @Test
    public void testFindByReference() {

        Membership membership = new Membership();
        membership.setRoleId("role#1");
        membership.setReferenceType(ReferenceType.ORGANIZATION);
        membership.setReferenceId(ORGANIZATION_ID);
        membership.setMemberType(MemberType.USER);
        membership.setMemberId("user#1");

        Membership createdMembership = membershipRepository.create_migrated(membership).block();

        TestObserver<List<Membership>> obs = RxJava2Adapter.monoToSingle(membershipRepository.findByReference_migrated(ORGANIZATION_ID, ReferenceType.ORGANIZATION).collectList()).test();
        obs.awaitTerminalEvent();

        obs.assertComplete();
        obs.assertValue(m -> m.size() == 1 && m.get(0).getId().equals(createdMembership.getId()));
    }

    @Test
    public void testFindByMember() {

        Membership membership = new Membership();
        membership.setRoleId("role#1");
        membership.setReferenceType(ReferenceType.ORGANIZATION);
        membership.setReferenceId(ORGANIZATION_ID);
        membership.setMemberType(MemberType.USER);
        membership.setMemberId("user#1");

        Membership createdMembership = membershipRepository.create_migrated(membership).block();

        TestObserver<List<Membership>> obs = RxJava2Adapter.monoToSingle(membershipRepository.findByMember_migrated("user#1", MemberType.USER).collectList()).test();
        obs.awaitTerminalEvent();

        obs.assertComplete();
        obs.assertValue(m -> m.size() == 1 && m.get(0).getMemberId().equals(createdMembership.getMemberId()));
    }

    @Test
    public void testFindByReferenceAndMember() {

        Membership membership = new Membership();
        membership.setRoleId("role#1");
        membership.setReferenceType(ReferenceType.ORGANIZATION);
        membership.setReferenceId(ORGANIZATION_ID);
        membership.setMemberType(MemberType.USER);
        membership.setMemberId("user#1");

        Membership createdMembership = membershipRepository.create_migrated(membership).block();

        TestObserver<Membership> obs = RxJava2Adapter.monoToMaybe(membershipRepository.findByReferenceAndMember_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, membership.getMemberType(), membership.getMemberId())).test();
        obs.awaitTerminalEvent();

        obs.assertComplete();
        obs.assertValue(m -> m.getId().equals(createdMembership.getId()));
    }

    @Test
    public void testFindByCriteria() {

        Membership membership = new Membership();
        membership.setRoleId("role#1");
        membership.setReferenceType(ReferenceType.ORGANIZATION);
        membership.setReferenceId(ORGANIZATION_ID);
        membership.setMemberType(MemberType.USER);
        membership.setMemberId("user#1");

        Membership groupMembership = new Membership();
        groupMembership.setRoleId("role#1");
        groupMembership.setReferenceType(ReferenceType.ORGANIZATION);
        groupMembership.setReferenceId(ORGANIZATION_ID);
        groupMembership.setMemberType(MemberType.GROUP);
        groupMembership.setMemberId("group#1");

        membershipRepository.create_migrated(membership).block();
        membershipRepository.create_migrated(groupMembership).block();

        MembershipCriteria criteria = new MembershipCriteria();
        TestSubscriber<Membership> obs = RxJava2Adapter.fluxToFlowable(membershipRepository.findByCriteria_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, criteria)).test();

        obs.awaitTerminalEvent();
        obs.assertValueCount(2);

        criteria.setUserId("user#1");
        obs = RxJava2Adapter.fluxToFlowable(membershipRepository.findByCriteria_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, criteria)).test();

        obs.awaitTerminalEvent();
        obs.assertValueCount(1);
        obs.assertValue(m -> m.getMemberType() == MemberType.USER && m.getMemberId().equals("user#1"));

        criteria.setUserId(null);
        criteria.setGroupIds(Arrays.asList("group#1"));
        obs = RxJava2Adapter.fluxToFlowable(membershipRepository.findByCriteria_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, criteria)).test();

        obs.awaitTerminalEvent();
        obs.assertValueCount(1);
        obs.assertValue(m -> m.getMemberType() == MemberType.GROUP && m.getMemberId().equals("group#1"));

        criteria.setUserId("user#1");
        criteria.setGroupIds(Arrays.asList("group#1"));
        obs = RxJava2Adapter.fluxToFlowable(membershipRepository.findByCriteria_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, criteria)).test();

        obs.awaitTerminalEvent();
        obs.assertNoValues();
        obs.assertNoErrors();

        criteria.setUserId("user#1");
        criteria.setGroupIds(Arrays.asList("group#1"));
        criteria.setLogicalOR(true);
        obs = RxJava2Adapter.fluxToFlowable(membershipRepository.findByCriteria_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, criteria)).test();

        obs.awaitTerminalEvent();
        obs.assertValueCount(2);
    }

}
