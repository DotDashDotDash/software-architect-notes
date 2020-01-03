# JVM

## JVM运行过程

<div align=center><img src="../pics/jvm1.png"></div>

## 虚拟机实例

一个程序开始运行，虚拟机就开始实例化了，多个程序启动会有多个虚拟机实例，程序关闭或者退出，虚拟机实例就会消失，多个虚拟机实例之间**数据**不能共享

## 线程

Hotspot虚拟机中的Java线程和原生操作系统的线程是**映射**的关系，原生线程创建完毕之后，就会调用Java线程的run()方法

## 内存区域

<div align=center><img src="../pics/jvm2.png"></div>

## 运行时内存区域

<div align=center><img src="../pics/jvm3.png"></div>

### > MinorGC

eden区不够会触发MinorGC，MinorGC采用**复制算法**，流程为:

* 复制: 将Eden和from区的对象复制到to区域，(若有对象年龄到达了老年，转到old区)，同时年龄+1，年龄达到15就会被移到old区中
* 清除: 清除eden和from区的对象
* 交换: from区和to区交换，上一次的to区作为下一次GC的from区

### > MajorGC

old区不够会会触发MajorGC，MajorGC采用**标记清除算法**，一般老年代的GC不是很频繁，最频繁的是新生代，所以MajorGC触发之前一般会先触发MinorGC，**当新分配的对象很大时会直接进入old区，当old区空间不够时，会触发MajorGC再分配

## 垃圾收集与垃圾收集器

<div align=center><img src="../pics/jvm4.png"></div>

### > 分代收集算法

* **新生代**: 复制算法
* **老年代**: 标记整理算法

### > 分区收集算法

将整个**堆**空间分成若干个不同的小区间，每个区间独立使用，独立回收，优点是**可以一次回收多个小区间**，根据目标停顿，合理回收若干个小区间，减少GC停顿时间

### > GC垃圾收集器

* Hostspot虚拟机GC

<div align=center><img src="../pics/HotSpot GC.png"></div>

## 代码测试

* [Java引用类型](../src/jvm/JavaFourReferences.java)