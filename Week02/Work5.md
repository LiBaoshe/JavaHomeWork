### 单线程的socket程序 sb 压力测试  
![1636682253(1)](https://user-images.githubusercontent.com/18158758/141395653-46fea474-53a6-40ca-856d-aa8af9ac241c.png)  

### 每个请求一个线程 sb 压力测试  
![1636682495(1)](https://user-images.githubusercontent.com/18158758/141396019-40efae58-7665-4816-9be9-82d57b4236cd.png)  

### 固定大小的线程池处理请求 sb 压力测试  
![1636682656(1)](https://user-images.githubusercontent.com/18158758/141396271-92e7ed30-277c-4a15-a6ea-3bbf92ae586b.png)  

### Netty sb 压力测试
![1636683123(1)](https://user-images.githubusercontent.com/18158758/141396949-f9d2842d-d8e2-49da-82c1-51e15ab5310d.png)  

### 测试结果统计  
![a9f7f3cb7c55aca05446b1abb360c71](https://user-images.githubusercontent.com/18158758/141647602-7dcc9127-afee-4e7b-bff1-2d6c366c0c68.png)  

在并发数 -c 20 执行时间 -N 60 的 sb 压力测试下，netty 的各项指标都最优：   
RPS 排序为：Netty > 固定大小的线程池 socket 程序 > 每个线程一个请求的 socket 程序 > 单线程的 socket 程序；  
最大等待时间：固定线程池 > 每个请求一个线程 > 单线程 > Netty；   
最小等待时间都为0；   
平均等待时间：单线程 > 每个请求一个线程 > 固定大小的线程池 > Netty
