# 9.（必做）基于 Redis 的 PubSub 实现订单异步处理

Java 端使用 Redisson 的 RTopic 实现基于 Redis 的 PubSub 订单异步处理。

```java
@Slf4j
public class Homework09 {

    private static final RedissonClient CLIENT;
    private static final String ORDER_TOPIC = "order_topic";

    static {
        // 初始化 redis 连接信息
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.10.200:6379");
        CLIENT = Redisson.create(config);
    }

    public static void main(String[] args) {

        subscribeOrder();

        for (int i = 0; i < 10; i++) {
            long time = System.currentTimeMillis();
            publishOrder(new OrderEntity(1000L + i, 10 * (1 + new Random().nextDouble()), time, time));
        }
    }

    /**
     * 发布（创建）订单
     * @param orderEntity
     */
    public static void publishOrder(OrderEntity orderEntity){
        RTopic topic = CLIENT.getTopic(ORDER_TOPIC);
        topic.publish(orderEntity);
    }

    /**
     * 订阅（消费）订单
     */
    public static void subscribeOrder(){
        RTopic topic = CLIENT.getTopic(ORDER_TOPIC);
        topic.addListener(OrderEntity.class, (charSequence, orderEntity) -> {
            log.info("接受到消息主题 = {}，内容 = {}",charSequence, orderEntity);
            System.out.println("传输的订单为=" + orderEntity);
            // 订单出来逻辑
        });
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class OrderEntity implements Serializable {
        private Long id;
        private Double money;
        private Long createTime;
        private Long updateTime;
    }
}
```

