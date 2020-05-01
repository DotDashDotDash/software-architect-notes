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

<div align=center><img src="/assets/suo1.jpg"/></div>

### 2.1 无锁升级为偏向锁

一个对象刚刚被创建的时候处于无锁的状态，**并且新创建出来的对象处于可偏向匿名状态**， 如果有线程上锁，此时就会把这个对象中mark word中的线程ID改为自己的线程ID，**偏向锁不能重偏向或者批量偏向，批量撤销**

### 2.2 偏向锁升级为轻量级锁(无锁，自旋锁)

对象的mark word中包含了一个指针`*JavaThread`，用于指向当前锁的线程ID，如果新请求这个锁的线程ID和这个ID不同，判断锁发生了争用，此时偏向锁升级为轻量级锁，**撤销偏向锁，升级为轻量级锁**，线程在自己的线程栈生成LockRecord，用CAS操作将mark word设置为指向自己这个线程LR指针，**大家同时设置，设置成功者拿到了锁对象** 

### 2.3 轻量级锁升级为重量级锁

偏向锁又称自旋锁，**意味着没有获取锁对象的线程会不断的自旋，重新尝试获得锁** ，当自旋的线程数目超过CPU核数的一半的时候(jdk1.6之后加入了自适应自旋Adaptive Self Spinning，由JVM自己控制)，此时线程向操作系统申请资源linux mutex，CPU从3级-0级调用，线程挂起，进入等待队列，等待操作系统的调度，然后映射到用户空间

**然而在jdk11中，对象新创建的状态就是偏向锁，而jdk8默认是无锁**

## 3. 内存屏障

### 3.1 CPU的乱序执行

程序里面的每行代码的执行顺序，有可能会被编译器和cpu根据某种策略，给打乱掉，目的是为了性能的提升，让指令的执行能够尽可能的并行起来。知道指令的乱序策略很重要，原因是这样我们就能够通过**barrier**等指令，在正确的位置告诉cpu或者是编译器，这里我可以接受乱序，那里我不能接受乱序等等。从而，能够在保证代码正确性的前提下，最大限度地发挥机器的性能。

下面看一个CPU乱序执行的例子，来自外国小哥的一段代码(美团面试)

```java
//CpuDisorder1.java
public class CpuDisorder1{
    public static void main(String[] args){
        int i = 0;
        while(true){
            i++;
            x = 0; y = 0;
            a = 0; b = 0;
            
            Thread one = new Thread(new Runnable(){
                public void run(){
                    a = 1;
                    x = b;
                }
            });
            
            Thread other = new Thread(new Runnable(){
                public void run(){
                    b = 1;
                    y = a;
                }
            });
            
            ont.start();
            other.start();
            one.join();
            other.join();
        }
    }
}
```

对于上面的代码，**在没有CPU指令重排序的情况下，一定不可能出现的情况就是x=0,y=0** ，但是执行上面的方法，存在x=0,y=0的情况，**说明发生了CPU的指令重排序**

### 3.2 什么是DCL(Double Check Lock)

首先回顾设计模式的单例模式，先看看下面能不能实现多线程情况下的安全性

```java
public class Singleton{
    private static volatile MyLock lock;
    
    public synchronized MyLock getInstance(){
        if(lock == null){
            lock = new MyLock();
        }
        return lock;
    }
}
```

上面的方法毫无疑问会在线程安全的情况下获得MyLock的单例模式，但是在方法上加synchronized会导致锁的粒度太粗，性能下降，看下面的:

```java
public class Singleton{
    private static volatile MyLock lock;
    
    public static MyLock getInstance(){
        if(lock == null){
            synchronized(MyLock.class){
                lock = new MyLock();
            }
        }
        return lock;
    }
}
```

仔细一看上面似乎锁的粒度变细了，也保证了线程的安全，**但是不是这样的!**这就和后面的DCL形成了对比，试想如下的情况:

* 线程A判断`lock==null`，正要准备对MyLock.class加锁的时候，线程B抢先一步，抢占了锁，初始化了对象，而线程A在B退出之后又获得锁，**就会导致MyLock被二次初始化！！！！**

正确的版本应该是DCL，即二次检查加锁:

```java
public class Singleton{
    private static volatile MyLock lock;
    
    public static MyLock getInstance(){
        if(lock == null){	//第一次检查
            synchronized(MyLock.class){
                if(lock == null){	//第二次检查
                    lock = new MyLock();
                }
            }
        }
        return lock;
    }
}
```

### 3.3 DCL到底要不要volatile

上面的单例模式我们对MyLock加上了volatile修饰符，下面针对MyLock的初始化过程看看volatile这个修饰符到底需不需要加上，学习JVM的都知道，一个对象的初始化过程包括**半初始化(先分配内存，再赋予默认值，最后再初始化为程序设置值)**，下面的汇编代码简易地描述了对象的**半初始化过程**:

