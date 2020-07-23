package com.wen.mapper.orm.generator;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.Configuration;

import java.text.MessageFormat;

/**
 * CRUD生成器默认实现
 * 
 * @author denis.huang
 * @since 2017年2月15日
 */
public class DefaultStatementGenerator extends BaseStatementGenerator {

	public DefaultStatementGenerator(Configuration configuration, Class<?> entityClass) {
		super(configuration, entityClass);
	}

	/**
	 * 生成where id=语句
	 */
	private String generateByPrimaryKeySql() {
		StringBuilder sql = new StringBuilder();
		for (int i = 0; i < pkColumnList.size(); i++) {
			String columnName = pkColumnList.get(i);

			StringBuilder condition = new StringBuilder();
			condition.append(columnName).append("=#{").append(tableFieldMap.get(columnName).getName());
			if (StringUtils.isNotEmpty(tableColumnMap.get(columnName).jdbcType())) {
				condition.append(", jdbcType=").append(tableColumnMap.get(columnName).jdbcType());
			}
			condition.append("}");

			if (i == 0) {
				sql.append(" where ").append(condition.toString());
			} else {
				sql.append(" and ").append(condition.toString());
			}
		}
		return sql.toString();
	}

	/**
	 * 生成selectByPrimaryKey Sql
	 */
	@Override
	protected String generateSelectByPrimaryKeySql() {
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(StringUtils.join(columnNameList, ","));
		sql.append(" from ").append(table.tableName());
		sql.append(generateByPrimaryKeySql());

		logger.debug("generateSelectByPrimaryKeySql:\n {}", sql.toString());
		return sql.toString();
	}

	/**
	 * 生成insert sql
	 */
	@Override
	protected String generateInsertSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into ").append(table.tableName());
		sql.append("(");
		sql.append(StringUtils.join(inserColumnNameList, ","));
		sql.append(") values (");

		for (int i = 0; i < inserColumnNameList.size(); i++) {
			String columnName = inserColumnNameList.get(i);
			sql.append("#{").append(tableFieldMap.get(columnName).getName());
			if (StringUtils.isNotEmpty(tableColumnMap.get(columnName).jdbcType())) {
				sql.append(",jdbcType=").append(tableColumnMap.get(columnName).jdbcType());
			}
			sql.append("}");
			if (i < inserColumnNameList.size() - 1) {
				sql.append(", ");
			}
		}

		sql.append(")");

