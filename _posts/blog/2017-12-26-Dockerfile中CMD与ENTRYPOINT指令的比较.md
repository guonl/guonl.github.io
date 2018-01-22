---
layout: post
title: Dockerfile创建自定义Docker镜像以及CMD与ENTRYPOINT指令的比较
categories: Docker
description: Docker
keywords: Docker
---
> docker是一个开源的引擎，可以轻松的为任何应用创建一个轻量级的、可移植的、自给自足的容器。开发者在笔记本上编译测试通过的容器可以批量地在生产环境中部署，包括VMs（虚拟机）、bare metal、OpenStack 集群和其他的基础应用平台。 



# 1.概述

创建<span style="font-family: Times New Roman;">Docker</span><span style="font-family: 宋体;">镜像的方式有三种</span>

*   docker&nbsp;commit<span style="font-family: 宋体;">命令：由容器生成镜像；</span>
*   Dockerfile<span style="font-family: 宋体;">文件</span><span style="font-family: Times New Roman;">+docker&nbsp;build</span><span style="font-family: 宋体;">命令；</span>
*   从本地文件系统导入：<span style="font-family: Times New Roman;">OpenVZ</span><span style="font-family: 宋体;">的模板。</span>


最近学习了<span style="font-family: Times New Roman;">Dockerfile</span><span style="font-family: 宋体;">文件的相关配置，这里做一下简单的总结，并对之前一直感到有些迷惑的</span><span style="font-family: Times New Roman;">CMD</span><span style="font-family: 宋体;">和</span><span style="font-family: Times New Roman;">ENTRYPOINT</span><span style="font-family: 宋体;">指令</span>做个差异对比。

# 2.Dockerfile文件总结

Dockerfile&nbsp;<span style="font-family: 宋体;">由一行行命令语句组成，并且支持以&nbsp;</span><span style="font-family: Times New Roman;">#&nbsp;</span><span style="font-family: 宋体;">开头的注释行。</span>

一般地，Dockerfile&nbsp;<span style="font-family: 宋体;">分为四部分：基础镜像信息、维护者信息、镜像操作指令和容器启动时执行指令。</span>

<table>
<tbody>
<tr>
<td valign="top" width="198">

四部分

</td>
<td valign="top" width="369">

指令

</td>
</tr>
<tr>
<td valign="top" width="198">

基础镜像信息

</td>
<td valign="top" width="369">

FROM

</td>
</tr>
<tr>
<td valign="top" width="198">

维护者信息

</td>
<td valign="top" width="369">

MAINTAINER

</td>
</tr>
<tr>
<td valign="top" width="198">

镜像操作指令

</td>
<td valign="top" width="369">

RUN<span style="font-family: 宋体;">、</span><span style="font-family: Times New Roman;">COPY</span><span style="font-family: 宋体;">、</span><span style="font-family: Times New Roman;">ADD</span><span style="font-family: 宋体;">、</span><span style="font-family: Times New Roman;">EXPOSE</span><span style="font-family: 宋体;">等</span>

</td>
</tr>
<tr>
<td valign="top" width="198">

容器启动时执行指令

</td>
<td valign="top" width="369">

CMD<span style="font-family: 宋体;">、</span><span style="font-family: Times New Roman;">ENTRYPOINT</span>

</td>
</tr>
</tbody>
</table>

Dockerfile<span style="font-family: 宋体;">文件的第一条指令必须是</span><span style="font-family: Times New Roman;">FROM</span><span style="font-family: 宋体;">，其后可以是各种镜像的操作指令，最后是</span><span style="font-family: Times New Roman;">CMD</span><span style="font-family: 宋体;">或</span><span style="font-family: Times New Roman;">ENTRYPOINT</span><span style="font-family: 宋体;">指定容器启动时执行的命令。</span>

