package com.syan.smart_park.common.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 标注在 Controller 方法上，要求当前用户拥有指定权限编码才能访问
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 权限编码，如 "system:user:create"
     */
    String[] value();

    /**
     * 逻辑关系：AND 表示必须拥有所有权限，OR 表示拥有任一即可
     */
    Logical logical() default Logical.OR;

    enum Logical {
        AND, OR
    }
}
