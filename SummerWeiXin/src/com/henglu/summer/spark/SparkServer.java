package com.henglu.summer.spark;

import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.util.Blowfish;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.henglu.summer.bo.SparkBO;
import com.henglu.summer.spark.interfaces.IMessageToSpakHandle;
import com.henglu.summer.spark.interfaces.IMessageToSpark;
import com.henglu.summer.utils.CommonUtils;

/**
 * Spark的人工客服中心操作类
 * 1 admin 做为管理员,所以admin加为好友的用户视为客服,用户处于"空闲"状态,视为可连接,其它视为不可连接
 * 2 启动后调用 init 方法,初始化后登录admin,所有在线处于"空闲"状态的好友用户视在线客服
 * 3 从在线客服中,取连接数最少的客服响应新的人工请求
 * 4 人工服务接入时,根据昵称(客服接口获取)加上OpenID的后5位,得到唯一用户名(如不存在则新建),作为临时接入人员用户名
 * 5 强行断开长时间未操作的连接,由定时任务调用
 * @author zhouxianglh@gmail.com
 * @version 1.0  2014-1-20 下午10:20:32
 */
public class SparkServer implements IMessageToSpark {
    private static Logger logger = Logger.getLogger(SparkServer.class);
    private static final String default_password_key = "zhouxianglh";// 默认密码
    private String host;
    private IMessageToSpakHandle handle;
    private JdbcTemplate jdbcTemplate;// Spring 的JDBC操作类
    private Map<String, SparkBO> map = new Hashtable<String, SparkBO>();
    private SparkBO adminSparkBO;
    public Map<String, Integer> serverMap = new Hashtable<String, Integer>();
    private static SparkServer sparkServer = new SparkServer();

    public static SparkServer getInstance() {
        return sparkServer;
    }

    private Blowfish blowfish = new Blowfish("7l4edvAdfom182T");// 用于对Spark的密码进行加密和解密

    private SparkServer() {
    }

    @Override
    public synchronized void customerDisconnection(String openID) {
        SparkBO sparkBO = map.get(openID);
        if (null != sparkBO) {
            try {
                sparkBO.getChat().sendMessage("用户主动断开了连接");
                sparkBO.getConn().disconnect();
                if (null != serverMap.get(sparkBO.getServer())) {
                    serverMap.put(sparkBO.getServer(), serverMap.get(sparkBO.getServer()) - 1);
                }
            } catch (XMPPException e) {
                logger.error("用户断开出错", e);
            }
        }
        map.remove(openID);
    }

    /**
     * 初始化
     */
    public synchronized void init() {
        try {
            // admin登录
            adminSparkBO = new SparkBO();
            adminSparkBO.setConn(getConn("admin", "admin", "Thinking"));
            adminSparkBO.setUserName("admin");
            // 获取当前在线客服信息
            Roster roster = adminSparkBO.getConn().getRoster();
            Thread.sleep(1000);// 这里要暂停下,不然可能因为延迟原因无法获取当前在线好友
            Collection<RosterEntry> collection = roster.getEntries();
            for (RosterEntry rosterEntry : collection) {// 获取状态为在线的好友
                Presence presence = roster.getPresence(rosterEntry.getUser());
                if ("空闲".equals(presence.getStatus())) {
                    serverMap.put(presence.getFrom(), 0);
                }
            }
            // 客服信息变更记录
            roster.addRosterListener(new RosterListener() {
                @Override
                public void entriesAdded(Collection<String> arg0) {
                }

                @Override
                public void entriesDeleted(Collection<String> arg0) {
                }

                @Override
                public void entriesUpdated(Collection<String> arg0) {
                }

                @Override
                public void presenceChanged(Presence presence) {
                    serverStatusChange(presence);
                }
            });
        } catch (XMPPException e) {
            logger.error("初始化Spark出错", e);
        } catch (InterruptedException e) {
            logger.error("初始化Spark出错", e);
        }
    }

    @Override
    public boolean sendMessageToSpark(final String openID, String openName, String content) throws Exception {
        SparkBO sparkBO = getSpark(openID, openName);
        if (null == sparkBO) {
            return false;
        } else {
            sparkBO.getChat().sendMessage(content);
            return true;
        }
    }

    public void setHandle(IMessageToSpakHandle handle) {
        this.handle = handle;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private synchronized SparkBO createSpark(final String openID, String openName) throws XMPPException {
        SparkBO sparkBO = new SparkBO();
        sparkBO.setUserName(openID);
        String server = getServer();
        if (null == server) {// 如果没有在线客服
            return null;
        }
        Connection conn = getConn(openID, openName, default_password_key);
        serverMap.put(server, serverMap.get(server) + 1);
        Chat chat = conn.getChatManager().createChat(server, new MessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
                try {
                    handle.sendMessageToWeiXin(openID, message.getBody());
                } catch (Exception e) {
                    logger.error("客服回复消息失败", e);
                }
            }
        });
        sparkBO.setConn(conn);
        sparkBO.setChat(chat);
        sparkBO.setServer(server);
        return sparkBO;
    }

    /**
     * 获取Spark连接
     * @param userName Spark用户名或OpenID
     * @param openName 昵称
     * @param password 密码
     */
    private synchronized Connection getConn(String userName, String openName, String password) throws XMPPException {
        if (userName.length() > 10) {
            userName = CommonUtils.joinString(openName.replace(" ", "_"), "-",
                    userName.substring(userName.length() - 5));
        }
        SqlRowSet rs = jdbcTemplate
                .queryForRowSet("select userName as rowCount from ofuser where userName=?", userName);
        if (!rs.next()) {// 没有查询结果就是没用用户
            String nowTime = "00" + new Date().getTime();
            jdbcTemplate.update("insert into ofuser values(?,null,?,?,'xpp@henglu.com',?,'0')", userName,
                    blowfish.encryptString(default_password_key), openName, nowTime);
        }
        Connection conn = new XMPPConnection(host);
        conn.connect();
        conn.login(userName, password);
        return conn;
    }

    /**
     * 获取当前任务最少的客服
     */
    private synchronized String getServer() {
        String server = null;
        int minConnection = Integer.MAX_VALUE;
        for (Map.Entry<String, Integer> entry : serverMap.entrySet()) {
            if (minConnection > entry.getValue()) {
                server = entry.getKey();
                minConnection = entry.getValue();
            }
        }
        return server;
    }

    /**
     * 获取 SparkBO 对象 (返回空则为建立连接错误)
     * @param openID 微信OpenID
     * @param openName 昵称
     */
    private synchronized SparkBO getSpark(String openID, String openName) throws XMPPException {
        SparkBO sparkBO = map.get(openID);
        if (null == sparkBO) {// 如果未建立连接
            sparkBO = createSpark(openID, openName);
            if (null != sparkBO) {
                map.put(openID, sparkBO);
            }
        }
        return sparkBO;
    }

    /**
     * 客服状态变更
     */
    private synchronized void serverStatusChange(Presence presence) {
        String status = presence.getStatus();
        String server = presence.getFrom();
        if (null == status) {// 下线,断开关联用户
            Iterator<Entry<String, SparkBO>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, SparkBO> entry = iterator.next();
                if (server.equals(entry.getValue().getServer())) {
                    try {
                        handle.serverDisconnection(entry.getKey());
                    } catch (Exception e) {
                        logger.error("客服断开连接出错", e);
                    }
                    iterator.remove();
                }
            }
            serverMap.remove(server);
        } else if ("空闲".equals(status)) {// 可以接入
            serverMap.put(server, 0);
        } else {// 不可以接入
            serverMap.remove(server);
        }
    }
}
