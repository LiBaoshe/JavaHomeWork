### 1.（选做）把示例代码，运行一遍，思考课上相关的问题。也可以做一些比较。

#### Thread/Object

thread 执行完成后会调用自己的 notifyAlll()方法，join会结束。join 方法内部调用了thread对象的 wait 方法。

#### synchronized

synchronized关键字是解决并发问题常用解决方案，常用的有三种使用方式：修饰代码块（需要显示指定锁对象）、修饰对象方法（锁对象是this）、修饰静态方法（锁对象是类的class对象）。

#### 守护线程 Daemon Thread

在Java中线程有两类：用户线程（User Thread）、守护线程（Daemon Thread）。当所有的非守护线程结束时，程序也就终止了，同时会杀死进程中的所有守护进程。将线程转换为守护线程可以通过调用Thread对象的setDaemon(true)方法来实现，在使用守护线程时需注意以下几点：  
（1）thread.setDaemon(true)必须在thread.start()之前设置，否则会抛出一个java.lang.IllegalThreadStateException异常。即不能把正在运行的线程设置为守护线程。  
（2）在Daemon线程中产生的新线程也是Daemon的。  
（3）守护线程应该永远不去访问固有资源，如文件、数据库，因为它会在任何时候甚至在一个操作的中间发生中断。

#### 线程中断 interrupt

thread.interrupt() 对象方法，中断这个线程。   
thread.isInterrupted() 对象方法，测试这个线程是否被中断。  
Thtead.interrupted() **静态**方法，测试**当前**线程是否被中断，该方法可以清除线程的中断状态。

#### 线程组 ThreadGroup

线程组代表一组线程。 此外，线程组还可以包括其他线程组。 线程组形成一个树，除了初始线程组（system ThreadGroup）之外，每个线程组都有一个父进程。 允许线程访问有关其线程组的信息，但不能访问有关其线程组的父线程组或任何其他线程组的信息。新的thread创建时会默认继承父线程组的优先级。

list() 方法：将有关此线程组的信息打印到标准输出。 

#### 线程优先级

Java中的线程优先级的范围是1~10，默认的优先级是5。“高优先级线程” 会优先于 “低优先级线程” 执行。

Thread类中定义的三个线程优先级常量：

```java
    /**
     * The minimum priority that a thread can have.
     */
    public final static int MIN_PRIORITY = 1;

   /**
     * The default priority that is assigned to a thread.
     */
    public final static int NORM_PRIORITY = 5;

    /**
     * The maximum priority that a thread can have.
     */
    public final static int MAX_PRIORITY = 10;
```

#### FutureTask

直接继承Thread或者实现Runnable接口都可以创建线程，但是这两种方法都有一个问题就是：没有返回值，也就是不能获取执行完的结果。因此java1.5就提供了Callable接口来实现这一场景，而Future和FutureTask就可以和Callable接口配合起来使用。

#### CompletableFuture

Java 1.8 提供的 CompletableFuture 实现了CompletionStage接口和Future接口，前者是对后者的一个扩展，增加了异步回调、流式处理、多个Future组合处理的能力，使Java在处理多任务的协同工作时更加顺畅便利。

#### 原子类

JUC 包 java.util.concurrent.atomic 中提供了 AtomicInteger、AtomicLong 等，Java 8 的文档中共有16个原子类。

Java8 新增了 LongAdder、LongAccumulator、DoubleAdder、DoubleAccumulator。

在每个线程执行的累加数量变多时，LongAdder比AtomicLong性能优势越发明显。LongAdder由于采用了分段理念，降低了线程间的竞争冲突，而AtomicLong却因多个线程并行竞争同一个value值，从而影响了性能。在低竞争的情况下，AtomicLong 和 LongAdder 这两个类具有相似的特征，吞吐量也是相似的，因为竞争不高。但是在竞争激烈的情况下，LongAdder 的预期吞吐量要高得多，经过试验，LongAdder 的吞吐量大约是 AtomicLong 的十倍，不过凡事总要付出代价，LongAdder 在保证高效的同时，也需要消耗更多的空间。原文链接：https://blog.csdn.net/limenghua9112/article/details/107950744

#### Lock

JUC 包 java.util.concurrent.locks 中提供了比 synchronized 更灵活的锁。

类 ReentrantLock：可重入锁。

接口 Condition：一个Condition实例本质上绑定到一个锁。 要获得特定Condition实例的Condition实例，请使用lock.newCondition()方法，一个锁可以有多个Condition。await/signal  等待/唤醒操作。

