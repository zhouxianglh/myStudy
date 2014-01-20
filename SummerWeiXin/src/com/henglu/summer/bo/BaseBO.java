package com.henglu.summer.bo;

import java.io.Serializable;

/**
 * 基础的BO对象,userObject 用来存放设定以外的值
 */
public abstract class BaseBO implements Serializable {
    private static final long serialVersionUID = 3176480568283999517L;
    protected Object userObject;

    public Object getUserObject() {
        return userObject;
    }

    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }
}
