# 8.（必做）基于 Redis 封装分布式数据操作：

- ## 在 Java 中实现一个简单的分布式锁；

### 一、使用 set nx 命令实现分布式锁

```java
void redisLock(){
//         boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", "1111");
    // 使用 UUID 作为锁的 value，用于删除锁时判断是否是当前业务的锁
    String uuid = UUID.randomUUID().toString();
    // 原子命令 加锁设置过期时间
    boolean lock = redisTemplate.opsForValue().setIfAbsent(
            "lock", uuid, 300, TimeUnit.SECONDS);
    if(lock){
        // 设置锁过期时间，应该使用一条原子命令加锁
        // redisTemplate.expire("lock", 30, TimeUtil.SECONDS);
        try {
            // 加锁成功，执行业务
        } finally {
            // 业务完成，释放锁

//                String lockValue = redisTemplate.opsForValue().get("lock");
//                if(uuid.equals(lockValue)){
//                    // 删除自己的锁，查询和删除应该是原子操作
//                    redisTemplate.delete("lock");
//                }

            // lua 脚本原子删除
            String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] " +
                    "then return redis.call(\"del\",KEYS[1]) " +
                    "else return 0 end";
            Long delLock = redisTemplate.execute(
                    new DefaultRedisScript<>(script, Long.class),
                    Collections.singletonList("lock"), uuid);
        }
    } else {
        // 加锁失败，重试
        // 可以休眠一小会（如100ms）后递归自旋
        redisLock();
    }
}
```

### 二、使用 Redisson 的 RLock

```java
    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.10.200:6379");

        final RedissonClient client = Redisson.create(config);
        RLock lock = client.getLock("lock1");

        try{
            lock.lock();
            // 业务代码
        }finally{
            lock.unlock();
        }
    }
```

- ## 在 Java 中实现一个分布式计数器，模拟减库存。

使用 hash 数据结构在 redis 中记录商品库存，hash = store，key = 商品 id，value = 库存量。

减库存时使用 RLock 锁住当前商品 id。

```java
public class Homework {
    
    private static final RedissonClient CLIENT;
    // 库存使用 hash 结构，key = 商品 id，value = 库存量
    private static final RMap<String, Integer> STORE;

    static {
        // 初始化 redis 连接信息
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.10.200:6379");
        CLIENT = Redisson.create(config);
        STORE = CLIENT.getMap("store");
    }

    public static void main(String[] args) {

        STORE.put("1001", 10);
        STORE.put("1002", 20);

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> minusStore("1001", new Random().nextInt(4) + 1));
        }

        for (int i = 0; i < 20; i++) {
            executorService.submit(() -> minusStore("1002", new Random().nextInt(4) + 1));
        }

        executorService.shutdown();
        while (!executorService.isTerminated()){
            Thread.yield();
        }

        System.out.println("减库存完毕！");
        STORE.forEach((id, num) -> System.out.println("商品 id = " + id + " 库存余量为 " + num));
    }

    /**
     * 减库存操作
     * @param id 商品 id
     * @param num 要减的库存数量
     */
    private static void minusStore(String id, int num){
        RLock lock = CLIENT.getLock(id);
        try {
            lock.lock();
            int curNum = STORE.get(id);
            System.out.println("id = " + id + "，当前库存为 " + curNum + ", 要减的库存为 " + num);
            if(curNum < num){
                System.out.println("id = " + id + "，库存不足，减库存失败。");
            } else {
                curNum -= num;
                STORE.put(id, curNum);
                System.out.println("id = " + id + "，减库存成功，库存余量为 " + curNum);
            }
        } finally {
            lock.unlock();
        }
    }
}
```

