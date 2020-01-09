# Java锁

## 乐观锁/悲观锁

* 乐观锁认为读多写少，即发生同时写的可能比较小
* 悲观锁认为写多读少，即发生同时写的可能比较大，每次写都要加锁，synchronized就是一个悲观锁

## 自旋锁

若持有锁的线程能够很快释放锁，那么等待锁的线程只需要稍稍等待一下(自旋)，不需要将线程从Runnable到Blocked进行转换，也就是阻塞当前线程，这部分开销是很大的

当然自旋锁也有缺点，**自旋是消耗CPU的**，如果长时间的自旋，消耗的资源会比线程的状态切换更加大

自旋锁有一个最长等待时间，这个自旋时间有如下变化:

* jdk1.6之前: 时间固定
* jdk1.6之后: 自适应自旋锁，根据上次自旋时间适应，还根据CPU线程数，<=CPUs/2自旋，>=CPUs/2阻塞
* 自旋锁的最坏情况是CPU A到CPU B的存储时间

## synchronized锁

参考[synchronized锁](/note/concurrent/synchronized.md)

## Semaphore信号量

类似ReentrantLock，几乎ReentrantLock的所有功能它都能实现

Semaphore设置的为可响应锁，也就是说，Semaphore.acquire()之后可以被Thread.interrupt()中断，类似ReentrantLock.lockInterruptibly()
