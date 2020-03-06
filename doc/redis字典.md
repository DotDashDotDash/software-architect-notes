## redis字典源码阅读

### 前言

最近品读redis源码，不禁深深地被其精巧绝伦的设计所折服，redis字典在redis数据库中起到了举足轻重的作用，面对海量高并发的情况下，redis依旧能保持运行的正确性，不得不令人叹为观止

### redis字典的实现

redis字典实现依赖的数据结构主要包含了三个部分:**字典，Hash表，Hash表节点**，其中:

* 字典中嵌入了两个Hash表
* Hash表的table字段存放着Hash表节点
* Hash表节点存储的是键值对

下面是redis字典的`Hash表`数据结构

```c
typedef struct dictht {
    dictEntry **table;
    unsigned long size;
    unsigned long sizemask;
    unsigned long used; //包含着table数据已经存储的元素的个数
} dictht;
```

下面是redis的字典的`Hash表节点`的数据结构，`dictEntry`代表对应索引位置处的链表的头节点，本身也是一个节点

```c
typedef struct dictEntry {
    void *key;
    union {
        void *val;      //db.dict中的val
        uint64_t u64;   
        int64_t s64;    //db.expires中存储的过期时间
        double d;
    } v;                //键值对中的值，是个联合体
    struct dictEntry *next; //处理hash冲突
} dictEntry;
```

下面是redis字典的数据结构，注意，一个字典包含着两张`Hash表`

```c
typedef struct dict {
    dictType *type;   //该字典对应的特定的操作函数
    void *privdata;   //该字典依赖的数据
    dictht ht[2];     //一个字典包含着两张表
    long rehashidx;   //标记该字典是否正在进行rehash,当这个值为-1的时候，表示没有在进行rehash
    unsigned long iterators; //该字典正在运行的迭代器的个数
} dict;
```

### redis字典的容量变更机制

对于字典的查没有什么需要注意的事情，但是增和删是值得注意的，因为这两个操作可能会触发redis字典的扩容和缩容操作

> #### 在了解扩容缩容机制之前首先先要了解什么时候会触发这两个操作

对于扩容机制，redis源码中这样写到:

```c
static int _dictExpandIfNeeded(dict *d)
{
    /* Incremental rehashing already in progress. Return. */
    if (dictIsRehashing(d)) return DICT_OK;

    /* If the hash table is empty expand it to the initial size. */
    if (d->ht[0].size == 0) return dictExpand(d, DICT_HT_INITIAL_SIZE);

    /* If we reached the 1:1 ratio, and we are allowed to resize the hash
     * table (global setting) or we should avoid it but the ratio between
     * elements/buckets is over the "safe" threshold, we resize doubling
     * the number of buckets. */
    if (d->ht[0].used >= d->ht[0].size &&
        (dict_can_resize ||
         d->ht[0].used/d->ht[0].size > dict_force_resize_ratio))//这里需要注意，当负载因子超过阈值需要触发扩容机制
    {
        return dictExpand(d, d->ht[0].used*2);
    }
    return DICT_OK;
}
```

而对于负载因子的大小，定义如下:

```c
static unsigned int dict_force_resize_ratio = 5;
```

> #### 扩容的主要流程

```c
int dictExpand(dict *d, unsigned long size)
{
    /* the size is invalid if it is smaller than the number of
     * elements already inside the hash table */
    if (dictIsRehashing(d) || d->ht[0].used > size)
        return DICT_ERR;

    dictht n; /* step1: 新的Hash表 */
    unsigned long realsize = _dictNextPower(size);  /* step2: 计算比新的size大的最小的2的幂次方容量 */

    /* 无效的rehash */
    if (realsize == d->ht[0].size) return DICT_ERR;

    /* step3: 申请一块新的空间并初始化所有的指针为NULL */
    n.size = realsize;
    n.sizemask = realsize-1;
    n.table = zcalloc(realsize*sizeof(dictEntry*));
    n.used = 0;

    /* 如果字典的第一个Hash表是NULL的话，就说明这是第一次初始化而不是一次rehash */
    if (d->ht[0].table == NULL) {
        d->ht[0] = n;
        return DICT_OK;
    }

    /* 准备第二个Hash表的rehash，一旦扩容操作触发，第二个表的rehashidx就会从-1变为0 */
    /* 并把新扩容的表赋给第二个Hash表 */ 
    d->ht[1] = n;
    d->rehashidx = 0;
    return DICT_OK;
}
```

由于扩容时可能会导致一个原来的数据索引失效而出现的查不到数据的情况，所以扩容**中**的增删改查需要综合**字典中的第一张表和第二张表来进行**

rehash除了扩容时会触发，缩容时也会触发，**当used占比不足10%的时候，会触发缩容操作**，缩容操作也需要将字典中字段`rehashidx`标识为0

rehash之后，需要将字典中的第一张`Hash表`和第二张`Hash表`进行调换，并且把第二张表的`rehashidx`表示为-1

