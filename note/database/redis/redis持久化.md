# Redis持久化

## 快照持久化RDB

所谓快照就是创建内存数据在某一时间节点的镜像值并保存，当服务器发生宕机的时候，主机就会丢失最近一次快照之后的操作及数据

redis提供的快照持久化选项:

```redis
save 60 1000    //60s内有1000次写入触发bgsave
stop-write-on-bgsave-error no
rdbcompression yes
dbfilename dump.rdb
```

创建快照的方法有:

* 客户端键入`BGSAVE`，此时redis会fork一个子进程，将快照写入磁盘
* 客户端键入`SAVE`，一般情况下会选择这个命令，只有当客户端没有足够内存的时候才去使用`BGSAVE`
* redis通过`SHUTDOWN`命令接收到关闭服务器的请求时，会执行`SAVE`
* redis接收到标准`TERM`信号时，会执行一个`SAVE`，阻塞所有客户端，不再执行客户端发送的任何命令
* 当一个redis服务器连接另外一个redis服务器，并向对方发送`SYNC`来进行一次复制的时候，如果主服务器并非没有执行或者刚执行完`BGSAVE`，主服务器就会执行`BGSAVE`

快照适应的情况是: **即使丢失一部份数据也不会造成问题的应用程序**

## AOF持久化

redis提供的aof持久化选项

```redis
appendonly no   //是否使用aof持久化
appendfsync everysec   //多久将写入的内容同步到磁盘
no-appendfsync-on-rewrite no    //在对aof进行压缩的时候是否执行同步操作
auto-aof-rewrite-percentage 100 //aof体积大了一倍之后执行BGREWRITEAOF
auto-aof-rewrite-min-size 64mb  //aof体积达到64mb时执行BGREWRITEAOF
```

redis在重启的时候通过aof中的所有写命令来还原数据集，所以aof的体积会越来越大，重启还原数据所花的时间也会越来越长，用户可以键入`BGREWRITEAOF`来去除aof中的冗余命令来重写aof文件

## Redis主从复制

|步骤|主服务器操作|从服务器操作|
|:---:|:---:|:---:|
|1|(等待命令)|连接(或者重连接)主服务器，发送SYNC命令|
|2|开始执行BGSAVE，并使用缓冲区记录BGSAVE之后执行的所有写命令|根据配置选项来决定继续使用现有的数据(如果有的话)来处理客户端的命令请求，还是想发送请求客户端返回错误|
|3|BGSAVE执行完毕，向从服务器发送快照文件，并在发送期间继续使用缓冲区记录被执行的写命令|丢弃所有旧数据(如果有的话)，开始载入主服务器发来的快照文件|
|4|快照文件发送完毕之后，开始向从服务器发送存储在缓冲区里面的写命令|完成对快照文件的解释操作，像往常一样开始接受命令请求|
|5|缓冲区存储的写命令发送完毕，从现在开始，每执行一个写命令，就开始像从服务器发送相同的写命令|执行主服务器发来的所有存储在缓冲区里面的写命令，从现在开始，接受并且执行主服务器传来的每个写命令|

设置从服务器的方式很简单:

```redis
SLAVEOF host port
```

redis并不支持主主复制

## 检验硬盘写入

* 如何检验主服务器是否将写数据发送至从服务器:**主服务器构建一个特殊值，检查这个特殊值是否存在于从服务器**

* 如何检验写数据已经存储进从服务器的硬盘里: **检查info命令的输出结果aof_pending_bio_fsync属性值是否为0**

## 处理系统故障

redis提供了下面两个工具来检测和修复快照和aof文件

```shell
$ redis-check-aof
Usage: redis-check-aof [--fix] <file.aof>
$ redis-check-dump
Usage: redis-check-dump <dump.aof>
```

## 参考链接

* [Redis AOF持久化详解](https://www.cnblogs.com/remcarpediem/p/11644722.html)
