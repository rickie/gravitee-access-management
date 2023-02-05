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
package io.gravitee.am.gateway.handler.common.vertx.web.handler.impl.internal.mfa.filter;

import static java.util.Objects.isNull;

import io.gravitee.am.common.factor.FactorType;
import io.gravitee.am.gateway.handler.common.factor.FactorManager;
import io.gravitee.am.model.Factor;

import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Rémi SULTAN (remi.sultan at graviteesource.com)
 * @author GraviteeSource Team
 */
public class NoFactorFilter implements Supplier<Boolean> {

    private final Set<String> factors;
    private final FactorManager factorManager;

    public NoFactorFilter(Set<String> factors, FactorManager factorManager) {
        this.factors = factors;
        this.factorManager = factorManager;
    }

    @Override
    public Boolean get() {
        return isNull(factors) || factors.isEmpty() || onlyRecoveryCodeFactor();
    }

    private boolean onlyRecoveryCodeFactor() {
        if (factors.size() == 1) {
            String factorId = factors.stream().findFirst().get();
            Factor factor = factorManager.getFactor(factorId);
            return factor.getFactorType().equals(FactorType.RECOVERY_CODE);
        }
        return false;
    }
}
