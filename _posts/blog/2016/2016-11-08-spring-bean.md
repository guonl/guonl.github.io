---
layout: post
title: Bean的生命周期
categories: Spring
description: Spring Bean的生命周期
keywords: Bean
---
### 生命周期图解
下图描述了BeanFactory中Bean生命周期的完整过程：
![bean生命周期](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/spring_beanLife.jpg "bean生命周期")

Bean 的生命周期从Spring容器着手实例化Bean开始，直到最终销毁Bean,这当中经过了许多关键点，每个关键点都涉及特定方法的调用，可以将这些方法大致划分为3类：
1. Bean自身的方法:如调用Bean构造函数，实例化Bean,，调用Setter设置Bean的属性值以及通过<bean>的init-method和destroy-method所指定的方法；
2. Bean级生命周期接口方法：如BeanNameAware、BeanFactoryAware、InitializationBean和DisposableBean,这些接口方法由Bean类直接实现；
3. 容器级生命周期接口方法：如上图中的红色部分所示，由InstantiationAwareBeanPostProcessor和BeanPostProcessor这连个接口实现，一般称他们的实现类为”后处理器“。后处理器接口，一般不由Bean本身实现，他们独立于Bean,实现类以容器附加装置的形式注册到Spring容器中，并通过接口反射为Spring容器预先识别。当Spring容器创建任何Bean的时候，这些后处理器都会发生作用，所以这些后处理器的影响是全局性的。当然，用户可以通过合理的编写后处理器，让其仅对感兴趣的Bean进行加工处理；

InstantiationAwareBeanPostProcessor其实是BeanPostProcessor接口的子接口，在Spring 1.2中定义，在Spring2.0中为其提供了一个适配器类InstantiationAwareBeanPostProcessorAdapter，一般情况下，可以方便的扩展改适配器覆盖感兴趣的方法以定义实现类。下面我们通过一个具体的实例以更好的理解Bean生命周期的各个步骤.

### 窥探Bean生命周期的实例
实现各种生命周期控制访问的Car
```java
package com.merryyou.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;

/**
 * Created on 2016/11/8 0008.
 *
 * @author zlf
 * @since 1.0
 */
public class Car implements BeanFactoryAware ,BeanNameAware,InitializingBean,DisposableBean {

    private String brand;
    private String color;
    private int maxSpeed;
    private BeanFactory beanFactory;
    private String beanName;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        System.out.println("调用setBrand()设置属性");
        this.brand = brand;
    }

    public void introduce(){
        System.out.println("bradn:"+brand+";color"+color+"; maxSpeed:"+maxSpeed);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public String getBeanName() {
        return beanName;
    }

    //1、管理Bean生命周期的接口
    public Car(){
        System.out.println("调用Car构造函数");
    }

    //2、BeanFactoryAware接口方法
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("调用BeanFactoryAware.setBeanFactory()");
        this.beanFactory=beanFactory;
    }

    //3、BeanNameAware接口方法
    @Override
    public void setBeanName(String name) {
        System.out.println("调用BeanNameAware.setBeanName()");
        this.beanName=beanName;
    }

    //4 InitializingBean接口方法
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("调用InitializingBean.afterPropertiesSet()");
    }

    // 5  DisposableBean接口方法
    @Override
    public void destroy() throws Exception {
        System.out.println("调用DisposableBean.destroy()");
    }

    // 6  通过<bean>的init-method属性指定的初始化方法
    public void myInit(){
        System.out.println("调用inti-method所指定的myInit(),将maxSpeed设置为240.");
        this.maxSpeed=240;
    }

    //7  通过<bean>的destory-method属性指定的销毁方法
    public void myDestory(){
        System.out.println("调用destory-method所指定的myDestory()方法。");
    }
}

```

InstantiationAwareBeanPostProcessor实现类
```java
package com.merryyou.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;

import java.beans.PropertyDescriptor;

/**
 * Created on 2016/11/8 0008.
 *
 * @author zlf
 * @since 1.0
 */
public class MyInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {
    //1 接口方法：实例化bean前进行调用
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        //1-1 仅对容器中的car-bean 进行处理
        if("car".equals(beanName)){
            System.out.println("InstantiationAwareBeanPostProcessor.postProcessBeforeInstantiation()");
        }
        return null;
    }
    //2 接口方法：在实例化bean后进行调用
    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        if("car".equals(beanName)){
            System.out.println("InstantiationAwareBeanPostProcessor.postProcessAfterInstantiation()");
        }
        return true;
    }

    // 3 接口方法：在设置某个属性时调用
    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
        //3-1 仅对容器中car-bean进行处理，还可以通过post入参进行过滤，
        //进队car的某个特性进行处理
        if("car".equals(beanName)){
            System.out.println("InstantiationAwareBeanPostProcessor.postProcessPropertyValues");
        }
        return pvs;
    }
}

```

