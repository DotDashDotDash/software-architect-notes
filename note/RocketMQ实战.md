# RocketMQ总结

## RocketMQ消息发送

RocketMQ支持3种消息发送的方式: **同步，异步，单向**

* **同步**: 发送者向MQ发送消息API时，**同步等待**，直到消息服务器返回发送结果
* **异步**: 发送者向MQ执行发送消息API时，**指定消息发送成功之后的回调函数，然后调用消息发送API，立即返回**，消息发送线程不阻塞，直到运行结束，**消息发送成功或者失败的回调任务在一个新的线程中执行**
* **单向**: 消息发送者向MQ执行发送消息API，**直接返回**，不等待消息服务器的结果，也不注册回调函数，只管发

RocketMQ发送消息需要考虑以下几个问题:

1. 消息队列如何进行负载均衡？
2. 消息发送如何实现高可用？
3. 批量消息发送如何实现一致性？

### 认识RocketMQ的消息Message

RocketMQ的消息封装类Message含有如下的属性:

* **topic**: 消息所属的主题
* **flag**: RocketMQ不做处理
* **tag**: 用于消息的过滤
* **keys**: Message索引键，多个用空格隔开，RocketMQ可以用这些key快速检索消息
* **waitStoreMsgOK**: 消息发送时是否等待消息存储完成之后再返回
* **delayTimeLevel**: 消息延迟级别，用于定时消息或者消息重试

### RocketMQ默认生产者DefaultMQProducer

RocketMQ的消息生产者可以看成是一个客户端，也是消息的提供者，RocketMQ默认的消息生产者为DefaultMQProducer，它包含了如下的核心属性:

* **producerGroup**: 生产者所属的组，消息服务器再回查事务状态的时候会随机选择组中任何一个生产者发起事务回查请求
* **createTopicKey**: 默认的topicKey
* **defaultTopicQueueNums**: 默认主题在每一个Broker队列的数量
* **sendMsgTimeout**: 发送消息的默认超时时间，默认3s
* **compressMsgBodyOverHowmuch**: 消息体超过该值的时候启动压缩，默认4K
* **retryTimesWhenSendFailed**: 同步消息发送失败的尝试次数，默认2次，总共执行3次
* **retryTimesWhenSendAsyncFailed**: 异步消息发送失败的尝试次数，默认为2
* **retryAnotherBrokerWhenNotStoreOK**: 消息重试时选择另外一个Broker时，是否不等待存储结果就返回，默认false
* **maxMessageSize**: 允许发送的消息的最大长度，默认4M，最大可以到达2^32-1

### RocketMQ生产者的启动流程

1. 检查producerGroup是否合法，并改变生产者的instanceName为进程ID，这样可以防止instanceName的冲突
2. 创建MQClientInstance实例，**整个JVM实例中只存在一个MQClientManager实例，维护一个MQClientInstance缓存表ConcurrentMap<String clientId, MQClientInstance>**，一个clientId只会创建一个MQClientInstance，其中clientId为**客户端IP+instance+(unitname可选)**

```markdown
思考题: 那么这会不会导致一个问题，若clientId相同，程序会不会混乱?

解答: 
如果instance为默认值DEFAULT的话，RocketMQ会自动将其修改为进程的ID，从而避免了这种情况
但同一个JVM的不同消费者和不同生产者在启动时获得的MQClientInstance为同一个
```

3. 向MQClientInstance注册，将当前的生产者加入到MQClientInstance管理中，方便后续调用网络请求，进行心跳检测等
4. 启动MQClientInstance，**如果已经启动，则本次启动不会真正执行**

### RocketMQ消息发送的基本流程

1. 确保生产者处于运行状态，然后验证消息长度是否合法，**具体的规范是消息的主题名称，消息体不能为空，消息长度不能为0且默认不超过最大长度maxMessageSize**
2. 查找路由信息，首先要先获取**主题topic**的路由信息，只有获取了这些消息才能直到消息应该被发送到哪一个具体的Broker中。如果生产者中缓存了topic的路由信息，如果该路由信息包含了消息队列，则直接返回该路由信息，如果没有缓存或者没有包含MQ，则向NameServer查询该topic的路由信息。如果最终都没有找到，那么直接抛出异常，关于RocketMQ如何缓存路由信息，请看文章[《RocketMQ源码阅读——消息发送之主题路由的查找》]()
3. 选择消息队列，根据路由信息选择消息队列，返回的消息队列按照broker，序号排列。例如topicA在broker-a,broker-b上分别创建了4个MQ，那么返回的消息队列如下列json字符串所示，同时消息的发送端采用的是**失败重试机制，并且一次消息的发送失败会让RocketMQ在选择下次消息发送的broker时规避上次MessageQueue所在的broker，否则还是可能会失败**，关于broker的故障处理，请看[《RocketMQ源码阅读——Broker故障处理》]()

```json
[
    {
        "broker-Name": "broker-a",
        "queueId": 0
    }, 
    {
        "broker-Name": "broker-a",
        "queueId": 1
    }
]
```

4. 消息发送，消息发送的核心入口API为: DefaultMQProducerImpl#sendKernelImpl，关于RocketMQ的消息发送源码，请看[《RocketMQ源码阅读——消息发送》]()


