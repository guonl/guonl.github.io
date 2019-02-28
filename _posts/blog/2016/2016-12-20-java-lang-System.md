---
layout: post
title: java.lang.System.getProperties()
categories: Java
description: java.lang.System.getProperties()
keywords: Java,java,system
---
```java
public static Properties getProperties()
```
确定当前的系统属性。 
首先，如果有安全管理器，则不带参数直接调用其 checkPropertiesAccess 方法。这可能导致一个安全性异常。

将 <font color='red'>getProperty(String)</font> 方法使用的当前系统属性集合作为 Properties 对象返回。如果没有当前系统属性集合，则先创建并初始化一个系统属性集合。这个系统属性集合总是包含以下键的值： 

| 键  | 相关值的描述  |
| ------------ | ------------ |
| java.version  | Java 运行时环境版本  |
| java.vendor  | Java 运行时环境供应商  |
| java.vendor.url  | Java 供应商的 URL  |
| java.home  | Java 安装目录  |
| java.vm.specification.version  | Java 虚拟机规范版本  |
| java.vm.specification.vendor  | Java 虚拟机规范供应商  |
| java.vm.specification.name  | Java 虚拟机规范名称  |
|  java.vm.version | Java 虚拟机实现版本  |
| java.vm.vendor  | Java 虚拟机实现供应商  |
| java.vm.name  |Java 虚拟机实现名称   |
| java.specification.version  | Java 运行时环境规范版本  |
| java.specification.vendor  |  Java 运行时环境规范供应商 |
| java.specification.name  | Java 运行时环境规范名称  |
|  java.class.version |  Java 类格式版本号 |
| java.class.path  | Java 类路径  |
| java.library.path  | 加载库时搜索的路径列表  |
| java.io.tmpdir  | 默认的临时文件路径  |
| java.compiler  | 要使用的 JIT 编译器的名称  |
| java.ext.dirs  | 一个或多个扩展目录的路径  |
| os.name  | 操作系统的名称  |
| os.arch  | 操作系统的架构  |
| os.version  | 操作系统的版本  |
| file.separator  | 文件分隔符（在 UNIX 系统中是“/”）  |
| path.separator  | 路径分隔符（在 UNIX 系统中是“:”）  |
| line.separator  |  行分隔符（在 UNIX 系统中是“/n”） |
| user.name  | 用户的账户名称  |
| user.home  | 用户的主目录  |
| <font color='red'>user.dir</font>  |  用户的当前工作目录 |

**System.getProperty("user.dir")  当前工程路径**
**(Test.class.getPackage().getName()).replaceAll("//.","/")   当前包路径。**

```java
package test.mybatis.mapper;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created on 2016/12/20 0020.
 *
 * @author zlf
 * @since 1.0
 */
public class SystemPropertiesTest {
    public static void main(String[] args) throws MalformedURLException, URISyntaxException {

        System.out.println("java.home : " + System.getProperty("java.home"));

        System.out.println("java.class.version : " + System.getProperty("java.class.version"));

        System.out.println("java.class.path : " + System.getProperty("java.class.path"));

        System.out.println("java.library.path : " + System.getProperty("java.library.path"));

        System.out.println("java.io.tmpdir : " + System.getProperty("java.io.tmpdir"));

        System.out.println("java.compiler : " + System.getProperty("java.compiler"));

        System.out.println("java.ext.dirs : " + System.getProperty("java.ext.dirs"));

        System.out.println("user.name : " + System.getProperty("user.name"));

        System.out.println("user.home : " + System.getProperty("user.home"));

        System.out.println("user.dir : " + System.getProperty("user.dir"));

        System.out.println("===================");

        System.out.println("package: " + SystemPropertiesTest.class.getPackage().getName());

        System.out.println("package: " + SystemPropertiesTest.class.getPackage().toString());

        System.out.println("=========================");

        String packName = SystemPropertiesTest.class.getPackage().getName();

                /*URL packurl = new URL(packName);

                System.out.println(packurl.getPath());*/

        URI packuri = new URI(packName);

        System.out.println(packuri.getPath());

        //System.out.println(packuri.toURL().getPath());

        System.out.println(packName.replaceAll("//.", "/"));

        System.out.println(System.getProperty("user.dir") + "/" + (SystemPropertiesTest.class.getPackage().getName()).replaceAll("//.", "/") + "/");

    }
}

```

