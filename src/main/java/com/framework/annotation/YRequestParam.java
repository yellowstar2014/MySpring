package com.framework.annotation;

import java.lang.annotation.*;

/**
 * My write
 *
 * @author yellow
 * @date 2019/9/01 11:36
 * 温馨提醒:
 * 代码千万行，
 * 注释第一行。
 * 命名不规范，
 * 同事两行泪。
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YRequestParam {
    String value() default "";
}
