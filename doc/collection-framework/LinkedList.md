# LinkedList

## 底层数据结构

&emsp;&emsp;底层数据结构是一个链表

```java
private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
```

## 变量定义

&emsp;&emsp;`first`和`last`指向首尾，查询时根据索引的位置决定是顺序查询还是逆序查询

```java
/**
     * Pointer to first node.
     * Invariant: (first == null && last == null) ||
     *            (first.prev == null && first.item != null)
     */
    transient Node<E> first;

    /**
     * Pointer to last node.
     * Invariant: (first == null && last == null) ||
     *            (last.next == null && last.item != null)
     */
    transient Node<E> last;
```

## LinkedList和ArrayList的区别和联系

* 底层数据结构不同，`ArrayList`是数组，`LinkedList`是链表
* 数组和链表的区别可以近似归纳`ArrayList`和`LinkedList`区别，对于数组，增删很繁琐，改查很方便，对于链表，增删很方标，改查很繁琐
* `ArrayList`和`LinkedList`均支持变量为null
* `ArrayList`和`LinkedList`均不是线程安全的，在**增**和**删**的时候需要对`modCount`进行增加来实现`fast-fail`机制
* `LinkedList`在查找元素的时候可以根据索引来实现折半查找，即顺序查找或者逆序查找
* 无论是`ArrayList`还是`LinkedList`，在批量`add`的时候均需要调用`Collection.toArray()`来转化为数组，因为数组的操作更加方便
