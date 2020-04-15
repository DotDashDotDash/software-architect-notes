## SpringBoot RocketMQ快速入门(实战篇)

### 引入依赖pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.1.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>lab-31-rocketmq-demo</artifactId>

    <dependencies>
        <!-- 实现对 RocketMQ 的自动化配置 -->
        <dependency>
            <groupId>org.apache.rocketmq</groupId>
            <artifactId>rocketmq-spring-boot-starter</artifactId>
            <version>2.0.4</version>
        </dependency>

        <!-- 方便等会写单元测试 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

### 配置文件application.yml

```yml
# rocketmq 配置项，对应 RocketMQProperties 配置类
rocketmq:
  name-server: 127.0.0.1:9876 # RocketMQ Namesrv
  # Producer 配置项
  producer:
    group: demo-producer-group # 生产者分组
    send-message-timeout: 3000 # 发送消息超时时间，单位：毫秒。默认为 3000 。
    compress-message-body-threshold: 4096 # 消息压缩阀值，当消息体的大小超过该阀值后，进行消息压缩。默认为 4 * 1024B
    max-message-size: 4194304 # 消息体的最大允许大小。。默认为 4 * 1024 * 1024B
    retry-times-when-send-failed: 2 # 同步发送消息时，失败重试次数。默认为 2 次。
    retry-times-when-send-async-failed: 2 # 异步发送消息时，失败重试次数。默认为 2 次。
    retry-next-server: false # 发送消息给 Broker 时，如果发送失败，是否重试另外一台 Broker 。默认为 false
    access-key: # Access Key ，可阅读 https://github.com/apache/rocketmq/blob/master/docs/cn/acl/user_guide.md 文档
    secret-key: # Secret Key
    enable-msg-trace: true # 是否开启消息轨迹功能。默认为 true 开启。可阅读 https://github.com/apache/rocketmq/blob/master/docs/cn/msg_trace/user_guide.md 文档
    customized-trace-topic: RMQ_SYS_TRACE_TOPIC # 自定义消息轨迹的 Topic 。默认为 RMQ_SYS_TRACE_TOPIC 。
  # Consumer 配置项
  consumer:
    listeners: # 配置某个消费分组，是否监听指定 Topic 。结构为 Map<消费者分组, <Topic, Boolean>> 。默认情况下，不配置表示监听。
      test-consumer-group:
        topic1: false # 关闭 test-consumer-group 对 topic1 的监听消费
```

### 自定义要发送的消息Message

```java
public class MyMessage{

    //消息所在的topic
    public static final String TOPIC = "TOPIC-01";

    //消息id
    private Integer id;

    //省略getter setter
}
```

### 自定义消息生产者Producer

```java
@Component
public class MyMessageProducer{

    @Autowired
    private RocketMQTemplate mqTemplate;

    private MyMessage initMessage(Integer id){
        MyMessage msg = new MyMessage();
        msg.setId(id);
        return msg;
    }

    public SendResult syncSend(Integer id){
        MyMessage msg = initMessage(id);
        return mqTemplate.syncSend(MyMessage.TOPIC, msg);
    }

    public void asyncSend(Integer id, SendCallback callback){
        MyMessage msg = initMessage(id);
        mqTemplate.asyncSend(MyMessage.TOPIC, msg, callback);
    }

    public void oneWaySend(Integer id){
        MyMessage msg = initMessage(id);
        mqTemplate.sendOneWay(MyMessage.TOPIC, msg);
    }
}
```

RocketMQTemplate 类，它继承 Spring Messaging 定义的 AbstractMessageSendingTemplate 抽象类，以达到融入 Spring Messaging 体系中。

在 RocketMQTemplate 中，会创建一个 RocketMQ DefaultMQProducer 生产者 producer ，所以 RocketMQTemplate 后续的各种发送消息的方法，都是使用它。当然，因为 RocketMQTemplate 的封装，所以我们可以像使用 Spring Messaging 一样的方式，进行消息的发送，而无需直接使用 RocketMQ 提供的 Producer 发送消息。

RocketMQ-Spring 的默认使用 MappingJackson2MessageConverter 或 MappingFastJsonMessageConverter ，即使用 JSON 格式序列化和反序列化 Message 消息内容。

### 自定义消息消费者Consumer

```java
@Component
@RocketMQMessageListener(
    topic = MyMessage.TOPIC,
    consumerGroup = "consumer-group-" + MyMessage.TOPIC
)
public class MyMessageListener implements RocketMQListener<MyMessage>{

    @Override
    public void onMessage(MyMessage msg){
        //处理监听逻辑
    }
}
```

