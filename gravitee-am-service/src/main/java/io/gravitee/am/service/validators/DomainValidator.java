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
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.VirtualHost;
import io.gravitee.am.service.exception.InvalidDomainException;
import io.reactivex.Completable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DomainValidator {

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(DomainValidator.validate_migrated(domain, domainRestrictions))", imports = {"io.gravitee.am.service.validators.DomainValidator", "reactor.adapter.rxjava.RxJava2Adapter"})
@Deprecated
public static Completable validate(Domain domain, List<String> domainRestrictions) {
 return RxJava2Adapter.monoToCompletable(validate_migrated(domain, domainRestrictions));
}
public static Mono<Void> validate_migrated(Domain domain, List<String> domainRestrictions) {

        List<Completable> chain = new ArrayList<>();

        if (domain.getName().contains("/")) {
            return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.error(new InvalidDomainException("Domain name cannot contain '/' character"))));
        }

        if(!CollectionUtils.isEmpty(domainRestrictions) && !domain.isVhostMode()) {
            return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.error(new InvalidDomainException("Domain can only work in vhost mode"))));
        }

        if (domain.isVhostMode()) {
            if (domain.getVhosts() == null || domain.getVhosts().isEmpty()) {
                return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.error(new InvalidDomainException("VHost mode requires at least one VHost"))));
            }

            // Check at there is only one vhost flagged with override entrypoint.
            long count = domain.getVhosts().stream().filter(VirtualHost::isOverrideEntrypoint).count();
            if(count > 1) {
                return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.error(new InvalidDomainException("Only one vhost can be used to override entrypoint"))));
            } else if(count == 0) {
                return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.error(new InvalidDomainException("You must select one vhost to override entrypoint"))));
            }

            chain.addAll(domain.getVhosts().stream()
                    .map(vhost -> VirtualHostValidator.validate(vhost, domainRestrictions))
                    .collect(Collectors.toList()));
        } else {
            if("/".equals(domain.getPath())) {
                return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.error(new InvalidDomainException("'/' path is not allowed in context-path mode"))));
            }

            chain.add(PathValidator.validate(domain.getPath()));
        }

        return RxJava2Adapter.completableToMono(Completable.merge(chain));
    }
}
