package com.henglu.summer.interceptors.customerservice;

import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.henglu.summer.bo.CustomerBO;
import com.henglu.summer.bo.ServerBO;
import com.henglu.summer.bo.UserBO;
import com.henglu.summer.bo.WeiXinMessageBO;
import com.henglu.summer.utils.CommonWeixinUtils;

/**
 * 使用微信作为客服客户端(1对1 模式人工客服中心)
 */
public class WeiXinServiceCenter implements IServiceCenter {
    private static Logger logger = Logger.getLogger(WeiXinServiceCenter.class);
    private static WeiXinServiceCenter serviceCenter = new WeiXinServiceCenter();

    public static WeiXinServiceCenter getInstance() {
        return serviceCenter;
    }

    /**
     * 清理等待队列时间
     */
    private long clearWait;
    /**
     * 清理客服时间
     */
    private long clearServer;
    /**
     * 清理客户时间
     */
    private long clearCustomer;

    /**
     * 客户列表
     */
    private Map<String, CustomerBO> customerMap = new Hashtable<String, CustomerBO>();

    /**
     * 客服列表
     */
    private Map<String, ServerBO> serverMap = new Hashtable<String, ServerBO>();

    /**
     * 客服列表
     */
    private Map<String, ServerBO> serverNameMap = new Hashtable<String, ServerBO>();

    /**
     * 客户-客服关联表
     */
    private Map<CustomerBO, ServerBO> linkCustomerMap = new Hashtable<CustomerBO, ServerBO>();
    /**
     * 客服-客户关联表
     */
    private Map<ServerBO, CustomerBO> linkServerMap = new Hashtable<ServerBO, CustomerBO>();

    /**
     * 客户-客服关联表(等待中)
     */
    private Map<ServerBO, CustomerBO> waitMap = new Hashtable<ServerBO, CustomerBO>();
    /**
     * 客服-客户关联表(等待中)
     */
    private Map<CustomerBO, ServerBO> waitCustomerMap = new Hashtable<CustomerBO, ServerBO>();

    private CommonWeixinUtils weixinUtils;

    private WeiXinServiceCenter() {
    }

    /**
     * 添加客户
     */
    public synchronized void addCustomer(String customerID) {
        CustomerBO customerBO = new CustomerBO();
        customerBO.setCustomerID(customerID);
        customerBO.setLastMessageTime(new Date());
        customerBO.setStatus(CustomerBO.STATUS_WAIT);
        UserBO userBO = weixinUtils.getUserInfo(customerID);
        customerBO.setNickName(userBO.getNickname());
        customerBO.setUserObject(userBO);
        ServerBO freeBO = null;
        Collection<ServerBO> collection = serverMap.values();
        for (ServerBO newServerBO : collection) {
            if (ServerBO.TYPE_ONLINE == newServerBO.getStatus()) {
                freeBO = newServerBO;
                break;
            }
        }
        if (null == freeBO) {
            weixinUtils.sendTextMessageByServer(customerBO.getCustomerID(), "非常报歉,客服中心暂时无响应");
        } else {
            customerMap.put(customerID, customerBO);
            customerBO.setStatus(CustomerBO.STATUS_WAIT);
            freeBO.setStatus(ServerBO.TYPE_SERVER_WATI);
            waitMap.put(freeBO, customerBO);
            waitCustomerMap.put(customerBO, freeBO);
            weixinUtils.sendTextMessageByServer(customerBO.getCustomerID(), "正在转接人工服务...请稍候");
            freeBO.setStatus(ServerBO.TYPE_SERVER_WATI);
            weixinUtils.sendTextMessageByServer(freeBO.getServerID(), customerBO.getNickName() + " 请求接入，是否接入？#3接受，#4 拒接");
        }
    }

