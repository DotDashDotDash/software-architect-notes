## RocketMQ事务消息源码阅读

### 前言

学习RocketMQ的都知道，RocketMQ在事务方面采取的措施是将大事务分解成小事务+异步的方式执行的，这样的话，对于体量不断增长的业务，可以大大缩短事务的执行时间，同时如果小事务发生错误，可以通过重试或者其他手段防止整个事务的回滚(当然，如果小事务一直执行失败，整个事务仍然是要回滚的)，解耦代码。对于事务消息的好奇心，让我产生了阅读RocketMQ事务消息源码的兴趣。

在阅读源码之前，请带着如下疑问:

* 事务消息是如何被发送的？
* 事务消息是如何回滚或者提交的？
* 事务消息如何回查事务的状态？

### 事务消息的实现思想

**RocketMQ事务消息基于两阶段提交和定时事务状态回查来决定消息最终是回滚还是提交**

1. 应用程序在事物内部完成相关业务数据落库之后，需要**同步调用RocketMQ消息发送接口，发送状态为prepare的消息。** 消息发送成功之后，RocketMQ会回调RocketMQ消息发送者的事件监听程序，记录消息的**本地事务状态**， 该相关标记与本地业务属于同一个事务，确保消息发送与本地事务的原子性
2. RocketMQ在收到prepare的消息时，**会首先备份原消息的topic和consumerQueue，然后将消息存储在topic为RMQ_SYS_TRANS_HALF_TOPIC的consumerQueue中**
3. RocketMQ开启一个定时任务，消费RMQ_SYS_TRANS_HALF_TOPIC的消息，向消息发送端**发起消息事务状态回查**， 应用程序根据保存的事务状态回馈给消息服务器事务的状态(commit, rollback，unknown)
   * **unknown**: 等待下一次回查
   * **commit**: 提交
   * **rollback**: 回滚

### 事务消息发送的流程

RocketMQ事务消息的发送由TransactionMQProducer完成，流程大概如下图所示:

<div align=center><img src="/assets/ro8.png"/></div>

TransactionMQProducer内置了一个TransactionListener，用于事务的监听，主要定义实现本地事务状态执行和本地事务状态回查两个接口；同时还有一个ExecutorService，用于事务状态回查异步执行线程池，**注意，事务的状态回查是异步执行的！并且是定时任务！！！**

来到事务消息发送的API：TransactionMQProducer#sendMessageInTransacion

```java
//TransactionMQProducer.java

public TransactionSendResult sendMessageInTransaction(final Message msg, final Object arg) throws MQClientException{
    //首先检事务监听器，监听器为null的话，也就无法进行后面的本地事务执行状态和事务状态回查
    if(null == this.transactionListener){
        throw new MQClientException(...)
    }

    //调用默认子实现类执行事务消息的发送
    return this.defaultMQProducerImpl.sendMessageInTransaction(msg, transactionListener, arg);
}
```

```java
//DefaultMQProducerImpl#sendMessageInTransaction

SendResult sendResult = null;

//为消息添加属性
MessageAccessor.putProperty(msg, MessageConst.PROPERTY_TRANSACTION_PREPARED, "true");
MessageAccessor.putProperty(msg, MessageConst.PROPERTY_PRODUCER_GROUP, this.defaultMQProducer.getProducerGroup());

try{
    sendResult = this.send(msg);  //可以看到，这里发送事务消息是同步发送half消息
}catch(Exception e){
    //...
}

//处理发送half消息
//首先设置本地的事务执行状态为UNKNOWN，以便后面的事务消息状态回查
LocalTransactionState localTransactionState = LocalTransactionState.UNKNOW; 
switch (sendResult.getSendStatus()) {
         // 发送【Half消息】成功，执行【本地事务】逻辑
         case SEND_OK: {
             try {
                 if (sendResult.getTransactionId() != null) { // 事务编号。目前开源版本暂时没用到，猜想ONS在使用。
                     msg.putUserProperty("__transactionId__", sendResult.getTransactionId());
                 }
 
                 // 执行【本地事务】逻辑
                 localTransactionState = tranExecuter.executeLocalTransactionBranch(msg, arg);
                 if (null == localTransactionState) {
                     localTransactionState = LocalTransactionState.UNKNOW;
                 }
 
                 if (localTransactionState != LocalTransactionState.COMMIT_MESSAGE) {
                     log.info("executeLocalTransactionBranch return {}", localTransactionState);
                     log.info(msg.toString());
                 }
             } catch (Throwable e) {
                 log.info("executeLocalTransactionBranch exception", e);
                 log.info(msg.toString());
                 localException = e;
             }
         }
         break;
         // 发送【Half消息】失败，标记【本地事务】状态为回滚
         case FLUSH_DISK_TIMEOUT:
         case FLUSH_SLAVE_TIMEOUT:
         case SLAVE_NOT_AVAILABLE:
             localTransactionState = LocalTransactionState.ROLLBACK_MESSAGE;
             break;
         default:
             break;
     }
```

**看了上面的源码，你可能会产生一个疑问——为什么要设置消息的生产者组？** 目的是在查询事务消息本地状态的时候，从该生产者组中随机选择一个消息生产者即可，然后通过同步调用的方式向RocketMQ发送消息，就类似于你要坐车去另外一个地方，我把你(事务消息)送到一个停车区(生产者组)，等到适当的时候(取决于策略)你就可以坐车去到你想要的目的地

事务消息发送之后，根据事务消息发送的结果SendResult结束消息的发送

