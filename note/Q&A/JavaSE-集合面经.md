# JavaSE 集合面经

题目部分摘自牛客Java工程师面经

* 简述Collection和Collections的区别

```markdown
Collection是集合类的上级接口，继承它的接口主要有Set,List,Deque
Collections是针对集合类的一个辅助类，提供一系列静态方法实现对集合的搜索，排序，线程安全化的操作
```

* 简述HashMap和HashTable的区别

```markdown
HashMap和HashTable都实现了Map接口
HashMap允许键和值为null，而HashTable不允许键/值为null
HashMap是非线程安全的，HashTable是线程安全的
HashMap提供了可供迭代的键的集合，而HashTable提供了对键的Enumeration
一般认为HashTable是一个遗留的类
```

* 快速失败(fail-fast)和安全失败(fail-safe)的区别

```markdown
java.util包下面的都是快速失败的
java.util.concurrent包下面的所有类都是安全失败的

快速失败的迭代器会抛出ConcurrentModificationException
而安全失败不会抛出这样的异常
```

* Iterator和ListIterator之间的区别

```markdown
Iterator可以用来遍历List和Set集合，但是ListIterator只能用来遍历List
Iterator只支持向前遍历，但是ListIterator支持双向遍历
ListIterator实现了Iterator接口，增强了Iterator的功能
```

* Iterator使用的注意事项

```markdown
在迭代元素的过程中不能通过集合的方法删除元素，否则会触发fast-fail
要删除元素，可以通过Iterator.remove()删除元素
```

* 简述ConcurrentHashMap的原理(jdk1.7和jdk1.8)

```markdown
jdk1.7之前:
ConcurrentHashMap包含两个静态内部类，Segment和HashEntry
HashEntry存放映射表的键/值对
Segment充当锁的角色，每个Segment守护整个散列表的若干个桶
每个桶是由若干个HashEntry对象连接起来的链表

jdk1.8之后:
放弃了臃肿的Segment+HashEntry的组合
采用Node+CAS+Synchronized来保证并发的实现
维持了一个volatile的变量baseCount，当插入元素或者删除元素的时候
通过addCount()更新baseCount
通过累加baseCount和CounterCell数组中的数量，得到元素的总个数
```

* TreeMap的底层实现

```markdown
底层红黑树(平衡排序二叉树)
```

* 为什么HashMap的容量总是2^n

```markdown
1. HashMap默认的负载因子0.75，2^n有助于散列更均匀
2. 2^n-1得到的是二进制表示全1的数字，这样可以充分利用散列值，分布更加均匀
```

* 如果HashMap的key是一个自定义的类，需要注意什么

```markdown
一般来说，HashMap计算key的值是通过hashCode()，默认情况下是返回对象的引用地址
因此，要正确计算key，需要重写hashCode()和equals()
```

* 简述一下HashMap插入和删除的流程即底层原理

```markdown
HashMap是基于数组实现的，插入元素的时候，首先通过hashCode()计算key的值，对于hash冲突，拉链法解决

HashMap在jdk1.8之后引入了红黑树结构优化:
1. 当Map元素个数大于8的时候，链表进化成红黑树
2. 当Map元素个数小于6的时候，红黑树重新退化成链表

Q1: 为什么HashMap元素个数大于8的时候，进化成红黑树?
A1: 当个数为8的时候，树形查找长度为3，链表平均查找长度为4，因此需要进化成树形

Q2: 为什么HashMap元素个数小于6的时候，退化成链表而不是8个?
A2: 6和8之间有一个7的缓冲区，防止元素在8前后浮动的时候频繁进化和退化
```