BeanPostProcessor实现类

```java
package com.merryyou.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Created on 2016/11/8 0008.
 *
 * @author zlf
 * @since 1.0
 */
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.equals("car")) {
            Car car = (Car) bean;
            if (car.getColor() == null) {
                System.out.println("调用BeanPostProcessor.postProcessBeforeInitialization(),color为空，设置为默认黑色.");
                car.setColor("黑色");
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.equals("car")) {
            Car car = (Car) bean;
            if (car.getMaxSpeed() >= 200) {
                System.out.println("调用BeanPostProcessor.postProcessAfterInitialization(),将maxSpedd调整为200.");
                car.setMaxSpeed(200);
            }
        }
        return bean;
    }
}

```
在Spring配置文件中定义Car的配置信息

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="car" class="com.merryyou.bean.Car" init-method="myInit" destroy-method="myDestory"
          p:brand="test"
          p:maxSpeed="300"
          scope="singleton"
    />
</beans>
```
下面我们让容器装载配置文件，然后再分别注册上面所提供的两个后处理器：
```java
package com.merryyou.bean;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Created on 2016/11/8 0008.
 *
 * @author zlf
 * @since 1.0
 */
public class BeanLifeCycle {
    public static void lifiCycleInBeanFactory() {
        //1 下面两句装载配置文件，并启动容器
        Resource resource = new ClassPathResource("beans.xml");
        BeanFactory bf = new XmlBeanFactory(resource);

        //2 向容器中注册MyBeanPostProcesser处理器
        ((ConfigurableBeanFactory)bf).addBeanPostProcessor(new MyBeanPostProcessor());
        //3 想容器中注册MyInstantiationAwareBeanPostProcessor后处理器
        ((ConfigurableBeanFactory)bf).addBeanPostProcessor(new MyInstantiationAwareBeanPostProcessor());

        //4 第一次从容器中获取car ,将触发容器实例化该Bean,这将引发Bean生命周期方法的调用
        Car car = (Car) bf.getBean("car");
        car.introduce();
        car.setColor("红色");

        //5 第二次从容器中获取car，直接从缓存池中取（因为 scope="singleton"）
        Car car1 = (Car) bf.getBean("car");

        //6 查看car 和 car1 是否指向同一个引用
        System.out.println("car == car1 " +(car == car1));

        //7 关闭容器
        ((ConfigurableBeanFactory) bf).destroySingletons();

    }

    public static void main(String[] args) {
        lifiCycleInBeanFactory();
    }
}

```

运行后，结果如下:
```java
[14:22:12][INFO][XmlBeanDefinitionReader][317][] - Loading XML bean definitions from class path resource [beans.xml]
InstantiationAwareBeanPostProcessor.postProcessBeforeInstantiation()
调用Car构造函数
InstantiationAwareBeanPostProcessor.postProcessAfterInstantiation()
InstantiationAwareBeanPostProcessor.postProcessPropertyValues
调用setBrand()设置属性
调用BeanNameAware.setBeanName()
调用BeanFactoryAware.setBeanFactory()
调用BeanPostProcessor.postProcessBeforeInitialization(),color为空，设置为默认黑色.
调用InitializingBean.afterPropertiesSet()
调用inti-method所指定的myInit(),将maxSpeed设置为240.
调用BeanPostProcessor.postProcessAfterInitialization(),将maxSpedd调整为200.
bradn:test;color黑色; maxSpeed:200
car == car1 true
调用DisposableBean.destroy()
调用destory-method所指定的myDestory()方法。
Disconnected from the target VM, address: '127.0.0.1:57981', transport: 'socket'
```

仔细观察输出的信息，将发现它验证了我们前面所介绍的生命周期的过程。在7处，我们通过destroySingletons()方法关闭容器，由于Car实现了销毁接口并指定了销毁方法，所以容器将触发调用这两个方法.

### ApplicationContext中Bean的生命周期
Bean在应用上下文中的生命周期和在BeanFactory中生命周期类似，不同是，如果Bean实现了org.springframework.context.ApplicationContext接口，会增加一个调用该接口的方法setApplicationContext()步骤，该方法紧接着BeanFactoryAware之后，此外，如果配置文件中声明了工厂后处理器接口BeanFactoryPostProcessor的实现类，则应用上下文在装载配置文件之后，初始化Bean实例之前将调用这些BeanFactoryPostProcessor对配置信息进行加工处理。

ApplicationContext和BeanFactory另一个最大的不同之处在于：前者会利用JAVA机制自动识别出配置文件中定义的BeanPostProcessor，InstantiationAwareBeanPostProcessor和BeanFactoryPostProcessor，并自动将他们注册到应用上下文中；而后者需要在代码中通过手工调用addBeanPostProcessor()方法进行注册。这也是为什么在应用开发时，我们普遍使用ApplicationContext而很少使用BeanFactory的原因之一。

在ApplicationContext中，我们只需要在配置文件中通过<bean>定义工厂后处理器和Bean后处理器，他们就会按预期的方式执行。
 来看一个使用工厂后处理器的实例，假设我们希望对配置文件中car的brand配置属性进行调整，则可以编写一个如下的工厂后处理器

MyBeanFactoyPostProcessor.java
```java
package com.merryyou.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Created on 2016/11/8 0008.
 *
 * @author zlf
 * @since 1.0
 */
