<br>

<div align=center>

|[:coffee:](./note/basic)<br>&nbsp;Java核心&nbsp;| [:computer:](./note/jvm)<br>&nbsp;Java虚拟机&nbsp;|[:penguin:](./note/concurrent)<br>&nbsp;Java并发&nbsp;|[:cloud:](./note/web)<br>&nbsp;Web&nbsp;|[:wrench:](../note/database)<br>&nbsp;数据库&nbsp;|[:rotating_light:](./FAQ)<br>&nbsp;Bug记录&nbsp;|[:book:](./doc)<br>&nbsp;源码阅读&nbsp;|[:triangular_flag_on_post:](./note/distribution)<br>&nbsp;分布式&nbsp;|
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|

</div>

<div align=center><img src="/assets/a.png" width=300px></div>

### :coffee: Java核心

* [I/O模型](./note/basic/IO模型.md)
* [JavaNIO模型](./note/basic/NIO模型.md)
* [Java内部类](./note/basic/内部类.md)
* [Java反射机制](./note/basic/Java反射.md)
* [Java泛型](./note/basic/Java泛型.md)
* [Java序列化](./note/basic/序列化.md)
* [Java深/浅拷贝](./note/basic/Java拷贝.md)
* [Java注解(重点继承性)](/note/basic/注解.md)
* [设计模式](/note/basic/设计模式.md)

### :computer: Java虚拟机

* [ClassLoader](./note/jvm/Java类加载器.md)
* [JVM架构](./note/jvm/JVM结构.md)

### :cloud: Web

* [JavaWeb三大组件之Servlet](/note/web/spring/JavaWeb三大组件之Servlet.md)
* [JavaWeb三大组件之Filter](/note/web/spring/JavaWeb三大组件之Filter.md)
* [JavaWeb三大组件之Lisntener](/note/web/spring/JavaWeb三大组件之Listener.md)
* [Cookie与Session](/note/web/spring/Cookie与Session.md)
* [DAO层持久化--MyBatis框架](./note/web/spring/MyBatis.md)
* [SpringMVC](./note/web/spring/SpringMVC.md)
* [Spring拦截器](./note/web/spring/Spring拦截器.md)

### :penguin:Java并发

* [Java多线程基础](/note/concurrent/线程.md)
* [Java锁](/note/concurrent/Java锁.md)
* [Java阻塞队列](/note/concurrent/阻塞队列.md)
* [ExecutorService线程池](/note/concurrent/ExecutorService.md)
* [synchronized锁(重要)](/note/concurrent/synchronized.md)
* [Semaphore信号量](/note/concurrent/Semaphore的强大之处.md)
* [ReadWriteLock读写锁](/note/concurrent/ReadWriteLock.md)
* [CountDownLatch与join()](/note/concurrent/CountDownLatch与join().md)
* [CyclicBarrier循环栅栏](/note/concurrent/CyclicBarrier)
* [多线程数据共享](/note/concurrent/线程之间共享数据.md)
* [CAS](/note/concurrent/CAS.md)
* [AQS](/note/concurrent/AQS.md)

### :triangular_flag_on_post:分布式

* [ZooKeeper分布式集群](/note/distribution/ZooKeeper.md)
* [RMI+ZooKeeper实现分布式框架](/note/distribution/RMI+ZooKeeper分布式框架.md)

### :book:源码阅读

* [base-framework](./doc/base-framework)
  * [Transient](./doc/base-framework/transient.md)
* [collection-framework](./doc/collection-framework)
  * [HashMap](./doc/collection-framework/HashMap.md)
  * [HashTable](./doc/collection-framework/HashTable.md)
  * [HashSet](./doc/collection-framework/HashSet.md)
  * [Collection](./doc/collection-framework/Collection.md)
  * [AbstractCollection](./doc/collection-framework/AbstractCollection.md)
  * [ArrayList](./doc/collection-framework/ArrayList.md)
  * [LinkedList](./doc/collection-framework/LinkedList.md)
  * [PriorityQueue](./doc/collection-framework/PriorityQueue.md)
  * [Vector](./doc/collection-framework/Vector.md)
  * [Stack](./doc/collection-framework/Stack.md)
* [concurrent-framework](./doc/concurrent-framework)
  * [atomic](./doc/concurrent-framework/atomic)
    * [Violatile](./doc/concurrent-framework/atomic/voliatle.md)
    * [AtomicStampedReference](./doc/concurrent-framework/atomic/AtomicStampedReference.md)
    * [AtomicMarkableReference](./doc/concurrent-framework/atomic/AtomicMarkableReference.md)
  * [locks](./doc/concurrent-framework/locks)
    * [Condition](./doc/concurrent-framework/locks/Condition.md)
    * [AbstractQueuedSynchronizer--I](./doc/concurrent-framework/locks/AbstractQueuedSynchronizer(I).md)
    * [AbstractQueuedSynchronizer--II](./doc/concurrent-framework/locks/AbstractQueuedSynchronizer(II).md)
    * [ReentrantLock(unfair)](./doc/concurrent-framework/locks/ReentrantLock(unfair).md)
    * [ReentrantLock(fair)](./doc/concurrent-framework/locks/ReentrantLock(fair).md)
  * [collections](./doc/concurrent-framework/collections)
    * [ConcurrentHashMap](./doc/concurrent-framework/collections/ConcurrentHashMap.md)

### :rotating_light: FAQ
