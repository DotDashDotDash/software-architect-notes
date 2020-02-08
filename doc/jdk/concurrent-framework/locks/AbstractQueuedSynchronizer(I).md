# AbstractQueuedSynchronizer

## 有关`javadoc`声明

* `AbstractQueuedSynchronizer`的作用

```java
/*Provides a framework for implementing blocking locks and related
 * synchronizers (semaphores, events, etc) that rely on
 * first-in-first-out (FIFO) wait queues.  This class is designed to
 * be a useful basis for most kinds of synchronizers that rely on a
 * single atomic {@code int} value to represent state.
 */
```

&emsp;&emsp;一开始`javadoc`里面写的该同步器使用了`blocking locks`，依赖`FIFO`，后面实现会用到这个，同时很重要的一点就是`rely on a single atomic value to represent state`

## `AQS#Node`

```java
static final class Node {
    static final Node SHARED = new Node();//标识等待节点处于共享模式
    static final Node EXCLUSIVE = null;//标识等待节点处于独占模式

    static final int CANCELLED =  1;//由于超时或中断，节点已被取消
    static final int SIGNAL    = -1;//表示下一个节点是通过park堵塞的，需要通过unpark唤醒
    static final int CONDITION = -2;//表示线程在等待条件变量（先获取锁，加入到条件等待队列，然后释放锁，等待条件变量满足条件；只有重新获取锁之后才能返回）
    static final int PROPAGATE = -3;//表示后续结点会传播唤醒的操作，共享模式下起作用

    //等待状态：对于condition节点，初始化为CONDITION；其它情况，默认为0，通过CAS操作原子更新
    volatile int waitStatus;
    //前节点
    volatile Node prev;
    //后节点
    volatile Node next;
    //线程对象
    volatile Thread thread;
    //对于Condtion表示下一个等待条件变量的节点；其它情况下用于区分共享模式和独占模式；
    Node nextWaiter;

    final boolean isShared() {
        return nextWaiter == SHARED;//判断是否共享模式
    }
    //获取前节点，如果为null，抛出异常
    final Node predecessor() throws NullPointerException {
        Node p = prev;
        if (p == null)
            throw new NullPointerException();
        else
            return p;
    }

    Node() {    // Used to establish initial head or SHARED marker
    }

    Node(Thread thread, Node mode) {     //addWaiter方法使用
        this.nextWaiter = mode;
        this.thread = thread;
    }

    Node(Thread thread, int waitStatus) { //Condition使用
        this.waitStatus = waitStatus;
        this.thread = thread;
    }
}
```

&emsp;&emsp;这里我的理解就是一个`Node`就是对应一个`Thread`，这些`Node`被存储在一个队列(其实就是链表中)，按照在队列中的顺序，释放锁获得锁(但是好像有同步队列和等待队列我仍然没有搞清楚)

## `AQS#ConditionObject`

&emsp;&emsp;`ConditionObject`是`AbstractQueuedSynchronizer`的内部类，继承了`Condition`接口
&emsp;&emsp;其核心的方法就是`await()`，下面是`await()`的实现:

```java
/**
 * 不可中断的条件等待实现！！！！！！！！！
 * 很直接，中断，直接抛出异常或者报告错误
 * 1. 一开始检测到中断，抛出异常
 * 2. 后来检测到中断，报告错误
 */
public final void await() throws InterruptedException {
            //上来很直接，我发现你这个线程被中断了!!!直接走人
            if (Thread.interrupted())
                throw new InterruptedException();

            //为当前Thread创建一个Node，加入到条件队列
            //中，注意，是加入到条件等待队列的末尾！！！！
            Node node = addConditionWaiter();
            //释放锁
            int savedState = fullyRelease(node);
            int interruptMode = 0;
            //阻塞这个线程，就是通过这个死循环来实现的，
            //直到发现这个Node已经在同步队列当中了
            while (!isOnSyncQueue(node)) {
                //park当前线程，直到唤醒
                LockSupport.park(this);
                //如果线程被中断，同样退出循环
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }

            //由上面我们可以知道，到达这里，有下面原因
            //1. 该线程已经在同步队列当中
            //2. 线程被中断
            //所以我们下面要检查，是否是因为中断原因
            //如果不是中断原因，表示线程被唤醒，获取
            //之前释放的锁
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            //唤醒之后的操作，从等待队列中移除这个Node
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            //中断之后的操作
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
        }
```

