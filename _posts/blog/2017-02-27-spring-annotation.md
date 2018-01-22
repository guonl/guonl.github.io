---
layout: post
title: spring框架注解的用法分析
categories: Spring
description: spring框架注解的用法分析
keywords: Spring,spring,注解
---
## 前言
1. `@Component`是Spring定义的一个通用注解，可以注解任何bean。
1. @Scope定义bean的作用域，其默认作用域是”singleton”，除此之外还有prototype，request，session和global session。

### 案例：@Component和@Scope用法分析：
*BeanAnnotation类*：
```java
@Scope
@Component
public class BeanAnnotation {
 
    public void say(String arg) {
        System.out.println("BeanAnnotation : " + arg);
    }
 
    public void myHashCode() {
        System.out.println("BeanAnnotation : " + this.hashCode());
    }
 
}
```
*junit4测试类→TestBeanAnnotation类：*
```java
@RunWith(BlockJUnit4ClassRunner.class)
public class TestBeanAnnotation extends UnitTestBase {
 
    public TestBeanAnnotation() {
        super("classpath*:spring-beanannotation.xml");
    }
 
    @Test
    public void testSay() {
        BeanAnnotation bean = super.getBean("beanAnnotation");
        bean.say("This is test.");
    }
 
    @Test
    public void testScpoe() {
        BeanAnnotation bean = super.getBean("beanAnnotation");
        bean.myHashCode();
        bean = super.getBean("beanAnnotation");
        bean.myHashCode();
    }
 
}
```
*Spring配置文件→spring-beanannotation.xml：*
```xml
<context:component-scan base-package="com.beanannotation"></context:component-scan>
```
我们先从Spring配置文件分析，base-package="com.beanannotation"说明我们只处理这个包名下面的注解。

然后分析BeanAnnotation类，有一个say的方法。假设我们不清楚这是一个什么类型（注：Service或者DAO）的类，我们可以用一个通用的注解`@Component`。

最后分析TestBeanAnnotation类，testSay方法里super.getBean("beanAnnotation")是从IOC的容器中取到这个bean，并调用bean的say方法。

提出问题的时间到了，当我们super.getBean的时候是通过bean的id从IOC容器中获取的，那么这个id是什么呢？因为在我们添加@Component到BeanAnnotation类上的时候，默认的id为beanAnnotation。如果指定了@Component的名称，譬如指定为@Component(”bean”)的时候，在单元测试的时候就必须把super.getBean得到的id与之相对应才能测试成功。

在这里我把@Scope注解单独分离出来分析，在TestBeanAnnotation类里面有一个testScpoe方法。在BeanAnnotation类里面有一个myHashCode方法，可能大家有些疑惑，为什么要用this.hashCode()？因为@Scope指定的是bean的作用域，为了保证测试类的结果准确明了，所以采用哈希码值来判断是否为同一个对象。

3. @Repository、@Service、@Controller是更具有针对性的注解。
PS：这里我们需要明白这三个注解是基于@Component定义的注解哦：
①、@Repository通常用于注解DAO类，也就是我们常说的持久层。
②、@Service通常用于注解Service类，也就是服务层。
③、@Controller通常用于Controller类，也就是控制层（MVC）。
4. @Autowired理解为“传统”的setter方法，可以用在setter方法上，也可以用在构造器或者成员变量，能够进行Spring Bean的自动装配。

### 案例：@Autowired用法分析一：
*Spring配置文件→spring-beanannotation.xml：*
```xml
<context:component-scan base-package="com.beanannotation"></context:component-scan>
```
*SimpleMovieLister类：*
```java
public class SimpleMovieLister {
 
    private MovieFinder movieFinder;
 
    @Autowired(required=false)
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }
 
}
```
在默认的情况下，如果找不到合适的bean将会导致autowiring失败抛出异常，我们可以将@Autowired注解在这个set方法上，标记required=false来避免。但是，这不是一个必须的，如果找不到movieFinder的实例，是不会抛出异常的，只有在使用的时候发现movieFinder为null，在这种情况下，就要求我们在使用的时候，首先判断movieFinder是不是为null，如果是就会报空指针异常 。

值得注意的是，我们知道每个类可以有很多个构造器，但是在使用@Autowired的时候，有且只能有一个构造器能够被标记为required=true（注：required的默认值为false）。


### 案例：@Autowired用法分析二：
*BeanImplOne类：*
```java
@Order
@Component
public class BeanImplOne implements BeanInterface {
 
}
```
*BeanImplTwo类：*
```java
@Order
@Component
public class BeanImplTwo implements BeanInterface {
 
}
```
*BeanInterface类：*
```java
public interface BeanInterface {
 
}
```
*BeanInvoker类：*
```java
@Component
public class BeanInvoker {
 
    @Autowired
    private List<BeanInterface> list;
 
    @Autowired
    private Map<String, BeanInterface> map;
 
    public void say() {
        if (null != list && 0 != list.size()) {
            for (BeanInterface bean : list) {
                System.out.println(bean.getClass().getName());
            }
        } else {
            System.out.println(" list is null !");
        }
 
        if (null != map && 0 != map.size()) {
            for (Map.Entry<String, BeanInterface> entry : map.entrySet()) {
                System.out.println(entry.getKey() + "      " + entry.getValue().getClass().getName());
            }
        } else {
            System.out.println("map is null !");
        }
    }
}
```
*测试类TestInjection：*
```java
@RunWith(BlockJUnit4ClassRunner.class)
public class TestInjection extends UnitTestBase {
 
    public TestInjection() {
        super("classpath:spring-beanannotation.xml");
    }
 
    @Test
    public void testMultiBean() {
        BeanInvoker invoker = super.getBean("beanInvoker");
        invoker.say();
    }
 
}
```

