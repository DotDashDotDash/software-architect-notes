# Java高强度训练Day1

* Java类是单继承但是接口可以多继承

```java
interface1 extends interface2, interface3
```

* 为什么Java只允许类的单继承?

```markdown
因为A同时继承类B和类C，而B和C都有一个D方法，那么A不知道该继承哪一个方法
```

* 多重继承是通过哪些方式实现的?

```markdown
1. 扩展一个类并实现一个或者多个接口
2. 实现两个或者更多的接口
```

* 类A和类B都在同一个包中，类A有个protected方法a()，类B不是类A的子类，类B可以访问类A中的方法a()吗?

```markdown
1. public: 可以被其他所有的类访问
2. private: 只能被自己访问和修改
3. protected: 自身，子类和同一个包中可以访问
4. default: 同一个包中的类可以访问，声明时没有加修饰符，认为是friendly
```

* (判断题): 用户不能调用构造方法，只能通过new关键字自动调用

```markdown
错误: 
1. 在类的其他方法中可以调用this()
2. 在类的子类中可以调用super()
3. 在反射中可以调用newInstance()调用
```

* 多态的表现形式和根本方法

```markdown
表现形式: 重写
根本方法: 继承
```

* 以下程序的执行结果为:

```java
static boolean foo(char c){
    System.out.println(c);
    return true;
}

public static void main(String[] args){
    int i = 0;
    for(foo('A'); foo('B') && (i < 2); foo('C')){
        i++;
        foo('D');
    }
}
```

```markdown
起始条件可以不为boolean类型
结果为:
ABDCBDCB
```

* 说说有关内部类的问题

```markdown
1. 成员内部类可以访问外部所有的资源但是自身不能定义静态资源
2. 静态内部类可以访问外部类的静态变量但是不可以访问非静态变量
3. 局部内部类往往定义在方法体中，只能访问代码块或者方法中被定义为final的变量
4. 匿名内部类不能使用class, implements, extends，没有构造方法
```

* 谈谈子类的构造函数

```markdown
1. 子类不能继承父类的构造器
2. 在创建子类的时候，如果子类中不含带参的构造函数，先执行父类的构造函数，然后执行自己的无参构造函数
```

* 下列代码的执行结果为:

```java
public static void main(String[] args){
    String s = "com.jd.".replaceAll(".", "/") + "MyClass.class"
}
```

```markdown
执行结果为:
///////MyClass.class

. 代表任何字符，这是一个坑
```

* 哪些接口直接继承自Collection接口

```markdown
             |---- List
Collection-------- Set
             |---- Queue
```

* 以下代码可以使用的修饰符是:

```java
abstract interface Status{
    /** Code here **/ int value = 10;
}
```

```markdown
1. public
2. final
3. static

接口字段的默认修饰符: public static final
方法字段的默认修饰符: public abstract
```

* 下面哪些方法是Object类的

```markdown
clone()
toString()
wait()
finalize()
```

* 下列哪些方法的定义是不正确的:

```java
public class A{
    float func0(){
        byte i = 1;
        return i;
    }

    float func1(){
        int i = 1;
        return;
    }

    float func2(){
        short i = 1;
        return i;
    }

    float func3(){
        long i = 3;
        return i;
    }

    float func4(){
        double i = 1;
        return i;
    }
}
```

```markdown
func1: 没有return value
func4: double无法assign给float

容易出错的点是func3: long可以assign给float
```

* Java程序的种类有:

```markdown
Applet: 嵌入在浏览器中的小程序
Servlet: web程序
Application
```

* Java局部变量如果不赋初始值会有默认值

```markdown
错误: 局部变量必须要有初始值，否则会报错
```

* 所有的异常类都直接继承于哪一个类?

```markdown
所有的异常类都直接继承于java.lang.Exception
所有的异常类都继承于Throwable
```

* 下列代码执行结果为:

```java
public class Demo{
    public static void main(String[] args){
        Integer i1 = 128;
        Integer i2 = 128;
        System.out.println((i1 == i2) + ", ");

        String i3 = "100";
        String i4 = "1" + new String("00");
        System.out.println((i3 == i4) + ", ");

        Integer i5 = 100;
        Integer i6 = 100;
        System.out.println((i5 == i6));
    }
}
```

```markdown
false, false, true

当我们在为Integer赋值的时候，会自动调用Integer.valueOf()
1. 对于-128~127之间的数字，Java会对其进行缓存，超过这个范围创建新的对象
2. 对于i3和i4，在编译期会在字符串常量池中创建一个"100"常量，然后创建一个"1"常量，但是在运行时才会创建"00"，将其拼接创建一个新的String对象"100"，i3和i4不同
```

* 下面程序代码的执行结果为:

```java
Boolean flag = false;

if(flag = true){
    System.out.println("true");
}else{
    System.out.println("false");
}
```

```markdown
执行结果为: true

if(flag = true) ==> if((flag = true) == true)条件为true
```

* 下面代码的执行结果为:

```java
public class Test{
    static String x = "1";
    static int y = 1;

    public static void main(String[] args){
        static int z = 2;
        System.out.println(x + y + z);
    }
}
```

```markdown
执行结果: 编译错误

static修饰类成员变量不能修饰局部变量
```

* 假定str0, ..., str4后续代码都是只读引用，Java7中，以上代码为基础，在发生过一次FullGC后，上述代码在Heap空间(不包括PermGen)的保留字符数为:

```java
static String str0 = "0123456789";
static String str1 = "0123456789";
String str2 = str1.substring(5);
String str3 = new String(str2);
String str4 = new String(str3.toCharArray());
str0 = null;
```

