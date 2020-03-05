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