&emsp;&emsp;上面用到一个同步队列和等待队列，二者的区别就是，同步队列中的线程，有资格去竞争锁，而等待队列中的线程，只能挂起睡大觉，那么什么诱发因素能让线程从等待队列进入同步队列中？**就是线程调用了`signal()`方法**
&emsp;&emsp;`await()`首先的的流程就是为当前线程创建一个`Node`，加入到等待队列，怎么加入到等待队列中？看下面源码

```java
private Node addConditionWaiter() {
            Node t = lastWaiter;
            //如果队列中最后一个线程被取消了，就移除这个线程
            if (t != null && t.waitStatus != Node.CONDITION) {
                unlinkCancelledWaiters();
                t = lastWaiter;
            }
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            if (t == null)
                firstWaiter = node;
            else
                t.nextWaiter = node;
            lastWaiter = node;
            return node;
        }
```

&emsp;&emsp;上述代码还是很容易理解的，但是有一点我始终不太明白，就是为什么偏偏只检查最后一个线程是否被取消？？？(`@todo`)
&emsp;&emsp;再回到`await()`，将线程加入到条件等待队列中后，需要释放锁，那么释放什么锁？谁来释放锁？带着疑问我们往下看:

```java
final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            int savedState = getState();
            if (release(savedState)) {
                failed = false;
                return savedState;
            } else {
                throw new IllegalMonitorStateException();
            }
        } finally {
            if (failed)
                //如果失败，那么就判定该线程被取消
                node.waitStatus = Node.CANCELLED;
        }
    }

/**
 * 这里javadoc已经是说的很清楚了，在独占(exclusive)模式
 * 释放锁
 */
public final boolean release(int arg) {
        //tryRelease()是一个接口方法，实现
        //将在AQS实现类中完成，但是初步假设，
        //如果尝试释放成功，唤醒当前线程后面
        //的线程
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }
```

&emsp;&emsp;判定`Node`是否在同步队列中:

```java
final boolean isOnSyncQueue(Node node) {
        //如果node前置节点是null，那么一定在Condition队列中
        //因为我们addConditionWaiter的时候只设置了next
        //没有设置prev
        if (node.waitStatus == Node.CONDITION || node.prev == null)
            return false;
        //如果node的后置节点不为null，那么它一定在同步队列中
        if (node.next != null) // If has successor, it must be on queue
            return true;
        /*
         * 有可能会出现pre既不为null，同时node又不在同步队列中
         * 因为我们可能在CAS将其放进queue失败，所以我们
         * 还要从同步队列的尾部开始查询，判断其是否在同步队列
         * 因为node总是出现在队列的靠近尾部的地方，所以我们
         * 不需要遍历太多次
         */
        return findNodeFromTail(node);
    }

/**
 * 将state不是Condition的节点全部删除
 */
private void unlinkCancelledWaiters() {
            Node t = firstWaiter;
            Node trail = null;
            while (t != null) {
                Node next = t.nextWaiter;
                if (t.waitStatus != Node.CONDITION) {
                    t.nextWaiter = null;
                    if (trail == null)
                        firstWaiter = next;
                    else
                        trail.nextWaiter = next;
                    if (next == null)
                        lastWaiter = trail;
                }
                else
                    trail = t;
                t = next;
            }
        }
```

&emsp;&emsp;上述讲的是`await()`方法，下面也是一个很重要的方法，`signal()`：

```java
public final void signal() {
            //判断当前线程是否是拥有锁的独占线程
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            //这里javadoc的解释是，唤醒在条件队列中等待时间最长的线程
            //与之对应的就是条件队列中的第一个线程
            Node first = firstWaiter;
            if (first != null)
                //执行唤醒操作
                doSignal(first);
        }

private void doSignal(Node first) {
            do {
                if ( (firstWaiter = first.nextWaiter) == null)
                    lastWaiter = null;
                first.nextWaiter = null;
            } while (!transferForSignal(first) &&
                     (first = firstWaiter) != null);
        }

final boolean transferForSignal(Node node) {
        /*
         * 如果状态不能被改变，也就是说node的预期状态不是CONDITION
         * 该节点已经被取消了
         */
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false;

        /*
         * 将线程加入到同步队列当中，返回的是加入到同步队列中node
         * 的前一个节点
         */
        Node p = enq(node);
        int ws = p.waitStatus;
        /**
         * CANCELLED = 1
         * 如果p的状态为cancel并且修改waitStatus失败，直接唤醒
         */
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            LockSupport.unpark(node.thread);
        return true;
    }

private Node enq(final Node node) {
        for (;;) {
            Node t = tail;
            if (t == null) { // Must initialize
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }
```
