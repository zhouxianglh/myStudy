package com.henglu.summer.control;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.henglu.summer.bo.WeiXinMessageBO;
import com.henglu.summer.interceptors.interfaces.IServiceCenter;
import com.henglu.summer.utils.CommonWeixinUtils;

/**
 * 这里是做演示使用,如果要用,必需重写
 */
public class SummerControl extends BaseControl {
    private static Logger logger = Logger.getLogger(SummerControl.class);
    private IServiceCenter serviceCenter;

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WeiXinMessageBO messageBO = (WeiXinMessageBO) context.get(IControl.REQUEST_MESSAGE_BEAN);
        if (WeiXinMessageBO.MSGTYPE_TEXT.equals(messageBO.getMsgType())) {// 文字消息一律回复 提莫队长,正在待命
            logger.info(messageBO.getContent());
            context.put(IControl.RESPONSE_MESSAGE_BEAN, CommonWeixinUtils.createMessageBO(messageBO.getFromUserName(),
                    messageBO.getToUserName(), "提莫队长,正在待命!!"));
            logger.info("响应了请求....");
        } else {// 事件,第一次触会会接入人工客服功能,再次触发则退出
            if (serviceCenter.isSendToServer(messageBO.getFromUserName())) {
                serviceCenter.removeCustomer(messageBO.getFromUserName());
            } else {
                serviceCenter.addCustomer(messageBO.getFromUserName());
            }
            context.put(IControl.RESPONSE_NULL, "");
            logger.info("响应了请求....");
        }
    }

    public void setServiceCenter(IServiceCenter serviceCenter) {
        this.serviceCenter = serviceCenter;
    }
}