> #### redis的rehash过程面对百万级流量的时候怎么处理的

这就体现了渐进式rehash过程:

* 访问量大的时候，一次只对一个`key-value`进行rehash

* 访问量小的时候，批量对`key-value`进行rehash

### redis字典的遍历

遍历数据库的原则:

* 不重复出现数据
* 不遗漏任何数据

> #### redis遍历数据的方式

* 全遍历: `keys`
* 间断遍历: `hscan`（**这里是最最最最最精彩的！！！！**）

> #### 迭代器遍历

**迭代器遍历可能会出现的问题**: 遍历的过程中出现数据的增删改或者字典的扩容缩容操作怎么办

要回答上面的问题，首先要先看看redis迭代器的数据结构:

```c
typedef struct dictIterator {
    dict *d;          //正在迭代的字典
    long index;       //当前迭代到Hash表中的哪个索引值
    int table, safe;  //table用于表示当前正在迭代的hash表，safe表示是否是安全迭代
    dictEntry *entry, *nextEntry;
    /* unsafe iterator fingerprint for misuse detection. */
    //指纹，当字典未发生改变的时候，指纹是不会变的
    long long fingerprint;
} dictIterator;
```

**看了上面的源码，思考一个问题: 为什么需要定义`*entry`和`*nextEntry`**?

```markdown
这就涉及到了安全迭代的操作，当迭代器出现删除操作的时候，
`*nextEntry`能防止访问不会因空节点而被迫中止
```

上面提到了`safe`，所以我们就要搞懂安全迭代和普通迭代的区别:

* 普通迭代: 只访问数据，遍历时会对指纹进行严格的校验

```markdown
对字典进行**增删改查**，都会调用`dictRehashStep`，从而
进行渐进式rehash，导致指纹发生改变
```

* 安全迭代: 访问数据的同时有可能进行增删改，**安全迭代器之所以能够保证安全，不是因为限制字典的操作进行的（比如加锁），而是通过限制渐进式rehash保证的，也就是说，安全访问的时候，所有渐进式rehash的操作全部都要停止**

> #### 间断遍历(最最最精彩的来了!!!!!!!)

间断遍历和全遍历的区别是**一次只遍历一部分的数据而不是遍历全部**，其中用到了一种叫做 **“游标cursor”** 的变量

间断遍历主要调用的函数是`dictScan`，其源码如下:

```c
unsigned long dictScan(dict *d,
                       unsigned long v,
                       dictScanFunction *fn,
                       dictScanBucketFunction* bucketfn,
                       void *privdata)
{
    do{
        cursor = dictScan(ht, cursor, scanCallback, NULL, privdata);
    }while(cursor && maxiterations-- && listLength(keys) < (unsigned long) count>);
}
```

但是安全遍历的过程中可能会出现各种各样的问题，例如，字典的扩容或者缩容，针对这些情，下面展开讨论

* **遍历的过程中自始至终都没有遇到rehash操作**

这种情况不予讨论，没有发生rehash操作，结果都是正确的，不会发生遗漏遍历的情况

* **遍历的过程中出现了扩容**

为了兼容迭代操作中可能会发生扩容和缩容的情况，每次迭代都会对变量v进行修改，以确保迭代的数据没有遗漏，游标变更的算法为:

```c
v |= ~m0;
v = rev(v); //二进制逆转
v++;
v = rev(v); //二进制逆转
```

**下面我们假设Hash表的大小为4，第3次迭代的时候恰好发生了扩容操作，Hash表的容量扩大到了8**

|输入|初始值|`v|=0x100`|`v=rev(v)`|`v++`|`v=rev(v)`|最终结果|
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
|第一次游标为0|0x000|0x100|0x001|0x010|0x010|2|
|第二次游标为2|0x010|0x110|0x011|0x100|0x001|1|

这个时候发生了扩容操作，数组的掩码变成了`m0=0x111`，`~m0=0x1000`，接下来的游标值变成了

|输入|初始值|`v|=0x100`|`v=rev(v)`|`v++`|`v=rev(v)`|最终结果|
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
|第三次游标值为1|0x0001|0x1001|0x1001|0x1010|0x0101|5|
|第四次游标值为5|0x0101|0x1101|0x1011|0x1100|0x0011|3|
|第五次游标值为3|0x0011|0x1011|0x1101|0x1110|0x0111|7|
|第六次游标值为7|0x0111|0x1111|0x1111|0x0000|0x0000|0|

可以看到遍历的顺序为`2, 1, 5, 3, 7, 0`，少遍历了`4, 6`，但是由于游标为`0, 2`的数据在扩容之前已经迭代完成，而Hash表大小从4扩容至8，经过rehash之后，游标为`0, 2`的数据可能分布在`0|4`, `2|6`当中，因此不需要再迭代，这是最精彩的，可以看一下扩容后和扩容前节点位置的映射关系:

<div align=center><img src="/assets/r1.png"/></div>

* **对于缩容操作，原理也是一样的，下面就不做具体阐述了**