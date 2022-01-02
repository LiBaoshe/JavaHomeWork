# 3.（必做）改造自定义 RPC 的程序，提交到 GitHub

## 尝试将服务端写死查找接口实现类变成泛型和反射

接口：

```java
public interface RpcfxResolver {

    <T> T resolve(Class<T> tClass);

}
```

实现类：

```java
public class DemoResolver implements RpcfxResolver, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> T resolve(Class<T> tClass) {
        return this.applicationContext.getBean(tClass);
    }
}
```

RpcfxInvoker：

```java
public class RpcfxInvoker{

    private RpcfxResolver resolver;

    public RpcfxInvoker(RpcfxResolver resolver){
        this.resolver = resolver;
    }

    public RpcfxResponse invoke(RpcfxRequest request) {
        RpcfxResponse response = new RpcfxResponse();
        String serviceClass = request.getServiceClass();
        Class<?> tClass = null;
        try {
            tClass = Class.forName(serviceClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // 作业1：改成泛型和反射
        Object service = resolver.resolve(tClass);//this.applicationContext.getBean(serviceClass);

        try {
            Method method = resolveMethodFromClass(service.getClass(), request.getMethod());
            Object result = method.invoke(service, request.getParams()); // dubbo, fastjson,
            // 两次json序列化能否合并成一个
            response.setResult(JSON.toJSONString(result, SerializerFeature.WriteClassName));
            response.setStatus(true);
            return response;
        } catch ( IllegalAccessException | InvocationTargetException e) {

            // 3.Xstream

            // 2.封装一个统一的RpcfxException
            // 客户端也需要判断异常
            e.printStackTrace();
            response.setException(e);
            response.setStatus(false);
            return response;
        }
    }

    private Method resolveMethodFromClass(Class<?> klass, String methodName) {
        return Arrays.stream(klass.getMethods()).filter(m -> methodName.equals(m.getName())).findFirst().get();
    }

}
```

## 尝试将客户端动态代理改成 AOP，添加异常处理

pom 引入 aop 依赖

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
```

这块内容目前没理解透彻  - _ -

## 尝试使用 Netty+HTTP 作为 client 端传输方式

pom 引入 netty 依赖

```xml
		<dependency>
			<groupId>io.netty</groupId>
			<artifactId>netty-all</artifactId>
		</dependency>
```

ByteBufToBytes

```java
public class ByteBufToBytes {

    private ByteBuf temp;
    private boolean end = true;
    public final int contentLength;

    public ByteBufToBytes(){
        this(10);
    }
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

NettyHttpClient

```java
/**
 * Netty 发送 http 请求
 */
public class NettyHttpClient {

    public void connect(String url, RpcfxRequest req, final NettyHttpResp nettyHttpResp) throws Exception {

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
                    .option(EpollChannelOption.SO_REUSEPORT, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 客户端接收到的是httpResponse响应，所以要使用HttpResponseDecoder进行解码
                            pipeline.addLast(new HttpResponseDecoder());
                            // 客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
//                            ch.pipeline().addLast(new HttpRequestEncoder());
                            pipeline.addLast(new NettyHttpClientHandler(nettyHttpResp));
                        }
                    });
            ChannelFuture f = bootstrap.connect(host, port).sync();

            DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.POST, uri.toASCIIString());
            // 构建http请求
            request.setDecoderResult(DecoderResult.SUCCESS);
            request.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
            request.headers().add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
//            request.headers().add(HttpHeaderNames.ACCEPT, "application/json");
            request.headers().set(HttpHeaderNames.HOST, host);

            String reqJson = JSON.toJSONString(req);
            ByteBuf buffer = request.content().clear();
            int p0 = buffer.writerIndex();
            buffer.writeBytes(reqJson.getBytes());
            int p1 = buffer.writerIndex();

            int i = buffer.readableBytes();
            System.out.println("buffer.readableBytes(): " + i);
            System.out.println("p1 - p0: " + (p1 - p0) );

//            request.headers().add(HttpHeaderNames.CONTENT_LENGTH, p1 - p0);
            request.headers().add(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());

            // 发送http请求
            f.channel().write(request).sync();
            f.channel().flush();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    private static ByteBufToBytes reader;

    public static void main(String[] args) throws Exception {
        // 测试 NettyHttpClient
        NettyHttpClient client = new NettyHttpClient();
        String[] urls = new String[]{"http://127.0.0.1:8080/"};
        RpcfxRequest request = new RpcfxRequest();
        request.setServiceClass("io.kimmking.rpcfx.demo.api.UserService");
        request.setMethod("findById");

        for (int i = 0; i < 1; i++) {
            test(client, request, urls[i % urls.length]);
        }
    }

    private static void test(NettyHttpClient client, RpcfxRequest req,String url) throws Exception {
        client.connect(url, req, ((ctx, msg) -> {

            if (msg instanceof HttpResponse) {
                HttpResponse response = (HttpResponse) msg;
                System.out.println("STATUS: " + response.getStatus());
                System.out.println("VERSION: " + response.getProtocolVersion());
                if (!response.headers().isEmpty()) {
                    for (String name: response.headers().names()) {
                        for (String value: response.headers().getAll(name)) {
                            System.out.println("HEADER: " + name + " = " + value);
                        }
                    }
                }
                System.out.print("CONTENT {");
            }
            if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;

                System.out.print(content.content().toString(CharsetUtil.UTF_8));

                System.out.flush();
                if (content instanceof LastHttpContent) {
                    System.out.println("} END OF CONTENT");
                    ctx.close();
                }
            }
        }));
    }
}
```

NettyHttpClientHandler

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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
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

NettyHttpResp

```java
/**
 * NettyHttp 返回结果处理接口，NettyHttpClient 的 connect 方法中回调 http 的请求结果
 */
@FunctionalInterface
public interface NettyHttpResp {

    void handle(ChannelHandlerContext ctx, Object msg);

}
```