ImportNew
首页所有文章资讯Web架构基础技术书籍教程Java小组工具资源
浅谈Spring框架注解的用法分析
2017/02/20 | 分类： 基础技术 | 0 条评论 | 标签： SPRING
分享到： 9
原文出处： locality
1.@Component是Spring定义的一个通用注解，可以注解任何bean。

2.@Scope定义bean的作用域，其默认作用域是”singleton”，除此之外还有prototype，request，session和global session。

案例：@Component和@Scope用法分析：

BeanAnnotation类：

1
2
3
4
5
6
7
8
9
10
11
12
13
@Scope
@Component
public class BeanAnnotation {
 
    public void say(String arg) {
        System.out.println("BeanAnnotation : " + arg);
    }
 
    public void myHashCode() {
        System.out.println("BeanAnnotation : " + this.hashCode());
    }
 
}
junit4测试类→TestBeanAnnotation类：

1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
@RunWith(BlockJUnit4ClassRunner.class)
public class TestBeanAnnotation extends UnitTestBase {
 
    public TestBeanAnnotation() {
        super("classpath*:spring-beanannotation.xml");
    }
 
    @Test
    public void testSay() {
        BeanAnnotation bean = super.getBean("beanAnnotation");
        bean.say("This is test.");
    }
 
    @Test
    public void testScpoe() {
        BeanAnnotation bean = super.getBean("beanAnnotation");
        bean.myHashCode();
        bean = super.getBean("beanAnnotation");
        bean.myHashCode();
    }
 
}
Spring配置文件→spring-beanannotation.xml：

1
<context:component-scan base-package="com.beanannotation"></context:component-scan>
我们先从Spring配置文件分析，base-package="com.beanannotation"说明我们只处理这个包名下面的注解。

然后分析BeanAnnotation类，有一个say的方法。假设我们不清楚这是一个什么类型（注：Service或者DAO）的类，我们可以用一个通用的注解@Component。

最后分析TestBeanAnnotation类，testSay方法里super.getBean("beanAnnotation")是从IOC的容器中取到这个bean，并调用bean的say方法。

提出问题的时间到了，当我们super.getBean的时候是通过bean的id从IOC容器中获取的，那么这个id是什么呢？因为在我们添加@Component到BeanAnnotation类上的时候，默认的id为beanAnnotation。如果指定了@Component的名称，譬如指定为@Component(”bean”)的时候，在单元测试的时候就必须把super.getBean得到的id与之相对应才能测试成功。

在这里我把@Scope注解单独分离出来分析，在TestBeanAnnotation类里面有一个testScpoe方法。在BeanAnnotation类里面有一个myHashCode方法，可能大家有些疑惑，为什么要用this.hashCode()？因为@Scope指定的是bean的作用域，为了保证测试类的结果准确明了，所以采用哈希码值来判断是否为同一个对象。

3.@Repository、@Service、@Controller是更具有针对性的注解。
PS：这里我们需要明白这三个注解是基于@Component定义的注解哦：
①、@Repository通常用于注解DAO类，也就是我们常说的持久层。
②、@Service通常用于注解Service类，也就是服务层。
③、@Controller通常用于Controller类，也就是控制层（MVC）。

4.@Autowired理解为“传统”的setter方法，可以用在setter方法上，也可以用在构造器或者成员变量，能够进行Spring Bean的自动装配。

案例：@Autowired用法分析一：

Spring配置文件→spring-beanannotation.xml：

1
<context:component-scan base-package="com.beanannotation"></context:component-scan>
SimpleMovieLister类：

1
2
3
4
5
6
7
8
9
10
public class SimpleMovieLister {
 
    private MovieFinder movieFinder;
 
    @Autowired(required=false)
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }
 
}
在默认的情况下，如果找不到合适的bean将会导致autowiring失败抛出异常，我们可以将@Autowired注解在这个set方法上，标记required=false来避免。但是，这不是一个必须的，如果找不到movieFinder的实例，是不会抛出异常的，只有在使用的时候发现movieFinder为null，在这种情况下，就要求我们在使用的时候，首先判断movieFinder是不是为null，如果是就会报空指针异常 。

值得注意的是，我们知道每个类可以有很多个构造器，但是在使用@Autowired的时候，有且只能有一个构造器能够被标记为required=true（注：required的默认值为false）。

案例：@Autowired用法分析二：

BeanImplOne类：

