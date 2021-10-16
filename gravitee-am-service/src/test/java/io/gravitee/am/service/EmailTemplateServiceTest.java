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
package io.gravitee.am.service;

import static org.mockito.Mockito.*;

import io.gravitee.am.model.Email;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Template;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.api.EmailRepository;
import io.gravitee.am.service.exception.EmailAlreadyExistsException;
import io.gravitee.am.service.exception.EmailNotFoundException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.impl.EmailTemplateServiceImpl;
import io.gravitee.am.service.model.NewEmail;
import io.gravitee.am.service.model.UpdateEmail;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class EmailTemplateServiceTest {

    @InjectMocks
    private EmailTemplateService emailTemplateService = new EmailTemplateServiceImpl();

    @Mock
    private EventService eventService;

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private AuditService auditService;

    private final static String DOMAIN = "domain1";

    @Test
    public void shouldFindAll() {
        when(emailRepository.findAll_migrated(ReferenceType.DOMAIN, DOMAIN)).thenReturn(Flux.just(new Email()));
        TestSubscriber testObserver = RxJava2Adapter.fluxToFlowable(emailTemplateService.findAll_migrated(ReferenceType.DOMAIN, DOMAIN)).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindByDomainAndTemplate() {
        when(emailRepository.findByTemplate_migrated(ReferenceType.DOMAIN, DOMAIN, Template.LOGIN.template())).thenReturn(Mono.just(new Email()));
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(emailTemplateService.findByDomainAndTemplate_migrated(DOMAIN, Template.LOGIN.template())).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindByDomainAndTemplate_notExistingEmail() {
        when(emailRepository.findByTemplate_migrated(ReferenceType.DOMAIN, DOMAIN, Template.LOGIN.template())).thenReturn(Mono.empty());
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(emailTemplateService.findByDomainAndTemplate_migrated(DOMAIN, Template.LOGIN.template())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindByDomainAndTemplate_technicalException() {
        when(emailRepository.findByTemplate_migrated(ReferenceType.DOMAIN, DOMAIN, Template.LOGIN.template())).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));
        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToMaybe(emailTemplateService.findByDomainAndTemplate_migrated(DOMAIN, Template.LOGIN.template())).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldCreate() {
        NewEmail newEmail = Mockito.mock(NewEmail.class);
        when(newEmail.getTemplate()).thenReturn(Template.REGISTRATION);
        when(emailRepository.findByTemplate_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), anyString())).thenReturn(Mono.empty());
        when(emailRepository.create_migrated(any(Email.class))).thenReturn(Mono.just(new Email()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(emailTemplateService.create_migrated(DOMAIN, newEmail)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(emailRepository, times(1)).findByTemplate_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), anyString());
        verify(emailRepository, times(1)).create_migrated(any(Email.class));
    }

    @Test
    public void shouldCreate_technicalException() {
        NewEmail newEmail = Mockito.mock(NewEmail.class);
        when(newEmail.getTemplate()).thenReturn(Template.REGISTRATION);
        when(emailRepository.findByTemplate_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), anyString())).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(emailTemplateService.create_migrated(DOMAIN, newEmail)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(emailRepository, never()).create_migrated(any(Email.class));
    }

    @Test
    public void shouldCreate_uniquenessException() {
        NewEmail newEmail = Mockito.mock(NewEmail.class);
        when(newEmail.getTemplate()).thenReturn(Template.REGISTRATION);
        when(emailRepository.findByTemplate_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), any())).thenReturn(Mono.just(new Email()));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(emailTemplateService.create_migrated(DOMAIN, newEmail)).subscribe(testObserver);

        testObserver.assertError(EmailAlreadyExistsException.class);
        testObserver.assertNotComplete();

        verify(emailRepository, never()).create_migrated(any(Email.class));
    }

    @Test
    public void shouldUpdate() {
        UpdateEmail updateEmail = Mockito.mock(UpdateEmail.class);
        when(emailRepository.findById_migrated(ReferenceType.DOMAIN, DOMAIN, "my-email")).thenReturn(Mono.just(new Email()));
        when(emailRepository.update_migrated(any(Email.class))).thenReturn(Mono.just(new Email()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(emailTemplateService.update_migrated(DOMAIN, "my-email", updateEmail)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(emailRepository, times(1)).findById_migrated(ReferenceType.DOMAIN, DOMAIN, "my-email");
        verify(emailRepository, times(1)).update_migrated(any(Email.class));
        verify(eventService, times(1)).create_migrated(any());
    }

    @Test
    public void shouldUpdate_technicalException() {
        UpdateEmail updateEmail = Mockito.mock(UpdateEmail.class);
        when(emailRepository.findById_migrated(ReferenceType.DOMAIN, DOMAIN,"my-email")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(emailTemplateService.update_migrated(DOMAIN, "my-email", updateEmail)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(emailRepository, never()).findByTemplate_migrated(any(), any(), any());
        verify(emailRepository, never()).update_migrated(any(Email.class));
    }

    @Test
    public void shouldUpdate_emailNotFound() {
        when(emailRepository.findById_migrated(ReferenceType.DOMAIN, DOMAIN,"my-email")).thenReturn(Mono.empty());

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(emailTemplateService.update_migrated(DOMAIN, "my-email", new UpdateEmail())).subscribe(testObserver);

        testObserver.assertError(EmailNotFoundException.class);
        testObserver.assertNotComplete();

        verify(emailRepository, never()).update_migrated(any(Email.class));
    }

    @Test
    public void shouldDelete_notExistingEmail() {
        when(emailRepository.findById_migrated("my-email")).thenReturn(Mono.empty());

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(emailTemplateService.delete_migrated("my-email")).test();

        testObserver.assertError(EmailNotFoundException.class);
        testObserver.assertNotComplete();

        verify(emailRepository, never()).delete_migrated(anyString());
    }

    @Test
    public void shouldDelete_technicalException() {
        when(emailRepository.findById_migrated("my-email")).thenReturn(Mono.just(new Email()));
        when(emailRepository.delete_migrated(anyString())).thenReturn(Mono.error(TechnicalException::new));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(emailTemplateService.delete_migrated("my-email")).test();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete() {
        Email email = new Email();
        email.setId("my-email");
        email.setReferenceType(ReferenceType.DOMAIN);
        email.setReferenceId(DOMAIN);

        when(emailRepository.findById_migrated(email.getId())).thenReturn(Mono.just(email));
        when(emailRepository.delete_migrated(email.getId())).thenReturn(Mono.empty());
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(emailTemplateService.delete_migrated(email.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(emailRepository, times(1)).delete_migrated(email.getId());
        verify(eventService, times(1)).create_migrated(any());
    }

    @Test
    public void copyFromClient() {

        final String sourceUid = "sourceUid";
        final String targetUid = "targetUid";

        Email mailOne = new Email();
        mailOne.setId("templateId");
        mailOne.setEnabled(true);
        mailOne.setReferenceType(ReferenceType.DOMAIN);
        mailOne.setReferenceId(DOMAIN);
        mailOne.setClient(sourceUid);
        mailOne.setTemplate("login");
        mailOne.setFrom("fromMail");
        mailOne.setFromName("mailName");
        mailOne.setSubject("mailSubject");
        mailOne.setContent("mailContent");
        mailOne.setExpiresAfter(0);

        Email mailTwo = new Email(mailOne);
        mailTwo.setTemplate("error");
        mailTwo.setExpiresAfter(1000);

        when(emailRepository.findByClient_migrated(ReferenceType.DOMAIN, DOMAIN, sourceUid)).thenReturn(Flux.just(mailOne, mailTwo));
        when(emailRepository.findByClientAndTemplate_migrated(ReferenceType.DOMAIN, DOMAIN, targetUid, "login")).thenReturn(Mono.empty());
        when(emailRepository.findByClientAndTemplate_migrated(ReferenceType.DOMAIN, DOMAIN, targetUid, "error")).thenReturn(Mono.empty());
        when(emailRepository.create_migrated(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver<List<Email>> testObserver = RxJava2Adapter.monoToSingle(emailTemplateService.copyFromClient_migrated(DOMAIN, sourceUid, targetUid).collectList()).test();
        testObserver.assertComplete().assertNoErrors();
        testObserver.assertValue(emails -> emails != null && emails.size() == 2 && emails.stream().filter(
                email -> email.getReferenceType() == ReferenceType.DOMAIN &&
                        email.getReferenceId().equals(DOMAIN) &&
                        email.getClient().equals(targetUid) &&
                        !email.getId().equals("templateId") &&
                        Arrays.asList("login", "error").contains(email.getTemplate()) &&
                        email.getFrom().equals("fromMail") &&
                        email.getFromName().equals("mailName")
                ).count() == 2
        );
    }

    @Test
    public void copyFromClient_duplicateFound() {

        final String sourceUid = "sourceUid";
        final String targetUid = "targetUid";

        Email mailOne = new Email();
        mailOne.setId("templateId");
        mailOne.setEnabled(true);
        mailOne.setReferenceType(ReferenceType.DOMAIN);
        mailOne.setReferenceId(DOMAIN);
        mailOne.setClient(sourceUid);
        mailOne.setTemplate("login");
        mailOne.setFrom("fromMail");
        mailOne.setFromName("mailName");
        mailOne.setSubject("mailSubject");
        mailOne.setContent("mailContent");
        mailOne.setExpiresAfter(0);

        when(emailRepository.findByClientAndTemplate_migrated(ReferenceType.DOMAIN, DOMAIN, targetUid, "login")).thenReturn(Mono.just(new Email()));
        when(emailRepository.findByClient_migrated(ReferenceType.DOMAIN, DOMAIN, sourceUid)).thenReturn(Flux.just(mailOne));

        TestSubscriber<Email> testSubscriber = RxJava2Adapter.fluxToFlowable(emailTemplateService.copyFromClient_migrated(DOMAIN, sourceUid, targetUid)).test();
        testSubscriber.assertNotComplete();
        testSubscriber.assertError(EmailAlreadyExistsException.class);
    }
}
