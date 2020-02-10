# 从Zookeeper谈分布式应用

最近一直在读《从Paxos到ZooKeeper分布式一致性原理与实践》，看到了ZooKeeper的在企业项目中的应用，特此记录一下(因机器原理，集群有限)

## 开始前的思考

Q: **ZooKeeper节点究竟是怎么储存数据的?**

我们都知道ZooKeeper的每一个节点都有Data域，可以通过API调用来实现对节点的Data域的修改，然而，数据在每一个节点的存储形式是**byte**，而且默认的ZooKeeper的单节点存储上限是1M，如果设置的存储数据量大于这个值将无法写入，要修改这个配置，可以通过修改`zkServer.sh`文件，在文件中添加如下命令:

```shell
ZOO_USER_CFG="-Djute.maxbuffer=10240000"
```

对于字面常量类型的数据，存储起来很容易，直接转换成byte即可，但是，**一个对象如何存储在ZooKeeper节点中呢?**

可以首先想到的是: Java序列化，通过Object/Input/OutputStream来将一个对象序列化为byte:

```java
Object obj = new Object();

byte[] bt = null;
ByteArrayOutputStream bos = new ByteArrayOutputStream();
ObjectOutputStream oos = new ObjectOutputStream(bos);
oos.writeObject(obj);

bt = bos.toByteArray();

//写入到ZooKeeper节点
zk.create(path, bt, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENCE);
```

还有一种更好的办法，就是利用fastjson工具将Object转换为json字符串，**这种方式存储的对象占用的空间更小**，而序列化占用的byte[]空间很大

## 场景一: 数据的发布/订阅

利用ZooKeeper集群可以完美的实现数据的发布/订阅功能，在创建节点的时候对节点进行监听，如果服务器向节点“推”数据，即发布，而订阅的用户(监听者)就能收到节点的变动从而实现“拉”数据

## 场景二: 负载均衡

先来谈谈一次服务器请求的过程

* 客户请求目标服务器的url，即在web地址栏键入访问的网页
* 用户的请求(如果需要的话会先经过DNS)发送到服务器的默认网关
* 一般一个域名会有多个服务器别名，这样同一请求地址可能会被分配到不同的服务器上，实现了负载均衡(但是对外只有一个访问地址，多个访问地址是对内表现的)

上面的大概流程已经明白了，下面谈谈负载均衡的几种策略:

* 最简单的大概是轮询了，服务器维持一个“压力”指标，“压力”过大的服务器不在请求的范围之内，这个“压力”可能是动态的，考虑一种情况，所有的服务器“压力”都很大，总不能所有的服务器都把请求推给别人吧，这个可能导致死锁

ZooKeeper本身并没有负载均衡的实现，需要用户来自己实现负载均衡，其实业务场景中，ZooKeeper并不用来做负载均衡，因为Nginx完全可以完美的实现负载均衡

## 场景三: 命名服务

在分布式系统下，可以用ZooKeeper实现全局唯一ID的功能，在单机数据库中，利用主键的auto_increment可以实现唯一ID，但是分布式环境下不行

利用ZooKeeper的create()可以生成顺序节点，再拼接上适当的字符串就可以生成唯一的全局ID

## 场景四: 分布式协调/通知

分布式环境下运行程序，需要一个“协调者”来实现协调多机程序的运行，ZooKeeper的Watcher可以很好的完成这个任务

## 场景五: 机器上下线通知

很简单，机器上线，ZooKeeper集群在指定位置创建节点，机器下线，ZooKeeper集群在指定位置删除节点，Wacther监听，处理逻辑

## 场景六: Master选举

多台机器如何选举Master(Leader)?

多个机器同时向指定path创建同名节点，首先创建成功的机器当选Master

## 场景七: 分布式锁

* **排他锁**: 同Master选举，指定path，例如`/distributed/lock`为锁路径，多机器同时向该路径创建相同的节点`./getLock`，那么只有一个机器能够创建成功，创建成功的节点就获得了锁，释放锁之后删除节点，ZooKeeper创建的这种锁是**排他锁**
* **共享锁**: 共享锁的描述就是**读锁共享，写锁排他**，每次尝试获取锁，都按照如下的逻辑进行:

```markdown
指定锁路径: /distributed/lock

1. 尝试获取锁的时候，各个节点在此路径下创建有序节点，此时每个节点都有一个序号ID
2. Watcher事件触发，节点查看自己的ID
3. 如果要获取读锁，检查自己的ID是否是最小的或者比自己ID小的节点是否都是读请求，如果是，获取到读锁
4. 如果要获取写锁，检查自己的ID是否是最小的，是则获取到锁，不是则等待
5. 释放锁触发Watcher，等待队列重复步骤2
```

但是这样的方式可能会触发**羊群效应**，即一个Watcher事件触发，会导致大量的等待机器触发操作，系统性能严重下降或者崩溃

改进后的分布式锁的思路:

```markdown
1. 要获取读锁，则向比自己ID小的最后一个写节点注册Watcher
2. 要获取写锁，向自己的前一个节点注册Watcher
```

## 场景八: 分布式FIFO队列

向自己的前一个节点注册Watcher
