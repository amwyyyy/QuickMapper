package com.wen.mapper.generator.java;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.codegen.AbstractXmlGenerator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.wen.mapper.common.IBaseMapper;
import com.wen.mapper.generator.xml.MapperXmlGenerator;


/**
 * Mapper类生成器
 * mapper的产生通过配置文件来指定，不让插件生成mapper.xml内容，而由代码动态生成
 */
public class MapperGenerator extends BaseJavaGenerator {

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		super.calculateJavaFileAttributes(introspectedTable.getFullyQualifiedTable());
		CommentGenerator commentGenerator = context.getCommentGenerator();
				
		FullyQualifiedJavaType type = new FullyQualifiedJavaType(mapperType);
		Interface interfaze = new Interface(type);
		interfaze.setVisibility(JavaVisibility.PUBLIC);
		commentGenerator.addJavaFileComment(interfaze);

		interfaze.addImportedType(new FullyQualifiedJavaType(IBaseMapper.class.getName()));//导入包Import
		interfaze.addImportedType(new FullyQualifiedJavaType(poType));
		interfaze.addSuperInterface(new FullyQualifiedJavaType(IBaseMapper.class.getName() + "<" + poType + ", "
				+ pkType + ">"));
		
		List<CompilationUnit> answer = new ArrayList<>();
		if (context.getPlugins().clientGenerated(interfaze, null, introspectedTable)) {
			answer.add(interfaze);
		}

		return answer;
	}

	public AbstractXmlGenerator getMatchedXMLGenerator(){
		if(isExist()) return null;
		return new MapperXmlGenerator();
	}
	
	//如果mapper.xml文件已存在不覆盖
	private boolean isExist() {
		String targetPackage = context.getSqlMapGeneratorConfiguration().getTargetPackage();
		String entityClassName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
		
		targetPackage = targetPackage.replaceAll("[.]", "\\"+File.separator);
		Resource resource = new ClassPathResource(targetPackage + File.separator + entityClassName+"Mapper.xml");
		return resource.exists();
	}
}
