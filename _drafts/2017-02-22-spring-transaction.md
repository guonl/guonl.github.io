---
layout: post
title: spring - 事务的问题
categories: Spring
description: spring事务的问题
keywords: spring, 事务
---
后端开发免不了要和事务打交道，比较常用的就是利用 Spring 框架的声明式事务管理，简单的说就是在需要事务管理的类或方法上添加` @Transactional` 注解，然后在配置类上添加 `@EnableTransactionManagement`注解(这里配置采用 JavaConfig 的方式，如果是 XML， 是要在 XML 文件中添加`<tx:annotation-driven/>`)。然后 Spring 框架会利用 AOP 在相关方法调用的前后进行事务管理。

直到。。。。。。。。


```java
public class MerryYouServiceImpl implements MerryYouService {

    public void A (List<MerryYou> merryYouList){
        for(MerryYou merryYou:merryYouList){
            B(merryYou);
        }
    }
    @Transactional("transactionManager")
    public void B(MerryYou merryYou){
        //do something
    }
}
```
大概就是 Service 中有一个方法 A，会内部调用方法 B， 方法 A 没有事务管理，方法 B 采用了声明式事务，通过在方法上声明 `Transactional` 的注解来做事务管理。

but，通过下面的 Junit 测试方法 A 的时候发现方法 B 的事务并没有开启， 而直接调用方法 B 事务是正常开启的。

```java
public class MerryYouServiceTest {

    @Autowired
    private MerryYouService merryYouService;

    // 没有开启事务
    @Test
    public void testA() {
        merryYouService.A();
    }

    // 正常开启事务
    @Test
    public void testB() {
        merryYouService.B();
    }
}
```

原来：Spring 在加载目标 Bean 的时候，会为声明了 `@Transactional` 的 目标 Bean 创造一个代理类，而`目标类本身并不能感知到代理类的存在`。调用通过 Spring 上下文注入的 Bean 的方法， 并不是直接调用目标类的方法。
![不是](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/2017-02-22_Transactional01.png "不是")
而是先调用代理类的方法，再调用目标类的。
![是](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/2017-02-22_Transactional03.png "是")
对于加了`@Transactional`注解的方法来说，在调用代理类的方法时，会先通过拦截器`TransactionInterceptor`开启事务，然后在调用目标类的方法，最后在调用结束后，`TransactionInterceptor` 会提交或回滚事务，大致流程如下图。
![transaction manager](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/2017-02-22_Transactional02.png "transaction manager")
而对于第一段的代码，我在方法 A 中调用方法 B，实际上是通过“this”的引用，也就是直接调用了目标类的方法，而非通过 Spring 上下文获得的代理类，所以。。。事务是不会开启滴。

解决办法也蛮简单，通过实现ApplicationContextAware接口获得 Spring 的上下文，然后获得目标类的代理类，通过代理类的对象，调用方法 B，即可。
```java
public class MerryYouServiceImpl implements MerryYouService,ApplicationContextAware {


    @Setter
    private ApplicationContext applicationContext;

    public void A (List<MerryYou> merryYouList){
        MerryYouService service = applicationContext.getBean(MerryYouService.class);
        for(MerryYou merryYou:merryYouList){
            service.B(merryYou);
        }
    }
    @Transactional("transactionManager")
    public void B(MerryYou merryYou){
        //do something
    }
}
```

也可以从context中获取。

![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/2017-02-22.gif)

参考链接：
- [http://yemengying.com/2016/11/14/something-about-spring-transaction/](http://yemengying.com/2016/11/14/something-about-spring-transaction/)