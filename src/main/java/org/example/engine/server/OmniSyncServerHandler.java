package org.example.engine.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class OmniSyncServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        // ✨ 新增：拦截心跳包，不当做正常数据处理
        if (msg.contains("\"action\":\"PING\"")) {
            System.out.println("💚 收到客户端心跳，连接健康。");
            return;
        }

        System.out.println("[Server 接收到消息] : " + msg);
        ctx.writeAndFlush("ACK: 已成功接收数据\n");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("[系统通知] : 有一个新的节点接入了引擎！IP: " + ctx.channel().remoteAddress());
    }

    // ✨ 新增：检测如果是长时间没有动静，就断开连接
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                System.out.println("💔 超过 90 秒未收到数据或心跳，判定为死连接，强制踢出！IP: " + ctx.channel().remoteAddress());
                ctx.close(); // 掐断死连接
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}