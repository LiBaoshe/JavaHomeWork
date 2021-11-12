### 单线程的socket程序 sb 压力测试  
![1636682253(1)](https://user-images.githubusercontent.com/18158758/141395653-46fea474-53a6-40ca-856d-aa8af9ac241c.png)  

### 每个请求一个线程 sb 压力测试  
![1636682495(1)](https://user-images.githubusercontent.com/18158758/141396019-40efae58-7665-4816-9be9-82d57b4236cd.png)  

### 固定大小的线程池处理请求 sb 压力测试  
![1636682656(1)](https://user-images.githubusercontent.com/18158758/141396271-92e7ed30-277c-4a15-a6ea-3bbf92ae586b.png)  

### Netty sb 压力测试
![1636683123(1)](https://user-images.githubusercontent.com/18158758/141396949-f9d2842d-d8e2-49da-82c1-51e15ab5310d.png)  

在并发数 -c 20 执行时间 -N 60 的 sb 压力测试下，性能排序为：Netty > 固定大小的线程池 socket 程序 > 每个线程一个请求的 socket 程序 > 单线程的 socket 程序。
