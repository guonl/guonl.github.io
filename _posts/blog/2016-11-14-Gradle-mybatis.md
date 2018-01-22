---
layout: post
title: Gradle中使用Mybatis Generator生成bo和mapper
categories: Gradle
description: Gradle中使用Mybatis Generator生成bo和mapper
keywords: Gradle
---
## 前言
[Mybatis Generator](http://www.mybatis.org/generator/index.html)是一个mybatis工具项目，用于生成mybatis的model,mapper,dao持久层代码。**Mybatis Generator**提供了maven plugin,ant target，java三种方式启动。现在主流的构建工具是[Gradle](https://gradle.org/),虽然mybatis generator没有提供gradle的插件，但gradle可以调用ant任务，因此，gradle也能启动Mybatis Generator。

## 环境说明
- 数据库:mysql
- 数据库配置文件:src/main/resources/jdbc.properties
- 项目中使用了通用mapper 3.3.2 插件

## 项目依赖
在**build.gradle**中:
运行ant需要运行环境，也就是相应的jar包，因此添加一个配置
```groovy
configurations {
    mybatisGenerator
}
```
<!--more-->
给这个配置添加依赖
```groovy
dependencies {
    mybatisGenerator "org.mybatis.generator:mybatis-generator-core:${generatorVersion}"
    mybatisGenerator "mysql:mysql-connector-java:${mysqlVersion}"
    mybatisGenerator "tk.mybatis:mapper:${mybatisMapperVersion}"
}
```
## 配置task
```groovy
def getDbProperties = {
    def properties = new Properties()
    file("src/main/resources/jdbc.properties").withInputStream { inputStream ->
        properties.load(inputStream)
    }
    properties;
}
task mybatisGenerate << {
    def properties = getDbProperties()
    ant.properties['targetProject'] = projectDir.path
    ant.properties['driverClass'] = properties.getProperty("jdbc.driverClassName")
    ant.properties['connectionURL'] = properties.getProperty("jdbc.url")
    ant.properties['userId'] = properties.getProperty("jdbc.username")
    ant.properties['password'] = properties.getProperty("jdbc.password")
    ant.properties['src_main_java'] = sourceSets.main.java.srcDirs[0].path
    ant.properties['src_main_resources'] = sourceSets.main.resources.srcDirs[0].path
    ant.properties['modelPackage'] = this.modelPackage
    ant.properties['mapperPackage'] = this.mapperPackage
    ant.properties['sqlMapperPackage'] = this.sqlMapperPackage
    ant.taskdef(
            name: 'mbgenerator',
            classname: 'org.mybatis.generator.ant.GeneratorAntTask',
            classpath: configurations.mybatisGenerator.asPath
    )
    ant.mbgenerator(overwrite: true,
            configfile: 'src/main/resources/generatorConfig.xml', verbose: true) {
        propertyset {
            propertyref(name: 'targetProject')
            propertyref(name: 'userId')
            propertyref(name: 'driverClass')
            propertyref(name: 'connectionURL')
            propertyref(name: 'password')
            propertyref(name: 'src_main_java')
            propertyref(name: 'src_main_resources')
            propertyref(name: 'modelPackage')
            propertyref(name: 'mapperPackage')
            propertyref(name: 'sqlMapperPackage')
        }
    }
}
```
大致思路
- 从*jdbc.propertis*读取配置
- *把配置注入ant任务*
- *运行ant*生成文件

jdbc.properties
```groovy
jdbc.driverClassName=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://127.0.0.1:3306/userdb?useUnicode=true&characterEncoding=utf8&autoReconnect=true&allowMultiQueries=true
jdbc.username=root
jdbc.password=admin
```

其他配置*gradle.propertis*
```groovy
#生成实体类所在的包
modelPackage=test.mybatis.pojo
#生成的mapper接口类所在包
mapperPackage=test.mybatis.mapper
#生成的mapper xml文件所在包，默认存储在resources目录下
sqlMapperPackage=mybatis
```

## generatorConfig配置
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">
<generatorConfiguration>
    <context id="Mysql" targetRuntime="MyBatis3Simple" defaultModelType="flat">
        <commentGenerator>
            <property name="suppressAllComments" value="false"></property>
            <property name="suppressDate" value="true"></property>
            <property name="javaFileEncoding" value="utf-8"/>
        </commentGenerator>

        <jdbcConnection driverClass="${driverClass}"
                        connectionURL="${connectionURL}"
                        userId="${userId}"
                        password="${password}">
        </jdbcConnection>

        <javaTypeResolver >
            <property name="forceBigDecimals" value="false" />
        </javaTypeResolver>

        <javaModelGenerator targetPackage="${modelPackage}" targetProject="${src_main_java}">
            <property name="enableSubPackages" value="false"></property>
            <property name="trimStrings" value="true"></property>
        </javaModelGenerator>

        <sqlMapGenerator targetPackage="${sqlMapperPackage}" targetProject="${src_main_resources}">
            <property name="enableSubPackages" value="false"></property>
        </sqlMapGenerator>


        <javaClientGenerator targetPackage="${mapperPackage}" targetProject="${src_main_java}" type="XMLMAPPER">
            <property name="enableSubPackages" value="false"/>
        </javaClientGenerator>

        <table tableName="ta_user" enableCountByExample="false"
               enableDeleteByExample="false"
               enableSelectByExample="false"
               enableUpdateByExample="false">
            <generatedKey column="id" sqlStatement="MySql" identity="true"/>
        </table>
    </context>
</generatorConfiguration>
```
## run生成代码
```groovy
gradle  mybatisGenerate
```
## 项目目录结构
![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/gradle_mybatis_generator.png)

dependencies.gradle配置
```groovy
ext {
    //gradle
    gradleVersion = '2.5'
    //junit
    junitVersion = '4.12'
    //log4j
    log4jVersion = '1.2.16'
    //mybatis
    mybatisMapperVersion = '3.3.2'
    //mybatis
    mybatisVersion = '3.2.6'
    //mysql驱动
    mysqlVersion = '5.1.18'
    //mybatis-spring
    mybatisSpringVersion = '1.3.0'
    //generatorVersion
    generatorVersion = '1.3.2'
}
```
build.gradle完整配置
```groovy
group 'test.mybatis'
version '1.0-SNAPSHOT'

apply from: "${rootDir}/gradle/dependencies.gradle"

apply plugin: 'java'
apply plugin: 'war'

configurations {
    mybatisGenerator
}

repositories {
    mavenCentral()
}
dependencies {
    mybatisGenerator "org.mybatis.generator:mybatis-generator-core:${generatorVersion}"
    mybatisGenerator "mysql:mysql-connector-java:${mysqlVersion}"
    mybatisGenerator "tk.mybatis:mapper:${mybatisMapperVersion}"
}



compileJava {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
//    compile 'tk.mybatis:mapper:3.3.9'
    compile "log4j:log4j:${log4jVersion}"
    compile "org.mybatis:mybatis:${mybatisVersion}"
    compile "mysql:mysql-connector-java:${mysqlVersion}"
    compile "junit:junit:${junitVersion}"
    compile "org.mybatis:mybatis-spring:${mybatisSpringVersion}"
}

def getDbProperties = {
    def properties = new Properties()
    file("src/main/resources/jdbc.properties").withInputStream { inputStream ->
        properties.load(inputStream)
    }
    properties;
}
task mybatisGenerate << {
    def properties = getDbProperties()
    ant.properties['targetProject'] = projectDir.path
    ant.properties['driverClass'] = properties.getProperty("jdbc.driverClassName")
    ant.properties['connectionURL'] = properties.getProperty("jdbc.url")
    ant.properties['userId'] = properties.getProperty("jdbc.username")
    ant.properties['password'] = properties.getProperty("jdbc.password")
    ant.properties['src_main_java'] = sourceSets.main.java.srcDirs[0].path
    ant.properties['src_main_resources'] = sourceSets.main.resources.srcDirs[0].path
    ant.properties['modelPackage'] = this.modelPackage
    ant.properties['mapperPackage'] = this.mapperPackage
    ant.properties['sqlMapperPackage'] = this.sqlMapperPackage
    ant.taskdef(
            name: 'mbgenerator',
            classname: 'org.mybatis.generator.ant.GeneratorAntTask',
            classpath: configurations.mybatisGenerator.asPath
    )
    ant.mbgenerator(overwrite: true,
            configfile: 'src/main/resources/generatorConfig.xml', verbose: true) {
        propertyset {
            propertyref(name: 'targetProject')
            propertyref(name: 'userId')
            propertyref(name: 'driverClass')
            propertyref(name: 'connectionURL')
            propertyref(name: 'password')
            propertyref(name: 'src_main_java')
            propertyref(name: 'src_main_resources')
            propertyref(name: 'modelPackage')
            propertyref(name: 'mapperPackage')
            propertyref(name: 'sqlMapperPackage')
        }
    }
}
```

刷新gradle出现mybatisGenerate
![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/gradle_mybatisgenerate.png)

完整项目地址：[mybatisDemo](https://github.com/longfeizheng/mybatisDemo)

参考链接

- [http://chenkaihua.com/2015/12/19/running-mybatis-generator-with-gradle/](http://chenkaihua.com/2015/12/19/running-mybatis-generator-with-gradle/)