```java
//DefaultMQProducerImpl#sendMessageInTransaction

try{
    this.endTransaction(sendResult, localTransactionState, localException);
}catch(Exception e){
    //...
}
```

**由于this.endTransaction的执行，其业务事务并没有提交，故在使用事务消息监听器TransactionListener#executeLocalTransaction的时候，应该返回UNKNOWN以便后面的事务消息状态回查，之后再决定提交或者回滚**

事务消息在发送到消息队列之后，会变更topic主题，RocketMQ消费端并不会消费该事务消息，**直到事务处于被提交的状态，会恢复原消息主题，进而被消费者消费**

### 事务消息的提交或者回滚

RocketMQ会根据消息所属的消息队列获取Broker的IP和端口信息，然后发送结束事务命令，关键就是根据本地执行的事务状态分别发送commit, rollback或者unknown命令。Broker服务器端结束事务处理器为EndTransactionProcessor：

```java
//EndTransactionProcessor.java

public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {
    final RemotingCommand response = RemotingCommand.createResponseCommand(null);
     final EndTransactionRequestHeader requestHeader = (EndTransactionRequestHeader) request.decodeCommandCustomHeader(EndTransactionRequestHeader.class);

     //打印日志（只处理 COMMIT / ROLLBACK）
     //...
 
     // 查询提交的消息
     final MessageExt msgExt = this.brokerController.getMessageStore().lookMessageByOffset(requestHeader.getCommitLogOffset());
     if (msgExt != null) {
         // 省略代码 =》校验消息
 
         // 生成消息
         MessageExtBrokerInner msgInner = this.endMessageTransaction(msgExt);
         msgInner.setSysFlag(MessageSysFlag.resetTransactionValue(msgInner.getSysFlag(), requestHeader.getCommitOrRollback()));
         msgInner.setQueueOffset(requestHeader.getTranStateTableOffset());
         msgInner.setPreparedTransactionOffset(requestHeader.getCommitLogOffset());
         msgInner.setStoreTimestamp(msgExt.getStoreTimestamp());
         if (MessageSysFlag.TRANSACTION_ROLLBACK_TYPE == requestHeader.getCommitOrRollback()) {
             msgInner.setBody(null);
         }
 
         // 存储生成消息
         final MessageStore messageStore = this.brokerController.getMessageStore();
         final PutMessageResult putMessageResult = messageStore.putMessage(msgInner);
 
         // 处理存储结果
         if (putMessageResult != null) {
             switch (putMessageResult.getPutMessageStatus()) {
                 // Success
                 case PUT_OK:
                 case FLUSH_DISK_TIMEOUT:
                 case FLUSH_SLAVE_TIMEOUT:
                 case SLAVE_NOT_AVAILABLE:
                     response.setCode(ResponseCode.SUCCESS);
                     response.setRemark(null);
                     break;
                 // Failed
                 case CREATE_MAPEDFILE_FAILED:
                     response.setCode(ResponseCode.SYSTEM_ERROR);
                     response.setRemark("create maped file failed.");
                     break;
                 case MESSAGE_ILLEGAL:
                 case PROPERTIES_SIZE_EXCEEDED:
                     response.setCode(ResponseCode.MESSAGE_ILLEGAL);
                     response.setRemark("the message is illegal, maybe msg body or properties length not matched. msg body length limit 128k, msg properties length limit 32k.");
                     break;
                 case SERVICE_NOT_AVAILABLE:
                     response.setCode(ResponseCode.SERVICE_NOT_AVAILABLE);
                     response.setRemark("service not available now.");
                     break;
                 case OS_PAGECACHE_BUSY:
                     response.setCode(ResponseCode.SYSTEM_ERROR);
                     response.setRemark("OS page cache busy, please try another machine");
                     break;
                 case UNKNOWN_ERROR:
                     response.setCode(ResponseCode.SYSTEM_ERROR);
                     response.setRemark("UNKNOWN_ERROR");
                     break;
                 default:
                     response.setCode(ResponseCode.SYSTEM_ERROR);
                     response.setRemark("UNKNOWN_ERROR DEFAULT");
                     break;
             }
 
             return response;
         } else {
             response.setCode(ResponseCode.SYSTEM_ERROR);
             response.setRemark("store putMessage return null");
         }
     } else {
         response.setCode(ResponseCode.SYSTEM_ERROR);
         response.setRemark("find prepared transaction message failed");
         return response;
     }
 
     return response;
}
```

如果结束事务动作为提交事务，则执行提交事务逻辑，逻辑如下:

1. 从结束事务请求中获取消息的物理偏移量(commitlogOffset)
2. 恢复消息的主题，消费队列，构建新的消息对象
3. 然后将消息再次存储在commitlog文件中，此时消费的主题是正常的业务方发送的topic，消费队列也恢复
4. 转发该消息到对应的消息消费队列当中，供消费者消费
5. 消息存储之后，**删除prepare消息，然而并不是真正的删除！！！而是将prepare消息存储到RMQ_SYS_TRANS_OP_HALF_TOPIC中，表示该事务消息已经处理过(commit或者rollback)** ，为未处理的事务进行事务回查提供依据

**事务的rollback与commit的区别就是无需将消息恢复原主题，直接删除prepare消息即可，同样需要存储在RMQ_SYS_TRANS_OP_HALF_TOPIC中，表示已经处理过消息**

### 事务消息的回查事务状态

这部分内容比较多，暂时不写了