```markdown
Java垃圾回收主要针对堆区的回收，因为栈区的内存是随着线程而释放的，堆区主要分为三个区:
1. Young Gen: 对象被创建后通常放在Yound Gen，除了一些非常大的对象，经过一定的Minor GC还活着的对象被移到Old Gen
2. Old Gen: 从Young Gen移动过来的游戏额比较大的对象，Minor GC(Full GC)针对年老代的回收
3. Perm Gen(Java8改Meta Space): 存储的是final变量，static变量，常量池

str0和str1在编译时创建"0123456789"保存到常量池，即Perm Gen中
substring的本质还是会new一个String对象，str2为5 char，str3为5 char，str4也是创建一个新的对象为5 char，经过FullGC(Minor GC)之后
综上所述，不包含Perm Gen的一共有5 + 5+ 5 = 15 char
```

* 下面代码的执行结果为:

```java
byte b1 = 1, b2 = 2, b3, b6;
final byte b4 = 4, b5 = 6;
b6 = b4 + b5;
b3 = (b1 + b2);
System.out.println(b3 + b6);
```

```markdown
byte在运算的时候会自动提升至int类型
运算结果是int类型无法再赋给byte类型，要进行类型的强制转换
除此之外，short, char在计算的时候也会自动提升至int类型
```

* Java的关键字只能由数字字母和下划线组成

```markdown
错误
还有$也可以作为标识符的组成部分
```

* Java类名必须和文件名一致

```markdown
错误
内部类名可以不一样
```

* 下列流中，属于处理流的是:

```java
FileInputStream
InputStream
DataInputStream
BufferdInputStream
```

```markdown
区分节点流，处理流
1. 节点流: 从或者向一个地方读写数据
    Input/Output/Stream
    FileInput/Output/Stream
    String/Reader/Writer
    ByteArrayInputStream/Out
    Piped/Input/Output/Stream(Reader/Writer)
2. 处理流(处理节点流的流)
    Buffered...
    Input/Output/Stream/Reader/Writer
    Data/Input/Output/Stream
```

* 根据如下继承关系判断哪些是对的哪些是错的

```java
class A{}
class B extends A{}
class C extends A{}
class D extends B{}
```

```markdown
List = List<A>                          正确: 点到范围
List<A> = List<B>                       错误: 点到点
List<?> = List<Object>                  正确: 点到范围
List<? extends B> = List<D>             正确: 点到范围
List<A> = List<? extends A>             错误: 范围到点
List<? extends A> = List<? extends B>   正确: 小于A的范围小于小于B的范围
```

* 那个修饰符是所有同一个类生成的对象共享?

```markdown
static
static修饰的某个字段只有一个存储空间，所有的实例共享
```

* 下面为true的是

```java
Integer i = 42;
Long l = 42l;
Double d = 42.0;
```

```markdown
(i == l)        false: 不同类型的引用比较，编译错误
(i == d)        false: 同上
(l == d)        false: 同上
i.equals(d)     false: 包装类的equals()不处理数据转型
d.equals(l)     false: 同上
i.equals(l)     false: 同上
l.equals(42l)   true 
```

* 下面一段代码，当T分别为引用类型和值类型的时候，分别产生了多少个T对象

```java
T t = new T();
Func(t);

public void Func(T t){}
```

```markdown
引用类型作为参数的时候，传递的是引用对象的地址，不产生新的对象
值类型作为参数的时候，传递的是对象的值副本，产生了一个新的对象

答案为: 1, 2
```

* 下列代码的输出为:

```java
public class P{
    public static int abc = 123;

    static{
        System.out.println("P is int");
    }
}

public class S extends P{
    static{
        System.out.println("S is int");
    }
}

public class Test{
    public static void main(String[] args){
        System.out.println(S.abc);
    }
}
```

```markdown
虚拟机严格规定了有且只有5中情况必须立即对类进行“初始化”:

1. 使用new关键字，读取或者设置一个类的静态字段的时候，或者调用一个类的静态方法的时候
2. 使用java.lang.reflect包的方法对类进行反射调用的时候，如果类没有被初始化，必须对其进行初始化
3. 当初始化一个类的时候，如果发现其父类没有被初始化就先初始化它的父类
4. 当虚拟机启动的时候，用户需要指定一个要执行的主类(包含main)，虚拟机会先初始化这个类
5. 使用jdk1.7动态语言支持的时候的一些情况

而属于被动引用不会触发子类的初始化
1. 子类引用父类的静态字段，只会出发子类的加载，父类的初始化，不会导致子类的初始化
2. 通过数组定义来引用类，不会触发此类的初始化
3. 常量在编译阶段被存入调用类的常量池，本质上没有引用到定义常量的类，因此不会触发定义常量的类的初始化
```

* 判断下列关于HashMap和HashTable的说法是否正确

```markdown
1. HashMap是线程不安全的，HashTable是线程不安全的(正确)
2. HashTable键和值均不允许null，HashMap也是(错误)： HashMap允许值为null
3. HashMap通过get()可判断是否含有键(错误): HashTable是的
```

* final修饰的变量不允许被赋值?

```markdown
final修饰的属性值已经固定，无法再被赋值
```

* 假设下面两个赋值语句

```java
a = Integer.parseInt("1024");
b = Integer.valueOf("1024").intValue();
```

判断a和b的类型

```markdown
a和b都是int

intValue()是把Integer变成int
parseInt()是把String变成int
valueOf()是把String转化为Integer
```
