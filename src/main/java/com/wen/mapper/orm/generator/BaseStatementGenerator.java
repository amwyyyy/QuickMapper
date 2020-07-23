package com.wen.mapper.orm.generator;

import java.lang.reflect.Field;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.CacheBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.XMLScriptBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wen.mapper.orm.annotation.Association;
import com.wen.mapper.orm.annotation.Column;
import com.wen.mapper.orm.annotation.Entity;
import com.wen.mapper.utils.BeanUtils;

/**
 * 动态CRUD生成器
 * 
 * @author denis.huang
 * @since 2017年2月15日
 */
public abstract class BaseStatementGenerator {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/** mybatis总配置类 */
	private Configuration configuration;

	/** 实体类类型 */
	protected Class<?> poClass;

	/** 实体类注解 */
	protected Entity table;

	protected Class<?> exampleClass;

	/** 命名空间，一般是dao类完整名 */
	private String namespace;

	/** 当前使用的缓存 */
	private Cache currentCache;

	/** <字段名,字段Field> */
	protected Map<String, Field> tableFieldMap = new HashMap<>();

	/** <字段名,字段Column> */
	protected Map<String, Column> tableColumnMap = new HashMap<>();

	/** <字段名,Association> */
	private Map<String, Association> tableAssociationMap = new HashMap<>();

	/** 所有列名 */
	protected List<String> columnNameList = new ArrayList<>();

	/** 主键列名 */
	protected List<String> pkColumnList = new ArrayList<>();

	/** 主键类型 */
	protected List<String> pkPropertyList = new ArrayList<>();

	/** 需要插入的字段 */
	protected List<String> inserColumnNameList = new ArrayList<>();

	/** 需要更新的字段 */
	protected List<String> updateColumnNameList = new ArrayList<>();

	/** 需要级联查询字段 */
	protected List<String> associationColumnNameList = new ArrayList<>();

	/**
	 * @param configuration
	 * @param poClass
	 */
	public BaseStatementGenerator(Configuration configuration, Class<?> poClass) {
		this.configuration = configuration;
		this.poClass = poClass;
	}

	/**
	 * 递归获取表实体类注解
	 * 
	 * @param clazz
	 * @return
	 */
	private Entity getEntity(Class<?> clazz) {
		if (clazz == null) {
			return null;
		}

		if (clazz.equals(Object.class)) {
			return null;
		}

		Entity model = clazz.getAnnotation(Entity.class);
		if (model != null) {
			return model;
		}

		return getEntity(clazz.getSuperclass());
	}

	/**
	 * 获取Example类型
	 * 
	 * @return
	 */
	private Class<?> getExampleClass() {
		String exampleClassName = StringUtils.substringBeforeLast(poClass.getName(), ".") + "."
				+ poClass.getSimpleName() + "Example";
		logger.info("{} example name = {}", poClass.getSimpleName(), exampleClassName);

		try {
			return Class.forName(exampleClassName);
		} catch (ClassNotFoundException e) {
			throw new BuilderException("获取Example类异常:" + poClass.getSimpleName(), e);
		}
	}

	/**
	 * 取命名空间
	 * 
	 * @param model
	 * @return 如com.wen.db.mapper.EmpMapper
	 */
	private String getNamespace(Entity model) {
		String namespace = model.namespace();
		if (StringUtils.isEmpty(namespace)) {
			String po = StringUtils.substringBeforeLast(poClass.getName(), ".");
			String db = StringUtils.substringBeforeLast(po, ".");
			namespace = db + ".mapper." + poClass.getSimpleName() + "Mapper";
		}

		return namespace;
	}

	/**
	 * 取ResultMap名
	 * 
	 * @return 如com.wen.mapper.EmpMapper.EmpResultMap
	 */
	protected String getResultMapName() {
		return namespace + "." + poClass.getSimpleName() + "ResultMap";
	}

