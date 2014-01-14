package com.henglu.summer.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class CommonUtils {
	private static Logger logger = Logger.getLogger(CommonUtils.class);
	private final static String[] enSymbol = { "-", "<", ">", "\"", "\"", "\"", "\"", ",", ",", ".", "?" };
	private final static String[] cnSymbol = { "—", "〈", "〉", "“", "”", "‘", "’", "、", "，", "。", "？" };

	public static void close(Closeable closeable) {
		if (null != closeable) {
			try {
				closeable.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * sha-1加密
	 */
	public static String encrypt(String inputText) {
		try {
			MessageDigest m = MessageDigest.getInstance("sha-1");
			m.update(inputText.getBytes("UTF8"));
			byte s[] = m.digest();
			return hex(s);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("sha-1 加密错误", e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("sha-1 加密错误", e);
		}
	}

	/**
	 * 返回十六进制字符串
	 */
	private static String hex(byte[] arr) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arr.length; ++i) {
			sb.append(Integer.toHexString((arr[i] & 0xFF) | 0x100).substring(1, 3));
		}
		return sb.toString();
	}

	/**
	 * 连接字符串
	 */
	public static String joinString(Object... objArr) {
		StringBuffer sf = new StringBuffer();
		for (Object object : objArr) {
			if (null != object) {
				sf.append(object);
			}
		}
		return sf.toString();
	}

	/**
	 * 连接字符串
	 */
	public static String joinString(String... objArr) {
		StringBuffer sf = new StringBuffer();
		for (Object object : objArr) {
			if (null != object) {
				sf.append(object);
			}
		}
		return sf.toString();
	}

	/**
	 * 向指定URL发送GET方法的请求(这里只使用String流文件接收)
	 */
	public static String sendGet(String url) throws IOException {
		CloseableHttpClient httpclient = null;
		CloseableHttpResponse httpResponse = null;
		try {
			httpclient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(url);
			httpResponse = httpclient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			return EntityUtils.toString(httpEntity);
		} finally {
			close(httpResponse);
			close(httpclient);
		}
	}

	public static void sendPost(String url, String str) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = null;
		try {
			httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(url);
			StringEntity entity = new StringEntity(str);
			httpPost.setEntity(entity);
			CloseableHttpResponse httpResponse = httpclient.execute(httpPost);
			logger.info(httpResponse.getStatusLine());
			HttpEntity httpEntity = httpResponse.getEntity();
			System.out.println(EntityUtils.toString(httpEntity));
			EntityUtils.consume(httpEntity);
		} finally {
			close(httpclient);
		}

	}

	/**
	 * 全角字符->半角字符转换
	 */
	public static String StringConver(String str) {
		char[] c = str.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375) {
				c[i] = (char) (c[i] - 65248);
			}
		}
		return new String(c);
	}

	public static String StringConver(String str, String oldCharsetName, String newCharsetName) {
		try {
			return new String(str.getBytes(oldCharsetName), newCharsetName);
		} catch (UnsupportedEncodingException e) {
			logger.error("字符转换出错", e);
			return "";
		}
	}

	/**
	 * 中文符号转英文符号
	 */
	public static String StringSymbol(String str) {
		for (int i = 0, j = cnSymbol.length; i < j; i++) {
			str = str.replaceAll(cnSymbol[i], enSymbol[i]);
		}
		return str;
	}

	/**
	 * 将Json对象转换成Map
	 */
	public static String toJson(Object obj) {
		JSONObject jsonObject = JSONObject.fromObject(obj);
		return jsonObject.toString();
	}

	/**
	 * 将Json对象转换成Map
	 */
	public static Map<String, String> toMap(String jsonString) {
		Map<String, String> result = new HashMap<String, String>();
		JSONObject jsonObject = JSONObject.fromObject(jsonString);
		for (Object obj : jsonObject.keySet()) {
			String key = obj.toString();
			result.put(key, jsonObject.getString(key));
		}
		return result;
	}
}
