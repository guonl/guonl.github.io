---
layout: post
title: Java8中的Lambda表达式
categories: Java
description: Java8中的Lambda表达式
keywords: Java,Lambda,Java8
---
## 前言
Lambda 表达式是 Java 8 最受欢迎的功能。人们将函数式编程的概念引入了 Java 这门完全面向对象的命令式编程语言。关于函数式编程是如何运作的，这个话题超出了本文的范围，不过我们会提炼出它一个明显有别于我们所经常使用的 OOP （面向对象编程）的功能来加以讨论。

在本文中, 我们将了解到 lambda 表达式具体是什么东西，还有就是它们是如何将自己融入整个 Java 生态系统的。我们也会对没有使用 lambda 表达式的代码以及后面使用 lambda 进行重构的示例代码进行一下观察和比较。

### 了解 Lambda 表达式
Lambda 表达式是一块我们可以将其传入并执行的代码。对于作为 Java 程序员的我们而言，并不会怎么习惯将一块代码传入一个函数这样的方式。我们的习惯是将定义的代码封装到方法体里面，然后通过对象引用来加以执行，如下所示：
```java
public class LambdaDemo {
    public void printSomething(String something) {
        System.out.println(something);
    }

    public static void main(String[] args) {
        LambdaDemo demo = new LambdaDemo();
        String something = "I am learning Lambda";
        demo.printSomething(something);
    }
}
```
这是经典 OOP 开发范式的风格，将方法实现对调用者隐藏。调用者只是简单地向方法传入一个变量，然后方法拿这个变量会执行一些操作，并返回另外一个变量值，或者如我们的示例所示，会产生一些副作用效果。


------------

现在我们要来看看一种使用了行为传递方式，而不是变量传递的等效实现。为此，我们要创建一个函数式的接口，里面定义的是对行为，而不是对方法的抽象。一个函数式接口是一种只有一个方法的接口：
```java
public class LambdaDemo {
    interface Printer {
        void print(String val);
    }

    public void printSomething(String something, Printer printer) {
        printer.print(something);
    }
}
```
在上面的代码实现中， Printer 接口负责所有的打印操作。printSomething 方法不再对行为进行定义，而是执行由 Printer 定义的行为:
```java
public static void main(String[] args) {
    LambdaDemo demo = new LambdaDemo();
    String something = "I am using a Functional interface";
    Printer printer = new Printer() {
        @Override
        public void print(String val) {
            System.out.println(val);
        }
    };
    demo.printSomething(something, printer);
}
```
读者中比较有观察能力的可能已经注意到，我并没有在这里做什么新的事情。的确是这样的，因为我还没有应用到 lambda 表达式。我们只是简单地创建了一个 Printer 接口的具体实现，并将它传入了 printSomething 方法。

上面的示例旨在给我们带来一个将 Lambda 表达式引入到 Java 中的关键目标:

> Lambda 表达式原被用于定义一个函数式接口的内联实现。

在我们使用 lambda 表达式对上面的示例进行重构之前，先来学习一下必要的语法知识:

```java
(param1,param2,param3...,paramN) - > {//代码块;}
```
一个 lambda 表达式的组成，是一个我们通常会定义在方法声明中的，以括弧封闭起来并以逗号分隔的参数列表，后面跟上一个箭头标记指向要执行的代码。现在，让我们来使用 lambda 对上面的代码进行重构:
```java
public static void main(String[] args) {
    LambdaDemo demo = new LambdaDemo();
    String something = "I am learning Lambda";
    /**/
    Printer printer = (String toPrint)->{System.out.println(toPrint);};
    /**/
    demo.printSomething(something, printer);
}
```
看上去非常紧凑且美观。因为函数式接口只声明了一个方法，所以在 lambda 的第一部分中传入的参数被自动地映射到了方法的参数列表上，而箭头右边的代码则被当做是方法的具体实现了。

------------

### 为什么要使用 Lambda 表达式
如同前面的示例， lambda 表达式能让我们拥有更加紧凑的代码，更加易于阅读和跟踪。这个在性能和多核处理方法还有其它的一些好处，不过它们得在你了解了 Streams API 以后才有用，而这个超出了本文的范围。

通过比较使用和没使用 lambda 的 main 方式实现，当它一下子把代码变得简短的时候，我们切实地看到了 lambda 表达式的能力：
```java
public static void main(String[] args) {
    LambdaDemo demo = new LambdaDemo();
    String something = "I am learning Lambda";
    /**/
    Printer printer = (String toPrint)->{System.out.println(toPrint);};
    /**/
    demo.printSomething(something, printer);
}
```
我们还可以让代码比这里所展示的更简洁。这样的事情发生时，你甚至无需指定箭头左边参数的类型，而其类型会由编译器根据接口方法的形参推断出来。
```java
Printer printer = (toPrint)->{System.out.println(toPrint);};
```
我们还可以做得更好。lambda 的另外一个特性就是: 如果只有一个参数, 就可以将括弧完全消除掉。同样的，如果在箭头右边只有一条语句，也可以将大括号去掉:
```java
Printer printer = toPrint -> System.out.println(toPrint);
```
现在的代码看起来真正变得可爱起来，不过我们才刚刚开始而已。如果我们的接口方法并不要任何参数，那就可以将生命用一对空的括弧替换掉：
```java
() -> System.out.println("anything");
```
如果我们只是内联一个 lambda 进去，而不去首先创建一个对象然后将其传入到 saySomething 方法，会如何呢：
```java
public static void main(String[] args) {
    LambdaDemo demo = new LambdaDemo();
    String something="I am Lambda";
    /**/
    demo.printSomething(something, toPrint -> System.out.println(toPrint));
}
```
现在我们才是真的在谈论函数式编程了。我们的 main 函数体从一开始的 9 行代码减少到了 3 行。这样紧凑的代码使得 lambda 表达式对于 Java 程序员非常有吸引力。
## 总结
在本文中，我们对 Java 中的 Lambda 表达式进行了简单介绍，了解了它们可以被用来提升函数式接口实现的代码质量。关注这个网站可以获得有关 Lambda 的更多知识，因为我还会在这里涉及 Stream API 的内容，并对其如何同 Collections 框架结合在一起使用来给予我们更多 Lambda 的好处进行讨论。

参考链接：
- [https://www.oschina.net/translate/lambda-expressions-in-java-8](https://www.oschina.net/translate/lambda-expressions-in-java-8)