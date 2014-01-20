package com.henglu.summer.bo;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;

/**
 * Spark 对象,用于存入一个Spark对话对象
 * @author zhouxianglh@gmail.com
 * @version 1.0  2014-1-20 下午10:20:32
 */
public class SparkBO extends BaseBO {
    private static final long serialVersionUID = 6113212184391147220L;
    private Connection conn;
    private Chat chat;
    private String userName;
    private String server;

    @Override
    protected void finalize() throws Throwable {
        if (null != conn && conn.isConnected()) {
            conn.disconnect();
        }
        super.finalize();
    }

    public Chat getChat() {
        return chat;
    }

    public Connection getConn() {
        return conn;
    }

    public String getServer() {
        return server;
    }

    public String getUserName() {
        return userName;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
