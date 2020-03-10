## Hystrix原理与实战

### 为什么需要Hystrix

分布式系统环境下，服务间类似依赖非常常见，一个业务调用通常依赖多个基础服务。如下图，对于同步调用，当库存服务不可用时，商品服务请求线程被阻塞，当有大批量请求调用库存服务时，最终可能导致整个商品服务资源耗尽，无法继续对外提供服务。并且这种不可用可能沿请求调用链向上传递，这种现象被称为**雪崩效应**

<div align=center><img src="/assets/h1.png"/></div>

<div align=center><img src="/assets/h2.png"/></div>

### 常见服务雪崩问题

* 硬件故障：如服务器宕机，机房断电，光纤被挖断等。
* 流量激增：如异常流量，重试加大流量等。
* 缓存穿透：一般发生在应用重启，所有缓存失效时，以及短时间内大量缓存失效时。大量的缓存不命中，使请求直击后端服务，造成服务提供者超负荷运行，引起服务不可用。
* 程序BUG：如程序逻辑导致内存泄漏，JVM长时间FullGC等。
* 同步等待：服务间采用同步调用模式，同步等待造成的资源耗尽

### Hystrix的设计目标

* 对来自依赖的延迟和故障进行防护和控制——这些依赖通常都是通过网络访问的
* 阻止故障的连锁反应
* 快速失败并迅速恢复
* 回退并优雅降级
* 提供近实时的监控与告警

### Hystrix遵循的设计目标

* 防止任何单独的依赖耗尽资源（线程）
* 过载立即切断并快速失败，防止排队
* 尽可能提供回退以保护用户免受故障
* 使用隔离技术（例如隔板，泳道和断路器模式）来限制任何一个依赖的影响
* 通过近实时的指标，监控和告警，确保故障被及时发现
* 通过动态修改配置属性，确保故障及时恢复
* 防止整个依赖客户端执行失败，而不仅仅是网络通信

### Hystrix实战

> #### 服务提供者单侧使用fallback

* 首先是业务逻辑类`PaymentService`

```java
@Service
public class PaymentService{

    public String paymentStatusOk(Integer id){
        return Thread.currentThread().getName() + " payment status ok.";
    }

    public String paymentStatueTimeout(Integer id){
        try{
            Thread.sleep(10 * 1000);
        }catch(Exception e){
            e.printStackTrace();
        }
        return Thread.currentThread().getName() + " payment status timeout.";
    }
}
```

* 控制器`PaymentController`

```java
@RestController
@Slf4j
public class PaymentController{

      @Resource
      private PaymentService paymentService;

      @GetMapping("/payment/hystrix/ok/{id}")
      public String paymentStatusOk(@PathVariable("id") Integer id){
          return paymentService.paymentStatusOk(id);
      }

      @HystrixCommand(
          fallbackMethod = "paymentTimeoutHandler",
          commandProperties = {
              @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds" value = "3000")
          })
      @GetMapping("/payment/hystrix/timeout/{id}")
      public String paymentStatusTimeout(@PathVariable("id") Integer id){
          return paymentService.paymentStatusTimeout(id);
      } 

      public String paymentTimeoutHandler(){
          return "此前业务繁忙，这里是fallback方法";
      }      
}
```

* 需要在主启动类配置`Hystrix`生效

```java
@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker //需要配置此项
public class PaymentHystrixApp8001{
    public static void main(String[] args){
        SpringApplication.run(PaymentHystrixApp8001.class, args);
    }
}
```

> #### 消费者Feign调用fallback

* 在`application.yml`中开启功能

```yml
feign:
  hystrix:
    enabled: true
```

* 主启动类配置

```java
@SpringBootApplication
@EnbaleEurekaClient
@EnableCircuitBreaker
public class OrderFeignHystrixApp80{
    public static void main(String[] args){
        SpringApplication.run(OrderFeignHystrixApp80.class, args);
    }
}
```

* `Feign`消费接口

```java
@Component //需要注入到容器中
public interface PaymentFeignService{
    
    @Resource
    PaymentService paymentService;

    @GetMapping("/payment/hystrix/get/{id}")
    paymentService.paymentStatusOk(@PathVaraible("id") Integer id);

    @GetMapping("/payment/hystrix/timeout/{id}")
    paymentService.paymentStatusTimeout(@PathVariable("id") Integer id);
}
```

* 业务控制器