下面引用[yeasy/docker_practice](https://github.com/yeasy/docker_practice/blob/master/dockerfile/instructions.md)对<span style="font-family: Times New Roman;">Dockerfile</span><span style="font-family: 宋体;">中各个指令的介绍，</span>

> **指令**
> 
> 指令的一般格式为&nbsp;<span style="font-family: Times New Roman;">INSTRUCTION&nbsp;arguments<span style="font-family: 宋体;">，指令包括&nbsp;<span style="font-family: Times New Roman;">FROM<span style="font-family: 宋体;">、<span style="font-family: Times New Roman;">MAINTAINER<span style="font-family: 宋体;">、<span style="font-family: Times New Roman;">RUN&nbsp;<span style="font-family: 宋体;">等。</span></span></span></span></span></span></span></span>
> 
> **FROM**
> 
> 格式为&nbsp;<span style="font-family: Times New Roman;">FROM&nbsp;&lt;image&gt;<span style="font-family: 宋体;">或<span style="font-family: Times New Roman;">FROM&nbsp;&lt;image&gt;:&lt;tag&gt;<span style="font-family: 宋体;">。</span></span></span></span>
> 
> 第一条指令必须为&nbsp;<span style="font-family: Times New Roman;">FROM&nbsp;<span style="font-family: 宋体;">指令。并且，如果在同一个<span style="font-family: Times New Roman;">Dockerfile<span style="font-family: 宋体;">中创建多个镜像时，可以使用多个&nbsp;<span style="font-family: Times New Roman;">FROM&nbsp;<span style="font-family: 宋体;">指令（每个镜像一次）。</span></span></span></span></span></span>
> 
> **MAINTAINER**
> 
> 格式为&nbsp;<span style="font-family: Times New Roman;">MAINTAINER&nbsp;&lt;name&gt;<span style="font-family: 宋体;">，指定维护者信息。</span></span>
> 
> **RUN**
> 
> 格式为&nbsp;<span style="font-family: Times New Roman;">RUN&nbsp;&lt;command&gt;&nbsp;<span style="font-family: 宋体;">或&nbsp;<span style="font-family: Times New Roman;">RUN&nbsp;["executable",&nbsp;"param1",&nbsp;"param2"]<span style="font-family: 宋体;">。</span></span></span></span>
> 
> 前者将在&nbsp;<span style="font-family: Times New Roman;">shell&nbsp;<span style="font-family: 宋体;">终端中运行命令，即&nbsp;<span style="font-family: Times New Roman;">/bin/sh&nbsp;-c<span style="font-family: 宋体;">；后者则使用&nbsp;<span style="font-family: Times New Roman;">exec&nbsp;<span style="font-family: 宋体;">执行。指定使用其它终端可以通过第二种方式实现，例如&nbsp;<span style="font-family: Times New Roman;">RUN&nbsp;["/bin/bash",&nbsp;"-c",&nbsp;"echo&nbsp;hello"]<span style="font-family: 宋体;">。</span></span></span></span></span></span></span></span>
> 
> 每条&nbsp;<span style="font-family: Times New Roman;">RUN&nbsp;<span style="font-family: 宋体;">指令将在当前镜像基础上执行指定命令，并提交为新的镜像。当命令较长时可以使用&nbsp;<span style="font-family: Times New Roman;">\&nbsp;<span style="font-family: 宋体;">来换行。</span></span></span></span>
> 
> **CMD**
> 
> 支持三种格式
> 
> &nbsp;&nbsp;&nbsp;&nbsp;CMD&nbsp;["executable","param1","param2"]&nbsp;<span style="font-family: 宋体;">使用&nbsp;<span style="font-family: Times New Roman;">exec&nbsp;<span style="font-family: 宋体;">执行，推荐方式；</span></span></span>
> 
> &nbsp;&nbsp;&nbsp;&nbsp;CMD&nbsp;command&nbsp;param1&nbsp;param2&nbsp;<span style="font-family: 宋体;">在&nbsp;<span style="font-family: Times New Roman;">/bin/sh&nbsp;<span style="font-family: 宋体;">中执行，提供给需要交互的应用；</span></span></span>
> 
> &nbsp;&nbsp;&nbsp;&nbsp;CMD&nbsp;["param1","param2"]&nbsp;<span style="font-family: 宋体;">提供给&nbsp;<span style="font-family: Times New Roman;">ENTRYPOINT&nbsp;<span style="font-family: 宋体;">的默认参数；</span></span></span>
> 
> 指定启动容器时执行的命令，每个&nbsp;<span style="font-family: Times New Roman;">Dockerfile&nbsp;<span style="font-family: 宋体;">只能有一条&nbsp;<span style="font-family: Times New Roman;">CMD&nbsp;<span style="font-family: 宋体;">命令。如果指定了多条命令，只有最后一条会被执行。</span></span></span></span>
> 
> 如果用户启动容器时候指定了运行的命令，则会覆盖掉&nbsp;<span style="font-family: Times New Roman;">CMD&nbsp;<span style="font-family: 宋体;">指定的命令。</span></span>
> 
> **EXPOSE**
> 
> 格式为&nbsp;<span style="font-family: Times New Roman;">EXPOSE&nbsp;&lt;port&gt;&nbsp;[&lt;port&gt;...]<span style="font-family: 宋体;">。</span></span>
> 
> 告诉&nbsp;<span style="font-family: Times New Roman;">Docker&nbsp;<span style="font-family: 宋体;">服务端容器暴露的端口号，供互联系统使用。在启动容器时需要通过&nbsp;<span style="font-family: Times New Roman;">-P<span style="font-family: 宋体;">，<span style="font-family: Times New Roman;">Docker&nbsp;<span style="font-family: 宋体;">主机会自动分配一个端口转发到指定的端口。</span></span></span></span></span></span>
> 
> **ENV**
> 
> 格式为&nbsp;<span style="font-family: Times New Roman;">ENV&nbsp;&lt;key&gt;&nbsp;&lt;value&gt;<span style="font-family: 宋体;">。&nbsp;指定一个环境变量，会被后续&nbsp;<span style="font-family: Times New Roman;">RUN&nbsp;<span style="font-family: 宋体;">指令使用，并在容器运行时保持。</span></span></span></span>
> 
> 例如
> 
> ENV&nbsp;PG_MAJOR&nbsp;9.3
> 
> ENV&nbsp;PG_VERSION&nbsp;9.3.4
> 
> RUN&nbsp;curl&nbsp;-SL&nbsp;http://example.com/postgres-$PG_VERSION.tar.xz&nbsp;|&nbsp;tar&nbsp;-xJC&nbsp;/usr/src/postgress&nbsp;&amp;&amp;&nbsp;…
> 
> ENV&nbsp;PATH&nbsp;/usr/local/postgres-$PG_MAJOR/bin:$PATH
> 
> **ADD**
> 
> 格式为&nbsp;<span style="font-family: Times New Roman;">ADD&nbsp;&lt;src&gt;&nbsp;&lt;dest&gt;<span style="font-family: 宋体;">。</span></span>
> 
> 该命令将复制指定的&nbsp;<span style="font-family: Times New Roman;">&lt;src&gt;&nbsp;<span style="font-family: 宋体;">到容器中的&nbsp;<span style="font-family: Times New Roman;">&lt;dest&gt;<span style="font-family: 宋体;">。&nbsp;其中&nbsp;<span style="font-family: Times New Roman;">&lt;src&gt;&nbsp;<span style="font-family: 宋体;">可以是<span style="font-family: Times New Roman;">Dockerfile<span style="font-family: 宋体;">所在目录的一个相对路径；也可以是一个&nbsp;<span style="font-family: Times New Roman;">URL<span style="font-family: 宋体;">；还可以是一个&nbsp;<span style="font-family: Times New Roman;">tar&nbsp;<span style="font-family: 宋体;">文件（自动解压为目录）。</span></span></span></span></span></span></span></span></span></span></span></span>
> 
> **COPY**
> 
> 格式为&nbsp;<span style="font-family: Times New Roman;">COPY&nbsp;&lt;src&gt;&nbsp;&lt;dest&gt;<span style="font-family: 宋体;">。</span></span>
> 
> 复制本地主机的&nbsp;<span style="font-family: Times New Roman;">&lt;src&gt;<span style="font-family: 宋体;">（为&nbsp;<span style="font-family: Times New Roman;">Dockerfile&nbsp;<span style="font-family: 宋体;">所在目录的相对路径）到容器中的&nbsp;<span style="font-family: Times New Roman;">&lt;dest&gt;<span style="font-family: 宋体;">。</span></span></span></span></span></span>
> 
> 当使用本地目录为源目录时，推荐使用&nbsp;<span style="font-family: Times New Roman;">COPY<span style="font-family: 宋体;">。</span></span>
> 
> **ENTRYPOINT**
> 
> 两种格式：
> 
> &nbsp;&nbsp;&nbsp;&nbsp;ENTRYPOINT&nbsp;["executable",&nbsp;"param1",&nbsp;"param2"]
> 
> &nbsp;&nbsp;&nbsp;&nbsp;ENTRYPOINT&nbsp;command&nbsp;param1&nbsp;param2<span style="font-family: 宋体;">（<span style="font-family: Times New Roman;">shell<span style="font-family: 宋体;">中执行）。</span></span></span>
> 
> 配置容器启动后执行的命令，并且不可被&nbsp;<span style="font-family: Times New Roman;">docker&nbsp;run&nbsp;<span style="font-family: 宋体;">提供的参数覆盖。</span></span>
> 
> 每个&nbsp;<span style="font-family: Times New Roman;">Dockerfile&nbsp;<span style="font-family: 宋体;">中只能有一个&nbsp;<span style="font-family: Times New Roman;">ENTRYPOINT<span style="font-family: 宋体;">，当指定多个时，只有最后一个起效。</span></span></span></span>
> 
> **VOLUME**
> 
> 格式为&nbsp;<span style="font-family: Times New Roman;">VOLUME&nbsp;["/data"]<span style="font-family: 宋体;">。</span></span>
> 
> 创建一个可以从本地主机或其他容器挂载的挂载点，一般用来存放数据库和需要保持的数据等。
> 
> **USER**
> 
> 格式为&nbsp;<span style="font-family: Times New Roman;">USER&nbsp;daemon<span style="font-family: 宋体;">。</span></span>
> 
> 指定运行容器时的用户名或&nbsp;<span style="font-family: Times New Roman;">UID<span style="font-family: 宋体;">，后续的&nbsp;<span style="font-family: Times New Roman;">RUN&nbsp;<span style="font-family: 宋体;">也会使用指定用户。</span></span></span></span>
> 
> 当服务不需要管理员权限时，可以通过该命令指定运行用户。并且可以在之前创建所需要的用户，例如：<span style="font-family: Times New Roman;">RUN&nbsp;groupadd&nbsp;-r&nbsp;postgres&nbsp;&amp;&amp;&nbsp;useradd&nbsp;-r&nbsp;-g&nbsp;postgres&nbsp;postgres<span style="font-family: 宋体;">。要临时获取管理员权限可以使用&nbsp;<span style="font-family: Times New Roman;">gosu<span style="font-family: 宋体;">，而不推荐&nbsp;<span style="font-family: Times New Roman;">sudo<span style="font-family: 宋体;">。</span></span></span></span></span></span>
> 
> **WORKDIR**
> 
> 格式为&nbsp;<span style="font-family: Times New Roman;">WORKDIR&nbsp;/path/to/workdir<span style="font-family: 宋体;">。</span></span>
> 
> 为后续的&nbsp;<span style="font-family: Times New Roman;">RUN<span style="font-family: 宋体;">、<span style="font-family: Times New Roman;">CMD<span style="font-family: 宋体;">、<span style="font-family: Times New Roman;">ENTRYPOINT&nbsp;<span style="font-family: 宋体;">指令配置工作目录。</span></span></span></span></span></span>
> 
> 可以使用多个&nbsp;<span style="font-family: Times New Roman;">WORKDIR&nbsp;<span style="font-family: 宋体;">指令，后续命令如果参数是相对路径，则会基于之前命令指定的路径。例如</span></span>
> 
> WORKDIR&nbsp;/a
> 
> WORKDIR&nbsp;b
> 
> WORKDIR&nbsp;c
> 
> RUN&nbsp;pwd
> 
> 则最终路径为&nbsp;<span style="font-family: Times New Roman;">/a/b/c<span style="font-family: 宋体;">。</span></span>
> 
> **ONBUILD**
> 
> 格式为&nbsp;<span style="font-family: Times New Roman;">ONBUILD&nbsp;[INSTRUCTION]<span style="font-family: 宋体;">。</span></span>
> 
> 配置当所创建的镜像作为其它新创建镜像的基础镜像时，所执行的操作指令。
> 
> 例如，<span style="font-family: Times New Roman;">Dockerfile&nbsp;<span style="font-family: 宋体;">使用如下的内容创建了镜像&nbsp;<span style="font-family: Times New Roman;">image-A<span style="font-family: 宋体;">。</span></span></span></span>
> 
> [...]
> 
> ONBUILD&nbsp;ADD&nbsp;.&nbsp;/app/src
> 
> ONBUILD&nbsp;RUN&nbsp;/usr/local/bin/python-build&nbsp;--dir&nbsp;/app/src
> 
> [...]
> 
> 如果基于&nbsp;<span style="font-family: Times New Roman;">image-A&nbsp;<span style="font-family: 宋体;">创建新的镜像时，新的<span style="font-family: Times New Roman;">Dockerfile<span style="font-family: 宋体;">中使用&nbsp;<span style="font-family: Times New Roman;">FROM&nbsp;image-A<span style="font-family: 宋体;">指定基础镜像时，会自动执行&nbsp;<span style="font-family: Times New Roman;">ONBUILD&nbsp;<span style="font-family: 宋体;">指令内容，等价于在后面添加了两条指令。</span></span></span></span></span></span></span></span>
> 
> FROM&nbsp;image-A&nbsp;#Automatically&nbsp;run&nbsp;the&nbsp;followingADD&nbsp;.&nbsp;/app/srcRUN&nbsp;/usr/local/bin/python-build&nbsp;--dir&nbsp;/app/src
> 
> 使用&nbsp;<span style="font-family: Times New Roman;">ONBUILD&nbsp;<span style="font-family: 宋体;">指令的镜像，推荐在标签中注明，例如&nbsp;<span style="font-family: Times New Roman;">ruby:1.9-onbuild<span style="font-family: 宋体;">。</span></span></span></span>

# 3.创建镜像

编写完Dockerfile文件后，通过运行<span style="font-family: Times New Roman;">docker&nbsp;build</span><span style="font-family: 宋体;">命令</span>来创建自定义的镜像。<span style="font-family: Times New Roman;">Docker&nbsp;build</span><span style="font-family: 宋体;">命令格式如下：</span>

> <span style="font-family: 宋体;">docker&nbsp;build&nbsp;[options]&nbsp;&lt;path&gt;</span>
> 
> <span style="font-family: 宋体;">该命令将读取指定路径下（包括子目录）的&nbsp;<span style="font-family: Times New Roman;">Dockerfile<span style="font-family: 宋体;">，并将该路径下所有内容发送给&nbsp;<span style="font-family: Times New Roman;">Docker&nbsp;<span style="font-family: 宋体;">服务端，由服务端来创建镜像。因此一般建议放置&nbsp;<span style="font-family: Times New Roman;">Dockerfile&nbsp;<span style="font-family: 宋体;">的目录为空目录。也可以通过&nbsp;<span style="font-family: Times New Roman;">.dockerignore&nbsp;<span style="font-family: 宋体;">文件（每一行添加一条匹配模式）来让&nbsp;<span style="font-family: Times New Roman;">Docker&nbsp;<span style="font-family: 宋体;">忽略路径下的目录和文件。</span></span></span></span></span></span></span></span></span></span></span>

例如下面使用<span style="font-family: Times New Roman;">Dockerfile</span><span style="font-family: 宋体;">样例来创建了镜像</span><span style="font-family: Times New Roman;">test:0.0.1</span><span style="font-family: 宋体;">，其中</span><span style="font-family: Times New Roman;">-t</span><span style="font-family: 宋体;">选项用来指定镜像的</span><span style="font-family: Times New Roman;">tag</span><span style="font-family: 宋体;">。Dockerfile文件内容如下：</span>

<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div>
<pre>FROM ubuntu:14.04
MAINTAINER lienhua34@xxx.com

RUN mkdir /opt/leh
RUN touch /opt/leh/test

CMD echo "Hello lienhua34"</pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div></div>

下面运行docker build命令生成镜像test:0.0.1，

<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div>
<pre><span style="color: #008000;">lienhua34@test$</span> <span style="color: #0000ff;">sudo</span> docker build -t test:<span style="color: #800080;">0.0</span>.<span style="color: #800080;">1</span><span style="color: #000000;"> .
Sending build context to Docker daemon </span><span style="color: #800080;">3.072</span><span style="color: #000000;"> kB
Step </span><span style="color: #800080;">1</span> : FROM ubuntu:<span style="color: #800080;">14.04</span>
 ---&gt;<span style="color: #000000;"> a5a467fddcb8
Step </span><span style="color: #800080;">2</span> : MAINTAINER lienhua34@<span style="color: #800080;">163</span><span style="color: #000000;">.com
 </span>---&gt; Running <span style="color: #0000ff;">in</span><span style="color: #000000;"> ce9e7b02f075
 </span>---&gt;<span style="color: #000000;"> 332259a92e74
Removing intermediate container ce9e7b02f075
Step </span><span style="color: #800080;">3</span> : RUN <span style="color: #0000ff;">mkdir</span> /opt/<span style="color: #000000;">leh
 </span>---&gt; Running <span style="color: #0000ff;">in</span><span style="color: #000000;"> e93f0a98040f
 </span>---&gt;<span style="color: #000000;"> 097e177cf37f
Removing intermediate container e93f0a98040f
Step </span><span style="color: #800080;">4</span> : RUN <span style="color: #0000ff;">touch</span> /opt/leh/<span style="color: #000000;">test
 </span>---&gt; Running <span style="color: #0000ff;">in</span><span style="color: #000000;"> f1531d3dea1a
 </span>---&gt;<span style="color: #000000;"> 0f68852f8356
Removing intermediate container f1531d3dea1a
Step </span><span style="color: #800080;">5</span> : CMD <span style="color: #0000ff;">echo</span> <span style="color: #800000;">"</span><span style="color: #800000;">Hello lienhua34</span><span style="color: #800000;">"</span>
 ---&gt; Running <span style="color: #0000ff;">in</span><span style="color: #000000;"> cf3c5ce2af46
 </span>---&gt;<span style="color: #000000;"> 811ce27ce692
Removing intermediate container cf3c5ce2af46
Successfully built 811ce27ce692</span></pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div></div>

然后启动该镜像的容器来查看结果，

<div class="cnblogs_code">
<pre><span style="color: #008000;">lienhua34@test$</span> <span style="color: #0000ff;">sudo</span><span style="color: #000000;"> docker images
REPOSITORY                   TAG                 IMAGE ID            CREATED             VIRTUAL SIZE
test                         </span><span style="color: #800080;">0.0</span>.<span style="color: #800080;">1</span>               811ce27ce692        <span style="color: #800080;">32</span> seconds ago      <span style="color: #800080;">187.9</span><span style="color: #000000;"> MB
<span style="color: #008000;">lienhua34@test$ </span></span><span style="color: #0000ff;">sudo</span> docker run -ti test:<span style="color: #800080;">0.0</span>.<span style="color: #800080;">1</span><span style="color: #000000;">
Hello lienhua34</span></pre>
</div>

Dockerfile<span style="font-family: 宋体;">文件的每条指令生成镜像的一层（</span><span style="color: #ff0000;">注：一个镜像不能超过</span><span style="font-family: Times New Roman; color: #ff0000;">127</span><span style="font-family: 宋体; color: #ff0000;">层</span>）。<span style="font-family: Times New Roman;">Dockerfile</span><span style="font-family: 宋体;">中的指令被一条条地执行。每一步都创建一个新的容器，在容器中执行指令并提交修改。当所有指令执行完毕后，返回最终的镜像</span><span style="font-family: Times New Roman;">id</span><span style="font-family: 宋体;">。</span>

# 4.Dockerfile文件中的CMD和ENTRYPOINT指令差异对比

CMD<span style="font-family: 宋体;">指令和</span><span style="font-family: Times New Roman;">ENTRYPOINT</span><span style="font-family: 宋体;">指令的作用都是为镜像指定容器启动后的命令，那么它们两者之间有什么各自的优点呢？</span>

为了更好地对比<span style="font-family: Times New Roman;">CMD</span><span style="font-family: 宋体;">指令和</span><span style="font-family: Times New Roman;">ENTRYPOINT</span><span style="font-family: 宋体;">指令的差异，我们这里再列一下这两个指令的说明，</span>

> **CMD**
> 
> 支持三种格式
> 
> &nbsp;&nbsp;&nbsp;&nbsp;CMD&nbsp;["executable","param1","param2"]&nbsp;<span style="font-family: 宋体;">使用&nbsp;<span style="font-family: Times New Roman;">exec&nbsp;<span style="font-family: 宋体;">执行，推荐方式；</span></span></span>
> 
> &nbsp;&nbsp;&nbsp;&nbsp;CMD&nbsp;command&nbsp;param1&nbsp;param2&nbsp;<span style="font-family: 宋体;">在&nbsp;<span style="font-family: Times New Roman;">/bin/sh&nbsp;<span style="font-family: 宋体;">中执行，提供给需要交互的应用；</span></span></span>
> 
> &nbsp;&nbsp;&nbsp;&nbsp;CMD&nbsp;["param1","param2"]&nbsp;<span style="font-family: 宋体;">提供给&nbsp;<span style="font-family: Times New Roman;">ENTRYPOINT&nbsp;<span style="font-family: 宋体;">的默认参数；</span></span></span>
> 
> 指定启动容器时执行的命令，每个&nbsp;<span style="font-family: Times New Roman;">Dockerfile&nbsp;<span style="font-family: 宋体;">只能有一条&nbsp;<span style="font-family: Times New Roman;">CMD&nbsp;<span style="font-family: 宋体;">命令。如果指定了多条命令，只有最后一条会被执行。</span></span></span></span>
> 
> 如果用户启动容器时候指定了运行的命令，则会覆盖掉&nbsp;<span style="font-family: Times New Roman;">CMD&nbsp;<span style="font-family: 宋体;">指定的命令。</span></span>
> 
> **ENTRYPOINT**
> 
> 两种格式：
> 
> &nbsp;&nbsp;&nbsp;&nbsp;ENTRYPOINT&nbsp;["executable",&nbsp;"param1",&nbsp;"param2"]
> 
> &nbsp;&nbsp;&nbsp;&nbsp;ENTRYPOINT&nbsp;command&nbsp;param1&nbsp;param2<span style="font-family: 宋体;">（<span style="font-family: Times New Roman;">shell<span style="font-family: 宋体;">中执行）。</span></span></span>
> 
> 配置容器启动后执行的命令，并且不可被&nbsp;<span style="font-family: Times New Roman;">docker&nbsp;run&nbsp;<span style="font-family: 宋体;">提供的参数覆盖。</span></span>
> 
> 每个&nbsp;<span style="font-family: Times New Roman;">Dockerfile&nbsp;<span style="font-family: 宋体;">中只能有一个&nbsp;<span style="font-family: Times New Roman;">ENTRYPOINT<span style="font-family: 宋体;">，当指定多个时，只有最后一个起效。</span></span></span></span>

从上面的说明，我们可以看到有两个共同点：

1.  **<span style="font-family: 宋体;">都可以指定</span><span style="font-family: Times New Roman;">shell</span><span style="font-family: 宋体;">或</span><span style="font-family: Times New Roman;">exec</span><span style="font-family: 宋体;">函数调用的方式执行命令；</span>**
2.  **<span style="font-family: 宋体;">当存在多个</span><span style="font-family: Times New Roman;">CMD</span><span style="font-family: 宋体;">指令或</span><span style="font-family: Times New Roman;">ENTRYPOINT</span><span style="font-family: 宋体;">指令时，只有最后一个生效；</span>**

而它们有如下差异：

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **差异<span style="font-family: Times New Roman;">1</span><span style="font-family: 宋体;">：</span><span style="font-family: Times New Roman;">CMD</span><span style="font-family: 宋体;">指令指定的容器启动时命令可以被</span><span style="font-family: Times New Roman;">docker&nbsp;run</span><span style="font-family: 宋体;">指定的命令覆盖，而</span><span style="font-family: Times New Roman;">ENTRYPOINT</span><span style="font-family: 宋体;">指令指定的命令不能被覆盖，而是将</span><span style="font-family: Times New Roman;">docker&nbsp;run</span><span style="font-family: 宋体;">指定的参数当做</span><span style="font-family: Times New Roman;">ENTRYPOINT</span><span style="font-family: 宋体;">指定命令的参数。</span>**

**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 差异<span style="font-family: Times New Roman;">2</span><span style="font-family: 宋体;">：</span><span style="font-family: Times New Roman;">CMD</span><span style="font-family: 宋体;">指令可以为</span><span style="font-family: Times New Roman;">ENTRYPOINT</span><span style="font-family: 宋体;">指令设置默认参数，</span>而且可以被<span style="font-family: Times New Roman;">docker&nbsp;run</span><span style="font-family: 宋体;">指定的参数覆盖；</span>**

下面分别对上面两个差异点进行详细说明，

## 4.1&nbsp;差异1

> CMD<span style="font-family: 宋体;">指令指定的容器启动时命令可以被<span style="font-family: Times New Roman;">docker&nbsp;run<span style="font-family: 宋体;">指定的命令覆盖；而<span style="font-family: Times New Roman;">ENTRYPOINT<span style="font-family: 宋体;">指令指定的命令不能被覆盖，而是将<span style="font-family: Times New Roman;">docker&nbsp;run<span style="font-family: 宋体;">指定的参数当做<span style="font-family: Times New Roman;">ENTRYPOINT<span style="font-family: 宋体;">指定命令的参数。</span></span></span></span></span></span></span></span></span>

下面有个命名为<span style="font-family: Times New Roman;">startup</span><span style="font-family: 宋体;">的可执行</span><span style="font-family: Times New Roman;">shell</span><span style="font-family: 宋体;">脚本，其功能就是输出命令行参数而已。内容如下所示，</span>

<div class="cnblogs_code">
<pre>#!/bin/<span style="color: #000000;">bash

</span><span style="color: #0000ff;">echo</span> "in startup, args: $@"</pre>
</div>

### 通过<span style="font-family: Times New Roman;">CMD</span><span style="font-family: 宋体;">指定容器启动时命令：</span>

现在我们新建一个<span style="font-family: Times New Roman;">Dockerfile</span><span style="font-family: 宋体;">文件，其将</span><span style="font-family: Times New Roman;">startup</span><span style="font-family: 宋体;">脚本拷贝到容器的</span><span style="font-family: Times New Roman;">/opt</span><span style="font-family: 宋体;">目录下，并通过</span><span style="font-family: Times New Roman;">CMD</span><span style="font-family: 宋体;">指令指定容器启动时运行该</span><span style="font-family: Times New Roman;">startup</span><span style="font-family: 宋体;">脚本。其内容如下，</span>

<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div>
<pre>FROM ubuntu:14.04
MAINTAINER lienhua34@xxx.com

ADD startup /opt
RUN chmod a+x /opt/startup

CMD ["/opt/startup"]</pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div></div>

然后我们通过运行<span style="font-family: Times New Roman;">docker&nbsp;build</span><span style="font-family: 宋体;">命令生成</span><span style="font-family: Times New Roman;">test:latest</span><span style="font-family: 宋体;">镜像，</span>

<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div>
<pre><span style="color: #008000;">lienhua34@test$</span> <span style="color: #0000ff;">sudo</span> docker build -<span style="color: #000000;">t test .
Sending build context to Docker daemon </span><span style="color: #800080;">4.096</span><span style="color: #000000;"> kB
Step </span><span style="color: #800080;">1</span> : FROM ubuntu:<span style="color: #800080;">14.04</span>
 ---&gt;<span style="color: #000000;"> a5a467fddcb8
Step </span><span style="color: #800080;">2</span> : MAINTAINER lienhua34@<span style="color: #800080;">163</span><span style="color: #000000;">.com
 </span>---&gt;<span style="color: #000000;"> Using cache
 </span>---&gt;<span style="color: #000000;"> 332259a92e74
Step </span><span style="color: #800080;">3</span> : ADD startup /<span style="color: #000000;">opt
 </span>---&gt;<span style="color: #000000;"> 3c26b6a8ef1b
Removing intermediate container 87022b0f30c5
Step </span><span style="color: #800080;">4</span> : RUN <span style="color: #0000ff;">chmod</span> a+x /opt/<span style="color: #000000;">startup
 </span>---&gt; Running <span style="color: #0000ff;">in</span><span style="color: #000000;"> 4518ba223345
 </span>---&gt;<span style="color: #000000;"> 04d9b53d6148
Removing intermediate container 4518ba223345
Step </span><span style="color: #800080;">5</span> : CMD /opt/<span style="color: #000000;">startup
 </span>---&gt; Running <span style="color: #0000ff;">in</span><span style="color: #000000;"> 64a07c2f5e64
 </span>---&gt;<span style="color: #000000;"> 18a2d5066346
Removing intermediate container 64a07c2f5e64
Successfully built 18a2d5066346</span></pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div></div>

然后使用<span style="font-family: Times New Roman;">docker&nbsp;run</span><span style="font-family: 宋体;">启动两个</span><span style="font-family: Times New Roman;">test:latest</span><span style="font-family: 宋体;">镜像的容器，第一个</span><span style="font-family: Times New Roman;">docker&nbsp;run</span><span style="font-family: 宋体;">命令没有指定容器启动时命令，第二个</span><span style="font-family: Times New Roman;">docker&nbsp;run</span><span style="font-family: 宋体;">命令指定了容器启动时的命令为</span><span style="font-family: Times New Roman;">“</span>/bin/bash&nbsp;-c&nbsp;'echo&nbsp;Hello'”<span style="font-family: 宋体;">，</span>

<div class="cnblogs_code">
<pre><span style="color: #008000;">lienhua34@test$</span> <span style="color: #0000ff;">sudo</span> docker run -ti --<span style="color: #0000ff;">rm</span>=<span style="color: #0000ff;">true</span><span style="color: #000000;"> test
</span><span style="color: #0000ff;">in</span><span style="color: #000000;"> startup, args: 
<span style="color: #008000;">lienhua34@test$</span> </span><span style="color: #0000ff;">sudo</span> docker run -ti --<span style="color: #0000ff;">rm</span>=<span style="color: #0000ff;">true</span> test /bin/bash -c <span style="color: #800000;">'</span><span style="color: #800000;">echo Hello</span><span style="color: #800000;">'</span><span style="color: #000000;">
Hello</span></pre>
</div>

从上面运行结果可以看到，docker&nbsp;run<span style="font-family: 宋体;">命令启动容器时指定的运行命令覆盖了</span><span style="font-family: Times New Roman;">Dockerfile</span><span style="font-family: 宋体;">文件中</span><span style="font-family: Times New Roman;">CMD</span><span style="font-family: 宋体;">指令指定的命令</span>。

### 通过<span style="font-family: Times New Roman;">ENTRYPOINT</span>指定容器启动时命令：

将上面的<span style="font-family: Times New Roman;">Dockerfile</span><span style="font-family: 宋体;">中的</span><span style="font-family: Times New Roman;">CMD</span><span style="font-family: 宋体;">替换成</span><span style="font-family: Times New Roman;">ENTRYPOINT</span><span style="font-family: 宋体;">，内容如下所示，</span>

<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div>
<pre>FROM ubuntu:14.04
MAINTAINER lienhua34@xxx.com

ADD startup /opt
RUN chmod a+x /opt/startup

ENTRYPOINT [“/opt/startup”]</pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div></div>

同样，通过运行<span style="font-family: Times New Roman;">docker&nbsp;build</span><span style="font-family: 宋体;">生成</span><span style="font-family: Times New Roman;">test:latest</span><span style="font-family: 宋体;">镜像，</span>

<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div>
<pre><span style="color: #008000;">lienhua34@test$</span> <span style="color: #0000ff;">sudo</span> docker build -<span style="color: #000000;">t test .
Sending build context to Docker daemon </span><span style="color: #800080;">4.096</span><span style="color: #000000;"> kB
Step </span><span style="color: #800080;">1</span> : FROM ubuntu:<span style="color: #800080;">14.04</span>
 ---&gt;<span style="color: #000000;"> a5a467fddcb8
Step </span><span style="color: #800080;">2</span> : MAINTAINER lienhua34@<span style="color: #800080;">163</span><span style="color: #000000;">.com
 </span>---&gt;<span style="color: #000000;"> Using cache
 </span>---&gt;<span style="color: #000000;"> 332259a92e74
Step </span><span style="color: #800080;">3</span> : ADD startup /<span style="color: #000000;">opt
 </span>---&gt;<span style="color: #000000;"> Using cache
 </span>---&gt;<span style="color: #000000;"> 3c26b6a8ef1b
Step </span><span style="color: #800080;">4</span> : RUN <span style="color: #0000ff;">chmod</span> a+x /opt/<span style="color: #000000;">startup
 </span>---&gt;<span style="color: #000000;"> Using cache
 </span>---&gt;<span style="color: #000000;"> 04d9b53d6148
Step </span><span style="color: #800080;">5</span> : ENTRYPOINT /opt/<span style="color: #000000;">startup
 </span>---&gt; Running <span style="color: #0000ff;">in</span><span style="color: #000000;"> cdec60940ad7
 </span>---&gt;<span style="color: #000000;"> 78f8aca2edc2
Removing intermediate container cdec60940ad7
Successfully built 78f8aca2edc2</span></pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div></div>

然后使用<span style="font-family: Times New Roman;">docker&nbsp;run</span><span style="font-family: 宋体;">启动两个</span><span style="font-family: Times New Roman;">test:latest</span><span style="font-family: 宋体;">镜像的容器，第一个</span><span style="font-family: Times New Roman;">docker&nbsp;run</span><span style="font-family: 宋体;">命令没有指定容器启动时命令，第二个</span><span style="font-family: Times New Roman;">docker&nbsp;run</span><span style="font-family: 宋体;">命令指定了容器启动时的命令为</span><span style="font-family: Times New Roman;">“</span>/bin/bash&nbsp;-c&nbsp;'echo&nbsp;Hello'”<span style="font-family: 宋体;">，</span>

<div class="cnblogs_code">
<pre><span style="color: #008000;">lienhua34@test$</span> <span style="color: #0000ff;">sudo</span> docker run -ti --<span style="color: #0000ff;">rm</span>=<span style="color: #0000ff;">true</span><span style="color: #000000;"> test
</span><span style="color: #0000ff;">in</span><span style="color: #000000;"> startup, args: 
lienhua34@test$ </span><span style="color: #0000ff;">sudo</span> docker run -ti --<span style="color: #0000ff;">rm</span>=<span style="color: #0000ff;">true</span> test /bin/bash -c <span style="color: #800000;">'</span><span style="color: #800000;">echo Hello</span><span style="color: #800000;">'</span>
<span style="color: #0000ff;">in</span> startup, args: /bin/bash -c <span style="color: #0000ff;">echo</span> Hello</pre>
</div>

通过上面的运行结果可以看出，<span style="font-family: Times New Roman;">docker&nbsp;run</span><span style="font-family: 宋体;">命令指定的容器运行命令不能覆盖</span><span style="font-family: Times New Roman;">Dockerfile</span><span style="font-family: 宋体;">文件中</span><span style="font-family: Times New Roman;">ENTRYPOINT</span><span style="font-family: 宋体;">指令指定的命令，反而被当做参数传递给</span><span style="font-family: Times New Roman;">ENTRYPOINT</span><span style="font-family: 宋体;">指令指定的命令。</span>

## 4.2&nbsp;差异2

> CMD<span style="font-family: 宋体;">指令可以为<span style="font-family: Times New Roman;">ENTRYPOINT<span style="font-family: 宋体;">指令设置默认参数，而且可以被<span style="font-family: Times New Roman;">docker&nbsp;run<span style="font-family: 宋体;">指定的参数覆盖；</span></span></span></span></span>

同样使用上面的<span style="font-family: Times New Roman;">startup</span><span style="font-family: 宋体;">脚本。编写</span><span style="font-family: Times New Roman;">Dockerfile</span><span style="font-family: 宋体;">，内容如下所示，</span>

<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div>
<pre>FROM ubuntu:<span style="color: #800080;">14.04</span><span style="color: #000000;">
MAINTAINER lienhua34@xxx.com

ADD startup </span>/<span style="color: #000000;">opt
RUN </span><span style="color: #0000ff;">chmod</span> a+x /opt/<span style="color: #000000;">startup

ENTRYPOINT [</span><span style="color: #800000;">"</span><span style="color: #800000;">/opt/startup</span><span style="color: #800000;">"</span>, <span style="color: #800000;">"</span><span style="color: #800000;">arg1</span><span style="color: #800000;">"</span><span style="color: #000000;">]
CMD [</span><span style="color: #800000;">"</span><span style="color: #800000;">arg2</span><span style="color: #800000;">"</span>]</pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div></div>

运行<span style="font-family: Times New Roman;">docker&nbsp;build</span><span style="font-family: 宋体;">命令生成</span><span style="font-family: Times New Roman;">test:latest</span><span style="font-family: 宋体;">镜像，</span>

<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div>
<pre><span style="color: #008000;">lienhua34@test$</span> <span style="color: #0000ff;">sudo</span> docker build -<span style="color: #000000;">t test .
Sending build context to Docker daemon </span><span style="color: #800080;">4.096</span><span style="color: #000000;"> kB
Step </span><span style="color: #800080;">1</span> : FROM ubuntu:<span style="color: #800080;">14.04</span>
 ---&gt;<span style="color: #000000;"> a5a467fddcb8
Step </span><span style="color: #800080;">2</span> : MAINTAINER lienhua34@<span style="color: #800080;">163</span><span style="color: #000000;">.com
 </span>---&gt;<span style="color: #000000;"> Using cache
 </span>---&gt;<span style="color: #000000;"> 332259a92e74
Step </span><span style="color: #800080;">3</span> : ADD startup /<span style="color: #000000;">opt
 </span>---&gt;<span style="color: #000000;"> Using cache
 </span>---&gt;<span style="color: #000000;"> 3c26b6a8ef1b
Step </span><span style="color: #800080;">4</span> : RUN <span style="color: #0000ff;">chmod</span> a+x /opt/<span style="color: #000000;">startup
 </span>---&gt;<span style="color: #000000;"> Using cache
 </span>---&gt;<span style="color: #000000;"> 04d9b53d6148
Step </span><span style="color: #800080;">5</span> : ENTRYPOINT /opt/<span style="color: #000000;">startup arg1
 </span>---&gt; Running <span style="color: #0000ff;">in</span><span style="color: #000000;"> 54947233dc3d
 </span>---&gt;<span style="color: #000000;"> 15a485253b4e
Removing intermediate container 54947233dc3d
Step </span><span style="color: #800080;">6</span><span style="color: #000000;"> : CMD arg2
 </span>---&gt; Running <span style="color: #0000ff;">in</span><span style="color: #000000;"> 18c43d2d90fd
 </span>---&gt;<span style="color: #000000;"> 4684ba457cc2
Removing intermediate container 18c43d2d90fd
Successfully built 4684ba457cc2</span></pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div></div>

下面运行<span style="font-family: Times New Roman;">docker&nbsp;run</span><span style="font-family: 宋体;">启动两个</span><span style="font-family: Times New Roman;">test:latest</span><span style="font-family: 宋体;">镜像的容器，第一条</span><span style="font-family: Times New Roman;">docker&nbsp;run</span><span style="font-family: 宋体;">命令没有指定参数，第二条</span><span style="font-family: Times New Roman;">docker&nbsp;run</span><span style="font-family: 宋体;">命令指定了参数</span><span style="font-family: Times New Roman;">arg3</span><span style="font-family: 宋体;">，其运行结果如下，</span>

<div class="cnblogs_code">
<pre>lienhua34@test$ <span style="color: #0000ff;">sudo</span> docker run -ti --<span style="color: #0000ff;">rm</span>=<span style="color: #0000ff;">true</span><span style="color: #000000;"> test
</span><span style="color: #0000ff;">in</span><span style="color: #000000;"> startup, args: arg1 arg2
lienhua34@test$ </span><span style="color: #0000ff;">sudo</span> docker run -ti --<span style="color: #0000ff;">rm</span>=<span style="color: #0000ff;">true</span><span style="color: #000000;"> test arg3
</span><span style="color: #0000ff;">in</span> startup, args: arg1 arg3</pre>
</div>

从上面第一个容器的运行结果可以看出<span style="font-family: Times New Roman;">CMD</span><span style="font-family: 宋体;">指令为</span><span style="font-family: Times New Roman;">ENTRYPOINT</span><span style="font-family: 宋体;">指令设置了默认参数；从第二个容器的运行结果看出，</span><span style="font-family: Times New Roman;">docker&nbsp;run</span><span style="font-family: 宋体;">命令指定的参数覆盖了</span><span style="font-family: Times New Roman;">CMD</span><span style="font-family: 宋体;">指令指定的参数。</span>

## 4.3注意点

<span style="color: #ff0000;">**CMD<span style="font-family: 宋体;">指令为</span><span style="font-family: Times New Roman;">ENTRYPOINT</span><span style="font-family: 宋体;">指令提供默认参数是基于镜像层次结构生效的，而不是基于是否在同个</span><span style="font-family: Times New Roman;">Dockerfile</span><span style="font-family: 宋体;">文件中。意思就是说，如果</span><span style="font-family: Times New Roman;">Dockerfile</span><span style="font-family: 宋体;">指定基础镜像中是</span><span style="font-family: Times New Roman;">ENTRYPOINT</span><span style="font-family: 宋体;">指定的启动命令，则该</span><span style="font-family: Times New Roman;">Dockerfile</span><span style="font-family: 宋体;">中的</span><span style="font-family: Times New Roman;">CMD</span><span style="font-family: 宋体;">依然是为基础镜像中的</span><span style="font-family: Times New Roman;">ENTRYPOINT</span><span style="font-family: 宋体;">设置默认参数。</span>**</span>

例如，我们有如下一个<span style="font-family: Times New Roman;">Dockerfile</span><span style="font-family: 宋体;">文件，</span>

<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div>
<pre>FROM ubuntu:14.04
MAINTAINER lienhua34@xxx.com

ADD startup /opt
RUN chmod a+x /opt/startup

ENTRYPOINT ["/opt/startup", "arg1"]</pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div></div>

通过运行<span style="font-family: Times New Roman;">docker&nbsp;build</span><span style="font-family: 宋体;">命令生成</span><span style="font-family: Times New Roman;">test:0.0.1</span><span style="font-family: 宋体;">镜像，然后创建该镜像的一个容器，查看运行结果，</span>

<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div>
<pre><span style="color: #008000;">lienhua34@test$</span> <span style="color: #0000ff;">sudo</span> docker build -t test:<span style="color: #800080;">0.0</span>.<span style="color: #800080;">1</span><span style="color: #000000;"> .
Sending build context to Docker daemon </span><span style="color: #800080;">6.144</span><span style="color: #000000;"> kB
Step </span><span style="color: #800080;">1</span> : FROM ubuntu:<span style="color: #800080;">14.04</span>
 ---&gt;<span style="color: #000000;"> a5a467fddcb8
Step </span><span style="color: #800080;">2</span> : MAINTAINER lienhua34@<span style="color: #800080;">163</span><span style="color: #000000;">.com
 </span>---&gt; Running <span style="color: #0000ff;">in</span><span style="color: #000000;"> 57a96522061a
 </span>---&gt;<span style="color: #000000;"> c3bbf1bd8068
Removing intermediate container 57a96522061a
Step </span><span style="color: #800080;">3</span> : ADD startup /<span style="color: #000000;">opt
 </span>---&gt;<span style="color: #000000;"> f9884fbc7607
Removing intermediate container 591a82b2f382
Step </span><span style="color: #800080;">4</span> : RUN <span style="color: #0000ff;">chmod</span> a+x /opt/<span style="color: #000000;">startup
 </span>---&gt; Running <span style="color: #0000ff;">in</span><span style="color: #000000;"> 7a19f10b5513
 </span>---&gt;<span style="color: #000000;"> 16c03869a764
Removing intermediate container 7a19f10b5513
Step </span><span style="color: #800080;">5</span> : ENTRYPOINT /opt/<span style="color: #000000;">startup arg1
 </span>---&gt; Running <span style="color: #0000ff;">in</span><span style="color: #000000;"> b581c32b25c3
 </span>---&gt;<span style="color: #000000;"> c6b1365afe03
Removing intermediate container b581c32b25c3
Successfully built c6b1365afe03
<span style="color: #008000;">lienhua34@test$ </span></span><span style="color: #0000ff;">sudo</span> docker run -ti --<span style="color: #0000ff;">rm</span>=<span style="color: #0000ff;">true</span> test:<span style="color: #800080;">0.0</span>.<span style="color: #800080;">1</span>
<span style="color: #0000ff;">in</span> startup, args: arg1</pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div></div>

下面新建一个<span style="font-family: Times New Roman;">Dockerfile</span><span style="font-family: 宋体;">文件，基础镜像是刚生成的</span><span style="font-family: Times New Roman;">test:0.0.1</span><span style="font-family: 宋体;">，通过</span><span style="font-family: Times New Roman;">CMD</span><span style="font-family: 宋体;">指定要通过</span><span style="font-family: Times New Roman;">echo</span><span style="font-family: 宋体;">打印字符串</span><span style="font-family: Times New Roman;">“in&nbsp;test:0.0.2”</span><span style="font-family: 宋体;">。文件内容如下所示，</span>

<div class="cnblogs_code">
<pre>FROM test:0.0.1
MAINTAINER lienhua34@xxx.com

CMD ["/bin/bash", "-c", "echo in test:0.0.2"]</pre>
</div>

运行<span style="font-family: Times New Roman;">docker&nbsp;build</span><span style="font-family: 宋体;">命令生成</span><span style="font-family: Times New Roman;">test:0.0.2</span><span style="font-family: 宋体;">镜像，然后通过运行</span><span style="font-family: Times New Roman;">docker&nbsp;run</span><span style="font-family: 宋体;">启动一个</span><span style="font-family: Times New Roman;">test:0.0.2</span><span style="font-family: 宋体;">镜像的容器来查看结果，</span>

<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div>
<pre><span style="color: #008000;">lienhua34@test$</span> <span style="color: #0000ff;">sudo</span> docker build -t test:<span style="color: #800080;">0.0</span>.<span style="color: #800080;">2</span><span style="color: #000000;"> .
Sending build context to Docker daemon </span><span style="color: #800080;">6.144</span><span style="color: #000000;"> kB
Step </span><span style="color: #800080;">1</span> : FROM test:<span style="color: #800080;">0.0</span>.<span style="color: #800080;">1</span>
 ---&gt;<span style="color: #000000;"> c6b1365afe03
Step </span><span style="color: #800080;">2</span> : MAINTAINER lienhua34@<span style="color: #800080;">163</span><span style="color: #000000;">.com
 </span>---&gt; Running <span style="color: #0000ff;">in</span><span style="color: #000000;"> deca95cf4c15
 </span>---&gt;<span style="color: #000000;"> 971b5a819b48
Removing intermediate container deca95cf4c15
Step </span><span style="color: #800080;">3</span> : CMD /bin/bash -c <span style="color: #0000ff;">echo</span> <span style="color: #0000ff;">in</span> test:<span style="color: #800080;">0.0</span>.<span style="color: #800080;">2</span>
 ---&gt; Running <span style="color: #0000ff;">in</span><span style="color: #000000;"> 4a31c4652e1e
 </span>---&gt;<span style="color: #000000;"> 0ca06ba31405
Removing intermediate container 4a31c4652e1e
Successfully built 0ca06ba31405
<span style="color: #008000;">lienhua34@test$ </span></span><span style="color: #0000ff;">sudo</span> docker run -ti --<span style="color: #0000ff;">rm</span>=<span style="color: #0000ff;">true</span> test:<span style="color: #800080;">0.0</span>.<span style="color: #800080;">2</span>
<span style="color: #0000ff;">in</span> startup, args: arg1 /bin/bash -c <span style="color: #0000ff;">echo</span> <span style="color: #0000ff;">in</span> test:<span style="color: #800080;">0.0</span>.<span style="color: #800080;">2</span></pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"></span></div></div>

从上面结果可以看到，镜像<span style="font-family: Times New Roman;">test:0.0.2</span><span style="font-family: 宋体;">启动的容器运行时并不是打印字符串</span><span style="font-family: Times New Roman;">”in&nbsp;test:0.0.2”</span><span style="font-family: 宋体;">，而是将</span><span style="font-family: Times New Roman;">CMD</span><span style="font-family: 宋体;">指令指定的命令当做基础镜像</span><span style="font-family: Times New Roman;">test:0.0.1</span><span style="font-family: 宋体;">中</span><span style="font-family: Times New Roman;">ENTRYPOINT</span><span style="font-family: 宋体;">指定的运行脚本</span><span style="font-family: Times New Roman;">startup</span><span style="font-family: 宋体;">的参数。</span>

(done)
</div>