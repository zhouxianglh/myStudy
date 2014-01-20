package com.henglu.summer.bo;

import java.util.Date;

/**
 * 客户对象(线程安全)
 */
public class CustomerBO extends BaseBO {
    /**
     * 等待
     */
    public static final int STATUS_WAIT = 1;
    /**
     * 使用中
     */
    public static final int STATUS_LINE = 2;
    /**
     * 转接
     */
    public static final int STATUS_LINK = 3;
    private static final long serialVersionUID = -1249339095576888538L;
    private int status;
    private String customerID;
    private String nickName;
    private Date lastMessageTime;

    public synchronized String getCustomerID() {
        return customerID;
    }

    public synchronized Date getLastMessageTime() {
        return lastMessageTime;
    }

    public synchronized String getNickName() {
        return nickName;
    }

    public synchronized int getStatus() {
        return status;
    }

    @Override
    public synchronized Object getUserObject() {
        return super.getUserObject();
    }

    public synchronized void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public synchronized void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public synchronized void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public synchronized void setStatus(int status) {
        this.status = status;
    }

    @Override
    public synchronized void setUserObject(Object userObject) {
        super.setUserObject(userObject);
    }
}
