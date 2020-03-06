## Netty内存泄漏元凶之——ByteBuf缓冲区

### 什么是ByteBuf

Netty提供了`ByteBuf`来替代Java NIO的`ByteBuffer`，**用来操纵内存缓冲区**

与Java NIO的`ByteBuffer`相比，`ByteBuf`具有下面的优势:

* Pooling(池化)，减少了内存的GC，提高了效率
* 复合缓冲区类型，支持零复制
* 不需要调用`flip()`方法来切换读/写模式
* 扩展性好
* 可以自定义缓冲类型
* 读取和写入的索引分开
* 方法的链式调用
* **可以进行引用计数，方便重复使用**

> #### 什么是池化，为什么池化能够减少内存的GC，又为什么`ByteBuf`可以池化而`ByteBuffer`不能池化

学习JVM的知识都知道，一个对象被创建的过程如下:

* 根据`new`后面的参数，在常量池中寻找符号引用
* 如果对应的符号引用的类没有被加载器加载，则首先进行类的加载，如果已经加载，进入下面一个步骤
* 在`Heap`中为对象分配内存空间(**耗时操作**)
* 对已经分配内存空间的对象进行初始化操作(**耗时操作**)

如果存在池化技术，那么就可以复用已经存在的对象，减少耗时操作的反复执行，以为一个对象失效，就会被JVM GC掉，而有的GC垃圾收集器执行GC操作的时候需要暂停工作线程，这就导致了很大的一部分可以说不必要的性能损耗

Netty既能够创建池化类型的`ByteBuf`，又可以创建非池化类型的`ByteBuf`

> #### 复合缓冲区的类型

Netty为了内存管理的不同，将内存分为了**堆缓存区**和**直接缓存区**，除此之外，为了方便缓冲区进行组合，提供了一种**组合缓冲区**

|类型|说明|优点|缺点|
|:---:|:---:|:---:|:---:|
|`XxxHeapByteBuf`|内部存储在一个Java数组中，存储在JVM的堆空间中，通过`hasArray`来判断是不是堆缓冲区|未使用池化的情况下，能够提供快速分配和释放|写入底层传输通道之前，都会复制到直接缓冲区|
|`XxxDiretByteBuf`|内部存储在操作系统的物理内存当中|能够获取超过JVM堆限制大小的内存空间，写入传输通道比堆缓冲区更快|释放和分配空间代价昂贵，在Java中操作的时候会复制一次到堆上|
|`CompositeBuffer`|多个缓冲区的组合表示|方便一次操作到多个缓冲区实例||

**注意，在使用`DirectBuf`的时候，Netty不会释放不再使用的`DirectBuf`，因为这属于堆外内存，应该在程序中手动触发`System.gc()`来释放内存**

### ByteBuf的释放

在了解释放之前，首先先要了解`ByteBuf`是什么时候被创建的，Netty的Reactor线程会在底层的Java NIO通道读取数据，也就是`AbstractNioByteChannel.NioByteUnsafe.read()`处，调用`ByteBufAllocator`方法，创建`ByteBuf`实例，将操作系统缓冲区内的数据(例如TCP缓冲区)读取到`ByteBuf`中，然后调用`pipline.fireChannelRead(buf)`将读取到的数据包送入到入站处理流水线当中

有了上面的大概了解，下面就来了解一下`ByteBuf`的释放方式:

> #### TailHandler释放

<div align=center><img src="/assets/n10.png"></div>

同一个`pipeline`中的`handler`会以流水线的方式依次调用，默认的`Channel`最后一个`TailHandler`会释放掉`ByteBuf`实例

```java
public class ByteBufReleaseDemo extends ChannelInboundHandlerAdapter{
    /**
     * @param ctx 上下文
     * @param msg 入站数据包
     */
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        //业务逻辑
        super.channelRead(ctx, msg);
    }
}
```

通过调用父类的入站方法，能够将msg向后传递，依赖后面的处理器释放`ByteBuf`，或者也可以手动释放`ByteBuf`

```java
public class ByteBufReleaseDemo extends ChannelInboundHandlerAdapter{
    /**
     * @param ctx 上下文
     * @param msg 入站数据包
     */
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        ByteBuf byteBuf = (ByteBuf) msg;
        //业务逻辑
        byteBuf.release();
    }
}
```


> #### SimpleChannelInboundHandler释放

上述由`TailHandler`释放是建立在pipeline中的处理器流水线没有阻断的情况下，但是如果流水线被阻断了之后呢，例如没有调用`super.channelRead()`

<div align=center><img src="/assets/n11.png"/></div>

```java
public abstract class SimpleChannelInboundHandler<I> extends ChannelInboundHandlerAdapter 
{
//...
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    boolean release = true;
    try {
        if (acceptInboundMessage(msg)) {
            @SuppressWarnings("unchecked")
            I imsg = (I) msg;
            channelRead0(ctx, imsg);
        } else {
            release = false;
            ctx.fireChannelRead(msg);
        }
    } finally {
        if (autoRelease && release) {
            ReferenceCountUtil.release(msg);
        }
    }
}
```

可以继承 `SimpleChannelInboundHandler`，实现业务`Handler`。 `SimpleChannelInboundHandler` 会完成`ByteBuf` 的自动释放，释放的处理工作，在其入站处理方法 `channelRead` 中

> #### 出站ByteBuf的释放

出站处理流程中，申请分配到的`ByteBuf`，通过`HeadHandler`完成自动释放。

出站处理用到的`Bytebuf`缓冲区，一般是要发送的消息，通常由应用所申请。在出站流程开始的时候，通过调用`ctx.writeAndFlush(msg)`，`Bytebuf` 缓冲区开始进入出站处理的 `pipeline` 流水线 。在每一个出站`Handler`中的处理完成后，最后消息会来到出站的最后一棒`HeadHandler`，再经过一轮复杂的调用，在flush完成后终将被release掉