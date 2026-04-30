package org.example.engine.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

// 继承 SimpleChannelInboundHandler，指明我们处理的数据类型是 String
public class OmniSyncServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        // 当收到消息时，触发这个方法
        System.out.println("[Server 接收到消息] : " + msg);

        // 收到消息后，给客户端回一个确认信息
        ctx.writeAndFlush("我已经收到你的消息啦！数据同步准备就绪。\n");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 当有新的客户端连接进来时，触发这个方法
        System.out.println("[系统通知] : 有一个新的节点接入了引擎！IP: " + ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 发生异常时关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}