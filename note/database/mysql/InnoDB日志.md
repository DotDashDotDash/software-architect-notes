# InnoDB日志

MySQL是日志优先的数据库，即对于数据的操作，会首先写入到日志当中，然后再刷新回磁盘当中，这叫做Write-Ahead Logging，利用到的MySQL日志主要有`redo-log`和`undo-log`


## redo-log

redo-log叫做重做日志，是一种基于页的物理日志，其中的记录包含:

* 数据页
* undo页

每次事务提交的时候，都会将redo-log刷新回磁盘，当数据库因故障崩溃重启之后，会重新执行上一次checkpoint之后的事务操作

**redo-log保证了事务的持久性**

## undo-log

undo-log叫做回滚日志，在事务回滚的时候将数据恢复到原始值，undo-log不同于redo-log，是一种逻辑日志，记录了最近**事务内部对数据库的一系列操作，通过逆操作执行来恢复原来的状态**

undo-log的执行顺序如下:

```markdown
1. START T          记录事务的开始
2. MARK OLD         记录需要修改的数据的旧值(这个必须完成持久化)
3. DO MODIFICATION  更新数据(这个必须完成持久化)
4. COMMIT T         记录事务的结束
```

而事务的回滚执行顺序如下:

```markdown
1. 扫描undo-log，找到所有的没有对应COMMIT的START
2. 针对所有的扫描结果，执行逆操作
```

如果数据库操作很多，宕机回滚的事务很多，可以借助checkpoint来增加回滚性能

```markdown
1. 记录CHECKPOINT_START(T1, T2, T3, ..., Tn)
2. 等待所有事务COMMIT
3. 日志中记录CHECKPOINT-END
```

使用undo-log的时候，要求redo-log以及所有事务COMMIT的持久化全部执行完毕，很影响性能

**undo-log保证了事务的原子性**
