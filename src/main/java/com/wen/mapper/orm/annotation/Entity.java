package com.wen.mapper.orm.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 实体注解，与数据表对应
 * 
 * @author denis.huang
 * @since 2017年2月15日
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Entity {
	/**
	 * 表名
	 */
	String tableName() default "";

	/**
	 * 是否自动生成主键
	 */
	boolean useGeneratedKeys() default true;

	/**
	 * 命名空间
	 */
	String namespace() default "";

	String remark() default "";
}
