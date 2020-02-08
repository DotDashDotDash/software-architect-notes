# ReentrantLock(公平锁)

## 与非公平的不同

&emsp;&emsp;所谓公平锁就是在争取锁的时候是公平的，也就是说不允许后来的`Node`能够先争取到锁，就跟排队上大号一样，
代码中的实现就是检查同步队列中要争取锁的节点的前节点有没有`Node`

```java
protected final boolean tryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        if (!hasQueuedPredecessors() &&                       //判断是否有等待的线程在队列中
            compareAndSetState(0, acquires)) {                //尝试争抢锁操作
            setExclusiveOwnerThread(current);                 //设置当前线程独占锁资源
            return true;                                      //获得锁成功
        }
    }
    else if (current == getExclusiveOwnerThread()) {    //当前线程和独占锁资源的线程一致，则可以重入
        int nextc = c + acquires;         //state递增
        if (nextc < 0)
            throw new Error("Maximum lock count exceeded");
        setState(nextc);                                       //设置state状态
        return true;                                           //获得锁成功
    }
    return false;                                              //获得锁失败
}

public final boolean hasQueuedPredecessors() {
    // The correctness of this depends on head being initialized
    // before tail and on head.next being accurate if the current
    // thread is first in queue.
    Node t = tail; // 获得尾节点
    Node h = head; // 获得头节点
    Node s;
    return h != t &&    //头节点和尾节点相同代表队列为空
        ((s = h.next) == null || s.thread != Thread.currentThread());    //头节点的next节点为空代表头节点，以及s.thread不是当前线程不是自己的话代表队列中存在元素
}
```
