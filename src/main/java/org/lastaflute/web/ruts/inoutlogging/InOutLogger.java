/*
 * Copyright 2015-2017 the original author or authors.
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
package org.lastaflute.web.ruts.inoutlogging;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.dbflute.optional.OptionalThing;
import org.dbflute.util.DfTraceViewUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;
import org.lastaflute.core.mail.RequestedMailCount;
import org.lastaflute.db.dbflute.callbackcontext.traceablesql.RequestedSqlCount;
import org.lastaflute.web.LastaWebKey;
import org.lastaflute.web.ruts.process.ActionRuntime;
import org.lastaflute.web.servlet.request.RequestManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.0.0 (2017/08/11 Friday)
 */
public class InOutLogger {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String LOGGER_NAME = "lastaflute.inout";
    protected static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);
    protected static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // ===================================================================================
    //                                                                             Logging
    //                                                                             =======
    protected void log(String msg) { // define at top for small line number
        logger.info(msg);
    }

    // ===================================================================================
    //                                                                               Show
    //                                                                              ======
    public void showInOutLog(RequestManager requestManager, ActionRuntime runtime, InOutLogKeeper keeper) {
        // no async for now, because this process is after response committed by jflute (2017/08/11)
        try {
            doShowInOutLog(requestManager, runtime, keeper);
        } catch (RuntimeException continued) { // not main process
            logger.info("*Failed to show in-out log: " + runtime.getRequestPath(), continued);
        }
    }

    protected void doShowInOutLog(RequestManager requestManager, ActionRuntime runtime, InOutLogKeeper keeper) {
        final StringBuilder sb = new StringBuilder();
        final String requestPath = requestManager.getRequestPath();
        sb.append(requestPath);
        final String actionName = runtime.getActionType().getSimpleName();
        final String methodName = runtime.getActionExecute().getExecuteMethod().getName();
        sb.append(" ").append(actionName).append("@").append(methodName).append("()");
        final String beginExp = keeper.getBeginDateTime().map(begin -> {
            return dateTimeFormatter.format(begin);
        }).orElse("no begun"); // basically no way, just in case
        sb.append(" (").append(beginExp).append(")");
        keeper.getBeginDateTime().ifPresent(begin -> {
            final long before = DfTypeUtil.toDate(begin).getTime();
            final long after = DfTypeUtil.toDate(requestManager.getTimeManager().currentDateTime()).getTime();
            sb.append(" [").append(DfTraceViewUtil.convertToPerformanceView(after - before)).append("]");
        });
        requestManager.getHeaderUserAgent().ifPresent(userAgent -> {
            sb.append(" {").append(Srl.cut(userAgent, 50, "...")).append("}");
        });
        final RuntimeException failureCause = runtime.getFailureCause();
        if (failureCause != null) {
            sb.append(" *").append(failureCause.getClass().getSimpleName());
            sb.append(" #").append(Integer.toHexString(failureCause.hashCode()));
        }
        boolean alreadyLineSep = false;
        final String requestParameterExp = buildRequestParameterExp(keeper);
        if (requestParameterExp != null) {
            alreadyLineSep = buildInOut(sb, "requestParameter", requestParameterExp, alreadyLineSep);
        }
        if (keeper.getRequestBody().isPresent()) {
            final String body = keeper.getRequestBody().get();
            alreadyLineSep = buildInOut(sb, "requestBody", body, alreadyLineSep);
        }
        if (keeper.getResponseBody().isPresent()) {
            final String body = keeper.getResponseBody().get();
            alreadyLineSep = buildInOut(sb, "responseBody", body, alreadyLineSep);
        }
        final OptionalThing<RequestedSqlCount> optSql =
                requestManager.getAttribute(LastaWebKey.DBFLUTE_SQL_COUNT_KEY, RequestedSqlCount.class);
        if (optSql.isPresent()) {
            final RequestedSqlCount count = optSql.get();
            if (count.getTotalCountOfSql() > 0) {
                alreadyLineSep = buildInOut(sb, "sqlCount", count.toString(), alreadyLineSep);
            }
        }
        final OptionalThing<RequestedMailCount> optMail =
                requestManager.getAttribute(LastaWebKey.MAILFLUTE_MAIL_COUNT_KEY, RequestedMailCount.class);
        if (optMail.isPresent()) {
            final RequestedMailCount count = optMail.get();
            if (count.getCountOfPosting() > 0) {
                alreadyLineSep = buildInOut(sb, "mailCount", count.toString(), alreadyLineSep);
            }
        }
        log(sb.toString());
    }

    protected boolean buildInOut(StringBuilder sb, String title, String value, boolean alreadyLineSep) {
        boolean nowLineSep = alreadyLineSep;
        if (value.contains("\n")) {
            sb.append("\n").append(title).append(":").append("\n");
            nowLineSep = true;
        } else {
            sb.append(alreadyLineSep ? "\n" : " ").append(title).append(":");
        }
        sb.append(value == null || !value.isEmpty() ? value : "(empty)");
        return nowLineSep;
    }

    // ===================================================================================
    //                                                                   Request Parameter
    //                                                                   =================
    protected String buildRequestParameterExp(InOutLogKeeper keeper) {
        final Map<String, Object> parameterMap = keeper.getRequestParameterMap();
        if (parameterMap.isEmpty()) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        parameterMap.forEach((key, value) -> {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(key).append("=");
            if (value instanceof Object[]) {
                final Object[] objArray = (Object[]) value;
                if (objArray.length == 1) {
                    sb.append(objArray[0]);
                } else {
                    int index = 0;
                    sb.append("[");
                    for (Object obj : objArray) {
                        if (index > 0) {
                            sb.append(", ");
                        }
                        sb.append(obj);
                        ++index;
                    }
                    sb.append("]");
                }
            } else {
                sb.append(value);
            }
        });
        sb.insert(0, "{").append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected String getDelimiter(String exp) {
        return exp.contains("\n") ? "\n" : " ";
    }
}