### 2.（必做）思考有多少种方式，在 main 函数启动一个新线程，运行一个方法，拿到这个方法的返回值后，退出主线程? 写出你的方法，越多越好，提交到 GitHub。

#### 参考代码：

```java
/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 *
 * 一个简单的代码参考：
 */
public class Homework03 {
    
    public static void main(String[] args) {
        
        long start=System.currentTimeMillis();

        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
    
        int result = sum(); //这是得到的返回值
        
        // 确保  拿到result 并输出
        System.out.println("异步计算结果为："+result);
         
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
        
        // 然后退出main线程
    }
    
    private static int sum() {
        return fibo(36);
    }
    
    private static int fibo(int a) {
        if ( a < 2) {
            return 1;
        }
        return fibo(a-1) + fibo(a-2);
    }
}
```

#### 方式1 ：Thread.sleep()

创建一个新线程调用方法，在主线程中等待 sleep 一会等待线程计算完成。因为基本数据类型的局部变量在匿名内部类中必须是final的（也可以定义全局静态属性来记录），所以可以利用引用类型记录线程中计算的结果，这里使用数组记录，代码如下：

```java
    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        int[] nums = new int[1]; // 利用数组记录线程的计算结果
        new Thread(() -> {
            nums[0] = sum();
        }).start();

        // 确保  拿到result 并输出
        Thread.sleep(1000);
        int result = nums[0]; //这是得到的返回值

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

#### 方式2 ：Thread.yield() + 标志位

方式一中有个明显的缺点，就是线程的执行时间是不确定的，主线程中 sleep 多久是无法提前预知的，可以进一步改进，将记录结果的数组改为带有标记位的引用类型（这里也可以定义全局静态属性标志），主线程中一直监视标志位，如果计算完成则获取计算结果，代码如下：

```java
    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        class R{
            int val; // 记录计算结果
            boolean calcFinish; // 记录时间计算完成
        }
        R r = new R();
        new Thread(() -> {
            r.val = sum();
            r.calcFinish = true;
        }).start();

        // 确保  拿到result 并输出
        while (!r.calcFinish){
            Thread.yield(); // 如果还没有计算完成，则让出 cpu 时间片等待
        }
        int result = r.val; //这是得到的返回值

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

#### 方式3：Thread.yield() + Thread.activeCount()

方式二中需要定义额外的标志位判断，如果不用标志位，可以根据当前线程数判断是否计算完成，在Idea环境中，main方法运行时，JVM中至少会有两个活动线程：main 线程 和 Idea 线程（Thread[Monitor Ctrl-Break,5,main]），所以可以判断当前活动线程数是否为 2 即可，代码如下：

```java
    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        int[] nums = new int[1]; // 利用数组记录线程的计算结果
        new Thread(() -> {
            nums[0] = sum();
        }).start();

        // 确保  拿到result 并输出
        while (Thread.activeCount() > 2){
            Thread.yield(); // 如果还没有计算完成，则让出 cpu 时间片等待
        }
        int result = nums[0]; //这是得到的返回值

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

#### 方式4：thread.join()

thread.join() 方法可以等待这个线程死亡后执行，代码如下：

```java
    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        int[] nums = new int[1]; // 利用数组记录线程的计算结果
        Thread thread = new Thread(() -> {
            nums[0] = sum();
        });
        thread.start();
        // 确保  拿到result 并输出
        thread.join();
        int result = nums[0]; //这是得到的返回值

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

#### 方法5：thread.isAlive() + Thread.yield()

thread.isAlive() 可以测试这个线程是否活着，代码如下：

```java
    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        int[] nums = new int[1]; // 利用数组记录线程的计算结果
        Thread thread = new Thread(() -> {
            nums[0] = sum();
        });
        thread.start();
        // 确保  拿到result 并输出
        while (thread.isAlive()){
            Thread.yield();
        }
        int result = nums[0]; //这是得到的返回值

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

#### 方法6：thread.getState() + Thread.yield()

Java 中Thread.State定义了6重线程状态：NEW, RUNNABLE,  BLOCKED,  WAITING, TIME_WAITING, TERMINATED, 分别为：新建，运行，阻塞，等待，带超时的等待，死亡，因此可以判断线程状态是否死亡，代码如下：

```java
    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        int[] nums = new int[1]; // 利用数组记录线程的计算结果
        Thread thread = new Thread(() -> {
            nums[0] = sum();
        });
        thread.start();
        // 确保  拿到result 并输出
        while (thread.getState() != Thread.State.TERMINATED){
            Thread.yield();
        }
        int result = nums[0]; //这是得到的返回值

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

#### 方法7：synchronized (thread)

thread 线程结束后会调用自己的 notifyAll()方法，所以可以将 thread 作为同步代码块的锁阻塞主线程直到 thread 线程结束，代码如下：

