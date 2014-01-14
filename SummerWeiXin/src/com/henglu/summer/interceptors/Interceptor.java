package com.henglu.summer.interceptors;

import com.henglu.summer.control.IControl;

/**
 * 拦截器
 */
public interface Interceptor extends IControl {
	public void setControl(IControl control);
}
