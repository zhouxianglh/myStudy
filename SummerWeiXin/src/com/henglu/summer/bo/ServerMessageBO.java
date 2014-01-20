package com.henglu.summer.bo;

/**
 * 客服接口消息,暂时只支持文字消息
 * @author zhouxianglh@gmail.com
 * @version 1.0  2014-1-20 下午10:20:32
 */
public class ServerMessageBO extends BaseBO {
    public class TextBO {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    private static final long serialVersionUID = 7467680886565778201L;
    public static final String MSG_TYPE_TEXT = "text";
    private String touser;
    private String msgtype;
    private TextBO text;

    public String getMsgtype() {
        return msgtype;
    }

    public TextBO getText() {
        return text;
    }

    public String getTouser() {
        return touser;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public void setText(TextBO text) {
        this.text = text;
    }

    public void setTouser(String touser) {
        this.touser = touser;
    }
}
