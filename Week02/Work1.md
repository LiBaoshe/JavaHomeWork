## 默认参数
[GC (Allocation Failure) [PSYoungGen: 78512K->22201K(116736K)] 379873K->342485K(466432K), 0.0042620 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[Full GC (Ergonomics) [PSYoungGen: 22201K->0K(116736K)] [ParOldGen: 320284K->291491K(349696K)] 342485K->291491K(466432K), [Metaspace: 3362K->3362K(1056768K)], 0.0286482 secs] [Times: user=0.41 sys=0.00, real=0.03 secs] 
执行结束!共生成对象次数:10051
Heap
 PSYoungGen      total 116736K, used 8261K [0x00000000f5580000, 0x0000000100000000, 0x0000000100000000)
  eden space 58880K, 14% used [0x00000000f5580000,0x00000000f5d917e8,0x00000000f8f00000)
  from space 57856K, 0% used [0x00000000f8f00000,0x00000000f8f00000,0x00000000fc780000)
  to   space 57856K, 0% used [0x00000000fc780000,0x00000000fc780000,0x0000000100000000)
 ParOldGen       total 349696K, used 341340K [0x00000000e0000000, 0x00000000f5580000, 0x00000000f5580000)
  object space 349696K, 97% used [0x00000000e0000000,0x00000000f4d57068,0x00000000f5580000)
 Metaspace       used 3436K, capacity 4500K, committed 4864K, reserved 1056768K
  class space    used 373K, capacity 388K, committed 512K, reserved 1048576K

## -XX:+UseSerialGC -XX:+PrintGCDetails
[GC (Allocation Failure) [DefNew: 139776K->139776K(157248K), 0.0000092 secs][Tenured: 337884K->349307K(349568K), 0.0347790 secs] 477660K->357769K(506816K), [Metaspace: 3500K->3500K(1056768K)], 0.0348273 secs] [Times: user=0.05 sys=0.00, real=0.04 secs] 
[Full GC (Allocation Failure) [Tenured: 349307K->349213K(349568K), 0.0380163 secs] 506316K->368780K(506816K), [Metaspace: 3867K->3867K(1056768K)], 0.0380525 secs] [Times: user=0.03 sys=0.00, real=0.04 secs] 
执行结束!共生成对象次数:11970
Heap
 def new generation   total 157248K, used 24252K [0x00000000e0000000, 0x00000000eaaa0000, 0x00000000eaaa0000)
  eden space 139776K,  17% used [0x00000000e0000000, 0x00000000e17af100, 0x00000000e8880000)
  from space 17472K,   0% used [0x00000000e9990000, 0x00000000e9990000, 0x00000000eaaa0000)
  to   space 17472K,   0% used [0x00000000e8880000, 0x00000000e8880000, 0x00000000e9990000)
 tenured generation   total 349568K, used 349213K [0x00000000eaaa0000, 0x0000000100000000, 0x0000000100000000)
   the space 349568K,  99% used [0x00000000eaaa0000, 0x00000000fffa77c0, 0x00000000fffa7800, 0x0000000100000000)
 Metaspace       used 3873K, capacity 4572K, committed 4864K, reserved 1056768K
  class space    used 429K, capacity 460K, committed 512K, reserved 1048576K

## -XX:+UseParNewGC -XX:+PrintGCDetails
Java HotSpot(TM) 64-Bit Server VM warning: Using the ParNew young collector with the Serial old collector is deprecated and will likely be removed in a future release

[GC (Allocation Failure) [ParNew: 139776K->139776K(157248K), 0.0000106 secs][Tenured: 348364K->349438K(349568K), 0.0386435 secs] 488140K->356860K(506816K), [Metaspace: 3362K->3362K(1056768K)], 0.0386955 secs] [Times: user=0.03 sys=0.00, real=0.04 secs] 
[Full GC (Allocation Failure) [Tenured: 349438K->345789K(349568K), 0.0474081 secs] 506568K->345789K(506816K), [Metaspace: 3362K->3362K(1056768K)], 0.0474471 secs] [Times: user=0.05 sys=0.00, real=0.05 secs] 
执行结束!共生成对象次数:11832
Heap
 par new generation   total 157248K, used 47103K [0x00000000e0000000, 0x00000000eaaa0000, 0x00000000eaaa0000)
  eden space 139776K,  33% used [0x00000000e0000000, 0x00000000e2dfffd0, 0x00000000e8880000)
  from space 17472K,   0% used [0x00000000e8880000, 0x00000000e8880000, 0x00000000e9990000)
  to   space 17472K,   0% used [0x00000000e9990000, 0x00000000e9990000, 0x00000000eaaa0000)
 tenured generation   total 349568K, used 349566K [0x00000000eaaa0000, 0x0000000100000000, 0x0000000100000000)
   the space 349568K,  99% used [0x00000000eaaa0000, 0x00000000fffff9f0, 0x00000000fffffa00, 0x0000000100000000)
 Metaspace       used 3775K, capacity 4540K, committed 4864K, reserved 1056768K
  class space    used 420K, capacity 428K, committed 512K, reserved 1048576K
  
## -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails
[GC (Allocation Failure) [ParNew: 156499K->156499K(157248K), 0.0000141 secs][CMS: 348985K->349319K(349568K), 0.0522025 secs] 505484K->372981K(506816K), [Metaspace: 3362K->3362K(1056768K)], 0.0522606 secs] [Times: user=0.06 sys=0.00, real=0.05 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 349319K(349568K)] 373744K(506816K), 0.0002173 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (CMS Final Remark) [YG occupancy: 45723 K (157248 K)][Rescan (parallel) , 0.0002086 secs][weak refs processing, 0.0000046 secs][class unloading, 0.0001830 secs][scrub symbol table, 0.0003051 secs][scrub string table, 0.0000815 secs][1 CMS-remark: 349319K(349568K)] 395042K(506816K), 0.0008216 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 156947K->156947K(157248K), 0.0000124 secs][CMS: 348455K->349544K(349568K), 0.0455396 secs] 505402K->374914K(506816K), [Metaspace: 3852K->3852K(1056768K)], 0.0455932 secs] [Times: user=0.05 sys=0.00, real=0.04 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 349544K(349568K)] 376823K(506816K), 0.0004273 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
执行结束!共生成对象次数:12202
Heap
 par new generation   total 157248K, used 31348K [0x00000000e0000000, 0x00000000eaaa0000, 0x00000000eaaa0000)
  eden space 139776K,  22% used [0x00000000e0000000, 0x00000000e1e9d278, 0x00000000e8880000)
  from space 17472K,   0% used [0x00000000e8880000, 0x00000000e8880000, 0x00000000e9990000)
  to   space 17472K,   0% used [0x00000000e9990000, 0x00000000e9990000, 0x00000000eaaa0000)
 concurrent mark-sweep generation total 349568K, used 349544K [0x00000000eaaa0000, 0x0000000100000000, 0x0000000100000000)
 Metaspace       used 3867K, capacity 4572K, committed 4864K, reserved 1056768K
  class space    used 428K, capacity 460K, committed 512K, reserved 1048576K

## -XX:+UseG1GC -XX:+PrintGC
[GC pause (G1 Evacuation Pause) (young) 373M->348M(512M), 0.0017900 secs]
[GC pause (G1 Humongous Allocation) (young) (initial-mark) 351M->348M(512M), 0.0009615 secs]
[GC concurrent-root-region-scan-start]
[GC concurrent-root-region-scan-end, 0.0000853 secs]
[GC concurrent-mark-start]
[GC concurrent-mark-end, 0.0011047 secs]
[GC remark, 0.0008660 secs]
[GC cleanup 361M->361M(512M), 0.0005402 secs]
[GC pause (G1 Evacuation Pause) (young) 378M->357M(512M), 0.0016031 secs]
执行结束!共生成对象次数:13402

