# Java泛型

## 泛型的特点

* 不能实例化泛型类
* 静态变量或者普通方法不能引用泛型变量，但是静态泛型方法是允许的
* 基本类型无法作为泛型类型
* 无法使用instanceof或者==来判断泛型类型
* 泛型类的原生类型与所传递的泛型类型无关，无论传递什么类型，原生类型是一样的
* 泛型数组可以声明但是无法实例化
* 泛型类不能继承Exception或者Throwable
* 不能捕获泛型异常

## 泛型类型继承规则

* 泛型参数是继承关系的泛型类之间没有继承关系
* 泛型类可以继承其他类，例如

```java
public class A<T> extends B<T>{}
```

* 泛型类型的继承关系在使用中仍然会收到泛型类型的影响

## 通配符类型

* `<? extends A>`决定了上界
* `<? super B>`决定了下界
* `<?>`指没有限制的泛型类型

## 获取泛型的参数类型Type类

```java
/**
 * Author：Jay On 2019/5/11 22:41
 * <p>
 * Description: 获取泛型类型测试类
 */
public class GenericType<T> {
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static void main(String[] args) {
        GenericType<String> genericType = new GenericType<String>() {};
        Type superclass = genericType.getClass().getGenericSuperclass();
        //getActualTypeArguments 返回确切的泛型参数, 如Map<String, Integer>返回[String, Integer]
        Type type = ((ParameterizedType) superclass).getActualTypeArguments()[0]; 
        System.out.println(type);//class java.lang.String
    }
}
```

## JVM如何实现泛型

Java泛型是Java1.5之后才引入的，为了向下兼容。Java采用了C++完全不同的实现思想。Java中的泛型更多的看起来像是编译期用的

Java中泛型在运行期是不可见的，会被擦除为它的上级类型。如果是没有限定的泛型参数类型，就会被替换为Object.

```java
GenericClass<String> stringGenericClass=new GenericClass<>();
GenericClass<Integer> integerGenericClass=new GenericClass<>();
```

C++中`GenericClass<String>`和`GenericClass<Integer>`是两个不同的类型

Java进行了类型擦除之后统一改为`GenericClass<Object>`

```java
/**
 * Author：Jay On 2019/5/11 16:11
 * <p>
 * Description:泛型原理测试类
 */
public class GenericTheory {
    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("Key", "Value");
        System.out.println(map.get("Key"));
        GenericClass<String, String> genericClass = new GenericClass<>();
        genericClass.put("Key", "Value");
        System.out.println(genericClass.get("Key"));
    }

    public static class GenericClass<K, V> {
        private K key;
        private V value;

        public void put(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public V get(V key) {
            return value;
        }
    }

    /**
     * 类型擦除后GenericClass2<Object>
     * @param <T>
     */
    private class GenericClass2<T> {

    }

    /**
     * 类型擦除后GenericClass3<ArrayList>
     * 当使用到Serializable时会将相应代码强制转换为Serializable
     * @param <T>
     */
    private class GenericClass3<T extends ArrayList & Serializable> {

    }
}

对应的字节码文件
 public static void main(String[] args) {
        Map<String, String> map = new HashMap();
        map.put("Key", "Value");
        System.out.println((String)map.get("Key"));
        GenericTheory.GenericClass<String, String> genericClass = new GenericTheory.GenericClass();
        genericClass.put("Key", "Value");
        System.out.println((String)genericClass.get("Key"));
    }
```
