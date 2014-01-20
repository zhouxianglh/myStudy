package com.henglu.summer.control;

import java.util.Hashtable;
import java.util.Map;

/**
 * 控制器实例
 * @author zhouxianglh@gmail.com
 * @version 1.0  2014-1-20 下午10:20:32
 */
public abstract class BaseControl implements IControl {
    protected Map<String, Object> context = new Hashtable<String, Object>();

    @Override
    public Map<String, Object> getContext() {
        return context;
    }
}