```java
    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        int[] nums = new int[1]; // 利用数组记录线程的计算结果
        Thread thread = new Thread(() -> {
            nums[0] = sum();
        });
        thread.start();
        // 确保  拿到result 并输出
        synchronized (thread){
            thread.wait();
        }
        int result = nums[0]; //这是得到的返回值

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

#### 方法8：synchronized + Object中的 wait()/notify()/notifyAll()

使用对象锁，通过 wait()/notify()/notifyAll() 机制唤醒主线程，这种方式的缺点是无法保证notify方法一定在wait方法之后执行，代码如下：

```java
    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        int[] nums = new int[1]; // 利用数组记录线程的计算结果
        Thread thread = new Thread(() -> {
            synchronized (nums){
                nums[0] = sum();
                nums.notifyAll();
            }
        });
        thread.start();
        // 确保  拿到result 并输出
        synchronized (nums){
            nums.wait();
        }
        int result = nums[0]; //这是得到的返回值

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

以上方法无法保证主线程和子线程谁先获取到锁，为了保证让子线程先获取锁，可以在主线程获取到锁后再启动子线程：

```java
    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        int[] nums = new int[1]; // 利用数组记录线程的计算结果
        Thread thread = new Thread(() -> {
            synchronized (nums){
                nums[0] = sum();
                nums.notifyAll();
            }
        });
        
        // 确保  拿到result 并输出
        synchronized (nums){
            thread.start();
            nums.wait();
        }
        int result = nums[0]; //这是得到的返回值

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

#### 方法9：Callable<V> + Thread

使用 Callable<V> 创建带返回值的异步任务，通过实现了 Runnable 和  Future<V> 的子接口（ RunnableFuture <V> 、RunnableScheduledFuture <V> 等）或实现类（FutureTask 等）开启异步任务，这里使用FutureTask 实现，代码如下：

```java
    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        FutureTask<Integer> futureTask = new FutureTask<>(() -> sum());
        Thread thread = new Thread(futureTask);
        thread.start();
        // 确保  拿到result 并输出
        int result = futureTask.get(); //这是得到的返回值

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

#### 方法10： 线程池 executorService.submit(Callable<V>)

利用线程池（ExecutorService、ScheduledExecutorService 等）提交一个有返回值的任务（可以用invokeAll()、invokeAny()、submit() 等方法），这里使用executorService.submit()，代码如下：

```java
    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<Integer> future = executorService.submit(() -> sum());
        // 确保  拿到result 并输出
        int result = future.get(); //这是得到的返回值

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

#### 方法11：CompletableFuture

Java 1.8 提供的 CompletableFuture 实现了CompletionStage接口和Future接口，前者是对后者的一个扩展，增加了异步回调、流式处理、多个Future组合处理的能力，使Java在处理多任务的协同工作时更加顺畅便利。这里使用CompletableFuture.supplyAsync() 创建一个异步任务，代码如下：

```java
    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        // 确保  拿到result 并输出
        int result = CompletableFuture.supplyAsync(() -> sum()).get();

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

#### 方法12：Lock + Condition await/signal

方法 8 中的 wait/notify 方式有个缺点是不能保证 wait 和 notify 调用的先后顺序，如果 wait 执行比 notify 延后，则主线程会一直等。使用 Java 并发包（JUC）的 Lock 与 Condition 的 await/signal 机制可以做到精确唤醒，代码如下：

```java
    static final Lock lock = new ReentrantLock(); // 定义锁
    static final Condition condition = lock.newCondition(); // Condition

    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        int[] nums = new int[1];
        new Thread(() -> {
            lock.lock();
            try {
                nums[0] = sum();
                condition.signal(); // 通知主线程可以获取值了
            } finally {
                lock.unlock();
            }
        }).start();

        lock.lock();
        try {
            // 确保  拿到result 并输出
            condition.await();
            int result = nums[0];

            System.out.println("异步计算结果为："+result);
            System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
        } finally {
            lock.unlock();
        }
    }
```

更加完善的 Lock/Condition 实现：

```java
    static boolean calcFinish; // 计算标记
    static final Lock lock = new ReentrantLock(); // 定义锁
    static final Condition subC = lock.newCondition(); // 子线程 Condition
    static final Condition mainC = lock.newCondition(); // 主线程 Condition

    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        calcFinish = false;
        int[] nums = new int[1];
        new Thread(() -> {
            lock.lock();
            try {
                while (calcFinish){ // 如果已经计算完则等待
                    subC.await();
                }
                nums[0] = sum();
                calcFinish = true; // 计算完成，设置标志位为 true
                mainC.signal(); // 通知主线程可以获取值了
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }).start();
        // 确保  拿到result 并输出
        lock.lock();
        try {
            while (!calcFinish){ // 如果没有计算完，则循环等待
                mainC.await();
            }
            int result = nums[0];
            System.out.println("异步计算结果为："+result);
            System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");

            calcFinish = false; // 结果获取完成，设置标志位为 true
            subC.signal(); // 通知子线程可以进行下一次计算了
        } finally {
            lock.unlock();
        }
    }
```

#### 方法13：LockSupport park/unpark

LockSupport类，是JUC包中的一个工具类，是用来创建锁和其他同步类的基本线程阻塞原语。

