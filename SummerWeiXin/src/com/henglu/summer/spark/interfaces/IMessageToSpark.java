package com.henglu.summer.spark.interfaces;

/**
 * Spark作为人工客服中心的接口
 * @author zhouxianglh@gmail.com
 * @version 1.0  2014-1-20 下午10:33:36
 */
public interface IMessageToSpark {
    /**
     * 微信断开人工客服
     */
    public void customerDisconnection(String openID) throws Exception;

    /**
     * 给 Spark 发送消息
     * 
     * @param openID 微信用户OpenID
     * @param openName 昵称(只在第一次发消息时必需要,以后可以不要)
     * @param content 发送消息内容
     */
    public boolean sendMessageToSpark(String openID, String openName, String content) throws Exception;
}
