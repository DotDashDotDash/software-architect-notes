# Semaphore信号量

## Semaphore与ReentrantLock的区别

* Semaphore信号量为多个线程协作提供了更强大的控制方法，无论是ReentrantLock还是Synchronized一次都只允许一个线程访问一个资源。信号量允许多个线程同时访问同一个资源
* Semaphore.acquire()默认为可响应中断锁，与ReentrantLock.lockInterruptibly()作用一致
* Semaphore还有可轮询锁和定时锁的功能

## 用法

```java
Semaphore semaphore = new Semaphore(5, true);   //公平锁

    private class Task extends Thread{
        @Override
        public void run(){
            try {
                semaphore.acquire();
                System.out.println(Thread.currentThread().getName() + " acquired a lock.");
                //Thread.sleep(3000);
                /**
                 * logic code
                 */
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                semaphore.release();
                System.out.println(Thread.currentThread().getName() + " released a lock.");
            }
        }
    }
```

## 参考链接

* [MySemaphore.java](/src/concurrent/MySemaphore.java)