---
layout: post
title: Docker Compose 官方阅读笔记
categories: Docker
description: Docker
keywords: Docker
---
> 我们将构建一个运行在`Docker`上的简单的`Python Web`应用程序。
应用程序使用`Flask` 框架，并在redis中维护一个计数器。
虽然示例使用python，即使你不熟悉它也没关系。 



## 前言
确保你已经安装了`docker engine`和`docker compose`。不需要安装`python`或`redis`，因为两者都是由`docker`镜像提供的。

如下：

![http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_01.png](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_01.png)

## 第一步：设置 
定义应用程序依赖关系
1. 为该项目创建目录
	```shell
	$ mkdir compose
	$ cd compose
	```
2. 在项目目录下创建一个名为app.py的文件，并设置内容如下：
	```python
	from flask import Flask
	from redis import Redis

	app = Flask(__name__)
	redis = Redis(host='redis', port=6379)

	@app.route('/')
	def hello():
		count = redis.incr('hits')
		return 'Hello World! I have been seen {} times.\n'.format(count)

	if __name__ == "__main__":
		app.run(host="0.0.0.0", debug=True)
	```
	在这个例子中，redis是应用程序网络上的redis容器的主机名。我们使用redis的默认端口6379
3. 在项目目录中创建另一个名为requirements.txt的文件，并设置内容如下：
	```
	flask
	redis
	```

## 第二步：创建Dockerfile文件 
在这一步中，我们将编写一个构建`docker`镜像的`dockerfile`文件。该文件包含`python`应用程序所需的所有依赖项，也包括python本身。

在我们的项目目录中，创建一个名为`dockerfile`的文件并粘贴以下内容：
```shell
FROM python:3.4-alpine
ADD . /code
WORKDIR /code
RUN pip install -r requirements.txt
CMD ["python", "app.py"]
```
命令的解释如下：
- 从`Python 3.4`镜像基础上创建容器
- 把当前目录添加到容器的`/code`目录中
- 将容器的工作目录设置为`/code`
- 安装`python`所需要的依赖
- 设置容器的默认命令`python app.py`

## 第三步：在docker-compose.yml中定义服务
在我们的项目目录下创建一个名为`docker-compose.yml`的文件并粘贴下面的代码：
```shell
version: '3'
services:
  web:
    build: .
    ports:
     - "5000:5000"
    links:
     - redis
  redis:
    image: "redis:alpine"
```
这个组合文件定义了两个服务，`web`和`redis`。`web`服务：
- 使用从当前目录中的`dockerfile`构建的容器
- 将容器上的暴露端口5000转发到主机上的端口5000。我们使用`Flask web`服务器的默认端口5000。

redis服务使用从docker注册表中心中提取的公共redis映像.

目录结构如下：

[![http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_02.png](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_02.png "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_02.png")](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_02.png "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_02.png")

## 第四步：构建和运行我们的应用程序

1. 从我们的项目目录中，通过运行`docker-compose up`启动您的应用程序.如下：

![http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_03.png](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_03.png "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_03.png")

`docker-compose.yml`文件拉去`Redis`镜像，构建我们的镜像并启动服务。

2. 在浏览器中输入`http://localhost:5000/`，浏览器回输出以下消息

```
Hello World! I have been seen 1 times.
```

3. 刷新页面数字会递增

```
Hello World! I have been seen 2 times.
```

4. 开启另一个终端使用 `docker images `列出镜像。

[![http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_06.png](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_06.png "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_06.png")](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_06.png "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_06.png")

5. 停止应用程序，通过在第二个终端的项目目录中运行`docker-compose down`，或者在启动应用程序的原始终端中按ctrl + c

## 第五步：修改`docker-cmopose.yml` 添加挂载目录 如下：

```shell
version: '3'
services:
  web:
    build: .
    ports:
     - "5000:5000"
    volumes:
     - .:/code
  redis:
    image: "redis:alpine"
```

`volumes` 关键字挂在当前目录到容器的`/code`,允许我们即时修改代码，而无需重新生成镜像。

## 第六步：重新构建并运行应用程序

从项目目录中输入`docker-compose up`，然后用已更新的文件构建应用程序，运行它

```shell
$ docker-compose up
Creating network "composetest_default" with the default driver
Creating composetest_web_1 ...
Creating composetest_redis_1 ...
Creating composetest_web_1
Creating composetest_redis_1 ... done
Attaching to composetest_web_1, composetest_redis_1
web_1    |  * Running on http://0.0.0.0:5000/ (Press CTRL+C to quit)
...
```

再次在Web浏览器中查看hello world消息，然后刷新以查看有没有递增。

## 第七步：更新应用程序

由于应用程序代码现在使用数据卷挂载到容器中，因此可以更改本地代码并立即查看更改后效果，而无需重新生成镜像。

1. 更改`app.py`中` Hello World!` 为 `Hello from Docker!`:

```python
return 'Hello from Docker! I have been seen {} times.\n'.format(count)
```

2. 刷新浏览器，`Hello World`!更新成`Hello from Docker` 数字依旧递增

## 第八步：扩展其它命令

如果你想在后台运行你的服务，可以使用 `-d `参数，并且可以使用`docker-compose ps`查看当前运行的服务。
```shell
$ docker-compose up -d
Starting composetest_redis_1...
Starting composetest_web_1...

$ docker-compose ps
Name                 Command            State       Ports
-------------------------------------------------------------------
composetest_redis_1   /usr/local/bin/run         Up
composetest_web_1     /bin/sh -c python app.py   Up      5000->5000/tcp
```

`docker-compose run`命令允许为服务运行一次性命令。例如，查看哪些环境变量可用于Web服务

```shell
 docker-compose run web env
```
![http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_04.png](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_04.png "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_04.png")


可以参阅`docker-compose --help`以查看其他可用的命令。

如果开始使用`docker-compose up -d`运行服务，则可以使用 `docker-compose stop` 命令停止服务运行。

```shell
$ docker-compose stop
```

你可以把所有服务停下，用`down`命令完全移除容器。通过`--volumes`还删除`redis`容器使用的数据卷：
```shell
$ docker-compose down --volumes
```
![http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_05.png](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_05.png "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_05.png")

翻译链接：[https://docs.docker.com/compose/gettingstarted/#step-8-experiment-with-some-other-commands](https://docs.docker.com/compose/gettingstarted/#step-8-experiment-with-some-other-commands "https://docs.docker.com/compose/gettingstarted/#step-8-experiment-with-some-other-commands")



















