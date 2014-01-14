package com.henglu.summer.control;

import java.util.Hashtable;
import java.util.Map;

/**
 * 控制器实例
 */
public abstract class BaseControl implements IControl {
	protected Map<String, Object> context = new Hashtable<String, Object>();

	public Map<String, Object> getContext() {
		return context;
	}

}
