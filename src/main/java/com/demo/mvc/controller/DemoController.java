package com.demo.mvc.controller;

import com.demo.mvc.service.IDemoService;
import com.framework.annotation.YAutowired;
import com.framework.annotation.YController;
import com.framework.annotation.YRequestMapping;
import com.framework.annotation.YRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * test
 *
 * @author yellow
 * @date 2019/9/01 11:28
 * 温馨提醒:
 * 代码千万行，
 * 注释第一行。
 * 命名不规范，
 * 同事两行泪。
 */
@YController
@YRequestMapping("/demo")
public class DemoController {

    @YAutowired
    private IDemoService demoService;

    @YRequestMapping("/queryName")
    public  void  queryName(@YRequestParam("name") String name,HttpServletRequest req, HttpServletResponse resp){
        String result = "error";
        if (null!=demoService) result = demoService.getName(name);

        try {
            System.out.println("######result="+result);
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @YRequestMapping("/test")
    public  void  test(HttpServletRequest req, HttpServletResponse resp){
        try {
            resp.getWriter().write("test test");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @YRequestMapping("/hello")
    public  void  hello(@YRequestParam("name") String name,@YRequestParam("age") String age,HttpServletRequest req, HttpServletResponse resp){
        String result = "age:"+age+"name:";
        if (null!=demoService) result += demoService.getName(name);

        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
