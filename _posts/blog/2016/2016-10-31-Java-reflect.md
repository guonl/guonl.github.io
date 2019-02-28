---
layout: post
title: Java反射详解
categories: Java
description: Java I/O 总结
keywords: Java,java,反射
---

**AccessibleObject 类是 Field、Method 和 Constructor 对象的基类。它提供了将反射的对象标记为在使用时取消默认 Java 语言访问控制检查的能力。对于公共成员、默认（打包）访问成员、受保护成员和私有成员，在分别使用 Field、Method 或 Constructor 对象来设置或获取字段、调用方法，或者创建和初始化类的新实例的时候，会执行访问检查。**

*在反射对象中设置 accessible 标志允许具有足够特权的复杂应用程序（比如 Java Object Serialization 或其他持久性机制）以某种通常禁止使用的方式来操作对象。*

*setAccessible(boolean flag) 将此对象的 accessible 标志设置为指示的布尔值。*


##### 【案例1】获得其他类中的全部公有构造函数
```java
package com.merryyou.reflect;

/**
 * Created on 2016/10/31 0031.
 *
 * @author zlf
 * @since 1.0
 */
public class Person01 {
    public Person01() {
    }
    private Person01(String name, int age) {
        this.name = name;
        this.age = age;
    }

    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void sayChina(){
        System.out.println("hello China");
    }
    public void sayHello(String name , int age){
        System.out.println(name +"   "+age);
    }
}

```
```java
package com.merryyou.reflect;

import java.lang.reflect.Constructor;

/**
 * Created on 2016/10/31 0031.
 *
 * @author zlf
 * @since 1.0
 */
public class Test05 {
    public static void main(String[] args) {
        Class<?> demo = null;
        try {
            demo = Class.forName("com.merryyou.reflect.Person01");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Constructor<?>[] cons = demo.getConstructors();//返回一个包含某些 Constructor 对象的数组，这些对象反映此 Class 对象所表示的类的所有公共构造方法。
        for(Constructor c: cons){
            c.setAccessible(true);
            System.out.println("构造方法:"+c);
        }
    }
}

```
输出结果：
构造方法:public com.merryyou.reflect.Person01()

