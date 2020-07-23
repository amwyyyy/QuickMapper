# 1、简介
	本项目主要是整合mybatis-generator，PageHelper和动态生成通用crud方法，以达到快速开发的目的。

# 2、概要说明
	1.项目需要配合spring使用，使用JDK1.7。
	2.项目通过重写SqlSessionFactoryBean来增强mybatis,在启动时自动生成通用crud方法节点，通用的方法不需要每个实体类的mapper文件中再写一遍。
	3.集成mybatis-generator省去创建文件的麻烦，并对mybatis-generator进行了扩展，让生成的文件更好的配合通用crud方法使用。
	4.因个人水平有限，项目还不完整，任何希望使用本工具的人或者组织，请自行斟酌，对于因为我的代码造成的损失，我不负任何责任。

# 3、使用说明
	1.导入jar包或使用maven的加上依赖。
	2.spring配置文件中配置sqlSessionFactory：
	<!-- mybatis配置 -->
	<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
		<property name="dataSource" ref="dataSource"/>
		<property name="configLocation" value="classpath:mybatis-configuration.xml"/>
		<property name="mapperLocations" value="classpath*:/mapper/*.xml"/>
	</bean>
	
	<!-- 通用CRUD初始化 -->
	<bean class="com.wen.mapper.orm.QuickMapperGenerator">
		<property name="sqlSessionFactory" ref="sqlSessionFactory"/>
		<property name="entityPackage" value="com.test.project.db.po"/>
	</bean>
	
	<!-- 动态代理Mapper配置 -->
	<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
		<property name="basePackage" value="com.test.project.db.mapper" />
	</bean>
	3.写一个main方法来启动mybatis-generator：
		public class GeneratorMain {
			public static void main(String[] args) throws Exception {
				MybatisGeneratorBuilder builder = new MybatisGeneratorBuilder();
				builder.setJdbcConnection("com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/test", 
						"root", "123", "lib\\mysql-connector-java-5.1.28.jar");	//配置数据连接
				builder.setBasePackage("com.test.project");	//生成文件的根目录
				builder.addTable("test", "t_order", "Order");	//添加要生成的表
				builder.addTable("test", "t_order_place", "OrderPlace");
				builder.build();
			}
		}
		重新生成只会覆盖entity，所以增删字段后重生成不怕覆盖其它文件
# 4、未来
	项目还在改进阶段，如果有什么好的建议可以留言。
