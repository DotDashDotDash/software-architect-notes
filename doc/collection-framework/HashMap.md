# HashMap

## 扩容机制单位

* `size`:Map中KV的数目
* `capactiy`:桶的数目
* `threshold`:阈值，超过阈值double桶的数目
* `loadfactor`:`size/capacity`负载因子

## 何时扩容

```java
//jdk1.8
if ((++size >= threshold) && (null != table[bucketIndex])){...}
```

## 新加入的节点如何通过hash确定在桶的序号

```java
static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
```

## 链表的转化

&emsp;&emsp;数组+链表,链表长度大于8转化为红黑树，小于6退化为链表

## 相同hash值(&之后)的链表插入

&emsp;&emsp;尾插法，头插法在高并发情况下会产生环
