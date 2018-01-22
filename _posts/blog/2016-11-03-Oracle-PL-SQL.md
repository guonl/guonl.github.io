---
layout: post
title: Oracle PL/SQL入门语法点
categories: Oracle
description: Oracle PL/SQL入门语法点
keywords: Oracle,oracle,pl/sql
---
```sql
PL_SQL：带有分支和循环,面向过程
匿名块：
declare(可选，声明各种变量和游标的地方)
begin(必要的，从此开始执行)
exception(抓取到异常后执行的)
end;
[sql] view plaincopy
set serveroutput on;(默认是关闭) 
--最简单的PL/SQL语句块 
begin 
dbms_output.put_line('HelloWorld!'); 
end; 
--最简单的语句块 
declare 
v_name varchar2(20); 
begin 
v_name := 'myname'; 
dbms_output.put_line(v_name); 
end; 
--语句块的组成 
declare 
v_name number := 0; 
begin 
v_name := 2/v_num; 
dbms_output.put_line(v_name); 
exception 
when others then 
dbms_output.put_line('error'); 
end; 
--变量声明
1. 变量名不能够使用保留字，如from，select等
2. 第一个字符必须是字母
3. 变量名最多包含30个字符
4. 不要与数据库表或者列同名
5. 每一行只能声明一个变量
--常用变量类型
1. binary_integer： 整数，主要用来计数而不是用来表示字段类型，效率高
2. number： 数字类型
3. char： 定长字符串
4. varchar2： 变长字符串
5. date： 日期
6. long： 长字符串，最长2GB
7. boolean： 布尔类型，可以取值为ture、false和null值，一定要给初值，无法打印
--变量声明

[sql] view plaincopy
declare 
v_temp number(1); 
v_count binary_integer := 0; 
v_sal number(7,2) := 4000.00; 
v_date date := sysdate; 
v_pi constant number(3,2) := 3.14; 
v_valid boolean := false; 
v_name varchar2(20) not null := 'MyName'; 
begin 
dbms_output.put_line('v_temp value:' || v_count); 
end; 
--变量声明，使用%type属性

[sql] view plaincopy
/*注释多行*/ --注释一行 
declare 
v_empno number(4); 
v_empno2 emp.empno%type;--用表内字段类型声明变量类型 
v_empno3 v_empno2%type;--用变量type属性声明变量类型 
begin 
dbms_output.put_line('Test'); 
end; 
--简单变量赋值

[sql] view plaincopy
declare 
v_name varchar2(20); 
v_sal number(7,2); 
v_sal2 number(7,2); 
v_valid boolean := false; 
v_date date; 
begin 
v_name := 'MyName'; 
v_sal := 23.77; 
v_sal2 := 23.77; 
v_valid := (v_sal = v_sal2);--判断是否相等用= 
v_date := to_date('1999-08-12 12:23:30','YYYY-MM-DD HH24:MI:SS'); 
end; 
--Table变量类型，表示一个数组

[sql] view plaincopy
declare 
type type_table_emp_empno is table of emp.empno%type index by binary_integer;--首先声明一个类型 
v_empnos type_table_emp_empno;--再用这个类型声明变量 
begin 
v_empnos(0) := 7369; 
v_empnos(2) := 7839; 
v_empnos(-1) := 9999; 
dbms_output.put_line(v_empnos(-1));---1访问不到，0,2可以访问到 
end; 
--Record类型，表示一个类

[sql] view plaincopy
declare 
type type_record_dept is record 
( 
deptno dept.deptno%type, 
dname dept.dname%tpye, 
loc dept.loc%type 
); 
v_temp type_record_dept; 
begin 
v_temp.deptno := 50; 
v_temp.dname := 'aaaa'; 
v_temp.loc := 'bj'; 
dbms_output.put_line(v_temp.deptno || ' ' || v_temp.dname); 
end; 
--使用%rowtype声明record变量
[sql] view plaincopy
declare 
v_temp dept%rowtype; 
begin 
v_temp.deptno := 50; 
v_temp.dname := 'aaaa'; 
v_temp.loc := 'bj'; 
dbms_output.put_line(v_temp.deptno || ' ' || v_temp.dname); 
end; 
--PL/SQL里执行select语句的话，配合into，必须返回并只能返回一条记录
--SQL语句的运用
[sql] view plaincopy
declare 
v_ename emp.ename%type; 
v_sal emp.sal%type; 
begin 
select ename,sal into v_ename,v_sal from emp where empno = 7369; 
dbms_output.put_line(ename || ' ' || v_sal); 
end; 

declare 
v_emp emp%rowtype; 
begin 
select * into v_emp from emp where empno = 7369; 
dbms_output.put_line(v_emp.ename); 
end; 

declare 
v_deptno dept.deptno%type := 50; 
v_dname dept.dname%type := 'aaaa'; 
v_loc dept.loc%type := 'bj'; 
begin 
insert into dept2 values(v_deptno,v_dname,v_loc); 
commit;--别忘了!!! 
end; 

select * from dept2; 

declare 
v_deptno emp2.deptno%type := 10; 
v_count number; 
begin 
--update emp2 set sal = sal/2 where deptno = v_deptno; 
--select deptno into v_deptno from emp2 where empno = 7369; 
select count(*) into v_count from emp2; 
dbms_output.put_line(sql%rowcount || '条记录被影响');//sql,关键字，代表刚刚执行的sql程序 
end; 
--PL/SQL执行DDL语句,需要加execute immediate
[sql] view plaincopy
begin 
execute immediate 'create table T(nnn varchar2(20) default ''aaaa'')'; 
end; 
--if语句
[sql] view plaincopy
--取出7369的薪水，如果<1200，则输出'low'，如果<2000则输出'middle'，否则输出'high' 

declare 
v_sal emp.sal%type; 
begin 
select sal into v_sal from emp where empno = 7369; 
if (v_sal<1200) then 
dbms_output.put_line('low'); 
elsif(v_sal<2000) then --elsif没e 
dbms_output.put_line('midddle'); 
else 
dbms_output.put_line('high'); 
end if; 
end; 
--循环
[sql] view plaincopy
declare 
i binary_integer := 1; 
begin 
loop 
dbms_output.put_line(i); 
i := i + 1; 
exit when (i >= 11); 
end loop; 
end; 

declare 
j binary_integer := 1; 
begin 
while j < 11 loop 
dbms_output.put_line(j); 
j := j + 1; 
end loop; 
end; 

begin 
for k in 1..10 loop 
dbms_output.put_line(k); 
end loop; 

for k in reverse 1..10 loop 
dbms_output.put_line(k); 
end loop; 
end; 
--错误处理
[sql] view plaincopy
declare 
v_temp number(4); 
begin 
select empno into v_temp from emp where deptno = 10; 
exception 
when too_many_rows then 
dbms_output.putline('太多记录了'); 
when others then 
dbms_output.put_line('error'); 
end; 

declare 
v_temp number(4); 
begin 
select empno into v_temp from emp where empno = 2222; 
exception 
when no_data_found then 
dbms_output.put_line('没有数据'); 
end;

--sql程序跨数据库平台较好，PL/SQL效率高
--DBA，错误日志
[sql] view plaincopy
create table errorlog 
( 
id number primary key, 
errcode number, 
errmsg varchar2(1024), 
errdate date 
); 

create sequence seq_errorlog_id start with 1 increment by 1; 

declare 
v_deptno dept.deptno%type := 10; 
v_errcode number; 
v_errmsg varchar2(1024); 
begin 
delete from dept where deptno = v_deptno; 
commit; 
exception 
when others then 
rollback; 
v_errcode := SQLCODE; 
v_errmsg := SQLERRM; 
insert into errorlog values (seq_errorlog_id.nextval,v_errcode,v_errmsg,sysdate); 
commit; 
end; 
--游标cursor，结果集上的指针
[sql] view plaincopy
declare 
cursor c is 
select * from emp;--只是声明游标，还未取数据 
v_emp c%rowtype 
begin 
open c; 
fetch c into v_emp; 
dbms_output.put_line(v_emp.ename); 
close c; 
end; 

declare 
cursor c is 
select * from emp; 
v_emp c%rowtype; 
beigin 
open c; 
loop 
fetch c into v_emp; 
exit when (c%notfound); 
dbms_output.put_line(v_emp.ename); 
end loop; 
close c; 
end; 
--带参数的游标
[sql] view plaincopy
declare 
cursor c(v_deptno emp.deptno%type,v_job emp.job%type) 
is 
select ename,sal from emp where deptno = v_deptno and job = v_job; 
begin 
for v_temp in c(30,'clerk') loop 
dbms_output.put_line(v_temp.ename); 
end loop; 
end; 
--可更新的游标
[sql] view plaincopy
declare 
cursor c 
is 
select * from emp2 for update; 
begin 
for v_temp in c loop 
if(v_temp.sal < 2000) then 
update emp2 set sal = sal*2 where current of c; 
else if (v_temp.sal = 5000) then 
delete from emp2 where current of c; 
end if; 
end loop; 
commit; 
end; 
--创建存储过程，带有名字的PL/SQL块
[sql] view plaincopy
grant create procedure to shijin; 
create or replace procedure p 
is 
cursor c is 
select * from emp2 for update; 
begin 
for v_emp in c loop 
if(v_emp.deptno = 10) then 
update emp2 set sal = sal + 10 where current of c; 
else if (v_emp.deptno = 20) then 
update emp2 set sal = sal + 20 where current of c; 
else 
update emp2 set sal = sal + 50 where current of c; 
end if; 
end loop; 
commit; 
end loop; 

begin 
p; 
end; 
或者 
exec p; 
--带参数的存储过程，默认是in
[sql] view plaincopy
create or replace procedure p 
(v_a in number,v_b number,v_ret out number,v_temp in out number) 
is 

begin 
if (v_a > v_b) then 
v_ret := v_a; 
else 
v_ret := v_b; 
end if; 
v_temp := v_temp + 1; 
end; 

declare 
v_a number := 3; 
v_b number := 4; 
v_ret number; 
v_temp number := 5; 
begin 
p(v_a,v_b,v_ret,v_temp); 
dbms_output.put_line(v_ret); 
dbms_output.put_line(v_temp); 
end; 
--函数
[sql] view plaincopy
create or replace function sal_tax 
(v_sal number) 
return number 
is 
begin 
if(v_sal < 2000) then 
return 0.10; 
elsif(v_sal < 2750) then 
return 0.15; 
else 
return 0.20; 
end if; 
end; 

select sal_tax(sal) from emp; 
--触发器！！！！牢牢掌握概念
[sql] view plaincopy
create table emp2_log 
( 
uname varchar2(20), 
action varchar2(10), 
atime date 
); 
grant create triger to shijin; 
create or replace trigger trig 
after insert or delete or update on emp2 --此处可以加上for each row 
begin 
if inserting then 
insert into emp2_log values (USER,'insert',sysdate); 
elsif updating then 
insert into emp2_log values (USER,'update',sysdate); 
elsif deleting then 
insert into emp2_log values (USER,'delete',sysdate); 
end if; 
end; 

update emp2 set sal = sal*2 where deptno = 30; 

drop trigger trig; 

create or replace trigger trig 
after update on dept 
for each row 
begin 
update emp set deptno = :NEW.deptno where deptno = :OLD.deptno; 
end; 
--树状结构数据的存储与显示
[sql] view plaincopy
drop table article; 

create table article 
( 
id number primary key, 
cont varchar2(4000), 
pid number, 
isleaf number(1),--0代表非叶子节点，1代表叶子节点 
alevel number(2) 
) 

create or replace procedure p (v_pid article.pid%type,v_level binary_integer) is 
cursor c is select * from article where pid = v_pid; 
v_preStr varchar2(1024) := ''; 
begin 
for i in 1..v_level loop 
v_preStr := v_preStr || '****'; 
end loop; 
for v_article in c loop 
dbms_output.put_line(v_preStr || v_article.cont); 
if(v_article.isleaf = 0) then 
p(v_articel.id,v_level + 1); 
end if; 
end loop; 
end; 
insert into article values (1,'蚂蚁大战大象',0,0,0); 
insert into article values (2,'大象被打趴下',1,0,1); 
insert into article values (3,'蚂蚁也不好过',2,1,2); 
insert into article values (4,'瞎 说',2,0,2); 
insert into article values (5,'没 有 瞎 说',4,1,3); 
insert into article values (6,'怎 么 可 能',1,0,1); 
insert into article values (7,'怎么没有可能',6,1,2); 
insert into article values (8,'可能性很大的',6,1,2); 
insert into article values (9,'大象进医院了',2,0,2); 
insert into article values (10,'护士是蚂蚁',9,1,3); 

set serverout on; 
begin 
p(0,0); 
end; 
show error; 
--展示emp的树状结构
```

参考链接：

- [http://www.cnblogs.com/xlhblogs/p/3507185.html](http://www.cnblogs.com/xlhblogs/p/3507185.html)