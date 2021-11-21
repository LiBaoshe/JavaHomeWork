### 2.（选做）使用 Netty 实现后端 HTTP 访问（代替上一步骤）。

本次实现参考了CSDN的一篇博客（基于Netty4构建HTTP服务----浏览器访问和Netty客户端访问）：https://blog.csdn.net/wangshuang1631/article/details/73251180/

#### 实现思路：

NettyHttpClient 的请求结果在 NettyHttpClientHandler 中获取，定义一个处理消息接口 NettyHttpResp，调用 http 请求的时候传递一个接口 NettyHttpResp 实例到 NettyHttpClientHandler 中获取请求内容。

#### 存在的问题：

请求不稳定，会出现 java.io.IOException：你的主机中的软件中止了一个已建立的连接，目前还没找到具体原因，将发送http请求的操作放到了workerGroup.submit()中后稍有改善，但是还是会出现异常。这是个严重的bug，后续有时间还会继续研究的~

#### 实现过程/代码：

定义接口 NettyHttpResp

```java
/**
 * NettyHttp 返回结果处理接口，NettyHttpClient 的 connect 方法中回调 http 的请求结果
 */
@FunctionalInterface
public interface NettyHttpResp {
    void handle(ChannelHandlerContext ctx, Object msg);
}
```

实现 NettyHttpClientHandler

```java
public class NettyHttpClientHandler extends ChannelInboundHandlerAdapter {

    private final NettyHttpResp nettyHttpResp;

    public NettyHttpClientHandler(NettyHttpResp nettyHttpResp) {
        this.nettyHttpResp = nettyHttpResp;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        // 结果交给使用者处理
        nettyHttpResp.handle(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```

实现NettyHttpClient

```java
/**
 * Netty 发送 http 请求
 */
public class NettyHttpClient {

    public void connect(String url, final NettyHttpResp nettyHttpResp) throws Exception {

        URI uri = URI.create(url);
        String host = uri.getHost();
        int port = uri.getPort();

        System.out.println("使用 NettyHttpClient 访问：" + url);

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                    .option(ChannelOption.SO_SNDBUF, 32 * 1204)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            // 客户端接收到的是httpResponse响应，所以要使用HttpResponseDecoder进行解码
                            ch.pipeline().addLast(new HttpResponseDecoder());
                            // 客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
                            ch.pipeline().addLast(new HttpRequestEncoder());
                            ch.pipeline().addLast(new NettyHttpClientHandler(nettyHttpResp));
                        }
                    });
            ChannelFuture f = bootstrap.connect(host, port).sync();
            workerGroup.submit(() -> {
                String msg = "Are you ok?";
                DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                        HttpVersion.HTTP_1_1, HttpMethod.POST, uri.toASCIIString(),
                        Unpooled.wrappedBuffer(msg.getBytes()));
                // 构建http请求
                request.headers().set(HttpHeaderNames.HOST, host);
                request.headers().set(HttpHeaderNames.CONNECTION,
                        HttpHeaderNames.CONNECTION);
                request.headers().set(HttpHeaderNames.CONTENT_LENGTH,
                        request.content().readableBytes());
                request.headers().set("messageType", "normal");
                request.headers().set("businessType", "testServerState");
                // 发送http请求
                f.channel().write(request);
                f.channel().flush();
            });
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    private static ByteBufToBytes reader;

    public static void main(String[] args) throws Exception {
        // 测试 NettyHttpClient
        NettyHttpClient client = new NettyHttpClient();
        String[] urls = new String[]{"http://127.0.0.1:8801", "http://127.0.0.1:8802", "http://127.0.0.1:8803"};
        for (int i = 0; i < 10; i++) {
            test(client, urls[i % 3]);
        }
    }

    private static void test(NettyHttpClient client, String url) throws Exception {
        client.connect(url, ((ctx, msg) -> {
            if (msg instanceof HttpResponse) {
                HttpResponse response = (HttpResponse) msg;
                System.out.println("CONTENT_TYPE:" + response.headers().get(HttpHeaderNames.CONTENT_TYPE));
                if (HttpUtil.isContentLengthSet(response)) {
                    reader = new ByteBufToBytes(
                            (int) HttpUtil.getContentLength(response));
                }
            }
            if (msg instanceof HttpContent) {
                HttpContent httpContent = (HttpContent) msg;
                ByteBuf content = httpContent.content();
                reader.reading(content);
                content.release();
                if (reader.isEnd()) {
                    String resultStr = new String(reader.readFull());
                    System.out.println("Server said:" + resultStr);
                }
            }
            ctx.close();
        }));
    }
}
```

