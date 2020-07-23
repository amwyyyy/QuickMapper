package com.wen.mapper.generator.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * dto类生成器
 * @author huangwg
 *
 */
@Deprecated
public class DtoGenerator extends BaseJavaGenerator {

	public DtoGenerator() {
		super();
	}

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		calculateJavaFileAttributes(introspectedTable.getFullyQualifiedTable());
		
		CommentGenerator commentGenerator = context.getCommentGenerator();
		FullyQualifiedJavaType type = new FullyQualifiedJavaType("dtoType");
		TopLevelClass topLevelClass = new TopLevelClass(type);
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		commentGenerator.addJavaFileComment(topLevelClass);
		topLevelClass.setSuperClass(introspectedTable.getBaseRecordType());
		topLevelClass.addImportedType(introspectedTable.getBaseRecordType());
		topLevelClass.addSuperInterface(new FullyQualifiedJavaType(Serializable.class.getName()));
		topLevelClass.addImportedType(new FullyQualifiedJavaType(Serializable.class.getName()));
		addSerialVersionUID(topLevelClass);
		
		List<CompilationUnit> answer = new ArrayList<>();
		if (context.getPlugins().modelExampleClassGenerated(topLevelClass, introspectedTable)) {
			answer.add(topLevelClass);
		}
		return answer;
	}
	
	private void addSerialVersionUID(TopLevelClass topLevelClass) {
		Field field = new Field("serialVersionUID", new FullyQualifiedJavaType(long.class.getName()));
		field.setFinal(true);
		field.setStatic(true);
		field.setVisibility(JavaVisibility.PRIVATE);
		field.setInitializationString(createSerialVersionUID(topLevelClass.getType().getFullyQualifiedName()));
		topLevelClass.addField(field);
	}
	
	private String createSerialVersionUID(String className){
		long hashCode = (className + System.currentTimeMillis()).hashCode();
		return hashCode + "L";
	}

}