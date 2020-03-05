## Netty原理与基础

### 什么是Netty

Netty是为了快速开发可维护的高性能，高可扩展，网络服务器和客户端程序而提供的异步事件驱动基础框架和工具。简而言之，Netty是一个Java NIO客户端/服务器框架。Netty极大的简化了TCP, UDP套接字，HTTP Web服务程序的开发。

### 从Netty的Reactor反应器模式开始

**还没有看Reactor反应器模式的文章可以先传送至此处[Reactor反应器模式](Reactor入门(一).md)**

学习Netty的朋友都知道，Reactor反应器模式是贯穿Netty的一种设计模式，是学习Netty的基础，其核心组件分为:

* **channel**: 在NIO中，IO是源自于通道的，IO是和通道强相关的，**某个IO事件，一定数据某个通道**
* **selector**: 在反应器模式中，一个反应器会负责一个线程，通道会被注册到selector中，等到具体IO事件到达的时候会被selector查询到
* **reactor**: selector查询到IO事件，会被分发到具体的Handler业务处理器当中
* **handler**: 真正地处理具体的业务逻辑

具体的Netty对于一次IO事件的处理流程如下:

<div align=center><img src="/assets/n1.png"/></div>

了解了大概的流程，下面就来看看具体的组件

### Netty的核心组件

> #### Channel

`Channel`组件是Netty中非常重要的组件，因为反应器的模式和通道的关系是紧密相关的，Netty为不同的通信协议，不同的同步机制异步机制提供了不同的`Channel`

**但是Netty并没有直接采用NIO的`Channel`，而是自己封装了自己的通道**，Netty除了能够处理NIO，也能处理OIO

对于网络编程而言，最常用的`Channel`组件为`NioSocketChannel`和`NioServerSocketChannel`，拿`NioSocketChannel`来说，其UML继承关系如下所示:

<div align=center><img src="/assets/n2.png"/></div>

> #### EventLoop

在Netty中，EventLoop扮演的角色就是Reactor反应器模式中的`Reactor`，`EventLoop`定义了Netty的核心抽象，用于处理连接的生命周期中所发生的事件

* 一个`EventLoop`拥有一个`Thread线程`
* 一个`EventGroup`包含一个或者多个`EventLoop`
* 所有的`EventLoop`处理的IO事件都在它独有的线程中进行处理
* 一个`Channel`在它的生命周期中只注册到一个`EventLoop`中
* 一个`EventLoop`可能会注册多个`Channel`

<div align=center><img src="/assets/n3.png"/></div>

> #### Handler

在学习NIO中我们知道，可供选择器监控的通道IO事件类型包括:

* 可读: `SelectionKey.OP_READ`
* 可写: `SelectionKey.OP_WRITE`
* 连接: `SelectionKey.OP_CONNECT`
* 接受: `SelectionKey.OP_ACCEPT`

拿`NioEventLoop`举例，其内部定义了一个java Nio类型的`Selector`:

```java
public final class NioEventLoop extends SingleThreadEventLoop {
    /**
     * The NIO {@link Selector}.
     */
    Selector selector;
}
```

内部的Nio的`Selector`将会用于事件的分发，事件分发的最终的目的地就是Netty自己的`Handler`处理器

Netty中的`Handler`分为两大类:

* `ChannelInboundHandler`
* `ChannelOutboundHandler`

<div align=center><img src="/assets/n4.png"/></div>

下面以一次`OP_READ`IO操作说明`ChannelInboundHandler`的一次简单执行流程:

<div align=center><img src="/assets/n5.png"/></div>

由上述可以看到，Netty充分运用了Reactor反应器模式，真正的业务逻辑处理是在`Handler`中进行的，所以我们的应用程序代码主要专注于`Handler`的编写，但是每次写`Handler`都会有大量的代码冗余，以及编写难度巨大，于是Netty为了方便开发环境，提供了出入站的处理器映射器`ChannelInboundHandlerAdapter`和`ChannelOutboundHandlerAdapter`，程序开发者只需要自己继承这两个实现类就能编写相应的出站入站业务逻辑操作

> #### ChannelPipeline

`ChannelPipeline`是Netty为了组织通道和`Handler`处理器实例之间的绑定关系，它像一条管道，将绑定到一个通道的多个`Handler`处理器实例，串在一起，形成一条流水线，`ChannelPipeline`被设计成一个双向链表，所有的`Handler`处理器实例都被包装成了双向链表的节点，被加入到了`ChannelPipeline`中

当`ChannelHander`被创建的时候，它会被自动地加入到`ChannelPipeline`中

下面先来看看流水线的数据结构大概组织形式:

