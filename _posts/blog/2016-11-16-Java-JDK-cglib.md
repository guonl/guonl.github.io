---
layout: post
title: Java动态代理（JDK和cglib）
categories: Java
description: Java动态代理（JDK和cglib）
keywords: Java,java,JDK,代理,cglib
---
JAVA的动态代理 
代理模式 
代理模式是常用的java设计模式，他的特征是代理类与委托类有同样的接口，代理类主要负责为委托类预处理消息、过滤消息、把消息转发给委托类，以及事后处理消息等。代理类与委托类之间通常会存在关联关系，一个代理类的对象与一个委托类的对象关联，代理类的对象本身并不真正实现服务，而是通过调用委托类的对象的相关方法，来提供特定的服务。


**按照代理的创建时期，代理类可以分为两种**。
 
1. 静态代理：由程序员创建或特定工具自动生成源代码，再对其编译。在程序运行前，代理类的.class文件就已经存在了。 
2. 动态代理：在程序运行时，运用反射机制动态创建而成。 
<!--more-->

#### 静态代理
Sourceable.java
```java
package com.merryyou.designpatterns.proxy;

/**
 * Created on 2016/5/31 0031.
 *
 * @author zlf
 * @since 1.0
 */
public interface Sourceable {
    public void method();
}

```
SourceableImpl.java
```java
package com.merryyou.designpatterns.proxy;

/**
 * Created on 2016/5/31 0031.
 *
 * @author zlf
 * @since 1.0
 */
public class SourceableImpl implements Sourceable {
    @Override
    public void method() {
        System.out.println("this original method");
    }
}

```
Porxy.java
```java
package com.merryyou.designpatterns.proxy;

/**
 * Created on 2016/5/31 0031.
 *
 * @author zlf
 * @since 1.0
 */
public class Porxy implements Sourceable {

    private SourceableImpl source;

    public Porxy() {
        super();
        this.source = new SourceableImpl();
    }

    @Override
    public void method() {
        before();
        source.method();
        after();
    }

    private void after() {
        System.out.println("after proxy!");
    }

    private void before() {
        System.out.println("before proxy!");
    }
}

```
ProxyTest.java
```java
package com.merryyou.designpatterns.proxy;

/**
 * Created on 2016/5/31 0031.
 *
 * @author zlf
 * @since 1.0
 */
public class ProxyTest {
    public static void main(String[] args) {
        Sourceable source = new Porxy();
        source.method();

    }
}

```
每一个代理类只能为一个接口服务，这样一来程序开发中必然会产生过多的代理，而且，所有的代理操作除了调用的方法不一样之外，其他的操作都一样，则此时肯定是重复代码。解决这一问题最好的做法是可以通过一个代理类完成全部的代理功能，那么此时就必须使用动态代理完成。
#### JDK动态代理
InvocationHandler接口： 
方法摘要：
```java
Object invoke(Object proxy,
              Method method,
              Object[] args)
              throws Throwable
```

- proxy - 在其上调用方法的代理实例
- method - 对应于在代理实例上调用的接口方法的 Method 实例。Method 对象的声明类将是在其中声明方法的接口，该接口可以是代理类赖以继承方法的代理接口的超接口。
- args - 包含传入代理实例上方法调用的参数值的对象数组，如果接口方法不使用参数，则为 null。基本类型的参数被包装在适当基本包装器类（如 java.lang.Integer 或 java.lang.Boolean）的实例中。 

Proxy类： 
Proxy类是专门完成代理的操作类，可以通过此类为一个或多个接口动态地生成实现类，此类提供了如下的操作方法： 
```java
public static Object newProxyInstance(ClassLoader loader,
                                      Class<?>[] interfaces,
                                      InvocationHandler h)
                               throws IllegalArgumentException
```

- loader - 定义代理类的类加载器
- interfaces - 代理类要实现的接口列表
- h - 指派方法调用的调用处理程序 

**Ps:类加载器 **
在Proxy类中的newProxyInstance（）方法中需要一个ClassLoader类的实例，ClassLoader实际上对应的是类加载器，在Java中主要有一下三种类加载器; 

1. Booststrap ClassLoader：此加载器采用C++编写，一般开发中是看不到的； 
1. Extendsion ClassLoader：用来进行扩展类的加载，一般对应的是jre\lib\ext目录中的类; 
1. AppClassLoader：(默认)加载classpath指定的类，是最常使用的是一种加载器。

DynamicProxy.java
```java
package com.merryyou.designpatterns.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created on 2016/11/16 0016.
 *
 * @author zlf
 * @since 1.0
 */
public class DynamicProxy implements InvocationHandler {

    private Object target;
    /**
     * 绑定委托对象并返回一个代理类
     * @param target
     * @return
     */
    public Object bind(Object target) {
        this.target = target;
        //取得代理对象
        return Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(), this);   //要绑定接口(这是一个缺陷，cglib弥补了这一缺陷)
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result=null;
        System.out.println("事物开始");
        //执行方法
        result=method.invoke(target, args);
        System.out.println("事物结束");
        return result;
    }
}

```
DynamicProxyTest.java
```java
package com.merryyou.designpatterns.proxy;

/**
 * Created on 2016/11/16 0016.
 *
 * @author zlf
 * @since 1.0
 */
public class DynamicProxyTest {
    public static void main(String[] args) {
        DynamicProxy proxy = new DynamicProxy();
        Sourceable sourceable = (Sourceable) proxy.bind(new SourceableImpl());
        sourceable.method();
    }
}

```
但是，JDK的动态代理依靠接口实现，如果有些类并没有实现接口，则不能使用JDK代理，这就要使用cglib动态代理了。 
#### Cglib动态代理 
JDK的动态代理机制只能代理实现了接口的类，而不能实现接口的类就不能实现JDK的动态代理，cglib是针对类来实现代理的，他的原理是对指定的目标类生成一个子类，并覆盖其中方法实现增强，但因为采用的是继承，所以不能对final修饰的类进行代理。 

DynamicCglibProxy.java
```java
package com.merryyou.designpatterns.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * Created on 2016/11/16 0016.
 *
 * @author zlf
 * @since 1.0
 */
public class DynamicCglibProxy implements MethodInterceptor {
    private Object target;

    /**
     * 创建代理对象
     *
     * @param target
     * @return
     */
    public Object getInstance(Object target) {
        this.target = target;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(this.target.getClass());
        // 回调方法
        enhancer.setCallback(this);
        // 创建代理对象
        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("事物开始");
        methodProxy.invokeSuper(o, objects);
        System.out.println("事物结束");
        return null;
    }
}

```
DynamicCglibProxyTest.java
```java
package com.merryyou.designpatterns.proxy;

/**
 * Created on 2016/11/16 0016.
 *
 * @author zlf
 * @since 1.0
 */
public class DynamicCglibProxyTest {
    public static void main(String[] args) {
        DynamicCglibProxy cglib=new DynamicCglibProxy();
        SourceableImpl cglibInstance=(SourceableImpl)cglib.getInstance(new SourceableImpl());
        cglibInstance.method();
    }
}

```

参考链接：

- [http://www.cnblogs.com/jqyp/archive/2010/08/20/1805041.html](http://www.cnblogs.com/jqyp/archive/2010/08/20/1805041.html)