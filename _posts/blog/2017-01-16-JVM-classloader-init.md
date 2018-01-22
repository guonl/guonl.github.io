---
layout: post
title: JVM 的类初始化机制
categories: JVM
description: JVM 的类初始化机制
keywords: JVM,jvm,java虚拟机
---
## 前言
当你在 Java 程序中**new**对象时，有没有考虑过 JVM 是如何把静态的字节码（byte code）转化为运行时对象的呢，这个问题看似简单，但清楚的同学相信也不会太多，这篇文章首先介绍 JVM 类初始化的机制，然后给出几个易出错的实例来分析，帮助大家更好理解这个知识点。

JVM 将字节码转化为运行时对象分为三个阶段，分别是：*loading 、Linking、initialization。*
![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/2017-01-16_01.png)

### Loading
Loading 过程主要工作是由ClassLoader完成。该过程具体包括三件事：
1. 根据类的全名，生成一份二进制字节码来表示该类
1. 将二进制的字节码解析成方法区对应的数据结构
1. 最后生成一 Class 对象的实例来表示该类

![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/2017-01-16_02.png)

JVM 中除了最顶层的Boostrap ClassLoader是用 C/C++ 实现外，其余类加载器均由 Java 实现，我们可以用getClassLoader方法来获取当前类的类加载器：
```java
public class ClassLoaderDemo {
    public static void main(String[] args) {
        System.out.println(ClassLoaderDemo.class.getClassLoader());
    }
}

# sun.misc.Launcher$AppClassLoader@30a4effe
# AppClassLoader 也就是上图中的 System Class Loader
```
此外，我们在启动java传入-verbose:class来查看加载的类有那些。
```java
java -verbose:class ClassLoaderDemo

[Opened /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/jre/lib/rt.jar]
[Loaded java.lang.Object from /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/jre/lib/rt.jar]
[Loaded java.io.Serializable from /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/jre/lib/rt.jar]
[Loaded java.lang.Comparable from /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/jre/lib/rt.jar]
[Loaded java.lang.CharSequence from /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/jre/lib/rt.jar]

....
....

[Loaded java.security.BasicPermissionCollection from /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/jre/lib/rt.jar]
[Loaded ClassLoaderDemo from file:/Users/liujiacai/codes/IdeaProjects/mysql-test/target/classes/]
[Loaded sun.launcher.LauncherHelper$FXHelper from /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/jre/lib/rt.jar]
[Loaded java.lang.Class$MethodArray from /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/jre/lib/rt.jar]
[Loaded java.lang.Void from /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/jre/lib/rt.jar]
sun.misc.Launcher$AppClassLoader@2a139a55
[Loaded java.lang.Shutdown from /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/jre/lib/rt.jar]
[Loaded java.lang.Shutdown$Lock from /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/jre/lib/rt.jar]
```
### Linking
#### Verification
Verification 主要是保证类符合 Java 语法规范，确保不会影响 JVM 的运行。包括但不限于以下事项：

- bytecode 的完整性（integrity）
- 检查final类没有被继承，final方法没有被覆盖
- 确保没有不兼容的方法签名

#### Preparation

在一个类已经被load并且通过verification后，就进入到preparation阶段。在这个阶段，JVM 会为类的成员变量分配内存空间并且赋予默认初始值，需要注意的是这个阶段不会执行任何代码，而只是根据变量类型决定初始值。如果不进行默认初始化，分配的空间的值是随机的，有点类型c语言中的野指针问题。

```java
Type    Initial Value
int    0
long    0L
short    (short) 0
char    '\u0000'
byte    (byte) 0
boolean    false
reference    null
float    0.0f
double    0.0d
```

在这个阶段，JVM 也可能会为有助于提高程序性能的数据结构分配内存，常见的一个称为method table的数据结构，它包含了指向所有类方法（也包括也从父类继承的方法）的指针，这样再调用父类方法时就不用再去搜索了。

#### Resolution
Resolution 阶段主要工作是确认类、接口、属性和方法在类run-time constant pool的位置，并且把这些符号引用（symbolic references）替换为直接引用（direct references）。

> locating classes, interfaces, fields, and methods referenced symbolically from a type's constant pool, and replacing those symbolic references with direct references.

这个过程不是必须的，也可以发生在第一次使用某个符号引用时。

![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/2017-01-16_03.png)

### Initialization

经过了上面的load、link后，第一次 主动调用某类的最后一步是Initialization，这个过程会去按照代码书写顺序进行初始化，这个阶段会去真正执行代码，注意包括：代码块（static与static）、构造函数、变量显式赋值。如果一个类有父类，会先去执行父类的initialization阶段，然后在执行自己的。

上面这段话有两个关键词：第一次与主动调用。第一次是说只在第一次时才会有初始化过程，以后就不需要了，可以理解为每个类有且仅有一次初始化的机会。那么什么是主动调用呢？
JVM 规定了以下六种情况为主动调用，其余的皆为被动调用：

1. 一个类的实例被创建（new操作、反射、cloning，反序列化）
1. 调用类的static方法
1. 使用或对类/接口的static属性进行赋值时（这不包括final的与在编译期确定的常量表达式）
1. 当调用 API 中的某些反射方法时
1. 子类被初始化
1. 被设定为 JVM 启动时的启动类（具有main方法的类）

本文后面会给出一个示例用于说明主动调用的被动调用区别。

在这个阶段，执行代码的顺序遵循以下两个原则：

