大家学习String的时候基本上就能发现这是一个很特殊的类型，String和String比较，有时相等，有时不相等，很容易搞晕，下面就简单揭示以下这个Java中很特殊的类，String类型


### 先从String类的定义开始谈起

```java
final class String implements ...{
    private final char value[];
}
```

首先看到的是final，也就是String类型是不可修改的，所以当我们调用

```java
String s = "abc";
s.charAt(0) = 'c';
```

一定会报错，这就是String类型的不可修改性

### String和常量池的关系

众所周知，String类型的创建方式有两种:

* 第一种是从常量池中直接拿(如果没有再创建)

```java
String s = "abc";
```

* 第二种是直接在堆内存空间中创建一个新的对象

```java
String s = new String("abc");

//不管常量池中有没有这个字符串，一定会创建一个新的对象
```

既然是两个不同的对象，那么等号判断一定是不相等的

```java
String s1 = new String("abc");
String s2 = new String("abc");

System.out.println(s1 == s2); //一定是false
```

**String类型的常量池比较特殊，使用有如下两种方法**

* 直接用双引号声明出来的String会存储在常量池当中
* 如果不是双引号声明的String对象，可以使用`String.intern()`来获得在常量池中的引用

```java
String s1 = new String("abc");
String s2 = s1.intern();
String s3 = "abc";

System.out.println(s1 == s2); //false，一个在堆上，一个在常量池当中
System.out.println(s2 == s3); //true，两个都是常量池当中的对象
```

**当使用到字符串的拼接的时候**

```java
String s1 = "a";
String s2 = "b";
String s3 = "a" + "b";
String s4 = s1 + s2;
String s5 = "ab";

System.out.println(s3 == s4); //false 一个常量池，一个堆上
System.out.println(s3 == s5); //true 两个常量池
System.out.println(s4 == s5); //false 一个堆上，一个常量池
```

**String s = new String("abc")创建了几个字符串对象**

* 首先检查常量池中有没有这个字符串
* 有的话，只创建一个值为abc的字符串堆对象，那么只有1个
* 没有的话，先在常量池中创建一个abc常量，然后再创建一个堆对象，那么有2个

### String的拼接操作

```java
String s1 = "a";
String s2 = "b";
String s3 = s1 + s2;
```

会隐式调用`StringBuilder.append()`方法，要想提高性能，手动设置`append`比`+`效率更高

### String和StringBuilder和StringBuffer

* String是不可变的，StringBuilder和StringBuffer是可变的
* StringBuilder是非线程安全的，StringBuffer是线程安全的
* 执行速度StringBuilder>StringBuffer>String

### 从String谈Integer

* **Byte,Short,Integer,Long,Character,Boolean默认创建了[-128-127]之间的数据**
* **Character创建了数值在[0,127]之间的缓存数据**

```java
Integer i1 = 40;    //会调用Integer.valueOf(40)从常量池当中返回
Integer i2 = new Integer(40); //创建了一个新的对象
```

**一个更复杂的例子**

```java
Integer a = 40;
Integer b = 40;
Integer c = 0;
Integer d = new Integer(40);
Integer e = new Integer(40);
Integer f = new Integer(0);

a == b          //true
a == (b + c)    //true
a == d          //false
d == e          //false
d == (e + f)    //true 自动拆箱
40 == (e + f)   //true
```