    /**
     * 添加客服人员
     */
    public synchronized void addServer(String serverID) {
        ServerBO serverBO = new ServerBO();
        serverBO.setServerID(serverID);
        serverBO.setLastMessageTime(new Date());
        UserBO userBO = weixinUtils.getUserInfo(serverID);
        serverBO.setUserObject(userBO);
        serverBO.setStatus(ServerBO.TYPE_ONLINE);
        serverBO.setNickName(userBO.getNickname());
        if (null != serverNameMap.get(serverBO.getNickName())) {
            weixinUtils.sendTextMessageByServer(serverID, "已有相同昵称的客服人员登录,请修改昵称后重新登录");
            return;
        } else {
            serverMap.put(serverBO.getNickName(), serverBO);
            serverMap.put(serverID, serverBO);
            weixinUtils.sendTextMessageByServer(serverID, "成功能客服身份登录微信客服中心,操作:#1 在线,#2 离线,#3 接受,#4 拒接,#5 挂断,#6 客服昵称 转接,#7 退出");
        }
    }

    /**
     * 每20秒更新一下等待中的客户
     */
    public synchronized void changeWait() {
        logger.info("更新等待队列......");
        Collection<ServerBO> collection = serverMap.values();
        long nowTime = new Date().getTime();
        for (Map.Entry<ServerBO, CustomerBO> entry : linkServerMap.entrySet()) {
            CustomerBO customerBO = entry.getValue();
            ServerBO serverBO = waitCustomerMap.get(customerBO);
            if(null == serverBO){
                continue;
            }
            waitMap.remove(serverBO);
            waitCustomerMap.remove(customerBO);
            if (nowTime - customerBO.getLastMessageTime().getTime() > clearWait) {
                ServerBO freeBO = null;
                for (ServerBO newServerBO : collection) {
                    if (ServerBO.TYPE_ONLINE == newServerBO.getStatus()) {
                        freeBO = newServerBO;
                        break;
                    }
                }
                if (null == freeBO) {
                    customerMap.remove(customerBO.getCustomerID());
                    weixinUtils.sendTextMessageByServer(customerBO.getCustomerID(), "非常报歉,客服中心暂时无响应");
                } else {
                    freeBO.setStatus(ServerBO.TYPE_SERVER_WATI);
                    waitMap.put(freeBO, customerBO);
                    waitCustomerMap.put(customerBO, freeBO);
                    weixinUtils.sendTextMessageByServer(customerBO.getCustomerID(), "正在转接人工服务...请稍候");
                }
            }
        }
    }

    /**
     * 每2分钟
     */
    public synchronized void freeCustomer() {
        logger.info("清理长时间未响应客户......");
        long nowTime = new Date().getTime();
        Iterator<CustomerBO> iterator = customerMap.values().iterator();
        while (iterator.hasNext()) {
            CustomerBO customerBO = iterator.next();
            if (nowTime - customerBO.getLastMessageTime().getTime() > clearCustomer) {
                iterator.remove();
                weixinUtils.sendTextMessageByServer(customerBO.getCustomerID(), "长时间未操作与服务器断开");
                ServerBO serverBO = waitCustomerMap.get(customerBO);
                if (null != serverBO) {
                    waitCustomerMap.remove(serverBO);
                    waitMap.remove(serverBO);
                    serverBO.setStatus(ServerBO.TYPE_ONLINE);
                    weixinUtils.sendTextMessageByServer(serverBO.getServerID(), "客户取消等待,已断开");
                }
                serverBO = linkCustomerMap.get(customerBO);
                if (null != serverBO) {
                    linkCustomerMap.remove(customerBO);
                    linkServerMap.remove(serverBO);
                    serverBO.setStatus(ServerBO.TYPE_ONLINE);
                    weixinUtils.sendTextMessageByServer(serverBO.getServerID(), "客户长时间未操作与服务器断开");
                }
            }
        }
    }

    /**
     * 每3个小时清一下客服
     */
    public synchronized void freeServer() {
        logger.info("清理长时间未响应客服......");
        long nowTime = new Date().getTime();
        Iterator<ServerBO> iterator = serverMap.values().iterator();
        while (iterator.hasNext()) {
            ServerBO serverBO = iterator.next();
            if (nowTime - serverBO.getLastMessageTime().getTime() > clearServer) {
                iterator.remove();
                serverNameMap.remove(serverBO.getNickName());
                weixinUtils.sendTextMessageByServer(serverBO.getServerID(), "长时间未操作与服务器断开");
            }
        }
    }

    public synchronized Collection<CustomerBO> getCurrnetCustomer() {
        return customerMap.values();
    }

    public synchronized Collection<ServerBO> getCurrnetServer() {
        return serverMap.values();
    }

