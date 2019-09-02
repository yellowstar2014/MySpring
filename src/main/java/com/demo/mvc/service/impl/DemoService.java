package com.demo.mvc.service.impl;

import com.demo.mvc.service.IDemoService;
import com.framework.annotation.YService;

/**
 * @author yellow
 * @date 2019/9/01 13:53
 * 温馨提醒:
 * 代码千万行，
 * 注释第一行。
 * 命名不规范，
 * 同事两行泪。
 */
@YService
public class DemoService implements IDemoService {

    @Override
    public String getName(String name) {
        return "hi, wellcome "+name+"!";
    }
}
