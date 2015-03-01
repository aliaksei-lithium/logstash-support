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

package com.commercehub

import org.junit.rules.ExternalResource
import redis.embedded.RedisServer

class RedisResource extends ExternalResource {

    // Note: com.orange.redis-embedded:embedded-redis:0.5 only supports Mac OS X when using Redis version 2.8.5
    private static final REDIS_SERVER_VERSION = '2.8.5'

    private RedisServer redisServer

    RedisServer getRedisServer() {
        return redisServer
    }

    @Override
    protected void before() {
        redisServer = new RedisServer(REDIS_SERVER_VERSION, getPort())
        redisServer.start()
    }

    @Override
    protected void after() {
        try {
            redisServer.stop()
        } catch (InterruptedException ignored) {
        }
    }

    private static int getPort() {
        def localhost = InetAddress.localHost
        if (!localhost.loopbackAddress) {
            localhost = InetAddress.getByName('localhost')
        }
        def socket = new ServerSocket(0, 0, localhost)
        int port = socket.localPort
        socket.close()
        return port
    }

}