1. 有static先初始化static，然后是非static的
1. 显式初始化，构造块初始化，最后调用构造函数进行初始化

### 示例
#### 属性在不同时期的赋值
```java
class Singleton {

    private static Singleton mInstance = new Singleton();// 位置1
    public static int counter1;
    public static int counter2 = 0;

//    private static Singleton mInstance = new Singleton();// 位置2

    private Singleton() {
        counter1++;
        counter2++;
    }

    public static Singleton getInstantce() {
        return mInstance;
    }
}

public class InitDemo {

    public static void main(String[] args) {

        Singleton singleton = Singleton.getInstantce();
        System.out.println("counter1: " + singleton.counter1);
        System.out.println("counter2: " + singleton.counter2);
    }
}
```
当mInstance在位置1时，打印出
```java
counter1: 1
counter2: 0
```
当mInstance在位置2时，打印出
```java
counter1: 1
counter2: 1
```
Singleton中的三个属性在Preparation阶段会根据类型赋予默认值，在Initialization阶段会根据显示赋值的表达式再次进行赋值（按顺序自上而下执行）。根据这两点，就不难理解上面的结果了。

#### 主动调用 vs. 被动调用

```java
class NewParent {

    static int hoursOfSleep = (int) (Math.random() * 3.0);

    static {
        System.out.println("NewParent was initialized.");
    }
}

class NewbornBaby extends NewParent {

    static int hoursOfCrying = 6 + (int) (Math.random() * 2.0);

    static {
        System.out.println("NewbornBaby was initialized.");
    }
}

public class ActiveUsageDemo {

    // Invoking main() is an active use of ActiveUsageDemo
    public static void main(String[] args) {

        // Using hoursOfSleep is an active use of NewParent,
        // but a passive use of NewbornBaby
        System.out.println(NewbornBaby.hoursOfSleep);
    }

    static {
        System.out.println("ActiveUsageDemo was initialized.");
    }
}
```
上面的程序最终输出：
```java
ActiveUsageDemo was initialized.
NewParent was initialized.
1
```
之所以没有输出NewbornBaby was initialized.是因为没有主动去调用NewbornBaby，如果把打印的内容改为NewbornBaby.hoursOfCrying 那么这时就是主动调用NewbornBaby了，相应的语句也会打印出来。
#### 首次主动调用才会初始化
```java
public class Alibaba {

    public static int k = 0;
    public static Alibaba t1 = new Alibaba("t1");
    public static Alibaba t2 = new Alibaba("t2");
    public static int i = print("i");
    public static int n = 99;
    private int a = 0;
    public int j = print("j");
    {
        print("构造块");
    }
    static {
        print("静态块");
    }

    public Alibaba(String str) {
        System.out.println((++k) + ":" + str + "   i=" + i + "    n=" + n);
        ++i;
        ++n;
    }

    public static int print(String str) {
        System.out.println((++k) + ":" + str + "   i=" + i + "    n=" + n);
        ++n;
        return ++i;
    }

    public static void main(String args[]) {
        Alibaba t = new Alibaba("init");
    }
}
```
上面这个例子是阿里巴巴在14年的校招附加题，我当时看到这个题，就觉得与阿里无缘了。囧
```java
1:j   i=0    n=0
2:构造块   i=1    n=1
3:t1   i=2    n=2
4:j   i=3    n=3
5:构造块   i=4    n=4
6:t2   i=5    n=5
7:i   i=6    n=6
8:静态块   i=7    n=99
9:j   i=8    n=100
10:构造块   i=9    n=101
11:init   i=10    n=102
```
上面是程序的输出结果，下面我来一行行分析之。

1. 由于Alibaba是 JVM 的启动类，属于主动调用，所以会依此进行 loading、linking、initialization 三个过程。
1. 经过 loading与 linking 阶段后，所有的属性都有了默认值，然后进入最后的 initialization 阶段。
1. 在 initialization 阶段，先对 static 属性赋值，然后在非 static 的。k 第一个显式赋值为 0 。
1. 接下来是t1属性，由于这时Alibaba这个类已经处于 initialization 阶段，static 变量无需再次初始化了，所以忽略 static 属性的赋值，只对非 static 的属性进行赋值，所有有了开始的：
```java
1:j   i=0    n=0
 2:构造块   i=1    n=1
 3:t1   i=2    n=2
```
1. 接着对t2进行赋值，过程与t1相同
```java
 4:j   i=3    n=3
 5:构造块   i=4    n=4
 6:t2   i=5    n=5
```
1. 之后到了 static 的 i 与 n：
```java
 7:i   i=6    n=6
```
1. 到现在为止，所有的static的成员变量已经赋值完成，接下来就到了 static 代码块
```java
8:静态块   i=7    n=99
```
1. 至此，所有的 static 部分赋值完毕，接下来是非 static 的 j
```java
 9:j   i=8    n=100
```
1. 所有属性都赋值完毕，最后是构造块与构造函数
```java
10:构造块   i=9    n=101
 11:init   i=10    n=102
```

经过上面这9步，Alibaba这个类的初始化过程就算完成了。这里面比较容易出错的是第3步，认为会再次初始化 static 变量或代码块。而实际上是没必要，否则会出现多次初始化的情况。

参考链接：
- [https://gold.xitu.io/post/587b6ada8d6d810058c9221f?utm_source=gold_browser_extension](https://gold.xitu.io/post/587b6ada8d6d810058c9221f?utm_source=gold_browser_extension)
