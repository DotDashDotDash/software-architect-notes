# CountDownLatch与join()

* 二者都是阻塞当前线程待其他线程执行完毕之后继续执行本线程
* CountDownLatch要比join()更灵活，因为join()只有等待线程执行完成，而CountDownLatch只需计数至0就可以了

## 参考链接

* [CountDownLatch与join()区别](https://www.jianshu.com/p/795151ac271b)
* [MyCountDownLatch.java](/src/concurrent/MyCountDownLatch.java)