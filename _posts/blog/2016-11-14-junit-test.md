---
layout: post
title: JUnit常用断言及注解
categories: Junit
description: JUnit常用断言及注解
keywords: Junit, 断言, 测试
---
断言是编写测试用例的核心实现方式，即期望值是多少，测试的结果是多少，以此来判断测试是否通过。

#### 断言核心方法
| 公式  | 描述  |
| ------------ | ------------ |
| assertArrayEquals(expecteds, actuals) | 查看两个数组是否相等。  |
| assertEquals(expected, actual)  | 查看两个对象是否相等。类似于字符串比较使用的equals()方法  |
| assertNotEquals(first, second)  | 查看两个对象是否不相等。  |
| assertNull(object)  | 查看对象是否为空。  |
| assertNotNull(object)  | 查看对象是否不为空。  |
| assertSame(expected, actual)  | 查看两个对象的引用是否相等。类似于使用“==”比较两个对象  |
| assertNotSame(unexpected, actual)  | 查看两个对象的引用是否不相等。类似于使用“!=”比较两个对象  |
| assertTrue(condition)  | 查看运行结果是否为true。  |
| assertFalse(condition)  | 查看运行结果是否为false。  |
| assertThat(actual, matcher)  | 查看实际值是否满足指定的条件  |
| fail()  | 让测试失败  |

#### 注解
| 注解  | 描述  |
| ------------ | ------------ |
| @Before  | 初始化方法  |
| @After  | 释放资源  |
| @Test  | 测试方法，在这里可以测试期望异常和超时时间  |
| @Ignore  | 忽略的测试方法 |
| @BeforeClass  | 针对所有测试，只执行一次，且必须为static void  |
| @AfterClass  | 针对所有测试，只执行一次，且必须为static void  |
| @RunWith  | 指定测试类使用某个运行器  |
| @Parameters  | 指定测试类的测试数据集合  |
| @Rule  | 允许灵活添加或重新定义测试类中的每个测试方法的行为  |
| @FixMethodOrder  | 指定测试方法的执行顺序  |

	一个测试类单元测试的执行顺序为：
	@BeforeClass –> @Before –> @Test –> @After –> @AfterClass
	每一个测试方法的调用顺序为：
	@Before –> @Test –> @After

参考链接

- [http://blog.csdn.net/jaune161/article/details/40025861](http://blog.csdn.net/jaune161/article/details/40025861)


