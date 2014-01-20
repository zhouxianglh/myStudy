package com.henglu.summer.bo;

/**
 * 微信消息对象(暂时只支持文本和事件)
 */
public class WeiXinMessageBO extends BaseBO {
    public static final String ERROR_MESSAGE = "转换失败";
    public static final String MSGTYPE_TEXT = "text";
    public static final String MSGTYPE_EVENT = "event";
    public static final String EVENT_subscribe = "subscribe";
    public static final String EVENT_unsubscribe = "unsubscribe";
    public static final String EVENT_click = "CLICK";
    public static final String EVENT_LOCATION = "LOCATION";
    public static final String EVENT_scan = "scan";
    private static final long serialVersionUID = -629790958020691998L;
    private String toUserName;
    private String fromUserName;
    private long createTime;
    private String msgType;
    private String content;
    private String mesgId;
    private String event;
    private String eventKey;
    private String ticket;

    public String getContent() {
        return content;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getEvent() {
        return event;
    }

    public String getEventKey() {
        return eventKey;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public String getMesgId() {
        return mesgId;
    }

    public String getMsgType() {
        return msgType;
    }

    public String getTicket() {
        return ticket;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public void setMesgId(String mesgId) {
        this.mesgId = mesgId;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }
}
