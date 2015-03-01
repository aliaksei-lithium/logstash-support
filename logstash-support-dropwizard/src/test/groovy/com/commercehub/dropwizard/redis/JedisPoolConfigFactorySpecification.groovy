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

package com.commercehub.dropwizard.redis

import spock.lang.Specification

class JedisPoolConfigFactorySpecification extends Specification {

    def jedisPoolConfigFactory = new JedisPoolConfigFactory()

    def "builds a JedisPoolConfig with properties specified"() {
        given:
            // These values intentionally differ from the defaults the factory would use if they weren't specified
            jedisPoolConfigFactory.maxTotal = 10
            jedisPoolConfigFactory.maxIdle = 9
            jedisPoolConfigFactory.minIdle = 1
            jedisPoolConfigFactory.lifo = false
            jedisPoolConfigFactory.maxWaitMillis = 1000
            jedisPoolConfigFactory.minEvictableIdleTimeMillis = 30000
            jedisPoolConfigFactory.softMinEvictableIdleTimeMillis = 10000
            jedisPoolConfigFactory.numTestsPerEvictionRun = 1
            jedisPoolConfigFactory.testOnBorrow = true
            jedisPoolConfigFactory.testOnReturn = true
            jedisPoolConfigFactory.testWhileIdle = false
            jedisPoolConfigFactory.timeBetweenEvictionRunsMillis = 10000
            jedisPoolConfigFactory.evictionPolicyClassName = 'com.acme.SomeEvictionPolicy'
            jedisPoolConfigFactory.blockWhenExhausted = false
            jedisPoolConfigFactory.jmxEnabled = false
            jedisPoolConfigFactory.jmxNamePrefix = 'foo'

        when:
            def config = jedisPoolConfigFactory.build()

        then:
            config.maxTotal == 10
            config.maxIdle == 9
            config.minIdle == 1
            !config.lifo
            config.maxWaitMillis == 1000
            config.minEvictableIdleTimeMillis == 30000
            config.softMinEvictableIdleTimeMillis == 10000
            config.numTestsPerEvictionRun == 1
            config.testOnBorrow
            config.testOnReturn
            !config.testWhileIdle
            config.timeBetweenEvictionRunsMillis == 10000
            config.evictionPolicyClassName == 'com.acme.SomeEvictionPolicy'
            !config.blockWhenExhausted
            !config.jmxEnabled
            config.jmxNamePrefix == 'foo'
    }

}
