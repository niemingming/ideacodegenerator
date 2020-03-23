# LOGGING
logging:
  level:
    root: INFO

# DATASOURCE (DataSourceAutoConfiguration & DataSourceProperties)
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: 123456
    type: com.zaxxer.hikari.HikariDataSource
    url: jdbc:mysql://localhost:3306/test?autoReconnect=true&useUnicode=true&characterEncoding=UTF8&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai
    hikari:
      minimum-idle: 2
      connection-timeout: 2000
      maximum-pool-size: 20
      auto-commit: true
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true

# Feign
feign:
  client:
    config:
      default:
        connectTimeout: 3000
        readTimeout: 3000
        loggerLevel: full
  httpclient:
    enabled: true
    max-connections: 200
    max-connections-per-route: 50
    time-to-live: 900
    time-to-live-unit: seconds
    connection-timeout: 3000
    follow-redirects: true
    disable-ssl-validation: false
