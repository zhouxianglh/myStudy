package com.henglu.summer.interceptors.interfaces;

import java.util.Collection;

import com.henglu.summer.bo.CustomerBO;
import com.henglu.summer.bo.WeiXinMessageBO;

/**
 * 拦截器接口
 * @author zhouxianglh@gmail.com
 * @version 1.0  2014-1-20 下午10:19:28
 */
public interface IServiceCenter {
    /**
     * 添加用户到人工服务
     */
    public void addCustomer(String customerID) throws Exception;

    /**
     * 获取当前连接中的客户
     */
    public Collection<CustomerBO> getCurrnetCustomer() throws Exception;

    /**
     * 当前请求是否是发给人工客服中心的
     */
    public boolean isSendToServer(String customerID) throws Exception;

    /**
     * 接收客户发给人工客服的消息
     */
    public void reciveMessage(WeiXinMessageBO messageBO) throws Exception;

    /**
     * 根据ID把客户从人工服务移除
     */
    public void removeCustomer(String openID) throws Exception;

    /**
     * 释放长时间未操作的客户
     */
    public void freeCustomer() throws Exception;

    /**
     * 释放长时间未操作的客户之前发送能知
     */
    public void freeCustomerSendNote() throws Exception;
}
