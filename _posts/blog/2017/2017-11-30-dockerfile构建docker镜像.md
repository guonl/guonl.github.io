---
layout: post
title: Dockefile文件创建Docker镜像
categories: Docker
description: Docker
keywords: Docker
---
> Docker 是一个开源的应用容器引擎，让开发者可以打包他们的应用以及依赖包到一个可移植的容器中，然后发布到任何流行的 Linux 机器上，也可以实现虚拟化。容器是完全使用沙箱机制，相互之间不会有任何接口。

### Dockerfile示例 ###

    # Base images 基础镜像
	FROM centos
	
	#MAINTAINER 维护者信息
	MAINTAINER lorenwe 
	
	#ENV 设置环境变量
	ENV PATH /usr/local/nginx/sbin:$PATH
	
	#ADD  文件放在当前目录下，拷过去会自动解压
	ADD nginx-1.13.7.tar.gz /tmp/
	
	#RUN 执行以下命令
	RUN rpm --import /etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-7 \
	    && yum update -y \
	    && yum install -y vim less wget curl gcc automake autoconf libtool make gcc-c++ zlib zlib-devel openssl openssl-devel perl perl-devel pcre pcre-devel libxslt libxslt-devel \
	    && yum clean all \
	    && rm -rf /usr/local/src/*
	RUN useradd -s /sbin/nologin -M www
	
	#WORKDIR 相当于cd
	WORKDIR /tmp/nginx-1.13.7
	
	RUN ./configure --prefix=/usr/local/nginx --user=www --group=www --with-http_ssl_module --with-pcre && make && make install
	
	RUN cd / && rm -rf /tmp/
	
	COPY nginx.conf /usr/local/nginx/conf/
	
	#EXPOSE 映射端口
	EXPOSE 80 443
	
	#ENTRYPOINT 运行以下命令
	ENTRYPOINT ["nginx"]
	
	#CMD 运行以下命令
	CMD ["-h"]
以上代码示例是我编写的一个认为很有代表性的 dockerfile 文件，涉及到的内容不多，但基本上把所有 dockerfile 指令都用上了，也包含一些细节方面的东西，为了达到示例的效果所以并不是最简洁的 dockerfile，建立一个文件夹将以上 dockerfile 放在该文件内，再去 nginx 官网把 nginx 源码包下来放到该文件夹内，之后再在该文件夹内打开命令行窗口，最好是以管理员权限打开命令行窗口，以免出现一些权限问题的错误，此时的目录结构应该是以下样子的

### 指令分析 ###
FROM 表示的是这个 dockerfile 构建镜像的基础镜像是什么，有点像代码里面类的继承那样的关系，基础镜像所拥有的功能在新构建出来的镜像中也是存在的，一般用作于基础镜像都是最干净的没有经过任何三方修改过的，比如我用的就是最基础的 centos，这里有必要说明一下，因为我用的镜像加速源是阿里云的，所以我 pull 下来的 centos 是自带阿里云的 yum 源的镜像，如果你用的不是阿里云的镜像加速源，pull 下来的镜像 yum 源也不一样，到时 yum 安装软件的时候可能会遇到很多问题（你懂得）。

MAINTAINER 就是维护者信息了，填自己名字就可了，不用说什么了

ENV 设置环境变量，简单点说就是设置这个能够帮助系统找到所需要运行的软件，比如我上面写的是 “ENV PATH /usr/local/nginx/sbin:$PATH”，这句话的意思就是告诉系统如果运行一个没有指定路径的程序时可以从 /usr/local/nginx/sbin 这个路径里面找，只有设置了这个，后面才可以直接使用 ngixn 命令来启动 nginx，不然的话系统会提示找不到应用。

ADD 顾名思义，就是添加文件的功能了，但是他比普通的添加做的事情多一点，源文件可以是一个文件，或者是一个 URL 都行，如果源文件是一个压缩包，在构建镜像的时候会自动的把压缩包解压开来，示例我写的是 ‘ADD nginx-1.13.7.tar.gz /tmp/’ 其中 nginx-1.13.7.tar.gz 这个压缩包是必须要在 dockefile 文件目录内的，不在 dockerfile 文件目录内的 比如你写完整路径 D:test/nginx-1.13.7.tar.gz 都是会提示找不到文件的。

RUN 就是执行命令的意思了，RUN 可以执行多条命令， 用 && 隔开就行，如果命令太长要换行的话在末尾加上 ‘\’ 就可以换行命令，RUN 的含义非常简单，就是执行命令，但其中有一些细节还是需要注意的，现在就通过上面的示例来看看需要注意的地方有哪些吧。其中 RUN rpm --import /etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-7 的作用就是导入软件包签名来验证软件包是否被修改过了，为做到安全除了系统要官方的之外软件也要保证是可信的。yum update -y 升级所有包，改变软件设置和系统设置,系统版本内核都升级，我们知道 linux 的软件存在依赖关系，有时我们安装新的软件他所依赖的工具软件也需要是最新的，如果没有用这个命令去更新原来的软件包，就很容易造成我们新安装上的软件出问题，报错提示不明显的情况下我们更是难找到问题了，为避免此类情况发生我们还是先更新一下软件包和系统，虽然这会使 docker 构建镜像时变慢但也是值得的，至于后面的命令自然是安装各种工具库了，接着来看这句 yum clean all ，把所有的 yum 缓存清掉，这可以减少构建出来的镜像大小，rm -rf /usr/local/src/ 清除用户源码文件，都是起到减少构建镜像大小的作用。RUN 指令是可以分步写的，比如上面的 RUN 可以拆成以下这样：

    # 不推荐
	RUN rpm --import /etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-7 \
	RUN yum update -y \
	RUN yum install -y vim less wget curl gcc automake autoconf libtool make gcc-c++ zlib zlib-devel openssl openssl-devel perl perl-devel pcre pcre-devel libxslt libxslt-devel \
	RUN yum clean all \
	RUN rm -rf /usr/local/src/*

这样也是可以的，但是最好不要这样，因为 dockerfile 构建镜像时每执行一个关键指令都会去创建一个镜像版本，这有点像 git 的版本管理，比如执行完第一个 RUN 命令后在执行第二个 RUN 命令时是会在一个新的镜像版本中执行，这会导致 yum clean all 这个命令失效，没有起到精简镜像的作用，虽然不推荐多写几个 RUN，但也不是说把所有的操作都放在一个 RUN 里面，这里有个原则就是把所有相关的操作都放在同一个 RUN 里面，就比如我把 yum 更新，安装工具库，清除缓存放在一个 RUN 里面，后面的编译安装 nginx 放在另外一个 RUN 里面。

WORKDIR 表示镜像活动目录变换到指定目录，就相当于 linux 里面 cd 到指定目录一样，其实完全没有必要使用这个指令的，在需要时可以直接使用 cd 命令就行，因为这里使用了 WORKDIR，所以后面的 RUN 编译安装 nginx 不用切换目录，讲到这里又想起了另外一个问题，如下：

    RUN cd /tmp/nginx-1.13.7
    
    RUN ./configure

这样可不可以呢，我想前面看懂的朋友应该知道答案了吧，这里还是再啰嗦一下，这样是会报找不到 configure 文件错误的，原因很简单，因为这个两个命令都不是在同一个镜像中执行的，第一个镜像 cd 进入的目录并不代表后面的镜像也进入了。

COPY 这个指令很简单，就是把文件拷贝到镜像中的某个目录，注意源文件也是需要在 dockerfile 所在目录的，示例的意思是拷贝一份 nginx 配置文件，现在就在 dockerfile 所在目录创建这个文件

	user  www;
	worker_processes  2;
	daemon off;
	pid        logs/nginx.pid;

	events {    
		worker_connections  1024;
	}

	http { 
	   include       mime.types;
	   default_type  application/octet-stream;

	   sendfile        on;
	   keepalive_timeout  65;
	   server {
	        listen       80;
	        server_name  localhost;
	        location / {
	            root   html;
	            index  index.html index.htm;
	        }
	        error_page   500 502 503 504  /50x.html;
	        location = /50x.html {
	            root   html;
	        }
	    }
	}

配置很简单，就是对官方的配置文件把注释去掉了，注意里面的 daemon off; 配置，意思是关闭 nginx 后台运行，原因在上一篇文章中讲过，这里再来絮叨一下，容器默认会把容器内部第一个进程是否活动作为docker容器是否正在运行的依据，如果 docker 容器运行完就退出了，那么docker容器便会直接退出，docker run 的时候把 command 作为容器内部命令，如果使用 nginx，那么 nginx 程序将后台运行，这个时候 nginx 并不是第一个执行的程序，而是执行的 bash，这个 bash 执行了 nginx 指令后就挂了，所以容器也就退出了，如果我们设置了 daemon off 后启动 nginx 那么 nginx 就会一直占用命令窗口，自然 bash 没法退出了所以容器一直保持活动状态。

EXPOSE 示例注释写的是映射端口，但我觉得用暴露端口来形容更合适，因为在使用 dockerfile 创建容器的时候不会映射任何端口，映射端口是在用 docker run 的时候来指定映射的端口，比如我把容器的 80 端口映射到本机的 8080 端口，要映射成功就要先把端口暴露出来，有点类似于防火墙的功能，把部分端口打开。

ENTRYPOINT 和 CMD 要放在一起来说，这两者的功能都类似，但又有相对独特的地方，他们的作用都是让镜像在创建容器时运行里面的命令。当然前提是这个镜像是使用这个 dockerfile 构建的，也就是说在执行 docker run 时 ENTRYPOINT 和 CMD 里面的命令是会执行的，两者是可以单独使用，并不一定要同时存在，当然这两者还是有区别的。

先从 CMD 说吧，CMD 的一个特点就是可被覆盖，比如把之前的 dockerfile 的 ENTRYPOINT 这一行删除，留下 CMD 填写["nginx"]，构建好镜像后直接使用 docker run lorenwe/centos_nginx 命令执行的话通过 docker ps 可以看到容器正常运行了，启动命令也是 “ngixn”，但是我们使用 docker run lorenwe/centos_nginx bin/bash 来启动的话通过 docker ps 查看到启动命令变成了 bin/bash，这就说明了 dockerfile 的 CMD 指令是可被覆盖的，也可以把他看做是容器启动的一个默认命令，可以手动修改的。

而 ENTRYPOINT 恰恰相反，他是不能被覆盖，也就是说指定了值后再启动容器时不管你后面写的什么 ENTRYPOINT 里面的命令一定会执行，通常 ENTRYPOINT 用法是为某个镜像指定必须运行的应用，例如我这里构建的是一个 centos_nginx 镜像，也就是说这个镜像只运行 ngixn，那么我就可以在 ENTRYPOINT 写上["nginx"]，有些人在构建自己的基础镜像时（基础镜像只安装了一些必要的库）就只有 CMD 并写上 ['bin/bash']，当 ENTRYPOINT 和 CMD 都存在时 CMD 中的命令会以 ENTRYPOINT 中命令的参数形式来启动容器，例如上面的示例 dockerfile，在启动容器时会以命令为 nginx -h 来启动容器，遗憾的是这样不能保持容器运行，所以可以这样启动 docker run -it lorenwe/centos_nginx -c /usr/local/nginx/conf/nginx.conf，那么容器启动时运行的命令就是 nginx -c /usr/local/nginx/conf/nginx.conf，是不是很有意思，可以自定义启动参数了。

当然还有一些没有用到的指令：

ARG，ARG指令用以定义构建时需要的参数，比如可以在 dockerfile中写上这句 ARG a_nother_name=a_default_value，ARG指令定义的参数，在docker build命令中以 --build -arg a_name=a_value 形式赋值，这个用的一般比较少。
VOLUME，VOLUME指令创建一个可以从本地主机或其他容器挂载的挂载点，用法是比较多的，都知道 docker 做应用容器比较方便，其实 docker 也可做数据容器，创建数据容器镜像的 dockerfile 就主要是用 VOLUME 指令，要讲明 VOLUME 用法有必要在开一篇文章，再此就不做介绍了，

USER，USER用来切换运行属主身份的。docker 默认是使用 root 用户，但若不需要，建议切换使用者身分，毕竟 root 权限太大了，使用上有安全的风险。LABEL，定义一个 image 标签。

### 构建演示 ###
dockerfile 构建镜像的命令很简单，在我的示例中我的命令是 "docker build -t lorenwe/centos_nginx . ",注意后面的点不能省略，表示的从当前目录中寻找 dockerfile 来构建镜像
    docker build -t lorenwe/centos_nginx .



原文链接：[https://juejin.im/post/5a1bd8a36fb9a0450f21a966](https://juejin.im/post/5a1bd8a36fb9a0450f21a966 "https://juejin.im/post/5a1bd8a36fb9a0450f21a966")