public class MyBeanFactoyPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinition bd=beanFactory.getBeanDefinition("car");
        bd.getPropertyValues().addPropertyValue("brand", "奇瑞QQ");
        System.out.println("调用BeanFactoryPostProcessor.postProcessBeanFactory()!");
    }
}

```
ApplicationContext在启动时，将首先为配置文件中的每个<bean>生成一个BeanDefinition对象，BeanDefinition是<bean>在Spring容器中的内部表示。当配置文件中所有的<bean>都被解析成Definition时，ApplicationContext将调用工厂后处理器的方法，因此我们有机会通过程序的方式调整bean的配置信息。在这里，我们将car的BeanDefinition进行调整，将brand属性设置为"奇瑞QQ"，下面是具体的配置:
beans.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <!-- 1 这个brand属性的值将被工厂后处理器更改掉-->
    <bean id="car" class="com.merryyou.bean.Car" init-method="myInit" destroy-method="myDestory"
          p:brand="test"
          p:maxSpeed="300"
          scope="singleton"
    />
    <!-- 2 Bean后处理器-->
    <bean id="myBeanPostprocessor" class="com.merryyou.bean.MyBeanPostProcessor"/>

    <!-- 3 Bean工厂后处理器 -->
    <bean id="myBeanFactory" class="com.merryyou.bean.MyBeanFactoyPostProcessor"/>
</beans>
```
2和3处定义的BeanPostProcessor和BeanFactoryPostProcessor会自动被ApplicationContext识别并注册到容器中。3处注册的工厂后处理器将会对1处配置的属性值进行调整。在2处，我们还定义了一个Bean后处理器，它也可以对1处配置的属性进行调整。启动容器并查看car Bean的信息，我们将发现car Bean的brand属性成功被工厂后处理器修改了.
```java
package com.merryyou.bean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created on 2016/11/8 0008.
 *
 * @author zlf
 * @since 1.0
 */
public class Test {
    public static void main(String[] args) {
        ApplicationContext ctx=new ClassPathXmlApplicationContext("beans.xml");
        Car car =(Car) ctx.getBean("car");
        System.out.println(car.getBrand());
    }
}

```
输出结果
```java
[14:55:15][INFO][ClassPathXmlApplicationContext][573][] - Refreshing org.springframework.context.support.ClassPathXmlApplicationContext@47987356: startup date [Tue Nov 08 14:55:15 CST 2016]; root of context hierarchy
[14:55:16][INFO][XmlBeanDefinitionReader][317][] - Loading XML bean definitions from class path resource [beans.xml]
调用BeanFactoryPostProcessor.postProcessBeanFactory()!
调用Car构造函数
调用setBrand()设置属性
调用BeanNameAware.setBeanName()
调用BeanFactoryAware.setBeanFactory()
调用BeanPostProcessor.postProcessBeforeInitialization(),color为空，设置为默认黑色.
调用InitializingBean.afterPropertiesSet()
调用inti-method所指定的myInit(),将maxSpeed设置为240.
调用BeanPostProcessor.postProcessAfterInitialization(),将maxSpedd调整为200.
奇瑞QQ
```
参考链接：

- [http://blog.csdn.net/yulei_qq/article/details/22274361#java](http://blog.csdn.net/yulei_qq/article/details/22274361#java)

