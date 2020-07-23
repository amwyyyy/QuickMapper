package com.wen.mapper.dataSource;

import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * 数据源设置的切面,用于根据方法的注解设置数据源
 * 
 * @author denis.huang
 * @since 2017年2月15日
 */
public class DataSourceAspect implements MethodBeforeAdvice {
	@Override
	public void before(Method method, Object[] args, Object target) throws Throwable {
		if (method != null) {
			if (method.isAnnotationPresent(Master.class)) {
				DynamicDataSourceHolder.putDataSource("master");
			} else if (method.isAnnotationPresent(Slave.class)) {
				DynamicDataSourceHolder.putDataSource("slave");
			}
		}
	}
}
