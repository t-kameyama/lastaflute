/*
 * Copyright 2014-2015 the original author or authors.
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
package org.dbflute.lastaflute.web.aspect;

import java.lang.reflect.Method;

import org.dbflute.lasta.di.core.customizer.PointTxAttributeCustomizer;
import org.dbflute.lastaflute.web.Execute;

/**
 * @author jflute
 */
public class ActionTxAttributeCustomizer extends PointTxAttributeCustomizer {

    @Override
    protected boolean isImplicitTxSupportedMethod(Method method) {
        return isActionExecute(method);
    }

    protected boolean isActionExecute(final Method method) {
        return method.getAnnotation(Execute.class) != null;
    }
}
