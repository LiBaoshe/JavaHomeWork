# 6.（必做）基于 hmily TCC 或 ShardingSphere 的 Atomikos XA 实现一个简单的分布式事务应用 demo（二选一），提交到 Github。

## hmily

GitHub：https://github.com/dromara/hmily

中文官网：https://dromara.org/zh/projects/hmily/overview/

Hmily是一款高性能，零侵入，金融级分布式事务解决方案，目前主要提供柔性事务的支持，包含 `TCC`, `TAC`(自动生成回滚SQL) 方案，未来还会支持 `XA` 等方案。

### 必要前提

- 必须使用 `JDK8+`

- TCC模式下，用户必须要使用一款 `RPC` 框架, 比如 : `Dubbo`, `SpringCloud`,`Motan`

  TCC模式是经典的柔性事务解决方案，需要使用者提供 `try`, `confirm`, `cancel` 三个方法， 正常的情况下会执行 `try`, `confirm`, 异常情况下会执行`try`, `cancel`。

- TAC模式下，用户必须使用关系型数据库, 比如：`mysql`, `oracle`, `sqlsever`

  `TAC`模式其实是`TCC`模式的变种,顾名思义 `TAC` 模式被称为自动回滚,相比于 `TCC`模式，用户完全不用关心 回滚方法如何去写，减少了用户的开发量，对用户完全透明。

  `TAC` 模式会拦截用户的SQL语句生成反向回滚SQL，SQL的兼容度也会是一大考验。

### Spring Boot 使用 hmily TCC

创建 hmily 数据库

```sql
CREATE DATABASE  IF NOT EXISTS  `hmily`  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

引入 maven 依赖

```yaml
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>hmily-spring-boot-starter-dubbo</artifactId>
    <version>2.1.1</version>
</dependency>
```

创建配置文件 hmily.yml



## ShardingSphere-Proxy 分布式事务

官网：https://shardingsphere.apache.org/document/5.0.0/cn/user-manual/shardingsphere-proxy/usage/transaction/

ShardingSphere-Proxy 接入的分布式事务 API 同 ShardingSphere-JDBC 保持一致，支持 LOCAL，XA，BASE 类型的事务。

### XA 事务

ShardingSphere-Proxy 原生支持 XA 事务，默认的事务管理器为 Atomikos（还有 Narayana 事务管理器、Bitronix事务管理器等）。 可以通过在 ShardingSphere-Proxy 的 `conf` 目录中添加 `jta.properties` 来定制化 Atomikos 配置项。 具体的配置规则请参考 Atomikos 的[官方文档](https://www.atomikos.com/Documentation/JtaProperties)。

### BASE 事务

BASE 目前没有集成至 ShardingSphere-Proxy 的二进制发布包中，使用时需要将实现了 `ShardingSphereTransactionManager` SPI 的 jar 拷贝至 `conf/lib` 目录，然后切换事务类型为 BASE。

