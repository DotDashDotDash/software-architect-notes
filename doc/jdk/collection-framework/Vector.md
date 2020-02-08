# Vector

## 继承关系

```java
public class Vector<E>
    extends AbstractList<E>
    implements List<E>, RandomAccess, Cloneable, java.io.Serializable
```

## 底层数据结构

```java
/**
     * The array buffer into which the components of the vector are
     * stored. The capacity of the vector is the length of this array buffer,
     * and is at least large enough to contain all the vector's elements.
     *
     * <p>Any array elements following the last element in the Vector are null.
     *
     * @serial
     */
    protected Object[] elementData;
```

## 容量

```java
//默认容量为10，没有指定initCapacity
public Vector() {
        this(10);
    }

/**
 * 尽管指定最大容量是Integer.MAX_VALUE - 8，但是当容量超过
 * Integer.MAX_VALUE - 8的时候，会指定newCapacity为
 * Integer.MAX_VALUE
 */
private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
```

## 扩容机制

&emsp;&emsp;这些都和其他的一些`Collection`集合差不多

```java
public synchronized void ensureCapacity(int minCapacity) {
        if (minCapacity > 0) {
            modCount++;
            ensureCapacityHelper(minCapacity);
        }
    }

private void ensureCapacityHelper(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + ((capacityIncrement > 0) ?
                                         capacityIncrement : oldCapacity);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
```

## 总结

* `Vector`源码和`ArrayList`相差无几，但是保证了线程安全，在对`modCount`进行计数的时候，触发`fast-fail`机制，同时用`sychronized`锁来保证同步性，但是这种安全性并不是完全安全性，在并发环境下，`Vector`的复合操作仍然是不安全的，因为单个`Vector`的方法并不能保证复合操作是安全的，在迭代器进行迭代的过程中，如果发现`modCount`被改变，同样会抛出`ConcurrentModificationException`，因此看到源码中对于`Iterator`的`add`,`remove`中都对`Vector.this`进行了加锁
