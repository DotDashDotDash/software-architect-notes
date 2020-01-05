# synchronized锁

## 锁的是什么

* 作用于普通方法的时候，是给当前调用该方法的实例(this)加锁
* 作用于静态方法的时候，是给对应的Class的所有实例加锁:**因为Class的相关数据存储在metaspace(jdk1.8)，metaspace是全局共享的，因此静态方法锁会锁柱所有调用该方法的线程**
* **锁住的是以该对象为锁的代码块**，该锁有多个队列，当有多个线程同时访问这个锁，会被放到不同的容器中

## synchronized锁的核心组件

* Wait Set: 哪些线程调用了wait()被阻塞
* Contention List: 所有请求锁的线程会首先放在这个容器中
* Entry List: Contention List中有资格竞争锁的线程被放到这个容器当中
* OnDeck: 任何一个时候，最多一个线程有资格称为锁的持有者，这个线程被称为OnDeck
* Owner: 当前应获得锁的线程
* !Owner: 释放锁

## synchronized的实现流程

<div align=center><img src="/assets/syn1.png"/></div>

* synchronized是非公平锁，在线程进入Contention List之前，首先先自旋尝试获得锁，也就是“我先试试看插个队”
* 由于可能会有多个线程同时请求锁，会从Contention List中选取一些能够有资格竞争锁的线程进入Entry List，也称“辛德勒的名单”
* Entry List中只有一个最终会得到锁，称为OnDeck，“天选之子”，但是这个“天子”可能随时被还没有进入Contention List的线程自旋时“篡位”，通俗一点就是，“天选之子”还没登基，就被“篡位了”
* Owner线程表示“现任天子”，调用了wait()，会进入Wait Set，即“打入凡间”
* Wait Set中的线程在被notify()或者notifyAll()时，进入Entry Set重新竞争锁

## synchronized锁自jdk1.6之后的优化(待做)

## 参考链接

* [Java核心复习-synchronized](https://www.cnblogs.com/fonxian/p/10872814.html)
* [彻底搞懂synchronized(从偏向锁到重量级锁)](https://blog.csdn.net/qq_38462278/article/details/81976428)