## 垃圾收集器是垃圾回收算法（标记-清除算法、复制算法、标记-整理算法）的具体实现。

### 串行垃圾收集器 Serial + SerialOld
开启参数：-XX:+UseSerialGC  
日志显示：DefNew + Tenured

### 并行垃圾收集器 ParNew（Serial 的多线程版）
开启参数：-XX:+UseParNewGC  
日志显示：ParNew + Tenured  
Java HotSpot(TM) 64-Bit Server VM warning: Using the ParNew young collector with the Serial old collector is deprecated and will likely be removed in a future release  
Java HotSpot(TM) 64位服务器虚拟机警告:使用ParNew年轻收集器与串行旧收集器已被弃用，可能会在未来的版本中被移除  

### 并行垃圾收集器 Parallel Scavenge + Parallel Old （JKD1.8 默认方式）
开启参数：-XX:+UseParallelGC 或 -XX:+UseParallelOldGC  
日志显示：PSYoungGen + ParOldGen  

### CMS（Concurrent Mark Sweep）垃圾收集器 ParNew + CMS(Serial Old)
开启参数：-XX:+UseConcMarkSweepGC  
日志显示：  

### G1（JDK9 默认方式）
开启参数：-XX:+UseG1GC  
日志显示：CMS-concurrent-xxx  
