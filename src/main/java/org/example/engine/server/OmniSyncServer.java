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

public class OmniSyncServer {

    // 我们先暂定引擎运行在 8080 端口
    private static final int PORT = 8080;

    public static void main(String[] args) {
        // bossGroup 负责接待连接，就像餐厅的“迎宾员”
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // workerGroup 负责处理具体的业务，就像餐厅的“服务员”
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // ServerBootstrap 是 Netty 的启动助手
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // 指定使用 NIO 模式
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 这里的 Pipeline 就像是流水线，我们先装上基础的字符串编解码器
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new StringEncoder());
                            // 稍后我们会在这里加入自定义的“数据处理器”
                            ch.pipeline().addLast(new OmniSyncServerHandler());
                        }
                    });

            // 绑定端口，同步等待成功
            ChannelFuture f = b.bind(PORT).sync();
            System.out.println("🚀 OmniSync 核心引擎启动成功！正在监听端口: " + PORT);

            // 阻塞主线程，直到服务器通道被关闭
            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            System.err.println("引擎启动被打断: " + e.getMessage());
        } finally {
            // 优雅地关闭线程池
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}