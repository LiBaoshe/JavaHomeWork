# 6.（必做）基于电商交易场景（用户、商品、订单），设计一套简单的表结构，提交 DDL 的 SQL 文件到 Github（后面 2 周的作业依然要是用到这个表结构）。

### 简单 E-R 关系图

![image-20211211003528911](https://raw.githubusercontent.com/LiBaoshe/images/master/imgs/image-20211211003528911.png)

### 物理模型

![image-20211211001736270](https://raw.githubusercontent.com/LiBaoshe/images/master/imgs/image-20211211001736270.png)

### DDL SQL 语句

创建数据库：

```mysql
CREATE DATABASE IF NOT EXISTS db_mall DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
```

创建表：

```mysql
CREATE TABLE IF NOT EXISTS `t_goods`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '编码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '名称',
  `type` varchar(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '分类',
  `weight` double(10, 2) NULL COMMENT '重量（kg）',
  `price` decimal(10, 2) NULL COMMENT '单价（￥元）',
  `stock` int NULL COMMENT '库存',
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `t_order`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `total_price` decimal(18, 2) NULL COMMENT '总金额',
  `discount_price` decimal(18, 2) NULL COMMENT '折后价（实际金额）',
  `status` int NULL COMMENT '状态',
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `t_order_goods`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_id` int NOT NULL COMMENT '订单ID',
  `goods_id` int NOT NULL COMMENT '商品ID',
  `goods_num` int NULL COMMENT '商品数量',
  `discount` float(3, 2) NULL COMMENT '折扣',
  `discount_price` decimal(10, 2) NULL COMMENT '折后价',
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `t_user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '用户名',
  `password` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '密码',
  `nickname` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '昵称',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '身份证',
  PRIMARY KEY (`id`)
);

ALTER TABLE `t_order` ADD CONSTRAINT `fk_user_order` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`);
ALTER TABLE `t_order_goods` ADD CONSTRAINT `fk_order` FOREIGN KEY (`order_id`) REFERENCES `t_order` (`id`);
ALTER TABLE `t_order_goods` ADD CONSTRAINT `fk_goods` FOREIGN KEY (`goods_id`) REFERENCES `t_goods` (`id`);
```

删除外键：

```mysql
ALTER TABLE `t_order` DROP FOREIGN KEY `fk_user_order`;
ALTER TABLE `t_order_goods` DROP FOREIGN KEY `fk_order`;
ALTER TABLE `t_order_goods` DROP FOREIGN KEY `fk_goods`;
```

