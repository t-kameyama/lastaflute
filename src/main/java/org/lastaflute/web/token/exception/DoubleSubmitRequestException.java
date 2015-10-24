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
package org.lastaflute.web.token.exception;

import org.lastaflute.web.exception.MessageKeyApplicationException;

/**
 * @author jflute
 */
public class DoubleSubmitRequestException extends MessageKeyApplicationException {

    private static final long serialVersionUID = 1L;

    public DoubleSubmitRequestException(String msg, String messageKey) {
        super(msg, messageKey);
    }

    public DoubleSubmitRequestException(String msg, String messageKey, Throwable cause) {
        super(msg, messageKey, cause);
    }
}