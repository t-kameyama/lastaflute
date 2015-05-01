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
package org.dbflute.lastaflute.web.login;

import java.time.LocalDateTime;

import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 */
public interface SyncCheckable {

    /**
     * Get the latest date of synchronized check.
     * @return The optional date-time that specifies the latest check date. (NullAllowed: no check yet)
     */
    OptionalThing<LocalDateTime> getLastestSyncCheckDateTime();

    /**
     * Get the latest date of synchronized check.
     * @param checkDt The date that specifies the latest check date. (NullAllowed: checked at next access)
     */
    void setLastestSyncCheckDateTime(LocalDateTime checkDt);
}
