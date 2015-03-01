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

package com.commercehub.logging.logback.redis;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * A {@link ch.qos.logback.core.Appender} that sends logging events to Redis.
 * It is highly recommended that this appender be wrapped in a {@link com.commercehub.logging.logback.AsyncAppender}.
 */
public class RedisAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = Protocol.DEFAULT_PORT;
    public static final int DEFAULT_TIMEOUT = Protocol.DEFAULT_TIMEOUT;
    public static final int DEFAULT_DATABASE = Protocol.DEFAULT_DATABASE;

    private JedisPoolConfig poolConfig = new JedisPoolConfig();
    private String host = DEFAULT_HOST;
    private int port = DEFAULT_PORT;
    private int timeout = DEFAULT_TIMEOUT;
    private String password = null;
    private int database = DEFAULT_DATABASE;
    private String key = null;
    private Layout<ILoggingEvent> layout;

    private JedisPool pool;

    @Override
    public void start() {
        try {
            pool = new JedisPool(poolConfig, host, port, timeout, password, database);
            super.start();
        } catch (Exception e) {
            addError("Failed to create redis client pool", e);
        }
    }

    @Override
    protected void append(ILoggingEvent event) {
        Jedis client = getClient();
        if (client == null) {
            return;
        }

        String eventString = applyLayout(event);
        if (eventString == null) {
            return;
        }

        sendEventString(client, eventString);
    }

    private Jedis getClient() {
        Jedis client;
        try {
            client = pool.getResource();
        } catch (JedisConnectionException e) {
            addError("Problem occurred while getting redis client from pool; event will be lost", e);
            client = null;
        }
        return client;
    }

    private String applyLayout(ILoggingEvent event) {
        String eventString;
        try {
            eventString = layout.doLayout(event);
        } catch (Exception e) {
            addError("Failed to layout event; event will be lost", e);
            eventString = null;
        }
        return eventString;
    }

    private void sendEventString(Jedis client, String eventString) {
        try {
            client.rpush(key, eventString);
        } catch (Exception e) {
            addError("Problem occurred while sending event to redis; event will be lost", e);
            pool.returnBrokenResource(client);
            client = null;
        } finally {
            if (client != null) {
                pool.returnResource(client);
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        try {
            pool.destroy();
        } catch (Exception e) {
            addError("Problem occurred while destroying redis client pool", e);
        }
    }

    public JedisPoolConfig getPoolConfig() {
        return poolConfig;
    }

    public void setPoolConfig(JedisPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Layout<ILoggingEvent> getLayout() {
        return layout;
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

}
