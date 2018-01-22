---
layout: post
title: Oracle-decode函数对null的处理
categories: Oracle
description: Oracle-decode函数对null的处理
keywords: oracle,Oracle,decode
---
今天查询sql需要这么一个判断，在一个SQL语句中，当值为空的时候，返回0，否则返回1。应该是一个挺简单的问题，我首先想到的是NVL和DECODE函数相结合，decode(nvl(column_name,0),column_name,1,0)，这样就完成了，照理说这个问题也就结束了，但是百度了一下发现用decode直接就可以实现
```sql
SELECT DECODE(sfr.zj_count,NULL,0,sfr.zj_count) FROM SL_FJ_RULE sfr;
```

参考链接：
- [http://blog.itpub.net/10018/viewspace-886867/](http://blog.itpub.net/10018/viewspace-886867/ "http://blog.itpub.net/10018/viewspace-886867/")