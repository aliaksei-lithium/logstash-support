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

import com.commercehub.logging.logback.LogstashEventLayout;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class LogstashEventLayoutFactory {

    private String host;
    private String userFields;

    public LogstashEventLayout build() {
        LogstashEventLayout layout = new LogstashEventLayout();
        if (host != null && !host.trim().isEmpty()) {
            layout.setHost(host);
        } else {
            try {
                layout.setHost(InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException ignored) {
                layout.setHost("unknown");
            }
        }
        layout.setUserFields(userFields);
        return layout;
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
    public String getUserFields() {
        return userFields;
    }

    @JsonProperty
    public void setUserFields(String userFields) {
        this.userFields = userFields;
    }

}
