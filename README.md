# 极客时间 Java进阶训练营 毕业总结

分别用 100 个字以上的一段话，加上一幅图（架构图或脑图），总结自己对下列技术的关键点思考和经验认识。

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

GCEasy 官网地址：https://gceasy.io/，GCeasy是一款在线的GC日志分析器，可以通过GC日志分析进行内存泄露检测、GC暂停原因分析、JVM配置建议优化等功能，而且是可以免费使用的（有一些服务是收费的）。

离线版的GCViewer，需要jdk1.8才可以使用，Github地址为https://github.com/chewiebug/GCViewer，下载下来之后执行 mvn clean install -Dmaven.test.skip=true 命令进行编译，编译完成后在target目录下会看到jar包，双击打开即可。

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



## 四、Spring 和 ORM 等框架



## 五、MySQL 数据库和 SQL



## 六、分库分表



## 七、RPC 和微服务



## 八、分布式缓存



## 九、分布式消息队列

