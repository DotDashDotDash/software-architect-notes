# Java高强度训练Day2

* off-heap指的是哪种内存

```markdown
JVM的内存结构:

1. 程序计数器: 几乎不占用内存，用于取下一条执行的指令
2. 堆: 所有通过new生成的对象全部都放在heap中
3. 栈: 每个线程执行每个方法的时候都会在栈中申请一个栈帧，每个栈帧包含局部变量区和操作数栈，用于存放此方法调用过程中的临时变量，参数和中间结果
4. 本地方法栈: 用于支持native方法的执行，存储了每个native方法调用的状态
5. 方法区: 存放了主要加载的类信息，静态变量，final类型的常量，属性和方法信息，JVM用永久代来存放方法区

off-heap意味着把内存对象分配在Java虚拟机的堆以外的内存当中，这些内存直接受操作系统管理，JVM负责回收方法区和堆，因此off-heap指的是进程管理的内存
```

* 下面的import语句可以访问的空间是:

```java
import java.util.*;
```

```markdown
能访问java.util目录下的所有类，不能访问java.util子目录下的所有类
```

* `URL url = new URL("http://www.123.com")`，如果www.123.com不存在，返回的结果是:

```markdown
URL url = new URL("XXX");

如果XXX不符合URL的格式会抛出异常，但是除此之外，返回的就只有一个URL链接，不管目标地址是否存在

例如:
URL url = new URL("http://www.123.com")
返回的是
http://www.123.com
```

* 写出下列语句1和语句2的执行结果

```java
public class Test{
    public static void main(String[] args){
        String s = "tommy";
        Object o = s;
        sayHello(o);    //语句1
        sayHello(s);    //语句2
    }

    public static void sayHello(String to){
        System.out.println(String.format("Hello, %s", to));
    }

    public static void sayHello(Object to){
        System.out.println(String.format("Welcome, %s", to));
    }
}
```

```markdown
语句1: Welcome, tommy
语句2: Hello, tommy
```

* `static abstract void f1();`是否正确?

```markdown
错误

abstract不能和final, private, static同时使用
abstract方法的默认修饰符是public
```

* `null, false, null, friendly, sizeof`是不是java关键字?

```markdown
不是java关键字，是java保留字
```

* 下面的switch语句中，x可以是哪些类型的数据:

```java
switch(x){
    default:
        System.out.println("Hello");
}
```

```markdown
在Java7之前: byte, short, char, int
在Java7之后: String, byte, short, int, char, Enum
```

* 往OuterClass类的代码段中插入内部类声明，哪一个是错误的:

```java
public class OuterClass{
    private float f = 1.0f;
    //插入代码段
}

//1
class InnerClass{
    public static float func(){
        return f;
    }
}

//2
abstract class InnerClass{
    public abstract float func(){}
}

//3
static class InnerClass{
    protected static float func(){
        return f;
    }
}

//4
public class InnerClass{
    static float func(){
        return f;
    }
}
```

```markdown
1错误: 成员内部类不能有任何static方法或者字段
2错误: abstract没有方法体
3错误: 静态内部类不能访问外部非静态变量
4错误: 同1
``` 

* 下面代码执行之后cnt的值是:

```java
public class Test{
    static int cnt = 6;

    static{
        cnt += 9;
    }

    static{
        cnt /= 3;
    }

    public static void main(String[] args){
        System.out.println("cnt = " + cnt);
    }
}
```

```markdown
这道题很重要，要知道静态代码块和静态成员的初始化和执行顺序

执行顺序如下:
1. 父类静态成员和静态代码块，按照在代码中出现的顺序依次执行
2. 子类静态成员和静态代码块，按照在代码中出现的顺序依次执行
3. 父类实例成员和实例初始化块，按照在代码中出现的顺序依次执行
4. 执行父类构造函数
5. 子类实例成员和实例初始化块，按照在代码中出现的顺序依次执行
6. 执行子类构造函数

其中要注意的是，静态成员变量和静态代码块是同一个级别的，有自己出现的顺序
```

* 下面代码的执行顺序是:

```java
public class IfTest{
    public static void main(String[] args){
        int x = 3;
        int y = 1;
        if(x = y)
            System.out.println("Not Equal");
        else
            System.out.println("Equal");
    }
}
```

```markdown
输出结果: Not Equal

if(x = y) ==> if((x = y) == true)
```

* 下面程序运行的结果是:

```java
public static void main(String[] args){
    Thread t = new Thread(){
        public void run(){
            do();
        }
    };

    t.run();
    System.out.println("b");
}

public static void do(){
    System.out.println("a");
}
```

