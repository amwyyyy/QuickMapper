package com.wen.mapper.generator;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.config.PropertyRegistry;

import com.wen.mapper.orm.annotation.Column;
import com.wen.mapper.orm.annotation.Entity;


/**
 * Mybatis Generator 插件
 * @author denis.huang
 * @since 2016-10-10
 */
public class MybatisGeneratorPlugin extends PluginAdapter {
	//自定义生成器
	private AbstractJavaGenerator[] extJavaFileGenerators = 
			new AbstractJavaGenerator[]{/** new ServiceGenerator(), new ServiceInterfaceGenerator()*/};
	
	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	/**
	 * 生成po文件
	 */
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		topLevelClass.addImportedType(Column.class.getName());
		topLevelClass.addImportedType(Entity.class.getName());
		
		String sb = "@" + Entity.class.getSimpleName() + "(" +
				"tableName=\"" + introspectedTable.getTableConfiguration().getTableName() + "\"" +
				")";
		topLevelClass.addAnnotation(sb);

		return true;
	}

	@Override
	public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
			IntrospectedTable introspectedTable, Plugin.ModelClassType modelClassType) {
		StringBuilder sb = new StringBuilder();
		sb.append("@").append(Column.class.getSimpleName()).append("(");
		sb.append("name=\"").append(introspectedColumn.getActualColumnName()).append("\"");
		sb.append(", jdbcType=\"").append(introspectedColumn.getJdbcTypeName()).append("\"");

		if (introspectedColumn.isIdentity()
				|| isPrimaryKeyColumn(introspectedTable.getPrimaryKeyColumns(), introspectedColumn)) {
			sb.append(", pk=true");
		}

		if (introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName().equals(Double.class.getName())
				|| introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName().equals(Float.class.getName())) {
			sb.append(", precision=").append(introspectedColumn.getLength());
			sb.append(", scale=").append(introspectedColumn.getScale());
		}

		if (introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName().equals(String.class.getName())) {
			sb.append(", length=").append(introspectedColumn.getLength());
		}

		if (introspectedColumn.getRemarks() != null && !introspectedColumn.getRemarks().equals("")) {
			sb.append(", remark=\"").append(introspectedColumn.getRemarks()).append("\"");
		}
		sb.append(")");
		field.addAnnotation(sb.toString());

		return true;
	}

	@Override
	public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
		return true;
	}

	private boolean isPrimaryKeyColumn(List<IntrospectedColumn> primaryKeyColumns, IntrospectedColumn introspectedColumn) {
		if (primaryKeyColumns == null || primaryKeyColumns.isEmpty() || introspectedColumn == null) {
			return false;
		}

		for (IntrospectedColumn primaryKeyColumn : primaryKeyColumns) {
			if (primaryKeyColumn.getActualColumnName().equalsIgnoreCase(introspectedColumn.getActualColumnName())) {
				return true;
			}
		}

		return false;
	}

//	private void addSerialVersionUID(TopLevelClass topLevelClass) {
//		Field field = new Field("serialVersionUID", new FullyQualifiedJavaType(long.class.getName()));
//		field.setFinal(true);
//		field.setStatic(true);
//		field.setVisibility(JavaVisibility.PRIVATE);
//		field.setInitializationString(createSerialVersionUID(topLevelClass.getType().getFullyQualifiedName()));
//		topLevelClass.addField(field);
//	}
	
//	private String createSerialVersionUID(String className){
//		long hashCode = (className + System.currentTimeMillis()).hashCode();
//		return hashCode + "L";
//	}
	
    @Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }
    
    //生成自定义java文件
    @Override
	public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> answer = new ArrayList<>();

        for (AbstractJavaGenerator javaGenerator : extJavaFileGenerators) {
        	javaGenerator.setIntrospectedTable(introspectedTable);
        	javaGenerator.setContext(context);
            List<CompilationUnit> compilationUnits = javaGenerator
                    .getCompilationUnits();
            for (CompilationUnit compilationUnit : compilationUnits) {
                GeneratedJavaFile gjf = new GeneratedJavaFile(compilationUnit,
                        context.getJavaModelGeneratorConfiguration()
                                .getTargetProject(),
                                context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING),
                                context.getJavaFormatter());
                answer.add(gjf);
            }
        }

        return answer;
    }
}
