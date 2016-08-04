# ABOUT

**This is a fork of deprecated [commercehub-oss/logstash-support](https://github.com/commercehub-oss/logstash-support) that's no linger maintained.**

Main motivation: keep dependencies updated, because of usage this library in projects.

Library support stable release of DW 1.0

# logstash-support

An opinionated collection of libraries that support sending logging events to Redis from JVM applications for use with
logstash. Logging events are sent to Redis with a specific (JSON) format. While these libraries don't concern
themselves with what happens from there, typically logstash is used to retrieve the events from Redis and forward them
to an Elasticsearch server, and Kibana is used to view the events stored in Elasticsearch.

# Usage

See the README of each library for its usage instructions.

# License
This library is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

(c) All rights reserved Commerce Technologies, Inc.