与Object的wait/notify的区别主要有两点：

（1）wait/notify都是Object中的方法,在调用这两个方法前必须先获得锁对象，但是park/unpark不需要获取某个对象的锁就可以锁住线程。

（2）notify只能随机选择一个线程唤醒，无法唤醒指定的线程，unpark却可以唤醒一个指定的线程。

使用 LockSupport 的实现代码如下：

```java
    static int sum = 0; // 全局静态属性记录计算结果

    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        final Thread mainThread = Thread.currentThread();
        new Thread(() -> {
            sum = sum();
            LockSupport.unpark(mainThread);
        }).start();
        // 确保  拿到result 并输出
        LockSupport.park();
        int result = sum;

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

#### 方法14：CountDownLatch

CountDownLatch是在java1.5被引入，跟它一起被引入的工具类还有CyclicBarrier、Semaphore、ConcurrentHashMap和BlockingQueue，都在JUC工具包中。

CountDownLatch是通过一个计数器来实现的，计数器的初始化值为线程的数量。每当一个线程完成了自己的任务后，计数器的值就相应得减1。当计数器到达0时，表示所有的线程都已完成任务，然后在闭锁上等待的线程就可以恢复执行任务。 

实现代码：

```java
    static int sum = 0; // 全局静态属性记录计算结果

    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            sum = sum();
            countDownLatch.countDown();
        }).start();
        // 确保  拿到result 并输出
        countDownLatch.await();
        int result = sum;

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

#### 方法15：CyclicBarrier

利用CyclicBarrier类可以实现一组线程相互等待，当所有线程都到达某个屏障点后再进行后续的操作。实现代码：

```java
    static int sum = 0; // 全局静态属性记录计算结果

    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        CyclicBarrier barrier = new CyclicBarrier(2);
        new Thread(() -> {
            sum = sum();
            try {
                barrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        // 确保  拿到result 并输出
        barrier.await();
        int result = sum;

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

#### 方法16：Semaphore

Semaphore 通常我们叫它信号量， 可以用来控制同时访问特定资源的线程数量，通过协调各个线程，以保证合理的使用资源。

代码如下：

```java
    static int sum = 0; // 全局静态属性记录计算结果

    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        Semaphore semaphore = new Semaphore(0);

        new Thread(() -> {
            sum = sum();
            semaphore.release();
        }).start();
        // 确保  拿到 result 并输出
        semaphore.acquire();
        int result = sum;

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

#### 方法17：阻塞队列 BlockingQueue

BlockingQueue不光实现了一个完整队列所具有的基本功能，同时在多线程环境下，他还自动管理了多线间的自动等待于唤醒功能，从而使得程序员可以忽略这些细节，关注更高级的功能。

所有已知实现类： 
ArrayBlockingQueue ， DelayQueue ， LinkedBlockingDeque ， LinkedBlockingQueue ， LinkedTransferQueue ， PriorityBlockingQueue ， SynchronousQueue 。

SynchronousQueue 也是一个队列来的，但它的特别之处在于它内部没有容器，一个生产线程，当它生产产品（即put的时候），如果当前没有人想要消费产品(即当前没有线程执行take)，此生产线程必须阻塞，等待一个消费线程调用take操作，take操作将会唤醒该生产线程，同时消费线程会获取生产线程的产品（即数据传递），这样的一个过程称为一次配对过程(当然也可以先take后put,原理是一样的)。

不像ArrayBlockingQueue、LinkedBlockingDeque之类的阻塞队列依赖AQS实现并发操作，SynchronousQueue直接使用CAS实现线程的安全访问。

原文链接：https://blog.csdn.net/yanyan19880509/article/details/52562039

实现代码：

```java
    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();

        // 结果只有一个元素，可以使用 SynchronousQueue
        BlockingQueue<Integer> blockingQueue = new SynchronousQueue<>();
        new Thread(() -> {
            try {
                blockingQueue.put(sum());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // 确保  拿到result 并输出
        int result = blockingQueue.take();

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

#### 方法18：线程间交换数据的 Exchanger

Exchanger（交换者）是一个用于线程间协作的工具类。Exchanger用于进行线程间的数据交 换。它提供一个同步点，在这个同步点，两个线程可以交换彼此的数据。这两个线程通过 exchange方法交换数据，如果第一个线程先执行exchange()方法，它会一直等待第二个线程也 执行exchange方法，当两个线程都到达同步点时，这两个线程就可以交换数据，将本线程生产 出来的数据传递给对方。

实现代码：

```java
    public static void main(String[] args) throws Exception {

        long start=System.currentTimeMillis();
        Exchanger<Integer> exchanger = new Exchanger<>();

        Thread thread = new Thread(() -> {
            try {
                Integer exchange = exchanger.exchange(sum());
                System.out.println("主线程传给子线程的值：" + exchange);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        
        // 确保  拿到result 并输出
        int result = exchanger.exchange(0); //这是得到的返回值

        System.out.println("异步计算结果为："+result);
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }
```

