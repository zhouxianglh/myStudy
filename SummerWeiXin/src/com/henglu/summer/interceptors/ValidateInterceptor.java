package com.henglu.summer.interceptors;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.henglu.summer.control.IControl;
import com.henglu.summer.interceptors.interfaces.Interceptor;
import com.henglu.summer.utils.CommonUtils;
import com.henglu.weixin.servlet.StartServlet;

/**
 * 验证请求是否来自微信服务器(页面接口测试时此类会验证不通过)
 */
public class ValidateInterceptor implements Interceptor {
    private static Logger logger = Logger.getLogger(StartServlet.class);
    private String token;
    private IControl control;
    private Map<String, Object> context;

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
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

    @Override
    public Map<String, Object> getContext() {
        return context;
    }

    @Override
    public void setControl(IControl control) {
        this.control = control;
        context = control.getContext();
    }

    public void setToken(String token) {
        this.token = token;
    }
}
