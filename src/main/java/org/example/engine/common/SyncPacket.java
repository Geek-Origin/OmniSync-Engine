package org.example.engine.common;

// 这是一个标准的数据传输载体（Data Transfer Object）
public class SyncPacket {

    private String action;      // 操作类型：INSERT, UPDATE, DELETE
    private String payload;     // 具体的变更数据
    private long timestamp;     // 发生变更的时间戳

    // 构造函数
    public SyncPacket(String action, String payload) {
        this.action = action;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters 和 Setters (这里为了节省篇幅设为 public，实际开发中推荐用 Lombok)
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}