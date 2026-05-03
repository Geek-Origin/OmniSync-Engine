package org.example.engine.server;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class OmniSyncServerHandler extends SimpleChannelInboundHandler<String> {

    // ✨ 新增：初始化 Redis 连接池 (默认连接本地 6379 端口)
    private static final JedisPool jedisPool = new JedisPool("localhost", 6379);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        // 1. 过滤心跳
        if (msg.contains("\"action\":\"PING\"")) {
            return;
        }

        // 2. 打印收到的原始数据
        System.out.println("[Server 接收到消息] : " + msg);

        // 3. ✨ 真正的存储逻辑在这里！
        try (Jedis jedis = jedisPool.getResource()) {
            // 解析 JSON (需要引入 com.alibaba.fastjson2.JSONObject)
            com.alibaba.fastjson2.JSONObject jsonObject = com.alibaba.fastjson2.JSON.parseObject(msg);
            String action = jsonObject.getString("action");
            long ts = jsonObject.getLongValue("timestamp");

            // 存入 Redis
            String key = "omnisync:data:" + action + ":" + ts;
            jedis.setex(key, 86400, msg); // 缓存 24 小时

            System.out.println("💾 已成功写入 Redis! Key: " + key);
        } catch (Exception e) {
            System.err.println("❌ 写入 Redis 失败: " + e.getMessage());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("[系统通知] : 有一个新的节点接入了引擎！IP: " + ctx.channel().remoteAddress());
    }

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