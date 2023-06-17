# Spring Cloud Gateway服务网关

[Spring Cloud Gateway官方参考文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)	

## 1. 术语

- **Route路由**：网关的基础构建块，由一个ID、一个目标URI、一组断言集合和一组过滤器组成。如果断言结果为true则路由成功匹配。
- **Predicate断言**：用来匹配HTTP请求的任意数据，如报文头、参数、请求路径等。
- **Filter过滤器**：可以用过滤器在向下游发送请求之前或之后来修改请求或响应的报文数据。

## 2. 工作原理

![Spring Cloud Gateway工作原理示意图](spring-cloud-gateway-ref.assets/spring_cloud_gateway_diagram.png)

客户端向Spring Cloud Gateway发送请求。Gateway Handler Mapping判断是匹配中路由规则，如果匹配上了则发送给Gateway Web Handler。Handler控制请求经过多个过滤器组成的处理链进行修改，以及代理请求后台服务。具体过程先是控制请求执行前处理过滤器（pre）的逻辑，而后代理发送请求到后台服务，获取到响应后控制响应执行后处理过滤器（post）的逻辑，最后往上返回。

## 3. 格式示例

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: after_route
        uri: https://example.org
        predicates:
        - Cookie=mycookie,mycookievalue
```

上面是缩略格式，等价于下面全量格式。区别只在于断言的不同。

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: after_route
        uri: https://example.org
        predicates:
        - name: Cookie
          args:
            name: mycookie
            regexp: mycookievalue
```

## 4. 路由断言工厂

Spring Cloud Gateway包含了很多内置的路由断言工厂（Predicate Factory），这些断言匹配了HTTP请求的不同属性。可以用逻辑与将多个路由断言工厂组合起来。

- After路由断言工厂
- Before路由断言工厂
- Between路由断言工厂
- Cookie路由断言工厂
- Header路由断言工厂
- Host路由断言工厂
- Method路由断言工厂
- Path路由断言工厂
- Query路由断言工厂
- RemoteAddr路由断言工厂
- Weight路由断言工厂
- XForwarded路由断言工厂

### 4.1. After路由断言工厂

包含一个参数`datetime`（Jave`ZonedDateTime`格式）。匹配在特定时间之后发起的请求。示例：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: after_route
        uri: https://example.org
        predicates:
        - After=2017-01-20T17:42:47.789-07:00[America/Denver]
```

### 4.2. Before路由断言工厂

包含一个参数`datetime`（Jave`ZonedDateTime`格式）。匹配在特定时间之前发起的请求。示例：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: before_route
        uri: https://example.org
        predicates:
        - Before=2017-01-20T17:42:47.789-07:00[America/Denver]
```

### 4.3. Between路由断言工厂

包含两个参数`datetime1`和`datetime2`（Jave`ZonedDateTime`格式）。匹配在`datetime1`之后，但是在`datetime2`之前发起的请求。示例：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: between_route
        uri: https://example.org
        predicates:
        - Between=2017-01-20T17:42:47.789-07:00[America/Denver], 2017-01-21T17:42:47.789-07:00[America/Denver]
```

### 4.4. Cookie路由断言工厂

包含两个参数，Cookie的`name`和`regexp`。匹配Cookie包含名称为键值对<`name`,`regexp`>的请求。示例：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: cookie_route
        uri: https://example.org
        predicates:
        - Cookie=chocolate, ch.p
```

### 4.5. Header路由断言工厂

包含两个参数`header`和`regexp`。匹配报文头包含键值对<`header`,`regexp`>的请求。示例：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: header_route
        uri: https://example.org
        predicates:
        - Header=X-Request-Id, \d+
```

### 4.6. Host路由断言工厂

包含一个参数：主机名列表`patterns`。这个列表中的主机名是Ant-style的模板。匹配报文头`Host`属性能匹配上主机名列表的请求。示例：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: host_route
        uri: https://example.org
        predicates:
        - Host=**.somehost.org,**.anotherhost.org
```

同时也支持URI模板变量，比如`{sub}.myhost.org`。断言会把URI模板变量（例如`sub`）解析为键值对，使用在`ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE`定义的键，并存放在`ServerWebExchange。getAttributes()`中。这样这些值就可以被过滤器工厂所使用。

### 4.7. Method路由断言工厂

包含一个参数`methods`，包含若干个HTTP请求方法列表。用来根据HTTP请求方法进行匹配。示例：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: method_route
        uri: https://example.org
        predicates:
        - Method=GET,POST
