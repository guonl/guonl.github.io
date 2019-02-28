---
layout: post
title: Spring分布式事务配置(atomikos)
categories: Spring
description: Spring分布式事务配置(atomikos)
keywords: spring,事务
---
### 开发原因
在Java后端开发过程中事务控制非常重要，而Spring为我们提供了方便的声明式事务方法@transactional。但是默认的Spring事务只支持单数据源，而实际上一个系统往往需要写多个数据源，这个时候我们就需要考虑如何通过Spring实现对分布式事务的支持。

### 开发组件
框架：SpringBoot
组件：Atomikos
IDE：Intellij
### 开发思路
对于分布式事务而言，JTA是一个不错的解决方案，通常JTA需要应用服务器的支持，但在查阅SpringBoot的文档时发现，它推荐了Atomikos和Bitronix两种无需服务器支持的分布式事务组件，文档内容如下：
> Spring Boot supports distributed JTA transactions across multiple XA resources using either an Atomikos or Bitronix embedded transaction manager. JTA transactions are also supported when deploying to a suitable Java EE Application Server.

在这两个组件中，Atomikos更受大家的好评，所以我选择使用它：
> Atomikos is a popular open source transaction manager which can be embedded into your Spring Boot application. You can use the spring-boot-starter-jta-atomikos Starter POM to pull in the appropriate Atomikos libraries. Spring Boot will auto-configure Atomikos and ensure that appropriate depends-on settings are applied to your Spring beans for correct startup and shutdown ordering.

### 实现细节
#### Pom依赖
就如上面文档内容所说，要在SpringBoot中使用atomikos，仅需要添加一个依赖，这也是SpringBoot非常便利的地方：
```xml
 <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jta-atomikos</artifactId>
    </dependency>
```

#### Atomikos配置
值得一提的是，Spring支持通过xml配置bean，和通过annotation配置bean两种方式，在这里我们采用后者，因为xml方式真是太烦人。方式的配置方法其实很简单，只需要在注解了@Configuration的类里面，通过@Bean来配置，详细的配置内容如下：
```java
/************************** atomikos 多数据源配置 ***************************/

/*------- db1 -------*/

/**
 * db1的 XA datasource
 *
 * @return
 */
@Bean
@Primary
@Qualifier("db1")
public AtomikosDataSourceBean db1DataSourceBean() {
    AtomikosDataSourceBean atomikosDataSourceBean = new AtomikosDataSourceBean();
    atomikosDataSourceBean.setUniqueResourceName("db1");
    atomikosDataSourceBean.setXaDataSourceClassName(
            "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
    Properties properties = new Properties();
    properties.put("URL", db1_url);
    properties.put("user", db1_username);
    properties.put("password", db1_password);
    atomikosDataSourceBean.setXaProperties(properties);
    return atomikosDataSourceBean;
}

/**
 * 构造db1 sessionFactory
 *
 * @return
 */
@Bean
@Autowired
public AnnotationSessionFactoryBean sessionFactory(@Qualifier("db1") AtomikosDataSourceBean atomikosDataSourceBean) {
    AnnotationSessionFactoryBean sessionFactory = new AnnotationSessionFactoryBean();
    sessionFactory.setDataSource(atomikosDataSourceBean);
    sessionFactory.setPackagesToScan(db1_entity_package);
    Properties properties = new Properties();
    properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLInnoDBDialect");
    properties.put("hibernate.show_sql", "false");
    properties.put("hibernate.format_sql", "format");
    properties.put("hibernate.connection.autocommit", "true");
    properties.put("hibernate.connection.url",
	 atomikosDataSourceBean.getXaProperties().get("URL"));
    properties.put("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
    sessionFactory.setHibernateProperties(properties);
    return sessionFactory;
}

/*------- db2 -------*/

/**
 * db2的 XA datasource
 *
 * @return
 */
@Bean
@Qualifier("db2")
public AtomikosDataSourceBean db2DataSourceBean() {
    AtomikosDataSourceBean atomikosDataSourceBean = new AtomikosDataSourceBean();
    atomikosDataSourceBean.setUniqueResourceName("db2");
    atomikosDataSourceBean.setXaDataSourceClassName(
            "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
    Properties properties = new Properties();
    properties.put("URL", db2_url);
    properties.put("user", db2_username);
    properties.put("password", db2_password);
    atomikosDataSourceBean.setXaProperties(properties);
    return atomikosDataSourceBean;
}

/**
 * 构造db2 sessionFactory
 *
 * @return
 */
@Bean
@Autowired
public AnnotationSessionFactoryBean db2SessionFactory(
        @Qualifier("db2") AtomikosDataSourceBean atomikosDataSourceBean) {
    AnnotationSessionFactoryBean sessionFactory = new AnnotationSessionFactoryBean();
    sessionFactory.setDataSource(atomikosDataSourceBean);
    sessionFactory.setPackagesToScan(db2_entity_package);
    Properties properties = new Properties();
    properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLInnoDBDialect");
    properties.put("hibernate.show_sql", "false");
    properties.put("hibernate.format_sql", "format");
    properties.put("hibernate.connection.autocommit", "true");
    properties.put("hibernate.connection.url",
	 atomikosDataSourceBean.getXaProperties().get("URL"));
    properties.put("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
    sessionFactory.setHibernateProperties(properties);
    return sessionFactory;
}

/*--------- atomikos -----------*/

/**
 * transaction manager
 *
 * @return
 */
@Bean(destroyMethod = "close", initMethod = "init")
public UserTransactionManager userTransactionManager() {
    UserTransactionManager userTransactionManager = new UserTransactionManager();
    userTransactionManager.setForceShutdown(false);
    return userTransactionManager;
}

/**
 * jta transactionManager
 *
 * @return
 */
@Bean
public JtaTransactionManager transactionManager() {
    JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
    jtaTransactionManager.setTransactionManager(userTransactionManager());
    return jtaTransactionManager;
}
```

