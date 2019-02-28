---
layout: post
title: Java问题排查工具单
categories: Java
description: Java问题排查工具单
keywords: Java,工具,linux
---
## 前言
> 平时的工作中经常碰到很多疑难问题的处理，在解决问题的同时，有一些工具起到了相当大的作用，在此书写下来，一是作为笔记，可以让自己后续忘记了可快速翻阅，二是分享，希望看到此文的同学们可以拿出自己日常觉得帮助很大的工具，大家一起进步。 闲话不多说，开搞。 ## Linux命令类 ###tail 最常用的tail -f tail -3

### Linux命令类
#### tail
最常用的tail -f
```shell
tail -300f shopbase.log #倒数300行并进入实时监听文件写入模式
```
#### grep
```shell
grep forest f.txt   #文件查找
grep forest f.txt cpf.txt #多文件查找
grep 'log' /home/admin -r -n #目录下查找所有符合关键字的文件
cat f.txt | grep -i shopbase    
grep 'shopbase' /home/admin -r -n --include *.{vm,java} #指定文件后缀
grep 'shopbase' /home/admin -r -n --exclude *.{vm,java} #反匹配
seq 10 | grep 5 -A 3    #上匹配
seq 10 | grep 5 -B 3    #下匹配
seq 10 | grep 5 -C 3    #上下匹配，平时用这个就妥了
cat f.txt | grep -c 'SHOPBASE'
```
#### awk

1. 基础命令
```shell
awk '{print $4,$6}' f.txt
awk '{print NR,$0}' f.txt cpf.txt   
awk '{print FNR,$0}' f.txt cpf.txt
awk '{print FNR,FILENAME,$0}' f.txt cpf.txt
awk '{print FILENAME,"NR="NR,"FNR="FNR,"$"NF"="$NF}' f.txt cpf.txt
echo 1:2:3:4 | awk -F: '{print $1,$2,$3,$4}'
```

1. 匹配
```shell
awk '/ldb/ {print}' f.txt   #匹配ldb
awk '!/ldb/ {print}' f.txt  #不匹配ldb
awk '/ldb/ && /LISTEN/ {print}' f.txt   #匹配ldb和LISTEN
awk '$5 ~ /ldb/ {print}' f.txt #第五列匹配ldb
```

1. 内建变量

NR,NR表示从awk开始执行后，按照记录分隔符读取的数据次数，默认的记录分隔符为换行符，因此默认的就是读取的数据行数，NR可以理解为Number of Record的缩写。

FNR:在awk处理多个输入文件的时候，在处理完第一个文件后，NR并不会从1开始，而是继续累加，因此就出现了FNR，每当处理一个新文件的时候，FNR就从1开始计数，FNR可以理解为File Number of Record。

NF: NF表示目前的记录被分割的字段的数目，NF可以理解为Number of Field。

#### find
```shell
sudo -u admin find /home/admin /tmp /usr -name \*.log(多个目录去找)
find . -iname \*.txt(大小写都匹配)
find . -type d(当前目录下的所有子目录)
find /usr -type l(当前目录下所有的符号链接)
find /usr -type l -name "z*" -ls(符号链接的详细信息 eg:inode,目录)
find /home/admin -size +250000k(超过250000k的文件，当然+改成-就是小于了)
find /home/admin f -perm 777 -exec ls -l {} \; (按照权限查询文件)
find /home/admin -atime -1  1天内访问过的文件
find /home/admin -ctime -1  1天内状态改变过的文件 
find /home/admin -mtime -1  1天内修改过的文件
find /home/admin -amin -1  1分钟内访问过的文件
find /home/admin -cmin -1  1分钟内状态改变过的文件   
find /home/admin -mmin -1  1分钟内修改过的文件
```
#### pgm
批量查询vm-shopbase满足条件的日志
```shell
pgm -A -f vm-shopbase 'cat /home/admin/shopbase/logs/shopbase.log.2017-01-17|grep 2069861630'
```
#### tsar
tsar是咱公司自己的采集工具。很好用, 将历史收集到的数据持久化在磁盘上，所以我们快速来查询历史的系统数据。当然实时的应用情况也是可以查询的啦。大部分机器上都有安装。
```shell
tsar  ##可以查看最近一天的各项指标
tsar --live ##可以查看实时指标，默认五秒一刷
tsar -d 20161218 ##指定查看某天的数据，貌似最多只能看四个月的数据
tsar --mem
tsar --load
tsar --cpu
##当然这个也可以和-d参数配合来查询某天的单个指标的情况 
```
#### top
top除了看一些基本信息之外，剩下的就是配合来查询vm的各种问题了
```shell
ps -ef | grep java
top -H -p pid
```
获得线程10进制转16进制后jstack去抓看这个线程到底在干啥
#### 其他
```shell
netstat -nat|awk  '{print $6}'|sort|uniq -c|sort -rn 
#查看当前连接，注意close_wait偏高的情况，比如如下
```
### 排查利器
#### btrace
首当其冲的要说的是btrace。真是生产环境&预发的排查问题大杀器。 简介什么的就不说了。直接上代码干

