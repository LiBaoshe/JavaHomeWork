# 2.（必做）设计对前面的订单表数据进行水平分库分表，拆分 2 个库，每个库 16 张表。并在新结构在演示常见的增删改查操作。代码、sql 和配置文件，上传到 Github。

### ShardingSphere-Proxy 

使用版本：5.0.0

ShardingSphere-Proxy 是 Apache ShardingSphere 的第二个产品。 它定位为透明化的数据库代理端，提供封装了数据库二进制协议的服务端版本，用于完成对异构语言的支持。 目前提供 MySQL 和 PostgreSQL（兼容 openGauss 等基于 PostgreSQL 的数据库）版本，它可以使用任何兼容 MySQL/PostgreSQL 协议的访问客户端（如：MySQL Command Client, MySQL Workbench, Navicat 等）操作数据，对 DBA 更加友好。

下载地址：https://shardingsphere.apache.org/document/5.0.0/cn/downloads/

解压缩后修改 `conf/server.yaml`和以 `config-` 前缀开头的文件，如：`conf/config-xxx.yaml` 文件，进行分片规则、读写分离规则配置。配置方式参考[配置手册](https://shardingsphere.apache.org/document/5.0.0/cn/user-manual/shardingsphere-proxy/configuration/)

### server.yaml 服务配置

conf/server.yaml 文件

```yaml
scaling:
 blockQueueSize: 10000
 workerThread: 40
 clusterAutoSwitchAlgorithm:
   type: IDLE
   props:
     incremental-task-idle-minute-threshold: 30
 dataConsistencyCheckAlgorithm:
   type: DEFAULT

# mode:
#  type: Cluster
#  repository:
#    type: ZooKeeper
#    props:
#      namespace: governance_ds
#      server-lists: localhost:2181
#      retryIntervalMilliseconds: 500
#      timeToLiveSeconds: 60
#      maxRetries: 3
#      operationTimeoutMilliseconds: 500
#  overwrite: false

rules:
 - !AUTHORITY
   users:
     - root@%:root
     - sharding@:sharding
   provider:
     type: ALL_PRIVILEGES_PERMITTED
 - !TRANSACTION
   defaultType: XA
   providerType: Atomikos

props:
 max-connections-size-per-query: 1
 kernel-executor-size: 16  # Infinite by default.
 proxy-frontend-flush-threshold: 128  # The default value is 128.
 proxy-opentracing-enabled: false
 proxy-hint-enabled: false
 sql-show: true
 check-table-metadata-enabled: false
 show-process-list-enabled: false
   # Proxy backend query fetch size. A larger value may increase the memory usage of ShardingSphere Proxy.
   # The default value is -1, which means set the minimum value for different JDBC drivers.
 proxy-backend-query-fetch-size: -1
 check-duplicate-table-enabled: false
 sql-comment-parse-enabled: true
 proxy-frontend-executor-size: 0 # Proxy frontend executor size. The default value is 0, which means let Netty decide.
   # Available options of proxy backend executor suitable: OLAP(default), OLTP. The OLTP option may reduce time cost of writing packets to client, but it may increase the latency of SQL execution
   # if client connections are more than proxy-frontend-netty-executor-size, especially executing slow SQL.
 proxy-backend-executor-suitable: OLAP
 proxy-frontend-max-connections: 0 # Less than or equal to 0 means no limitation.
 sql-federation-enabled: true
```

### config-sharding.yaml 水平分库分表配置

conf/config-sharding.yaml 文件，根据用户编号 user_id 字段分库，根据订单编号 id 字段分表

```yaml
schemaName: sharding_db

dataSources:
 ds_1:
   url: jdbc:mysql://192.168.10.100:3306/db_mall_1?serverTimezone=UTC&useSSL=false
   username: root
   password: mysql8
   connectionTimeoutMilliseconds: 30000
   idleTimeoutMilliseconds: 60000
   maxLifetimeMilliseconds: 1800000
   maxPoolSize: 50
   minPoolSize: 1
 ds_2:
   url: jdbc:mysql://192.168.10.100:3306/db_mall_2?serverTimezone=UTC&useSSL=false
   username: root
   password: mysql8
   connectionTimeoutMilliseconds: 30000
   idleTimeoutMilliseconds: 60000
   maxLifetimeMilliseconds: 1800000
   maxPoolSize: 50
   minPoolSize: 1

rules:
- !SHARDING
 tables:
   t_order:
     actualDataNodes: ds_${1..2}.t_order_${1..16}
     tableStrategy:
       standard:
         shardingColumn: id
         shardingAlgorithmName: t_order_inline
     keyGenerateStrategy:
       column: id
       keyGeneratorName: snowflake
 bindingTables:
   - t_order
 defaultDatabaseStrategy:
   standard:
     shardingColumn: user_id
     shardingAlgorithmName: database_inline
 defaultTableStrategy:
   none:
 
 shardingAlgorithms:
   database_inline:
     type: INLINE
     props:
       algorithm-expression: ds_${user_id % 2 + 1}
   t_order_inline:
     type: INLINE
     props:
       algorithm-expression: t_order_${id % 10 + id % 7 + 1}
 
 keyGenerators:
   snowflake:
     type: SNOWFLAKE
     props:
       worker-id: 123
```

### 启动服务，创建表

运行 bin/start.bat 启动服务，使用命令 mysql -uroot -P3307 -proot 登录 ShardingSphere-Proxy，执行建表语句：

```sql
CREATE TABLE `t_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `total_price` decimal(18, 2) NULL DEFAULT NULL COMMENT '总金额',
  `discount_price` decimal(18, 2) NULL DEFAULT NULL COMMENT '折后价（实际金额）',
  `status` tinyint NULL DEFAULT NULL COMMENT '状态',
  `create_time` bigint NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;
```

ShardingSphere-Proxy 水平分库分表配置完成，可以进行增删改查操作了。

完整测试代码：https://github.com/LiBaoshe/simple-mall 中的 shardingsphere-proxy-demo 模块。
