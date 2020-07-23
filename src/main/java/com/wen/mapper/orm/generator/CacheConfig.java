package com.wen.mapper.orm.generator;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.impl.PerpetualCache;

/**
 * 缓存配置
 * 
 * @author denis.huang
 * @since 2017-01-24
 */
public class CacheConfig {
	/** 是否使用缓存 */
	private boolean cacheEnable;

	/** 缓存淘汰策略,LRU,FIFO,SOFT,WEAK */
	private String eviction;

	/** 刷新时间（毫秒） */
	private Long flushInterval;

	/** 缓存对象最大数量 */
	private Integer size;

	/** 是否只读 */
	private boolean readOnly;

	private boolean blocking;

	/** 指定缓存实现类 */
	private Class<? extends Cache> cacheImpl;

	@SuppressWarnings("unchecked")
	public static CacheConfig build(Properties prop) {
		// 没有配置文件，默认不启用
		if (prop == null) {
			CacheConfig config = new CacheConfig();
			config.setCacheEnable(false);
			return config;
		}

		CacheConfig config = new CacheConfig();
		config.setCacheEnable(prop.getProperty("cacheEnable", "false").equals("true"));
		config.setEviction(prop.getProperty("eviction", "LRU"));
		config.setFlushInterval(Long.parseLong(prop.getProperty("flushInterval", "60000")));
		config.setSize(Integer.parseInt(prop.getProperty("size", "2048")));
		config.setReadOnly(prop.getProperty("readOnly", "false").equals("true"));
		config.setBlocking(prop.getProperty("blocking", "false").equals("true"));

		try {
			String clazzName = prop.getProperty("cacheImpl", PerpetualCache.class.getName());
			Class<?> clazz = Class.forName(clazzName);

			Object obj = clazz.getConstructor(String.class).newInstance("");
			if (obj instanceof Cache) {
				config.setCacheImpl((Class<? extends Cache>) clazz);
			} else {
				throw new BuilderException(clazzName + " not implement Cache");
			}
		} catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | SecurityException e) {
			throw new BuilderException("instance obj error!!", e);
		}

		return config;
	}

	public Class<? extends Cache> getCacheImpl() {
		return cacheImpl;
	}

	public void setCacheImpl(Class<? extends Cache> cacheImpl) {
		this.cacheImpl = cacheImpl;
	}

	public boolean isCacheEnable() {
		return cacheEnable;
	}

	public void setCacheEnable(boolean cacheEnable) {
		this.cacheEnable = cacheEnable;
	}

	public String getEviction() {
		return eviction;
	}

	public void setEviction(String eviction) {
		this.eviction = eviction;
	}

	public Long getFlushInterval() {
		return flushInterval;
	}

	public void setFlushInterval(Long flushInterval) {
		this.flushInterval = flushInterval;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}
}
