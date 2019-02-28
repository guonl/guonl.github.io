---
layout: post
title: Windows-IntelliJ-IDEA-helloDocker
categories: Docker
description: helloDocker
keywords: Docker
---
> Docker 是一个开源的应用容器引擎，让开发者可以打包他们的应用以及依赖包到一个可移植的容器中，然后发布到任何流行的 Linux 机器上，也可以实现虚拟化。容器是完全使用沙箱机制，相互之间不会有任何接口。

### 安装docker

1. [Docker官网](https://www.docker.com/docker-windows "Docker官网")下载Docker,然后一直下一步。启动时可能会出现内存不足的错误。桌面右下角找到docker图标右键->settings设置如下
[![docker](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170822091932.png "docker")](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170822091932.png "docker")
一般就可以正常启动了，如若不行，关机重启一下电脑。
2. 设置docker加速器[DaoCloud ](https://account.daocloud.io "DaoCloud ")。注册登录后进入到控制面板右上角![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170822093045.png)点击选择windows获得你自己的加速地址。然后在docker中加入![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170822092141.png)，apply后docker会自动重启。命令行中使用`docker info`命令![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170822092105.png)表示加速成功。

### IDEA安装docker插件并配置docker

1. `file->setting->plugins`安装`docker integration`插件![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170822093532.png)
2. IDEA配置docker链接`file->setting->Build,Execution,Deployment->Clouds`选择加号添加Docker![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170822093705.png),会提示链接失败，需要配置一下客户端docker如下![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170822092201.png)idea下方控制面板上会显示docker选项，右键链接如下[](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170822094457.png)

### helloDocker
1. 创建一个静态的web项目![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170822094734.png)
2. 创建`docker-dir`目录并在目录下创建Dockerfile文件目录结构如下![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170822094932.png)
3. 配置artifact helloDocker.war包![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170822093734.png)
4. 编写`Dockerfile`和配置`Docker Deployment`

	Dockerfile如下：
	
	    FROM jboss/wildfly
		ADD helloDocker.war /opt/jboss/wildfly/standalone/deployments/
	Docker Deployment如下：(**配置Container，先单机后面的向右箭头，然后指向docker-dir会自动生成container_settings.json**)![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170822093755.png)![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170822093807.png)
5. 以Docker方式运行和访问：![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170822133601.png)![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170822133715.png)

参考链接：[http://www.jetbrains.com/help/idea/2016.2/docker.html#d190283e119](http://www.jetbrains.com/help/idea/2016.2/docker.html#d190283e119 "http://www.jetbrains.com/help/idea/2016.2/docker.html#d190283e119")(这是适合安装docker toolbox的用户)

