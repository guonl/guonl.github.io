---
layout: post
title: Windows配置Github的SSH-key
categories: Github
description: Github
keywords: Github
---
> gitHub是一个面向开源及私有软件项目的托管平台，因为只支持git 作为唯一的版本库格式进行托管，故名gitHub。

## 检查已存在的SSH key ##
1. 打开Git Bash
2. 输入 
 
	`cd ~/.ssh`

	`ls`
    

3. 检查/.ssh目录来查看是否存在公开的ssh key

## 生成一个新的SSH key并添加到ssh-agent  ##

1. 打开Git Bash
2. 输入 
 
	    ssh-keygen -t rsa -b 4096 -C "your_email@example.com"
    	生成一个新的ssh key，使用填入的邮箱地址作为ssh key的标签，并生成RSA密钥对

3. 看到如下提示时：

	    Enter a file in which to save the key (/Users/you/.ssh/id_rsa): [Press enter] 
    	默认按回车
4. 然后为ssh key设置密码：(**必填)**
5. 查看ssh-anent是否启用

	    eval "$(ssh-agent -s)" 
    	返回进程ID

6. 使用如下指令把ssh key添加到ssh-agent中

	`ssh-add ~/.ssh/id_rsa`  
		
## 为你的github账号添加SSH key ##

1.  前往 GitHub 网站的"account settings"，依次点击"Setting -> SSH Keys"->"New SSH key"
2.  title随填写，key处拷贝 ~/.ssh/id_rsa.pub 中的内容
## 测试你的SSH连接  ##

1. 打开Git Bash
2. 输入 
 
	  ` ssh -T git@github.com ` 
		

3. 看到如下提示时：
	 

    `Hi longfeizheng! You've successfully authenticated, but GitHub does not provide shell access.`

![http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/TIM%E6%88%AA%E5%9B%BE20171116102220.png](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/TIM%E6%88%AA%E5%9B%BE20171116102220.png)