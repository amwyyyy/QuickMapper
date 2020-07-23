package com.wen.mapper.orm.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 需要级联时用此注解,一对一，一对多均可
 * 
 * @author denis.huang
 * @since 2017年2月15日
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface Association {
	/**
	 * 关联表的列名
	 */
	String columnName() default "";

	/**
	 * 关联列的数据类型
	 */
	String jdbcType() default "";

	/**
	 * 查询的statment
	 */
	String select();

	/**
	 * 是否懒加载
	 */
	boolean lazy() default false;

	String remark() default "";
}
