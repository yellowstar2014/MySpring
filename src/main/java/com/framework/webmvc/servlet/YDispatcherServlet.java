package com.framework.webmvc.servlet;


import com.framework.annotation.YAutowired;
import com.framework.annotation.YController;
import com.framework.annotation.YRequestMapping;
import com.framework.annotation.YService;
import com.framework.webmvc.handle.HandleClass;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * My write DispatcherServlet
 *
 * @author yellow
 * @date 2019/9/01 11:05
 * 温馨提醒:
 * 代码千万行，
 * 注释第一行。
 * 命名不规范，
 * 同事两行泪。
 */
public class YDispatcherServlet  extends HttpServlet{

    private Properties contextConfig = new Properties();//读取属性文件

    private List<String> classNames = new ArrayList<String>();

    private Map<String,Object> ioc = new HashMap<String, Object>();//ioc容器

    private List<HandleClass> haddleMapper = new ArrayList<HandleClass>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            doDispath(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception");
        }
    }

    private void doDispath(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath,"").replaceAll("/+","/");
        HandleClass handleClass = getHaddleClass(url);
            if (null != handleClass){
                Method method = handleClass.getMethod();
                method.setAccessible(true);
                Class<?>[] paramTypes = method.getParameterTypes();
                Object[] paramValues = new Object[paramTypes.length];

                Map<String, String[]> params = req.getParameterMap();
                int index = 0;
                for (Map.Entry<String,String[]> param : params.entrySet()){
                paramValues[index++] = (Object) param.getValue()[0];
                 }
//                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
//                for (Annotation[] annotations : parameterAnnotations) {
//                    for (Annotation annotation : annotations) {annotation.annotationType().
//                        //获取注解名
//                        String name = annotation.annotationType().getSimpleName();
//                    }
//                }
                paramValues[index++] = req;
                paramValues[index] = resp;
                method.invoke(handleClass.getController(),paramValues);
            }
            else {
                resp.getWriter().write("404 not find");

        }

    }

    /**
     * 根据requestURI 获取 HandleClass
     * @param requestURI
     * @return
     */
    private HandleClass getHaddleClass(String requestURI){

        Iterator<HandleClass> iterator = haddleMapper.iterator();
        while (iterator.hasNext()){
            HandleClass handleClass =  iterator.next();
            if (requestURI.equals(handleClass.getMethodUrl())) return handleClass;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        //1、加载配置文件
        doLoadConfigFile(config.getInitParameter("contextConfigLocation"));

        //2、扫描所有被注解标注的类
        doScannerClass(contextConfig.getProperty("scannerPackage"));

        //3、初始化所有相关的类
        doInstane();

        //4、注入相关对象
        doAutowired();

        //5、初始化HandlerMapping,属于SpringMVC
        initHandlerMapping();

        System.out.println("Spring init ok ...");
    }

    private void initHandlerMapping() {
        if (ioc.isEmpty())return;
        for (Map.Entry<String,Object> entry : ioc.entrySet()){

            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(YController.class))continue;//该类不是controller类

            String baseUrl = "";
            if (clazz.isAnnotationPresent(YRequestMapping.class)){//是controller类，则获取映射的url
                YRequestMapping requestMapping = clazz.getAnnotation(YRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //扫描controller类下所有的公共方法,获取映射的rul
            for (Method method : clazz.getMethods()){
                if (!method.isAnnotationPresent(YRequestMapping.class))continue;
                YRequestMapping requestMapping = method.getAnnotation(YRequestMapping.class);
                String methodUrl =("/" + baseUrl + requestMapping.value()).replaceAll("/+", "/");
                haddleMapper.add(new HandleClass(entry.getValue(),method,methodUrl));
                System.out.println("Mapping:" + methodUrl + ",method:" + method);
            }


        }
    }

    /**
     * 注入相关对象
     */
    private void doAutowired() {
        if (ioc.isEmpty())return;

        //循环IOC容器中所有类，对需要赋值的属性进行赋值
        for (Map.Entry<String,Object> entry : ioc.entrySet()){
            //依赖注入
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields){
                if (!field.isAnnotationPresent(YAutowired.class))continue;//没有YAutowired注解，不赋值
                YAutowired autowired = field.getAnnotation(YAutowired.class);
                String beanName = autowired.value().trim();
                if ("".equals(beanName)){
                    beanName = field.getType().getName();
                }

                //暴力访问
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }

            }
        }
    }

    /**
     * 初始化所有相关的类
     */
    private void doInstane() {
        if (classNames.isEmpty())return;

        try {
                for (String className : classNames) {
                    Class<?> clazz = null;
                    clazz = Class.forName(className);
                    //不是所有的类都要实例化，只实例化加了注解的类
                    if (clazz.isAnnotationPresent(YController.class)) {

                        //key类名首字母小写
                        String beanName = lowerFirstCase(clazz.getName());
                        ioc.put(beanName,clazz.newInstance());//放入ioc容器



                    } else if (clazz.isAnnotationPresent(YService.class)) {

                        //1、如果是自定义了service名字，则优先使用自动义的名字
                        YService service = clazz.getAnnotation(YService.class);
                       String beanName = service.value();
                       if ("".equals(beanName.trim())){ //2、否则采用类名首字母小写
                            beanName = lowerFirstCase(clazz.getName());
                       }

                       Object instance = clazz.newInstance();
                        ioc.put(beanName,instance);//放入ioc容器
                        //3、根据接口类型来赋值
                        for (Class<?> i: clazz.getInterfaces()){
                           ioc.put(i.getName(),instance);
                        }
                    }
                    else {
                        continue;
                    }
                }
        } catch(ClassNotFoundException e){
             e.printStackTrace();
        }
       catch (IllegalAccessException e) {
             e.printStackTrace();
        } catch (InstantiationException e) {
             e.printStackTrace();
        }
    }

    /**
     * 返回 首字母小写的字符串
     * @param str
     * @return
     */
    private  String lowerFirstCase(String str){
        char[] chars = str.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);

    }

    /**
     * 扫描所有被注解标注的类
     * @param scannerPackage
     */
    private void doScannerClass(String scannerPackage) {
        //得到文件夹路径
        URL url = this.getClass().getClassLoader().getResource("/"+scannerPackage.replaceAll("\\.","/"));
        System.out.println("@@@@@@@scanner url:"+url);
        File classDir = new File(url.getFile());//得到文件夹
        for (File file:classDir.listFiles()){//开始遍历文件夹内的文件

            if (file.isDirectory()){//如果是文件夹
                doScannerClass(scannerPackage + "." + file.getName());//递归继续扫描文件夹里面的文件
            }else {
                String className = scannerPackage + "." + file.getName().replace(".class","");//得到完整的类名，不含后缀
                classNames.add(className);//把类放入classNames
            }

        }

    }

    /**
     * 加载配置文件
     * @param contextConfigLocation
     */
    private void doLoadConfigFile(String contextConfigLocation){
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);

        try {
            contextConfig.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (null != inputStream){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
