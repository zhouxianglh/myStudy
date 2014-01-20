package com.henglu.summer.bo;

/**
 * 根据客服接口获取的用户信息
 */
public class UserBO extends BaseBO {
    private static final long serialVersionUID = 292914876240375848L;
    public static final int SEX_NULL = 0;
    public static final int SEX_MALE = 1;
    public static final int SEX_FEMALE = 2;
    private int subscribe;
    private String openid;
    private String nickname;
    private int sex;
    private String language;
    private String city;
    private String country;
    private String headimgurl;
    private long subscribe_time;
    private String province;

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public String getLanguage() {
        return language;
    }

    public String getNickname() {
        return nickname;
    }

    public String getOpenid() {
        return openid;
    }

    public String getProvince() {
        return province;
    }

    public int getSex() {
        return sex;
    }

    public int getSubscribe() {
        return subscribe;
    }

    public long getSubscribe_time() {
        return subscribe_time;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public void setSubscribe(int subscribe) {
        this.subscribe = subscribe;
    }

    public void setSubscribe_time(long subscribe_time) {
        this.subscribe_time = subscribe_time;
    }
}
