# OnePlatform - 统一平台

## 1. 前言

作为一个有追求的 ITer，虽然现在的日常工作与程序设计没有多大关系，但我还是拥有想要写出优秀的、有用的应用的想法。我可以把生活中遇到的场景作为需求，秉持“不管黑猫白猫，一点一点拼凑起我的应用王国 WyattAppRealm。不追求十全十美，只要求够用实用好用，抓住主要矛盾，以最小的代价实现既定目标。

## 2. 简介

OnePlatform - 统一平台，定位为 WyattAppRealm 系列应用中大一统的运后端服务，为前端提供共享能力和业务逻辑支撑。对于在生活中捕获到的场景需求，业务功能实现全部集成到 OnePlatform 里面实现。这样一来，可以最大程度的复用服务网关、注册中心、监控中心等基础组件，同时又可以保持高内聚、低耦合的组件设计原则。

## 3. 组件和模块

- 基础组件
  - basic-gateway 服务网关（8000 端口）
  - basic-registry 服务注册中心（8500 端口）
  - basic-monitor 服务监控中心（8001 端口）
- 业务模块

## 部署

### 部署 MySQL 数据库

拉取 MySQL 8.0.29 数据库 docker 镜像：

```shell
docker pull mysql:8.0.29
```

启动镜像容器 oneplatform-mysql，要求：

- 容器`/etc/mysql/conf.d`用 rw 模式挂载到服务器`/root/one-platform/mysql/conf`，存放配置文件；
- 容器`/var/lib/mysql`用 rw 模式挂载到服务器`/root/one-platform/mysql/data`存放数据；
- 容器`/var/log/mysql用rw模式`挂载到服务器`/root/one-platform/mysql/logs`存放日志；
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
quit;
```

### 部署微服务应用