	/**
	 * 初始化
	 */
	public void init() {
		// 获取实体类注解
		table = getEntity(poClass);
		if (table == null) {
			logger.warn("没有Entity注解 : {}", poClass.getSimpleName());
			return;
		}

		// 获取example类型
		exampleClass = getExampleClass();

		// 获取命名空间
		namespace = getNamespace(table);

		// 检查表名
		if (StringUtils.isEmpty(table.tableName())) {
			throw new BuilderException(poClass.getName() + " tableName is empty!");
		}

		// 获取实体类中所有字段
		Field[] fields = BeanUtils.findSuperFields(poClass);

		for (Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if (column != null && StringUtils.isNotEmpty(column.name())) {
				// 字段名对应map
				tableFieldMap.put(column.name(), field);
				tableColumnMap.put(column.name(), column);
				columnNameList.add(column.name());

				// 处理主键
				if (column.pk()) {
					pkColumnList.add(column.name());
					pkPropertyList.add(field.getName());
				}

				// 要插入字段
				if (column.insertable()) {
					inserColumnNameList.add(column.name());
				}

				// 要更新字段
				if (column.updatable()) {
					updateColumnNameList.add(column.name());
				}
			} else {
				Association association = field.getAnnotation(Association.class);
				if (association != null && StringUtils.isNotEmpty(association.select())
						&& StringUtils.isNotEmpty(association.columnName())) {
					// 要级联查询的字段
					associationColumnNameList.add(association.columnName());
					tableAssociationMap.put(association.columnName(), association);
				}
			}
		}
	}

	/**
	 * 生成entity类型的映射 默认名称为:类名+ResultMap
	 */
	public void generateModelResultMap() {
		List<ResultMapping> resultMappings = new ArrayList<>();

		// 添加ResultMap普通列
		for (String columnName : columnNameList) {
			addColumnMapper(resultMappings, tableColumnMap.get(columnName), tableFieldMap.get(columnName));
		}

		// 添加ResultMap级联查询列
		for (String associationName : associationColumnNameList) {
			addAssociationMapper(resultMappings, tableAssociationMap.get(associationName),
					tableFieldMap.get(associationName));
		}

		ResultMap.Builder resultMapBuilder = new ResultMap.Builder(configuration, getResultMapName(), poClass,
				resultMappings, false);
		configuration.addResultMap(resultMapBuilder.build());
	}

	/**
	 * 为ResultMap添加列
	 * 
	 * @param resultMappings
	 * @param column
	 * @param field
	 */
	private void addColumnMapper(List<ResultMapping> resultMappings, Column column, Field field) {
		JdbcType jdbcType = resolveJdbcType(column.jdbcType());
		ResultMapping.Builder builder = new ResultMapping.Builder(configuration, field.getName(), column.name(),
				field.getType());
		builder.jdbcType(jdbcType);
		resultMappings.add(builder.build());
	}

	/**
	 * 为ResultMap添加级联查询
	 * 
	 * @param resultMappings
	 * @param association
	 * @param field
	 */
	private void addAssociationMapper(List<ResultMapping> resultMappings, Association association, Field field) {
		JdbcType jdbcType = resolveJdbcType(association.jdbcType());
		ResultMapping.Builder builder = new ResultMapping.Builder(configuration, field.getName(),
				association.columnName(), field.getType());
		builder.jdbcType(jdbcType);
		builder.nestedQueryId(association.select());
		builder.lazy(association.lazy());
		resultMappings.add(builder.build());
	}

	/**
	 * 将类型名转为JdbcType对象
	 * 
	 * @param alias
	 *            字符串类型名
	 * @return
	 */
	private JdbcType resolveJdbcType(String alias) {
		if (StringUtils.isEmpty(alias)) {
			return null;
		}

		try {
			return JdbcType.valueOf(alias);
		} catch (IllegalArgumentException e) {
			throw new BuilderException("Error resolving JdbcType. Cause: " + e, e);
		}
	}

