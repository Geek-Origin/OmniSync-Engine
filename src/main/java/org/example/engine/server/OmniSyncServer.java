package org.example.engine.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

public class OmniSyncServer {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {

        // 🌟 1. 加载服务器的“身份证” (Keystore)
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("src/main/resources/certs/server.jks"), "123456".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, "123456".toCharArray());

        // 🌟 2. 加载服务器的“信任名单” (Truststore)
        KeyStore ts = KeyStore.getInstance("JKS");
        ts.load(new FileInputStream("src/main/resources/certs/server-trust.jks"), "123456".toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);

        // 🌟 3. 构建 SSL 上下文 (开启双向认证 REQUIRE)
        final SslContext sslContext = SslContextBuilder.forServer(kmf)
                .trustManager(tmf)
                .clientAuth(ClientAuth.REQUIRE) // 这一行价值千金：强制要求客户端出示合法证书！
                .build();

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 🌟 4. 加密拦截器必须放在流水线的最前面！
                            ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));

                            //如果90秒内没有读到任何数据（包含心跳），触发读空闲事件
                            ch.pipeline().addLast(new IdleStateHandler(90, 0, 0, TimeUnit.SECONDS));

                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new OmniSyncServerHandler());
                        }
                    });

            ChannelFuture f = b.bind(PORT).sync();
            System.out.println("🔒 OmniSync 安全核心引擎启动成功！正在监听加密端口: " + PORT);
            f.channel().closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}