实现 NettyOutboundHandler 访问后端服务

```java
/**
 * 通过 Netty 访问后端服务
 */
public class NettyOutboundHandler extends OutboundHandler {

    final private ExecutorService proxyService;
    final private List<String> backendUrls;
    final private NettyHttpClient nettyHttpClient;

    public NettyOutboundHandler(List<String> backends) {
        this.backendUrls = backends;

        int cores = Runtime.getRuntime().availableProcessors();
        long keepAliveTime = 1000;
        int queueSize = 2048;
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        proxyService = new ThreadPoolExecutor(
                cores,
                cores,
                keepAliveTime,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueSize),
                new NamedThreadFactory("proxyService"),
                handler);

        nettyHttpClient = new NettyHttpClient();
    }

    @Override
    public void handle(FullHttpRequest fullRequest, ChannelHandlerContext ctx, HttpRequestFilter filter) {
        String backendUrl = router.route(this.backendUrls);
        final String url = backendUrl + fullRequest.uri();
        filter.filter(fullRequest, ctx);
        proxyService.submit(() -> fetchGet(fullRequest, ctx, url));
    }

    private ByteBufToBytes reader;

    private void fetchGet(FullHttpRequest inbound, ChannelHandlerContext ctx, final String url) {
        try {
            // 发送 Netty 请求
            nettyHttpClient.connect(url, (nettyCtx, msg) -> {
                if (msg instanceof HttpResponse) {
                    HttpResponse response = (HttpResponse) msg;
                    System.out.println("CONTENT_TYPE:" + response.headers().get(HttpHeaderNames.CONTENT_TYPE));
                    if (HttpUtil.isContentLengthSet(response)) {
                        reader = new ByteBufToBytes(
                                (int) HttpUtil.getContentLength(response));
                    }
                }
                if (msg instanceof HttpContent) {
                    HttpContent httpContent = (HttpContent) msg;
                    ByteBuf content = httpContent.content();
                    reader.reading(content);
                    content.release();
                    if (reader.isEnd()) {
                        handleResponse(inbound, ctx, reader.readFull(), reader.contentLength);
                    }
                }
                nettyCtx.close();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleResponse(FullHttpRequest fullRequest, ChannelHandlerContext ctx, byte[] body, int contentLength) {
        FullHttpResponse response = null;
        try {
            response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(body));
            response.headers().set("Content-Type", "application/json");
            response.headers().setInt("Content-Length", contentLength);
            filter.filter(response);
        } catch (Exception e){
            e.printStackTrace();
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NO_CONTENT);
            exceptionCaught(ctx, e);
        } finally {
            if(fullRequest != null){
                if(!HttpUtil.isKeepAlive(fullRequest)){
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx.write(response);
                }
            }
            ctx.flush();
        }
    }
}
```

字节处理工具类 ByteBufToBytes

```java
public class ByteBufToBytes {

    private ByteBuf temp;
    private boolean end = true;
    public final int contentLength;
    public ByteBufToBytes(int contentLength) {
        this.contentLength = contentLength;
        temp = Unpooled.buffer(contentLength);
    }
    public void reading(ByteBuf datas) {
        datas.readBytes(temp, datas.readableBytes());
        if (this.temp.writableBytes() != 0) {
            end = false;
        } else {
            end = true;
        }
    }
    public boolean isEnd() {
        return end;
    }
    public byte[] readFull() {
        if (end) {
            byte[] contentByte = new byte[this.temp.readableBytes()];
            this.temp.readBytes(contentByte);
            this.temp.release();
            return contentByte;
        } else {
            return null;
        }
    }
    public byte[] read(ByteBuf datas) {
        byte[] bytes = new byte[datas.readableBytes()];
        datas.readBytes(bytes);
        return bytes;
    }
}
```

