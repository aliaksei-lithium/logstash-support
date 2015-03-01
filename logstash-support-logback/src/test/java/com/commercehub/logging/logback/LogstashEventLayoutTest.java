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

package com.commercehub.logging.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LogstashEventLayoutTest {

    Logger logger = (Logger) LoggerFactory.getLogger(LogstashEventLayoutTest.class);
    long timeStamp = new Date(1397521334308L).getTime();
    Level level = Level.ERROR;
    String message = "I am an awesome message";
    String threadName = "i-am-a-thread";
    String exceptionMessage = "I am an Exception.";
    String nestedExceptionMessage = "I am a nested Exception.";
    RuntimeException runtimeException = new RuntimeException(exceptionMessage, new RuntimeException(nestedExceptionMessage));
    Map<String, String> properties = new HashMap<>();

    LoggingEvent loggingEvent;

    @Before
    public void setUp() throws Exception {
        properties.put("Property1", "Value1");
        properties.put("Property2!", "Value2!");

        loggingEvent = new LoggingEvent(null, logger, level, message, runtimeException, null);
        loggingEvent.setTimeStamp(timeStamp);
        loggingEvent.setThreadName(threadName);
        loggingEvent.setMDCPropertyMap(properties);
    }

    @Test
    public void testFormat() throws Exception {
        LogstashEventLayout logstashEventLayout = new LogstashEventLayout();
        logstashEventLayout.setUserFields("app:myApp,someField:myField");
        logstashEventLayout.setHost("some-host");

        String theEvent = logstashEventLayout.doLayout(loggingEvent);

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
