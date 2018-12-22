package com.msjf.finance.cas.common.dao;

@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
@java.lang.annotation.Documented
@org.springframework.stereotype.Component
/**
 * Dao接口公共扫描控件
 */
public @interface MyBatisDao {
    java.lang.String value() default "";
}