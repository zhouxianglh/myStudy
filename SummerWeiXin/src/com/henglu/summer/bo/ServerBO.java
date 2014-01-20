package com.henglu.summer.bo;

import java.util.Date;

/**
 * 微信客服对象(线程安全)
 * @author zhouxianglh@gmail.com
 * @version 1.0  2014-1-20 下午10:20:32
 */
public class ServerBO extends BaseBO {
    private static final long serialVersionUID = -6978409234359906633L;
    public static final int TYPE_ONLINE = 1;
    public static final int TYPE_BUSY = 2;
    public static final int TYPE_OFFLINE = 3;
    public static final int TYPE_SERVER_WATI = 4;
    private String serverID;
    private int status;
    private String nickName;
    private Date lastMessageTime;

    public synchronized Date getLastMessageTime() {
        return lastMessageTime;
    }

    public synchronized String getNickName() {
        return nickName;
    }

    public synchronized String getServerID() {
        return serverID;
    }

    public synchronized int getStatus() {
        return status;
    }

    @Override
    public synchronized Object getUserObject() {
        return super.getUserObject();
    }

    public synchronized void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public synchronized void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public synchronized void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public synchronized void setStatus(int status) {
        this.status = status;
    }

    @Override
    public synchronized void setUserObject(Object userObject) {
        super.setUserObject(userObject);
    }
}
