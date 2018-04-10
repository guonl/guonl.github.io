---
layout: post
title: Docker容器化应用日志中心 -- ELK的搭建
categories: Docker系列
description: Docker容器化应用日志中心 -- ELK的搭建
keywords: docker,ELK,nginx,redis
---

## 前言
应用一旦容器化以后，需要考虑的就是如何采集位于Docker容器中的应用程序的打印日志供运维分析。典型的比如SpringBoot应用的日志收集。本文即将阐述如何利用ELK日志中心来收集容器化应用程序所产生的日志，并且可以用可视化的方式对日志进行查询与分析，其架构如下图所示：

![架构图](https://upload-images.jianshu.io/upload_images/714680-ba4badb971150351.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


## 镜像准备

镜像的来源：[网易云镜像中心](https://c.163yun.com/hub#/m/home/)

- ElasticSearch镜像
- Logstash镜像
- Kibana镜像
- Nginx镜像（生产日志来源之一，磁盘文件）
- Redis镜像（缓存生产日志，方便容器与容器之间的交互，可选）

![image](https://upload-images.jianshu.io/upload_images/714680-be068c9e10a65412.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

这里需要解释一下：
1. 如果采用nginx的日志作为日志的来源，首先需要解决的问题就是，容器化的`logstash日志收集器`，如何读取到容器化的nginx内部的日志问题？我们可以通过Docker容器中的Nginx应用日志转发到宿主机的syslog服务中，然后由syslog服务将数据转给容器化的Logstash进行收集，本方案是该篇文章介绍的重点；

2. 如果采用redis作为日志的来源，可以很好的解决容器间文件隔离的问题，redis作为中间缓冲，web应用的日志可以先缓存在redis中，logstash从redis从读取后，发送给ElasticSearch，这样就可以完美的实现了，备选方案；

## 开启Linux系统Rsyslog服务

修改Rsyslog服务配置文件：


```shell
vim /etc/rsyslog.conf
```


开启下面三个参数：


```shell
$ModLoad imtcp

$InputTCPServerRun 514

*.* @@localhost:4560
```
意图很简单：让Rsyslog加载imtcp模块并监听514端口，然后将Rsyslog中收集的数据转发到本地4560端口！

然后重启Rsyslog服务

```shell
systemctl restart rsyslog
```

查看rsyslog启动状态：

```shell
netstat -tnl
```


![image](https://upload-images.jianshu.io/upload_images/714680-049c43418ead63c9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


## 部署ElasticSearch服务

启动ElasticSearch容器
```shell
docker run -d  -p 9200:9200 \
 -v $PWD/elasticsearch/data:/usr/share/elasticsearch/data \
 --name elasticsearch hub.c.163.com/library/elasticsearch
```

启动后可以通过这个url来查看：http://localhost:9200/
![image](https://upload-images.jianshu.io/upload_images/714680-7899f9e2f930b598.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 部署Logstash服务

因为Logstash的配置文件需要自定义，所以我们需要定制化一个符合自己需求的镜像，
创建Dockerfile，添加下面的内容：

```shell
FROM hub.c.163.com/library/logstash
MAINTAINER guonl 983220871@qq.com
COPY logstash.conf /etc/logstash.conf
```

在当前文件夹下面创建logstash.conf，添加内容如下：

```shell
input {
  syslog {
    type => "rsyslog"
    port => 4560
  }
}

output {
  elasticsearch {
    hosts => [ "elasticsearch:9200" ]
  }
}
```

构建自己的logstash镜像

```shell
docker build -t guonl/logstash:latest .
```
这个镜像只是简单地修改了一下配置文件而已，之后启动容器

```shell
docker run -d -p 4560:4560 \
--link elasticsearch:elasticsearch \
--name logstash guonl/logstash \
logstash -f /etc/logstash.conf
```

## 部署Kibana服务

```shell
docker run -d -p 5601:5601 \
--link elasticsearch:elasticsearch \
-e ELASTICSEARCH_URL=http://elasticsearch:9200 \
--name kibana hub.c.163.com/library/kibana
```
 我们查看服务的进程和docker的log
 
```shell
docker ps
docker logs logstash
docker logs kibana
```

![image](https://upload-images.jianshu.io/upload_images/714680-c50f2ba942aafdc5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



## 启动Nginx容器来生产日志


```shell
docker run -d -p 90:80 --log-driver syslog --log-opt \
syslog-address=tcp://localhost:514 \
--log-opt tag="nginx" --name nginx nginx
```

很明显Docker容器中的Nginx应用日志转发到本地syslog服务中，然后由syslog服务将数据转给Logstash进行收集。

至此，日志中心搭建完毕，目前一共四个容器在工作


实验验证
- 浏览器打开localhost:90来打开Nginx界面，并刷新几次，让后台产生GET请求的日志
- 打开Kibana可视化界面：localhost:5601

![image](https://upload-images.jianshu.io/upload_images/9824247-6cda91b21f1c0b50.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

- 收集Nginx应用日志

![image](https://upload-images.jianshu.io/upload_images/9824247-86fe3ba2c9e9889a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)


- 查询应用日志
在查询框中输入program=nginx可查询出特定日志

![image](https://upload-images.jianshu.io/upload_images/9824247-2870e77bbc2cf67b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)



---
---
接下来介绍一下第二套方案，使用redis来取代nginx

## 部署nginx服务

```shell
docker run -d -p 6379:6379 --name redis hub.c.163.com/library/redis
```

接来下唯一的不同就是配置logstash

## 部署第二套方案的Logstash服务

logstash.conf配置文件修改，修改成数据源来自redis

vim logstash.conf

```shell
input {
    redis {
        data_type => "list" #使用redis的list类型存储数据
        key => "logstash"   #key为"logstash"
        host => "redis"     #host 是关联了redis容器的名称
        port => 6379        #端口号
        threads => 5        #启用线程数量
        codec => "json"
    }
}
filter {
}
output {
  elasticsearch {
    hosts => [ "elasticsearch:9200" ]
  }
}
```

注意启动指令和之前的不同：

```shell
docker run -d -p 4560:4560 \
--link elasticsearch:elasticsearch \
--link redis:redis \  # 关联redis容器
--name logstash guonl/logstash \
logstash -f /etc/logstash.conf
```

## 测试&验证：

java客户端代码

```java
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by guonl
 * Date 2018/4/10 下午2:49
 * Description:
 */
public class RedisDemoTest {

    private final String ip = "localhost";

    private final int port = 6379;

    private final String key = "logstash";//就是logstash里面配置的key

    @Test
    public void test() {
        JedisPool jedisPool = new JedisPool(ip, port);
        Jedis jedis = jedisPool.getResource();
        for (int i = 0; i <= 10; i++) {
            jedis.lpush(key, "测试日志0" + i);//通过list方式存放
            System.out.println("测试日志0" + i);
        }

    }
}
```
代码执行成功之后，登录kibana查看，可以看到我们日志已经在页面上显示了：

![image](https://upload-images.jianshu.io/upload_images/714680-9ec776439261506e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


参考链接：

- [http://mp.weixin.qq.com/s/VJ_an5gILsemrlgQA9Ctig](http://mp.weixin.qq.com/s/VJ_an5gILsemrlgQA9Ctig)