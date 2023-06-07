# 微服务接入服务监控中心

`pom.xml`引入依赖：

```xml
<!-- Spring Boot -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>
<!-- 服务监控中心客户端 -->
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-client</artifactId>
    <version>2.5.2</version>
</dependency>
```

`application.yaml`添加全局配置：

```yaml
# 为服务监控中心开放健康检查接口，即对/actuator/*路径的访问
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
```

`application-dev.yaml`和`application-run.yaml`添加服务监控服务器的配置：

```yaml
# 测试环境application-dev.yaml
spring:
  boot:
    # 服务监控
    admin:
      client:
        url: "http://localhost:8001"

# 测试环境application-run.yaml
spring:
  boot:
    # 服务监控
    admin:
      client:
        url: "http://oneplatform-monitor:8001"
```

