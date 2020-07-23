package com.wen.mapper.orm;

import static org.springframework.util.StringUtils.tokenizeToStringArray;

import java.util.Set;

import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

import com.wen.mapper.orm.annotation.Entity;

/**
 * 通用CRUD初始化工具
 * 
 * @author denis.huang
 * @since 2017年2月15日
 */
public class QuickMapperGenerator implements InitializingBean {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private SqlSessionFactory sqlSessionFactory;

	private String entityPackage;

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.entityPackage, "Property \'entityPackage\' is required");
		Assert.notNull(this.sqlSessionFactory, "Property \'sqlSessionFactory\' is required");

		// 获取mybatis总配置
		Configuration configuration = sqlSessionFactory.getConfiguration();

		// 需要扫描的实体类包
		String[] entityPackageArray = tokenizeToStringArray(entityPackage,
				ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);

		for (String packageToScan : entityPackageArray) {
			// 扫描出所有带有Entity注解的类
			Set<Class<? extends Class<?>>> entitySet = listAllEntityClass(packageToScan);

			for (Class<?> entity : entitySet) {
				if (!entity.isAnonymousClass() && !entity.isInterface() && !entity.isMemberClass()) {
					try {
						// 根据Entity上的注解动态生成通用CRUD方法
						MybatisMapperBuilder modalMapperBuilder = new MybatisMapperBuilder(configuration, entity);
						modalMapperBuilder.build();

						logger.info("finish build mapper className={}", entity.getName());
					} catch (Exception e) {
						logger.error(entity.getName() + " generator base CRUD error", e);
					}
				}
			}
		}
	}

	/**
	 * 获取目录下所有带有Entity的类
	 *
	 * @param packageToScan
	 * @return
	 */
	private Set<Class<? extends Class<?>>> listAllEntityClass(String packageToScan) {
		ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
		resolverUtil.find(new ResolverUtil.AnnotatedWith(Entity.class), packageToScan);
		return resolverUtil.getClasses();
	}

	public String getEntityPackage() {
		return entityPackage;
	}

	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}

	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}

	public void setEntityPackage(String entityPackage) {
		this.entityPackage = entityPackage;
	}
}
