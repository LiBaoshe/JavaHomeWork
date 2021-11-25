### 3.（选做）列举常用的并发操作 API 和工具类，简单分析其使用场景和优缺点。

### synchronized 和 ReentrantLock

synchronized是java关键字，隐式获取和释放锁，ReentrantLock 是Java API 提供的，需要显示获取和释放锁。

synchronized是非公平的，ReentrantLock可以定义公平/非公平锁。

synchronized是同步阻塞，悲观锁，Lock是同步非阻塞，乐观锁。

Lock 可以通过 Condition 绑定多个条件精确等待/唤醒线程（await/signal）。

Lock 可响应中断、可轮询、可创建定时锁，比 synchronized 更加灵活。

### ReentrantReadWriteLock/StampedLock

读写锁 ReentrantReadWriteLock 实现了多个读锁可以同时进行，读与写、写于写互斥，适用于读多写少的场景。StampedLock是JDK8引入的，相比较ReentrantReadWriteLock性能更好，因为ReentrantReadWriteLock在读写之间是互斥的，使用的是一种悲观策略，在读线程特别多的情况下，会造成写线程处于饥饿状态，虽然可以在初始化的时候设置为 true 指定为公平，但是吞吐量又下去了，而StampedLock是提供了一种乐观策略，更好的实现读写分离，并且吞吐量不会下降。
原文链接：https://blog.csdn.net/qq_36094018/article/details/90575768

### CountDownLatch、CyclicBarrier、Semphore

| CountDownLatch                                               | CyclicBarrier                                  | Semphore                                     |
| ------------------------------------------------------------ | ---------------------------------------------- | -------------------------------------------- |
| 一个或多个线程一起等待**其它**线程的操作执行完成后再执行相关操作。 | 一组线程等待至某个状态之后再**全部同时**执行。 | 信号量，用于控制同时访问某些资源的线程个数。 |
| 不可重用                                                     | 可以重用                                       | 与锁功能类似，申请到许可才能执行线程任务。   |
| await()/countDown()                                          | await()/reset()                                | acquire()/release()                          |

CountDownLatch 和 CyclicBarrier 都是用于实现多线程之间的互相等待，但二者的关注点不同，CountDownLatch 主要用于主线程等待其它子线程任务均执行完成后再执行接下来的逻辑，而 CyclicBarrier 主要用于一组互相等待的线程大家都到某个状态后，再同时执行各自接下来的逻辑。

### 阻塞队列 BlockingQueue

Java中实现的阻塞队列有：ArrayBlockingQueue、LinkedBlockingQueue、PriorityBlockingQueue、DelayQueue、SynchronousQueue、LinkedTransferQueue、LinkedBlockingDeque。

阻塞队列的主要操作方法：

- ### add()/remove() 

常规添加删除方法，不会阻塞，队列满时 add(e) 或队列空时 remove() 会抛异常。

java.lang.IllegalStateException: Queue full，java.util.NoSuchElementException

- ### offer()/poll()

队列满时 offer(e) 返回false，队列空时poll() 返回 null，没有设定等时间不会阻塞。

offer(e, time, unit) 和 poll(time, unit) 设定了等待时间会阻塞，如果在等待时间内不能完成则返回 false / null。

- ### put()/take()

都是阻塞方法，队列已满时 put(e) 将会阻塞，直到队列中有可用的空间，队列空时 take() 将会阻塞，直到队列中有新的元素加入。

### 并发集合

在多线程并发环境中，java.util 包下的集合类都存在线程安全问题，多线程环境中应该尽量使用JUC(java.util.concurrent)包下的集合：

ConcurrentHashMap、CopyOnWriteArrayList、CopyOnWriteArraySet。

### 原子类

多线程下需要保证原子性操作时，应该使用java.util.concurrent.atomic包里原子类，AtomicInteger，AtomicBoolean等，通过AtomicReference<V>将一个对象的所有操作都转化为原子操作。

JDK8中新增了LongAdder、DoubleAdder、LongAccumulator、DoubleAccumulator 四个原子类，内部采用了分段的思想，将数据分为 Cell[] cells 数组，分段进行原子操作，最后累加到一起得到最终结果。

### 线程池

线程池常用类/接口：Executor、ExecutorService、Executors、ThreadPoolExecutor。

Executors 中提供了一些快速创建线程池的方法，但是不推荐使用，因为默认的参数并不是很合理，如线程队列workQueue的上线是Integer.MAX_VALUE，这相当于是无界队列了，如果并发过多则可能系统处理不过来。所以还是使用ThreadPoolExecutor自定义参数比较好。

ThreadPoolExecutor 构造方法有7个参数：

- corePoolSize - 即使空闲时仍保留在池中的线程数，除非设置 allowCoreThreadTimeOut 
- maximumPoolSize - 池中允许的最大线程数
- keepAliveTime - 当线程数大于corePoolSize 时，这是多余的空闲线程在终止前等待新任务的最大时间。
- unit - keepAliveTime参数的时间单位 
- workQueue - 用于在执行任务之前使用的队列。 这个队列将仅保存execute方法提交的Runnable任务。
- threadFactory - 执行程序创建新线程时使用的工厂 
- handler - 执行被阻止时使用的处理程序，因为达到线程限制和队列容量

### CompletableFuture

在Java8中，CompletableFuture实现了 CompletionStage 和 Future 两个接口，提供了非常强大的Future的扩展功能，可以帮助我们简化异步编程的复杂性，并且提供了函数式编程的能力，可以通过回调的方式处理计算结果，也提供了转换和组合 CompletableFuture 的方法。

当每个操作很复杂需要花费相对很长的时间（比如，调用多个其它的系统的接口；比如，商品详情页面这种需要从多个系统中查数据显示的）的时候用CompletableFuture才合适，不然区别真的不大，还不如顺序同步执行。

### Stream

Stream 可以很方便的开启并行操作：

list.parallelStream()

list.stream().parallel()
