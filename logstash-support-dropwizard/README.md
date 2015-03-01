# logstash-support-dropwizard

[ ![Download](https://api.bintray.com/packages/commercehub-oss/main/logstash-support-dropwizard/images/download.svg) ](https://bintray.com/commercehub-oss/main/logstash-support-dropwizard/_latestVersion)

A library that supports sending logging events from Dropwizard applications to Redis for use with logstash. Logging
events are sent to Redis with a specific (JSON) format. While these libraries don't concern themselves with what happens
from there, typically logstash is used to retrieve the events from Redis and forward them to an Elasticsearch server,
and Kibana is used to view the events stored in Elasticsearch.

# Usage

First, add a dependency to your build file.  Releases are published to
[Bintray JCenter](https://bintray.com/bintray/jcenter).

Gradle:

```groovy
...
repositories {
    jcenter()
}
...
dependencies {
    compile "com.commercehub:logstash-support-dropwizard:1.0.0"
}
...
```

Maven:

```xml
...
<repositories>
  <repository>
    <id>jcenter</id>
    <url>http://jcenter.bintray.com</url>
  </repository>
</repositories>
...
<dependency>
  <groupId>com.commercehub</groupId>
  <artifactId>logstash-support-dropwizard</artifactId>
  <version>1.0.0</version>
</dependency>
...
```

If you're using the [Gradle Shadow Plugin](https://github.com/johnrengelman/shadow) or the
[Maven Shade Plugin](http://maven.apache.org/plugins/maven-shade-plugin/) to package your application as an uber-jar,
ensure that `META-INF/services/io.dropwizard.logging.AppenderFactory` is properly merged.

Add an appender of type `logstash` to your configuration file:

```yaml
logging:
  appenders:
    - type: logstash
      host: logs.acme.com
      key: logstash
      threshold: INFO
      pool:
        maxTotal: 10
        maxIdle: 5
        minIdle: 1
        testOnBorrow: true
        testOnReturn: true
        testWhileIdle: true
        blockWhenExhausted: false
      layout:
        userFields: "env:prod,app:foo"
```

# License
This library is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

(c) All rights reserved Commerce Technologies, Inc.