然后在该配置类上，通过@EnableTransactionManagement来启用事务管理，该注解会自动通过by-type查找满足条件的PlatformTransactionManager。其实通过上面的范例可以发现，该配置与我们通常单数据源配置所不同的是使用了AtomikosDataSourceBean来配置数据源，以及定义了UserTransactionManager，更详细的配置方法可以参见Atomikos Spring Integration。

Atomikos的参数配置可以通过jta.propertis来配置，这里我主要配置了日志的输出位置：
```java
# log
com.atomikos.icatch.service=com.atomikos.icatch.standalone.UserTransactionServiceFactory
com.atomikos.icatch.log_base_dir=translogs
```
一开始我觉得这不过是Atomikos自己打的一些纪录日志，没什么用，干脆关掉得了，但通过查阅资料发现并不是这样。Atomikos就是通过这些日志来保障事务过程的（比如进程挂了后怎么恢复），所以千万不能关，关于这点可参考文章扯淡下XA事务。

至此为止，配置就完成了，之后只需要再需要事务控制的地方加上@transactional注解即可。

#### 测试
测试用的MultiDataSourceTransTest类：
```java
@Autowired
private DB1TestDao db1Dao;

@Autowired
private DB2TestDaO db2Dao;

@Test
@Transactional
public void testMulitSourceTransaction() {
    db1Dao.saveOrUpdate(new TestEntity());
    db2Dao.saveOrUpdate(new TestEntity());
}

@Test
@Transactional
@Rollback(false)
public void testMulitSourceTransactionWithOutRollBack() {
    db1Dao.saveOrUpdate(new TestEntity());
    db2Dao.saveOrUpdate(new TestEntity());
}
```
关于SpringBoot的单元测试配置请参见AOP之AntiXSS中的范例，在SpringBoot的测试中，默认带有@transactionl的测试会回滚，也就是执行完了啥也没变，所以可以通过@Rollback(false)来强制不回滚，通过对比回滚和不回滚的执行结果，就能测试分布式事务是否得到了支持。

参考链接：
- [http://sadwxqezc.github.io/HuangHuanBlog/framework/2016/05/29/Spring%E5%88%86%E5%B8%83%E5%BC%8F%E4%BA%8B%E5%8A%A1%E9%85%8D%E7%BD%AE.html](http://sadwxqezc.github.io/HuangHuanBlog/framework/2016/05/29/Spring%E5%88%86%E5%B8%83%E5%BC%8F%E4%BA%8B%E5%8A%A1%E9%85%8D%E7%BD%AE.html)