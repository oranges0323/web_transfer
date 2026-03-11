package com.oranges.fileuploaderbackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
// 定义一个自定义注解 AuthCheck
@Target({ElementType.METHOD}) // 指定该注解可以应用于方法
@Retention(RetentionPolicy.RUNTIME) // 指定该注解在运行时可用
public @interface AuthCheck {
    // 定义一个字符串类型的属性 mustRole，表示必须有的角色
    // 如果使用时没有提供值，则默认为空字符串
    String mustRole() default "";
}
