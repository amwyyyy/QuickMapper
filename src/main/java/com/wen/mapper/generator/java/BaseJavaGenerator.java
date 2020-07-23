package com.wen.mapper.generator.java;

import org.apache.commons.lang.StringUtils;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.codegen.AbstractJavaClientGenerator;
import org.mybatis.generator.codegen.AbstractXmlGenerator;

/**
 * Java类生成器基类
 * 
 * @author huangwg
 * @since 2016-10-10
 */
public abstract class BaseJavaGenerator extends AbstractJavaClientGenerator {
	protected String poType;
	protected String mapperType;
	protected String pkType;

	public BaseJavaGenerator() {
		super(false);
	}

	protected void calculateJavaFileAttributes(FullyQualifiedTable fullyQualifiedTable) {
		if (context.getJavaModelGeneratorConfiguration() == null) {
			return;
		}
		String poTargetPackage = context.getJavaModelGeneratorConfiguration().getTargetPackage();
		String mapperTargetPackage = poTargetPackage;

		if (StringUtils.isNotEmpty(poTargetPackage)) {
			String[] array = poTargetPackage.split("\\.");

			array[array.length - 1] = "mapper";
			mapperTargetPackage = StringUtils.join(array, ".");
		}

		StringBuilder sb = new StringBuilder();

		sb.append(poTargetPackage).append(fullyQualifiedTable.getSubPackageForModel(true));
		sb.append('.');
		sb.append(fullyQualifiedTable.getDomainObjectName());
		poType = sb.toString();

		String mapperPackage = mapperTargetPackage + fullyQualifiedTable.getSubPackageForModel(true);
		sb.setLength(0);
		sb.append(mapperPackage);
		sb.append('.');
		sb.append(fullyQualifiedTable.getDomainObjectName());
		sb.append("Mapper");
		mapperType = sb.toString();

		pkType = void.class.getName();
		if (introspectedTable.getPrimaryKeyColumns() != null && !introspectedTable.getPrimaryKeyColumns().isEmpty()) {
			pkType = introspectedTable.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType()
					.getFullyQualifiedName();
		}
	}

	@Override
	public AbstractXmlGenerator getMatchedXMLGenerator() {
		return null;
	}
}
