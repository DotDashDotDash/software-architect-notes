# HashTable

## 底层数据结构

```java
/**
     * The hash table data.
     */
    private transient Entry<?,?>[] table;
```

## HashTable的线程安全性

&emsp;&emsp;HashTable是线程安全的

## HashTable最大容量

```java
private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
```

## HashTable的初始容量及初始负载因子

```java
public Hashtable() {
        this(11, 0.75f);
    }
```

## HashTable插入过程

```java
public synchronized V put(K key, V value) {
        // Make sure the value is not null
        if (value == null) {
            throw new NullPointerException();
        }

        // Makes sure the key is not already in the hashtable.
        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        @SuppressWarnings("unchecked")
        Entry<K,V> entry = (Entry<K,V>)tab[index];
        for(; entry != null ; entry = entry.next) {
            if ((entry.hash == hash) && entry.key.equals(key)) {
                V old = entry.value;
                //如果发现重复，新值代替旧值，返回旧值
                entry.value = value;
                return old;
            }
        }

        //没有重复，直接插入
        addEntry(hash, key, value, index);
        return null;
    }
```

## HashTable与HashMap区别

* 与HashMap不同，key和value均不能为空
* Hashtable计算hash值，直接用key的hashCode()，而HashMap重新计算了key的hash值
* HashTable计算索引用数值计算，HashMap采用位运算

## HashTable确定索引

```java
int index = (hash & 0x7FFFFFFF) % tab.length;
```

## HashTable的扩容机制

```java
protected void rehash() {
        int oldCapacity = table.length;
        Entry<?,?>[] oldMap = table;

        // 新容量和旧容量的关系
        int newCapacity = (oldCapacity << 1) + 1;
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            if (oldCapacity == MAX_ARRAY_SIZE)
                // 如果旧的容量已经到达了最大值，继续按照这个最大值运行
                return;
            newCapacity = MAX_ARRAY_SIZE;
        }
        Entry<?,?>[] newMap = new Entry<?,?>[newCapacity];

        modCount++;
        //门限值，超过这个值将会再次分配
        threshold = (int)Math.min(newCapacity * loadFactor, MAX_ARRAY_SIZE + 1);
        table = newMap;

        for (int i = oldCapacity ; i-- > 0 ;) {
            for (Entry<K,V> old = (Entry<K,V>)oldMap[i] ; old != null ; ) {
                Entry<K,V> e = old;
                old = old.next;
                //newCapacity = (oldCapacity << 1) + 1;
                //写成int index = (e.hash & 0x7FFFFFFF) & (newCapacity - 2)也是可以的
                int index = (e.hash & 0x7FFFFFFF) % newCapacity;
                e.next = (Entry<K,V>)newMap[index];
                newMap[index] = e;
            }
        }
    }
```

## HashTable计算hash的过程

&emsp;&emsp;`loadFactor`取负数代表正在计算

```java
public synchronized int hashCode() {
        /*
         * This code detects the recursion caused by computing the hash code
         * of a self-referential hash table and prevents the stack overflow
         * that would otherwise result.  This allows certain 1.1-era
         * applets with self-referential hash tables to work.  This code
         * abuses the loadFactor field to do double-duty as a hashCode
         * in progress flag, so as not to worsen the space performance.
         * A negative load factor indicates that hash code computation is
         * in progress.
         */
        int h = 0;
        if (count == 0 || loadFactor < 0)
            return h;  // Returns zero

        loadFactor = -loadFactor;  // 标志正在计算hashCode
        Entry<?,?>[] tab = table;
        for (Entry<?,?> entry : tab) {
            while (entry != null) {
                h += entry.hashCode();
                entry = entry.next;
            }
        }

        loadFactor = -loadFactor;  // 计算结束

        return h;
    }
```
