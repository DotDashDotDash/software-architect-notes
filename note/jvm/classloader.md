# ClassLoader

## ClassLoader执行流程

<div align=center><img src="../assets/classloader1.png"/></div>

* 加载: 通过一个类的全限定名来获取该类的二进制字节流，把二进制字节流代表的静态数据结构转化为运行时数据区的数据结构，将该二进制字节流转化为Class类
* 验证: 验证.class文件是否对虚拟机有危害，主要进行下面的验证
  * 文件格式的验证: 验证.class文件符合class文件的格式
  * 元数据的验证: 对字节码进行语义分析，判断里面描述的信息是否符合java语言的规范
  * 字节码验证: 对字节码进行数据流和控制流的分离验证，判断语句的语义，是否会做出危害虚拟机的行为
  * 符号引用的验证: 虚拟机在将符号引用转化为直接引用的时候，对类外的信息进行检查
* 准备: 主要对类变量分配内存及赋值，这里赋值不是程序语句中定义的值，而是数据类型的默认的值，赋值语句的赋值发生在初始化阶段，在准备阶段会为static变量分配内存，但是实例变量不会，实例变量会被分配到java堆中，static变量会被分配到常量池中
* 解析: 将符号引用转化为直接引用的过程
* 初始化: 执行类构造器`<clinit>`，这里是java代码真正开始执行的地方

## ClassLoader种类

<div align=center><img src="../assets/classloader2.png"></div>

## 类加载的三种方式

* 命令行JVM启动时含有main()的主类
* Class.forName()
* ClassLoader.loadClass()动态加载，不会执行代码块

## Classloader代码测试

* [双亲委托模型PDM](../src/jvm/ParentDelegateModel.java)
* [Class.forName()触发static块执行，JDBC如何破坏PDM注册Driver](../src/jvm/ParentDelegateModel.java)
* [ClassLoader继承关系测试](../src/jvm/MyClassLoader.java)