		logger.debug("generateInsertSql:\n {}", sql.toString());
		return sql.toString();
	}

	/**
	 * 生成UpdateByPrimaryKey sql
	 */
	@Override
	protected String generateUpdateByPrimaryKeySql() {
		StringBuilder sql = new StringBuilder();
		sql.append("<update id=\"updateByPrimaryKey\" parameterType=\"").append(poClass.getName()).append("\">");
		sql.append("update ").append(table.tableName());
		sql.append(" set ");
		sql.append("<trim suffixOverrides=\",\">");

		for (String columnName : updateColumnNameList) {
			if (pkColumnList.contains(columnName)) {
				continue;// 主键跳过，不需要更新
			}
			sql.append(columnName).append("=#{").append(tableFieldMap.get(columnName).getName());
			if (StringUtils.isNotEmpty(tableColumnMap.get(columnName).jdbcType())) {
				sql.append(",jdbcType=").append(tableColumnMap.get(columnName).jdbcType());
			}
			sql.append("}, ");
		}
		sql.append("</trim>");
		sql.append(generateByPrimaryKeySql());
		sql.append("</update>");

		logger.debug("generateUpdateByPrimaryKeySql:\n {}", sql.toString());
		return sql.toString();
	}

	/**
	 * 生成deleteByPK Sql
	 */
	@Override
	protected String generateDeleteByPrimaryKeySql() {
		StringBuilder sql = new StringBuilder();
		sql.append("delete from ").append(table.tableName());
		sql.append(generateByPrimaryKeySql());

		logger.debug("generateDeleteByPrimaryKeySql:\n {}", sql.toString());
		return sql.toString();
	}

	/**
	 * 生成findByPKs Sql
	 */
	@Override
	protected String generateSelectByPrimaryKeysSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("<select id=\"selectByPrimaryKeys\" resultMap=\"").append(poClass.getName()).append("\"> ");
		sql.append("select ");
		sql.append(StringUtils.join(columnNameList, ","));
		sql.append(" from ").append(table.tableName());
		String pkName = pkColumnList.get(0); // 暂不考虑多主键的情况
		String jdbcType = tableColumnMap.get(pkName).jdbcType();
		sql.append(" where ").append(pkName).append(" in ");
		sql.append("<foreach collection=\"array\" item=\"id\" open=\"(\" separator=\",\" close=\")\">");
		sql.append("#{id, jdbcType=").append(jdbcType).append("}");
		sql.append("</foreach>");
		sql.append("</select>");

		logger.debug("generateSelectByPrimaryKeysSql:\n {}", sql.toString());
		return sql.toString();
	}

	@Override
	protected String generateUpdateByPrimaryKeySelectiveSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("<update id=\"updateByPrimaryKeySelective\" parameterType=\"").append(poClass.getName())
				.append("\">");
		sql.append("update ").append(table.tableName());
		sql.append("<set>");

		for (String columnName : updateColumnNameList) {
			if (pkColumnList.contains(columnName)) {
				continue;
			}
			sql.append("<if test=\"").append(tableFieldMap.get(columnName).getName()).append(" != null\">");
			sql.append(columnName).append(" = #{").append(tableFieldMap.get(columnName).getName());
			if (StringUtils.isNotEmpty(tableColumnMap.get(columnName).jdbcType())) {
				sql.append(",jdbcType=").append(tableColumnMap.get(columnName).jdbcType());
			}
			sql.append("},");
			sql.append("</if>");
		}
		sql.append("</set>");
		sql.append(generateByPrimaryKeySql());
		sql.append("</update>");

		logger.debug("generateUpdateByPrimaryKeySelectiveSql:\n {}", sql.toString());
		return sql.toString();
	}

	@Override
	protected String generateUpdateByExampleSelectiveSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("<update id=\"updateByExampleSelective\" parameterType=\"map\">");
		sql.append("update ").append(table.tableName());
		sql.append("<set>");
		for (String columnName : updateColumnNameList) {
			if (pkColumnList.contains(columnName)) {
				continue;// 主键跳过，不需要更新
			}
			sql.append("<if test=\"").append("record.").append(tableFieldMap.get(columnName).getName())
					.append(" != null\">");
			sql.append(columnName).append(" = #{").append("record.").append(tableFieldMap.get(columnName).getName());
			if (StringUtils.isNotEmpty(tableColumnMap.get(columnName).jdbcType())) {
				sql.append(",jdbcType=").append(tableColumnMap.get(columnName).jdbcType());
			}
			sql.append("}, ");
			sql.append("</if>");
		}
		sql.append("</set>");
		sql.append("<if test=\"_parameter != null\">");
		sql.append(generateUpdateByExampleWhereClause());
		sql.append("</if>");
		sql.append("</update>");

		logger.debug("generateUpdateByExampleSelectiveSql:\n {}", sql.toString());
		return sql.toString();
	}

	@Override
	protected String generateDeleteByExampleSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("<delete id=\"deleteByExample\" parameterType=\"").append(exampleClass.getName()).append("\">");
		sql.append("delete from ").append(table.tableName());
		sql.append("<if test=\"_parameter != null\">");
		sql.append(generateExampleWhereSql());
		sql.append("</if>");
		sql.append("</delete>");

		logger.debug("generateDeleteByExampleSql:\n {}", sql.toString());
		return sql.toString();
	}

	@Override
	protected String generateCountByExampleSql() {
		String str = "<select id=\"countByExample\" resultType=\"java.lang.Integer\" parameterType=\"{0}\">"
				+ "select count(*) "
				+ "from {1}"
				+ "<if test=\"_parameter != null\">{2}</if>"
				+ "</select>";

		String sql = MessageFormat.format(str, exampleClass.getName(), table.tableName(), generateExampleWhereSql());

		logger.debug("generateCountByExampleSql:\n {}", sql);
		return sql;
	}

	@Override
	protected String generateSelectByExampleSql() {
		String str = "<select id=\"selectByExample\" resultMap=\"{0}\" parameterType=\"{1}\">"
				+ "select "
				+ "<if test=\"distinct\">distinct </if>"
				+ "{2} "
				+ "from {3} "
				+ "<if test=\"_parameter != null\">{4} </if>"
				+ "<if test=\"orderByClause != null\">order by {5}</if>"
				+ "</select>";

		String sql = MessageFormat.format(str, poClass.getName(), exampleClass.getName(),
				StringUtils.join(columnNameList, ","), table.tableName(), generateExampleWhereSql(), "${orderByClause}");

		logger.debug("generateSelectByExampleSql:\n {}", sql);
		return sql;
	}

	@Override
	protected String generateUpdateByExampleWhereClause() {
		return "<where><foreach collection=\"example.oredCriteria\" item=\"criteria\" separator=\"or\">"
				+ "<if test=\"criteria.valid\"><trim prefix=\"(\" suffix=\")\" prefixOverrides=\"and\">"
				+ "<foreach collection=\"criteria.criteria\" item=\"criterion\"><choose>"
				+ "<when test=\"criterion.noValue\"> and ${criterion.condition}</when>"
				+ "<when test=\"criterion.singleValue\"> and ${criterion.condition} #{criterion.value}</when>"
				+ "<when test=\"criterion.betweenValue\">"
				+ " and ${criterion.condition} #{criterion.value} and #{criterion.secondValue}</when>"
				+ "<when test=\"criterion.listValue\"> and ${criterion.condition}"
				+ "<foreach collection=\"criterion.value\" item=\"listItem\" open=\"(\" close=\")\" separator=\",\">"
				+ "#{listItem}</foreach></when></choose></foreach></trim></if></foreach></where>";
	}

	@Override
	protected String generateExampleWhereSql() {
		return "<where><foreach collection=\"oredCriteria\" item=\"criteria\" separator=\"or\">"
				+ "<if test=\"criteria.valid\"><trim prefix=\"(\" suffix=\")\" prefixOverrides=\"and\">"
				+ "<foreach collection=\"criteria.criteria\" item=\"criterion\"><choose>"
				+ "<when test=\"criterion.noValue\"> and ${criterion.condition}"
				+ "</when><when test=\"criterion.singleValue\">"
				+ " and ${criterion.condition} #{criterion.value}</when><when test=\"criterion.betweenValue\">"
				+ " and ${criterion.condition} #{criterion.value} and #{criterion.secondValue}"
				+ "</when><when test=\"criterion.listValue\">and ${criterion.condition}"
				+ "<foreach collection=\"criterion.value\" item=\"listItem\" open=\"(\" close=\")\" separator=\",\">"
				+ "#{listItem}</foreach></when></choose></foreach></trim></if></foreach></where>";
	}
}
