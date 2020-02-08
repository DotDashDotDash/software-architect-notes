# ReetrantLock(非公平)

## 继承关系

&emsp;&emsp;`ReetrantLock`实现了`AQS`接口，`AQS`中没有具体实现`acquire`等方法，需要子类实现

## 构造方法

```java
//ReentrantLock默认是非公平锁
public ReentrantLock() {
        sync = new NonfairSync();
    }

//可以选择参数来构造公平锁
public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }
```

## 加锁`lock()`

```java
public void lock() {
    sync.lock();
}
```

&emsp;&emsp;仅讨论非公平锁的加锁过程:

```java
/**
 * 非公平模式锁
 */
static final class NonfairSync extends Sync {
    private static final long serialVersionUID = 7316153563782823691L;

    /**
     * 执行锁动作，先进行修改状态，如果锁被占用则进行请求申
     * 请锁，申请锁失败则将线程放到队列中，注意state=0代表没有
     * 任何线程占用锁，置1代表有线程占用锁
     */
    final void lock() {
        if (compareAndSetState(0, 1))
            //cas成功，把占用锁的线程设置为当前线程
            setExclusiveOwnerThread(Thread.currentThread());
        else
            //锁已经被占用，尝试获得
            acquire(1);
    }
}
```

&emsp;&emsp;当第二个线程尝试去获取锁的时候，发现锁已经被占用了，因为上一个线程并没有释放锁，所以第二线程直接获取锁时获取失败则进入到`acquire`方法中，这个方法是`AbstractQueuedSynchronizer`中的方法`acquire`:

```java
/**
 * AQS中的方法
 */
public final void acquire(int arg) {
    //以独占方式！！！！！！加入到等待队列中
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}

// 继承自AQS的tryAcquire方法，尝试获取锁操作，这个方法会被AQS的acquire调用
protected final boolean tryAcquire(int acquires) {
    return nonfairTryAcquire(acquires);
}

final boolean nonfairTryAcquire(int acquires) {
    final Thread current = Thread.currentThread();  //获取当前线程
    int c = getState();  //获取state的状态值
    if (c == 0) {   //如果状态等于0代表线程没有被占用
        if (compareAndSetState(0, acquires)) {      //cas修改state值
            setExclusiveOwnerThread(current);       //设置当前线程为独占模式
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {//如果state状态不等于0则先判断是否是当前线程占用锁，如果是则进行下面的流程。
        /**
         * 这个地方就说明重入锁的原理，如果拥有锁的是当前线程，
         * 则每次获取锁state值都会跟随递增
         */
        int nextc = c + acquires;  
        if (nextc < 0)   //溢出了
            throw new Error("Maximum lock count exceeded");
        setState(nextc);  //直接设置state值就可以不需要CAS
        return true;
    }
    return false;                                                                       //都不是就返回false
}

private Node addWaiter(Node mode) {
    //首先生成当前线程拥有的节点
    Node node = new Node(Thread.currentThread(), mode);
    // 下面的内容是尝试快速进行插入末尾的操作，在没有其他线程同时操作的情况
    Node pred = tail;            //获取尾节点
    if (pred != null) {          //尾节点不为空，代表队列不为空
        node.prev = pred;        //尾节点设置为当前节点的前节点
        if (compareAndSetTail(pred, node)) {//修改尾节点为当前节点
            pred.next = node;    //原尾节点的下一个节点设置为当前节点
            return node;         //返回node节点
        }
    }
    enq(node);        //如果前面入队失败，这里进行循环入队操作，直到入队成功
    return node;
}

private Node enq(final Node node) {
    for (;;) {    //死循环进行入队操作，直到入队成功
        Node t = tail;  //获取尾节点
        if (t == null) { //判断尾节点为空，则必须先进行初始化
            if (compareAndSetHead(new Node()))//生成一个Node，并将当前Node作为头节点
                tail = head;   //head和tail同时指向上面Node节点
        } else {
            node.prev = t;     //设置入队的当前节点的前节点设置为尾节点
            if (compareAndSetTail(t, node)) {  //将当前节点设置为尾节点
                t.next = node;     //修改原有尾节点的下一个节点为当前节点
                return t;          //返回最新的节点
            }
        }
    }
}

final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;      //取消节点标志位
    try {
        boolean interrupted = false;   //中断标志位
        for (;;) {
            final Node p = node.predecessor();      //获取前节点
            if (p == head && tryAcquire(arg)) {     //这里的逻辑是如果前节点为头结点并且获取到锁则进行头结点变换
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            if (shouldParkAfterFailedAcquire(p, node) &&   //设置waitStatus状态
                parkAndCheckInterrupt())   //挂起线程
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);    //取消操作
    }
}

private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus;
    if (ws == Node.SIGNAL)
        /*
         * 此节点已经设置了状态，要求对当前节点进行挂起操作
         */
        return true;
    if (ws > 0) {
        /*
         * 如果前节点被取消，则将取消节点移除队列操作
         */
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
        /*
         * waitStatus=0或者PROPAGATE时，表示当前节点还没有被挂起停止，需要等待信号来通知节点停止操作。
         */
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}

private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this);                 //挂起线程
    return Thread.interrupted();            //判断是否被中断，获取中断标识
}
```

