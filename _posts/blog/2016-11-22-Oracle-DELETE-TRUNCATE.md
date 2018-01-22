---
layout: post
title: Oracle DELETE和TRUNCATE 的区别
categories: Oracle
description: Oracle DELETE和TRUNCATE 的区别
keywords: oracle,Oracle
---
## 语句
```sql
delete from aa 
truncate table aa 
```
## 区别
1. delete from后面可以写条件，truncate不可以。
1. delete from记录是一条条删的，所删除的每行记录都会进日志，而truncate一次性删掉整个页，因此日至里面只记录页释放，简言之，delete from更新日志，truncate基本不，所用的事务日志空间较少。
1. delete from删空表后，会保留一个空的页，truncate在表中不会留有任何页。
1. 当使用行锁执行 DELETE 语句时，将锁定表中各行以便删除。truncate始终锁定表和页，而不是锁定各行。 
1. 如果有identity产生的自增id列，delete from后仍然从上次的数开始增加，即种子不变，而truncate后，种子会恢复初始。
1. truncate不会触发delete的触发器，因为truncate操作不记录各个行删除。

## 总结
1. truncate和 delete只删除数据不删除表的结构(定义)。 drop语句将删除表的结构被依赖的约束(constrain),触发器(trigger),索引(index); 依赖于该表的存储过程/函数将保留,但是变为invalid状态。
1. delete语句是dml,这个操作会放到rollback segement中,事务提交之后才生效;如果有相应的trigger,执行的时候将被触发。 truncate,drop是ddl, 操作立即生效,原数据不放到rollback segment中,不能回滚. 操作不触发trigger。
1. delete语句不影响表所占用的extent, 高水线(high watermark)保持原位置不动 。 显然drop语句将表所占用的空间全部释放 ，truncate 语句缺省情况下见空间释放到 minextents个 extent,除非使用reuse storage;   truncate会将高水线复位(回到最开始)。
1. 速度,一般来说: drop> truncate > delete。
1. 安全性:小心使用drop 和truncate,尤其没有备份的时候.否则哭都来不及。
1. 使用上,想删除部分数据行用delete,注意带上where子句. 回滚段要足够大.     想删除表,当然用drop。想保留表而将所有数据删除. 如果和事务无关,用truncate即可. 如果和事务有关,或者想触发trigger,还是用delete。如果是整理表内部的碎片,可以用truncate跟上reuse stroage,再重新导入/插入数据。
