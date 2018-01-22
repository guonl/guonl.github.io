---
layout: post
title: Java InputStream 详解
categories: Java
description: Java InputStream 详解
keywords: InputStream,java
---
![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/javase_io.jpg)
#### 一、字节流
##### 1.字节流有输入和输出流，我们首先看输入流InputStream,我们首先解析一个例子（FileInputStream）。
<!--more-->
```java
package com.merryyou.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created on 2016/11/15 0015.
 *
 * @author zlf
 * @since 1.0
 */
public class FileCount {
    /**
     * 我们写一个检测文件长度的小程序，利用Try-Catch-Finally管理资源
     */
    public static void main(String[] args) {
        int count = 0;  //统计文件字节长度
        try(InputStream stream = new FileInputStream(new File("C:/Users/Administrator/Desktop/javase_io.jpg"))) {
          /*1.new File()里面的文件地址也可以写成D:\\David\\Java\\java 高级进阶\\files\\tiger.jpg,前一个\是用来对后一个
          */
            while (stream.read() != -1) {  //读取文件字节，并递增指针到下一个字节
                count++;
            }
            System.out.println("---长度是： " + count + " 字节");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
```
> 我们一步一步来，首先，上面的程序存在问题是，每读取一个自己我都要去用到FileInputStream，我输出的结果是“---长度是： 64982 字节”，那么进行了64982次操作！可能想象如果文件十分庞大，这样的操作肯定会出大问题，所以引出了缓冲区的概念。可以将stream.read()改成stream.read(byte[]b)此方法读取的字节数目等于字节数组的长度，读取的数据被存储在字节数组中，返回读取的字节数，InputStream还有其他方法mark,reset,markSupported方法，例如：

- markSupported 判断该输入流能支持mark 和 reset 方法。
- mark用于标记当前位置；在读取一定数量的数据(小于readlimit的数据)后使用reset可以回到mark标记的位置。FileInputStream不支持mark/reset操作；BufferedInputStream支持此操作；
- mark(readlimit)的含义是在当前位置作一个标记，制定可以重新读取的最大字节数，也就是说你如果标记后读取的字节数大于readlimit，你就再也回不到回来的位置了。
- 通常InputStream的read()返回-1后，说明到达文件尾，不能再读取。除非使用了mark/reset。

##### 2.FileOutputStream 循序渐进版， InputStream是所有字节输出流的父类，子类有ByteArrayOutputStream,FileOutputStream,ObjectOutputStreanm,这些我们在后面都会一一说到。先说FileOutputStream

我以一个文件复制程序来说，顺便演示一下缓存区的使用。(Java I/O默认是不缓冲流的，所谓“缓冲”就是先把从流中得到的一块字节序列暂存在一个被称为buffer的内部字节数组里，然后你可以一下子取到这一整块的字节数据，没有缓冲的流只能一个字节一个字节读，效率孰高孰低一目了然。有两个特殊的输入流实现了缓冲功能，一个是我们常用的BufferedInputStream)
```java
package com.merryyou.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created on 2016/11/15 0015.
 *
 * @author zlf
 * @since 1.0
 */
public class FileCopy {
    public static void main(String[] args) {
        byte[] buffer = new byte[1024*3];   //一次取出的字节数大小,缓冲区大小
        int numberRead = 0;
        try (FileInputStream input = new FileInputStream("C:/Users/Administrator/Desktop/javase_io.jpg");
             FileOutputStream out = new FileOutputStream("C:/Users/Administrator/Desktop/javase_io1.jpg")) {
            //如果文件不存在会自动创建
            while ((numberRead = input.read(buffer)) != -1) {  //numberRead的目的在于防止最后一次读取的字节小于buffer长度，
                out.write(buffer, 0, numberRead);       //否则会自动被填充0
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
```