* 在类上，添加了 @RocketMQMessageListener 注解，声明消费的 Topic 是 "TOPIC-01" ，消费者分组是 "consumer-group-TOPIC-01" 。**一般情况下，我们建议一个消费者分组，仅消费一个 Topic**。这样做会有两个好处：
  * 每个消费者分组职责单一，只消费一个 Topic 。
  * 每个消费者分组是独占一个线程池，这样能够保证多个 Topic 隔离在不同线程池，保证隔离性，从而避免一个 Topic 消费很慢，影响到另外的 Topic 的消费。
* 实现 RocketMQListener 接口，在 T 泛型里，设置消费的消息对应的类。此处，我们就设置了 MyMessage 类。

还有一种扩展的消息消费者的实现:

```java
@Component
@RocketMQMessageListener(
    topic = MyMessage.TOPIC,
    consumerGroup = "consumer-group-" + MyMessage.TOPIC
)
public class MyMessageListener implements RocketMQListener<MessageExt>{

    @Override
    public void onMessage(MyMessage msg){
        //处理监听逻辑
    }
}
```

使用MessageExt扩展自定义Message，能够获取到更多消息的信息，例如所属队列，创建时间等等，但是消息的实体body需要自己去反序列化，一般情况下不推荐使用这种方法。

在集群模式下，同一个消费者组的消费者平摊同一个topic的信息，也就是一个topic的消息只能被同一个消费者组的消费者消费，但是不同的消费者组可以重复消费同一个topic的信息。

在上面的代码中，我们通过添加@RocketMQMessageListener来设置消费者，该注解常用的属性如下:

```java
/**
 * Consumer 所属消费者分组
 *
 * Consumers of the same role is required to have exactly same subscriptions and consumerGroup to correctly achieve
 * load balance. It's required and needs to be globally unique.
 *
 * See <a href="http://rocketmq.apache.org/docs/core-concept/">here</a> for further discussion.
 */
String consumerGroup();

/**
 * 消费的 Topic
 *
 * Topic name.
 */
String topic();

/**
 * 选择器类型。默认基于 Message 的 Tag 选择。
 *
 * Control how to selector message.
 *
 * @see SelectorType
 */
SelectorType selectorType() default SelectorType.TAG;
/**
 * 选择器的表达式。
 * 设置为 * 时，表示全部。
 *
 * 如果使用 SelectorType.TAG 类型，则设置消费 Message 的具体 Tag 。
 * 如果使用 SelectorType.SQL92 类型，可见 https://rocketmq.apache.org/rocketmq/filter-messages-by-sql92-in-rocketmq/ 文档
 *
 * Control which message can be select. Grammar please see {@link SelectorType#TAG} and {@link SelectorType#SQL92}
 */
String selectorExpression() default "*";

/**
 * 消费模式。可选择并发消费，还是顺序消费。
 *
 * Control consume mode, you can choice receive message concurrently or orderly.
 */
ConsumeMode consumeMode() default ConsumeMode.CONCURRENTLY;

/**
 * 消息模型。可选择是集群消费，还是广播消费。
 *
 * Control message mode, if you want all subscribers receive message all message, broadcasting is a good choice.
 */
MessageModel messageModel() default MessageModel.CLUSTERING;

/**
 * 消费的线程池的最大线程数
 *
 * Max consumer thread number.
 */
int consumeThreadMax() default 64;

/**
 * 消费单条消息的超时时间
 *
 * Max consumer timeout, default 30s.
 */
long consumeTimeout() default 30000L;
```

@RocketMQMessageListener不常用的注解如下:

