package com.henglu.study;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 用来从 http://wallbase.cc/toplist 下载壁纸图片
 * 
 * @author zhouxiang@gmail.com
 * @version 1.0 2014-1-15 下午1:50:47
 */
public class GetImage {
    public static final Logger logger = Logger.getLogger(GetImage.class);

    public static void close(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 向指定URL发送GET方法的请求(这里只使用String流文件接收)
     */
    public static void getImage(CloseableHttpResponse httpResponse, File file) throws IOException {
        OutputStream outputStream = new FileOutputStream(file);
        try {
            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream stream = httpEntity.getContent();
            byte[] byteArr = new byte[10240];
            int i = 0;
            while ((i = stream.read(byteArr)) != -1) {
                outputStream.write(byteArr, 0, i);
            }
        } finally {
            close(outputStream);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            sendGet(20);
            logger.info("下载完成.........");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Set<String> readHTML(String html) {
        Set<String> set = new TreeSet<String>();
        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByAttribute("data-id");
        for (Element element : elements) {
            set.add(element.attr("data-id"));
        }
        return set;
    }

    /**
     * 向指定URL发送GET方法的请求(这里只使用String流文件接收)
     * 
     * @return
     */
    public static void sendGet(int pageSize) throws IOException {
        CloseableHttpClient httpclient = null;
        try {
            httpclient = HttpClients.createDefault();
            // 首页
            HttpGet httpGet = new HttpGet("http://wallbase.cc/user/login");
            CloseableHttpResponse httpResponse = httpclient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            String html = EntityUtils.toString(httpEntity);
            Document document = Jsoup.parse(html);
            Elements elements = document.getElementsByAttributeValue("name", "csrf");
            String csrf = elements.get(0).attr("value");
            elements = document.getElementsByAttributeValue("name", "ref");
            String ref = elements.get(0).attr("value");
            close(httpResponse);
            logger.info("成功进入登录页....");
            // 提交 登录
            HttpPost httpPost = new HttpPost("http://wallbase.cc/user/do_login");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("csrf", csrf));
            nvps.add(new BasicNameValuePair("ref", ref));
            nvps.add(new BasicNameValuePair("username", "zhouxianglh"));
            nvps.add(new BasicNameValuePair("password", "Thinking"));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            httpResponse = httpclient.execute(httpPost);
            httpEntity = httpResponse.getEntity();
            close(httpResponse);
            logger.info("成功登录....");
            // 获取精彩图片
            int i = 0;
            while (i++ < pageSize) {
                logger.info("开始获取第 " + i + " 页图片");
                httpGet = new HttpGet("http://wallbase.cc/toplist/index/" + (i * 60));
                httpResponse = httpclient.execute(httpGet);
                httpEntity = httpResponse.getEntity();
                html = EntityUtils.toString(httpEntity);
                close(httpResponse);
                close(httpResponse);
                Set<String> set = readHTML(html);
                for (String string : set) {
                    String impagPath = "http://wallpapers.wallbase.cc/rozne/wallpaper-" + string + ".jpg";
                    File file = new File("D:\\temp\\NSFW\\" + string + ".jpg");
                    if (file.exists()) {
                        logger.info(file.getPath() + " 已存在,不作下载");
                    } else {
                        httpGet = new HttpGet(impagPath);
                        httpResponse = httpclient.execute(httpGet);
                        getImage(httpResponse, file);
                    }
                    close(httpResponse);
                    logger.info("下载完成  " + impagPath);
                }
            }
        } finally {
            close(httpclient);
        }
    }

    public static String StringConver(String str, String newCharsetName) {
        try {
            return URLDecoder.decode(str, newCharsetName);
        } catch (UnsupportedEncodingException e) {
            logger.error("字符转换出错", e);
            return "";
        }
    }
}
