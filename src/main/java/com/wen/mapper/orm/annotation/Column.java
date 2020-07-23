package com.wen.mapper.orm.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 字段注解，与数据库字段一一对应
 * 
 * @author denis.huang
 * @since 2017年2月15日
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface Column {

	/**
	 * 对应列名
	 */
	String name() default "";

	/**
	 * 数据类型
	 */
	String jdbcType() default "";

	/**
	 * 是否主键字段
	 * 
	 * @return
	 */
	boolean pk() default false;

	/**
	 * 能否为空
	 */
	boolean nullable() default true;

	/**
	 * 插入语句是否包含该字段
	 */
	boolean insertable() default true;

	/**
	 * 更新语句是否包含该字段
	 */
	boolean updatable() default true;

	/**
	 * 字段长度
	 */
	int length() default 255;

	/**
	 * 数字类型字段，长度
	 */
	int precision() default 0;

	/**
	 * 数字类型字段，小数位数
	 */
	int scale() default 0;

	String remark() default "";

	String sql() default "";
}