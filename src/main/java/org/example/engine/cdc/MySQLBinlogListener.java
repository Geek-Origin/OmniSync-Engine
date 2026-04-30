package org.example.engine.cdc;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventType;
import io.netty.channel.Channel;
import org.example.engine.client.OmniSyncClient;

public class MySQLBinlogListener {

    private static final String DB_HOST = "127.0.0.1";
    private static final int DB_PORT = 3306;
    private static final String DB_USER = "root";
    // ⚠️ 注意：修改为你真实的 MySQL 密码！
    private static final String DB_PASSWORD = "123456";

    // 直接在 main 方法上抛出异常，这是最简单的解决方式
    public static void main(String[] args) throws Exception {

        // 1. 启动带有 SSL 双向加密的 Netty 客户端！
        Channel nettyChannel = OmniSyncClient.start();

        // 2. 创建 Binlog 客户端
        BinaryLogClient client = new BinaryLogClient(DB_HOST, DB_PORT, DB_USER, DB_PASSWORD);

        // 3. 注册事件监听器
        client.registerEventListener(event -> {
            Event data = event;
            EventType type = data.getHeader().getEventType();

            String action = "";
            if (EventType.isWrite(type)) {
                action = "🟢 [新增数据]";
            } else if (EventType.isUpdate(type)) {
                action = "🟡 [修改数据]";
            } else if (EventType.isDelete(type)) {
                action = "🔴 [删除数据]";
            }

            if (!action.isEmpty()) {
                String payload = action + " : " + data.getData();
                System.out.println("本机捕获: " + payload);

                // ✨ 核心合体：把捕获到的数据，通过加密的 Netty 管道发给 Server！
                nettyChannel.writeAndFlush(payload + "\n");
            }
        });

        System.out.println("🕵️ OmniSync 安全数据捕获器启动！正在监听数据库变化...");
        client.connect();
    }
}