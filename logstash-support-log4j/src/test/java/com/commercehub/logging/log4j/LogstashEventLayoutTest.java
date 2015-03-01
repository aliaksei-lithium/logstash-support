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
import net.minidev.json.JSONValue;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LogstashEventLayoutTest {

    Category logger = Logger.getLogger(LogstashEventLayoutTest.class);
    long timeStamp = new Date(1397521334308L).getTime();
    Level level = Level.ERROR;
    Object message = "I am an awesome message";
    String threadName = "i-am-a-thread";
    String exceptionMessage = "I am an Exception.";
    String nestedExceptionMessage = "I am a nested Exception.";
    RuntimeException runtimeException = new RuntimeException(exceptionMessage, new RuntimeException(nestedExceptionMessage));
    ThrowableInformation throwableInformation = new ThrowableInformation(runtimeException);
    String ndc = "i am a ndc";
    LocationInfo info = new LocationInfo("file1", "classname1", "method1", "line1");
    Map<String, String> properties = new HashMap<>();

    LoggingEvent loggingEvent;

    @Before
    public void setUp() throws Exception {
        properties.put("Property1", "Value1");
        properties.put("Property2!", "Value2!");

        loggingEvent = new LoggingEvent(
                null, logger, timeStamp, level, message, threadName,
                throwableInformation, ndc, info, properties);
    }

    @Test
    public void testFormat() throws Exception {
        LogstashEventLayout logstashEventLayout = new LogstashEventLayout();
        logstashEventLayout.setUserFields("app:myApp,someField:myField");
        logstashEventLayout.setHost("some-host");

        String theEvent = logstashEventLayout.format(loggingEvent);

        JSONObject parsedActual = (JSONObject) JSONValue.parse(theEvent);
        JSONObject parsedActualException = (JSONObject) parsedActual.get("exception");

        assertEquals(properties, parsedActual.get("mdc"));
        assertEquals(level.toString(), parsedActual.get("level"));
        assertEquals(1, parsedActual.get("@version"));
        assertEquals(exceptionMessage, parsedActualException.get("message"));
        assertEquals("java.lang.RuntimeException", parsedActualException.get("class"));
        assertTrue(((String) parsedActualException.get("stacktrace")).contains(nestedExceptionMessage));
        assertEquals(message, parsedActual.get("message"));
        assertEquals(LogstashEventLayoutTest.class.getCanonicalName(), parsedActual.get("logger_full"));
        assertEquals(LogstashEventLayoutTest.class.getSimpleName(), parsedActual.get("logger_simple"));
        assertEquals(threadName, parsedActual.get("thread"));
        assertEquals("2014-04-15T00:22:14.308Z", parsedActual.get("@timestamp"));
        assertEquals("myApp", parsedActual.get("app"));
        assertEquals("myField", parsedActual.get("someField"));
        assertEquals("some-host", parsedActual.get("host"));
    }

}