## 加锁流程总结(非公平锁)

* 首先使用一个CAS操作来判断锁目前是被占用还是空闲
  * 如果锁空闲(`state=0`)，CAS成功，把当前线程设置为拥有锁的线程
  * 如果锁已经被占用(`state>=1`)，CAS操作失败，调用acquire来尝试获得锁
    * 在锁被占用的前提下，`tryAcquire`会返回`false`，这时会把当前线程以**独占方式**加入到同步队列中，表示随时可以获取锁
      * 加入同步队列的方式有快速加入(同步队列不为空)，`enq`方式(同步队列为空)
      * `acquireQueued`不间断的去获取已经入队队列中的前节点的状态，如果前节点的状态为大于0，则代表当前节点被取消了，会一直往前面的节点进行查找，如果节点状态小于0并且不等于`SIGNAL`则将其设置为`SIGNAL`状态，设置成功后将当前线程挂起(这由`shouldParkAfterFailedAcquire`完成)，挂起线程后也有可能会反复唤醒挂起操作，如果找到头节点并且头节点获取锁成功，(也就是说等待队列的第一个得到了锁，需要重新设置同步队列的头节点)
    * 如果线程在获得锁的时候抛出了异常会进入到`cancelAcquire`过程，这个过程会取消获得锁
  
## 释放锁`unlock`

```java
public void unlock() {
    sync.release(1);
}

public final boolean release(int arg) {
    if (tryRelease(arg)) {                                  //调用ReentrantLock中的Sync里面的tryRelease方法
        Node h = head;                                          //获取头节点
        if (h != null && h.waitStatus != 0) //头节点不为空且状态不为0时进行unpark方法
            unparkSuccessor(h);                         //唤醒下一个未被取消的节点
        return true;
    }
    return false;
}

protected final boolean tryRelease(int releases) {
    int c = getState() - releases;                                                      //获取state状态，标志信息减少1
    if (Thread.currentThread() != getExclusiveOwnerThread())    //线程不一致抛出异常
        throw new IllegalMonitorStateException();
    boolean free = false;                                                                           //是否已经释放锁
    if (c == 0) {                                                                                           //state=0时表示锁已经释放
        free = true;                                                                                    //将标志free设置为true
        setExclusiveOwnerThread(null);                                              //取消独占锁信息
    }
    setState(c);                                                                                            //设置锁标志信息
    return free;
}

private void unparkSuccessor(Node node) {
    /*
     * 获取节点的waitStatus状态
     */
    int ws = node.waitStatus;
    // 如果小于0则设置为0
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);

    /*
     * 唤醒下一个节点，唤醒下一个节点之前需要判断节点是否存在或已经被取消了节点，如果没有节点则不需唤醒操作，如果下一个节点被取消了则一直一个没有被取消的节点。
     */
    Node s = node.next;
    if (s == null || s.waitStatus > 0) {
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    if (s != null)
        LockSupport.unpark(s.thread);
}

final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;                          //取消节点标志位
    try {
        boolean interrupted = false;                //中断标志位
        for (;;) {
            final Node p = node.predecessor();      //获取前节点
            if (p == head && tryAcquire(arg)) {     //这里的逻辑是如果前节点为头结点并且获取到锁则进行头结点变换
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            if (shouldParkAfterFailedAcquire(p, node) &&    //设置waitStatus状态
                parkAndCheckInterrupt())                    //挂起线程
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);  //取消操作
    }
}
```
