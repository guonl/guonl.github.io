---
layout: post
title: SpringBoot-自定义处理静态资源
categories: SpringBoot
description: SpringBoot-自定义处理静态资源
keywords: SpringBoot,springboot
---

**静态资源路径**是指系统可以直接访问的路径，且路径下的所有文件均可被用户直接读取。

在Springboot中默认的静态资源路径有：classpath:/META-INF/resources/，classpath:/resources/，classpath:/static/，classpath:/public/，从这里可以看出这里的静态资源路径都是在classpath中


### 自定义目录

Spring Boot默认是使用resources下的静态资源进行映射。如果我们需要增加以 /myres/* 映射到 classpath:/myres/* 为例的代码处理为： 
实现类继承 WebMvcConfigurerAdapter 并重写方法 addResourceHandlers 

```java
package org.hsweb.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by 11 on 2017/4/17.
 */
@Configuration
public class WebAppConfigurer extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/myres/**").addResourceLocations("classpath:/myres/");
        super.addResourceHandlers(registry);
    }
}

```


访问myres 文件夹中的test.jpg 图片的地址为 http://localhost:8080/myres/test.jpg 
这样使用代码的方式自定义目录映射，并不影响Spring Boot的默认映射，可以同时使用。

如果我们将/myres/* 修改为 /* 与默认的相同时，则会覆盖系统的配置，可以多次使用 addResourceLocations 添加目录，优先级先添加的高于后添加的。

其中 addResourceLocations 的参数是动参，可以这样写 addResourceLocations(“classpath:/img1/”, “classpath:/img2/”, “classpath:/img3/”);

如果我们要指定一个绝对路径的文件夹（如 D:/data/api_files ），则只需要使用 addResourceLocations 指定即可。

// 可以直接使用addResourceLocations 指定磁盘绝对路径，同样可以配置多个位置，注意路径写法需要加上file:
```java
registry.addResourceHandler("/api_files/**").addResourceLocations("file:D:/data/api_files");
```

可参考：[https://github.com/longfeizheng/ueditor-test](https://github.com/longfeizheng/ueditor-test "springboot-ueditor")