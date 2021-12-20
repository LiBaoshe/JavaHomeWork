# 2.（必做）按自己设计的表结构，插入 100 万订单模拟数据，测试不同方式的插入效率

完整测试代码：https://github.com/LiBaoshe/simple-mall 中的 order 模块。

## 测试环境

- MySql 8.0.23（安装在虚拟机VMWare中，分配内存2G，处理器2，内核2，硬盘100G）

  mysql 最大连接数设置为 10000，默认是151。

  ```mysql
  set global max_connections=10000; 
  ```

- Spring Boot 2.6.1
- mybatis-plus 3.3.1
- 数据库连接池 HikariCP

## 测试准备

创建订单表：

```mysql
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `total_price` decimal(18, 2) NULL COMMENT '总金额',
  `discount_price` decimal(18, 2) NULL COMMENT '折后价（实际金额）',
  `status` tinyint NULL COMMENT '状态',
  `create_time` bigint NULL COMMENT '创建时间',
  `update_time` bigint NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
);
```

使用 mybatis-plus generator 生成代码（包括controller、service、mapper 和 xml 文件），其中实体类为：

```java
/**
 * <p>
 * 
 * </p>
 *
 * @author baoge
 * @since 2021-12-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_order")
public class Order extends Model<Order> {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 总金额
     */
    private BigDecimal totalPrice;
    /**
     * 折后价（实际金额）
     */
    private BigDecimal discountPrice;
    /**
     * 状态
     */
    private Integer state;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}
```

## 测试内容

使用 Spring5 的单元测试类进行测试单表插入：

```java
@SpringBootTest
class OrderApplicationTests {
    
    // 模拟插入订单的数量
    private final int INSERT_ORDER_NUM = 100_0000;
    
    // 简单模拟插入一条订单数据
    @Test
    void insertOrder() {
        getOrder().insert();
    }
    
    /**
     * 返回一个模拟订单
     * @return
     */
    private Order getOrder(){
        Order order = new Order();
        order.setUserId(1);
        order.setTotalPrice(BigDecimal.valueOf(100));
        order.setDiscountPrice(BigDecimal.valueOf(100));
        order.setState(1);
        long time = System.currentTimeMillis();
        order.setCreateTime(time);
        order.setUpdateTime(time);
        return order;
    }
    
    // ... 其它测试方法
}
```

### 测试一：单线程单条插入100万订单数据

Hikari 连接池配置：

```yaml
# 最小空闲连接数量
minimum-idle: 5
# 空闲连接存活最大时间，默认600000（10分钟）
idle-timeout: 180000
# 连接池最大连接数，默认是10
maximum-pool-size: 10
# 此属性控制从池返回的连接的默认自动提交行为,默认值：true
auto-commit: true
```

测试单线程单条插入：

```java
    @Test
    void insertSingnle() {
        System.out.println("插入 " + INSERT_ORDER_NUM + " 条订单数据...");
        long start = System.currentTimeMillis();
        for (int i = 0; i < INSERT_ORDER_NUM; i++) {
            insertOrder();
        }
        long end = System.currentTimeMillis();
        System.out.println("插入完成，共耗时：" + (end - start) + "ms.");
    }
```

运行结果：插入完成，共耗时：1207899ms.

Hikari 连接池最大数量配置为100：

```yaml
# 连接池最大连接数，默认是10
maximum-pool-size: 100
```

运行 insert1()再次插入100万订单，执行结果为：插入完成，共耗时：1211470ms.

Hikari 连接池最大数量配置为32：

```yaml
# 连接池最大连接数，默认是10
maximum-pool-size: 32
```

运行 insert1()再次插入100万订单，执行结果为：插入完成，共耗时：1201303ms.

发现修改数据库连接池无效，因为程序是在单线程中执行的，所以只修改连接池数量是没有效果的。

### 测试二：多线程单条插入100万订单数据

使用线程池 Executors.newFixedThreadPool() 以多线程方式插入数据：

```java
    @Test
    void insertThreadPool() {
        int n = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(2 * n);
        System.out.println("插入 " + INSERT_ORDER_NUM + " 条订单数据...");
        long start = System.currentTimeMillis();
        for (int i = 0; i < INSERT_ORDER_NUM; i++) {
            executorService.execute(this::insertOrder);
        }
        executorService.shutdown();
        while (true){
            if(executorService.isTerminated()){
                break;
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("插入完成，共耗时：" + (end - start) + "ms.");
    }
```

此时线程池大小为 2n = 32，数据库连接池为32，

执行结果：插入完成，共耗时：91309ms.

插入改为多线程后，效果明显改善了，现在把线程池改为100测试一下：

```java
ExecutorService executorService = Executors.newFixedThreadPool(100);
```

执行结果：插入完成，共耗时：126324ms.

此时连接池是32，将连接池也改为 100 测试一下：

```yaml
# 连接池最大连接数，默认是10
maximum-pool-size: 100
```

执行结果：插入完成，共耗时：77817ms.

### 测试三：单线程批量插入100万订单数据

定义一个模拟批量插入的方法：

