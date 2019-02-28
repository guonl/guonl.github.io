---
layout: post
title: 使用 Docker 搭建代码质量检测平台 SonarQube
categories: Docker
description: SonarQube
keywords: Docker
---
> 想成为一名优秀的工程师，代码质量一定要过关！

### 开始搭建

1. 获取 postgresql 的镜像
  	-   `docker pull postgres`
2. 启动 postgresql
    - `docker run --name db -e POSTGRES_USER=sonar -e POSTGRES_PASSWORD=sonar -d postgres`
3. 获取 sonarqube 的镜像 
    - `docker pull sonarqube`
4. 启动 sonarqube
	-  `docker run --name sq --link db -e SONARQUBE_JDBC_URL=jdbc:postgresql://db:5432/sonar -p 9000:9000 -d sonarqube`
### 代码质量检验
1. 打开 [http://localhost:9000/](http://localhost:9000/ "http://localhost:9000/") , 点击 "Log in"
![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170911160200.png)
> 登录账号：admin 密码：admin

2. 以 Maven 项目为例
3. 执行命令，检测代码质量
	- `mvn sonar:sonar`
4. 成功之后，返回到浏览器，就可以浏览自己的项目的代码质量了
![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170911160226.png)
![](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/TIM%E6%88%AA%E5%9B%BE20170911160250.png)

参考链接：[http://www.jianshu.com/p/a1450aeb3379](http://www.jianshu.com/p/a1450aeb3379 "http://www.jianshu.com/p/a1450aeb3379")