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

package com.commercehub.dropwizard.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.spi.FilterAttachable;
import com.commercehub.dropwizard.redis.JedisPoolConfigFactory;
import com.commercehub.logging.logback.AsyncAppender;
import com.commercehub.logging.logback.redis.RedisAppender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.AppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import io.dropwizard.util.Duration;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@JsonTypeName("logstash")
public class LogstashAppenderFactory implements AppenderFactory {

    private static final String LOGSTASH_APPENDER_NAME = "logstash-appender";
    private static final String REDIS_APPENDER_NAME = "redis-appender";

    @NotBlank
    private String host = RedisAppender.DEFAULT_HOST;

    @Min(0L)
    @Max(65535L)
    private int port = RedisAppender.DEFAULT_PORT;

    @NotNull
    private Duration timeout = Duration.milliseconds(RedisAppender.DEFAULT_TIMEOUT);

    private String password;

    @Min(0L)
    @Max(65535L)
    private int database = RedisAppender.DEFAULT_DATABASE;

    @NotBlank
    private String key;

    @NotNull
    private Level threshold = Level.INFO;

    @Min(1L)
    @Max(65535L)
    private int queueSize = AsyncAppender.DEFAULT_QUEUE_SIZE;

    @Min(-1L)
    @Max(65535L)
    private int discardingThreshold = AsyncAppender.DISCARDING_THRESHOLD_UNDEFINED;

    @Valid
    @NotNull
    private JedisPoolConfigFactory pool = new JedisPoolConfigFactory();

    @Valid
    @NotNull
    private LogstashEventLayoutFactory layout = new LogstashEventLayoutFactory();


    @Override
    public Appender build(LoggerContext context, String applicationName,
                          LayoutFactory layoutFactory, LevelFilterFactory levelFilterFactory,
                          AsyncAppenderFactory asyncAppenderFactory) {

        RedisAppender appender = new RedisAppender();
        appender.setContext(context);
        appender.setName(REDIS_APPENDER_NAME);
        appender.setPoolConfig(pool.build());
        appender.setHost(host);
        appender.setPort(port);
        appender.setTimeout((int) timeout.toMilliseconds());
        appender.setPassword(password);
        appender.setDatabase(database);
        appender.setKey(key);
        appender.setLayout(this.layout.build());
        addThresholdFilter(appender);
        appender.start();
        return wrapAsync(context, appender);
    }

    private void addThresholdFilter(FilterAttachable<ILoggingEvent> appender) {
        ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(threshold.toString());
        filter.start();
        appender.addFilter(filter);
    }

    private Appender<ILoggingEvent> wrapAsync(LoggerContext context, Appender<ILoggingEvent> appender) {
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(context);
        asyncAppender.setName(LOGSTASH_APPENDER_NAME);
        asyncAppender.setQueueSize(queueSize);
        asyncAppender.setDiscardingThreshold(discardingThreshold);
        asyncAppender.addAppender(appender);
        asyncAppender.start();
        return asyncAppender;
    }

    @JsonProperty
    public String getHost() {
        return host;
    }

    @JsonProperty
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty
    public Duration getTimeout() {
        return timeout;
    }

    @JsonProperty
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    @JsonProperty
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty
    public int getDatabase() {
        return database;
    }

    @JsonProperty
    public void setDatabase(int database) {
        this.database = database;
    }

    @JsonProperty
    public String getKey() {
        return key;
    }

    @JsonProperty
    public void setKey(String key) {
        this.key = key;
    }

    @JsonProperty
    public Level getThreshold() {
        return threshold;
    }

    @JsonProperty
    public void setThreshold(Level threshold) {
        this.threshold = threshold;
    }

    @JsonProperty
    public int getQueueSize() {
        return queueSize;
    }

    @JsonProperty
    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    @JsonProperty
    public int getDiscardingThreshold() {
        return discardingThreshold;
    }

    @JsonProperty
    public void setDiscardingThreshold(int discardingThreshold) {
        this.discardingThreshold = discardingThreshold;
    }

    @JsonProperty
    public JedisPoolConfigFactory getPool() {
        return pool;
    }

    @JsonProperty
    public void setPool(JedisPoolConfigFactory pool) {
        this.pool = pool;
    }

    @JsonProperty
    public LogstashEventLayoutFactory getLayout() {
        return layout;
    }

    @JsonProperty
    public void setLayout(LogstashEventLayoutFactory layout) {
        this.layout = layout;
    }

}