```java
    @Resource
    private DataSource dataSource;

    // 批量插入 n 个订单
    private int insertBath(int n){
        String sql = "INSERT INTO t_order(user_id, total_price, discount_price, state, create_time, update_time) " +
                "values (?, ?, ?, ?, ?, ?)";
        int count = 0;
        try (Connection conn = dataSource.getConnection()){
            // 关闭自动提交
            conn.setAutoCommit(false);
            // 预编译 sql
            PreparedStatement ps = conn.prepareStatement(sql);
            // 设置参数
            for (int i = 0; i < n; i++) {
                Order order = getOrder();
                ps.setInt(1, order.getUserId());
                ps.setBigDecimal(2, order.getTotalPrice());
                ps.setBigDecimal(3, order.getDiscountPrice());
                ps.setInt(4, order.getState());
                ps.setLong(5, order.getCreateTime());
                ps.setLong(6, order.getUpdateTime());
                // 添加到批处理
                ps.addBatch();
            }
            // 批量添加
            int[] batch = ps.executeBatch();

            for (int b : batch) {
                count += b;
            }
            // System.out.println("批量添加了 " + count + " 条订单数据.");
            conn.commit(); // 提交事务
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
```

测试单线程批量插入：

```java
    // 每次批量插入的数量
    private static final int BATCH_SIZE = 100;    

    @Test
    void insertSingleBath(){
        System.out.println("单线程批量插入 " + INSERT_ORDER_NUM + " 条数据，每次插入 " + BATCH_SIZE + " 条数据...");
        long start = System.currentTimeMillis();
        int count = 0;
        for (int i = 0; i < INSERT_ORDER_NUM; i += BATCH_SIZE) {
            count += insertBath(BATCH_SIZE);
        }
        long end = System.currentTimeMillis();
        System.out.println("插入完成，共耗时：" + (end - start) + "ms.");
        System.out.println("共插入了 " + count + " 条订单数据.");
    }
```

每次批量插入 10 条数据的运行结果：插入完成，共耗时：603382ms.

每次批量插入 100 条数据的运行结果：插入完成，共耗时：420983ms.

每次批量插入 1000 条数据的运行结果：插入完成，共耗时：408443ms.

每次批量插入 10000 条数据的运行结果：插入完成，共耗时：404403ms.

每次批量插入 100000 条数据的运行结果：插入完成，共耗时：403681ms.

每次批量插入 1000000 条数据的运行结果：插入完成，共耗时：405988ms.

### 测试四：多线程批量插入100万订单数据

测试多线程批量插入：

```java
    private AtomicInteger count = new AtomicInteger();

    @Test
    void insertThreadPoolBatch(){
        int threadSize = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
        System.out.println("多线程批量插入 " + INSERT_ORDER_NUM + " 条数据，每次插入 " + BATCH_SIZE + " 条数据...");
        long start = System.currentTimeMillis();
        count.set(0);
        for (int i = 0; i < INSERT_ORDER_NUM; i += BATCH_SIZE) {
            executorService.execute(() -> count.getAndAdd(insertBath(BATCH_SIZE)));
        }
        executorService.shutdown();
        while (true){
            if(executorService.isTerminated()){
                break;
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("插入完成，共耗时：" + (end - start) + "ms.");
        System.out.println("共插入了 " + count.get() + " 条订单数据.");
    }
```

每次批量插入 10 条数据的运行结果：插入完成，共耗时：42900ms.

每次批量插入 100 条数据的运行结果：插入完成，共耗时：35827ms.

每次批量插入 1000 条数据的运行结果：插入完成，共耗时：36045ms.

每次批量插入 10000 条数据的运行结果：插入完成，共耗时：35759ms.

每次批量插入 100000 条数据的运行结果：插入完成，共耗时：51884ms.

## 测试总结

整理每种插入的平均时间：

| 批量数  | 单线程单条          | 多线程单条         | 单线程批量 | 多线程批量 |
| ------- | ------------------- | ------------------ | ---------- | ---------- |
| 10      | -                   | -                  | 603382ms   | 42900ms    |
| 100     | -                   | -                  | 420983ms   | 35827ms    |
| 1000    | -                   | -                  | 408443ms   | 36045ms    |
| 10000   | -                   | -                  | 404403ms   | 35759ms    |
| 100000  | -                   | -                  | 403681ms   | 51884ms    |
| 1000000 | 1207899ms，约20分钟 | 91309ms，约1.5分钟 | 405988ms   | -          |

可以看出单线程单条插入100万订单数据最慢，约要20分钟。多线程批量插入，批量数为100~10000时，时间比较优，差不多都在 35 秒左右。

将 mysql 虚拟机配置从 4核2G 增加打 8核8G，多线程批量插入可以到25秒左右。

## 补充问题

通过学习群里讨论，还可以使用 load data 命令、Java8 Stream parallel 切割数据 、JDBC参数rewriteBatchedStatements、各种 log 都设置成不立即落盘、MySQL Shell import_table 数据导入 等优化方式，可以优化到10秒之内，看大佬们讨论，收获颇多，有空再按这些方向优化。

