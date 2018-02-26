/*
 * Copyright 2015-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.lastaflute.web.aspect;

import org.lastaflute.di.core.customizer.AspectCustomizer;
import org.lastaflute.di.core.customizer.ext.ConcreteDrivenCustomizerChain;

/**
 * The customizer chain for action. <br>
 * It also has concrete-driven features.
 * @author jflute
 */
public class ActionCustomizerChain extends ConcreteDrivenCustomizerChain {

    @Override
    protected AspectCustomizer newAspectCustomizer() {
        return new ActionAspectCustomizer();
    }
}
