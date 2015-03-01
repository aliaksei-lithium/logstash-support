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

package com.commercehub.logging.log4j.redis;

import com.commercehub.logging.log4j.LogstashEventLayout;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.easymock.EasyMock;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PooledRedisAppenderTest {

    Category logger = Logger.getLogger(PooledRedisAppenderTest.class);
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

    @Test
    public void testBrokenConnectionCausesReturnOfBrokenJedisToPool() throws Throwable {

        LoggingEvent loggingEvent = new LoggingEvent(
                null, logger, timeStamp, level, message, threadName,
                throwableInformation, ndc, info, properties);

        JedisPool pool = EasyMock.createMock(JedisPool.class);
        Jedis jedis = new Jedis("Test");

        PooledRedisAppender appender = new PooledRedisAppender();
        appender.setKey("key");
        appender.setLayout(new LogstashEventLayout());
        appender.setJedisPoolConfig(new JedisPoolConfig());
        appender.activateOptions();
        appender.setBatchSize(1);

        appender.setJedisPool(pool);
        appender.append(loggingEvent);

        EasyMock.expect(pool.getResource()).andReturn(jedis).times(1);

        // make sure we return the resource, this call being checked below by verify(pool)
        pool.returnBrokenResource(jedis);
        EasyMock.replay(pool);
        appender.run();

        EasyMock.verify(pool);
    }

    @Test
    public void testJedisPulledFromPoolAndUsedRightWhenHappy() throws Throwable {
        LoggingEvent loggingEvent = new LoggingEvent(
                null, logger, timeStamp, level, message, threadName,
                throwableInformation, ndc, info, properties);

        JedisPool pool = EasyMock.createMock(JedisPool.class);
        Jedis jedis = EasyMock.createMock(Jedis.class);

        PooledRedisAppender appender = new PooledRedisAppender();
        appender.setKey("key");
        appender.setLayout(new LogstashEventLayout());
        appender.setJedisPoolConfig(new JedisPoolConfig());
        appender.activateOptions();
        appender.setBatchSize(1);

        appender.setJedisPool(pool);
        appender.append(loggingEvent);

        // make sure call to pool to get resource.
        EasyMock.expect(pool.getResource()).andReturn(jedis).times(1);

        // make sure we push something!
        EasyMock.expect(jedis.rpush("key", "")).andReturn(2l);

        // make sure resources were returned
        pool.returnResource(jedis);

        EasyMock.replay(pool);
        appender.run();

        EasyMock.verify(pool);
    }

}
