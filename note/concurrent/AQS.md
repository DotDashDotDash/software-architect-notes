# AQS

AQS抽象同步队列是许多同步器的核心，其内部维护了一个state和FIFO队列，当多线程争夺锁的时候，失败线程会进入到这个FIFO队列中等待唤醒

获取state的方法只有三种:

* getState()
* setState()
* compareAndSetState()

AQS还定义了两种资源共享的方式:

* 独占模式Exclusive: ReentrantLock就是个例子
* 共享模式Share: Semaphore和CountDownLatch

AQS实现是ABS核心(以state状态为计数)，以ReentrantLock为例，初始设置state为0，当lock()时，会调用tryAcquire()来使state加1，tryRelease()会使state减1，当state=0时才表示锁当前是空闲的状态，要记住，**重入多少次就释放多少次，否则state不归零会使程序死锁**
