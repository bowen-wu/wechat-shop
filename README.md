# Wechat shop

## 表设计

### 用户表
- id 主键
- name 用户名
- tel 手机号码 unique
- avatarUrl 头像地址
- address 收货地址

### 店铺表
- id 主键
- name 店铺名称
- description 描述
- avatarUrl 店铺头像地址
- adminUserId 管理员账号ID

### 商品表
- id 主键
- name 商品名称
- shopId 店铺ID
- description 商品描述
- details 商品详情
- price 价格(分)
- stock 库存
- imageUrl 图片地址

### 订单表
- id 主键
- goodsId 商品ID
- count 数量
- customerUserId 下单用户ID