	/**
	 * 生成缓存元素
	 */
	public void generateCacheStatement() {
		CacheConfig cacheConfig = CacheConfig.build(configuration.getVariables());

		configuration.setCacheEnabled(cacheConfig.isCacheEnable());

		if (cacheConfig.isCacheEnable()) {
			// 缓存装饰器
			Class<? extends Cache> decoratorCache = configuration.getTypeAliasRegistry()
					.resolveAlias(cacheConfig.getEviction());

			CacheBuilder cacheBuilder = new CacheBuilder(namespace).implementation(cacheConfig.getCacheImpl())
					.addDecorator(decoratorCache).clearInterval(cacheConfig.getFlushInterval())
					.size(cacheConfig.getSize()).readWrite(cacheConfig.isReadOnly()).blocking(cacheConfig.isBlocking());

			Cache cache = cacheBuilder.build();
			configuration.addCache(cache);
			currentCache = cache;
		}
	}

	/**
	 * 根据XML生成XNode
	 * 
	 * @param xml
	 * @param express
	 * @return
	 */
	private XNode createXNode(String xml, String express) {
		XPathParser parser = new XPathParser(xml);
		return parser.evalNode(express);
	}

	/**
	 * 带mybatis元素的sql语句，生成sqlSource
	 * 
	 * @param xmlSql
	 *            xml格式sql语句，带有mybatis元素
	 * @param express
	 *            元素类型
	 * @param parameterType
	 *            参数类型
	 * @return
	 */
	private SqlSource createSqlSource4XML(String express, String xmlSql, Class<?> parameterType) {
		XNode xNode = createXNode(xmlSql, express);
		XMLScriptBuilder xmlScriptBuilder = new XMLScriptBuilder(configuration, xNode, parameterType);
		return xmlScriptBuilder.parseScriptNode();
	}

	/**
	 * 获取主键类型
	 * 
	 * @return
	 */
	private Class<?> getPrimaryKeyType() {
		Class<?> type = null;
		if (pkColumnList.size() == 1) {
			Field pkField = tableFieldMap.get(pkColumnList.get(0));
			if (pkField != null) {
				type = pkField.getType();
			}
		} else if (pkColumnList.size() > 1) {
			type = Map.class;
		}
		return type;
	}

	/**
	 * 生成根据主键查询元素
	 */
	public void generateSelectByPrimaryKeyStatement() {
		Class<?> parameterType = getPrimaryKeyType();
		SqlSource sqlSource = new RawSqlSource(configuration, generateSelectByPrimaryKeySql(), parameterType);

		MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration,
				namespace + ".selectByPrimaryKey", sqlSource, SqlCommandType.SELECT);

		List<ResultMap> resultMaps = new ArrayList<>();
		resultMaps.add(configuration.getResultMap(getResultMapName()));
		statementBuilder.resultMaps(resultMaps);

		statementBuilder.statementType(StatementType.PREPARED);
		statementBuilder.keyGenerator(new NoKeyGenerator());
		statementBuilder.useCache(true);
		statementBuilder.cache(currentCache);

