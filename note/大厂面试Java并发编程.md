# 大厂面试Java并发编程

## 1. Object o = new Object()在内存当中占了多少个字节?

利用openjdk提供的工具`Java Object Layout`可以查看对象在内存中的布局，一段程序对应着一个栈，当程序执行`Object o = new Object()`时，会在JVM的堆空间内开辟一段空间用于存储对象o，对象o在内存中的布局大致为:

1. 对象头mark word
2. 类型指针class pointer
3. 实例数据instance data
4. 内存对齐padding

打开cmd，输入:`java -XX:+PrintCommandLineFlag -version`，默认情况下会出现如下的结果:

```java
C:\Users\31811>java -XX:+PrintCommandLineFlags -version
-XX:InitialHeapSize=131509888 -XX:MaxHeapSize=2104158208 -XX:+PrintCommandLineFlags -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:-UseLargePagesIndividualAllocation -XX:+UseParallelGC
java version "1.8.0_221"
Java(TM) SE Runtime Environment (build 1.8.0_221-b11)
Java HotSpot(TM) 64-Bit Server VM (build 25.221-b11, mixed mode)
```

Oops是指Ordinary Object Pointers(普通对象指针)，默认情况下，一个对象的对象头有8个字节，但是如果开启了`-XX:+UseCompressedOops`，对象指针只有4个字节(**为什么会这样？64位的操作系统，一个指针为8个字节，指针压缩了之后，只有4个字节**)

新创建的对象o的实例数据instance data没有，大小为0，因此1，2，3共占了12个字节，为了对齐为2的幂次方，padding的大小为4个字节，**因此新创建的对象一共有16个字节**

**但是假如不开启`-XX:+UseCompressedOops`，对象指针为8个字节，1,2,3共16个字节，不需要padding，因此仍然是16个字节**

## 2.无锁，偏向锁，轻量级锁，重量级锁的升级过程

锁升级的大致流程如下:

1. 对象刚刚创建的时候，处于无锁的状态
2. 对象第一次上锁的时候，使用的是偏向锁
3. 对象的锁有人争用的时候，使用的是轻量级锁(无锁，自旋锁)
4. 对象的锁正争用的程度非常的剧烈，升级为重量级锁

**上述锁升级的状态的过程全部都记录在对象的mark word当中，mark word一共有64位(8字节)**

<div align=center><img src="/assets/suo1.png"/></div>

### 2.1 无锁升级为偏向锁

一个对象刚刚被创建的时候处于无锁的状态，**并且新创建出来的对象处于可偏向匿名状态**， 如果有线程上锁，此时就会把这个对象中mark word中的线程ID改为自己的线程ID，**偏向锁不能重偏向或者批量偏向，批量撤销**

### 2.2 偏向锁升级为轻量级锁(无锁，自旋锁)

对象的mark word中包含了一个指针`*JavaThread`，用于指向当前锁的线程ID，如果新请求这个锁的线程ID和这个ID不同，判断锁发生了争用，此时偏向锁升级为轻量级锁，**撤销偏向锁，升级为轻量级锁**，线程在自己的线程栈生成LockRecord，用CAS操作将mark word设置为指向自己这个线程LR指针，**大家同时设置，设置成功者拿到了锁对象** 

### 2.3 轻量级锁升级为重量级锁

偏向锁又称自旋锁，**意味着没有获取锁对象的线程会不断的自旋，重新尝试获得锁** ，当自旋的线程数目超过CPU核数的一半的时候(jdk1.6之后加入了自适应自旋Adaptive Self Spinning，由JVM自己控制)，此时线程向操作系统申请资源linux mutex，CPU从3级-0级调用，线程挂起，进入等待队列，等待操作系统的调度，然后映射到用户空间

**然而在jdk11中，对象新创建的状态就是偏向锁，而jdk8默认是无锁**