a. 查看当前谁调用了ArrayList的add方法，同时只打印当前ArrayList的size大于500的线程调用栈
```java
@OnMethod(clazz = "java.util.ArrayList", method="add", location = @Location(value = Kind.CALL, clazz = "/.*/", method = "/.*/"))
public static void m(@ProbeClassName String probeClass, @ProbeMethodName String probeMethod, @TargetInstance Object instance, @TargetMethodOrField String method) {
    if(getInt(field("java.util.ArrayList", "size"), instance) > 479){
        println("check who ArrayList.add method:" + probeClass + "#" + probeMethod  + ", method:" + method + ", size:" + getInt(field("java.util.ArrayList", "size"), instance));
        jstack();
        println();
        println("===========================");
        println();
    }
}
```
b. 监控当前服务方法被调用时返回的值以及请求的参数
```java
@OnMethod(clazz = "com.taobao.sellerhome.transfer.biz.impl.C2CApplyerServiceImpl", method="nav", location = @Location(value = Kind.RETURN))
public static void mt(long userId, int current, int relation, String check, String redirectUrl, @Return AnyType result) {
        println("parameter# userId:" + userId + ", current:" + current + ", relation:" + relation + ", check:" + check + ", redirectUrl:" + redirectUrl + ", result:" + result);

}
```
其他功能集团的一些工具或多或少都有，就不说了。感兴趣的请移步。
https://github.com/btraceio/btrace

注意:

经过观察，1.3.9的release输出不稳定，要多触发几次才能看到正确的结果

正则表达式匹配trace类时范围一定要控制，否则极有可能出现跑满CPU导致应用卡死的情况

由于是字节码注入的原理，想要应用恢复到正常情况，需要重启应用。
#### Greys
Greys是@杜琨的大作吧。说几个挺棒的功能(部分功能和btrace重合):

sc -df xxx: 输出当前类的详情,包括源码位置和classloader结构

trace class method: 相当喜欢这个功能! 很早前可以早JProfiler看到这个功能。打印出当前方法调用的耗时情况，细分到每个方法。对排查方法性能时很有帮助，比如我之前这篇就是使用了trace命令来的:http://www.atatech.org/articles/52947。

其他功能部分和btrace重合，可以选用，感兴趣的请移步。
http://www.atatech.org/articles/26247

另外相关联的是arthas，他是基于Greys的，感兴趣的再移步http://mw.alibaba-inc.com/products/arthas/docs/middleware-container/arthas.wiki/home.html?spm=a1z9z.8109794.header.32.1lsoMc

#### javOSize
就说一个功能
classes：通过修改了字节码，改变了类的内容，即时生效。 所以可以做到快速的在某个地方打个日志看看输出，缺点是对代码的侵入性太大。但是如果自己知道自己在干嘛，的确是不错的玩意儿。

其他功能Greys和btrace都能很轻易做的到，不说了。

可以看看我之前写的一篇javOSize的简介http://www.atatech.org/articles/38546
官网请移步http://www.javosize.com/

#### JProfiler
之前判断许多问题要通过JProfiler，但是现在Greys和btrace基本都能搞定了。再加上出问题的基本上都是生产环境(网络隔离)，所以基本不怎么使用了，但是还是要标记一下。
官网请移步https://www.ej-technologies.com/products/jprofiler/overview.html

### 大杀器
#### eclipseMAT
可作为eclipse的插件，也可作为单独的程序打开。 
详情请移步http://www.eclipse.org/mat/

#### zprofiler
集团内的开发应该是无人不知无人不晓了。简而言之一句话:有了zprofiler还要mat干嘛
详情请移步zprofiler.alibaba-inc.com

### java三板斧，噢不对，是七把
#### jps
```shell
sudo -u admin /opt/taobao/java/bin/jps -mlvV
```
#### jstack
```shell
sudo -u admin /opt/taobao/install/ajdk-8_1_1_fp1-b52/bin/jstack 2815
```
#### native+java栈:
```shell
sudo -u admin /opt/taobao/install/ajdk-8_1_1_fp1-b52/bin/jstack -m 2815
```
#### jinfo
```shell
sudo -u admin /opt/taobao/install/ajdk-8_1_1_fp1-b52/bin/jinfo -flags 2815
```
#### jmap
1.查看堆的情况
```shell
sudo -u admin /opt/taobao/install/ajdk-8_1_1_fp1-b52/bin/jmap -heap 2815
```
2.dump
```shell
sudo -u admin /opt/taobao/install/ajdk-8_1_1_fp1-b52/bin/jmap -dump:live,format=b,file=/tmp/heap2.bin 2815
```
```shell
sudo -u admin /opt/taobao/install/ajdk-8_1_1_fp1-b52/bin/jmap -dump:format=b,file=/tmp/heap3.bin 2815
```
3.看看堆都被谁占了? 再配合zprofiler和btrace，排查问题简直是如虎添翼
```shell
sudo -u admin /opt/taobao/install/ajdk-8_1_1_fp1-b52/bin/jmap -histo 2815 | head -10
```
#### jstat
jstat参数众多，但是使用一个就够了
```shell
sudo -u admin /opt/taobao/install/ajdk-8_1_1_fp1-b52/bin/jstat -gcutil 2815 1000 
```
#### jdb
时至今日，jdb也是经常使用的。 
jdb可以用来预发debug,假设你预发的java_home是/opt/taobao/java/，远程调试端口是8000.那么
```shell
sudo -u admin /opt/taobao/java/bin/jdb -attach 8000.
```

原文链接：
[我的java问题排查工具单](https://yq.aliyun.com/articles/69520?utm_campaign=wenzhang&utm_medium=article&utm_source=QQ-qun&utm_content=m_10374 "我的java问题排查工具单")