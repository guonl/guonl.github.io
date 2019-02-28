---
layout: post
title: ReentrantLock实现原理
categories: Java
description: Java 锁 ReentrantLock
keywords: Java,ReentrantLock,锁
---

### 1 synchronized和lock
#### 1.1 synchronized的局限性
    
synchronized是java内置的关键字，它提供了一种独占的加锁方式。synchronized的获取和释放锁由JVM实现，用户不需要显示的释放锁，非常方便。然而synchronized也有一定的局限性，例如：

1. 当线程尝试获取锁的时候，如果获取不到锁会一直阻塞。
2. 如果获取锁的线程进入休眠或者阻塞，除非当前线程异常，否则其他线程尝试获取锁必须一直等待。

JDK1.5之后发布，加入了Doug Lea实现的concurrent包。包内提供了Lock类，用来提供更多扩展的加锁功能。Lock弥补了synchronized的局限，提供了更加细粒度的加锁功能。

#### 1.2 Lock简介

```java
void lock();
void lockInterruptibly() throws InterruptedException;
boolean tryLock();
boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
void unlock();
Condition newCondition();

```
其中最常用的就是lock和unlock操作了。因为使用lock时，需要手动的释放锁，所以需要使用try..catch来包住业务代码，并且在finally中释放锁。典型使用如下


```java
private Lock lock = new ReentrantLock();
 
public void test(){
    lock.lock();
    try{
        doSomeThing();
    }catch (Exception e){
        // ignored
    }finally {
        lock.unlock();
    }
}
```
### 2 AQS
 AbstractQueuedSynchronizer简称AQS，是一个用于构建锁和同步容器的框架。事实上concurrent包内许多类都是基于AQS构建，例如ReentrantLock，Semaphore，CountDownLatch，ReentrantReadWriteLock，FutureTask等。AQS解决了在实现同步容器时设计的大量细节问题。

AQS使用一个FIFO的队列表示排队等待锁的线程，队列头节点称作“哨兵节点”或者“哑节点”，它不与任何线程关联。其他的节点与等待线程关联，每个节点维护一个等待状态waitStatus。如图