##### 【案例1】获得其他类中的全部公有构造函数和参数
```java
package com.merryyou.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * Created on 2016/10/31 0031.
 *
 * @author zlf
 * @since 1.0
 */
public class Test06 {
    public static void main(String[] args) {
        Class<?> demo = null;
        try {
            demo = Class.forName("com.merryyou.reflect.Person01");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Constructor<?>[] cons = demo.getConstructors();//返回一个包含某些 Constructor 对象的数组，这些对象反映此 Class 对象所表示的类的所有公共构造方法。
        for(int i =0;i<cons.length;i++){
            Class types[] = cons[i].getParameterTypes();//按照声明顺序返回一组 Class 对象，这些对象表示此 Constructor 对象所表示构造方法的形参类型。
            System.out.print("构造方法");
            int mo = cons[i].getModifiers();//以整数形式返回此 Constructor 对象所表示构造方法的 Java 语言修饰符。
            System.out.print(Modifier.toString(mo)+" ");
            System.out.print(cons[i].getName());//以字符串形式返回此构造方法的名称。
            System.out.print("(");
            for(int j=0;j<types.length;j++){
                System.out.println(types[j].getName()+"args");//以 String 的形式返回此 Class 对象所表示的实体（类、接口、数组类、基本类型或 void）名称。
                if(j<types.length-1){
                    System.out.print(",");
                }
            }
            System.out.println("){}");
        }
    }
}

```
##### 获得其他类中的全部公有函数
```java
package com.merryyou.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created on 2016/10/31 0031.
 *
 * @author zlf
 * @since 1.0
 */
public class Test07 {
    public static void main(String[] args) {
        Class<?> demo = null;
        try {
            demo = Class.forName("com.merryyou.reflect.Person01");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Method[] methods = demo.getMethods();//返回一个包含某些 Method 对象的数组，
        for(int i=0;i<methods.length;i++){
            Class<?> returnType = methods[i].getReturnType();//返回一个 Class 对象，该对象描述了此 Method 对象所表示的方法的正式返回类型。
            Class<?> para[] = methods[i].getParameterTypes();//按照声明顺序返回 Class 对象的数组，这些对象描述了此 Method 对象所表示的方法的形参类型。
            int temp = methods[i].getModifiers();//以整数形式返回此 Method 对象所表示方法的 Java 语言修饰符。
            System.out.print(Modifier.toString(temp)+" ");
            System.out.print(returnType.getName()+" ");
            System.out.print(methods[i].getName()+" ");
            System.out.print("(");
            for(int j=0;j<para.length;j++){
                System.out.print(para[j].getName()+" "+"args"+j);
                if(j<para.length-1){
                    System.out.print(",");
                }
            }
            Class<?> exec[] = methods[i].getExceptionTypes();// 返回 Class 对象的数组，这些对象描述了声明将此 Method 对象表示的底层方法抛出的异常类型。
            if(exec.length>0){
                System.out.print(") throws ");
                for(int k=0;k<exec.length;k++){
                    System.out.print(exec[k].getName()+" ");
                    if(k<exec.length-1){
                        System.out.print(",");
                    }
                }
            }else{
                System.out.print(")");
            }
            System.out.println();
        }
    }
}

```
##### 获得其他类中的全部属性
```java
package com.merryyou.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created on 2016/10/31 0031.
 *
 * @author zlf
 * @since 1.0
 */
public class Test08 {
    public static void main(String[] args) {
        Class<?> demo = null;
        try {
            demo = Class.forName("com.merryyou.reflect.Person01");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("=====本类属性======");
        Field[] fields = demo.getDeclaredFields();//返回 Field 对象的一个数组，这些对象反映此 Class 对象所表示的类或接口所声明的所有字段。
        for(int i=0;i<fields.length;i++){
            //权限修饰符
            int mo = fields[i].getModifiers();
            String priv = Modifier.toString(mo);
            //属性类型
            Class<?> type = fields[i].getType();
            System.out.println(priv+" "+type.getName()+" "+fields[i].getName());
        }
    }
}

```
##### 调用其它类中的方法
```java
package com.merryyou.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created on 2016/10/31 0031.
 *
 * @author zlf
 * @since 1.0
 */
public class Test09 {
    public static void main(String[] args) {
        Class<?> demo = null;
        try {
            demo = Class.forName("com.merryyou.reflect.Person01");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Method method = demo.getMethod("sayChina");
            method.invoke(demo.newInstance());//对带有指定参数的指定对象调用由此 Method 对象表示的底层方法。
            //调用Person的sayHello方法
            method=demo.getMethod("sayHello", String.class,int.class);//返回一个 Method 对象，它反映此 Class 对象所表示的类或接口的指定公共成员方法。
            method.invoke(demo.newInstance(),"Tom",20);//对带有指定参数的指定对象调用由此 Method 对象表示的底层方法。
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

```
##### JAVA中有三种类加载器
1. **Bootstrap ClassLoader 此加载器采用c++编写，一般开发中很少见。**
2. **Extension ClassLoader 用来进行扩展类的加载，一般对应的是jre\lib\ext目录中的类**
3. **AppClassLoader 加载classpath指定的类，是最常用的加载器。同时也是java中默认的加载器。**

##### 类的生命周期
1. 装载： 类的装载是通过类加载器完成的，加载器将.class文件的二进制文件装入JVM的方法区，并且在堆区创建描述这个类的java.lang.Class对象。用来封装数据。 但是同一个类只会被类装载器装载以前
2. 链接 ：链接就是把二进制数据组装为可以运行的状态。
2.1 校验：校验一般用来确认此二进制文件是否适合当前的JVM（版本）
2.2 准备：准备就是为静态成员分配内存空间，。并设置默认值
2.3 解析：解析指的是转换常量池中的代码作为直接引用的过程，直到所有的符号引用都可以被运行程序使用（建立完整的对应关系）
3. 初始化：为静态成员变量赋值等。


参考链接
- [http://www.cnblogs.com/rollenholt/archive/2011/09/02/2163758.html](http://www.cnblogs.com/rollenholt/archive/2011/09/02/2163758.html)
