package com.henglu.summer.interceptors.customerservice;

import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.henglu.summer.bo.CustomerBO;
import com.henglu.summer.bo.UserBO;
import com.henglu.summer.bo.WeiXinMessageBO;
import com.henglu.summer.interceptors.interfaces.IServiceCenter;
import com.henglu.summer.spark.SparkServer;
import com.henglu.summer.spark.interfaces.IMessageToSpakHandle;
import com.henglu.summer.spark.interfaces.IMessageToSpark;
import com.henglu.summer.utils.CommonWeixinUtils;

/**
 * Spark做人工客服客户端的人工客服中心
 * @author zhouxianglh@gmail.com
 * @version 1.0  2014-1-20 下午10:20:32
 */
public class SparkServiceCenter implements IServiceCenter, IMessageToSpakHandle {
    private static Logger logger = Logger.getLogger(SparkServiceCenter.class);
    private static SparkServiceCenter sparkServiceCenter = new SparkServiceCenter();
    private IMessageToSpark sparkServer;
    /**
     * 客户列表
     */
    private Map<String, CustomerBO> customerMap = new Hashtable<String, CustomerBO>();
    private CommonWeixinUtils weixinUtils;
    /**
     * 清理客户时间
     */
    private long clearCustomer;

    public static SparkServiceCenter getInstance() {
        return sparkServiceCenter;
    }

    private SparkServiceCenter() {
    }

    /**
     * 添加客户
     */
    @Override
    public synchronized void addCustomer(String customerID) throws Exception {
        if (null != customerMap.get(customerID)) {// 已经加入
            return;
        }
        CustomerBO customerBO = new CustomerBO();
        customerBO.setCustomerID(customerID);
        customerBO.setLastMessageTime(new Date());
        customerBO.setStatus(CustomerBO.STATUS_WAIT);
        UserBO userBO = weixinUtils.getUserInfo(customerID);// 微信接口获取用户信息
        customerBO.setNickName(userBO.getNickname());
        customerBO.setUserObject(userBO);
        // 第一次发送消息附加昵称,之后就不用了
        boolean flag = sparkServer.sendMessageToSpark(customerID, customerBO.getNickName(), customerBO.getNickName()
                + " 请求接入");
        if (flag) {
            weixinUtils.sendTextMessageByServer(customerID, "连接客服中心成功");
            customerMap.put(customerID, customerBO);
        } else {
            weixinUtils.sendTextMessageByServer(customerID, "连接客服中心失败");
        }
    }

    /**
     * 清理长时间未处理的连接
     */
    @Override
    public synchronized void freeCustomer() throws Exception {
        logger.info("清理长时间未响应客户......");
        long nowTime = new Date().getTime();
        Iterator<CustomerBO> iterator = customerMap.values().iterator();
        while (iterator.hasNext()) {
            CustomerBO customerBO = iterator.next();
            if (nowTime - customerBO.getLastMessageTime().getTime() > clearCustomer) {
                iterator.remove();
                weixinUtils.sendTextMessageByServer(customerBO.getCustomerID(), "长时间未操作与服务器断开");
                removeCustomer(customerBO.getCustomerID());
            }
        }
    }

    /**
     * 清理长时间未处理的连接前发送通知
     */
    @Override
    public synchronized void freeCustomerSendNote() {
        long nowTime = new Date().getTime();
        Iterator<CustomerBO> iterator = customerMap.values().iterator();
        while (iterator.hasNext()) {
            CustomerBO customerBO = iterator.next();
            if (nowTime - customerBO.getLastMessageTime().getTime() > clearCustomer) {
                iterator.remove();
                weixinUtils.sendTextMessageByServer(customerBO.getCustomerID(), "长时间未操作即将与服务器断开....回复任何内容保持连接");
            }
        }
    }

    @Override
    public synchronized Collection<CustomerBO> getCurrnetCustomer() {
        return customerMap.values();
    }

    @Override
    public synchronized boolean isSendToServer(String customerID) {
        return customerMap.containsKey(customerID);
    }

    @Override
    public synchronized void reciveMessage(WeiXinMessageBO messageBO) throws Exception {
        CustomerBO customerBO = customerMap.get(messageBO.getFromUserName());
        customerBO.setLastMessageTime(new Date());
        if (!sparkServer.sendMessageToSpark(messageBO.getFromUserName(), null, messageBO.getContent())) {
            weixinUtils.sendTextMessageByServer(messageBO.getFromUserName(), "人工客服无响应.");
        }
    }

    @Override
    public synchronized void removeCustomer(String openID) throws Exception {
        sparkServer.customerDisconnection(openID);
        customerMap.remove(openID);
        weixinUtils.sendTextMessageByServer(openID, "与人工客服断开连接");
    }

    @Override
    public synchronized void sendMessageToWeiXin(String openID, String content) {
        weixinUtils.sendTextMessageByServer(openID, content);
    }

    @Override
    public synchronized void serverDisconnection(String openID) {
        weixinUtils.sendTextMessageByServer(openID, "与人工客服断开连接");
        customerMap.remove(openID);
    }

    public void setClearCustomer(long clearCustomer) {
        this.clearCustomer = clearCustomer;
    }

    public void setSparkServer(IMessageToSpark sparkServer) {
        this.sparkServer = sparkServer;
    }

    public void setSparkServer(SparkServer sparkServer) {
        this.sparkServer = sparkServer;
    }

    public void setWeixinUtils(CommonWeixinUtils weixinUtils) {
        this.weixinUtils = weixinUtils;
    }
}
