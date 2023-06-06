# 拉取基镜像
FROM openjdk:15.0.2-oraclelinux8
# jar包添加到镜像中
ADD target/oneplatform-monitor.jar application.jar
# 容器运行时执行的命令
ENTRYPOINT ["java", "-jar", "/application.jar"]
# 容器对外暴露的端口
EXPOSE 8001