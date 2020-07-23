package com.wen.mapper.generator.java;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wen.mapper.common.IBaseMapper;

/**
 * service类生成器
 *
 */
@Deprecated
public class ServiceGenerator extends BaseJavaGenerator {

	public ServiceGenerator() {
		super();
	}

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		super.calculateJavaFileAttributes(introspectedTable.getFullyQualifiedTable());
		CommentGenerator commentGenerator = context.getCommentGenerator();
		String entityClassName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();

		TopLevelClass topLevelClass = new TopLevelClass("serviceType");
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		commentGenerator.addJavaFileComment(topLevelClass);

		topLevelClass.addImportedType(new FullyQualifiedJavaType(IBaseMapper.class.getName()));
		// topLevelClass.addImportedType(new
		// FullyQualifiedJavaType(BaseService.class.getName()));
		topLevelClass.addImportedType(new FullyQualifiedJavaType(poType));
		topLevelClass.addImportedType("serviceInterfaceType");
		// topLevelClass.setSuperClass(new
		// FullyQualifiedJavaType(BaseService.class.getName()+ "<" + poType + ",
		// " + pkType + ">"));
		topLevelClass.addSuperInterface(new FullyQualifiedJavaType("serviceInterfaceType"));
		topLevelClass.addAnnotation("@Service");
		topLevelClass.addImportedType(new FullyQualifiedJavaType(Service.class.getName()));
		addDaoField(topLevelClass, entityClassName);
		addDaoMethod(topLevelClass, entityClassName);

		List<CompilationUnit> answer = new ArrayList<>();
		answer.add(topLevelClass);

		return answer;
	}

	// 生成dao接口字段
	private void addDaoField(TopLevelClass topLevelClass, String entityClassName) {
		topLevelClass.addImportedType(new FullyQualifiedJavaType(mapperType));

		Field field = new Field(lowcaseFirstChar(entityClassName) + "Dao", new FullyQualifiedJavaType(mapperType));
		field.setVisibility(JavaVisibility.PRIVATE);
		field.addAnnotation("@Autowired");
		topLevelClass.addImportedType(new FullyQualifiedJavaType(Autowired.class.getName()));
		topLevelClass.addField(field);
	}

	// 生成getDao()方法
	private void addDaoMethod(TopLevelClass topLevelClass, String entityClassName) {
		Method method = new Method("getDao");
		method.setReturnType(
				new FullyQualifiedJavaType(IBaseMapper.class.getName() + "<" + poType + ", " + pkType + ">"));
		method.addBodyLine("return " + lowcaseFirstChar(entityClassName) + "Dao;");
		method.addAnnotation("@Override");
		method.setVisibility(JavaVisibility.PROTECTED);
		topLevelClass.addMethod(method);
	}

	private String lowcaseFirstChar(String s) {
		s = StringUtils.removeEnd(s, "PO");

		if (s == null || s.length() == 0) {
			return s;
		}

		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}
}
