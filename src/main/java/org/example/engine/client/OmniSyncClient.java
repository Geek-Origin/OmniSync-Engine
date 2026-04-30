package org.example.engine.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

public class OmniSyncClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8080;

    public static Channel start() throws Exception {

        // 🌟 1. 加载客户端的“身份证” (Keystore)
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("src/main/resources/certs/client.jks"), "123456".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, "123456".toCharArray());

        // 🌟 2. 加载客户端的“信任名单” (Truststore)
        KeyStore ts = KeyStore.getInstance("JKS");
        ts.load(new FileInputStream("src/main/resources/certs/client-trust.jks"), "123456".toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);

        // 🌟 3. 构建 SSL 上下文
        final SslContext sslContext = SslContextBuilder.forClient()
                .keyManager(kmf)
                .trustManager(tmf)
                .build();

        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        // 🌟 4. 添加 SSL 加密处理器
                        ch.pipeline().addLast(sslContext.newHandler(ch.alloc(), HOST, PORT));

                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new OmniSyncClientHandler());
                    }
                });

        System.out.println("🔗 正在建立加密连接，请求接入 OmniSync 核心节点...");
        return b.connect(HOST, PORT).sync().channel();
    }
}