输出
```java
java.home : E:\java\jdk1.8.0_45\jre
java.class.version : 52.0
java.class.path : E:\java\jdk1.8.0_45\jre\lib\charsets.jar;E:\java\jdk1.8.0_45\jre\lib\deploy.jar;E:\java\jdk1.8.0_45\jre\lib\ext\access-bridge-64.jar;E:\java\jdk1.8.0_45\jre\lib\ext\cldrdata.jar;E:\java\jdk1.8.0_45\jre\lib\ext\dnsns.jar;E:\java\jdk1.8.0_45\jre\lib\ext\jaccess.jar;E:\java\jdk1.8.0_45\jre\lib\ext\jfxrt.jar;E:\java\jdk1.8.0_45\jre\lib\ext\localedata.jar;E:\java\jdk1.8.0_45\jre\lib\ext\nashorn.jar;E:\java\jdk1.8.0_45\jre\lib\ext\sunec.jar;E:\java\jdk1.8.0_45\jre\lib\ext\sunjce_provider.jar;E:\java\jdk1.8.0_45\jre\lib\ext\sunmscapi.jar;E:\java\jdk1.8.0_45\jre\lib\ext\sunpkcs11.jar;E:\java\jdk1.8.0_45\jre\lib\ext\zipfs.jar;E:\java\jdk1.8.0_45\jre\lib\javaws.jar;E:\java\jdk1.8.0_45\jre\lib\jce.jar;E:\java\jdk1.8.0_45\jre\lib\jfr.jar;E:\java\jdk1.8.0_45\jre\lib\jfxswt.jar;E:\java\jdk1.8.0_45\jre\lib\jsse.jar;E:\java\jdk1.8.0_45\jre\lib\management-agent.jar;E:\java\jdk1.8.0_45\jre\lib\plugin.jar;E:\java\jdk1.8.0_45\jre\lib\resources.jar;E:\java\jdk1.8.0_45\jre\lib\rt.jar;G:\projects\mybatistest\build\classes\test;G:\projects\mybatistest\build\classes\main;G:\projects\mybatistest\build\resources\main;G:\FileGradle\.gradle\caches\modules-2\files-2.1\log4j\log4j\1.2.16\7999a63bfccbc7c247a9aea10d83d4272bd492c6\log4j-1.2.16.jar;G:\FileGradle\.gradle\caches\modules-2\files-2.1\org.mybatis\mybatis\3.2.6\4c53554a5f32a4315eeb4dbf74faf49cec449f5f\mybatis-3.2.6.jar;G:\FileGradle\.gradle\caches\modules-2\files-2.1\mysql\mysql-connector-java\5.1.18\85dfedad243dc0303ad7ae3a323c39421d220690\mysql-connector-java-5.1.18.jar;G:\FileGradle\.gradle\caches\modules-2\files-2.1\junit\junit\4.12\2973d150c0dc1fefe998f834810d68f278ea58ec\junit-4.12.jar;G:\FileGradle\.gradle\caches\modules-2\files-2.1\org.mybatis\mybatis-spring\1.3.0\d1dbdc46cac543447ffd5aeda59f1a9bb34f0912\mybatis-spring-1.3.0.jar;G:\FileGradle\.gradle\caches\modules-2\files-2.1\org.hamcrest\hamcrest-core\1.3\42a25dc3219429f0e5d060061f71acb49bf010a0\hamcrest-core-1.3.jar;E:\Program Files (x86)\JetBrains\IntelliJ IDEA 2016.3\lib\idea_rt.jar;G:\ideaconfig\.IntelliJIdea2016.1\system\groovyHotSwap\gragent.jar
java.library.path : E:\java\jdk1.8.0_45\bin;C:\WINDOWS\Sun\Java\bin;C:\WINDOWS\system32;C:\WINDOWS;F:\android\SDK\platform-tools;F:\nexus-2.3.1-01-bundle\nexus-2.3.1-01\bin;C:\ProgramData\Oracle\Java\javapath;F:\oracle\product\10.2.0\db_2\bin;C:\WINDOWS\system32;C:\WINDOWS;C:\WINDOWS\System32\Wbem;C:\WINDOWS\System32\WindowsPowerShell\v1.0\;E:\java\jdk1.8.0_45\bin;E:\java\jdk1.8.0_45\jre\bin;E:\Program Files (x86)\MySQL\MySQL Server 5.5\bin;F:\apache-maven-3.1.1\bin;F:\gradle-2.5-all\bin;E:\Program Files\TortoiseSVN\bin;F:\groovy-2.4.4\bin;C:\Program Files (x86)\NVIDIA Corporation\PhysX\Common;e:\Program Files (x86)\Git\cmd;F:\lib\spring-boot-cli-1.3.5.RELEASE-bin\spring-1.3.5.RELEASE\bin;C:\Program Files\nodejs\;F:\android\SDK\tools;D:\React_Native\watchman;C:\Users\Administrator\AppData\Local\Programs\Python\Python35\Scripts\;C:\Users\Administrator\AppData\Local\Programs\Python\Python35\;C:\Users\Administrator\AppData\Roaming\npm;.
java.io.tmpdir : C:\Users\ADMINI~1\AppData\Local\Temp\
java.compiler : null
java.ext.dirs : E:\java\jdk1.8.0_45\jre\lib\ext;C:\WINDOWS\Sun\Java\lib\ext
user.name : Administrator
user.home : C:\Users\Administrator
user.dir : G:\projects\mybatistest
===================
package: test.mybatis.mapper
package: package test.mybatis.mapper
=========================
test.mybatis.mapper
test.mybatis.mapper
G:\projects\mybatistest/test.mybatis.mapper/
```
