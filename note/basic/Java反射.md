# Java反射

## 什么是反射

JAVA反射机制是在运行状态中，对于任意一个类，都能够知道这个类的所有属性和方法；对于任意一个对象，都能够调用它的任意方法和属性；这种动态获取信息以及动态调用对象方法的功能称为java语言的反射机制。

## 用途

在日常的第三方应用开发过程中，经常会遇到某个类的某个成员变量、方法或是属性是私有的或是只对系统应用开放，这时候就可以利用Java的反射机制通过反射来获取所需的私有成员或是方法。当然，也不是所有的都适合反射，之前就遇到一个案例，通过反射得到的结果与预期不符。阅读源码发现，经过层层调用后在最终返回结果的地方对应用的权限进行了校验，对于没有权限的应用返回值是没有意义的缺省值，否则返回实际值起到保护用户的隐私目的。

## 反射的具体用法

反射之中包含了一个「反」字，所以想要解释反射就必须先从「正」开始解释。

一般情况下，我们使用某个类时必定知道它是什么类，是用来做什么的。于是我们直接对这个类进行实例化，之后使用这个类对象进行操作。如：

```java
Phone phone = new Phone(); //直接初始化，「正射」
phone.setPrice(4);
```

上面这样子进行类对象的初始化，我们可以理解为「正」。

而反射则是一开始并不知道我要初始化的类对象是什么，自然也无法使用 new 关键字来创建对象了。

这时候，我们使用 JDK 提供的反射 API 进行反射调用：

```java
Class clz = Class.forName("com.xxp.reflect.Phone");
Method method = clz.getMethod("setPrice", int.class);
Constructor constructor = clz.getConstructor();
Object object = constructor.newInstance();
method.invoke(object, 4);
```

上面两段代码的执行结果，其实是完全一样的。但是其思路完全不一样，第一段代码在未运行时就已经确定了要运行的类（Phone），而第二段代码则是在运行时通过字符串值才得知要运行的类（com.xxp.reflect.Phone）。

**所以说什么是反射？反射就是在运行时才知道要操作的类是什么，并且可以在运行时获取类的完整构造，并调用对应的方法。**

一个简单的例子：
上面提到的示例程序，其完整的程序代码如下：

```java
public class Phone {
    private int price;
    public int getPrice() {
        return price;
    }
    public void setPrice(int price) {
        this.price = price;
    }

    public static void main(String[] args) throws Exception{
        //正常的调用
        Phone phone = new Phone();
        phone.setPrice(5000);
        System.out.println("Phone Price:" + phone.getPrice());
        //使用反射调用
        Class clz = Class.forName("com.xxp.api.Phone");
        Method setPriceMethod = clz.getMethod("setPrice", int.class);
        Constructor phoneConstructor = clz.getConstructor();
        Object phoneObj = phoneConstructor.newInstance();
        setPriceMethod.invoke(phoneObj, 6000);
        Method getPriceMethod = clz.getMethod("getPrice");
        System.out.println("Phone Price:" + getPriceMethod.invoke(phoneObj));
    }
}
```

从代码中可以看到我们使用反射调用了 setPrice 方法，并传递了 6000 的值。之后使用反射调用了 getPrice 方法，输出其价格。上面的代码整个的输出结果是：

```markdown
Phone Price:5000
Phone Price:6000
```

从这个简单的例子可以看出，一般情况下我们使用反射获取一个对象的步骤：

```java
//获取类的 Class 对象实例
Class clz = Class.forName("com.xxp.api.Phone");
//根据 Class 对象实例获取 Constructor 对象
Constructor phoneConstructor = clz.getConstructor();
//使用 Constructor 对象的 newInstance 方法获取反射类对象
Object phoneObj = phoneConstructor.newInstance();
```

而如果要调用某一个方法，则需要经过下面的步骤：

```java
//获取方法的 Method 对象
Method setPriceMethod = clz.getMethod("setPrice", int.class);
//利用 invoke 方法调用方法
setPriceMethod.invoke(phoneObj, 6000);
```

## 反射常用API

在 JDK 中，反射相关的 API 可以分为下面几个方面：获取反射的 Class 对象、通过反射创建类对象、通过反射获取类属性方法及构造器。

反射常用API：

### 获取反射中的Class对象
在反射中，要获取一个类或调用一个类的方法，我们首先需要获取到该类的 Class 对象。
在 Java API 中，获取 Class 类对象有三种方法：

* 使用 Class.forName 静态方法。当知道某类的全路径名时，可以使用此方法获取 Class 类对象。用的最多，但可能抛出 ClassNotFoundException 异常。

```java
Class c1 = Class.forName(“java.lang.String”);
```

* 直接通过 类名.class 的方式得到，该方法最为安全可靠，程序性能更高。这说明任何一个类都有一个隐含的静态成员变量 class。这种方法只适合在编译前就知道操作的 Class。

```java
Class c2 = String.class;
```

* 通过对象调用 getClass() 方法来获取，通常应用在：比如你传过来一个 Object类型的对象，而我不知道你具体是什么类，用这种方法。

```java
String str = new String("Hello");
Class c3 = str.getClass();
```

需要注意的是：一个类在 JVM 中只会有一个 Class 实例，即我们对上面获取的 c1、c2和c3进行 equals 比较，发现都是true。

### 通过反射创建类对象

通过反射创建类对象主要有两种方式：通过 Class 对象的 newInstance() 方法、通过 Constructor 对象的 newInstance() 方法。

* 通过 Class 对象的 newInstance() 方法。

```java
Class clz = Phone.class;
Phone phone = (Phone)clz.newInstance();
```

* 通过 Constructor 对象的 newInstance() 方法

```java
Class clz = Phone.class;
Constructor constructor = clz.getConstructor();
Phone phone= (Phone)constructor.newInstance();
```

* 通过 Constructor 对象创建类对象可以选择特定构造方法，而通过 Class 对象则只能使用默认的无参数构造方法。下面的代码就调用了一个有参数的构造方法进行了类对象的初始化。

```java
Class clz = Phone.class;
Constructor constructor = clz.getConstructor(String.class, int.class);
Phone phone = (Phone)constructor.newInstance("华为",6666);
```

### 通过反射获取类属性、方法、构造器

我们通过 Class 对象的 getFields() 方法可以获取 Class 类的属性，但无法获取私有属性。

```java
Class clz = Phone.class;
Field[] fields = clz.getFields();
for (Field field : fields) {
    System.out.println(field.getName());
}
```

输出结果是：

```markdown
price
```

而如果使用 Class 对象的 getDeclaredFields() 方法则可以获取包括私有属性在内的所有属性：

```java
Class clz = Phone.class;
Field[] fields = clz.getDeclaredFields();
for (Field field : fields) {
    System.out.println(field.getName());
}
```

输出结果是：

```markdown
name
price
```

与获取类属性一样，当我们去获取类方法、类构造器时，如果要获取私有方法或私有构造器，则必须使用有 declared 关键字的方法。
