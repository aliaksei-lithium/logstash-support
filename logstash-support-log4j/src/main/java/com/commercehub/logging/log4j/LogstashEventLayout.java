/*
 * Copyright (C) 2015 Commerce Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commercehub.logging.log4j;

import net.minidev.json.JSONObject;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * This class is intentionally not thread-safe. Don't share an instance of this Layout across multiple Appenders. Don't
 * share instances of the Appenders that use this Layout across multiple Loggers.
 */
public class LogstashEventLayout extends Layout {

    private static final int version = 1;
    private final SimpleDateFormat simpleDateFormat;
    private String userFields;
    private Map<String, String> userFieldsMap = new HashMap<>();
    private boolean userFieldsNeedToBeParsed = false;
    private String host;

    public LogstashEventLayout() {
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            host = "unknown";
        }
    }

    @Override
    public String format(LoggingEvent loggingEvent) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("@version", version);
        jsonObject.put("@timestamp", simpleDateFormat.format(new Date(loggingEvent.getTimeStamp())));
        jsonObject.put("logger_full", loggingEvent.getLoggerName());
        jsonObject.put("level", loggingEvent.getLevel().toString());
        jsonObject.put("message", loggingEvent.getRenderedMessage());
        jsonObject.put("mdc", loggingEvent.getProperties());
        jsonObject.put("thread", loggingEvent.getThreadName());
        jsonObject.put("host", host);

        handleLoggerSimpleName(loggingEvent, jsonObject);

        handleThrowableInformation(loggingEvent, jsonObject);

        jsonObject.putAll(getUserFieldsMap());

        return jsonObject.toString();
    }

    private void handleLoggerSimpleName(LoggingEvent loggingEvent, JSONObject jsonObject) {
        String loggerName = loggingEvent.getLoggerName();
        if (loggerName != null) {
            jsonObject.put("logger_simple", loggerName.substring(loggerName.lastIndexOf('.') + 1));
        }
    }

    private void handleThrowableInformation(LoggingEvent loggingEvent, JSONObject jsonObject) {
        ThrowableInformation throwableInformation = loggingEvent.getThrowableInformation();
        if (throwableInformation != null) {
            //noinspection ThrowableResultOfMethodCallIgnored
            Throwable throwable = throwableInformation.getThrowable();
            if (throwable != null) {
                Map<String, String> exceptionMap = new HashMap<>();
                String canonicalName = throwable.getClass().getCanonicalName();
                if (canonicalName != null) {
                    exceptionMap.put("class", canonicalName);
                }

                String message = throwable.getMessage();
                if (message != null) {
                    exceptionMap.put("message", message);
                }

                String[] throwableStrReps = throwableInformation.getThrowableStrRep();
                if (throwableStrReps != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String throwableStrRep : throwableStrReps) {
                        stringBuilder.append(throwableStrRep);
                        stringBuilder.append("\n");
                    }
                    exceptionMap.put("stacktrace", stringBuilder.toString());
                }

                jsonObject.put("exception", exceptionMap);
            }
        }
    }

    public Map<String, String> getUserFieldsMap() {
        if (userFieldsNeedToBeParsed) {
            userFieldsMap = new HashMap<>();
            parseUserFields();
        }

        return userFieldsMap;
    }

    private void parseUserFields() {
        String userFields = this.userFields;
        if (userFields != null) {
            String[] userFieldPairs = userFields.split(",");
            for (String userFieldPair : userFieldPairs) {
                String[] splitUserFieldPair = userFieldPair.split(":", 2);
                String userFieldKey = splitUserFieldPair[0];
                String userFieldValue = splitUserFieldPair[1];
                if ((userFieldKey != null) && (userFieldValue != null)) {
                    userFieldsMap.put(userFieldKey, userFieldValue);
                }
            }
        }
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    @Override
    public void activateOptions() {
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setUserFields(String userFields) {
        this.userFields = userFields;
        userFieldsNeedToBeParsed = true;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setHost(String host) {
        this.host = host;
    }
}