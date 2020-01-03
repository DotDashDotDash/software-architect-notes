# AtomicStampedReference

## 解决的问题

&emsp;&emsp;其他的`AtomicXXX`无法解决`ABA`问题，即使有原子性，`AtomicStampedReference`避免了`ABA`的问题，解决的方法如下:

* **版本号**
* **不重复使用节点的引用**
* **直接操作元素而不是节点**

## 内部类

&emsp;&emsp;将元素值和版本号绑定在一起，存储在Pair的reference和stamp（邮票、戳的意思）中

```java
private static class Pair<T> {
    final T reference;      //节点的引用
    final int stamp;        //时间戳
    private Pair(T reference, int stamp) {
        this.reference = reference;
        this.stamp = stamp;
    }
    /**
     * 根据引用和时间戳创建新的Pair对象
     */
    static <T> Pair<T> of(T reference, int stamp) {
        return new Pair<T>(reference, stamp);
    }
}
```

## 属性

```java
private volatile Pair<V> pair;
private static final sun.misc.Unsafe UNSAFE = sun.misc.Unsafe.getUnsafe();
private static final long pairOffset =
    objectFieldOffset(UNSAFE, "pair", AtomicStampedReference.class);
```

## 构造方法

```java
public AtomicStampedReference(V initialRef, int initialStamp) {
    pair = Pair.of(initialRef, initialStamp);
}
```

## compareAndSet

```java
/**
 * 大概的意思就是如果引用不变，邮戳不变，期望不变，为true
 * 或者创建一个新的Pair，将当前引用和时间戳指向新Pair
 */
public boolean compareAndSet(V   expectedReference,
                             V   newReference,
                             int expectedStamp,
                             int newStamp) {
        // 获取当前的（元素值，版本号）对
        Pair<V> current = pair;
        return
            // 引用没变
            expectedReference == current.reference &&
            // 版本号没变
            expectedStamp == current.stamp &&
            // 新引用等于旧引用
            ((newReference == current.reference &&
            // 新版本号等于旧版本号
            newStamp == current.stamp) ||
            // 构造新的Pair对象并CAS更新
            casPair(current, Pair.of(newReference, newStamp)));
}

private boolean casPair(Pair<V> cmp, Pair<V> val) {
    // 调用Unsafe的compareAndSwapObject()方法CAS更新pair的引用为新引用
    return UNSAFE.compareAndSwapObject(this, pairOffset, cmp, val);
}
```

## get

```java
/**
 * 实际上就是修改的stampHolder并返回引用
 */
public V get(int[] stampHolder) {
        Pair<V> pair = this.pair;
        stampHolder[0] = pair.stamp;
        return pair.reference;
    }
```
