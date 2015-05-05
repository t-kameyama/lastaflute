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
package org.lastaflute.core.mail;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.dbflute.mail.PostOffice;
import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.SMailDeliveryDepartment;
import org.lastaflute.core.direction.FwAssistantDirector;
import org.lastaflute.core.direction.OptionalCoreDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.6.0 (2015/05/04 Monday)
 */
public class Postbox {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(Postbox.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The assistant directory (AD) for framework. (NotNull: after initialization) */
    @Resource
    protected FwAssistantDirector assistantDirector;

    /** Everybody knows, it's postOffice post office. (NullAllowed: null means no mail) */
    protected PostOffice postOffice;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initialize this component. <br>
     * This is basically called by DI setting file.
     */
    @PostConstruct
    public void initialize() {
        final OptionalCoreDirection direction = assistOptionalCoreDirection();
        final SMailDeliveryDepartment deliveryDepartment = direction.assistMailDeliveryDepartment();
        postOffice = deliveryDepartment != null ? new PostOffice(deliveryDepartment) : null;
        showBootLogging();
    }

    protected OptionalCoreDirection assistOptionalCoreDirection() {
        return assistantDirector.assistOptionalCoreDirection();
    }

    protected void showBootLogging() {
        if (logger.isInfoEnabled()) {
            logger.info("[Postbox]");
            logger.info(" postOffice: " + (postOffice != null ? postOffice : "*no used"));
        }
    }

    // ===================================================================================
    //                                                                             Deliver
    //                                                                             =======
    public void deliver(Postcard post) {
        assertPostOfficeWorks(post);
        postOffice.deliver(post);
    }

    protected void assertPostOfficeWorks(Postcard post) {
        if (postOffice == null) {
            String msg = "No mail settings so cannot send your mail: " + post;
            throw new IllegalStateException(msg);
        }
    }
}
