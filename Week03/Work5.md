### 5.（选做）跑一跑课上的各个例子，加深对多线程的理解。

#### Object 中多线程相关的方法：

void wait()	导致当前线程等待，直到另一个线程调用该对象的 notify()方法或 notifyAll()方法。  
void wait(long timeout) 	指定等待时间。  
void wait(long timeout, int nanos)	可以设置纳秒级时间。  
void notify()	唤醒正在等待对象监视器的单个线程。  
void notifyAll()	唤醒正在等待对象监视器的所有线程。    

wait() = wait(0) = wait(0, 0)，这三种调用方式是等价的，wait方法会释放锁。  

#### Thread 中常见的方法：

void join() 	等待这个线程死亡，可以设置毫秒、纳秒时间 。  
static void sleep(long millis)	使当前正在执行的线程以指定的毫秒数暂停（暂时停止执行），不会释放锁，第二个参数可以设置纳秒时间。  
void start() 	线程进入就绪状态，调用后Java虚拟机可以调用此线程的run方法。   
static void yield()	让出CPU时间片，与其它线程一起竞争CPU，不会释放锁。   

线程死的时候会调用自己的 notifyAll 方法，join会执行结束。  

#### Thread.State 枚举的6中线程状态：

​	NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED  

#### 线程池：

顶层接口：Executor  
子接口：ExecutorService ，ScheduledExecutorService   
常用实现类：ThreadPoolExecutor  
	参数：  
	corePoolSize - 即使空闲时仍保留在池中的线程数，除非设置 allowCoreThreadTimeOut   
	maximumPoolSize - 池中允许的最大线程数。  
	keepAliveTime - 当线程数大于内核时，这是多余的空闲线程在终止前等待新任务的最大时间。  
	unit - keepAliveTime参数的时间单位。  
	workQueue - 用于在执行任务之前使用的队列。 这个队列将仅保存execute方法提交的Runnable任务。  
	threadFactory - 执行程序创建新线程时使用的工厂。  
	handler - 执行被阻止时使用的处理程序，因为达到线程限制和队列容量。  
工具类：Executors  

#### 有返回值的线程：

可以通过ExecutorService 的submit() 方法提交一个有返回值的线程，也可以通过 Runnable 的子类（例如：FutureTask(Callable<V> callable) ）创建一个返回值的线程，例子：

```java
public class Test {

    public static void main(String[] args) throws Exception {
        FutureTask<Integer> task = new FutureTask<>(() -> {
            TimeUnit.SECONDS.sleep(5);
            int x = new Random().nextInt(10);
            System.out.println("x = " + x);
            return x;
        });
        new Thread(task).start();
        System.out.println("等待返回结果...");
        System.out.println(task.get());
    }

}
```