    public synchronized boolean isSendToServer(String customerID) {
        if (customerMap.containsKey(customerID)) {
            return true;
        } else {
            return serverMap.containsKey(customerID);
        }
    }

    public synchronized void reciveMessage(WeiXinMessageBO messageBO) {
        String content = messageBO.getContent();
        logger.info("reciveMessage " + content);
        ServerBO serverBO = serverMap.get(messageBO.getFromUserName());
        if (null != serverBO) {// 客服操作
            serverBO.setLastMessageTime(new Date());// 更新最后操作时间
            if ("#1".equals(content) && ServerBO.TYPE_OFFLINE == serverBO.getStatus()) {
                weixinUtils.sendTextMessageByServer(messageBO.getFromUserName(), "当前状态在线");
                return;
            } else if ("#2".equals(content) && ServerBO.TYPE_ONLINE == serverBO.getStatus()) {
                weixinUtils.sendTextMessageByServer(messageBO.getFromUserName(), "当前状态离线");
                return;
            } else if ("#3".equals(content)) {
                if (ServerBO.TYPE_SERVER_WATI != serverBO.getStatus()) {
                    weixinUtils.sendTextMessageByServer(serverBO.getServerID(), "当前没有未接收任务");
                    return;
                }
                CustomerBO customerBO = waitMap.get(serverBO);
                ServerBO oldServer = linkCustomerMap.get(customerBO);
                if (null != oldServer) {
                    oldServer.setStatus(ServerBO.TYPE_ONLINE);// 设置客服空闲
                    weixinUtils.sendTextMessageByServer(oldServer.getServerID(), "客户转接成功");
                }
                linkCustomerMap.put(customerBO, serverBO);
                linkServerMap.put(serverBO, customerBO);
                customerBO.setStatus(CustomerBO.STATUS_LINE);// 设置客户使用中
                serverBO.setStatus(ServerBO.TYPE_BUSY);// 设置客服忙碌中
                waitMap.remove(serverBO);
                waitCustomerMap.remove(customerBO);
                weixinUtils.sendTextMessageByServer(serverBO.getServerID(), "与客户: " + customerBO.getNickName() + " 连接成功.");
                weixinUtils.sendTextMessageByServer(customerBO.getCustomerID(),"与客服 "+serverBO.getNickName()+" 连接成功");
                return;
            } else if ("#4".equals(content)) {
                if (ServerBO.TYPE_SERVER_WATI == serverBO.getStatus()) {
                    weixinUtils.sendTextMessageByServer(serverBO.getServerID(), "当前没有未接收任务");
                    return;
                }
                // 为客户指定其它客服
                Collection<ServerBO> collection = serverMap.values();
                ServerBO freeBO = null;
                for (ServerBO newServerBO : collection) {
                    if (ServerBO.TYPE_ONLINE == newServerBO.getStatus()) {
                        freeBO = newServerBO;
                        break;
                    }
                }
                CustomerBO customerBO = waitMap.get(serverBO);
                customerBO.setLastMessageTime(new Date());
                waitCustomerMap.remove(customerBO);
                waitMap.remove(serverBO);
                if (null == freeBO) {
                    customerMap.remove(customerBO.getCustomerID());
                    weixinUtils.sendTextMessageByServer(customerBO.getCustomerID(), "非常报歉,客服中心暂时无响应,请稍后重试");
                } else {
                    waitMap.put(freeBO, customerBO);
                    waitCustomerMap.put(customerBO, freeBO);
                    freeBO.setStatus(ServerBO.TYPE_SERVER_WATI);
                    weixinUtils.sendTextMessageByServer(freeBO.getServerID(), "是否响应当前客户请求?回复: #1 接受,#2 拒接");
                }
                serverBO.setStatus(ServerBO.TYPE_ONLINE);// 设置客服空闲
                weixinUtils.sendTextMessageByServer(serverBO.getServerID(), "已拒绝客户清求");
                return;
            } else if ("#5".equals(content)) {
                if (ServerBO.TYPE_BUSY == serverBO.getStatus()) {
                    serverBO.setStatus(ServerBO.TYPE_ONLINE);
                    weixinUtils.sendTextMessageByServer(serverBO.getServerID(), "已与客户断开连接");
                    CustomerBO customerBO = linkServerMap.get(serverBO);
                    weixinUtils.sendTextMessageByServer(customerBO.getCustomerID(), "已与人工客服断开连接");
                } else {
                    weixinUtils.sendTextMessageByServer(serverBO.getServerID(), "当前没有连接中的客户");
                }
                return;
            } else if (content.length() > 4 && "#6".equals(content.substring(0, 2))) {// 转接
                if (ServerBO.TYPE_BUSY == serverBO.getStatus()) {
                    String serverName = content.substring(2);
                    ServerBO newServerBO = serverNameMap.get(serverName);
                    if (ServerBO.TYPE_ONLINE == newServerBO.getStatus()) {
                        CustomerBO customerBO = linkServerMap.get(serverBO);
                        newServerBO.setStatus(ServerBO.TYPE_SERVER_WATI);
                        weixinUtils.sendTextMessageByServer(newServerBO.getServerID(), "是否响应当" + serverBO.getNickName() + "转发来自" + customerBO.getNickName() + "的请求?回复: #1 接受,#2 拒接");
                    } else {
                        weixinUtils.sendTextMessageByServer(serverBO.getServerID(), "请求转接失败,该客服暂时无法接受新任务");
                    }
                } else {
                    weixinUtils.sendTextMessageByServer(serverBO.getServerID(), "当前没有连接中的客户");
                }
                return;
            } else if ("#7".equals(content) && ServerBO.TYPE_ONLINE == serverBO.getStatus()) {
                removeServer(serverBO.getServerID());
            } else {
                if (ServerBO.TYPE_BUSY == serverBO.getStatus()) {
                    CustomerBO customerBO = linkServerMap.get(serverBO);
                    weixinUtils.sendTextMessageByServer(customerBO.getCustomerID(), messageBO.getContent());
                } else {
                    weixinUtils.sendTextMessageByServer(serverBO.getServerID(), "未知的命令");
                }
                return;
            }
        } else {// 发送消息给客服
            CustomerBO customerBO = customerMap.get(messageBO.getFromUserName());
            customerBO.setLastMessageTime(new Date());// 更新最后操作时间
            serverBO = linkCustomerMap.get(customerBO);
            if (null != serverBO) {
                weixinUtils.sendTextMessageByServer(serverBO.getServerID(), content);
            } else {
                weixinUtils.sendTextMessageByServer(customerBO.getCustomerID(), "正在为您连线客服人员,请稍候....");
            }
        }
    }