```java
@RestController
@Slf4j
public class PaymentController{

      @Resource
      private PaymentFeignService paymentService;

      @GetMapping("/consumer/payment/hystrix/ok/{id}")
      public String paymentStatusOk(@PathVariable("id") Integer id){
          return paymentService.paymentStatusOk(id);
      }

      @HystrixCommand(
          fallbackMethod = "paymentTimeoutHandler",
          commandProperties = {
              @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds" value = "3000")
          })
      @GetMapping("/consumer/payment/hystrix/timeout/{id}")
      public String paymentStatusTimeout(@PathVariable("id") Integer id){
          return paymentService.paymentStatusTimeout(id);
      } 

      public String paymentTimeoutHandler(){
          return "此前业务繁忙，这里是fallback方法";
      }      
}
```

### Hystrix的处理流程

<div align=center><img src="/assets/h3.png"/></div>

Hystrix整个工作流如下：

1. 构造一个 HystrixCommand或HystrixObservableCommand对象，用于封装请求，并在构造方法配置请求被执行需要的参数；
2. 执行命令，Hystrix提供了4种执行命令的方法，后面详述；
3. 判断是否使用缓存响应请求，若启用了缓存，且缓存可用，直接使用缓存响应请求。Hystrix支持请求缓存，但需要用户自定义启动；
4. 判断熔断器是否打开，如果打开，跳到第8步；
5. 判断线程池/队列/信号量是否已满，已满则跳到第8步；
6. 执行HystrixObservableCommand.construct()或HystrixCommand.run()，如果执行失败或者超时，跳到第8步；否则，跳到第9步；
7. 统计熔断器监控指标；
8. 走Fallback备用逻辑
9. 返回请求响应

从流程图上可知道，第5步线程池/队列/信号量已满时，还会执行第7步逻辑，更新熔断器统计信息，而第6步无论成功与否，都会更新熔断器统计信息。

### Hystrix容错机制

Hystrix的容错主要是通过添加容许延迟和容错方法，帮助控制这些分布式服务之间的交互。 还通过隔离服务之间的访问点，阻止它们之间的级联故障以及提供回退选项来实现这一点，从而提高系统的整体弹性。Hystrix主要提供了以下几种容错方法：

* 资源隔离
* 熔断
* 降级

下面我们详细谈谈这几种容错机制

#### 资源隔离

资源隔离主要指对线程的隔离。Hystrix提供了两种线程隔离方式：**线程池**和**信号量**。

> #### 线程池隔离

Hystrix通过命令模式对发送请求的对象和执行请求的对象进行解耦，将不同类型的业务请求封装为对应的命令请求。如订单服务查询商品，查询商品请求->商品Command；商品服务查询库存，查询库存请求->库存Command。并且为每个类型的Command配置一个线程池，当第一次创建Command时，根据配置创建一个线程池，并放入ConcurrentHashMap，如商品Command：

```java
final static ConcurrentHashMap<String, HystrixThreadPool> threadPools = new ConcurrentHashMap<String, HystrixThreadPool>();
...
if (!threadPools.containsKey(key)) {
    threadPools.put(key, new HystrixThreadPoolDefault(threadPoolKey, propertiesBuilder));
}
```

后续查询商品的请求创建Command时，将会重用已创建的线程池。线程池隔离之后的服务依赖关系

<div align=center><img src="/assets/h4.png"/></div>

通过将发送请求线程与执行请求的线程分离，可有效防止发生级联故障。当线程池或请求队列饱和时，Hystrix将拒绝服务，使得请求线程可以快速失败，从而避免依赖问题扩散。

**线程池隔离的优缺点**

* 保护应用程序以免受来自依赖故障的影响，指定依赖线程池饱和不会影响应用程序的其余部分。
* 当引入新客户端lib时，即使发生问题，也是在本lib中，并不会影响到其他内容。
* 当依赖从故障恢复正常时，应用程序会立即恢复正常的性能。
* 当应用程序一些配置参数错误时，线程池的运行状况会很快检测到这一点（通过增加错误，延迟，超时，拒绝等），同时可以通过动态属性进行实时纠正错误的参数配置。
* 如果服务的性能有变化，需要实时调整，比如增加或者减少超时时间，更改重试次数，可以通过线程池指标动态属性修改，而且不会影响到其他调用请求。
* 除了隔离优势外，hystrix拥有专门的线程池可提供内置的并发功能，使得可以在同步调用之上构建异步门面（外观模式），为异步编程提供了支持（Hystrix引入了Rxjava异步框架）

**注意：尽管线程池提供了线程隔离，我们的客户端底层代码也必须要有超时设置或响应线程中断，不能无限制的阻塞以致线程池一直饱和。**

> #### 信号量隔离

**当依赖延迟极低的服务时，线程池隔离技术引入的开销超过了它所带来的好处。这时候可以使用信号量隔离技术来代替**，通过设置信号量来限制对任何给定依赖的并发调用量。

<div align=center><img src="/assets/h5.png"/></div>

