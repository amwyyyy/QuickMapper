package com.wen.mapper.orm;

import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wen.mapper.orm.generator.BaseStatementGenerator;
import com.wen.mapper.orm.generator.DefaultStatementGenerator;

/**
 * MyBatis Mapper Builder, 动态根据Model类生成与Model类相关的映射，避免手工编写dao,mapper xml文件
 * 
 * @author denis.huang
 * @since 2017年2月15日
 */
public class MybatisMapperBuilder extends BaseBuilder {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Class<?> poClass;

	/**
	 * @param configuration
	 *            mybatis总配置类
	 * @param poClass
	 *            实体类类型
	 */
	public MybatisMapperBuilder(Configuration configuration, Class<?> poClass) {
		super(configuration);

		this.poClass = poClass;
	}

	public void build() {
		logger.info("build begin:" + poClass.getName());

		// 实例化Statement生成器类
		BaseStatementGenerator statementGenerator = new DefaultStatementGenerator(configuration, poClass);

		// 初始化
		statementGenerator.init();

		// 缓存元素
		statementGenerator.generateCacheStatement();

		// 实体基本ResultMap
		statementGenerator.generateModelResultMap();

		// 基础查询元素
		statementGenerator.generateSelectByPrimaryKeyStatement();
		statementGenerator.generateSelectByPrimaryKeysStatement();
		statementGenerator.generateSelectByExampleStatement();
		statementGenerator.generateCountByExampleStatement();

		// 基础修改元素
		statementGenerator.generateInsertStatement();
		statementGenerator.generateUpdateByPrimaryKeyStatement();
		statementGenerator.generateUpdateByPrimaryKeySelectiveStatement();
		statementGenerator.generateUpdateByExampleStatement();
		statementGenerator.generateUpdateByExampleSelectiveStatement();
		statementGenerator.generateDeleteByPrimaryKeyStatement();
		statementGenerator.generateDeleteByExampleStatement();

		logger.info("build end:" + poClass.getName());
	}
}
