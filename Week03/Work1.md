### 1.（必做）整合你上次作业的 httpclient/okhttp。

完整demo地址：https://github.com/LiBaoshe/netty-gateway-demo

demo参考了 老师的例子完成，请求后端服务有多种实现方式（httpclient、okhttp、netty4），所以先在抽象类 OutboundHandler 中定义了一些公共操作：

```java
/**
 * 访问后端服务抽象类
 */
public abstract class OutboundHandler {

    // 定义默认 Response 过滤器
    protected HttpResponseFilter filter = new HeaderResponseFilter();
    // 定义默认路由规则
//    protected HttpEndpointRouter router = new RandomHttpEndpointRouter();
    protected HttpEndpointRouter router = new RoundRibbonHttpEndpointRouter();

    /**
     * 访问后端服务，由子类实现
     * @param fullRequest
     * @param ctx
     * @param filter
     */
    public abstract void handle(FullHttpRequest fullRequest, ChannelHandlerContext ctx, HttpRequestFilter filter);

    /**
     * 返回处理后的 url 地址集合
     * @param backends
     * @return
     */
    protected List<String> getBackendUrls(List<String> backends){
        return backends.stream().map(this::formatUrl).collect(Collectors.toList());
    }

    /**
     * 处理 url 地址格式
     * @param backend
     * @return
     */
    public String formatUrl(String backend){
        return backend.endsWith("/") ? backend.substring(0, backend.length() - 1) : backend;
    }

    /**
     * 发生异常时关闭通道
     * @param ctx
     * @param cause
     */
    protected void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
```

使用 OkHttp 访问后端服务实现：

```java
/**
 * 通过 OkHttp 访问后端服务
 */
public class OkhttpOutboundHandler extends OutboundHandler {

    final OkHttpClient okHttpClient;
    final private ExecutorService proxyService;
    final private List<String> backendUrls;

    public OkhttpOutboundHandler(List<String> backends) {

        this.backendUrls = super.getBackendUrls(backends);

        int cores = Runtime.getRuntime().availableProcessors();
        long keepAliveTime = 1000;
        int queueSize = 2048;
        RejectedExecutionHandler handler = new  ThreadPoolExecutor.CallerRunsPolicy();
        proxyService = new ThreadPoolExecutor(
                cores,
                cores,
                keepAliveTime,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueSize),
                new NamedThreadFactory("proxyService"),
                handler
        );

        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(5,TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void handle(FullHttpRequest fullRequest, ChannelHandlerContext ctx, HttpRequestFilter filter) {
        String backendUrl = router.route(this.backendUrls);
        final String url = backendUrl + fullRequest.uri();
        filter.filter(fullRequest, ctx);
        proxyService.submit(() -> fetchGet(fullRequest, ctx, url));
    }

    private void fetchGet(final FullHttpRequest inbound, final ChannelHandlerContext ctx, final String url) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("connection", "keep-alive")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponse(inbound, ctx, response);
            }

        });
    }

    private void handleResponse(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx, Response endpointResponse) {
        FullHttpResponse response = null;
        try {
            byte[] body = endpointResponse.body().bytes();
            response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(body));
            response.headers().set("Content-Type", "application/json");
            response.headers().set("Content-Length", endpointResponse.body().contentLength());
            System.out.println(endpointResponse.body().contentLength());
            filter.filter(response);
        } catch (IOException e) {
            e.printStackTrace();
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NO_CONTENT);
            exceptionCaught(ctx, e);
        } finally {
            if(fullRequest != null){
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            }
            ctx.flush();
        }
    }
}
```

