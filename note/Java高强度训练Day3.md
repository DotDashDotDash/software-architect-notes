# Java高强度训练Day3

* 下列程序的输出是:

```java
class Person{
    String name = "No name";

    public Person(String name){
        this.name = name;
    }
}

class Employee extends Pereson{
    String emId = "0000";

    public Employee(String emId){
        this.emId = emId;
    }
}

public static void main(String[] args){
    Employee employee = new Employee("123");
    System.out.println(employee.emId);
}
```

```markdown
子类创建的过程中会首先调用父类的默认构造函数
如果父类没有默认构造函数，需要显式指定父类的构造函数

因此上述代码会报错
```

* 以下代码运行的结果为:

```java
public class Main{
    public static void main(String[] args){
        String s;
        System.out.println(s);
    }
}
```

```markdown
会报错，因为方法内部的局部变量需要赋初始值
而类内的成员变量会在类加载的时候赋予初值
```

* 下面哪些属于正确地声明一个二维数组

```java
A. int[][] a = new int[][];
B. int []a[] = new int[10][10];
C. int[10][10] a = new int[][];
D. int[][] a = new int[10][10];
```

```markdown
A. 一维长度必须确定
B. 正确
C. 一维长度必须确定
D. 正确
```

* 下面代码是否正确

```java
public class Base{
    private int a, b, c, d, e;

    public Base(int a, int b){
        this.a = a;
        this.b = b;
    }

    public Base(int c, int d){
        Base(1, 2);
        this.c = c;
        this.d = d;
    }
}
```

```markdown
错误: 只能调用this(1, 2)
```

* Java以Stream结尾都是字节流，以Reader/Writer结尾都是字符流

* 下列代码的运行结果是:

```java
class Animal{
    public void move(){
        System.out.println("动物可以移动");
    }
}
class Dog extends Animal{
    public void move(){
        System.out.println("狗可以跑和走");
    }
    public void bark(){
        System.out.println("狗可以吠叫");
    }
}
public class TestDog{
    public static void main(String args[]){
        Animal a = new Animal();
        Animal b = new Dog(); 
        a.move();
        b.move();
        b.bark();
    }
}
```

```markdown
运行错误

Animal b = new Dog();

Animal没有bark()这个方法
```

* volatile可以实现多线程下的计数器

```markdown
必须加锁
```

* 下面哪些方法可以被继承

```java
public class Parent { 
    private void m1(){} 
    void m2(){} 
    protected void m3(){} 
    public static void m4(){} 
}
```

```markdown
通过继承，子类可以拥有所有父类对其可见的方法和域 
A.私有方法只能在本类中可见，故不能继承，A错误 
B.缺省访问修饰符只在本包中可见，在外包中不可见，B错误 
C.保护修饰符凡是继承自该类的子类都能访问，当然可被继承覆盖；C正确 
D.static修饰的成员属于类成员，父类字段或方法只能被子类同名字段或方法遮蔽，不能被继承覆盖，D错误 
```

* goto是java关键字吗

```markdown
是的
```

* 下列List扩充几次

```java
List<Integer> list = new ArrayList<>(10);
```

```markdown
Arraylist默认数组大小是10，扩容后的大小是扩容前的1.5倍，最大值小于Integer 的最大值减8，如果新创建的集合有带初始值，默认就是传入的大小，也就不会扩容 
```

* Java重写和重载需要注意的事项是

```markdown
方法重写 
    参数列表必须完全与被重写方法的相同； 
    返回类型必须完全与被重写方法的返回类型相同； 
    访问权限不能比父类中被重写的方法的访问权限更低。例如：如果父类的一个方法被声明为public，那么在子类中重写该方法就不能声明为protected。 
    父类的成员方法只能被它的子类重写。 
    声明为final的方法不能被重写。 
    声明为static的方法不能被重写，但是能够被再次声明。 
    子类和父类在同一个包中，那么子类可以重写父类所有方法，除了声明为private和final的方法。 
    子类和父类不在同一个包中，那么子类只能够重写父类的声明为public和protected的非final方法。 
    重写的方法能够抛出任何非强制异常，无论被重写的方法是否抛出异常。但是，重写的方法不能抛出新的强制性异常，或者比被重写方法声明的更广泛的强制性异常，反之则可以。 
    构造方法不能被重写。 
    如果不能继承一个方法，则不能重写这个方法。 
方法重载
    被重载的方法必须改变参数列表(参数个数或类型或顺序不一样)； 
    被重载的方法可以改变返回类型； 
    被重载的方法可以改变访问修饰符； 
    被重载的方法可以声明新的或更广的检查异常； 
    方法能够在同一个类中或者在一个子类中被重载。 
    无法以返回值类型作为重载函数的区分标准。 
```

