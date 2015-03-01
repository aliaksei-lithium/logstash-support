/*
 * Copyright (C) 2012 by Pavlo Baron (pb at pbit dot org)
 * Copyright (C) 2015 Commerce Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commercehub.logging.log4j.redis;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.SafeEncoder;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.*;

public class PooledRedisAppender extends AppenderSkeleton implements Runnable {

    private JedisPoolConfig jedisPoolConfig;
    private JedisPool jedisPool;

    private String host = "localhost";
    private int port = 6379;
    private String password;
    private String key;

    private int batchSize = 100;
    private long period = 500;
    private boolean alwaysBatch = true;
    private boolean purgeOnFailure = true;
    private boolean daemonThread = true;

    private int messageIndex = 0;
    private Queue<LoggingEvent> events;
    private byte[][] batch;

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> task;

    @Override
    public void activateOptions() {
        try {
            super.activateOptions();

            if (key == null) throw new IllegalStateException("Must set 'key'");

            if (executor == null) executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("RedisAppender", daemonThread));

            if (task != null && !task.isDone()) task.cancel(true);

            reInitializeJedisPool();

            events = new ConcurrentLinkedQueue<>();
            batch = new byte[batchSize][];
            messageIndex = 0;

            task = executor.scheduleWithFixedDelay(this, period, period, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LogLog.error("Error during activateOptions", e);
        }
    }

    private void reInitializeJedisPool() {
        if (jedisPool != null) {
            jedisPool.destroy();
        }

        if (jedisPoolConfig == null) {
            jedisPoolConfig = new JedisPoolConfig();
        }
        jedisPool = new JedisPool(jedisPoolConfig, host, port, Protocol.DEFAULT_TIMEOUT, password);
    }

    @Override
    protected void append(LoggingEvent event) {
        try {
            populateEvent(event);
            events.add(event);
        } catch (Exception e) {
            errorHandler.error("Error populating event and adding to queue", e, ErrorCode.GENERIC_FAILURE, event);
        }
    }

    protected void populateEvent(LoggingEvent event) {
        event.getThreadName();
        event.getRenderedMessage();
        event.getNDC();
        event.getMDCCopy();
        event.getThrowableStrRep();
    }

    @Override
    public void close() {
        try {
            task.cancel(false);
            executor.shutdown();
            jedisPool.destroy();
        } catch (Exception e) {
            errorHandler.error(e.getMessage(), e, ErrorCode.CLOSE_FAILURE);
        }
    }

    private Jedis getJedisFromPool() {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
        } catch (JedisConnectionException e) {
            LogLog.error("Exception getting Jedis from pool", e);
        }
        return jedis;
    }

    @Override
    public void run() {
        try {
            if (messageIndex == batchSize) push();

            LoggingEvent event;
            while ((event = events.poll()) != null) {
                try {
                    String message = layout.format(event);
                    batch[messageIndex++] = SafeEncoder.encode(message);
                } catch (Exception e) {
                    errorHandler.error(e.getMessage(), e, ErrorCode.GENERIC_FAILURE, event);
                }

                if (messageIndex == batchSize) push();
            }

            if (!alwaysBatch && messageIndex > 0) push();
        } catch (Exception e) {
            errorHandler.error(e.getMessage(), e, ErrorCode.WRITE_FAILURE);
        }
    }

    private void push() {
        Jedis jedis = getJedisFromPool();
        if (jedis == null) {
            purgeEventQueue();
            return;
        }

        LogLog.debug("Sending " + messageIndex + " log messages to Redis");
        try {
            jedis.rpush(SafeEncoder.encode(key),
                    batchSize == messageIndex
                            ? batch
                            : Arrays.copyOf(batch, messageIndex));
            messageIndex = 0;
        } catch (JedisConnectionException e) {
            LogLog.error("Exception sending log messages to Redis.", e);
            // returnBrokenResource when the state of the object is unrecoverable
            jedisPool.returnBrokenResource(jedis);
            jedis = null;
            purgeEventQueue();
        } finally {
            // It's important to return the Jedis instance to the pool once you've finished using it
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    private void purgeEventQueue() {
        if (purgeOnFailure) {
            LogLog.debug("Purging event queue");
            events.clear();
            messageIndex = 0;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setHost(String host) {
        this.host = host;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setPort(int port) {
        this.port = port;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setPassword(String password) {
        this.password = password;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setPeriod(long millis) {
        this.period = millis;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setPurgeOnFailure(boolean purgeOnFailure) {
        this.purgeOnFailure = purgeOnFailure;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setAlwaysBatch(boolean alwaysBatch) {
        this.alwaysBatch = alwaysBatch;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setDaemonThread(boolean daemonThread){
        this.daemonThread = daemonThread;
    }

    public boolean requiresLayout() {
        return true;
    }

    public void setJedisPoolConfig(JedisPoolConfig config){
        this.jedisPoolConfig = config;
    }

    // support testing
    protected void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

}
