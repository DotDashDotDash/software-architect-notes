# ZooKeeper

## 什么是ZooKeeper

ZooKeeper是一个分布式协调服务，用于服务发现，分布式领导选举，配置管理等功能，它提供了一些类似于Linux的树型结构，可用于文件管理，但是，这只是轻型的文件管理，**完全不适合存储大量的文件及数据**

## ZooKeeper特点

Zookeeper集群是一个基于主从复制的高可用集群，每个server担任下面的**其中之一角色**:

* **Leader**: 一个ZooKeeper集群在一个时刻只有一个Leader，它会发起并且维护leader与各follower及observer的心跳，所有的**写操作**都必须通过leader广播给各服务器，超过半数的follower(不包含observer写入成功就算写入成功)
* **Follower**: follower会直接返回客户端的读请求，但是写请求要发给leader，还会在leader处理写请求的时候投票
* **observer**: observer与follower的区别就是没有投票权，但是可以正常响应客户端的请求，引入observer可以在保证吞吐量的情况下，减少投票的时延，因为投票的server变少了

## ZAB

### Zxid

64位，高32位为epoch，低32位为一个单调递增的计数器，每当server处理一个客户端的事务请求之后，就会加1，而epoch在每一个leader产生之后就会加1

```txt
epoch就好像每一个皇帝都有一个朝代称号epoch，当leader产生就有一个自己的epoch号，过去的leader已经不能再被人使用了
```

### ZAB的两种模式

* 恢复模式: 选举leader
* 同步模式: 选举出leader之后

### ZAB的工作流程

* **leader election**: 选举出leader，但是这时候leader刚大选结束，还不是正式的leader
* **discovery**: 让大多数的server接受新的epoch并且让follower找到正确的leader
* **synchronize**: 主从之间的副本同步，当大多数节点都同步的时候，leader才是真正的leader
* **broadcast**: 这个时候zookeeper集群才开始对外服务

## Leader的选举模式

* 先给自己投票，比较zxid，超过半数投票就成为新的leader

## ZooKeeper核心

zookeeper的核心是ZAB，这个机制保证了各个server之间的同步
