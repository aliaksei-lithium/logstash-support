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

package com.commercehub.logging.logback.redis

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.LayoutBase
import ch.qos.logback.core.status.Status
import com.commercehub.RedisResource
import org.junit.ClassRule
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification

@IgnoreIf({ isUnsupportedOs() })
class RedisAppenderSpecification extends Specification {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(RedisAppenderSpecification.getClass())
    private static final KEY = 'redisAppenderSpecification'
    private static final MESSAGE = 'I am an awesome message'

    private static boolean isUnsupportedOs() {
        def osName = System.getProperty('os.name').toLowerCase()
        return !(osName in ['mac os x', 'linux'])
    }

    def rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    Jedis jedis
    RedisAppender appender

    @Shared
    @ClassRule
    RedisResource redisResource

    def setup() {
        def port = redisResource.redisServer.port

        jedis = new Jedis('localhost', port)

        appender = new RedisAppender()
        appender.context = rootLogger.loggerContext
        appender.port = port
        appender.key = KEY
        appender.layout = new SimpleLayout()
        appender.start()
    }

    def cleanup() {
        appender.stop()
        jedis.close()
    }

    def "sends events to redis" () {
        given:
            def event = new LoggingEvent(null, logger, Level.INFO, MESSAGE, null, null)

        when:
            appender.append(event)
            def eventString = jedis.rpop(KEY)

        then: "appender did not report any issues while handling logging event"
            !appender.context.statusManager.copyOfStatusList.find { it.level == Status.ERROR }

        and:
            eventString == MESSAGE
    }

}

class SimpleLayout extends LayoutBase<ILoggingEvent> {

    @Override
    String doLayout(ILoggingEvent event) {
        return event.message
    }

}
