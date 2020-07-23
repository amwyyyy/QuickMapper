package com.wen.mapper.dataSource;

/**
 * 数据源id存放在ThreadLocal中<br/>
 * 不通线程访问数据库不会有冲突
 * 
 * @author denis.huang
 * @since 2017年2月15日
 */
class DynamicDataSourceHolder {
	private static final ThreadLocal<String> holder = new ThreadLocal<>();

	static void putDataSource(String name) {
		holder.set(name);
	}

	static String getDataSource() {
		return holder.get();
	}
}