![image](http://images2015.cnblogs.com/blog/912007/201702/912007-20170215221250988-637607444.png)

AQS中还有一个表示状态的字段state，例如ReentrantLocky用它表示线程重入锁的次数，Semaphore用它表示剩余的许可数量，FutureTask用它表示任务的状态。对state变量值的更新都采用CAS操作保证更新操作的原子性。

AbstractQueuedSynchronizer继承了AbstractOwnableSynchronizer，这个类只有一个变量：exclusiveOwnerThread，表示当前占用该锁的线程，并且提供了相应的get，set方法。

理解AQS可以帮助我们更好的理解JCU包中的同步容器。

### 3 lock()与unlock()实现原理

#### 3.1 基础知识

-  ReentrantLock是Lock的默认实现之一。那么lock()和unlock()是怎么实现的呢？首先我们要弄清楚几个概念

- 可重入锁。可重入锁是指同一个线程可以多次获取同一把锁。ReentrantLock和synchronized都是可重入锁。

- 可中断锁。可中断锁是指线程尝试获取锁的过程中，是否可以响应中断。synchronized是不可中断锁，而ReentrantLock则提供了中断功能。

- 公平锁与非公平锁。公平锁是指多个线程同时尝试获取同一把锁时，获取锁的顺序按照线程达到的顺序，而非公平锁则允许线程“插队”。synchronized是非公平锁，而ReentrantLock的默认实现是非公平锁，但是也可以设置为公平锁。

- CAS操作(CompareAndSwap)。CAS操作简单的说就是比较并交换。CAS 操作包含三个操作数 —— 内存位置（V）、预期原值（A）和新值(B)。如果内存位置的值与预期原值相匹配，那么处理器会自动将该位置值更新为新值。否则，处理器不做任何操作。无论哪种情况，它都会在 CAS 指令之前返回该位置的值。CAS 有效地说明了“我认为位置 V 应该包含值 A；如果包含该值，则将 B 放到这个位置；否则，不要更改该位置，只告诉我这个位置现在的值即可。” Java并发包(java.util.concurrent)中大量使用了CAS操作,涉及到并发的地方都调用了sun.misc.Unsafe类方法进行CAS操作。

#### 3.2 内部结构
ReentrantLock提供了两个构造器，分别是

```java
public ReentrantLock() {
    sync = new NonfairSync();
}
 
public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
}
```
默认构造器初始化为NonfairSync对象，即非公平锁，而带参数的构造器可以指定使用公平锁和非公平锁。由lock()和unlock的源码可以看到，它们只是分别调用了sync对象的lock()和release(1)方法。

Sync是ReentrantLock的内部类，它的结构如下

![image](http://images2015.cnblogs.com/blog/912007/201702/912007-20170215221446535-285062236.png)

 可以看到Sync扩展了AbstractQueuedSynchronizer。
 
####  3.3 NonfairSync
    我们从源代码出发，分析非公平锁获取锁和释放锁的过程。 
    
##### 3.3.1 lock() 
    lock()源码如下


```java
final void lock() {
    if (compareAndSetState(0, 1))
        setExclusiveOwnerThread(Thread.currentThread());
    else
        acquire(1);
}
```
首先用一个CAS操作，判断state是否是0（表示当前锁未被占用），如果是0则把它置为1，并且设置当前线程为该锁的独占线程，表示获取锁成功。当多个线程同时尝试占用同一个锁时，CAS操作只能保证一个线程操作成功，剩下的只能乖乖的去排队啦。

“非公平”即体现在这里，如果占用锁的线程刚释放锁，state置为0，而排队等待锁的线程还未唤醒时，新来的线程就直接抢占了该锁，那么就“插队”了。

若当前有三个线程去竞争锁，假设线程A的CAS操作成功了，拿到了锁开开心心的返回了，那么线程B和C则设置state失败，走到了else里面。我们往下看acquire。

acquire(arg)


```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```
代码非常简洁，但是背后的逻辑却非常复杂，可见Doug Lea大神的编程功力。

 1. 第一步。尝试去获取锁。如果尝试获取锁成功，方法直接返回。

tryAcquire(arg)


```java
final boolean nonfairTryAcquire(int acquires) {
    //获取当前线程
    final Thread current = Thread.currentThread();
    //获取state变量值
    int c = getState();
    if (c == 0) { //没有线程占用锁
        if (compareAndSetState(0, acquires)) {
            //占用锁成功,设置独占线程为当前线程
            setExclusiveOwnerThread(current);
            return true;
        }
    } else if (current == getExclusiveOwnerThread()) { //当前线程已经占用该锁
        int nextc = c + acquires;
        if (nextc < 0) // overflow
            throw new Error("Maximum lock count exceeded");
        // 更新state值为新的重入次数
        setState(nextc);
        return true;
    }
    //获取锁失败
    return false;
}
```
非公平锁tryAcquire的流程是：检查state字段，若为0，表示锁未被占用，那么尝试占用，若不为0，检查当前锁是否被自己占用，若被自己占用，则更新state字段，表示重入锁的次数。如果以上两点都没有成功，则获取锁失败，返回false。

2. 第二步，入队。由于上文中提到线程A已经占用了锁，所以B和C执行tryAcquire失败，并且入等待队列。如果线程A拿着锁死死不放，那么B和C就会被挂起。

先看下入队的过程。

先看addWaiter(Node.EXCLUSIVE)


```java
/**
 * 将新节点和当前线程关联并且入队列
 * @param mode 独占/共享
 * @return 新节点
 */
private Node addWaiter(Node mode) {
    //初始化节点,设置关联线程和模式(独占 or 共享)
    Node node = new Node(Thread.currentThread(), mode);
    // 获取尾节点引用
    Node pred = tail;
    // 尾节点不为空,说明队列已经初始化过
    if (pred != null) {
        node.prev = pred;
        // 设置新节点为尾节点
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    // 尾节点为空,说明队列还未初始化,需要初始化head节点并入队新节点
    enq(node);
    return node;
}
```
B、C线程同时尝试入队列，由于队列尚未初始化，tail==null，故至少会有一个线程会走到enq(node)。我们假设同时走到了enq(node)里。


```java
/**
 * 初始化队列并且入队新节点
 */
private Node enq(final Node node) {
    //开始自旋
    for (;;) {
        Node t = tail;
        if (t == null) { // Must initialize
            // 如果tail为空,则新建一个head节点,并且tail指向head
            if (compareAndSetHead(new Node()))
                tail = head;
        } else {
            node.prev = t;
            // tail不为空,将新节点入队
            if (compareAndSetTail(t, node)) {
                t.next = node;
                return t;
            }
        }
    }
}
```

这里体现了经典的自旋+CAS组合来实现非阻塞的原子操作。由于compareAndSetHead的实现使用了unsafe类提供的CAS操作，所以只有一个线程会创建head节点成功。假设线程B成功，之后B、C开始第二轮循环，此时tail已经不为空，两个线程都走到else里面。假设B线程compareAndSetTail成功，那么B就可以返回了，C由于入队失败还需要第三轮循环。最终所有线程都可以成功入队。

当B、C入等待队列后，此时AQS队列如下：
![image](http://images2015.cnblogs.com/blog/912007/201702/912007-20170215221938238-133039818.png)

3. 第三步，挂起。B和C相继执行acquireQueued(final Node node, int arg)。这个方法让已经入队的线程尝试获取锁，若失败则会被挂起。


```java
/**
 * 已经入队的线程尝试获取锁
 */
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true; //标记是否成功获取锁
    try {
        boolean interrupted = false; //标记线程是否被中断过
        for (;;) {
            final Node p = node.predecessor(); //获取前驱节点
            //如果前驱是head,即该结点已成老二，那么便有资格去尝试获取锁
            if (p == head && tryAcquire(arg)) {
                setHead(node); // 获取成功,将当前节点设置为head节点
                p.next = null; // 原head节点出队,在某个时间点被GC回收
                failed = false; //获取成功
                return interrupted; //返回是否被中断过
            }
            // 判断获取失败后是否可以挂起,若可以则挂起
            if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                // 线程若被中断,设置interrupted为true
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}

```

code里的注释已经很清晰的说明了acquireQueued的执行流程。假设B和C在竞争锁的过程中A一直持有锁，那么它们的tryAcquire操作都会失败，因此会走到第2个if语句中。我们再看下shouldParkAfterFailedAcquire和parkAndCheckInterrupt都做了哪些事吧。


```java
/**
 * 判断当前线程获取锁失败之后是否需要挂起.
 */
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    //前驱节点的状态
    int ws = pred.waitStatus;
    if (ws == Node.SIGNAL)
        // 前驱节点状态为signal,返回true
        return true;
    // 前驱节点状态为CANCELLED
    if (ws > 0) {
        // 从队尾向前寻找第一个状态不为CANCELLED的节点
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
        // 将前驱节点的状态设置为SIGNAL
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}
  
/**
 * 挂起当前线程,返回线程中断状态并重置
 */
private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this);
    return Thread.interrupted();
}
```

线程入队后能够挂起的前提是，它的前驱节点的状态为SIGNAL，它的含义是“Hi，前面的兄弟，如果你获取锁并且出队后，记得把我唤醒！”。所以shouldParkAfterFailedAcquire会先判断当前节点的前驱是否状态符合要求，若符合则返回true，然后调用parkAndCheckInterrupt，将自己挂起。如果不符合，再看前驱节点是否>0(CANCELLED)，若是那么向前遍历直到找到第一个符合要求的前驱，若不是则将前驱节点的状态设置为SIGNAL。

整个流程中，如果前驱结点的状态不是SIGNAL，那么自己就不能安心挂起，需要去找个安心的挂起点，同时可以再尝试下看有没有机会去尝试竞争锁。

最终队列可能会如下图所示

![image](http://images2015.cnblogs.com/blog/912007/201702/912007-20170215222115269-71076930.png)

线程B和C都已经入队，并且都被挂起。当线程A释放锁的时候，就会去唤醒线程B去获取锁啦。

##### 3.3.2 unlock()
unlock相对于lock就简单很多。源码如下

``` java
public void unlock() {
    sync.release(1);
}
  
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```

如果理解了加锁的过程，那么解锁看起来就容易多了。流程大致为先尝试释放锁，若释放成功，那么查看头结点的状态是否为SIGNAL，如果是则唤醒头结点的下个节点关联的线程，如果释放失败那么返回false表示解锁失败。这里我们也发现了，每次都只唤起头结点的下一个节点关联的线程。

   最后我们再看下tryRelease的执行过程


```java
/**
 * 释放当前线程占用的锁
 * @param releases
 * @return 是否释放成功
 */
protected final boolean tryRelease(int releases) {
    // 计算释放后state值
    int c = getState() - releases;
    // 如果不是当前线程占用锁,那么抛出异常
    if (Thread.currentThread() != getExclusiveOwnerThread())
        throw new IllegalMonitorStateException();
    boolean free = false;
    if (c == 0) {
        // 锁被重入次数为0,表示释放成功
        free = true;
        // 清空独占线程
        setExclusiveOwnerThread(null);
    }
    // 更新state值
    setState(c);
    return free;
}
```

这里入参为1。tryRelease的过程为：当前释放锁的线程若不持有锁，则抛出异常。若持有锁，计算释放后的state值是否为0，若为0表示锁已经被成功释放，并且则清空独占线程，最后更新state值，返回free。 

##### 3.3.3 小结
用一张流程图总结一下非公平锁的获取锁的过程。
    
![image](http://images2015.cnblogs.com/blog/912007/201702/912007-20170215222303613-1038407072.png)

#### 3.4 FairSync
    公平锁和非公平锁不同之处在于，公平锁在获取锁的时候，不会先去检查state状态，而是直接执行aqcuire(1)，这里不再赘述。

### 4 超时机制

在ReetrantLock的tryLock(long timeout, TimeUnit unit) 提供了超时获取锁的功能。它的语义是在指定的时间内如果获取到锁就返回true，获取不到则返回false。这种机制避免了线程无限期的等待锁释放。那么超时的功能是怎么实现的呢？我们还是用非公平锁为例来一探究竟。


```java
public boolean tryLock(long timeout, TimeUnit unit)
        throws InterruptedException {
    return sync.tryAcquireNanos(1, unit.toNanos(timeout));
}
```

 还是调用了内部类里面的方法。我们继续向前探究 


```
public final boolean tryAcquireNanos(int arg, long nanosTimeout)
        throws InterruptedException {
    if (Thread.interrupted())
        throw new InterruptedException();
    return tryAcquire(arg) ||
        doAcquireNanos(arg, nanosTimeout);
}
```

这里的语义是：如果线程被中断了，那么直接抛出InterruptedException。如果未中断，先尝试获取锁，获取成功就直接返回，获取失败则进入doAcquireNanos。tryAcquire我们已经看过，这里重点看一下doAcquireNanos做了什么。


```java
/**
 * 在有限的时间内去竞争锁
 * @return 是否获取成功
 */
private boolean doAcquireNanos(int arg, long nanosTimeout)
        throws InterruptedException {
    // 起始时间
    long lastTime = System.nanoTime();
    // 线程入队
    final Node node = addWaiter(Node.EXCLUSIVE);
    boolean failed = true;
    try {
        // 又是自旋!
        for (;;) {
            // 获取前驱节点
            final Node p = node.predecessor();
            // 如果前驱是头节点并且占用锁成功,则将当前节点变成头结点
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return true;
            }
            // 如果已经超时,返回false
            if (nanosTimeout <= 0)
                return false;
            // 超时时间未到,且需要挂起
            if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                // 阻塞当前线程直到超时时间到期
                LockSupport.parkNanos(this, nanosTimeout);
            long now = System.nanoTime();
            // 更新nanosTimeout
            nanosTimeout -= now - lastTime;
            lastTime = now;
            if (Thread.interrupted())
                //相应中断
                throw new InterruptedException();
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```
doAcquireNanos的流程简述为：线程先入等待队列，然后开始自旋，尝试获取锁，获取成功就返回，失败则在队列里找一个安全点把自己挂起直到超时时间过期。这里为什么还需要循环呢？因为当前线程节点的前驱状态可能不是SIGNAL，那么在当前这一轮循环中线程不会被挂起，然后更新超时时间，开始新一轮的尝试



参考原文：
- [https://blog.csdn.net/u011202334/article/details/73188404](https://blog.csdn.net/u011202334/article/details/73188404)

