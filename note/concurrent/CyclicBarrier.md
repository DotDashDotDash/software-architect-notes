# CyclicBarrier

* CyclicBarrier与CountDownLatch实现的功能是一样的，但是CyclicBarrier更加精简
* CountDownLatch通过countDown()来使计数器减，而CyclicBarrier通过await()使计数器减
* CountDownLatch基于AQS共享模式设计而成，而CyclicBarrier基于ReentrantLock和Condition设计而成
* CyclicBarrier的流程如下:

<div align=center><img src="/assets/cycba.png"></div>

## 参考链接

* [深入理解CyclicBarrier](https://blog.csdn.net/qq_39241239/article/details/87030142)
* [MyCyclicBarrier.java](/src/concurrent/MyCyclicBarrier.java)