```

### 4.8. Path路由断言工厂

包含两个参数：（1）一个路径`patterns`的String列表；（2）一个叫做`matchTrailingSlash`的选填的标志（是否匹配末尾斜杠，默认为true）。匹配请求URL满足断言的请求。示例：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: path_route
        uri: https://example.org
        predicates:
        - Path=/red/{segment},/blue/{segment}
```

注意，如果`matchTrailingSlash`设置为false，则`/red/example/`不会命中，因为URL中包含了末尾斜杠。

断言会把URI模板变量（例如`segment`）解析为键值对，使用在`ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE`定义的键，并存放在`ServerWebExchange.getAttributes()`中。这样这些值就可以被过滤器工厂所使用。

### 4.9. Query路由断言工厂

包含两个参数：必填的`param`和选填的`regexp`。匹配请求参数（URL上面`?`之后的部分）是否在断言中。示例：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: query_route
        uri: https://example.org
        predicates:
        - Query=green, red.
```

### 4.10. RemoteAddr路由断言工厂

包含至少一个的源IP列表，用CIDR格式表示的IPv4或IPv6串。匹配远程地址是否在列表中。示例：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: remoteaddr_route
        uri: https://example.org
        predicates:
        - RemoteAddr=192.168.1.1/24
```

远程地址默认情况下知只是最接近服务网关的地址，但并不一定是真正的客户端地址，有可能经过了几次的代理。这时候可以修改远程地址的解析规则，具体详见[官方文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#modifying-the-way-remote-addresses-are-resolved)。

### 4.11. Weight路由断言工厂

包含两个参数`group`和`weight`（整数权重）。用来进行负载均衡，分组里面分配对应权重的流量，例如2、8则分配20%、80%的流量。示例：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: weight_high
        uri: https://weighthigh.org
        predicates:
        - Weight=group1, 8
      - id: weight_low
        uri: https://weightlow.org
        predicates:
        - Weight=group1, 2
```

### 4.12. XForwarded路由断言工厂

包含至少一个的源IP列表，用CIDR格式表示的IPv4或IPv6串。匹配HTTP报文头中的`X-Forwarded-For`属性中的地址是否在列表中。这个可以和反向代理一起使用，比如负载均衡器或者WAF。示例：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: xforwarded_remoteaddr_route
        uri: https://example.org
        predicates:
        - XForwardedRemoteAddr=192.168.1.1/24
```

## 5. 路由过滤器

路由过滤器作用是修改进入的HTTP请求或者出去的HTTP响应。Spring Cloud Gateway包含了很多内置的过滤器工厂。由于路由过滤器实在太多，这里只挑选几个常用的进行解释，其他详见[官方文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gatewayfilter-factories)。

1. AddRequestHeader：添加请求头。
2. AddRequestHeadersIfNotPresent：添加请求头（如果没出现过）。
3. AddRequestParameter：添加请求参数。
4. AddResponseHeader：添加响应头。
5. CircuitBreaker：熔断断路器。
6. CacheRequestBody：缓存请求体。
7. DedupeResponseHeader：删除重复响应头。
8. FallbackHeaders
9. JsonToGrpc：转换JSON格式负载为gRPC请求。
10. LocalResponseCache：缓存响应头和响应体。
11. MapRequestHeader
12. ModifyRequestBody：修改请求体。
13. ModifyResponseBody：修改响应体。
14. PrefixPath：为请求URL增加前缀。
15. PreserveHostHeader
16. RedirectTo：重定向。
17. RemoveJsonAttributesResponseBody
18. RemoveRequestHeader：删除请求头属性。
19. RemoveRequestParameter：删除请求参数。
20. RemoveResponseHeader：删除响应头属性。
21. RequestHeaderSize：限制请求头大小。
22. RequestRateLimiter：限制请求频率。
23. RewriteLocationResponseHeader：重写响应头Location属性。
24. RewritePath：重写请求URL路径。
25. RewriteResponseHeader：重写响应头。
26. SaveSession：保存会话。
27. SecureHeaders
28. SetPath：设置URL路径。
29. SetRequestHeader：设置请求头。
30. SetResponseHeader：设置响应头。
31. SetStatus：设置状态。
32. StripPrefix：删除请求URL路径前缀。
33. Retry
34. RequestSize
35. SetRequestHostHeader：设置请求头Host属性。
36. TokenRelay

路由过滤器一般作用于特定的路由，如果想要某些过滤器作用于所有路由，需要配置默认过滤器属性：

```shell
spring:
  cloud:
    gateway:
      default-filters:
      - AddResponseHeader=X-Response-Default-Red, Default-Blue
      - PrefixPath=/httpbin
```

