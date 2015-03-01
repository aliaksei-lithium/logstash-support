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

package com.commercehub.dropwizard.redis;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolConfigFactory {

    private int maxTotal = JedisPoolConfig.DEFAULT_MAX_TOTAL;

    private int maxIdle = JedisPoolConfig.DEFAULT_MAX_IDLE;

    private int minIdle = JedisPoolConfig.DEFAULT_MIN_IDLE;

    private boolean lifo = JedisPoolConfig.DEFAULT_LIFO;

    private long maxWaitMillis = JedisPoolConfig.DEFAULT_MAX_WAIT_MILLIS;

    private long minEvictableIdleTimeMillis = 60000;

    private long softMinEvictableIdleTimeMillis = JedisPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS;

    private int numTestsPerEvictionRun = -1;

    private boolean testOnBorrow = JedisPoolConfig.DEFAULT_TEST_ON_BORROW;

    private boolean testOnReturn = JedisPoolConfig.DEFAULT_TEST_ON_RETURN;

    private boolean testWhileIdle = true;

    private long timeBetweenEvictionRunsMillis = 30000;

    @NotBlank
    private String evictionPolicyClassName = JedisPoolConfig.DEFAULT_EVICTION_POLICY_CLASS_NAME;

    private boolean blockWhenExhausted = JedisPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED;

    private boolean jmxEnabled = JedisPoolConfig.DEFAULT_JMX_ENABLE;

    @NotBlank
    private String jmxNamePrefix = JedisPoolConfig.DEFAULT_JMX_NAME_PREFIX;

    public JedisPoolConfig build() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setLifo(lifo);
        config.setMaxWaitMillis(maxWaitMillis);
        config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        config.setSoftMinEvictableIdleTimeMillis(softMinEvictableIdleTimeMillis);
        config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        config.setTestWhileIdle(testWhileIdle);
        config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        config.setEvictionPolicyClassName(evictionPolicyClassName);
        config.setBlockWhenExhausted(blockWhenExhausted);
        config.setJmxEnabled(jmxEnabled);
        config.setJmxNamePrefix(jmxNamePrefix);
        return config;
    }

    @JsonProperty
    public int getMaxTotal() {
        return maxTotal;
    }

    @JsonProperty
    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    @JsonProperty
    public int getMaxIdle() {
        return maxIdle;
    }

    @JsonProperty
    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    @JsonProperty
    public int getMinIdle() {
        return minIdle;
    }

    @JsonProperty
    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    @JsonProperty
    public boolean isLifo() {
        return lifo;
    }

    @JsonProperty
    public void setLifo(boolean lifo) {
        this.lifo = lifo;
    }

    @JsonProperty
    public long getMaxWaitMillis() {
        return maxWaitMillis;
    }

    @JsonProperty
    public void setMaxWaitMillis(long maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    @JsonProperty
    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    @JsonProperty
    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    @JsonProperty
    public long getSoftMinEvictableIdleTimeMillis() {
        return softMinEvictableIdleTimeMillis;
    }

    @JsonProperty
    public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
        this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
    }

    @JsonProperty
    public int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    @JsonProperty
    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    @JsonProperty
    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    @JsonProperty
    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    @JsonProperty
    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    @JsonProperty
    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    @JsonProperty
    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    @JsonProperty
    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    @JsonProperty
    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    @JsonProperty
    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    @JsonProperty
    public String getEvictionPolicyClassName() {
        return evictionPolicyClassName;
    }

    @JsonProperty
    public void setEvictionPolicyClassName(String evictionPolicyClassName) {
        this.evictionPolicyClassName = evictionPolicyClassName;
    }

    @JsonProperty
    public boolean isBlockWhenExhausted() {
        return blockWhenExhausted;
    }

    @JsonProperty
    public void setBlockWhenExhausted(boolean blockWhenExhausted) {
        this.blockWhenExhausted = blockWhenExhausted;
    }

    @JsonProperty
    public boolean isJmxEnabled() {
        return jmxEnabled;
    }

    @JsonProperty
    public void setJmxEnabled(boolean jmxEnabled) {
        this.jmxEnabled = jmxEnabled;
    }

    @JsonProperty
    public String getJmxNamePrefix() {
        return jmxNamePrefix;
    }

    @JsonProperty
    public void setJmxNamePrefix(String jmxNamePrefix) {
        this.jmxNamePrefix = jmxNamePrefix;
    }

}