1
2
3
4
5
@Order
@Component
public class BeanImplOne implements BeanInterface {
 
}
BeanImplTwo类：

1
2
3
4
5
@Order
@Component
public class BeanImplTwo implements BeanInterface {
 
}
BeanInterface类：

1
2
3
public interface BeanInterface {
 
}
BeanInvoker类：

1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
@Component
public class BeanInvoker {
 
    @Autowired
    private List<BeanInterface> list;
 
    @Autowired
    private Map<String, BeanInterface> map;
 
    public void say() {
        if (null != list && 0 != list.size()) {
            for (BeanInterface bean : list) {
                System.out.println(bean.getClass().getName());
            }
        } else {
            System.out.println(" list is null !");
        }
 
        if (null != map && 0 != map.size()) {
            for (Map.Entry<String, BeanInterface> entry : map.entrySet()) {
                System.out.println(entry.getKey() + "      " + entry.getValue().getClass().getName());
            }
        } else {
            System.out.println("map is null !");
        }
    }
}
测试类TestInjection：

1
2
3
4
5
6
7
8
9
10
11
12
13
14
@RunWith(BlockJUnit4ClassRunner.class)
public class TestInjection extends UnitTestBase {
 
    public TestInjection() {
        super("classpath:spring-beanannotation.xml");
    }
 
    @Test
    public void testMultiBean() {
        BeanInvoker invoker = super.getBean("beanInvoker");
        invoker.say();
    }
 
}
首先，我们清楚BeanImplOne类和BeanImplTwo类是实现了BeanInterface接口的，在BeanInvoker类里面我们定义了list和map，我们通过@Autowired注解把BeanImplOne类和BeanImplTwo类注解进入其中。那么怎么证实是@Autowired注解把这两个类注入到list或者map中的呢？那么请看if循环语句和foreach循环打印，通过这个逻辑判断，如果能够打印出BeanImplOne类和BeanImplTwo类的路径名，就说明这样是可以的。如果有些小伙伴可能不信，那么可以试着不使用@Autowired注解，看结果怎么样。

测试类没有什么好说的，各位小伙伴有没有注意到@Order注解呢？这里需要解释的就是，如果在@Order注解里面输入执行的数字，比如1或者2，那么打印出来的路径名就会按顺序，也就是说通过指定@Order注解的内容可以实现优先级的功能。

5. @ImportResource注解引入一个资源，对应一个xml文件
6. @Value注解从资源文件中，取出它的key并赋值给当前类的成员变量

### 案例：@ImportResource和@Value用法分析：

*MyDriverManager类：*
```java
public class MyDriverManager {
 
    public MyDriverManager(String url, String userName, String password) {
        System.out.println("url : " + url);
        System.out.println("userName: " + userName);
        System.out.println("password: " + password);
    }
 
}
```
*config.xml:*
```xml
<context:property-placeholder location="classpath:/config.properties"/>
```
*StoreConfig类:*
```java
@Configuration
@ImportResource("classpath:config.xml")
public class StoreConfig {
 
    @Value("${jdbc.url}")
    private String url;
 
    @Value("${jdbc.username}")
    private String username;
 
    @Value("${jdbc.password}")
    private String password;
 
    @Bean
    public MyDriverManager myDriverManager() {
        return new MyDriverManager(url, username, password);
    }
```
这个案例我们使用注解配置jdbc数据库的连接，首先创建一个内含构造器的MyDriverManager类，然后配置config.xml里面的资源文件路径，以便@ImportResource注解获取，最后配置StoreConfig类。（注意url、username、password也必须要和数据库的保持一致哦）

详解StoreConfig类：首先我们定义三个成员变量，然后给每一个成员变量打上一个@value注解，注意@value里面的内容一定是资源文件里面的key值。这里的@ImportResource注解就是指明一个资源文件，在这个资源文件里面获取到对应的数据。那么@Configuration注解是用来干嘛的呢？为什么不用@Component注解呢？其实是这样的，@Component注解用于将所标注的类加载到 Spring 环境中，这时候是需要配置component-scan才能使用的，而@Configuration注解是Spring 3.X后提供的注解，它用于取代XML来配置 Spring。

7. @Bean注解用来标识配置和初始化一个由SpringIOC容器管理的新对象的方法，类似XML中配置文件的<bean/>
 ps:默认的@Bean注解是单例的，那么有什么方式可以指定它的范围呢？所以这里才出现了@Scope注解

8. @Scope注解，在@Scope注解里面value的范围和Bean的作用域是通用的，proxyMode的属性是采用哪一种的单例方式（一种是基于接口的注解，一种是基于类的代理）
### 案例：@Bean和@Scope用法分析：
```java
@Bean
@Scope(value ="session",proxyMode = "scopedProxyMode.TARGET_CLASS")
public UserPreferences userPreferences(){
    return new userPreferences();
}
 
@Bean
public service userService(){
    UserService service =new SimpleUserService();
    service.setUserPreferences(userPreferences);
    return service;
}
```

参考链接：
- [http://www.importnew.com/23592.html](http://www.importnew.com/23592.html "http://www.importnew.com/23592.html")