使用线程池时，发送请求的线程和执行依赖服务的线程不是同一个，而使用信号量时，发送请求的线程和执行依赖服务的线程是同一个，都是发起请求的线程。先看一个使用信号量隔离线程的示例：

```java
public class QueryByOrderIdCommandSemaphore extends HystrixCommand<Integer> {
    private final static Logger logger = LoggerFactory.getLogger(QueryByOrderIdCommandSemaphore.class);
    private OrderServiceProvider orderServiceProvider;

    public QueryByOrderIdCommandSemaphore(OrderServiceProvider orderServiceProvider) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("orderService"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("queryByOrderId"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withCircuitBreakerRequestVolumeThreshold(10)////至少有10个请求，熔断器才进行错误率的计算
                        .withCircuitBreakerSleepWindowInMilliseconds(5000)//熔断器中断请求5秒后会进入半打开状态,放部分流量过去重试
                        .withCircuitBreakerErrorThresholdPercentage(50)//错误率达到50开启熔断保护
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                        .withExecutionIsolationSemaphoreMaxConcurrentRequests(10)));//最大并发请求量
        this.orderServiceProvider = orderServiceProvider;
    }

    @Override
    protected Integer run() {
        return orderServiceProvider.queryByOrderId();
    }

    @Override
    protected Integer getFallback() {
        return -1;
    }
}
```

由于Hystrix默认使用线程池做线程隔离，使用信号量隔离需要显示地将属性execution.isolation.strategy设置为ExecutionIsolationStrategy.SEMAPHORE，同时配置信号量个数，默认为10。客户端需向依赖服务发起请求时，首先要获取一个信号量才能真正发起调用，由于信号量的数量有限，当并发请求量超过信号量个数时，后续的请求都会直接拒绝，进入fallback流程。

信号量隔离主要是通过控制并发请求量，防止请求线程大面积阻塞，从而达到限流和防止雪崩的目的。

线程池和信号量都可以做线程隔离，但各有各的优缺点和支持的场景，对比如下：

|线程切换	|支持异步|	支持超时|	支持熔断|	限流|	开销|
|:---:|:---:|:---:|:---:|:---:|:---:|
|信号量|	否|	否|	否|	是|	是|	小|
|线程池|	是|	是|	是|	是|	是|	大|

线程池和信号量都支持熔断和限流。相比线程池，信号量不需要线程切换，因此避免了不必要的开销。但是信号量不支持异步，也不支持超时，也就是说当所请求的服务不可用时，信号量会控制超过限制的请求立即返回，但是已经持有信号量的线程只能等待服务响应或从超时中返回，即可能出现长时间等待。线程池模式下，当超过指定时间未响应的服务，Hystrix会通过响应中断的方式通知线程立即结束并返回。

#### 熔断

现实生活中，可能大家都有注意到家庭电路中通常会安装一个保险盒，当负载过载时，保险盒中的保险丝会自动熔断，以保护电路及家里的各种电器，这就是熔断器的一个常见例子。Hystrix中的熔断器(Circuit Breaker)也是起类似作用，Hystrix在运行过程中会向每个commandKey对应的熔断器报告成功、失败、超时和拒绝的状态，熔断器维护并统计这些数据，并根据这些统计信息来决策熔断开关是否打开。如果打开，熔断后续请求，快速返回。隔一段时间（默认是5s）之后熔断器尝试半开，放入一部分流量请求进来，相当于对依赖服务进行一次健康检查，如果请求成功，熔断器关闭。

> #### 熔断器重要参数

Circuit Breaker主要包括如下6个参数：

1. `circuitBreaker.enabled`
是否启用熔断器，默认是TRUE。

2. `circuitBreaker.forceOpen`
熔断器强制打开，始终保持打开状态，不关注熔断开关的实际状态。默认值FLASE。

3. `circuitBreaker.forceClosed`
熔断器强制关闭，始终保持关闭状态，不关注熔断开关的实际状态。默认值FLASE。

4. `circuitBreaker.errorThresholdPercentage`
错误率，默认值50%，例如一段时间（10s）内有100个请求，其中有54个超时或者异常，那么这段时间内的错误率是54%，大于了默认值50%，这种情况下会触发熔断器打开。

5. `circuitBreaker.requestVolumeThreshold`
默认值20。含义是一段时间内至少有20个请求才进行errorThresholdPercentage计算。比如一段时间了有19个请求，且这些请求全部失败了，错误率是100%，但熔断器不会打开，总请求数不满足20。

6. `circuitBreaker.sleepWindowInMilliseconds`
半开状态试探睡眠时间，默认值5000ms。如：当熔断器开启5000ms之后，会尝试放过去一部分流量进行试探，确定依赖服务是否恢复。

> #### 熔断器工作原理

<div align=center><img src="/assets/h6.png"/></div>

