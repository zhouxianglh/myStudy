package com.henglu.summer.interceptors;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.henglu.summer.control.IControl;
import com.henglu.summer.utils.CommonUtils;
import com.henglu.weixin.servlet.StartServlet;

public class ValidateInterceptor implements Interceptor {
	private static Logger logger = Logger.getLogger(StartServlet.class);
	private String token;

	private IControl control;

	private Map<String, Object> context;

	public void execute(HttpServletRequest request, HttpServletResponse response) {
		String timestamp = request.getParameter("timestamp");
		String nonce = request.getParameter("nonce");
		String[] strArr = new String[] { nonce, token, timestamp };
		Arrays.sort(strArr);
		String valudateStr = CommonUtils.encrypt(CommonUtils.joinString(strArr));
		String signature = request.getParameter("signature");
		if (signature.equals(valudateStr)) {
			logger.info("验证通过...进一步执行..");
			control.execute(request, response);
		} else {
			logger.info("验证未通过.....");
			return;
		}
	}

	public Map<String, Object> getContext() {
		return context;
	}

	public void setControl(IControl control) {
		this.control = control;
		context = control.getContext();
	}

	public void setToken(String token) {
		this.token = token;
	}
}
