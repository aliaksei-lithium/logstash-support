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

package com.commercehub.dropwizard.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import com.commercehub.dropwizard.redis.JedisPoolConfigFactory
import com.commercehub.logging.logback.AsyncAppender
import com.commercehub.logging.logback.LogstashEventLayout
import com.commercehub.logging.logback.redis.RedisAppender
import io.dropwizard.util.Duration
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPoolConfig
import spock.lang.Specification

class LogstashAppenderFactorySpecification extends Specification {

    def rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    def jedisPoolConfig = new JedisPoolConfig()
    def jedisPoolConfigFactory = Mock(JedisPoolConfigFactory)
    def logstashEventLayout = new LogstashEventLayout()
    def logstashEventLayoutFactory = Mock(LogstashEventLayoutFactory)
    def appenderFactory = new LogstashAppenderFactory()

    def setup() {
        jedisPoolConfigFactory.build() >> jedisPoolConfig
        logstashEventLayoutFactory.build() >> logstashEventLayout
    }

    def "builds a LogstashAppender with properties specified"() {
        when:
            appenderFactory.host = 'foo.acme.com'
            appenderFactory.port = 6380
            appenderFactory.timeout = Duration.seconds(3)
            appenderFactory.password = 'somepassword'
            appenderFactory.database = 1
            appenderFactory.key = 'somekey'
            appenderFactory.threshold = Level.DEBUG
            appenderFactory.queueSize = 128
            appenderFactory.discardingThreshold = 20
            appenderFactory.pool = jedisPoolConfigFactory
            appenderFactory.layout = logstashEventLayoutFactory

            def appender = appenderFactory.build(rootLogger.loggerContext, null, null)

            AsyncAppender asyncAppender = null
            RedisAppender redisAppender = null
            List<Filter<ILoggingEvent>> filters = null
            if (appender instanceof AsyncAppender) {
                asyncAppender = appender
                def wrappedAppender = asyncAppender.getAppender('redis-appender')
                if (wrappedAppender instanceof RedisAppender) {
                    redisAppender = wrappedAppender
                }
                filters = wrappedAppender.copyOfAttachedFiltersList
            }

        then:
            asyncAppender.name == 'logstash-appender'
            asyncAppender.queueSize == 128
            asyncAppender.discardingThreshold == 20

            redisAppender.name == 'redis-appender'
            redisAppender.host == 'foo.acme.com'
            redisAppender.port == 6380
            redisAppender.timeout == 3000
            redisAppender.password == 'somepassword'
            redisAppender.database == 1
            redisAppender.key == 'somekey'
            redisAppender.poolConfig.is(jedisPoolConfig)
            redisAppender.layout.is(logstashEventLayout)

            filters != null
            filters.size() == 1
            filters.find { it instanceof ThresholdFilter && it.level == Level.DEBUG }
    }

}
