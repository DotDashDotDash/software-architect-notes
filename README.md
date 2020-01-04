<br>

|[:coffee:](./note/basic)<br>&nbsp;Java核心&nbsp;| [:computer:](./note/jvm)<br>&nbsp;Java虚拟机&nbsp;|[:penguin:](./note/concurrent)<br>&nbsp;Java并发&nbsp;|[:cloud:](./note/web)<br>&nbsp;Web&nbsp;|[:wrench:](../note/database)<br>&nbsp;数据库&nbsp;|[:rotating_light:](./FAQ)<br>&nbsp;Bug记录&nbsp;|[:book:](./doc)<br>&nbsp;源码阅读&nbsp;|
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|

<br>
<table>
  <tr>
    <td>
      <img src="./assets/tomcat.jpg" width="80px" height="60px">
      <img src="./assets/mybatis.png" width="60px" height="65px">
      <img src="./assets/spring.png" width="60px" height="60px">
      <img src="./assets/java.png" width="60px">
      <img src="./assets/maven.png" width="60px">
  </td>
  </tr>
</table>

### :coffee: Java核心

* [I/O模型](./note/basic/IO模型.md)
* [NIO模型](./note/basic/NIO模型.md)
* [内部类](./note/basic/内部类.md)
* [Java反射机制](./note/basic/Java反射.md)
* [Java泛型](./note/basic/Java泛型.md)
* [Java序列化](./note/basic/序列化.md)
* [Java深/浅拷贝](./note/basic/Java拷贝.md)

### :computer: Java虚拟机

* [ClassLoader](./note/jvm/Java类加载器.md)
* [JVM架构](./note/jvm/JVM结构.md)

### :cloud: Web

* [DAO层持久化--MyBatis框架](./note/web/spring/MyBatis.md)
* [SpringMVC](./note/web/spring/SpringMVC.md)
* [Spring拦截器](./note/web/spring/Spring拦截器.md)

### :penguin:Java并发

* [多线程基础](/note/concurrent/线程.md)
* [ExecutorService线程池](/note/concurrent/ExecutorService.md)

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