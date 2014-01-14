package com.henglu.summer.interceptors;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.henglu.summer.bo.WeiXinMessageBO;
import com.henglu.summer.control.IControl;
import com.henglu.summer.utils.CommonUtils;

/**
 * 对请求参数进行格式化的拦截器
 */
public class ParamsFormatInterceptor implements Interceptor {

	private IControl control;

	public void execute(HttpServletRequest request, HttpServletResponse response) {
		WeiXinMessageBO messageBO = (WeiXinMessageBO) context.get(IControl.REQUEST_MESSAGE_BEAN);
		if (null != messageBO.getContent()) {// 字符串格式化,方便后面识别
			messageBO.setContent(CommonUtils.StringSymbol(CommonUtils.StringConver(messageBO.getContent())).trim());
		}
		control.execute(request, response);
	}

	public void setControl(IControl control) {
		this.control = control;
		context = control.getContext();
	}

	private Map<String, Object> context;

	public Map<String, Object> getContext() {
		return context;
	}
}
