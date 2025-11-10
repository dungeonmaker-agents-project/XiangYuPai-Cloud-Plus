# XY相遇派内容模块配置

# 数据源配置
spring:
  datasource:
    dynamic:
      primary: master
      datasource:
        # 主库数据源
        master:
          type: ${spring.datasource.type}
          driver-class-name: com.mysql.cj.jdbc.Driver
          # 使用 xypai_content 数据库（内容相关表：posts, comments, likes等）
          url: ${datasource.xypai-content-master.url}
          username: ${datasource.xypai-content-master.username}
          password: ${datasource.xypai-content-master.password}

  # Redis 配置 - 使用全局配置 (application-common.yml)
  # 全局配置包含: host, port, password, database: 0, timeout
  # 无需在此覆盖，保持所有服务Redis配置一致

# Dubbo配置
dubbo:
  application:
    name: ${spring.application.name}
  protocol:
    name: dubbo
    port: -1
  registry:
    address: nacos://${spring.cloud.nacos.server-addr}
    group: ${spring.cloud.nacos.discovery.group}
    parameters:
      namespace: ${spring.cloud.nacos.discovery.namespace}

