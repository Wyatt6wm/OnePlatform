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
