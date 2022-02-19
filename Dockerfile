# 基础镜像
FROM openjdk:18-jdk-alpine

RUN mkdir /app
WORKDIR /app

RUN apk --no-cache add curl
COPY wechat-shop-main/target/wechat-shop-main-0.0.1-SNAPSHOT.jar /app
COPY wechat-shop-api/target/wechat-shop-api-0.0.2-SNAPSHOT.jar /app
COPY wechat-shop-order/target/wechat-shop-order-0.0.1-SNAPSHOT.jar /app

# 暴露端口
EXPOSE 8080

CMD ["java", "-jar", "project-0.0.1.jar"]