    public synchronized void removeCustomer(String openID) {
        if(serverMap.containsKey(openID)){
            weixinUtils.sendTextMessageByServer(openID, "不要挑衅我！！！");
            return;
        }
        CustomerBO customerBO = customerMap.get(openID);
        if (null != customerBO) {
            customerMap.remove(openID);
            ServerBO serverBO = waitCustomerMap.get(customerBO);
            if (null != serverBO) {
                waitCustomerMap.remove(serverBO);
                waitMap.remove(serverBO);
            }
            serverBO = linkCustomerMap.get(customerBO);
            if (null != serverBO) {
                linkCustomerMap.remove(customerBO);
                linkServerMap.remove(serverBO);
                serverBO.setStatus(ServerBO.TYPE_ONLINE);
                weixinUtils.sendTextMessageByServer(serverBO.getServerID(), "客户与断开人工服务");
            }
            weixinUtils.sendTextMessageByServer(customerBO.getCustomerID(), "已断开人工服务");
        }
    }

    public synchronized void removeServer(String openID) {
        ServerBO serverBO = serverMap.get(openID);
        if (null != serverBO) {
            serverMap.remove(openID);
            serverNameMap.remove(serverBO.getNickName());
            weixinUtils.sendTextMessageByServer(serverBO.getServerID(), "与服务器已断开");
        }
    }

    public void setClearCustomer(long clearCustomer) {
        this.clearCustomer = clearCustomer;
    }

    public void setClearServer(long clearServer) {
        this.clearServer = clearServer;
    }

    public void setClearWait(long clearWait) {
        this.clearWait = clearWait;
    }

    public void setWeixinUtils(CommonWeixinUtils weixinUtils) {
        this.weixinUtils = weixinUtils;
    }
}
