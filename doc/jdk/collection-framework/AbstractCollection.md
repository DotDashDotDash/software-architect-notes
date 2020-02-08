# 超级实现类AbstractCollection

## 官方说明

```java
/**
 * To implement an unmodifiable collection, the programmer needs only to
 * extend this class and provide implementations for the <tt>iterator</tt> and
 * <tt>size</tt> methods.  (The iterator returned by the <tt>iterator</tt>
 * method must implement <tt>hasNext</tt> and <tt>next</tt>.)<p>
 *
 * To implement a modifiable collection, the programmer must additionally
 * override this class's <tt>add</tt> method (which otherwise throws an
 * <tt>UnsupportedOperationException</tt>), and the iterator returned by the
 * <tt>iterator</tt> method must additionally implement its <tt>remove</tt>
 * method.<p>
 */
```

* 要实现一个不可修改的`Collection`，只需要重写`iterator`和`size`
* 要实现一个可修改的`Collection`，必须重写`add`，返回的`iterator`还必须实现`remove`

## toArray

```java
//这个实现相对复杂一些，可以看到扩容最主要的手段是Arrays.copyOf()方法，
//也就是需要将原数组通过复制到新的数组中来实现的。
//注意这里返回的顺序和Iterator顺序一致
//在这里实现是为了方便不同具体实现类互相转换，我们在后续会多次见到此方法
public Object[] toArray() {
    //先根据当前集合大小声明一个数组
    Object[] r = new Object[size()];
    Iterator<E> it = iterator();
    for (int i = 0; i < r.length; i++) {
        //集合元素没那么多，说明不需要那么大的数组
        if (! it.hasNext()) 
            return Arrays.copyOf(r, i); //仅返回赋完值的部分
        r[i] = it.next();
    }
    //元素比从size()中获取的更多，就需要进一步调整数组大小
    return it.hasNext() ? finishToArray(r, it) : r;
}

private static <T> T[] finishToArray(T[] r, Iterator<?> it) {
    //记录当前大小
    int i = r.length;
    while (it.hasNext()) {
        int cap = r.length;
        //r的长度不够，继续分配
        if (i == cap) {
            //扩充方式为cap+cap/2+1，也就是1.5倍扩容
            int newCap = cap + (cap >> 1) + 1;
            // 超过了最大容量，MAX_ARRAY_SIZE=Integer.MAX_VALUE-8
            if (newCap - MAX_ARRAY_SIZE > 0)
                //重新设置cap的值
                newCap = hugeCapacity(cap + 1);
            //对r进行扩容
            r = Arrays.copyOf(r, newCap);
        }
        //赋值，进入下一轮循环
        r[i++] = (T)it.next();
    }
    // 由于之前扩容是1.5倍进行的，最后再将其设置到和r实际需要的相同
    return (i == r.length) ? r : Arrays.copyOf(r, i);
}

private static int hugeCapacity(int minCapacity) {
    if (minCapacity < 0) // 超过了最大正整数，也就是负数
        throw new OutOfMemoryError
            ("Required array size too large");
    return (minCapacity > MAX_ARRAY_SIZE) ?
        Integer.MAX_VALUE :
        MAX_ARRAY_SIZE;
}

//和toArray()方法类似，就不再赘述，具体可以查看源码
public <T> T[] toArray(T[] a) {
    //...
}
```