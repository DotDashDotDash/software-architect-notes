## MySQL日志系统

### redo log模块

MySQL采用的是WAL技术(Write Ahead Logging)，先写日志再写磁盘，为什么需要这样？**因为每次MySQL的执行都写磁盘会导致系统的开销过大**，以一条更新sql语句执行为例，如果每一次都需要写入磁盘，那么首先磁盘要先找到那条记录，然后更新，整个IO代价过大

当一条操作需要更新的时候，InnoDB存储引擎就会把记录写道redo log中，并且更新内存，在InnoDB适当的时候，再将这个操作更新到磁盘当中

**InnoDB的redo log是大小固定的，并且是循环写入，写到末尾就会又重新写入到开头**

redo log维护着非常重要的几个变量(称为指针也行)

1. **write pos**: 记录当前写入的位置，一边写一边往后
2. **check point**: 需要擦除的位置，也是往后移并且循环的，擦除记录之前需要把记录更新到数据文件当中，write pos和check point中间的空白部分是可以写入的部分

**有了redo log，InnoDB可以保证数据库即使发生异常，之前提交的记录都不会丢失，这种能力被称为crash-safe**

### binlog模块

MySQL从整体来看，可以分为Server层和存储引擎层，redo log是存储引擎层的，而binlog是Server层的，又被称为**归档日志**

最初的MySQL没有InnoDB存储引擎，只有MyISAM存储引擎，然而MyISAM没有crash-safe能力，所以InnoDB使用另外一套日志，也就是redo log来实现crash-safe能力

### binlog与redo log的区别

* redo log为InnoDB独有的，binlog是MySQL的Server层实现的，所有的存储引擎都可以使用
* redo log是物理日志，记录的是**在某个数据上做了什么修改**，binlog是逻辑日志，记录的是语句的原始逻辑
* redo log是循环写的，binlog是可以追加写入，**这种追加指的是当binlog写满之后，会切换到下一个，并不会覆盖之前的日志**

### MySQL的两段式提交

回到开头的问题：**怎让让数据库恢复到半个月之前的状态？**

binlog记录所有的逻辑操作，并且采用追加写的形式，如果DBA承诺半个月内可以恢复，那么备份系统一定会保存最近半个月所有的binlog，同时系统会定时做整库备份，这里的定期取决于系统的重要性，时间不等。

两段式提交首先先写redo log进入prepare阶段，后写binlog进入commit阶段，这样即使binlog没有完全写入完成，依靠redo log也能恢复到原来的库