## 垃圾收集器是垃圾回收算法（标记-清除算法、复制算法、标记-整理算法）的具体实现。

### 串行垃圾收集器 Serial + SerialOld
开启参数：-XX:+UseSerialGC  
日志显示：DefNew + Tenured  
应用场景：简单高效（与其他收集器的单线程相比），对于限定单个CPU的环境来说，Serial收集器没有线程交互（切换）开销，可以获得最高的单线程收集效率。

### 并行垃圾收集器 ParNew（Serial 的多线程版）
开启参数：-XX:+UseParNewGC  
日志显示：ParNew + Tenured  
Java HotSpot(TM) 64-Bit Server VM warning: Using the ParNew young collector with the Serial old collector is deprecated and will likely be removed in a future release  
Java HotSpot(TM) 64位服务器虚拟机警告:使用ParNew年轻收集器与串行旧收集器已被弃用，可能会在未来的版本中被移除   
应用场景：但在单个CPU环境中，不会比Serail收集器有更好的效果，因为存在线程交互开销。推荐与 CMS 配合工作。

### 并行垃圾收集器 Parallel Scavenge + Parallel Old （JKD1.8 默认方式）
开启参数：-XX:+UseParallelGC 或 -XX:+UseParallelOldGC  
日志显示：PSYoungGen + ParOldGen   
应用场景：高吞吐量为目标，即减少垃圾收集时间，让用户代码获得更长的运行时间；当应用程序运行在具有多个CPU上，对暂停时间没有特别高的要求时，即程序主要在后台进行计算，而不需要与用户进行太多交互。

### CMS（Concurrent Mark Sweep）并发标记清理垃圾收集器 ParNew + CMS(Serial Old)
开启参数：-XX:+UseConcMarkSweepGC  
日志显示：CMS-concurrent-xxx    
应用场景：与用户交互较多的场景；希望系统停顿时间最短，注重服务的响应速度；以给用户带来较好的体验。    
CMS六个过程：1.初始标记（需STW）、2.并发标记、3.并发预清理、4.重新（最终）标记（需STW）、5.并发清除、6.并发重置 

### G1并发垃圾收集器（JDK9 默认方式）
开启参数：-XX:+UseG1GC  
日志显示：与前面的显示都不一样：  
      [Full GC (Allocation Failure)  421M->351M(512M), 0.0360324 secs]  
      [GC pause (G1 Humongous Allocation) (young) (initial-mark) 358M->351M(512M), 0.0009876 secs]  
      [GC concurrent-root-region-scan-start]  
      [GC concurrent-root-region-scan-end, 0.0000779 secs]  
      [GC concurrent-mark-start]  
      [GC concurrent-mark-end, 0.0011469 secs]  
      [GC remark, 0.0009199 secs]  
      [GC cleanup 364M->364M(512M), 0.0005201 secs]  
应用场景：面向服务端应用，针对具有大内存、多处理器的机器；最主要的应用是为需要低GC延迟，并具有大堆的应用程序提供解决方案；  

## 垃圾收集器搭配关系图
![1636603444](https://user-images.githubusercontent.com/18158758/141235039-d7f3740f-9e6c-433e-9d2d-c5466df33527.png)


