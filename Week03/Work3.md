### 3.（必做）实现过滤器。

Request 过滤器

```java
public class HeaderHttpRequestFilter implements HttpRequestFilter{

    @Override
    public void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        fullRequest.headers().set("gateway-name", GATEWAY_NAME);
        fullRequest.headers().set("gateway-version", GATEWAY_VERSION);
    }
}
```

Response 过滤器

```java
public class HeaderResponseFilter implements HttpResponseFilter{

    @Override
    public void filter(FullHttpResponse response) {
        response.headers().set("gateway-name", GATEWAY_NAME);
        response.headers().set("gateway-version", GATEWAY_VERSION);
    }
}
```

