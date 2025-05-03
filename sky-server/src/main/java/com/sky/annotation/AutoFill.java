package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.*;

/**
 * 自定义注解，用于标识某个方法需要进行功能字段自动填充处理
 * @author evan
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //数据库操作类型：UPDATE INSERT -->自定义的OperationType类中声明了UPDATE INSERT方法名
    OperationType value();
}