		configuration.addMappedStatement(statementBuilder.build());
	}

	protected abstract String generateSelectByPrimaryKeySql();

	/**
	 * 基础插入语句
	 */
	public void generateInsertStatement() {
		SqlSource sqlSource = new RawSqlSource(configuration, generateInsertSql(), poClass);
		MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, namespace + ".insert",
				sqlSource, SqlCommandType.INSERT);
		statementBuilder.statementType(StatementType.PREPARED);
		KeyGenerator keyGenerator = (table.useGeneratedKeys() || configuration.isUseGeneratedKeys())
				? new Jdbc3KeyGenerator() : new NoKeyGenerator();
		statementBuilder.keyGenerator(keyGenerator);
		statementBuilder.keyColumn(StringUtils.join(pkColumnList, ","));
		statementBuilder.keyProperty(StringUtils.join(pkPropertyList, ","));

		statementBuilder.useCache(false);
		statementBuilder.cache(currentCache);
		statementBuilder.flushCacheRequired(true);

		configuration.addMappedStatement(statementBuilder.build());
	}

	protected abstract String generateInsertSql();

	/**
	 * 生成根据ID更新语句元素
	 */
	public void generateUpdateByPrimaryKeyStatement() {
		SqlSource sqlSource = createSqlSource4XML("/update", generateUpdateByPrimaryKeySql(), poClass);

		MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration,
				namespace + ".updateByPrimaryKey", sqlSource, SqlCommandType.UPDATE);
		statementBuilder.statementType(StatementType.PREPARED);
		statementBuilder.keyGenerator(new NoKeyGenerator());

		statementBuilder.useCache(false);
		statementBuilder.cache(currentCache);
		statementBuilder.flushCacheRequired(true);

		MappedStatement statement = statementBuilder.build();
		configuration.addMappedStatement(statement);
	}

	protected abstract String generateUpdateByPrimaryKeySql();

	/**
	 * 生成根据ID删除数据元素
	 */
	public void generateDeleteByPrimaryKeyStatement() {
		Class<?> parameterType = getPrimaryKeyType();
		SqlSource sqlSource = new RawSqlSource(configuration, generateDeleteByPrimaryKeySql(), parameterType);

		MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration,
				namespace + ".deleteByPrimaryKey", sqlSource, SqlCommandType.DELETE);
		statementBuilder.statementType(StatementType.PREPARED);
		statementBuilder.keyGenerator(new NoKeyGenerator());

		statementBuilder.useCache(false);
		statementBuilder.cache(currentCache);
		statementBuilder.flushCacheRequired(true);

		MappedStatement statement = statementBuilder.build();
		configuration.addMappedStatement(statement);
	}

	protected abstract String generateDeleteByPrimaryKeySql();

	/**
	 * 生成按主键数组取记录数元素
	 */
	public void generateSelectByPrimaryKeysStatement() {
		SqlSource sqlSource = createSqlSource4XML("/select", generateSelectByPrimaryKeysSql(), Integer.class);

		MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration,
				namespace + ".selectByPrimaryKeys", sqlSource, SqlCommandType.SELECT);
		statementBuilder.statementType(StatementType.PREPARED);
		statementBuilder.keyGenerator(new NoKeyGenerator());

		List<ResultMap> resultMaps = new ArrayList<>();
		resultMaps.add(configuration.getResultMap(getResultMapName()));
		statementBuilder.resultMaps(resultMaps);

		statementBuilder.useCache(true);
		statementBuilder.cache(currentCache);

		MappedStatement statement = statementBuilder.build();
		configuration.addMappedStatement(statement);
	}

	protected abstract String generateSelectByPrimaryKeysSql();

	/**
	 * 根据example对象查询
	 */
	public void generateSelectByExampleStatement() {
		SqlSource sqlSource = createSqlSource4XML("/select", generateSelectByExampleSql(), exampleClass);

		MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration,
				namespace + ".selectByExample", sqlSource, SqlCommandType.SELECT);
		statementBuilder.statementType(StatementType.PREPARED);
		statementBuilder.keyGenerator(new NoKeyGenerator());
		List<ResultMap> resultMaps = new ArrayList<>();
		resultMaps.add(configuration.getResultMap(getResultMapName()));
		statementBuilder.resultMaps(resultMaps);

		statementBuilder.useCache(true);
		statementBuilder.cache(currentCache);

		MappedStatement statement = statementBuilder.build();
		configuration.addMappedStatement(statement);
	}

	protected abstract String generateSelectByExampleSql();

	protected abstract String generateExampleWhereSql();

	/**
	 * 根据example对象更新数据
	 */
	public void generateUpdateByExampleStatement() {
		SqlSource sqlSource = createSqlSource4XML("/update", generateUpdateByExampleSql(), Map.class);

		MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration,
				namespace + ".updateByExample", sqlSource, SqlCommandType.UPDATE);
		statementBuilder.statementType(StatementType.PREPARED);
		statementBuilder.keyGenerator(new NoKeyGenerator());

		statementBuilder.useCache(false);
		statementBuilder.cache(currentCache);
		statementBuilder.flushCacheRequired(true);

		MappedStatement statement = statementBuilder.build();
		configuration.addMappedStatement(statement);
	}

	private String generateUpdateByExampleSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("<update id=\"updateByExample\" parameterType=\"map\">");
		sql.append("update ").append(table.tableName());
		sql.append(" set ");
		sql.append("<trim suffixOverrides=\",\">");
		for (String columnName : updateColumnNameList) {
			if (pkColumnList.contains(columnName)) {
				continue;// 主键跳过，不需要更新
			}
			sql.append(columnName).append(" = #{").append("record.").append(tableFieldMap.get(columnName).getName());
			if (StringUtils.isNotEmpty(tableColumnMap.get(columnName).jdbcType())) {
				sql.append(",jdbcType=").append(tableColumnMap.get(columnName).jdbcType());
			}
			sql.append("}, ");
		}
		sql.append("</trim>");

		sql.append("<if test=\"_parameter != null\">");
		sql.append(generateUpdateByExampleWhereClause());
		sql.append("</if>");
		sql.append("</update>");

		if (logger.isDebugEnabled()) {
			logger.debug("generateUpdateByExampleSql:");
			logger.debug("\n" + sql.toString());
		}

		return sql.toString();
	}

	protected abstract String generateUpdateByExampleWhereClause();

	/**
	 * 根据example对象，统计数量
	 */
	public void generateCountByExampleStatement() {
		SqlSource sqlSource = createSqlSource4XML("/select", generateCountByExampleSql(), exampleClass);

		MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration,
				namespace + ".countByExample", sqlSource, SqlCommandType.SELECT);

		statementBuilder.statementType(StatementType.PREPARED);
		statementBuilder.keyGenerator(new NoKeyGenerator());

		// 返回int类型
		ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(configuration,
				statementBuilder.id() + "-Inline", Integer.class, new ArrayList<>());
		statementBuilder.resultMaps(Collections.singletonList(inlineResultMapBuilder.build()));

		statementBuilder.useCache(true);
		statementBuilder.cache(currentCache);

		MappedStatement statement = statementBuilder.build();
		configuration.addMappedStatement(statement);
	}

	protected abstract String generateCountByExampleSql();

	/**
	 * 根据example对象删除数据
	 */
	public void generateDeleteByExampleStatement() {
		SqlSource sqlSource = createSqlSource4XML("/delete", generateDeleteByExampleSql(), exampleClass);

		MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration,
				namespace + ".deleteByExample", sqlSource, SqlCommandType.DELETE);
		statementBuilder.statementType(StatementType.PREPARED);
		statementBuilder.keyGenerator(new NoKeyGenerator());

		statementBuilder.useCache(false);
		statementBuilder.cache(currentCache);
		statementBuilder.flushCacheRequired(true);

		MappedStatement statement = statementBuilder.build();
		configuration.addMappedStatement(statement);
	}

	protected abstract String generateDeleteByExampleSql();

	/**
	 * 根据example对象更新数据,不会更新空对象
	 */
	public void generateUpdateByExampleSelectiveStatement() {
		SqlSource sqlSource = createSqlSource4XML("/update", generateUpdateByExampleSelectiveSql(), Map.class);

		MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration,
				namespace + ".updateByExampleSelective", sqlSource, SqlCommandType.UPDATE);
		statementBuilder.statementType(StatementType.PREPARED);
		statementBuilder.keyGenerator(new NoKeyGenerator());

		statementBuilder.useCache(false);
		statementBuilder.cache(currentCache);
		statementBuilder.flushCacheRequired(true);

		MappedStatement statement = statementBuilder.build();
		configuration.addMappedStatement(statement);
	}

	protected abstract String generateUpdateByExampleSelectiveSql();

	/**
	 * 生成根据ID更新语句元素, 不会更新空对象
	 */
	public void generateUpdateByPrimaryKeySelectiveStatement() {
		SqlSource sqlSource = createSqlSource4XML("/update", generateUpdateByPrimaryKeySelectiveSql(), poClass);

		MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration,
				namespace + ".updateByPrimaryKeySelective", sqlSource, SqlCommandType.UPDATE);
		statementBuilder.statementType(StatementType.PREPARED);
		statementBuilder.keyGenerator(new NoKeyGenerator());

		statementBuilder.useCache(false);
		statementBuilder.cache(currentCache);
		statementBuilder.flushCacheRequired(true);

		MappedStatement statement = statementBuilder.build();
		configuration.addMappedStatement(statement);
	}

	protected abstract String generateUpdateByPrimaryKeySelectiveSql();
}
