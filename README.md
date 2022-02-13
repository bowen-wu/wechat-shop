# Wechat shop

## 表设计

### 用户表 USER

- id 主键
- name 用户名
- tel 手机号码 unique
- avatarUrl 头像地址
- address 收货地址

### 店铺表 SHOP

- id 主键
- name 店铺名称
- description 描述
- imgUrl
- ownerUserId 管理员ID
- statue 状态
- createdAt 创建时间
- updatedAt 更新时间

### 商品表 GOODS

- id 主键
- shopId 店铺ID
- name 商品名称
- description 商品描述
- details 商品详情
- imageUrl 图片地址
- price 价格(分)
- stock 库存
- status 商品状态
- createdAt 创建时间
- updatedAt 更新时间

### 订单表 ORDER

- id 主键
- userId
- totalPrice 总价格
- address 地址
- expressCompany 快递公司
- expressId 快递公司ID
- status 状态
- createdAt 创建时间
- updatedAt 更新时间

### 订单商品关系表 ORDER_GOODS

- id
- goodsId 商品ID
- number 商品数量

### 购物车表 SHOPPING_CART

// TODO

## TODO

1. apidoc => 接口文档

## 知识点

1. ` insert ` 之后自动设置了 entity 的 id

## 流程

### MySQL

1. docker 启动数据库
   ```
   docker run --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=shop -d mysql:8
   winpty docker exec -it <containerId> mysql -uroot -pmy-secret-pw -e 'create database if not exists `order`'
   ```
2. 通过 flyway 初始化数据库
   ```
   mvn flyway:migrate -pl wechat-shop-api
   mvn flyway:migrate -pl wechat-shop-main
   ```
3. 通过 Mybatis generator 自动生成 mapper
   ```
   mvn mybatis-generator:generate -pl wechat-shop-api
   mvn mybatis-generator:generate -pl wechat-shop-main
   ```

## 注意

1. 在 IDEA 中运行测试时，需要启动本地的 MySQL 和 Redis
   ```
   docker run --name wechat-shop-mysql-4-test -p 3307:3306 -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=shop -d mysql:8
   docker exec -it <containerId> mysql -uroot -pmy-secret-pw -e 'create database if not exists `order`'
   docker run --name wechat-shop-reids-4-test -p 6380:6379 -d redis
   ```
2. 同一个 mysql docker 开两个数据库
   ```
   docker run --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=shop -d mysql:8
   winpty docker exec -it <containerId> mysql -uroot -pmy-secret-pw -e 'create database if not exists `order`'
   ```
3. 在某个项目上执行 flyway:migrate 命令
   ```
   mvn exec:jave -pl my-module 
   ```
4. 明明依赖了本地的包，却找不到 => 将字节码安装到本地仓库中 => 把对应的所有模块的 jar 包都安装到本地仓库中，方便其他模块调用
   ```
   mvn install -DskipTests
   ```
5. ` @Transactional `
    1. 注解的方法必须是 ` public `
    2. 只有通过别的 service 调用时才生效 => 使用 this 调用不可以
6. [使用调试器的方式运行 test](https://maven.apache.org/surefire/maven-surefire-plugin/examples/debugging.html)
   ```
   mvn -Dmaven.surefire.debug test
   ```
   运行测试时会等待 5005 端口的调试器连接 => 新建 5005 端口的 remote debug => 运行的测试和 Maven 运行的环境完全一致
7. **不要在循环中进行SQL查询**
8. **不要在 ` stream ` 中使用 ` stream `**