```java
// 默认从配置文件读取的占位符
String NAME_SERVER_PLACEHOLDER = "${rocketmq.name-server:}";
String ACCESS_KEY_PLACEHOLDER = "${rocketmq.consumer.access-key:}";
String SECRET_KEY_PLACEHOLDER = "${rocketmq.consumer.secret-key:}";
String TRACE_TOPIC_PLACEHOLDER = "${rocketmq.consumer.customized-trace-topic:}";
String ACCESS_CHANNEL_PLACEHOLDER = "${rocketmq.access-channel:}";

/**
 * The property of "access-key".
 */
 String accessKey() default ACCESS_KEY_PLACEHOLDER;
 /**
 * The property of "secret-key".
 */
String secretKey() default SECRET_KEY_PLACEHOLDER;

/**
 * Switch flag instance for message trace.
 */
boolean enableMsgTrace() default true;
/**
 * The name value of message trace topic.If you don't config,you can use the default trace topic name.
 */
String customizedTraceTopic() default TRACE_TOPIC_PLACEHOLDER;

/**
 * Consumer 连接的 RocketMQ Namesrv 地址。默认情况下，使用 `rocketmq.name-server` 配置项即可。
 *
 * 如果一个项目中，Consumer 需要使用不同的 RocketMQ Namesrv ，则需要配置该属性。
 *
 * The property of "name-server".
 */
String nameServer() default NAME_SERVER_PLACEHOLDER;

/**
 * 访问通道。目前有 LOCAL 和 CLOUD 两种通道。
 *
 * LOCAL ，指的是本地部署的 RocketMQ 开源项目。
 * CLOUD ，指的是阿里云的 ONS 服务。具体可见 https://help.aliyun.com/document_detail/128585.html 文档。
 *
 * The property of "access-channel".
 */
String accessChannel() default ACCESS_CHANNEL_PLACEHOLDER;
```

RocketMQ-Spring 考虑到开发者可能需要连接多个不同的 RocketMQ 集群，所以提供了 @ExtRocketMQTemplateConfiguration 注解，实现配置连接不同 RocketMQ 集群的 Producer 的 RocketMQTemplate Bean 对象。

@ExtRocketMQTemplateConfiguration 注解的具体属性，和我们在 application.yml 中的 rocketmq.producer 配置项是一致的，就不重复赘述啦。

@ExtRocketMQTemplateConfiguration 注解的简单使用示例，代码如下：

```java
@ExtRocketMQTemplateConfiguration(nameServer = "${demo.rocketmq.extNameServer:demo.rocketmq.name-server}")
public class ExtRocketMQTemplate extends RocketMQTemplate {
}
```

* 在类上，添加 @ExtRocketMQTemplateConfiguration 注解，并设置连接的 RocketMQ Namesrv 地址。
* 同时，需要继承 RocketMQTemplate 类，从而使我们可以直接使用 @Autowire 或 @Resource 注解，注入 RocketMQTemplate Bean 属性。

在一些业务场景下，我们希望使用 Producer 批量发送消息，提高发送性能。在 RocketMQTemplate 中，提供了一个方法方法批量发送消息的方法。代码如下：

```java
// RocketMQTemplate.java

public <T extends Message> SendResult syncSend(String destination, Collection<T> messages, long timeout) {
    // ... 省略具体代码实现
}
```

通过方法参数 destination 可知，必须发送相同 Topic 的消息。

**要注意方法参数 messages ，每个集合的元素必须是 Spring Messaging 定义的 Message 消息。RocketMQTemplate 重载了非常多的 #syncSend(...) 方法，一定要小心**

通过方法名可知，这个是同步批量发送消息。

有一点要注意，虽然是批量发送多条消息，但是是以所有消息加起来的大小，不能超过消息的最大大小的限制，而不是按照单条计算。所以，一次性发送的消息特别多，还是需要分批的进行批量发送。

### 消费重试

RocketMQ 提供消费重试的机制。在消息消费失败的时候，RocketMQ 会通过消费重试机制，重新投递该消息给 Consumer ，让 Consumer 有机会重新消费消息，实现消费成功。

当然，RocketMQ 并不会无限重新投递消息给 Consumer 重新消费，而是在默认情况下，达到 16 次重试次数时，Consumer 还是消费失败时，该消息就会进入到死信队列。

死信队列用于处理无法被正常消费的消息。当一条消息初次消费失败，消息队列会自动进行消息重试；达到最大重试次数后，若消费依然失败，则表明消费者在正常情况下无法正确地消费该消息，此时，消息队列不会立刻将消息丢弃，而是将其发送到该消费者对应的特殊队列中。

**RocketMQ 将这种正常情况下无法被消费的消息称为死信消息（Dead-Letter Message），将存储死信消息的特殊队列称为死信队列（Dead-Letter Queue）。在 RocketMQ 中，可以通过使用 console 控制台对死信队列中的消息进行重发来使得消费者实例再次进行消费**。

每条消息的失败重试，是有一定的间隔时间。实际上，消费重试是基于「5. 定时消息」 来实现，第一次重试消费按照延迟级别为 3 开始。所以，默认为 16 次重试消费，也非常好理解，毕竟延迟级别最高为 18 呀。

**不过要注意，只有集群消费模式下，才有消息重试。**

### 顺序消息

