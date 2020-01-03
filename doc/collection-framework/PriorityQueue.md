# PriorityQueue

## 继承关系

```java
public class PriorityQueue<E> extends AbstractQueue 
    implements java.io.Serializable{...}
```

## 底层数据结构

&emsp;&emsp;`PriorityQueue`的底层是二叉堆，用数组表示

```java
transient Object[] queue;
```

## 容量

```java
//默认容量
private static final int DEFAULT_INITIAL_CAPACITY = 11;

//最大容量
private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
```

## 扩容机制

```java
private void grow(int minCapacity) {
        int oldCapacity = queue.length;
        // 小容量时(<64)加倍，大容量(>=64) 增加50%
        int newCapacity = oldCapacity + ((oldCapacity < 64) ?
                                         (oldCapacity + 2) :
                                         (oldCapacity >> 1));
        // 超出最大容量限制
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        queue = Arrays.copyOf(queue, newCapacity);
    }

private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
```

## 添加元素

&emsp;&emsp;会首先调用`add`

```java
public boolean add(E e) {
        return offer(e);
    }
```

&emsp;&emsp;`offer`会改变`modCount`，触发`fast-fail`失败，因此，`PriorityQueue`是非线程安全的

```java
public boolean offer(E e) {
        if (e == null)
            throw new NullPointerException();
        modCount++;
        int i = size;
        if (i >= queue.length)
            grow(i + 1);
        size = i + 1;
        if (i == 0)
            queue[0] = e;
        else
            siftUp(i, e);
        return true;
    }
```

&emsp;&emsp;`heap`添加元素总是在堆的叶子节点进行插入，因此为了保证`PriorityQueue`的有序性，会调用`siftUp`来**上浮**节点

```java
private void siftUp(int k, E x) {
        if (comparator != null)
            siftUpUsingComparator(k, x);    //使用特定的比较器进行比较上浮操作
        else
            siftUpComparable(k, x);         //如果数据类型是Comparable，可以不用比较器
    }

private void siftUpComparable(int k, E x) {
        Comparable<? super E> key = (Comparable<? super E>) x;
        while (k > 0) {
            //对于一个堆，假设为第n个节点，其左孩子是2n+1，右孩子是2(n+1)
            //插入节点的时候总是在右叶子节点插入，因此找寻其父亲节点就是
            //(n-1)>>1
            int parent = (k - 1) >>> 1;
            Object e = queue[parent];
            //当当前节点比父亲大的时候，跳出循环，否则继续，由此说明
            //PriorityQueue的heap类型是小根堆
            if (key.compareTo((E) e) >= 0)
                break;
            queue[k] = e;
            k = parent;
        }
        queue[k] = key;
    }
```

## 删除元素

```java
public boolean remove(Object o) {
    int i = indexOf(o);
    if (i == -1) //没有在数组中找到
        return false;
    else {
        removeAt(i);
        return true;
    }
}

private E removeAt(int i) {
    // assert i >= 0 && i < size;
    modCount++;
    int s = --size;
    if (s == i) //要移除的刚好是最后一个元素，直接移出
        queue[i] = null;
    else {
        E moved = (E) queue[s];//先尝试移除最后一个元素
        queue[s] = null;
        siftDown(i, moved);     //将最后一个元素下沉
        if (queue[i] == moved) {//如果下沉没有变化，试试上浮
            siftUp(i, moved);
            if (queue[i] != moved)
                return moved;
        }
    }
    return null;
}

private void siftDown(int k, E x) {
        if (comparator != null)
            siftDownUsingComparator(k, x);
        else
            siftDownComparable(k, x);
    }

@SuppressWarnings("unchecked")
private void siftDownComparable(int k, E x) {
    Comparable<? super E> key = (Comparable<? super E>)x;
    int half = size >>> 1;        // loop while a non-leaf
    while (k < half) {
        int child = (k << 1) + 1; // assume left child is least
        Object c = queue[child];
        int right = child + 1;
        if (right < size &&
            ((Comparable<? super E>) c).compareTo((E) queue[right]) > 0)
            c = queue[child = right];
        if (key.compareTo((E) c) <= 0)
            break;
        queue[k] = c;
        k = child;
    }
    queue[k] = key;
}
```
