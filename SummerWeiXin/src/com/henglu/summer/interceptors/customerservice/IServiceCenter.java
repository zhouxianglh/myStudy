package com.henglu.summer.interceptors.customerservice;

import java.util.Collection;

import com.henglu.summer.bo.CustomerBO;
import com.henglu.summer.bo.ServerBO;
import com.henglu.summer.bo.WeiXinMessageBO;

public interface IServiceCenter {
	/**
	 * 添加用户到人工服务
	 */
	public void addCustomer(String customerID);

	/**
	 * 获取当前连接中的客户
	 */
	public Collection<CustomerBO> getCurrnetCustomer();

	/**
	 * 获取当前连接中的客户
	 */
	public Collection<ServerBO> getCurrnetServer();

	/**
	 * 当前请求是否是发给人工客服中心的
	 */
	public boolean isSendToServer(String customerID);

	/**
	 * 接收客户发给人工客服的消息
	 */
	public void reciveMessage(WeiXinMessageBO messageBO);

	/**
	 * 根据ID把客户从人工服务移除
	 */
	public void removeCustomer(String openID);

	/**
	 * 根据ID把客户从人工服务移除
	 */
	public void removeServer(String openID);
}
