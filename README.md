# 极客时间 Java进阶训练营 毕业总结

分别用 100 个字以上的一段话，加上一幅图（架构图或脑图），总结自己对下列技术的关键点思考和经验认识。

- JVM
- NIO
- 并发编程
- Spring 和 ORM 等框架
- MySQL 数据库和 SQL
- 分库分表
- RPC 和微服务
- 分布式缓存
- 分布式消息队列

[TOC]

## 一、JVM

JVM是Java Virtual Machine（Java虚拟机）的缩写，Java语言使用Java虚拟机屏蔽了与具体平台相关的信息，使得Java语言编译程序只需生成在Java虚拟机上运行的目标代码（字节码），就可以在多种平台上不加修改地运行。

JVM包括类加载器子系统、运行时数据区、执行引擎和本地接口库，其中运行时数据区如下图所示：

![image-20220222114428280](https://raw.githubusercontent.com/LiBaoshe/images/master/imgs/image-20220222114428280.png)

### 字节码技术

字节码指令Java bytecode由单字节（byte）的指令组成，理论上最多支持256个操作码（opcode）,实际上Java只使用了200左右的操作码，还有一些操作码则保留给调试操作。根据指令的性质，主要分为四个大类：1.栈操作指令，2.程序流程控制指令，3.对象操作指令，4.算术运算以及类型转换指令。

### 类加载器

启动类加载器、扩展类加载器、应用类加载器、自定义类加载器。加载器特点：双亲委托、负责依赖、缓存加载。类的生命周期：加载（class文件）、验证（格式、依赖）、准备（静态变量初始值、方发表）、解析（常量池内的符号引用替换为直接引用）、初始化（构造器、静态变量赋值、静态代码块）、使用、卸载。

### 内存模型

JMM 规范明确定义了不同的线程之间，通过哪些方式，在什么时候可以看见其他线程保存到共享变量中的值；以及在必要时，如何对共享变量的访问进行同步。这样的好处是屏蔽各种硬件平台和操作系统之间的内存访问差异，实现了 Java 并发程序真正的跨平台。

### 启动参数

以 - 开头为标准参数，-D设置系统属性，-X开头为非标准参数，-XX开头为非稳定参数。

### JDK命令行工具

| 工具      | 简介                     | 工具           | 简介              |
| --------- | ------------------------ | -------------- | ----------------- |
| jps/jinfo | 查看java进程             | jstat          | 查看JVM内部GC信息 |
| jmap      | 查看heap或类占用空间统计 | jstack         | 查看线程信息      |
| jcmd      | 整合命令                 | jrunscript/jjs | 执行js命令        |

jstat -gcutil pid 1000 1000		jstat -gc pid 1000 1000		

jmap -heap pid 打印堆内存的配置和使用信息

jmap -histo pid 看哪些类占用的空间多，直方图

jmap -dump:fomat=b,file=pid.hprof pid	dump堆内存

jstack -F（强制执行）/ -m（混合模式）/ -l（长列表模式，包含locks信息） pid

jcmd pid VM.version		jcmd pid Thread.print		jcmd GC.heap_info

### JDK图形化工具

jconsole,  jvisualvm,  VisualGC,  jmc

### GC算法

#### 确定垃圾算法

1. 引用计数算法，存在循环引用的问题。
2. 可达性分析算法，从GC Roots开始搜索对象是否可达，可作为GC Roots的对象包括以下几种：栈中的引用、方法区中的静态引用、JNI中的引用、JVM内部引用（基本数据类型对应的Class对象，常驻异常对象（如NPE，OOM等），系统类加载器）、被同步锁（synchronized）持有的对象等。

#### 垃圾回收算法

| 算法         | 特点                                                         |
| ------------ | ------------------------------------------------------------ |
| 标记清除算法 | 引起内存碎片化，大对象无法活动连续可用的空间。               |
| 复制算法     | 内存浪费一半，只适合“朝生夕死”的对象回收。                   |
| 标记整理算法 | 结合了标记清除和复制算法的优点，标记完后将存活的对象移到另一端，清除该端。 |
| 分代收集算法 | 针对不同对象类型（生命周期长短、对象大小），采用不同的垃圾回收算法。 |

JVM运行时内存也叫作JVM堆，从GC的角度可以分为新生代（默认占1/3空间），老年代（默认占2/3空间），永久代（元空间，占非常少的空间）。新生代又分为Eden区、SurviorTo区和SurivorFrom区，默认占比 8:1:1。

新生代GC叫做MinorGC，采用复制算法。主要存生命周期短的对象和小对象，新建的对象存放在Eden区，空间不足时触发MinorGC。1）把Eden区和SurvivorFrom区中存活的对象复制到SurvivorTo区，2）清空Eden区和SurvivorFrom区中的对象，3）将SurvivorTo区和SurvivorFrom区互换。

老年代GC叫做MajorGC，采用标记清除/整理算法。主要存生命周期长的对象和大对象，MajorGC之前会进行一次MinorGC，MajorGC耗时较长，老年代没有空间分配时会抛出OOM异常。

永久代指内存的永久保存区域，GC不会在运行期间对永久代的内存进行清理。Java8中改为元空间，元空间并没有使用JVM内存，而是直接使用操作系统的本地内存，类的元数据放入本地内存中，常量池和类的静态变量放入Java堆中。

#### 4种引用类型

1. 强引用（造成内存泄漏）
2. 软引用（SoftReference，空间不足时会被回收）
3. 弱引用（WeakReference，垃圾回收过程中一定被回收）
4. 虚引用（PhantomRefrence，和引用队列联合使用，主要用于跟踪对象的垃圾回收状态）

### 经典垃圾收集器

并行（Parallel）：并行描述的是多条垃圾收集器线程之间的关系。

并发（Concurrent）：并发描述的是垃圾收集器线程与用户线程之间的关系。

| 收集器            | 模式 | 收集区 | 算法          | 目标         | 适用场景              |
| ----------------- | ---- | ------ | ------------- | ------------ | --------------------- |
| Serial            | 串行 | 新生代 | 复制算法      | 响应速度优先 | 单CPU的client模式     |
| Serial Old        | 串行 | 老年代 | 标记整理      | 响应速度优先 | 单CPU，CMS的备案      |
| ParNew            | 并行 | 新生代 | 复制算法      | 响应速度优先 | 多CPU配合CMS          |
| Parallel Scavenge | 并行 | 新生代 | 复制算法      | 吞吐量优先   | 运算多交互少          |
| Parallel Old      | 并行 | 老年代 | 标记整理      | 吞吐量优先   | 运算多交互少          |
| CMS               | 并发 | 老年代 | 标记清除      | 响应速度优先 | B/S服务端             |
| G1                | 并发 | both   | 标记整理+复制 | 响应速度优先 | 服务端,代替CMS,大内存 |

Parallel Scavenge收集器提供了两个参数用于精确控制吞吐量，分别是控制最大垃圾收集停顿时间的-XX：MaxGCPauseMillis参数以及直接设置吞吐量大小的-XX：GCTimeRatio参数。Parallel Scavenge收集器还有一个参数-XX：+UseAdaptiveSizePolicy值得我们关注。这是一 个开关参数，当这个参数被激活之后，就不需要人工指定新生代的大小（-Xmn）、Eden与Survivor区 的比例（-XX：SurvivorRatio）、晋升老年代对象大小（-XX：PretenureSizeThreshold）等细节参数 了，虚拟机会根据当前系统的运行情况收集性能监控信息，动态调整这些参数以提供最合适的停顿时 间或者最大的吞吐量。这种调节方式称为垃圾收集的自适应的调节策略（GC Ergonomics）。

CMS，Concurrent Mark Sweep，设计目标是避免在老年代垃圾收集时出现长时间的卡顿，主要通过两种手段来达成此目标：

1. 不对老年代进行整理，而是使用空闲列表（free-lists）来管理内存空间的回收。
2. 在 mark-and-sweep （标记-清除） 阶段的大部分工作和应用线程一起并发执行。

默认情况下，CMS 使用的并发线程数等于 CPU 核心数的 1/4。

CMS的六个阶段：

1. **初始标记（STW）**，只标记 GC Roots。
2. **并发标记**，和用户线程并发执行，根据GC Roots可达性分析。
3. 并发预清理，如果在并发标记过程中引用关系发生了变化，JVM 会通过“Card（卡片）”的方式将发生了改变的区域标记为“脏”区，这就是所谓的 卡片标记（Card Marking）。
4. **重新标记（STW）**，确保并发执行的部分对象的状态正确性。
5. **并发清除**，和用户线程并发执行。
6. 并发重置。

G1，Garbage-First，意为垃圾优先，哪一块的垃圾最多就优先清理它。G1 GC 最主要的设计目标是：将 STW 停顿的时间和分布，变成可预期且可配置的。-XX:+UseG1GC -XX:MaxGCPauseMillis=50，默认值是200毫秒

G1的4个步骤：

1. 初始标记（STW），仅标记 GC Roots。
2. 并发标记，可达性分析，与用户线程并发执行。
3. 最终标记（STW），处理并发阶段遗留的记录。
4. 筛选回收（STW），对各个Region的回收价值和成本进行排序，根据用户所期望的停顿时间来制定回收计划。

常用垃圾收集器组合

![image-20220222180340176](https://raw.githubusercontent.com/LiBaoshe/images/master/imgs/image-20220222180340176.png)

（1）Serial + Serial Old，实现单线程的低延迟垃圾回收机制。

（2）ParNew + CMS，实现多线程的低延迟垃圾回收机制。

（3）Parallel Scavenge + Parallel Old，实现多线程的高吞吐量垃圾回收机制。

### 低延迟垃圾收集器

ZGC，最大停顿时间不超过10ms，支持超大内存（4TB，JDK13升至16TB），当前只支持Linux/x64，JDK15后支持MacOS和Windows系统。

Shenandoah GC 立项比 ZGC 更早，设计为GC 线程与应用线程并发执行的方式，通过实现垃圾回收过程的并发处理，改善停顿时间，使得 GC 执行线程能够在业务处理线程运行过程中进行堆压缩、标记和整理，从而消除了绝大部分的暂停时间。Shenandoah 团队对外宣称 Shenandoah GC 的暂停时间与堆大小无关，无论是 200 MB 还是 200 GB的堆内存，都可以保障具有很低的暂停时间（注意:并不像 ZGC 那样保证暂停时间在 10ms 以内）。

### 调优分析

GCEasy 官网地址：https://gceasy.io/， GCeasy是一款在线的GC日志分析器，可以通过GC日志分析进行内存泄露检测、GC暂停原因分析、JVM配置建议优化等功能，而且是可以免费使用的（有一些服务是收费的）。

离线版的GCViewer，需要jdk1.8才可以使用，Github地址为https://github.com/chewiebug/GCViewer， 下载下来之后执行 mvn clean install -Dmaven.test.skip=true 命令进行编译，编译完成后在target目录下会看到jar包，双击打开即可。

fastthread 线程分析，https://fastthread.io/ 

Arthas 是Alibaba开源的Java诊断工具，深受开发者喜爱。在线排查问题，无需重启；动态跟踪Java代码；实时监控JVM状态。在线教程：https://arthas.aliyun.com/doc/arthas-tutorials.html?language=cn 

## 二、NIO

同步异步是通信模式，阻塞非阻塞是线程处理模式。

### Linux 网络 I/O 模型

| IO模型         | 描述                                             |
| -------------- | ------------------------------------------------ |
| 阻塞IO模型     | 用户线程一直阻塞等待数据就绪                     |
| 非阻塞IO模型   | 用户线程不断询问数据是否就绪                     |
| 多路复用IO模型 | 一个 Selector 线程轮询多个 Socket 上的事件       |
| 信号驱动IO模型 | 数据就绪时，发信号通知用户线程来调用对应的IO操作 |
| 异步IO模型     | 数据准备完成后直接复制到用户线程                 |

异步IO模型需要操作系统的底层支持，在 Java7 中提供了Asynchronous I/O 操作。

Java NIO 的核心类库多路复用器 Selector 基于 epoll 的多路复用技术实现。目前支持I/O多路复用的系统调用有select、plelect、poll、epoll，select存在缺陷，Linux最终选择了epoll。

epoll 的重大改进如下：

1. 支持一个进程打开的 socket 描述符（FD）不受限制（仅受限于操作系统的最大文件句柄数，1G内存大约10万个句柄）。select 单个进程打开的FD是有限制的，由 FD_SETSIZE 设置，默认是1024。
2. I/O 效率不会随着FD数目的增加而线性下降。select/poll每次调用都会线性扫描全部的socket集合，导致效率线性下降。epoll只会对 “活跃” 的socket进行操作，epoll是根据每个fd上面的callback函数实现的，只有 “活跃” 的 socket 才会主动调用 callback 函数，其它 idle 状态 socket 则不会。在这点上，epoll 实现了一个伪 AIO。
3. 使用 mmap 加速内核与用户空间的消息传递（**零拷贝**）。epoll 通过内核和用户空间 mmap 同一块内存实现。mmap适合小数据量读写，sendFile适合大文件传输。
4. epoll 的 API 更加简单。包括创建一个 epoll 描述符、添加监听事件、阻塞等待所监听的事件发生、关闭 epoll 描述符等。

### Java NIO

JDK1.0到JDK1.3使用BIO，JDK1.4引入NIO，JDK1.7升级NIO2.0，引入了AIO。

传统IO面向流，NIO面向缓冲区；传统IO的流操作是阻塞模式的，NIO的流操作是非阻塞模式的。

Java NIO三大核心内容：选择器、通道、缓冲区。

1. 缓冲区 Buffer，实质上是一个数组，最常用的是ByteBuffer，每一种Java基本类型（除了Boolean类型）都对应有一个缓冲区，ByteBuffer、CharBuffer、ShortBuffer、IntBuffer、LongBuffer、FloatBuffer、DoubleBuffer，都是 Buffer 接口的子类。MappedByteBuffer 是 ByteBuffer 的子类。
2. 通道 Channel，全双工通道，流（InputStream或OutputStream）是单向的。可以分为两大类：用于网络读写的 SelectableChannel 和用于文件操作的 FileChannel。
3. 多路复用器 Selector，Java NIO 编程的基础，不断轮询注册在其上的 Channel，监听SelectionKey事件：SelectionKey.OP_CONNECT 连接就绪，SelectionKey.OP_ACCEPT 接收就绪，SelectionKey.OP_READ 读就绪，SelectionKey.OP_WRITE 写就绪。

不选择Java原生NIO编程的原因：

1. NIO的类库和API繁杂，使用麻烦。
2. 需要其它额外技能，如多线程编程和网络编程。
3. 可靠性能力补齐，工作量和难度都非常大，如断连重连、网络闪断、半包读写、失败缓存、网络拥塞、异常码流的处理等问题。
4. JDK NIO 的 BUG，如臭名昭著的 epoll 空轮询 bug 导致 CPU 100%。官方声称修复了问题，但是问题仍旧存在，只不过是概率降低了而已，并没有根本解决。

### Netty

为什么选择 Netty？Netty 是业界最流行的 NIO 框架之一，很多 RPC 框架使用 Netty 作为底层通信框架。

1. API 使用简单，开发门槛低；
2. 功能强大，预制了多种编解码功能，支持多种主流协议；
3. 定制能力强，可以通过ChannelHandler对通信框架进行灵活的扩展；
4. 性能高，成熟，稳定，修复了所有 JDK NIO BUG；
5. 社区活跃，发现的BUG可以及时修复；
6. 经历了大规模的商业应用考验，质量得到验证。

**基本概念**

1. Channel，通道，Netty 对 Channel 的所有 IO 操作都是非阻塞的。
2. ChannelFuture，Netty 封装一个 ChannelFuture 接口，将回调方法传给 ChannelFuture，在操作完成时自动执行。
3. Event & Handler，Netty 基于事件驱动，事件和处理器可以关联到入站和出站数据流。
4. Encoder & Decoder，对入站数据进行解码，基类是 ByteToMessageDecoder。 对出站数据进行编码，基类是 MessageToByteEncoder。
5. ChannelPipeline，数据处理管道就是事件处理器链。有顺序、同一 Channel 的出站处理器和入站处理器在同一个列表中。

**什么是高性能？**

高并发用户、高吞吐量、低延迟、容量。

**Reactor 模型**

Reactor(NIO) 和 Proactor(AIO)。

根据 Reactor 的数量和处理资源池线程的数量不同，有3种典型的实现：

1. 单 Reactor 单线程，所有操作都在一个线程中完成。NioEventLoopGroup(1);

   ![image-20220223155331453](https://raw.githubusercontent.com/LiBaoshe/images/master/imgs/image-20220223155331453.png)

2. 单 Reactor 多线程，handler 只负责响应事件，具体业务交给业务线程池完成。NioEventLoopGroup(n>1);

   ![image-20220223155615820](https://raw.githubusercontent.com/LiBaoshe/images/master/imgs/image-20220223155615820.png)

3. 主从 Reactor 多线程，主线程只接收新连接，子线程完成后续的业务处理。bossGroup + workGroup

   ![image-20220223155749888](https://raw.githubusercontent.com/LiBaoshe/images/master/imgs/image-20220223155749888.png)

**TCP 粘包/拆包**

ByteToMessageDecoder 提供的一些常见的实现类：

LineBasedFrameDecoder 行分隔符解码器（“\n” 或 "\r\n" 为结束符），StringDecoder

DelimiterBasedFrameDecoder 可以自动完成以分割符作为码流结束标识的消息的解码。

FixedLengthFrameDecoder 固定长度解码器，指定固定的字节数。

LengthFieldBasedFrameDecoder：长度编码解码器，将报文划分为报文头/报文体

JsonObjectDecoder：json 格式解码器，当检测到匹配数量的“{” 、”}”或”[””]”时，则认为是一个完整的 json 对象或者 json 数组。

**Nagle 算法与 TCP_NODELAY**

TCP/IP协议中针对TCP默认开启了Nagle算法。Nagle算法通过减少需要传输的数据包，来优化网络。在内核实现中，数据包的发送和接受会先做缓存，分别对应于写缓存和读缓存。

启动TCP_NODELAY，就意味着禁用了Nagle算法，允许小包的发送。

关闭TCP_NODELAY，则是应用了Nagle算法。数据只有在写缓存中累积到一定量之后，才会被发送出去，这样明显提高了网络利用率（实际传输数据payload与协议头的比例大大提高）。但是这又不可避免地增加了延时；

![image-20220223164451036](https://raw.githubusercontent.com/LiBaoshe/images/master/imgs/image-20220223164451036.png)

## 三、并发编程

为什么会有多线程？本质原因是摩尔定律失效 -> 多核+分布式时代的来临。JVM、NIO、分布式系统都因为这个问题变复杂。

进程是资源分配的最小单位，线程是CPU调度的最小单位。一个进程由一个或多个线程组成，线程上下文切换比进程上下文切换要快得多。

上下文：线程切换时CPU寄存器和程序计数器所保存的当前线程的信息。

上下文切换：内核在CPU上对进程或者线程进行切换。

### 线程状态

![img](https://raw.githubusercontent.com/LiBaoshe/images/master/imgs/38595f7bf7094837a874377b3f567401.jpg)

Java将操作系统中的运行和就绪两个状态合并称为运行状态。阻塞状态是线程 阻塞在进入synchronized关键字修饰的方法或代码块（获取锁）时的状态，但是阻塞在 java.concurrent包中Lock接口的线程状态却是等待状态，因为java.concurrent包中Lock接口对于 阻塞的实现均使用了LockSupport类中的相关方法。

Thread.State 枚举的六种状态 NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED

Thread.sleep: 释放 CPU，Object#wait : 释放对象锁。

如果线程没有被阻塞，这时调用 interrupt() 将不起作用，直到执行到wait/sleep/join 时，才马上会抛出InterruptedException。如果线程被阻塞，调用 interrupt() 将抛出一个 InterruptedException 中断异常。

### 线程安全

多个线程竞争同一资源时，如果对资源的访问顺序不进行恰当的控制，会导致线程安全问题。

- **原子性**，操作不可被中断，要么执行，要么不执行。
- **可见性**，一个线程改变数据后其它线程可以感知到。
- **有序性**，Java 允许编译器和处理器对指令进行重排序，不会影响到单线程程序的执行，却会影响到多线程并发执行的正确性。happens-before 原则。

volatile 关键字保证可见性、有序性（禁止指令重排序），不保证原子性。

final 实例属性，构造函数结束返回时，final 域最新的值被保证对其他线程可见。

### 锁

**乐观锁**，每次读写数据时都认为其它线程不会修改该数据，先不加锁操作。Java中的乐观锁大部分是通过 CAS（Compare And Swap，比较并交换）操作实现的。

在Java中可以通过锁和循环CAS 的方式来实现原子操作，JVM中的 CAS 操作正是利用了处理器提供的 CMPXCHG 指令实现的。CAS 实现原子操作的三大问题：

1. ABA 问题。
2. 循环时间开销大。
3. 只能保证一个共享变量的原子操作。

**悲观锁**，每次读写数据时都认为其它线程会修改数据，每次读写都会加锁。Java中的悲观锁大部分基于AQS（AbstractQueuedSynchronizer，抽象队列同步器）架构实现，该框架下的锁会先尝试以 CAS 乐观锁去获取锁，如果获取不到，则会转为悲观锁。

**自旋锁**，不需要切换线程状态进入阻塞、等待、挂起状态，只需要等一等（自旋）即可。

- 优点：减少了 CPU 上下文切换，对竞争不激烈的线程性能大幅提升。
- 缺点：在持有锁的线程占用锁的时间过长或锁的竞争过于激烈时，会引起 CPU 的浪费。
- JDK1.5 的自旋周期为固定的时间，JDK1.6 引入了适应性自旋锁。

**Lock 接口**，ReentrantLock 可重入锁。

**synchronized**，关键字，独占式悲观锁。JDK1.6 做了优化，引入了适应自旋、锁消除、锁粗化、偏向锁、轻量级锁等以提高锁的效率。默认开启了偏向锁和轻量级锁，可以通过 -XX:UseBiasedLocking 禁用偏向锁。

**重量级锁**，基于操作系统的互斥量（Mutex Lock）实现的，会导致用户态与内核态之间切换，开销大。

**轻量级锁**，相对于重量级锁而言，没有多线程竞争的前提下，减少重量级锁的使用。如果同一时刻有多个线程访问同一个锁，会膨胀为重量级锁。

**偏向锁**，针对同一个锁被同一个线程多次获得的情况，在某个线程获取某个锁之后，消除这个线程锁重入的开销。

无锁 -> 偏向锁 -> 轻量级锁 -> 重量级锁，Java 中锁只单向升级，不会降级。

**可重入锁**，也叫递归锁，指同一线程中，外层函数获得锁后，内层函数仍然可以继续获得该锁。

**公平锁**，优先将锁分配给排队时间最长的线程。

**非公平锁**，不考虑排队等待的情况，直接尝试获取锁，获取不到时再排到队尾等待。

**读写锁**，支持并发读，不支持并发写。

**共享锁**，允许多个线程同时获取该锁。

**独占锁**，也叫互斥锁，每次只允许一个线程持有该锁。

**分段锁**，并非一种实际的锁，而是一种设计思想，每个分段单独加锁，以提高并发效率。

**死锁**，多个线程同时被阻塞，互相等待对方释放锁资源，就会出现死锁。

避免死锁的几个常见方法：

- 避免一个线程同时获得多个锁。
- 避免一个线程在锁内同时占用多个资源，尽量保证每个锁只占用一个资源。
- 尝试使用定时锁，使用lock.tryLock(timeoiut)来替代使用内部锁机制。
- 对于数据库锁，加锁和解锁必须在一个数据连接里，否则会出现解锁失败的情况。

用锁的最佳实践：

Doug Lea《Java 并发编程：设计原则与模式》一书中，推荐的三个用锁的最佳实践，它们分别是：

- 永远只在更新对象的成员变量时加锁。
- 永远只在访问可变的成员变量时加锁。
- 永远不在调用其他对象的方法时加锁。

### Java并发包（JUC）

java.util.concurrent.*，JDK1.5 引入。

#### 锁机制类 

Locks : Lock, Condition, ReentrantLock, ReadWriteLock,LockSupport

#### 原子操作类

Atomic : AtomicInteger, AtomicLong, LongAdder(分段思想改进)

#### 线程池相关类

Executor : Future, Callable, Executor, ExecutorService

1. Excutor: 执行者，顶层接口
2. ExcutorService: 接口 API
3. ThreadFactory: 线程工厂
4. ThreadPoolExecutor: 线程池实现类，有7个参数
   - corePoolSize - 即使空闲时仍保留在池中的线程数，除非设置 allowCoreThreadTimeOut
   - maximumPoolSize - 池中允许的最大线程数。
   - keepAliveTime - 当线程数大于 corePoolSize 时，多余的空闲线程在终止前等待新任务的最大时间。
   - unit - keepAliveTime参数的时间单位。
   - workQueue - 用于在执行任务之前使用的队列。 这个队列将仅保存execute方法提交的Runnable任务。
   - threadFactory - 执行程序创建新线程时使用的工厂。
   - handler - 执行被阻止时使用的处理程序，因为达到线程限制和队列容量。
     - ThreadPoolExecutor.AbortPolicy: 丢弃任务并抛出 RejectedExecutionException异常。
     - ThreadPoolExecutor.DiscardPolicy：丢弃任务，但是不抛出异常。
     - ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列最前面的任务，然后重新提交被拒绝的任务。
     - ThreadPoolExecutor.CallerRunsPolicy：由调用线程（提交任务的线程）处理该任务。
5. Excutors: 工具类，创建线程

创建固定线程池的经验

假设核心数为 N，如果是 CPU 密集型应用，则线程池大小设置为 N 或 N+1，如果是 IO 密集型应用，则线程池大小设置为 2N 或 2N+2。

#### 线程工具类 

CountDownLatch, CyclicBarrier, Semaphore

#### 并发集合类

CopyOnWriteArrayList, ConcurrentMap

#### CompletableFuture

Future/FutureTask 单个线程/任务的执行结果。

CompletableFuture 异步，回调，组合。

#### AQS

AQS（AbstractQueuedSynchronizer）抽象队列同步器，通过维护一个共享资源状态（volatile int state）和一个先进先出（FIFO）的线程等待队列来实现多线程访问共享资源的同步框架。

- 独占式：只有一个线程能执行，ReentractLock。
- 共享式：多个线程可同时执行，Semaphore、CountDownLatch。

CyclicBarrier 部封装了 ReentractLock/Condition 操作。

#### 阻塞队列

第4周作业：https://github.com/LiBaoshe/JavaHomeWork/tree/master/Week04

### ThreadLocal



## 四、Spring 和 ORM 等框架

Spring 是一个企业级 J2EE 应用开发一站式解决方案，Spring 的特性包括轻量、控制反转（IOC）、面向容器、面向切面（AOP）和框架灵活。

### Spring XML

自动化XML配置工具：XmlBeans -> Spring-xbean, 还有 XStream

解析XML的工具有哪些，都有什么特点？

DOM4J，STAX 或 SAX，详情：https://github.com/LiBaoshe/JavaHomeWork/blob/master/Week05/Work03.md

### Spring IOC

原理：xml 解析、工厂模式、反射机制。

#### bean 作用域

5 种作用域：singleton（单例）、prototype（原型）、request（请求级别）、session（会话级别）和 global session（全局会话）。

#### bean 生命周期

构造器实例化、*Aware 接口方法、BeanPostProcessor 前置处理、初始化、BeanPostProcessor 后置处理、使用、销毁。

### Spring AOP

原理：代理模式，JDK动态代理，cglib 动态代理。

应用场景：日志、事务、安全认证等。

5 种通知类型：前置通知、后置通知、成功通知、异常通知、环绕通知。

### Spring 事务

事务特性：原子性、一致性、隔离性、持久性。

隔离级别：读未提交、读已提交、可重复读、串行化。

@Transactional，可以在类、方法上  
propagation：事务传播行为 7个  
ioslation：事务隔离级别 4个  
timeout：超时时间，默认 -1，表示使用数据库默认的超时时间  
readOnly：是否只读  
rollbackFor：出现哪些异常回滚  
rollbackForClassName  
noRollbackFor：出现哪些异常不会滚  
noRollbackForClassName   

一口气说出6种，@Transactional注解的失效场景https://blog.csdn.net/wuzhiwei549/article/details/106015994/

### Spring JMS

Messaging 与 JMS

### ORM

ORM（Object-Relational Mapping） 表示对象关系映射。

半自动化 ORM - MyBatis，全自动化 ORM - Hibernate。

#### MyBatis 缓存

- 一级缓存，SqlSession 级缓存，两次查询中间出现 commit 操作时失效。
- 二级缓存，跨 SqlSession 的缓存，即 Mapper 级别的缓存，通过 CacheExecutor 实现，需要配置。
- 第三方缓存，Redis、es 等。

### Spring Boot

约定大于配置。

Spring Boot 是 Spring 的一套快速配置脚手架，关注于自动配置，开箱即用。

## 五、MySQL 数据库和 SQL

常见关系数据库

- 开源：MySQL、PostgreSQL
- 商业：Oracle，DB2，SQL Server

### 数据库范式

- 第一范式（1NF）：每一个属性只包含原子项（每一列不可再分）。
- 第二范式（2NF）：在 1N F的基础上消除非主属性对码的部分函数依赖，即主键唯一约束。
- 第三范式（3NF）：在 2NF 的基础上消除非主属性对码的传递函数依赖，即表中每列都和主键相关。
- BC范式（BCNF）：Boyce-Codd Normal Form（巴斯-科德范式），3NF 的基础上消除主属性对于码的部分与传递函数依赖。
- 第四范式（4NF）：消除非平凡的多值依赖。
- 第五范式（5NF）：消除一些不合适的连接依赖。

### SQL 语言

- 数据查询语言（DQL: Data Query Language），SELECT，WHERE，ORDER BY，GROUP BY，HAVING。
- 数据操作语言（DML：Data Manipulation Language），INSERT、UPDATE、DELETE。
- 事务控制语言（TCL），COMMIT（提交）命令、SAVEPOINT（保存点）命令、ROLLBACK（回滚）。
- 数据控制语言（DCL），通过 GRANT 或 REVOKE 实现权限控制。
- 数据定义语言（DDL），CREATE，ALTER，DROP。
- 指针控制语言（CCL），DECLARE CURSOR，FETCH INTO 和 UPDATE WHERE CURRENT。

### MySQL 存储

innodb_file_per_table=1 为使用独占表空间，在每个innodb表数据目录下都会有一个.ibd文件，用来存放对应表的数据和索引。

innodb_file_per_table=0 为使用共享表空间，所有表数据和索引存放在一个存储空间中：ibdata1。缺点：增删数据库的时候，ibdata1文件不会自动收缩。

InnoDB 使用 B+ 树实现聚集索引。

一般单表数据不超过2000万。

B+ 树：https://blog.csdn.net/jiang_wang01/article/details/113739230

### MySQL 优化

#### 配置优化

连接请求的变量，max_connections、back_log、wait_timeout和interative_timeout。

缓冲区变量，key_buffer_size、query_cache_size（查询缓存简称 QC)、max_connect_errors 等。

配置 Innodb 的几个变量，innodb_buffer_pool_size=128M、innodb_flush_log_at_trx_commit 等。

#### 设计优化

#### SQL 优化

**存储引擎选择**

| InnoDB       | ToKuDB                                 |
| ------------ | -------------------------------------- |
| 聚焦索引     | 高压缩比，尤其适用于压缩和归档（1:12） |
| 锁粒度是行锁 | 在线添加索引，不影响读写操作           |
| 支持事务     | 支持完整的ACID特性和事务机制           |

没有其他特别因素就用 InnoDB，归档库用 ToKuDB。

**隐式转换**

在 mysql 中，当条件左右两侧类型不匹配的时候会发生隐式转换，可能导致查询无法使用索引或者 sql 执行结果错误。

当把字符串转为数字的时候，其实是从左边开始处理的。如果字符串的第一个字符就是非数字的字符，那么转换为数字就是0，如果字符串中存在非数字，那么转换为的数字就是开头的那些数字对应的值。

**定位问题的方法**：

- 慢查询日志
- 看应用和运维的监控

**索引**

Hash，B-Tree，B+Tree

B+tree是通过二叉查找树，再由平衡二叉树，B树演化而来。

原文连接：https://www.cnblogs.com/zhuwenjoyce/p/14877585.html

- 二叉树：每个结点只存储一个关键字，等于则命中，小于走左结点，大于走右结点；
- B-树：多路搜索树，每个结点存储M/2到M个关键字，非叶子结点存储指向关键字范围的子结点；所有关键字在整颗树中出现，且只出现一次，非叶子结点可以命中；
- B+树：在B-树基础上，为叶子结点增加链表指针，所有关键字都在叶子结点中出现，非叶子结点作为叶子结点的索引；B+树总是到叶子结点才命中；
- B*树：在B+树基础上，为非叶子结点也增加链表指针，将结点的最低利用率从1/2提高到2/3；

为什么不适用 hash index？1. 区间值难找。2. 排序难。

为什么 B+Tree 更适合做索引？B+Tree所有索引数据都在叶子节点上，并且增加了顺序访问指针，每个叶子节点都有指向相邻叶子节点的指针。B+Tree在查找单条记录的速度比不上Hash索引，但是因为更适合排序等操作，所以它更受欢迎。

为什么主键长度不能过大？

- MyISAM的索引与数据分开存储，索引叶子存储指针，主键索引与普通索引无太大区别；
- InnoDB的聚集索引和数据行统一存储，聚集索引存储数据行本身，普通索引存储主键；
- InnoDB不建议使用太长字段作为PK（此时可以加入一个自增键PK），MyISAM则无所谓；

聚集索引和二级索引

索引冗余，长的包括短的，形成冗余；有为以约束的，组合冗余。

修改表结构的危害：索引重建、锁表、抢占资源、主从延时。

大批量写入优化：PreparedStatement 减少 SQL 解析，Multiple Values/Add Batch 减少交互，Load Data，直接导入，索引和约束问题。

数据的范围更新，注意 GAP Lock 的问题，导致锁范围扩大。

模糊查询

like 的问题，只有当这个作为模糊查询的条件字段以及所想要查询出来的数据字段都在索引列上时，才能真正使用索引，否则，索引失效全表扫描。

前缀匹配，否则不走索引，最左前缀匹配原则，是非常重要的原则， mysql 会一直向右匹配直到遇到范围查询(>、<、 between 、 like)就停止匹配，比如 a = 1 and b = 2 and c > 3 and d = 4 如果建立(a,b,c,d)顺序的索引， d 是用不到索引的。

全文检索，solr/ES。

连接查询，避免笛卡尔积。

索引失效的情况：

- NULL，not，not in，函数等；
- 减少使用 or，可以用 union（注意 union all 的区别），以及前面提到的 like；
- 大数据量下，放弃所有条件组合都走索引的幻想，出门左拐”全文检索“。
- 必要时可以使用 force index 来强制查询走某个索引。

乐观锁与悲观锁

### MySQL 事务

事务可靠性模型 ACID：

- Atomicity: 原子性，一次事务中的操作要么全部成功，要么全部失败。
- Consistency: 一致性，跨表、跨行、跨事务，数据库始终保持一致状态。
- Isolation: 隔离性，可见性，保护事务不会互相干扰，包含4种隔离级别。
  - 读未提交: READ UNCOMMITTED，存在脏读、幻读、不可重复读。
  - 读已提交: READ COMMITTED，存在幻读、不可重复读。
  - 可重复读: REPEATABLE READ，存在幻读。
  - 可串行化: SERIALIZABLE，最严格的级别，事务串行执行，资源消耗最大。
- Durability: 持久性，事务提交成功后，不会丢数据。如电源故障，系统崩溃。

表级锁、行级锁、死锁。

undo log: 撤消日志

redo log: 重做日志

MVCC 多版本并发控制，提供并发访问数据库时，对事务内读取的到的内存做处理，用来避免写操作堵塞读操作的并发问题。https://www.cnblogs.com/myseries/p/10930910.html，  

MVCC 原理 https://zhuanlan.zhihu.com/p/147372839 

## 六、分库分表

### 主从复制（读写分离）

主库写 binlog，存库 relay log。

### 主从切换（高可用）

主从手动切换，如果主节点挂掉，将某个从改成主；可能数据不一致。需要人工干预。代码和配置的侵入性。

用 LVS+Keepalived 实现多个节点的探活+请求路由。配置 VIP 或 DNS 实现配置不变更。手工处理主从切换。大量的配置和脚本定义

MHA（Master High Availability），需要配置 SSH 信息，至少3台。

MGR *，如果主节点挂掉，将自动选择某个从改成主；无需人工干预，基于组复制，保证数据一致性。外部获得状态变更需要读取数据库。外部需要使用 LVS/VIP 配置。特点：高一致性、高容错性、高扩展性、高灵活性。场景：弹性复制、高可用分片。

MySQL Cluster，完整的数据库层高可用解决方案。

Orchestrator，如果主节点挂掉，将某个从改成主；一款 MySQL 高可用和复制拓扑管理工具，支持复制拓扑结构的调整，自动故障转移和手动主从切换等。能直接在 UI 界面拖拽改变主从关系。

### 垂直分库分表

业务拆分。

### 水平分库分表

数据分片。

### 数据库中间件

ShardingSphere

### 数据迁移

全量，全量 + 增量，binlog + 全量 + 增量，

迁移工具 ShardingSphere-scaling 具有 UI 界面，可视化配置。

### 分布式事务

分布式条件下，多个节点操作的整体事务一致性。

CAP 原则又称 CAP 定理，指的是在一个分布式系统中，一致性（Consistency）、可用性（Availability）和分区容错性（Partition tolerance）三者不可兼得。

两阶段提交协议：1. Prepare（准备阶段），2. Commit（提交阶段）。

三阶段提交协议：CanCommit 阶段、PreCommit 阶段、DoCommit 阶段。

强一致：XA，应用程序 AP（定义事务的开始和结束），资源管理器 RM（数据库、文件系统等），事务管理器 TM（分配事务唯一标识，监控事务，提交、回滚）。

弱一致：不用事务，业务侧补偿冲正。所谓的柔性事务，使用一套事务框架保证最终一致的事务。

主流支持 XA 的框架，比较推荐 Atomikos 和 narayana

|          | Atomikos               | narayana               | seata                        |
| -------- | ---------------------- | ---------------------- | ---------------------------- |
| TM       | 去中心化设计，性能较高 | 去中心化设计，性能较高 | 中心化设计，性能较差，BUG 多 |
| 日志存储 | 只支持文件             | 支持文件，数据库       | 支持文件，数据库             |
| 扩展性   | 较好                   | 一般                   | 一般                         |
| 事务恢复 | 只支持单机事务恢复     | 支持集群模式恢复       | 问题很多，未能正确恢复       |
| XA实现   | 标准的XA实现           | 标准的XA实现           | 非标准的XA实现               |

柔性事务，基于 CAP 理论及 BASE 理论，阿里巴巴提出了柔性事务的概念，包括基本可用（Basically Available）、柔性状态（Soft State）、最终一致（Eventually Sonsistent）三个原则。我们通常所说的柔性事务分为：两阶段型、补偿型、异步确保型、最大努力通知型。

本地事务 -> XA(2PC) -> BASE

|          | 本地事务         | 两（三）阶段事务 | 柔性事务        |
| -------- | ---------------- | ---------------- | --------------- |
| 业务构造 | 无               | 无               | 实现相关接口    |
| 一致性   | 不支持           | 支持             | 最终一致        |
| 隔离性   | 不支持           | 支持             | 业务方保证      |
| 并发性能 | 无影响           | 严重衰退         | 略微衰退        |
| 适合场景 | 业务方处理不一致 | 短事务 & 低并发  | 长事务 & 高并发 |

TCC 通过手动补偿处理，Try， Confirm， Cancel，TCC 不依赖 RM 对分布式事务的支持，而是通过对业务逻辑的分解来实现分布式事务，不同于 AT 的是就是需要自行定义各个阶段的逻辑，对业务有侵入。

TCC 需要注意的几个问题：1、允许空回滚，2、防悬挂控制，3、幂等设计。

Saga 模式没有 try 阶段，直接提交事务。复杂情况下，对回滚操作的设计要求较高。

AT 通过自动补偿处理 Auto Commit，AT 模式就是两阶段提交，自动生成反向 SQL。

Seata 是阿里集团和蚂蚁金服联合打造的分布式事务框架。 其 AT 事务的目标是在微服务架构下，提供增量的事务 ACID 语意，让开发者像使用本地事务一样，使用分布式事务，核心理念同Apache ShardingSphere 一脉相承。

Seata AT 事务模型包含TM (事务管理器)，RM (资源管理器) 和 TC (事务协调器)。 TC 是一个独立部署的服务，TM 和 RM 以 jar 包的方式同业务应用一同部署，它们同 TC 建立长连接，在整个事务生命周期内，保持远程通信。TM 是全局事务的发起方，负责全局事务的开启，提交和回滚。RM 是全局事务的参与者，负责分支事务的执行结果上报，并且通过 TC 的协调进行分支事务的提交和回滚。

Seata 管理的分布式事务的典型生命周期：

1. TM 要求 TC 开始一个全新的全局事务。
2. TC 生成一个代表该全局事务的 XID。
3. XID 贯穿于微服务的整个调用链。
4. TM 要求 TC 提交或回滚 XID 对应全局事务。
5. TC 驱动 XID 对应的全局事务下的所有分支事务完成提交或回滚。

Hmily 是一个高性能分布式事务框架，开源于2017年，目前有2800个 Star，基于 TCC 原理实现，使用 Java 语言开发（JDK1.8+），天然支持 Dubbo、SpringCloud、Motan 等微服务框架的分布式事务。

ShardingSphere 对分布式事务的支持，整合现有的成熟事务方案，为本地事务、两阶段事务和柔性事务提供统一的分布式事务接口，并弥补当前方案的不足，提供一站式的分布式事务解决方案是 Apache ShardingSphere 分布式事务模块的主要设计目标。

ShardingSphere 支持 Seata 的柔性事务。

## 七、RPC 和微服务

RPC 是远程过程调用（Remote Procedure Call）的缩写形式。简单来说，就是“像调用本地方法一样调用远程方法”。核心是代理机制、网络通信。

Apache Dubbo 是一款高性能、轻量级的开源 Java 服务框架，六大核心能力：面向接口代理的高性能 RPC 调用，智能负载均衡，服务自动注册和发现，高度可扩展能力，运行期流量调度，可视化的服务治理与运维。

整体架构

1. config 配置层：对外配置接口，以 ServiceConfig, ReferenceConfig 为中心，可以直接初始化配置类，
   也可以通过 spring 解析配置生成配置类。
2. proxy 服务代理层：服务接口透明代理，生成服务的客户端 Stub 和服务器端 Skeleton, 以 ServiceProxy 为中心，扩展接口为 ProxyFactory
3. registry 注册中心层：封装服务地址的注册与发现，以服务 URL 为中心，扩展接口为 RegistryFactory, Registry, RegistryService
4. cluster 路由层：封装多个提供者的路由及负载均衡，并桥接注册中心，以 Invoker 为中心，扩展接口为Cluster，Directory，Router，LoadBalance
5. monitor 监控层：RPC 调用次数和调用时间监控，以 Statistics 为中心，扩展接口为 MonitorFactory, Monitor, MonitorService
6. protocol 远程调用层：封装 RPC 调用，以 Invocation，Result 为中心，扩展接口为 Protocol，Invoker，Exporter
7. exchange 信息交换层：封装请求响应模式，同步转异步，以 Request，Response 为中心，扩展接口为Exchanger，ExchangeChannel，ExchangeClient，ExchangeServer
8. transport 网络传输层：抽象 mina 和 netty 为统一接口，以 Message 为中心，扩展接口为 Channel，Transporter，Client，Server，Codec
9. serialize 数据序列化层：可复用的一些工具，扩展接口为 Serialization，ObjectInput， ObjectOutput，ThreadPool

SPI 的应用

ServiceLoader 机制，META-INF/接口全限定名，文件内容为实现类。

其他两个类似的机制：Callback 与 EventBus

Dubbo 的 SPI 扩展，最关键的 SPI：Protocol，xxx=com.alibaba.xxx.XxxProtocol，

启动时装配，并缓存到 ExtensionLoader 中。

服务如何暴露

服务如何引用

集群与路由

泛化引用

隐式传参

Mock

Dubbo 应用场景：分布式服务化改造、开放平台、直接作为前端使用的后端（BFF）、通过服务化建设中台

分布式服务化与 SOA/ESB 的区别

配置/注册/元数据中心

服务的注册与发现

服务的集群与路由

服务的过滤与流控

微服务架构发展历程，单体架构、垂直架构、SOA 架构、微服务架构。响应式微服务、服务网格、数据网格、云原生、单元化架构。

微服务架构应用场景

微服务架构最佳实践

Spring Cloud 技术体系

微服务相关技术与工具

## 八、分布式缓存

本地缓存

远程缓存

缓存常见问题，穿透、击穿、雪崩。

Redis 5种基本数据结构（字符串 string，散列 hash，列表 list，集合 set，有序集合 zset），3种高级数据结构（Bitmaps，Hyperlogs，GEO）

Redis 六大使用场景：

1. 业务数据缓存：通用数据缓存，实时热数据，会话缓存，token 等。
2. 业务数据处理：非严格一致性要求的数据：评论，点击等，业务数据去重：订单处理的幂等校验等，业务数据排序：排名，排行榜等。
3. 全局一致计数：全局流控计数、秒杀的库存计算、抢红包、全局 ID 生成。
4. 高效统计计数：id 去重，记录访问 ip 等全局 bitmap 操作，UV、PV 等访问量==>非严格一致性要求。
5. 发布订阅与 Stream：Pub-Sub 模拟队列，Redis Stream 是 Redis 5.0 版本新增加的数据结构。
6. 分布式锁：获取锁--单个原子性操作，释放锁--lua脚本-保证原子性+单线程，从而具有事务性。

Redis 的 Java 客户端：

Jedis，官方客户端，类似于 JDBC，可以看做是对 redis 命令的包装。基于 BIO，线程不安全，需要配置连接池管理连接。

Lettuce，目前主流推荐的驱动，基于 Netty NIO，API 线程安全。

Redisson，基于 Netty NIO，API 线程安全。亮点：大量丰富的分布式功能特性，比如 JUC 的线程安全集合和工具的分布式版本，分布式的基本数据类型和锁等。分布式锁，RLock ==> 能实现跨节点的锁状态，分布式的 Map，RMap ==> 全集群共享的，一个机器改了，其他都会自动同步。

Redis 事务

Redis Lua ~ open resty = nginx + lua jit

Redis 管道技术（pipeline）

Redis 数据备份与恢复--RDB ~ frm

Redis 数据备份与恢复--AOF ~ binlog

Redis 性能优化

Redis 集群，主从复制

Redis 高可用，主从切换，sentinel 基于 raft 协议

Redis Cluster：走向分片 ~ 全自动分库分表，Redis Cluster 通过一致性 hash 的方式，将数据分散到多个服务器节点：先设计 16384 个哈希槽，分配到多台 redis-server。当需要在 Redis Cluster中存取一个 key 时，Redis 客户端先对 key 使 用 crc16 算法计算一个数值，然后对 16384 取模，这样每个 key 都会对应一个编号在 0-16383 之间的哈希槽，然后在 此槽对应的节点上操作。

内存网格 - Hazelcast

## 九、分布式消息队列

MQ 的四大作用：

- 异步通信：异步通信，减少线程等待，特别是处理批量等大事务、耗时操作。
- 系统解耦：系统不直接调用，降低依赖，特别是不在线也能保持通信最终完成。
- 削峰平谷：压力大的时候，缓冲部分请求消息，类似于背压处理。
- 可靠通信：提供多种消息模式、服务质量、顺序保障等。

常见的有两种消息模式：

- 点对点：PTP，Point-To-Point，对应于 Queue。
- 发布订阅：PubSub，Publish-Subscribe，对应于 Topic。

开源消息中间件/消息队列：

- ActiveMQ/RabbitMQ
- Kafka/RocketMQ
- Apache Pulsar

EIP/Camel