<div align=center><img src="/assets/n6.png"/></div>

上面的图是为了区分出和入站进行简化后的，实际上的双向链表应该是下面的形式：

<div align=center><img src="/assets/n7.png"/></div>

**为什么要组成成双向链表的形式呢?**

双向链表的形式，便于事件的在处理器之间的流动，对于一次入站IO事件会从第一个`InboundHandler`开始处理，当处理完毕之后，**该IO事件可以选择交给下一个`InBoundHandler`处理**，当然，也可以选择不交给

### Bootstrap启动器类

在深入地学习了ChannelPipeline、ChannelHandler和EventLoop之后，你接下来 的问题可能是：“如何将这些部分组织起来，成为一个可实际运行的应用程序呢？”

`Bootstrap`其实是Netty提供的一个工厂类，通过它可以完成Netty客户端以及服务器Netty组件的组装，以及Netty程序的初始化，Netty官方也声明，`Bootstrap`类是开发者的**可选项**，只不过有了这个工具类，构建程序会更加方便

在Netty中，有两个启动器类，分别对应着服务器和客户端

`Bootstrap`类被用于客户端或者使用了无连接协议的应用程序中。`Bootstrap类`的API如下:

* `Bootstrap group(EventLoopGroup)`
设置用于处理`Channel`所有事件的`EventLoopGroup`
* `Bootstrap channel( Class<? extends C>)`
* `Bootstrap channelFactory(ChannelFactory<? extends C>)`
`channel()`方法指定了`Channel`的实现类。如果该实现类没提供默认的构造函数 ， 可以通过调用`channelFactory()`方法来指定一个工厂类，它将会被`bind()`方法调用。
* `Bootstrap localAddress(SocketAddress)`
指定`Channel`应该绑定到的本地地址。如果没有指定，则将由操作系统创建一个随机的地址。或者，也可以通过`bind()`或者`connect()`方法指定`localAddress`。
* `<T> Bootstrap option(ChannelOption<T> option, T value)`
设置`ChannelOption`， 其将被应用到每个新创建的`Channel`的`ChannelConfig`。 这些选项将会通过`bind()`或者`connect()`方法设置到`Channel` ，不管哪个先被调用。这个方法在`Channel`已经被创建后再调用将不会有任何的效果。支持的`ChannelOption`取决于使用的 `Channel`类型。
* `<T> Bootstrap attr( Attribute<T> key, T value)`
指定新创建的Channel的属性值。这些属性值是通过`bind()`或者`connect()`方法设置到`Channel`的，具体取决于谁最先被调用。这个方法在`Channel`被创建后将不会有任何的效果。
* `Bootstrap handler(ChannelHandler)`
设置将被添加到`ChannelPipeline`以接收事件通知的`ChannelHandler`。
* `Bootstrap clone()`
创建一个当前`Bootstrap`的克隆，其具有和原始的`Bootstrap`相同的设置信息。
* `Bootstrap remoteAddress(SocketAddress)`
设置远程地址。或者，也可以通过`connect()`方法来指定它。
* `ChannelFuture connect()`
连接到远程节点并返回一个`ChannelFuture`，其将会在连接操作完成后接收到通知。
* `ChannelFuture bind()`
绑定`Channel`并返回一个`ChannelFuture`，其将会在绑定操作完成后接收到通知，在那之后必须调用`Channel.connect()`方法来建立连接。

<div align=center><img src="/assets/n8.png"/></div>

### EventGroup线程组

在Reactor模式中我们知道，对于一个高效的网络程序，`Reactor`组件的个数不可能只有一个，此即Netty的多线程版本，“如何组织各个Reactor呢?”，这就是`EventLoopGroup`的作用

<div align=center><img src="/assets/n9.png"/></div>

### ChannelOption通道选项

使用`option()`方法可以将`ChannelOption`应用到引导，你所提供的值将会被自动应用到引导所创建的所有`Channel`（这样就可以不用在每个`Channel`创建时都手动配置它。）。可用的`ChannelOption`包括了底层连接的详细信息，如keep-alive或者超时属性以及缓存区设置。
Netty应用程序通常与组织的专有软件集成在一起，而像`Channel`这样的组件可能甚至会在正常的Netty生命周期之外被使用。 在某些常用的属性和数据不可用时， Netty提供了 `AttributeMap`抽象（一个由`Channel`和引导类提供的集合）以及`AttributeKey<T>`（一个用于插入和获取属性值的泛型类）。使用这些工具，便可以安全地将任何类型的数据项与客户端和服务器`Channel`（包含`ServerChannel`的子`Channel`）相关联了。
