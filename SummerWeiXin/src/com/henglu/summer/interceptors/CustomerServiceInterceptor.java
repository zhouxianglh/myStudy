package com.henglu.summer.interceptors;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.henglu.summer.bo.WeiXinMessageBO;
import com.henglu.summer.control.IControl;
import com.henglu.summer.interceptors.interfaces.IServiceCenter;
import com.henglu.summer.interceptors.interfaces.Interceptor;

/**
 * 客服接口拦截器(用于人工客服)
 */
public class CustomerServiceInterceptor implements Interceptor {
    private static Logger logger = Logger.getLogger(CustomerServiceInterceptor.class);
    private IControl control;
    private IServiceCenter serviceCenter;
    private Map<String, Object> context;

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WeiXinMessageBO messageBO = (WeiXinMessageBO) context.get(IControl.REQUEST_MESSAGE_BEAN);
        // 不管是使用微信,还是Spark人工客服客户端暂时都不按受除文本消息以外的消息,所以这里有这个限制
        if (WeiXinMessageBO.MSGTYPE_TEXT.equals(messageBO.getMsgType())
                && serviceCenter.isSendToServer(messageBO.getFromUserName())) {
            logger.info("消息发往人工客服....");
            serviceCenter.reciveMessage(messageBO);
            context.put(IControl.RESPONSE_NULL, IControl.RESPONSE_NULL);
        } else {
            control.execute(request, response);
        }
    }

    @Override
    public Map<String, Object> getContext() {
        return context;
    }

    public IServiceCenter getServiceCenter() {
        return serviceCenter;
    }

    @Override
    public void setControl(IControl control) {
        this.control = control;
        context = control.getContext();
    }

    public void setServiceCenter(IServiceCenter serviceCenter) {
        this.serviceCenter = serviceCenter;
    }
}
