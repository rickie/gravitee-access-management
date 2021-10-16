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
package io.gravitee.am.service.validators;

import static org.junit.Assert.*;

import io.gravitee.am.service.exception.InvalidPathException;
import org.junit.Test;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PathValidatorTest {

    @Test
    public void validate() {

        Throwable throwable = RxJava2Adapter.monoToCompletable(PathValidator.validate_migrated("/test")).blockingGet();

        assertNull(throwable);
    }

    @Test
    public void validateSpecialCharacters() {

        Throwable throwable = RxJava2Adapter.monoToCompletable(PathValidator.validate_migrated("/test/subpath/subpath2_with-and.dot/AND_UPPERCASE")).blockingGet();

        assertNull(throwable);
    }

    @Test
    public void validate_invalidEmptyPath() {

        Throwable throwable = RxJava2Adapter.monoToCompletable(PathValidator.validate_migrated("")).blockingGet();

        assertNotNull(throwable);
        assertTrue(throwable instanceof InvalidPathException);
    }

    @Test
    public void validate_nullPath() {

        Throwable throwable = RxJava2Adapter.monoToCompletable(PathValidator.validate_migrated(null)).blockingGet();

        assertNotNull(throwable);
        assertTrue(throwable instanceof InvalidPathException);
    }

    @Test
    public void validate_multipleSlashesPath() {

        Throwable throwable = RxJava2Adapter.monoToCompletable(PathValidator.validate_migrated("/////test////")).blockingGet();

        assertNotNull(throwable);
        assertTrue(throwable instanceof InvalidPathException);
    }

    @Test
    public void validate_invalidCharacters() {

        Throwable throwable = RxJava2Adapter.monoToCompletable(PathValidator.validate_migrated("/test$:\\;,+")).blockingGet();

        assertNotNull(throwable);
        assertTrue(throwable instanceof InvalidPathException);
    }
}