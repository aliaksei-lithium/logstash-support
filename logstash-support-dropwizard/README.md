Add an appender of type `logstash` to your configuration file. For example:

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

