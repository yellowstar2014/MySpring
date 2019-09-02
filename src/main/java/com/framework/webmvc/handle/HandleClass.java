package com.framework.webmvc.handle;

import java.lang.reflect.Method;

/**
 * @author yellow
 * @date 2019/9/01 13:36
 * 温馨提醒:
 * 代码千万行，
 * 注释第一行。
 * 命名不规范，
 * 同事两行泪。
 */
public class HandleClass {
    Object controller;
    Method method;
    String methodUrl;

    public HandleClass(Object controller, Method method, String methodUrl) {
        this.controller = controller;
        this.method = method;
        this.methodUrl = methodUrl;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getMethodUrl() {
        return methodUrl;
    }

    public void setMethodUrl(String methodUrl) {
        this.methodUrl = methodUrl;
    }
}
