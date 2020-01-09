# ReadWriteLock读写锁

```markdown
synchronized读与读互斥，但是读写锁可以保证仅有一个线程在写，其他线程可以同时读
```

## ReentrantReadWriteLock

### 获取锁的方式

* 公平模式
* 非公平模式

### 可重入性

什么是可重入锁，不可重入锁呢？"重入"字面意思已经很明显了，就是可以重新进入。可重入锁，就是说一个线程在获取某个锁后，还可以继续获取该锁，即允许一个线程多次获取同一个锁。比如synchronized内置锁就是可重入的，如果A类有2个synchornized方法method1和method2，那么method1调用method2是允许的。显然重入锁给编程带来了极大的方便。假如内置锁不是可重入的，那么导致的问题是：1个类的synchornized方法不能调用本类其他synchornized方法，也不能调用父类中的synchornized方法。与内置锁对应，JDK提供的显示锁ReentrantLock也是可以重入的，这里通过一个例子着重说下可重入锁的释放需要的事儿。

```java
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MyReentrantReadWriteLock {

    public static void main(String[] args) throws InterruptedException {
        final ReentrantReadWriteLock  lock = new ReentrantReadWriteLock ();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                lock.writeLock().lock();
                System.out.println("Thread real execute");
                lock.writeLock().unlock();
            }
        });

        lock.writeLock().lock();
        lock.writeLock().lock();
        t.start();
        Thread.sleep(200);

        System.out.println("release once");
        lock.writeLock().unlock();
    }

}
```

测试结果

```java
release once
```

因为上面写锁只释放了一次，因此主线程死锁了

### 锁降级

```markdown
在不允许中间写入的情况下，写入锁可以降级为读锁吗？读锁是否可以升级为写锁，优先于其他等待的读取或写入操作？简言之就是说，锁降级：从写锁变成读锁；锁升级：从读锁变成写锁，ReadWriteLock是否支持呢？
```

* 锁升级demo

```java
public void doDownGrades(){
        lock.writeLock().lock();
        System.out.println("write lock...");
        lock.readLock().lock();
        System.out.println("read lock...");
        lock.readLock().unlock();
        lock.writeLock().unlock();
    }
```

测试结果

```java
write lock...
```

很显然，不能进行锁升级，否则会卡死线程

* 锁降级demo

```java
public void doDownGrades(){
        lock.writeLock().lock();
        System.out.println("write lock...");
        lock.readLock().lock();
        System.out.println("read lock...");
        lock.readLock().unlock();
        lock.writeLock().unlock();
    }
```

测试结果

```java
write lock...
read lock...
```

可以进行锁降级

### 读写测试

```java
public void doRead(Thread t){
        lock.readLock().lock();
        boolean isWriteLock = lock.isWriteLocked();
        if(!isWriteLock){
            System.out.println("current lock is read lock...");
        }

        try{
            for(int i = 0; i < 5; i++){
                Thread.sleep(2000);
                System.out.println(t.getName() + " is reading...");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.readLock().unlock();
            System.out.println("release read lock");
        }
    }

public void doWrite(Thread t){
        lock.writeLock().lock();
        boolean isWriteLock = lock.isWriteLocked();
        if(isWriteLock){
            System.out.println("current lock is write lock...");
        }

        try {
            Thread.sleep(2000);
            System.out.println(t.getName() + " is writing....");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.writeLock().unlock();
            System.out.println("release write lock...");
        }
    }

public static void main(String[] args){
        MyReadWriteLock myReadWriteLock = new MyReadWriteLock();

        ExecutorService service = Executors.newCachedThreadPool();

        service.execute(new Thread(()->{
            myReadWriteLock.doRead(Thread.currentThread());
        }));

        service.execute(new Thread(()->{
            myReadWriteLock.doRead(Thread.currentThread());
        }));

        service.execute(new Thread(()->{
            myReadWriteLock.doRead(Thread.currentThread());
        }));

        service.execute(new Thread(()->{
            myReadWriteLock.doWrite(Thread.currentThread());
        }));

        service.execute(new Thread(()->{
            myReadWriteLock.doWrite(Thread.currentThread());
        }));
    }
```

测试结果

```java
current lock is read lock...
current lock is read lock...
current lock is read lock...
pool-1-thread-1 is reading...
pool-1-thread-2 is reading...
pool-1-thread-3 is reading...
pool-1-thread-1 is reading...
pool-1-thread-3 is reading...
pool-1-thread-2 is reading...
pool-1-thread-1 is reading...
pool-1-thread-2 is reading...
pool-1-thread-3 is reading...
pool-1-thread-1 is reading...
pool-1-thread-3 is reading...
pool-1-thread-2 is reading...
pool-1-thread-1 is reading...
release read lock
pool-1-thread-3 is reading...
pool-1-thread-2 is reading...
release read lock
release read lock
current lock is write lock...
pool-1-thread-4 is writing....
release write lock...
current lock is write lock...
pool-1-thread-5 is writing....
release write lock...
```

### 读/写锁之间的互斥关系

读锁和写锁之间是互斥的关系，也就是说，读线程一定可以看到上一次写锁释放后更新的内容

## 参考链接

* [MyReadWriteLock.java](/src/concurrent/MyReadWriteLock.java)
* [[Java并发]ReadWriteLock](https://www.jianshu.com/p/9cd5212c8841)
