# ArrayList

## ArrayList初始容量

```java
    /**
     * Default initial capacity.
     */
    private static final int DEFAULT_CAPACITY = 10;
```

## 构造的三种方式

```java
    /**
     * 无参，默认初始容量是10
     */
    public ArrayList() {...}

    /**
     * 其他数据结构形式
     */
    public ArrayList(Collection<? extends E> c){...}

    /**
     * 指定容量大小
     */
     public ArrayList(int initialCapacity){...}
```

## 常规添加元素(无索引)

&emsp;&emsp;添加元素会首先调用`add`方法:

```java
public boolean add(E e) {
        ensureCapacityInternal(size + 1);  //确保要添加的元素有位置可以存储
        elementData[size++] = e;
        return true;
    }
```

&emsp;&emsp;`add`会调用`ensureCapacity`:

```java
private void ensureCapacityInternal(int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            //每次都保证一个最小的空间增量，最小的空间增量为10，也就是说
            //当minCapacity小于10时，会按照10进行增加，否则每次增加1对
            //性能损耗比较严重
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        ensureExplicitCapacity(minCapacity);
    }
```

&emsp;&emsp;`ensureExplicitCapacity`中的`modCount`用于并发环境下的检测，执行`add`可能会导致迭代器越界，因此当迭代的时候发现`modCount`超乎预期地改变，抛出`ConcurrentModificationException`，启动快失败(`fast-fail`)机制，然而`fast-fail`并不能保证每次都能完美运行，只是**尽最大努力抛出`ConcurrentModificationException`**:

```java
private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        //未溢出的情况
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        //没有溢出，每次扩大1.5倍
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
            //MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

private static int hugeCapacity(int minCapacity) {
    if (minCapacity < 0) //溢出
        throw new OutOfMemoryError();
    return (minCapacity > MAX_ARRAY_SIZE) ?
        Integer.MAX_VALUE :
        MAX_ARRAY_SIZE;
    }
```

## contains

```java
public boolean contains(Object o) {
        return indexOf(o) >= 0; //线性搜索，时间复杂度O(n)
    }
```

## remove

```java
/**
 * 移出第一个出现的数据
 */
public E remove(int index) {
        rangeCheck(index);

        modCount++;
        E oldValue = elementData(index);

        int numMoved = size - index - 1;
        if (numMoved > 0)
            //线性时间复杂度，最坏的情况下O(n)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // GC回收器回收

        return oldValue;
    }
```

## subList

&emsp;&emsp;`SubList`实际上是创建一个原来容器的视图，当修改`subList`时也会改变原来父亲容器的元素

```java
SubList(AbstractList<E> parent, int offset, int fromIndex, int toIndex)
```

## iterator

&emsp;&emsp;调用`iterator`作遍历的时候实际上是创建一个内部类`Iterator`，当用`next`遍历的时候会检查`modCount`，发现不一致就会抛出`ConcurrentModificationException`

## 线程安全性

&emsp;&emsp;`ArrayList`非线程安全，当并发时检查`modCount`，并适当抛出`CocurrentModificationException`