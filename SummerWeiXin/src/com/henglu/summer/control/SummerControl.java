package com.henglu.summer.control;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.henglu.summer.bo.WeiXinMessageBO;
import com.henglu.summer.interceptors.customerservice.WeiXinServiceCenter;
import com.henglu.summer.utils.CommonWeixinUtils;

/**
 * 这里是做演示使用,如果要用,必需重写
 */
public class SummerControl extends BaseControl {
    private static Logger logger = Logger.getLogger(SummerControl.class);

    public void execute(HttpServletRequest request, HttpServletResponse response) {
        WeiXinServiceCenter weiXinServiceCenter = WeiXinServiceCenter.getInstance();
        WeiXinMessageBO messageBO = (WeiXinMessageBO) context.get(IControl.REQUEST_MESSAGE_BEAN);
        if (WeiXinMessageBO.MSGTYPE_TEXT.equals(messageBO.getMsgType())) {
            logger.info(messageBO.getContent());
            if ("求妹子".equals(messageBO.getContent())) {
                weiXinServiceCenter.addServer(messageBO.getFromUserName());
                logger.info("设置为客服人员....");
            } else {
                context.put(IControl.RESPONSE_MESSAGE_BEAN, CommonWeixinUtils.createMessageBO(messageBO.getFromUserName(), messageBO.getToUserName(), "提莫队长,正在待命!!"));
                logger.info("响应了请求....");
            }
        } else {
            if (weiXinServiceCenter.isSendToServer(messageBO.getFromUserName())) {
                weiXinServiceCenter.removeCustomer(messageBO.getFromUserName());
            } else {
                weiXinServiceCenter.addCustomer(messageBO.getFromUserName());
            }
            context.put(IControl.RESPONSE_NULL, "");
            logger.info("响应了请求....");
        }
    }
}