类 LockSupport：用于创建锁和其他同步类的基本线程阻塞原语。 方法park和unpark提供了阻止和解除阻塞线程的有效手段。

相比 Object 中的 wait/notify，Lock中的 await/singal 可以更加精确的唤醒某个线程。

#### 死锁

两个线程互相等待对方释放锁的情况。

避免死锁：   
避免一个线程同时获取多个锁；  
避免一个线程在锁内同时占用多个资源，尽量保证每个锁只占用一个资源；  
尝试使用定时锁，使用lock.tryLock(timeout)来替代使用内部锁机制；  
对于数据库锁，加锁和解锁必须在一个数据库连接里，否则会出现解锁失败的情况。

#### 线程池 

在JUC包java.util.concurrent中，主要接口有：Executor, ExecutorService, Callable, BlockingDeque, BlockingQueue, Future, ScheduleFuture 等。

工具类：Executors，快捷创建线程池。

ThreadPoolExecutor 的7个参数：  
corePoolSize - 即使空闲时仍保留在池中的线程数，除非设置 allowCoreThreadTimeOut   
maximumPoolSize - 池中允许的最大线程数   
keepAliveTime - 当线程数大于内核时，这是多余的空闲线程在终止前等待新任务的最大时间。  
unit - keepAliveTime参数的时间单位   
workQueue - 用于在执行任务之前使用的队列。 这个队列将仅保存execute方法提交的Runnable任务。   
threadFactory - 执行程序创建新线程时使用的工厂   
handler - 执行被阻止时使用的处理程序，因为达到线程限制和队列容量 。

#### CountDownLatch

CountDownLatch是在java1.5被引入，跟它一起被引入的工具类还有CyclicBarrier、Semaphore、ConcurrentHashMap和BlockingQueue。存在于java.util.cucurrent包下。来源链接：https://www.jianshu.com/p/e233bb37d2e6

CountDownLatch这个类使一个线程等待其他线程各自执行完毕后再执行。是通过一个计数器来实现的，计数器的初始值是线程的数量。每当一个线程执行完毕后，计数器的值就-1，当计数器的值为0时，表示所有线程都执行完毕，然后在闭锁上等待的线程就可以恢复工作了。

常用方法：countDown() 、await()。

#### CyclicBarrier

从字面上的意思可以知道，这个类的中文意思是“循环栅栏”。大概的意思就是一个可循环利用的屏障。它的作用就是会让所有线程都等待完成后才会继续下一步行动。

常用方法：await()、reset()。

CyclicBarrier 可以重复利用，这个是CountDownLatch做不到的。

#### Semaphore

Semaphore 通常我们叫它信号量， 可以用来控制同时访问特定资源的线程数量，通过协调各个线程，以保证合理的使用资源。

常用方法：acquire()、release()。

#### 本地线程 ThreadLocal

参考连接：https://www.cnblogs.com/fsmly/p/11020641.html

多线程访问同一个共享变量的时候容易出现并发问题，特别是多个线程对一个变量进行写入的时候，为了保证线程安全，一般使用者在访问共享变量的时候需要进行额外的同步措施才能保证线程安全性。ThreadLocal是除了加锁这种同步方式之外的一种保证一种规避多线程访问出现线程不安全的方法，当我们在创建一个变量后，如果每个线程对其进行访问的时候访问的都是线程自己的变量这样就不会存在线程不安全问题。

THreadLocalMap中的Entry的key使用的是ThreadLocal对象的弱引用，在没有其他地方对ThreadLoca依赖，ThreadLocalMap中的ThreadLocal对象就会被回收掉，但是对应的不会被回收，这个时候Map中就可能存在key为null但是value不为null的项，这需要实际的时候使用完毕**及时调用remove方法避免内存泄漏**。

#### 集合

常用的 java.util 包中的集合（ArrayList，LinkedList 等）是线程不安全的，可以通过集合工具类Collections中的静态同步方法（如：Collections.synchronizedList(list)）将线程不安全的集合转换为线程安全的集合。Collections 的同步方法也不一定线程安全（Vector 也是不一定线程安全的），多线程添加删除时可能会报java.util.ConcurrentModificationException 异常。

JUC包中提供了线程安全的集合：ConcurrentHashMap、CopyOnWriteArrayList 等，这些集合类都是线程安全的。

#### Stream

Java8中出现，在包 java.util.stream 中，可以很方便的开启并行模式：

list.parallelStream()

list.stream().parallel()

#### 常见异常 Exception

java.util.ConcurrentModificationException

java.lang.ArrayIndexOutOfBoundsException

java.lang.UnsupportedOperationException

java.lang.IllegalMonitorStateException
