# SPRING BOOT PROPERTIES

# ----------------------------------------
# CORE PROPERTIES
# ----------------------------------------
debug: false # Enable debug logs.
trace: false # Enable trace logs.

spring:

  # IDENTITY (ContextIdApplicationContextInitializer)
  application:
    name: ${projectName}

  # PROFILES
  profiles:
    active: dev

  # JMX
  jmx:
    enabled: false

# ----------------------------------------
# WEB PROPERTIES
# ----------------------------------------
server:
  port: 8000
management:
  endpoints:
    web:
      exposure:
        include: 'prometheus'

#配置mybatisplus
mybatis-plus:
  mapper-locations: classpath:mapper/*Mapper.xml
#  configuration:
#   log-impl: org.apache.ibatis.logging.stdout.StdOutImpl