```assembly
0 new #2 <T>
4 invokespecial #3 <T.<init>>
7 astore_1
```

**由于CPU存在着指令重排序的情况，假如上面的4和7发生了交换，显然程序就会发生错误！！！**

有人可能会问难道加了volatile就能防止这种情况吗？？？**没错！！！你猜对了！！！**

### 3.4 volatile如何禁止指令重排序

开始之前首先先要了解什么是内存屏障:

```markdown
屏障两边的指令不能重排序！！！！！！！！！！！！
```

如果我们再代码中加了volatile，那么在java字节码中就会加上一个`ACC_VOLATILE`标记，JSR内存屏障规范中定义了如下四种内存屏障:

* **LoadLoad屏障: ** 对于这样的语句`Load1 | LoadLoad | Load2`，能够保证Load2要读取的数据被读取完毕之前，Load1能够读取完毕
* **LoadStore屏障，StoreLoad屏障，StoreStore屏障类似，不再赘述**

对于volatile实现细节，在JVM层面，就是运用上面的不同屏障，对于读和写操作，加上不同的屏障:

```markdown
			StoreStoreBarrier			LoadLoadBarrier
			-----------------			---------------
			  volatile写操作			   volatile读操作
			-----------------			---------------
			StoreLoadBarrier			LoadStoreBarrier
```

### 3.5 HotSpots如何实现屏障

上核心代码:

```c++
//bytecodeinterperter.cpp

int field_offset = cache -> f2_as_index();
if(cache -> is_volatile()){
    if(supprt_IRIW_for_not_multiple_copy_atomic_cpu){
        OrderAccess::fence();
    }
}

//orderaccess_linux_x86.inline.hpp
inline void OrderAccess::fence(){
    if(os::is_MP()){
        //always use locked addl since mfence is sometimes expensive
        #ifdef AMD64
        	_asm_ volatile ("lock; addl $0,0(%%rsp)" : : : "cc", "memory");	//这里就是上了锁，等于加了屏障
        #else
        	_asm_ volatile ("lock: addl $0,0(%%esp)" : : : "cc", "memory");
    }
}
```

## 4. 强软弱虚引用类型

### 4.1 强引用

```java
Object o = new Object();	//强引用就是这么简单
```

### 4.2 软引用

```java
public class Soft{
    public static void main(String[] args){
        SoftReference<byte[]> m = new SoftReference<>(new byte[1024]);
        System.out.println(m.get());
        
        try{
            Thread.sleep(1 * 1000);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        byte[] newb = new byte[1024 * 1024];
        System.gc();
        System.out.println(m.get());
    }
}
```

上面的m定义了弱引用类型，为了方便测试环境，需要设置JVM的内存空间为20M或者适当的小

* 当内存空间不足的时候，软引用会被回收
* 当内存空间足够的时候，软引用不会被回收

### 4.3 弱引用

```java
public class Weak{
    public static void main(String[] args){
        WeakReference<M> m = new WeakReference<>(new M());
        
        System.out.println(m.get());
        System.gc();
        System.out.println(m.get());
        
        ThreadLocal<M> tl = new ThreadLocal<>();
        tl.set(new M());
        tl.remove();
    }
}
```

**弱引用的特点就是不管内存足部足够，只要触发GC，就一定会被回收！！！！**

### 4.4 虚引用

虚引用就更过分了，它比弱引用还弱，即使存在，你也无法get到，但是虚引用有什么用呢？

**虚引用用来管理堆外内存**

咳咳咳，重点来了！！！！！！！

我们都知道JVM的垃圾回收机制是根据引用链来判断一个对象是否应该被回收，对于DirectByteBuffer，如果里面的对象失效了，那么GC很容易地就将这些垃圾清除了，但是！！！堆外内存的对象失效了呢？？？？？这时候虚引用作用就来了，假如我们的虚引用队列为q，它引用了一个对象，当对象被回收的时候，q可以检测到，然后清理堆外内存。

```java
public class Phantom{
    private static final List<Object> list = new LinkedList<>();
    private static final ReferenceQueue<M> queue = new ReferenceQueue<>();
    
    public static void main(String[] args){
        PhantomReference<M> phantomReference = new PhantomReference<>(new M(), queue);
        
        new Thread(() -> {
            while(true){
                list.add(new byte[1024 * 1024]);
                //sleep
                System.out.println(phantomReference.get());
            }
        }).start();
        
        new Thread(() -> {
            while(true){
                Reference<? extends M> poll = queue.poll();
                if(poll != null){
                    System.out.println("---虚引用对象被回收了-----" + poll);
                }
            }
        }).start();
    }
}
```



