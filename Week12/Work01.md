# 1.（必做）配置 redis 的主从复制，sentinel 高可用，Cluster 集群。

## 一、Redis 主从复制

下载 redis 镜像

```shell
docker pull redis
```

启动3个redis容器服务，分别使用到6379、6380、6381端口

```shell
docker run --name redis-6379 -p 6379:6379 -d redis redis-server
docker run --name redis-6380 -p 6380:6379 -d redis redis-server
docker run --name redis-6381 -p 6381:6379 -d redis redis-server
```

查看容器内网信息

```shell
docker inspect 容器
```

3个 redis 的容器 IP 为：172.17.0.5、172.17.0.6、172.17.0.7

进入容器内部

```shell
docker exec -it 容器 /bin/bash
```

redis-cli 命令登录 redis，info 命令查看信息，role:master，3 台 redis 都是 master 角色。

使用 redis-cli 命令 （SLAVEOF IP 端口） 修改 redis-6380、redis-6381 的主机为172.17.0.5:6379

```shell
SLAVEOF 172.17.0.5 6379
```

## 二、Sentinel 高可用（主从切换）

Redis 的 Sentinel 系统用于管理多个 Redis 服务器（instance）， 该系统执行以下三个任务：

- **监控（Monitoring）**： Sentinel 会不断地检查你的主服务器和从服务器是否运作正常。
- **提醒（Notification）**： 当被监控的某个 Redis 服务器出现问题时， Sentinel 可以通过 API 向管理员或者其他应用程序发送通知。
- **自动故障迁移（Automatic failover）**： 当一个主服务器不能正常工作时， Sentinel 会开始一次自动故障迁移操作， 它会将失效主服务器的其中一个从服务器升级为新的主服务器， 并让失效主服务器的其他从服务器改为复制新的主服务器； 当客户端试图连接失效的主服务器时， 集群也会向客户端返回新主服务器的地址， 使得集群可以使用新主服务器代替失效服务器。

两种启动方式：

```shell
redis-sentinel sentinel.conf
redis-server redis.conf --sentinel
```

sentinel.conf 配置：

```shell
sentinel monitor mymaster 172.17.0.5 6379 2  // 主库，需要 2 个投票才被选举
sentinel down-after-milliseconds mymaster 60000  // 触发选举时间 1 分钟
sentinel failover-timeout mymaster 180000  // 允许选举的最大时间 3 分钟
sentinel parallel-syncs mymaster 1  // 同时允许同步的节点数目
port 26379  // sentinel 服务端口
```

不需要配置从节点，也不需要配置其它 sentinel 信息。

Java 端使用 JedisSentinelPool 连接。

## 三、Cluster 集群

Redis Cluster 通过一致性 hash 的方式，将数据分散到多个服务器节点：先设计 16384 个哈希槽，分配到多台 redis-server。当需要在 Redis Cluster中存取一个 key 时，Redis 客户端先对 key 使 用 crc16 算法计算一个数值，然后对 16384 取模，这样每个 key 都会对应一个编号在 0-16383 之间的哈希槽，然后在 此槽对应的节点上操作。

单机可以用 db0~db15，集群只能用 db0。

Java 端使用 JedisCluster 操作。
