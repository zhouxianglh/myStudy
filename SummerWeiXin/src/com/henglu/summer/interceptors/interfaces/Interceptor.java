package com.henglu.summer.interceptors.interfaces;

import com.henglu.summer.control.IControl;

/**
 * 拦截器接口
 * @author zhouxianglh@gmail.com
 */
public interface Interceptor extends IControl {
    /**
     * 设置控制器
     */
    public void setControl(IControl control);
}
