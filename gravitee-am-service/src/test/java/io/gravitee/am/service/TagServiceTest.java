/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.gravitee.am.model.Organization;
import io.gravitee.am.model.Tag;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.api.TagRepository;
import io.gravitee.am.service.exception.TagAlreadyExistsException;
import io.gravitee.am.service.exception.TagNotFoundException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.impl.TagServiceImpl;
import io.gravitee.am.service.model.NewTag;
import io.gravitee.am.service.model.UpdateTag;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class TagServiceTest {

    @InjectMocks private TagService tagService = new TagServiceImpl();

    @Mock private TagRepository tagRepository;

    @Mock private AuditService auditService;

    @Test
    public void shouldFindById() {
        when(tagRepository.findById("my-tag", Organization.DEFAULT))
                .thenReturn(Maybe.just(new Tag()));
        TestObserver testObserver = tagService.findById("my-tag", Organization.DEFAULT).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindById_notExistingScope() {
        when(tagRepository.findById("my-tag", Organization.DEFAULT)).thenReturn(Maybe.empty());
        TestObserver testObserver = tagService.findById("my-tag", Organization.DEFAULT).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindById_technicalException() {
        when(tagRepository.findById("my-tag", Organization.DEFAULT))
                .thenReturn(Maybe.error(TechnicalException::new));
        TestObserver testObserver = new TestObserver();
        tagService.findById("my-tag", Organization.DEFAULT).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldCreate() {
        NewTag newTag = Mockito.mock(NewTag.class);
        when(newTag.getName()).thenReturn("my-tag");
        when(tagRepository.findById("my-tag", Organization.DEFAULT)).thenReturn(Maybe.empty());
        when(tagRepository.create(any(Tag.class))).thenReturn(Single.just(new Tag()));

        TestObserver testObserver = tagService.create(newTag, Organization.DEFAULT, null).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(tagRepository, times(1)).findById(eq("my-tag"), eq(Organization.DEFAULT));
        verify(tagRepository, times(1)).create(any(Tag.class));
    }

    @Test
    public void shouldCreate_tagAlreadyExists() {
        NewTag newTag = Mockito.mock(NewTag.class);
        when(newTag.getName()).thenReturn("my-tag");
        when(tagRepository.findById("my-tag", Organization.DEFAULT))
                .thenReturn(Maybe.just(new Tag()));

        TestObserver<Tag> testObserver = new TestObserver<>();
        tagService.create(newTag, Organization.DEFAULT, null).subscribe(testObserver);

        testObserver.assertError(TagAlreadyExistsException.class);
        testObserver.assertNotComplete();

        verify(tagRepository, times(1)).findById(eq("my-tag"), eq(Organization.DEFAULT));
        verify(tagRepository, never()).create(any(Tag.class));
    }

    @Test
    public void shouldNotCreate_technicalException() {
        NewTag newTag = Mockito.mock(NewTag.class);
        when(newTag.getName()).thenReturn("my-tag");
        when(tagRepository.findById("my-tag", Organization.DEFAULT))
                .thenReturn(Maybe.error(TechnicalException::new));

        TestObserver testObserver = new TestObserver();
        tagService.create(newTag, Organization.DEFAULT, null).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(tagRepository, never()).create(any(Tag.class));
    }

    @Test
    public void shouldUpdate_tag() {
        UpdateTag updateTag = Mockito.mock(UpdateTag.class);
        when(updateTag.getName()).thenReturn("my-tag");
        when(updateTag.getDescription()).thenReturn("my-tag-desc");
        when(tagRepository.findById("my-tag", Organization.DEFAULT))
                .thenReturn(Maybe.just(new Tag()));
        when(tagRepository.update(any())).thenReturn(Single.just(new Tag()));

        TestObserver<Tag> testObserver = new TestObserver<>();
        tagService.update("my-tag", Organization.DEFAULT, updateTag, null).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();

        verify(tagRepository, times(1)).findById(eq("my-tag"), eq(Organization.DEFAULT));
        verify(tagRepository)
                .update(
                        argThat(
                                t ->
                                        Organization.DEFAULT.equals(t.getOrganizationId())
                                                && "my-tag-desc".equals(t.getDescription())));
    }

    @Test
    public void shouldNotUpdate_missingTag() {
        UpdateTag updateTag = Mockito.mock(UpdateTag.class);
        when(tagRepository.findById("my-tag", Organization.DEFAULT)).thenReturn(Maybe.empty());

        TestObserver<Tag> testObserver = new TestObserver<>();
        tagService.update("my-tag", Organization.DEFAULT, updateTag, null).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertError(TagNotFoundException.class);

        verify(tagRepository, times(1)).findById(eq("my-tag"), eq(Organization.DEFAULT));
        verify(tagRepository, never()).update(any());
    }

    @Test
    public void shouldDelete_notExistingTag() {
        when(tagRepository.findById("my-tag", Organization.DEFAULT)).thenReturn(Maybe.empty());

        TestObserver testObserver = new TestObserver();
        tagService.delete("my-tag", Organization.DEFAULT, null).subscribe(testObserver);

        testObserver.assertError(TagNotFoundException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete_technicalException() {
        when(tagRepository.findById("my-tag", Organization.DEFAULT))
                .thenReturn(Maybe.error(TechnicalException::new));

        TestObserver testObserver = new TestObserver();
        tagService.delete("my-tag", Organization.DEFAULT, null).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }
}
