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

public class OmniSyncClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8080;

    // 暴露一个 start 方法，供 Binlog 监听器调用，并返回一个可以发消息的 Channel (管道)
    public static Channel start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new OmniSyncClientHandler());
                    }
                });

        System.out.println("🔗 正在连接 OmniSync 核心服务节点...");
        return b.connect(HOST, PORT).sync().channel();
    }
}