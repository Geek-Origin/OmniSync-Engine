package org.example.engine.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class OmniSyncClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println("[收到 Server 回复] : " + msg);
    }

    // ✨ 新增：处理空闲事件（心跳机制核心）
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                // 如果 30 秒没发数据了，就发一个纯正的心跳包
                System.out.println("💓 发送心跳包，保持连接活跃...");
                ctx.writeAndFlush("{\"action\":\"PING\",\"payload\":\"HEARTBEAT\"}\n");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}