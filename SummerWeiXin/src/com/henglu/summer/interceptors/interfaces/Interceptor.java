package com.henglu.summer.interceptors.interfaces;

import com.henglu.summer.control.IControl;

/**
 * 拦截器接口
 * @author zhouxianglh@gmail.com
 * @version 1.0  2014-1-20 下午10:20:32
 */
public interface Interceptor extends IControl {
    /**
     * 设置控制器
     */
    public void setControl(IControl control);
}