* 关于ThreadLocal

```markdown
ThreadLocal不是一个线程而是一个线程的本地化对象。当工作于多线
程环境中的对象采用ThreadLocal维护变量时，ThreadLocal为每个使
用该变量的线程分配一个独立的副本。每个线程都可以独立的改变自己
的副本，而不影响其他线程的副本。
```

* 下列关于会话跟踪技术说法正确的是:

```markdown
A. Cookie是Web服务器发送给客户端的一小段信息，客户端请求时，可以读取该信息发送到服务器端
B. 关闭浏览器意味着临时会话ID丢失，但所有与原会话关联的会话数据仍保留在服务器上，直至会话过期
C. 在禁用Cookie时可以使用URL重写技术跟踪会话
D. 隐藏表单域将字段添加到HTML表单并在客户端浏览器中显示
```

```markdown
A,B,C均正确

隐藏域在页面中对于用户（浏览器）是不可见的，在表单中插入隐藏域
的目的在于收集或发送信息，以利于被处理表单的程序所使用。浏览者
单击发送按钮发送表单的时候，隐藏域的信息也被一起发送到服务器
```

* Java虚函数

```markdown
虚函数的存在是为了多态。

Java中其实没有虚函数的概念，它的普通函数就相当于C++的虚函数，动态绑定是Java的默认行为。如果Java中不希望某个函数具有虚函数特性，可以加上final关键字变成非虚函数

PS: 其实C++和Java在虚函数的观点大同小异，异曲同工罢了。
```

* 下列代码的输出结果为:

```java
public class Demo {
  public static void main(String args[])
  {
    String str=new String("hello");
    if(str=="hello")
    {
      System.out.println("true");
    }      
    else     {
      System.out.println("false");
    }
  }
}
```

```markdown
new String() 创建了一个新的String对象

与常量池中的String地址不同
```

* 什么情况下会发生永久代溢出，什么情况下会发生老年代溢出:

```markdown
CGIB动态代理大量生成代理对象导致Perm溢出
大量创造对象导致OOM(堆的老年代溢出)
```

* 创建派生类的时候构造顺序为:

```markdown
基类构造函数，派生类对象成员构造函数，派生类本身的构造函数
```

* 以下JSP代码定义了一个变量，如何输出这个变量的值？ 
`<bean:define id="stringBean" value="helloWorld"/>`

```jsp
1. <%=stringBean%>
2. <bean:write name="stringBean"/>
3. <%String myBean=(String)pageContext.getAttribute("stringBean",PageContext.PAGE_SCOPE);%>
<%=myBean%>
```

* 在try的括号里面有return一个值，那在哪里执行finally里的代码?

```markdown
return 前返回
```

* 关于下列jsp说法错误的是:

```jsp
<%@ page language="java" import="java.util.*" errorPage="error.jsp" isErrorPage="false" %>  
```

```markdown
A. 该页面可以使用 exception 对象(错误)
B. 该页面发生异常会转向 error.jsp(正确)
C. 存在 errorPage 属性时，isErrorPage 是默认为 false(正确)
D. error.jsp 页面一定要有isErrorPage 属性且值为 true(正确)

exception是JSP九大内置对象之一，其实例代表其他页面的异常和错
误。只有当页面是错误处理页面时，即isErroePage为 true时，该对象
才可以使用。对于C项，errorPage的实质就是JSP的异常处理机制,发生
异常时才会跳转到 errorPage指定的页面，没必要给errorPage再设置
一个errorPage。所以当errorPage属性存在时， isErrorPage属性值
为false 
```

* Java通过垃圾回收回收不再引用的变量，垃圾回收时对象的finallize方法一定会得到执行

```markdown
错误

GC线程属于优先级十分低的线程
(但是这个解释有失偏颇)
```