RocketMQ 提供了两种顺序级别：

* 普通顺序消息 ：Producer 将相关联的消息发送到相同的消息队列。
* 完全严格顺序 ：在【普通顺序消息】的基础上，Consumer 严格顺序消费。

目前已知的应用只有数据库 binlog 同步强依赖严格顺序消息，其他应用绝大部分都可以容忍短暂乱序，推荐使用普通的顺序消息。

如下是 RocketMQ 官方文档对这两种顺序级别的定义：

* 普通顺序消费模式下，消费者通过同一个消费队列收到的消息是有顺序的，不同消息队列收到的消息则可能是无顺序的。
* 严格顺序消息模式下，消费者收到的所有消息均是有顺序的。

> #### 顺序消息生产者

**在消息的发送方，可通过RocketMQTemplate#xxxSendOrderly(,,String order,..)实现顺序发送**，同时，需要传入方法参数 hashKey ，作为选择消息队列的键。

Producer 可以根据定义 MessageQueueSelector 消息队列选择策略，选择 Topic 下的队列。目前提供三种策略：

* **SelectMessageQueueByHash** ，基于 hashKey 的哈希值取余，选择对应的队列。
* **SelectMessageQueueByRandom** ，基于随机的策略，选择队列。
* **SelectMessageQueueByMachineRoom** ,`有点看不懂`
* **未使用 MessageQueueSelector 时，采用轮询的策略，选择队列**。

RocketMQTemplate 在发送顺序消息时，**默认采用 SelectMessageQueueByHash 策略**。如此，相同的 hashKey 的消息，就可以发送到相同的 Topic 的对应队列中。这种形式，就是我们上文提到的普通顺序消息的方式。

> #### 顺序消息消费者

```java
@Component
@RocketMQMessageListener(
        topic = Demo06Message.TOPIC,
        consumerGroup = "consumer-group-" + My6Message.TOPIC,
        consumeMode = ConsumeMode.ORDERLY // 设置为顺序消费
)
public class MyConsumer implements RocketMQListener<MyMessage> {
}
```

@RocketMQMessageListener 注解，通过设置了 consumeMode = ConsumeMode.ORDERLY ，表示使用顺序消费。

### 事务消息

* **事务消息**：消息队列 RocketMQ 版提供类似 X/Open XA 的分布式事务功能，通过消息队列 RocketMQ 版事务消息能达到分布式事务的最终一致。
* **半事务消息**：暂不能投递的消息，发送方已经成功地将消息发送到了消息队列 RocketMQ 版服务端，但是服务端未收到生产者对该消息的二次确认，此时该消息被标记成“暂不能投递”状态，处于该种状态下的消息即半事务消息。
* **消息回查**：由于网络闪断、生产者应用重启等原因，导致某条事务消息的二次确认丢失，消息队列 RocketMQ 版服务端通过扫描发现某条消息长期处于“半事务消息”时，需要主动向消息生产者询问该消息的最终状态（Commit 或是 Rollback），该询问过程即消息回查。

消息队列 RocketMQ 版分布式事务消息不仅可以实现应用之间的解耦，又能保证数据的最终一致性。同时，传统的大事务可以被拆分为小事务，不仅能提升效率，还不会因为某一个关联应用的不可用导致整体回滚，从而最大限度保证核心系统的可用性。在极端情况下，如果关联的某一个应用始终无法处理成功，也只需对当前应用进行补偿或数据订正处理，而无需对整体业务进行回滚。

> #### 事务消息的交互过程

<div align=center><img src="/assets/ro1.png"/></div>

**事务消息发送步骤如下：**

1. 发送方将半事务消息发送至消息队列 RocketMQ 版服务端。
2. 消息队列 RocketMQ 版服务端将消息持久化成功之后，向发送方返回 Ack 确认消息已经发送成功，此时消息为半事务消息。
3. 发送方开始执行本地事务逻辑。
4. 发送方根据本地事务执行结果向服务端提交二次确认（Commit 或是 Rollback），服务端收到 Commit 状态则将半事务消息标记为可投递，订阅方最终将收到该消息；服务端收到 Rollback 状态则删除半事务消息，订阅方将不会接受该消息。

**事务消息回查步骤如下：**

1. 在断网或者是应用重启的特殊情况下，上述步骤 4 提交的二次确认最终未到达服务端，经过固定时间后服务端将对该消息发起消息回查。
2. 发送方收到消息回查后，需要检查对应消息的本地事务执行的最终结果。
3. 发送方根据检查得到的本地事务的最终状态再次提交二次确认，服务端仍按照步骤 4 对半事务消息进行操作。