```markdown
输出结果: ab

原因: 代码中是t.run()，只是简单的方法调用，并不是启动线程，
若是t.start()，执行的结果可能是ab也可能是ba
```

* 以下程序执行之后将会有多少个字节被写入到a.txt中

```java
try{
    FileOutputStream fos = new FileOutputStream("a.txt");
    DataOutputStream dos = new DataOutputStream(fos);

    dos.writeInt(3);
    dos.writeChar(1);
    
    dos.close();
    fos.close();
}catch(Exception e){
    e.printStackTrace();
}
```

```markdown
Java采用Unicode编码，一个char占两个字节，一个int占4个字节，一共六个字节
```

* 判断下面说法是否正确

```markdown
异常分为Error和Exception                        正确
Throwable是所有异常类的父类                      正确
Exception是所有异常类的父类                      错误
Exception包括RuntimeException和除此之外的异常    正确
```

* 以下哪个选项不属于java类加载的过程

```markdown
A. 生成java.lang.Class对象
B. int类型对象成员变量赋予默认值
C. 执行static块代码
D. 类方法的解析
```

```markdown
Java类加载的过程如下:

加载，验证，准备，解析，初始化

B对象成员变量的初始化是在实例化对象的时候才赋予默认值的
而类成员变量的赋值是在类加载的过程中进行的
```

* 当我们都需要所有的线程都执行到某处，才进行后面代码的执行，应该使用哪一个?

```markdown
1. CountDownLatch
2. CyclicBarrier
```

```markdown
CyclicBarrier是所有的线程都执行完毕之后，才执行后面的操作
而CountDownLatch可以使所有的线程都执行到某一处的时候才进行后面的操作
```

* Java抽象类能不能有构造方法?

```markdown
可以
```

* 直接调用Thread的run()是否会报错?

```markdown
不会
```

* ThreadLocal用于创建线程的本地变量，变量是不是线程间共享的？

```markdown
ThreadLocal存放的值是线程内共享，线程间互斥的，主要用于线程内共享一些数据，避免通过参数来进行传递
```

* Java每个中文字符占用2个字节，每个英文字符占用一个字节?

```markdown
错误

Java一律采用Unicode编码，不论是中文字符还是英文字符均占用2个字节
```

* Java的char类型是怎么来存放的?

```markdown
Java的char类型，通常是以UTF-16 Big Endian存放的
```

* 下列赋值语句正确的是:

```java
double d = 5.3e12;
float f = 11.1;
int i = 0.0;
Double oD = 3;
```

```markdown
1. 正确
2. 11.1默认为double，需要强制转换
3. 0.0转int也需要强制转换
4. 包装类型的数值必须严格对应
```

* (这题我服了)以下代码执行的结果是多少

```java
public static void main(String[] args){
    int count = 0;
    int num = 0;
    for(int i = 0; i <= 100; i++){
        num = num + i;
        count = count++;
    }
    System.out.println("num * count = " + (num * count));
}
```

* 关于以下访问权限的说明正确的是:

```markdown
1. 外部类定义前面可以修饰public, protected和private
2. 局部内部类前面可以修饰public, protected和private
3. 匿名内部类前面可以修饰public, protected和private
4. 以上说法都不正确
```

```markdown
1. 错误: 外部类放在包中，只有可见和不可见之分，即public和default
2. 错误: 局部内部类是定义在方法里面的，不能有public, protected和private
3. 错误: 匿名内部类不能有访问修饰符和static修饰符的

所以
4. 正确
```

* (很重要)给出下面代码的运行结果

```java
class Two{
    Byte x;
}

class Pass0{
    public static void main(String[] args){
        Pass0 p = new Pass0();
        p.start();
    }

    void start(){
        Two t = new Two();
        System.out.println(t.x + " ");

        Two t2 = fix(t);
        System.out.println(t.x + " " + t2.x);
    }

    Two fix(Two tt){
        tt.x = 42;
        return tt;
    }
}
```

```markdown
基本类型和String=""创建的字符串
传递给方法的参数的时候是值传递
其余情况为引用传递

所以，输出为:
null 42 42
```

* 谈谈有关finalize()方法

```markdown
GC Roots失去引用链，第一次标记，调用finalize()，之后由Finalizer线程执行是否可重新到达GC Roots，如果能到，移出队列，如果不能到，第二次标记，回收内存
```

* 下列程序运行的结果为:

```java
public static void main(String[] args){
    Object o1 = true ? new Integer(1) : new Double(2.0);
    Object o2;

    if(true){
        o2 = new Integer(1);
    }
    System.out.println(o1);
    System.out.println(" ");
    System.out.println(o2);
}
```

```markdown
1.0 1

三元操作符类型提升，Integer提升为Double
```