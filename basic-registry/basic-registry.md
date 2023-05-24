服务注册中心是 OnePlatform 基础能力组件，选用 Consul 软件。由于 Consul 在[dockerhub](https://hub.docker.com/_/consul)上就有官方容器，拉取下来并运行容器：

```shell
docker pull consul:1.15.2
docker run --name basic-registry -d -p 8500:8500 --restart=unless-stopped consul:1.15.2
```

将容器接入 docker 网络，详见 README.md 文件。

