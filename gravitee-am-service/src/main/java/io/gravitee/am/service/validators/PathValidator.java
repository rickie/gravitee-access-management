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

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.service.exception.InvalidPathException;
import io.reactivex.Completable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PathValidator {

    private static final Pattern PATH_PATTERN = Pattern.compile("/?[a-z0-9-._]+(?:/[a-z0-9-._]+)*/?|/", Pattern.CASE_INSENSITIVE);

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(PathValidator.validate_migrated(path))", imports = {"io.gravitee.am.service.validators.PathValidator", "reactor.adapter.rxjava.RxJava2Adapter"})
@Deprecated
public static Completable validate(String path) {
 return RxJava2Adapter.monoToCompletable(validate_migrated(path));
}
public static Mono<Void> validate_migrated(String path) {

        if (path == null || "".equals(path)) {
            return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.error(new InvalidPathException("Path must not be null or empty"))));
        }

        Matcher matcher = PATH_PATTERN.matcher(path);

        if (!matcher.matches()) {
            return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.error(new InvalidPathException("Path [" + path + "] is invalid"))));
        }

        if (!path.startsWith("/")) {
            return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.error(new InvalidPathException("Path must start with a '/'"))));
        }

        if (path.contains("//")) {
            return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.error(new InvalidPathException("Path [" + path + "] is invalid"))));
        }

        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.empty()));
    }
}
