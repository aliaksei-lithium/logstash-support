To make the `LogstashAppenderFactory` available for use in your application's configuration file, add the following to
`META-INF/services/io.dropwizard.logging.AppenderFactory` in your project:

    com.commercehub.dropwizard.logging.LogstashAppenderFactory

Then, add the appender to your YAML configuration file. For example:

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

That's it!

