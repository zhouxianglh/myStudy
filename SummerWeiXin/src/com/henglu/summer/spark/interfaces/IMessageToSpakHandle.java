package com.henglu.summer.spark.interfaces;

/**
 * Spark给微信服务器发信息接接口
 * @author zhouxianglh@gmail.com
 * @version 1.0  2014-1-20 下午10:32:36
 */
public interface IMessageToSpakHandle {
    /**
     * 给微信发关客服消息
     * 
     * @param openID 用户OpenID
     * @param content 消息内容
     */
    public void sendMessageToWeiXin(String openID, String content) throws Exception;

    /**
     * 人工客服断开连接
     */
    public void serverDisconnection(String openID) throws Exception;
}