> #### 事务消息Producer示例

```java
@Component
public class MyProducer {

    private static final String TX_PRODUCER_GROUP = "producer-group";

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public TransactionSendResult sendMessageInTransaction(Integer id) {
        // <1> 创建 Message 消息
        Message message = MessageBuilder.withPayload(new MyMessage().setId(id))
                .build();
        // <2> 发送事务消息
        return rocketMQTemplate.sendMessageInTransaction(TX_PRODUCER_GROUP, MyMessage.TOPIC, message,
                id);
    }

    //内置MQ事务监听器，监听事务
    @RocketMQTransactionListener(txProducerGroup = TX_PRODUCER_GROUP)
    public class TransactionListenerImpl implements RocketMQLocalTransactionListener {

      private Logger logger = LoggerFactory.getLogger(getClass());

      @Override
      public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
          // ... local transaction process, return rollback, commit or unknown
          logger.info("[executeLocalTransaction][执行本地事务，消息：{} arg：{}]", msg, arg);
          return RocketMQLocalTransactionState.UNKNOWN;
      }

      @Override
      public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
          // ... check transaction status and return rollback, commit or unknown
          logger.info("[checkLocalTransaction][回查消息：{}]", msg);
          return RocketMQLocalTransactionState.COMMIT;
      }
    }
}
```

* **方法参数 txProducerGroup** ，事务消息的生产者分组。因为 RocketMQ 是回查（请求）指定指定生产分组下的 Producer ，从而获得事务消息的状态，所以一定要正确设置。这里，我们设置了 "producer-group" 。
* **方法参数 destination** ，消息的 Topic + Tag 。
* **方法参数 message** ，消息，没什么特别。
* **方法参数 arg** ，后续我们调用本地事务方法的时候，会传入该 arg 。如果要传递多个方法参数给本地事务的方法，可以通过数组，例如说 Object[]{arg1, arg2, arg3} 这样的形式。

**对于上面的RocketMQ事务监听的内部类，主要进行了以下操作:**

* 实现 #executeLocalTransaction(...) 方法，实现执行本地事务。
  * 注意，这是一个模板方法。在调用这个方法之前，RocketMQTemplate 已经使用 Producer 发送了一条事务消息。然后根据该方法执行的返回的 RocketMQLocalTransactionState 结果，提交还是回滚该事务消息。
  * 这里，我们为了模拟 RocketMQ 回查 Producer 来获得事务消息的状态，所以返回了 RocketMQLocalTransactionState.UNKNOWN 未知状态。
* 实现 #checkLocalTransaction(...) 方法，检查本地事务。
  * 在事务消息长事件未被提交或回滚时，RocketMQ 会回查事务消息对应的生产者分组下的 Producer ，获得事务消息的状态。此时，该方法就会被调用。
  * 这里，我们直接返回 RocketMQLocalTransactionState.COMMIT 提交状态。

**一般来说，有两种方式实现本地事务回查时，返回事务消息的状态:**

**第一种**，通过 msg 消息，获得某个业务上的标识或者编号，然后去数据库中查询业务记录，从而判断该事务消息的状态是提交还是回滚。

**第二种(推荐使用)**，记录 msg 的事务编号，与事务状态到数据库中。

* 第一步，在 #executeLocalTransaction(...) 方法中，先存储一条 id 为 msg 的事务编号，状态为 RocketMQLocalTransactionState.UNKNOWN 的记录。
* 第二步，调用带有事务的业务 Service 的方法。在该 Service 方法中，在逻辑都执行成功的情况下，更新 id 为 msg 的事务编号，状态变更为 RocketMQLocalTransactionState.COMMIT 。这样，我们就可以伴随这个事务的提交，更新 id 为 msg 的事务编号的记录的状为 RocketMQLocalTransactionState.COMMIT ，美滋滋。。
* 第三步，要以 try-catch 的方式，调用业务 Service 的方法。如此，如果发生异常，回滚事务的时候，可以在 catch 中，更新 id 为 msg 的事务编号的记录的状态为 RocketMQLocalTransactionState.ROLLBACK 。 极端情况下，可能更新失败，则打印 error 日志，告警知道，人工介入。

如此三步之后，我们在 #executeLocalTransaction(...) 方法中，就可以通过查找数据库，id 为 msg 的事务编号的记录的状态，然后返回。

> #### 事务消息Consumer示例

消费者模型大体相同

