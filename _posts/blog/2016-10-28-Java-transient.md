---
layout: post
title: Java transient关键字解析
categories: Java
description: Java transient关键字解析
keywords: Java,java,transient
---
## 前言
最近在看hsweb-framework项目，看到了对象属性有用**transient**关键字修饰，工作两年的我一脸懵逼，特意查阅了一下这个修饰符的意思，并记录下来！

Account.java
	
	``` java
	package com.winwill.simple.test;
	import java.io.Serializable;
	/**
	 * @author qifuguang
	 * @date 16-6-23
	 */
	public class Account implements Serializable {
	    private String userId;
	    private String password;
	    public Account(String userId, String password) {
	        this.userId = userId;
	        this.password = password;
	    }
	    public String getPassword() {
	        return password;
	    }
	    public String getUserId() {
	        return userId;
	    }
	    public void setUserId(String userId) {
	        this.userId = userId;
	    }
	    public void setPassword(String password) {
	        this.password = password;
	    }
	}
	```


TransientTest.java

	``` java
	package com.winwill.simple.test;
	import java.io.FileInputStream;
	import java.io.FileOutputStream;
	import java.io.IOException;
	import java.io.ObjectInputStream;
	import java.io.ObjectOutputStream;
	/**
	 * @author qifuguang
	 * @date 16-6-23
	 */
	public class TransientTest {
	    private static final String ACCOUNT_SERIALIZE_FILE_PATH = "/home/winwill2012/account";
	    public static void main(String[] args) throws IOException, ClassNotFoundException {
	        Account account = new Account("winwill2012", "168");
	        System.out.println("原始数据：");
	        System.out.println("userId: " + account.getUserId());
	        System.out.println("password: " + account.getPassword());
	        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(ACCOUNT_SERIALIZE_FILE_PATH));
	        os.writeObject(account);
	        os.flush();
	        os.close();
	        ObjectInputStream is = new ObjectInputStream(new FileInputStream(ACCOUNT_SERIALIZE_FILE_PATH));
	        Account recoverAccount = (Account) is.readObject();
	        is.close();
	        System.out.println("从文件恢复：");
	        System.out.println("userId: " + recoverAccount.getUserId());
	        System.out.println("password: " + recoverAccount.getPassword());
	    }
	}
	```

上面代码的大概逻辑就是：



> 先创建一个Account对象（实现了Serializable接口），这个对象有两个属性：用户名和密码，然后将改对象序列化写到文件，最后再从文件反序列化回来，前后两次打印account的信息，做一个对比。

很显然，上面代码的执行结果如下：
> 原始数据：userId: winwill2012password: 168

	``` java
	原始数据：
	userId: winwill2012
	password: 168
	从文件恢复：
	userId: winwill2012
	password: 168
	
	Process finished with exit code 0
	```

然后我们再用**transient**关键字修饰一下password属性

	``` java
	package com.winwill.simple.test;
	import java.io.Serializable;
	/**
	 * @author qifuguang
	 * @date 16-6-23
	 */
	public class Account implements Serializable {
	    private String userId;
	    private transient String password;       // 就修改了这行代码
	    public Account(String userId, String password) {
	        this.userId = userId;
	        this.password = password;
	    }
	    public String getPassword() {
	        return password;
	    }
	    public String getUserId() {
	        return userId;
	    }
	    public void setUserId(String userId) {
	        this.userId = userId;
	    }
	    public void setPassword(String password) {
	        this.password = password;
	    }
	}
	```

再次执行代码得到的结果如下

	``` java
	原始数据：
	userId: winwill2012
	password: 168
	从文件恢复：
	userId: winwill2012
	password: null
	
	Process finished with exit code 0
	```

## 正文
通过上面的示例，大家也许已经知道**transient**的作用是什么了。没错，它的作用就是阻止它所修饰的属性被序列化。为什么会有这样的需求呢？比如：
> 我们要在网络上传输一些客户的资料，但是对于非常敏感的数据（比如薪资，各类密码之类的），我们担心在传输过程中这些敏感数据被窃取。

在上面这样的场景下，transient就能配上用场了， **transient修饰的属性不能被序列化，但是这是有前提条件的。**

我们知道在Java中要想让一个类能够实现序列化，可以通过如下两种方法：

- 实现Serializable接口，这种情况下，对象的序列化和反序列化都是java自己的机制自动实现的，不需要人为实现；
- 实现Externalizable接口，可以实现writeExternal()和readExternal()这两个方法，完成自己的序列化/反序列化操作；

**transient修饰的属性不能被序列化**这句话只在上面的第一种情况下成立，为什么呢？

因为 如果实现的是Externalizable接口，序列化和反序列化都是自己控制的，不受transient的约束。
## 结语
既然说到序列化，那就顺便提一下，如果一个变量是static的，那么无论是否有transient修饰词，都不能够别序列化，因为序列化是序列化一个对象，**static变量根本不属于任何对象**。


参考链接：

- [http://qifuguang.me/2016/06/24/Java-transient%E5%85%B3%E9%94%AE%E5%AD%97%E8%A7%A3%E6%9E%90/](http://qifuguang.me/2016/06/24/Java-transient%E5%85%B3%E9%94%AE%E5%AD%97%E8%A7%A3%E6%9E%90/ "Java transient关键字解析")
- [http://blog.csdn.net/lfsf802/article/details/43239663](http://blog.csdn.net/lfsf802/article/details/43239663)

