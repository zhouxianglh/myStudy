package com.henglu.summer.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.henglu.summer.bo.ServerMessageBO;
import com.henglu.summer.bo.UserBO;
import com.henglu.summer.bo.WeiXinMessageBO;

public class CommonWeixinUtils {
    private static final Logger logger = Logger.getLogger(CommonWeixinUtils.class);
    private static CommonWeixinUtils weixinUtils = new CommonWeixinUtils();

    private CommonWeixinUtils() {
    }

    public static CommonWeixinUtils getInstance() {
        return weixinUtils;
    }

    /**
     * access_token有效期是7200秒,由定时任务获取
     */
    public void createtokenID() {
        logger.info("获取 access_token .....");
        try {
            String str = CommonUtils.sendGet(CommonUtils.joinString("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=", appID, "&secret=", secret));
            logger.info("createtokenID "+ str);
            Map<String, String> map = CommonUtils.toMap(str);
            token = map.get("access_token");
        } catch (IOException e) {
            logger.error("获取access_token出错", e);
        }
    }

    /**
     * 使用客服接口发送文本消息
     */
    public void sendTextMessageByServer(String openID, String context) {
        context = CommonUtils.StringConver(context, "UTF-8", "ISO-8859-1");
        ServerMessageBO messageBO = new ServerMessageBO();
        messageBO.setMsgtype(ServerMessageBO.MSG_TYPE_TEXT);
        messageBO.setTouser(openID);
        ServerMessageBO.TextBO text = messageBO.new TextBO();
        text.setContent(context);
        messageBO.setText(text);
        sendMessageByServer(messageBO);
    }

    /**
     * 发送客服消息
     */
    public void sendMessageByServer(ServerMessageBO messageBO) {
        try {
            String json = CommonUtils.toJson(messageBO);
            logger.info("sendMessageByServer "+json);
            CommonUtils.sendPost("https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + this.token, json);
        } catch (ClientProtocolException e) {
            logger.error("发送客服消息失败", e);
        } catch (IOException e) {
            logger.error("发送客服消息失败", e);
        }
    }

    /**
     * 获取用户信息
     */
    public UserBO getUserInfo(String userID) {
        try {
            String str = CommonUtils.sendGet(CommonUtils.joinString("https://api.weixin.qq.com/cgi-bin/user/info?access_token=", token, "&openid=", userID, "&lang=zh_CN"));
            str = CommonUtils.StringConver(str, "ISO-8859-1", "utf-8");
            logger.info("getUserInfo "+str);
            Map<String, String> map = CommonUtils.toMap(str);
            UserBO userBO = new UserBO();
            userBO.setSex(Integer.valueOf(map.get("sex")));
            userBO.setSubscribe(Integer.valueOf(map.get("subscribe")));
            userBO.setNickname(map.get("nickname"));
            userBO.setOpenid(userID);
            userBO.setHeadimgurl(map.get("headimgurl"));
            userBO.setCity(map.get("city"));
            userBO.setCountry(map.get("country"));
            userBO.setLanguage(map.get("language"));
            userBO.setProvince(map.get("province"));
            userBO.setSubscribe_time(Long.valueOf(map.get("subscribe_time")));
            return userBO;
        } catch (IOException e) {
            logger.error("获取UserInfo 出错", e);
        }
        return null;
    }

    /**
     * 创建消息对象(文本)
     */
    public static WeiXinMessageBO createMessageBO(String toUserName, String fromUserName, String content) {
        WeiXinMessageBO messageBO = new WeiXinMessageBO();
        messageBO.setToUserName(toUserName);
        messageBO.setFromUserName(fromUserName);
        messageBO.setCreateTime(new Date().getTime() / 1000);
        messageBO.setMsgType(WeiXinMessageBO.MSGTYPE_TEXT);
        messageBO.setContent(content);
        return messageBO;
    }

    /**
     * 获取请求中的xml字符串
     */
    public static String getRequestXML(final HttpServletRequest request) throws IOException, UnsupportedEncodingException {
        BufferedInputStream inputStream = null;
        inputStream = new BufferedInputStream(request.getInputStream());
        StringBuffer sb = new StringBuffer();
        byte[] message = new byte[1024];
        while (inputStream.read(message, 0, message.length) != -1) {
            sb.append(new String(message, 0, message.length, "UTF-8"));
        }
        return sb.toString().trim();
    }

    /**
     * 获取微信消息对象(获取失败,userObject反返回 WeiXinMessageBO.ERROR_MESSAGE)
     */
    public static WeiXinMessageBO getWeiXinMessageBO(String xmlString) throws DocumentException {
        WeiXinMessageBO messageBO = new WeiXinMessageBO();
        Document doc = DocumentHelper.parseText(xmlString.trim());
        Element root = doc.getRootElement();
        messageBO.setToUserName(root.elementTextTrim("ToUserName"));
        messageBO.setFromUserName(root.elementTextTrim("FromUserName"));
        messageBO.setCreateTime(Long.valueOf(root.elementTextTrim("CreateTime")));
        messageBO.setMsgType(root.elementTextTrim("MsgType"));
        if (WeiXinMessageBO.MSGTYPE_TEXT.equals(messageBO.getMsgType())) {
            messageBO.setMesgId(root.elementTextTrim("MsgId"));
            messageBO.setContent(root.elementTextTrim("Content"));
            return messageBO;
        } else if (WeiXinMessageBO.MSGTYPE_EVENT.equals(messageBO.getMsgType())) {
            messageBO.setEvent(root.elementTextTrim("Event"));
            if (WeiXinMessageBO.EVENT_LOCATION.equals(messageBO.getEvent())) {
                messageBO.setUserObject(WeiXinMessageBO.ERROR_MESSAGE);
                return messageBO;
            }
            messageBO.setEventKey(root.elementTextTrim("EventKey"));
            messageBO.setTicket(root.elementTextTrim("Ticket"));
            return messageBO;
        } else {
            messageBO.setUserObject(WeiXinMessageBO.ERROR_MESSAGE);
            return messageBO;
        }
    }

    /**
     * 消息对象转换成XML对象
     */
    public static String messageBOToStringXML(WeiXinMessageBO messageBO) {
        StringBuilder sb = new StringBuilder("<xml>");
        sb.append("<ToUserName><![CDATA[");
        sb.append(messageBO.getToUserName());
        sb.append("]]></ToUserName>");

        sb.append("<FromUserName><![CDATA[");
        sb.append(messageBO.getFromUserName());
        sb.append("]]></FromUserName>");

        sb.append("<CreateTime>");
        sb.append(messageBO.getCreateTime());
        sb.append("</CreateTime>");

        sb.append("<MsgType><![CDATA[");
        sb.append(messageBO.getMsgType());
        sb.append("]]></MsgType>");
        if (WeiXinMessageBO.MSGTYPE_TEXT.equals(messageBO.getMsgType())) {
            sb.append("<Content><![CDATA[");
            sb.append(messageBO.getContent());
            sb.append("]]></Content>");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    private String token;

    private String appID;

    private String secret;

    public String getAppID() {
        return appID;
    }

    public String getSecret() {
        return secret;
    }

    public String getToken() {
        return token;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
