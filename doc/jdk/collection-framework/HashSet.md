# HashSet

## 底层数据结构

```java
/**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * default initial capacity (16) and load factor (0.75).
     */
    public HashSet() {
        map = new HashMap<>();
    }
```

## HashSet与HashMap的区别与联系

* `HashSet`底层数据结构是`HashMap`，但是仅仅用到了`Map`的`key`，`value`则是`new Object`来填充，为了做到复用浪费了一些资源(个人认为)

```java
//HashSet#add
public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }

// Dummy value to associate with an Object in the backing Map
private static final Object PRESENT = new Object();

```