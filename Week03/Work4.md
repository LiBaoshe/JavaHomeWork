### 4.（选做）实现路由。

实现了一个简单的RoundRibbon 轮询访问路由策略，调试过程中发现一次请求中 HttpInboundHandler 中的 channelRead 方法会执行两次，所以轮询需要每两次增加1。

实现代码：

```java
/**
 * RoundRibbon 轮询访问路由策略
 */
public class RoundRibbonHttpEndpointRouter implements HttpEndpointRouter {

    private static volatile AtomicInteger count = new AtomicInteger(0);

    @Override
    public String route(List<String> urls) {
        // 因为一次请求中 HttpInboundHandler 中的 channelRead 方法会执行两次，
        // 所以这里的 index 每调用两次再加一
        int index = count.getAndIncrement() / 2;
        if(index >= urls.size()){
            index = 0;
            count.set(1);
        }
        return urls.get(index);
    }

}
```

