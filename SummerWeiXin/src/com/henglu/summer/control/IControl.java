package com.henglu.summer.control;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 控制器,用于处理微信过来的请求
 */
public interface IControl {
	/**
	 * 请求Bean
	 */
	public static String REQUEST_MESSAGE_BEAN = "REQUEST_MESSAGE_BEAN";
	/**
	 * 响应请求的Bean
	 */
	public static String RESPONSE_MESSAGE_BEAN = "RESPONSE_MESSAGE_BEAN";
	/**
	 * 响应空
	 */
	public static String RESPONSE_NULL = "RESPONSE_NULL";

	/**
	 * 获取上下文内容(线程安全,支持并发操作)
	 */
	public Map<String, Object> getContext();

	/**
	 * 执行请求分发的方法
	 */
	public void execute(HttpServletRequest request, HttpServletResponse response);
}
