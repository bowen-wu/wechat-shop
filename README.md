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

## 注意

1. 在 IDEA 中运行测试时，需要启动本地的 MySQL 和 Redis
   ```
   docker run --name wechat-shop-mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_shop -p 3307:3306 -d mysql:8 
   docker run --name wechat-shop-reids -p 6380:6379 -d redis
   ```


