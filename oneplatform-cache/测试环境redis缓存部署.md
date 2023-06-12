测试环境redis缓存部署

拉取 Redis docker 镜像：

```shell
docker pull redis: 7.0.11
```

启动 Redis 缓存容器：

```shell
docker run --name oneplatform-cache-test --restart=unless-stopped -p 9101:6379 -d redis:7.0.11
```

