# Condition(Object)

## Condition接口

```java
public interface Condition{
    /**
     * 暂停这个线程，直到下面的其中之一发生
     * 1. 此Condition被signal或者signalAll
     * 2. Thread.interrupt()
     * 3. 伪wakeup()，但是好像没有看到具体的原理
     */
     void await();

    //跟上面类似,不过不响应中断
    void awaitUninterruptibly();

    //带超时时间的await()
    long awaitNanos(long nanosTimeout) throws InterruptedException;

    //带超时时间的await()
    boolean await(long time, TimeUnit unit) throws InterruptedException;

    //带deadline的await()
    boolean awaitUntil(Date deadline) throws InterruptedException;

    //唤醒某个等待在此condition的线程
    void signal();
  
    //唤醒所有等待在此condition的所有线程
    void signalAll();
}
```
