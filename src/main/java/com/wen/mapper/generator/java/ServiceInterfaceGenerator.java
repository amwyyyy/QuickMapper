package com.wen.mapper.generator.java;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;


/**
 * serivce接口类生成器
 */
@Deprecated
public class ServiceInterfaceGenerator extends BaseJavaGenerator {

	public ServiceInterfaceGenerator() {
		super();
	}

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		super.calculateJavaFileAttributes(introspectedTable.getFullyQualifiedTable());
		CommentGenerator commentGenerator = context.getCommentGenerator();
						
		Interface interfaze = new Interface(new FullyQualifiedJavaType("serviceInterfaceType"));
		interfaze.setVisibility(JavaVisibility.PUBLIC);
		commentGenerator.addJavaFileComment(interfaze);

//		interfaze.addImportedType(new FullyQualifiedJavaType(IBaseService.class.getName()));
		interfaze.addImportedType(new FullyQualifiedJavaType(poType));
//		interfaze.addSuperInterface(new FullyQualifiedJavaType(IBaseService.class.getName() + "<" + entityType + ", " + pkType + ">"));
		
		List<CompilationUnit> answer = new ArrayList<>();
		answer.add(interfaze);

		return answer;
	}
}
