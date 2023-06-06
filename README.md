# OnePlatform - 统一平台

## 1. 前言

作为一个有追求的 ITer，虽然现在的日常工作与程序设计没有多大关系，但我还是拥有想要写出优秀的、有用的应用的想法。我可以把生活中遇到的场景作为需求，秉持“不管黑猫白猫，一点一点拼凑起我的应用王国 WyattAppRealm。不追求十全十美，只要求够用实用好用，抓住主要矛盾，以最小的代价实现既定目标。

## 2. 简介

OnePlatform - 统一平台，定位为 WyattAppRealm 系列应用中大一统的运后端服务，为前端提供共享能力和业务逻辑支撑。对于在生活中捕获到的场景需求，业务功能实现全部集成到 OnePlatform 里面实现。这样一来，可以最大程度的复用服务网关、注册中心、监控中心等基础组件，同时又可以保持高内聚、低耦合的组件设计原则。

## 3. 组件和模块

- 基础组件
  - oneplatform-common 公共模块：定义公共的类、配置、工具、能力等。
  - oneplatform-gateway 服务网关（8000 端口）：鉴权、路由。
  - oneplatform-registry 服务注册中心（8500 端口）。
  - oneplatform-monitor 服务监控中心（8001 端口）。
  - oneplatform-user 用户中心（8002 端口）：基于 RBAC 用户权限管理，认证授权。
  - oneplatform-cache 缓存服务（）。
  - oneplatform-mysql 数据库（9000 端口）。
- 业务模块
    - 

## 部署

### 部署数据库

拉取 MySQL 8.0.29 数据库 docker 镜像：

```shell
docker pull mysql:8.0.29
```

启动镜像容器 oneplatform-mysql，要求：

- 容器`/etc/mysql/conf.d`用 rw 模式挂载到服务器`/root/one-platform/mysql/conf`，存放配置文件；
- 容器`/var/lib/mysql`用 rw 模式挂载到服务器`/root/one-platform/mysql/data`存放数据；
- 容器`/var/log/mysql`用 rw 模式挂载到服务器`/root/one-platform/mysql/logs`存放日志；
- 绑定 9000 端口；
- 自动重启。

```shell
docker run --name oneplatform-mysql -v /root/one-platform/mysql/conf:/etc/mysql/conf.d:rw -v /root/one-platform/mysql/data:/var/lib/mysql:rw -v /root/one-platform/mysql/logs:/var/log/mysql:rw -e MYSQL_ROOT_PASSWORD=密码 -p 9000:3306 --restart=unless-stopped -d mysql:8.0.29
```

初始化数据库。首先启动数据库交互终端，用 root 用户登录：

```shell
docker exec -it oneplatform-mysql mysql -uroot -p
```

输入密码登录后执行以下 SQL，创建数据库：

```mysql
create database db_oneplatform;
```

创建数据库访问用户并授权：

```mysql
create user 'oneplatform_access'@'%' identified by '密码';
grant all privileges on oneplatform.* to oneplatform_access@'%';
```

### 部署微服务应用

构建镜像前，先将配置从测试环境改成生产环境，maven 构建 jar 包：

```yaml
spring:
  profiles:
    active: run
```

在项目根目录下构建 Dockerfile，按照如下模板填入构建命令：

```dockerfile
# 拉取基镜像
FROM openjdk:15.0.2-oraclelinux8
# jar包添加到镜像中（PS：换成对应应用生成的jar包名称）
ADD target/oneplatform-monitor.jar application.jar
# 容器运行时执行的命令
ENTRYPOINT ["java", "-jar", "/application.jar"]
# 容器对外暴露的端口（PS：换成端口）
EXPOSE 8001
```

构建镜像并推送到 dockerhub 仓库：

```shell
# 在Dockerfile文件所在目录下运行（PS：换成应用对应的镜像名称和标签号）
docker build -t wyatt6/oneplatform-monitor:1.0.0 ./
# （PS：换成应用对应的镜像名称和标签号）
docker push wyatt6/oneplatform-monitor:1.0.0
```

拉取镜像并运行容器：

```shell
# 创建容器网络
docker network create oneplatform-net
# （PS：换成应用对应的镜像名称和标签号）
docker pull wyatt6/oneplatform-monitor:1.0.0
# 生产态不需要暴露端口则不绑定端口（PS：换成应用对应的容器名、端口号、镜像名和标签号）
docker run --name oneplatform-monitor -d -p 8001:8001/tcp --net oneplatform-net --restart=unless-stopped -e TZ="Asia/Shanghai" wyatt6/oneplatform-monitor:1.0.0
```

