# ExecutorService

## 线程池

顾名思义，就是存储线程的容器，存储其中的线程的声明周期默认下与容器相同，**线程池是为了解决频繁创建和销毁线程而产生的资源浪费**

## 创建线程的方法

* 继承Thread类
* 实现Runnable接口

但是要想获得线程执行的结果，需要实现Callable接口

## 一个例子

```java
ExecutorService service = Executor.newFixedPool(5); //默认5个线程
Future f = service.submit(myCallable);              //提交Callable实现类，获得返回值Future
//读取线程返回对象...
```

## 四种线程池

* **newCachedThreadPool**: 这是一个可创建新的线程的线程池，他的基本原则是在原来的线程**可用时**复用他们，而当一个线程超过60s没有被使用，就从**缓存当中移出他们**，如果线程池里面没有线程可以用，就创建一个新的线程，如果**一个线程又很多的短期异步任务，这种线程池能够提高效率**
* **newFixedThreadPool**: 在线程或者线程池被显式地关闭之前，线程能够一直存在，如果一个线程还没有执行完成就被迫中途关闭，那么它剩下地任务将会被**另外一个可用的线程**来继续执行，由于池是固定大小，当线程不够用，task将会被推进等待队列等待，按顺序等待可用线程来执行
* **newScheduledThreadPool**: 创建一个线程池，可在指定时间后运行或者周期性运行

```java
ExecutorService service = Executors.newScheduledThreadPool(5);
        //延迟3s执行
        ((ScheduledExecutorService) service).schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println("delay for 3s...");
            }
        }, 3, TimeUnit.SECONDS);

        //延迟1s后每3s执行一次
        ((ScheduledExecutorService) service).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("execute at fixed rate...");
            }
        }, 1, 3,  TimeUnit.SECONDS);
```

* **newSingleThreadPool**: 这个线程池里面只有一个线程池，**可以在线程死后或者异常结束之后重新启动一个线程来继续原来的任务**!又称“死灰复燃”

## 参考链接

* [ExecutorService代码测试](/src/concurrent/MyExecutorService.java)
* [ExecutorService解析](https://blog.csdn.net/fwt336/article/details/81530581)
* [ExecutorService讲解](https://blog.csdn.net/wanghao_0206/article/details/76460877)