服务注册中心是 OnePlatform 基础能力组件，选用 Consul 软件。由于 Consul 在[dockerhub](https://hub.docker.com/_/consul)上就有官方容器，拉取下来并运行容器：

```shell
docker pull consul:1.15.2
docker run --name op-registry -d -p 8500:8500 --net oneplatform-net --restart=unless-stopped consul:1.15.2
```


