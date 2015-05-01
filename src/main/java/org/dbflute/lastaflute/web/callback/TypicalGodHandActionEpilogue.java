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
package org.dbflute.lastaflute.web.callback;

import java.lang.reflect.Method;

import org.dbflute.bhv.proposal.callback.ExecutedSqlCounter;
import org.dbflute.hook.CallbackContext;
import org.dbflute.hook.SqlStringFilter;
import org.dbflute.lastaflute.db.dbflute.accesscontext.PreparedAccessContext;
import org.dbflute.lastaflute.web.servlet.request.RequestManager;
import org.dbflute.lastaflute.web.servlet.request.ResponseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 */
public class TypicalGodHandActionEpilogue {

    private static final Logger LOG = LoggerFactory.getLogger(TypicalGodHandActionEpilogue.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final RequestManager requestManager;
    protected final ResponseManager responseManager;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TypicalGodHandActionEpilogue(TypicalGodHandResource resource) {
        this.requestManager = resource.getRequestManager();
        this.responseManager = resource.getResponseManager();
    }

    // ===================================================================================
    //                                                                            Prologue
    //                                                                            ========
    public void performEpilogue(ActionRuntimeMeta executeMeta) { // fixed process
        if (executeMeta.isForwardToHtml()) {
            arrangeNoCacheResponseWhenJsp(executeMeta);
        }
        handleSqlCount(executeMeta);
        clearCallbackContext();
        clearPreparedAccessContext();
    }

    protected void arrangeNoCacheResponseWhenJsp(ActionRuntimeMeta executeMeta) {
        responseManager.addNoCache();
    }

    // ===================================================================================
    //                                                                    Callback Context
    //                                                                    ================
    /**
     * Handle count of SQL execution in the request.
     * @param executeMeta The meta of action execute. (NotNull)
     */
    protected void handleSqlCount(final ActionRuntimeMeta executeMeta) {
        final CallbackContext context = CallbackContext.getCallbackContextOnThread();
        if (context == null) {
            return;
        }
        final SqlStringFilter filter = context.getSqlStringFilter();
        if (filter == null || !(filter instanceof ExecutedSqlCounter)) {
            return;
        }
        final ExecutedSqlCounter counter = ((ExecutedSqlCounter) filter);
        final int limitCountOfSql = getLimitCountOfSql(executeMeta);
        if (limitCountOfSql >= 0 && counter.getTotalCountOfSql() > limitCountOfSql) {
            handleTooManySqlExecution(executeMeta, counter);
        }
        final String exp = counter.toLineDisp();
        requestManager.setAttribute(RequestManager.DBFLUTE_SQL_COUNT_KEY, exp); // logged by logging filter
    }

    /**
     * Handle too many SQL executions.
     * @param executeMeta The meta of action execute. (NotNull)
     * @param sqlCounter The counter object for SQL executions. (NotNull)
     */
    protected void handleTooManySqlExecution(final ActionRuntimeMeta executeMeta, final ExecutedSqlCounter sqlCounter) {
        final String actionDisp = buildActionDisp(executeMeta);
        LOG.warn("*Too many SQL executions: " + sqlCounter.getTotalCountOfSql() + " in " + actionDisp);
    }

    protected String buildActionDisp(ActionRuntimeMeta executeMeta) {
        final Method method = executeMeta.getExecuteMethod();
        final Class<?> declaringClass = method.getDeclaringClass();
        return declaringClass.getSimpleName() + "." + method.getName() + "()";
    }

    /**
     * Get the limit count of SQL execution. <br>
     * You can override if you need.
     * @param executeMeta The meta of action execute. (NotNull)
     * @return The max count allowed for SQL executions. (MinusAllowed: if minus, no check)
     */
    protected int getLimitCountOfSql(ActionRuntimeMeta executeMeta) {
        return 30; // as default
    }

    /**
     * Clear callback context. <br>
     * This is called by callback process so you should NOT call this directly in your action.
     */
    protected void clearCallbackContext() {
        CallbackContext.clearSqlStringFilterOnThread();
        CallbackContext.clearSqlFireHookOnThread();
    }

    // ===================================================================================
    //                                                                      Access Context
    //                                                                      ==============
    /**
     * Clear prepared access context. <br>
     * This is called by callback process so you should NOT call this directly in your action.
     */
    protected void clearPreparedAccessContext() { // called by callback
        PreparedAccessContext.clearAccessContextOnThread();
    }
}
