package com.henglu.summer.interceptors;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.henglu.summer.bo.WeiXinMessageBO;
import com.henglu.summer.control.IControl;
import com.henglu.summer.interceptors.customerservice.IServiceCenter;

/**
 * 客服接口拦截器
 */
public class CustomerServiceInterceptor implements Interceptor {
	private static Logger logger = Logger.getLogger(CustomerServiceInterceptor.class);
	private IControl control;
	private IServiceCenter serviceCenter;

	private Map<String, Object> context;

	public void execute(HttpServletRequest request, HttpServletResponse response) {
		WeiXinMessageBO messageBO = (WeiXinMessageBO) context.get(IControl.REQUEST_MESSAGE_BEAN);
		// Spring 中如果注入了 serviceCenter 则说明使用人工客服功能,消息直接转接到人工客服
		if (null != serviceCenter && WeiXinMessageBO.MSGTYPE_TEXT.equals(messageBO.getMsgType()) && serviceCenter.isSendToServer(messageBO.getFromUserName())) {
			logger.info("消息发往人工客服....");
			serviceCenter.reciveMessage(messageBO);
			context.put(IControl.RESPONSE_NULL, IControl.RESPONSE_NULL);
		} else {
			control.execute(request, response);
		}
	}

	public Map<String, Object> getContext() {
		return context;
	}

	public IServiceCenter getServiceCenter() {
		return serviceCenter;
	}

	public void setControl(IControl control) {
		this.control = control;
		context = control.getContext();
	}

	public void setServiceCenter(IServiceCenter serviceCenter) {
		this.serviceCenter = serviceCenter;
	}
}
