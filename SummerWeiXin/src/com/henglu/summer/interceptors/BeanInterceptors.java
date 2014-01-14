package com.henglu.summer.interceptors;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.henglu.summer.bo.WeiXinMessageBO;
import com.henglu.summer.control.IControl;
import com.henglu.summer.utils.CommonUtils;
import com.henglu.summer.utils.CommonWeixinUtils;

/**
 * 对xml文件进行解析操作封装成Bean和反向操作.完成对象封装后另起一个线程执行程序内容()
 */
public class BeanInterceptors implements Interceptor {
	private static Logger logger = Logger.getLogger(BeanInterceptors.class);
	private IControl control;

	private Map<String, Object> context;
	/**
	 * 用于控制重发(如果未响应服务器请求,会连发三次)
	 */
	private static Set<String> messageRecord = new HashSet<String>();

	public void execute(final HttpServletRequest request, final HttpServletResponse response) {
		ServletOutputStream outputStream = null;
		try {
			// 获取请求中的XML数据
			outputStream = response.getOutputStream();
			String requestXML = CommonWeixinUtils.getRequestXML(request);
			if (null == requestXML || requestXML.length() == 0) {// 响应微信首次验证
				outputStream.print(CommonUtils.StringConver(request.getParameter("echostr"), "UTF-8", "ISO-8859-1"));
				outputStream.flush();
				logger.info("....响应首次认证请求");
				return;
			}
			WeiXinMessageBO messageBO = CommonWeixinUtils.getWeiXinMessageBO(requestXML);
			if (WeiXinMessageBO.ERROR_MESSAGE.equals(messageBO.getUserObject())) {// 未知消息类型不作响应
				logger.info("未知的消息类型.....不作响应");
				messageBO = CommonWeixinUtils.createMessageBO(messageBO.getFromUserName(), messageBO.getToUserName(), "暂时不支持的消息类型");
				String xml = CommonWeixinUtils.messageBOToStringXML(messageBO);
				outputStream.print(CommonUtils.StringConver(xml, "UTF-8", "ISO-8859-1"));
			} else {
				String key = null;
				if (WeiXinMessageBO.MSGTYPE_TEXT.equals(messageBO.getMsgType())) {
					key = messageBO.getMesgId();
				} else if (WeiXinMessageBO.MSGTYPE_EVENT.equals(messageBO.getMsgType())) {
					key = messageBO.getCreateTime() + messageBO.getFromUserName();
				}
				// 控制重发,处理消前记录消标识,处理后删除标识,如果标识已存在,则正在处理
				synchronized (messageRecord) {// 记录消息ID
					if (messageRecord.contains(key)) {
						logger.info("重复的信息,不作响应");
						return;
					} else {
						messageRecord.add(key);
					}
				}
				context.put(IControl.REQUEST_MESSAGE_BEAN, messageBO);
				control.execute(request, response);
				// 如果不响应服务会重发三次,所以相当于15秒的等待时间,所以这里没有考滤超时的问题,而是一直等待,也没有使用客户接口
				if (null != context.get(IControl.RESPONSE_MESSAGE_BEAN)) {// 响应微信请求
					messageBO = (WeiXinMessageBO) context.get(IControl.RESPONSE_MESSAGE_BEAN);
					String xml = CommonWeixinUtils.messageBOToStringXML(messageBO);
					logger.info(xml);
					outputStream.print(CommonUtils.StringConver(xml, "UTF-8", "ISO-8859-1"));
					outputStream.flush();
					logger.info("响应完毕.....");
				} else if (null != context.get(IControl.RESPONSE_NULL)) {// 回复空字符串表示不作响应,否则微信会再发数次请求,返回空,表示拒绝响应
					outputStream.print("");
					outputStream.flush();
					logger.info("拒绝响应.....不作响应");
				}
				synchronized (messageRecord) {// 清除消息ID
					messageRecord.remove(key);
				}
			}
		} catch (Exception e) {
			logger.error("响应请求出错", e);
		} finally {
			CommonUtils.close(outputStream);
		}
	}

	public Map<String, Object> getContext() {
		return context;
	}

	public void setControl(IControl control) {
		this.control = control;
		context = control.getContext();
	}

}
