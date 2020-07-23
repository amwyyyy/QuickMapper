package com.wen.mapper.generator;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;

/**
 * mybatisGenerator生成类文件入口
 * 
 * @author denis.huang
 * @since 2017年2月15日
 */
public class MybatisGeneratorBuilder {
	/** jdbc驱动类名 */
	private String driverClass;
	/** mysql连接url */
	private String connectionURL;
	/** 数据库用户名 */
	private String userId;
	/** 数据库密码 */
	private String password;
	/** jdbc驱动包路径 */
	private String classPathEntry;
	/** 生成文件目录路径 */
	private String basePackage;

	private Context context;
	private List<String> warnings;
	private Configuration config;
	private List<TableConfiguration> tableConfigurations = new ArrayList<>();

	public MybatisGeneratorBuilder() throws Exception {
		warnings = new ArrayList<>();
		ConfigurationParser cp = new ConfigurationParser(warnings);
		config = cp.parseConfiguration(
				MybatisGeneratorBuilder.class.getClassLoader().getResourceAsStream("mybatis-generatorConfig.xml"));
		context = config.getContext("mybatis");
	}

	/**
	 * 设置jdbc连接
	 * 
	 * @param driverClass
	 *            jdbc驱动类名
	 * @param connectionURL
	 *            mysql连接url
	 * @param userId
	 *            数据库用户名
	 * @param password
	 *            数据库密码
	 * @param classPathEntry
	 *            驱动包路径
	 */
	public void setJdbcConnection(String driverClass, String connectionURL, String userId, String password,
			String classPathEntry) {
		this.driverClass = driverClass;
		this.connectionURL = connectionURL;
		this.userId = userId;
		this.password = password;
		this.classPathEntry = classPathEntry;
	}

	/**
	 * 设置生成文件的根路径
	 */
	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

	/**
	 * 添加要生成的表
	 * 
	 * @param schema
	 * @param tableName
	 *            表名
	 * @param domainObjectName
	 *            对象名
	 */
	public void addTable(String schema, String tableName, String domainObjectName) {
		TableConfiguration table = new TableConfiguration(context);
		table.setSchema(schema);
		table.setTableName(tableName);
		table.setDomainObjectName(domainObjectName + "PO");// 约定PO结尾
		tableConfigurations.add(table);
	}

	/**
	 * 自动生成类文件,除了entity
	 * 
	 * @throws Exception
	 */
	public void build() throws Exception {
		if (StringUtils.isEmpty(driverClass) || StringUtils.isEmpty(connectionURL) || StringUtils.isEmpty(userId)
				|| StringUtils.isEmpty(password)) {
			throw new RuntimeException("jdbc配置为空");
		}
		JDBCConnectionConfiguration jdbc = context.getJdbcConnectionConfiguration();
		jdbc.setDriverClass(driverClass);
		jdbc.setConnectionURL(connectionURL);
		jdbc.setUserId(userId);
		jdbc.setPassword(password);

		config.addClasspathEntry(classPathEntry);

		if (StringUtils.isEmpty(basePackage)) {
			throw new RuntimeException("basePackage配置为空");
		}
		context.getJavaModelGeneratorConfiguration().setTargetPackage(basePackage + ".db.po");
		context.getSqlMapGeneratorConfiguration().setTargetPackage("mapper");
		context.getJavaClientGeneratorConfiguration().setTargetPackage(basePackage + ".db.mapper");

		context.getTableConfigurations().clear();
		for (TableConfiguration tableConfig : tableConfigurations) {
			context.addTableConfiguration(tableConfig);
		}

		DefaultShellCallback shellCallback = new DefaultShellCallback(true) {
			private String targetPackage;

			@Override
			public File getDirectory(String targetProject, String targetPackage) throws ShellException {
				this.targetPackage = targetPackage;
				return super.getDirectory(targetProject, targetPackage);
			}

			@Override
			public boolean isOverwriteEnabled() {
				return targetPackage.startsWith(basePackage + ".db.po");
			}

			@Override
			public boolean isMergeSupported() {
				return !isOverwriteEnabled();
			}

			@Override
			public String mergeJavaFile(String newFileSource, String existingFileFullPath, String[] javadocTags,
					String fileEncoding) throws ShellException {
				StringBuilder source = new StringBuilder();
				// 不覆盖，直接返回文件原来的内容
				try (FileInputStream fis = new FileInputStream(new File(existingFileFullPath))) {
					int n;
					byte[] bytes = new byte[1024];
					do {
						n = fis.read(bytes);
						if (n > 0) {
							source.append(new String(bytes, 0, n, fileEncoding));
						}
					} while (n > 0);
				} catch (Exception e) {
					e.printStackTrace();
				}

				return source.toString();
			}
		};

		MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, shellCallback, warnings);
		myBatisGenerator.generate(null);
	}
}
