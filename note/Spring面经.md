# Spring面经

## 1. 简述一下Spring框架

Spring框架是一个开源的容器性质的轻量级框架

主要有三大特点:

1. 容器
2. IOC
3. AOP

## 2. 简述一下IOC和AOP

* **IOC**: 控制反转，将对象的创建权反转给Spring，实现了程序的解耦
* **AOP**: 依赖注入，前提是IOC，在Spring创建Bean的时候，动态的将依赖对象注入到Bean对象中，也实现了程序的解耦

## 3. 简述一下BeanFactory和ApplicationContext

* **BeanFactory**: 是Spring框架的顶层接口，通过`new BeanFatory()`来启动Spring容器的时候，并不会创建Spring容器里面的对象，只有在`getBean()`时才会创建
* **ApplicationContext**: 替代BeanFactory接口的，与BeanFactory不同的是，通过`new ApplicationContext()`来启动Spring容器的时候，就会创建容器中所有的对象

## 4. Spring的工厂容器有哪些，具体如何启动容器

```java
BeanFactory container = new XmlBeanFactory(new ClassPathResource("applicationContext.xml"));
```

```java
Application container = new ClassPathXmlApplicationContext("applicationContext.xml");
```

## 5. Spring容器中bean标签的理解

`bean`标签用来描述Spring容器管理的对象

* **name**: 给被管理的对象起个名字，获得对象的时候根据名称获得
* **class**: 被管理对象的完整类名
* **scope**: 两种singleton和prototype

## 6. Spring属性注入有哪种方式

* **构造器注入**

```java
public class Bean{}

public class Container{
    private Bean bean;

    public Container(Bean bean){
        this.bean = bean;
    }

    /**略**/
}
```

```xml
<bean id="bean" class="com.wwh.Bean"/>

<bean id="container" class="com.wwh.Container">
    <constructor-arg name="bean" ref="bean"/>
</bean>
```

* **setter注入**

```java
public class Bean{}

public class Container{
    private Bean bean;

    public void setBean(){
        this.bean = bean;
    }
}
```

```xml
<bean id="bean" class="com.wwh.Bean"/>

<bean id="container" class="com.wwh.Container">
    <!-- 写法一 -->
    <property name="bean" ref="bean"/>
    <!-- 写法二 -->
    <!-- <property name="Bean" ref="bean"/> -->

    <!-- 
        spring会将name值的每个单词首字母换成大写
        然后在前面拼接上"set"构成一个方法名，然后
        去对应的类中查找该方法，实现注入
     -->
</bean>
```

* **注解注入**

spring提供了很多注解来实现依赖的注入:

```markdown
@Autowired: 默认是byType，还有byName，constructor两种
@Component: 所有的类加上这个注解都被识别为一个bean
@Repository: dao层注入
@Controller: 控制层注入
@Service: 服务层注入
```

* **p空间注入**

```java
public class C{}

public class Container{
    private String p1;
    private int p2;
    private C p3;
}
```

```xml
<bean id="c" class="com.wwh.C"/>

<bean id="container" class="com.wwh.Container"
      p:p1="property 1"
      p:p2="11"
      p:c-ref="c"/>
```

* **除此之外还有复杂类型注入**

## 7. Spring Bean的生命周期(重要)

* 实例化BeanFactoryPostProcessor实现类
* BeanFactoryPostProcessor.postProcessBeanFactory()
* 实例化BeanPostProcessor实现类
* 实例化InstantiationAwareBeanPostProcessorAdapter实现类
* InstantiationAwareBeanPostProcessorAdapter.posProcessorBeforeInstantiation()
* **执行Bean构造器**
* InstantiationAwareBeanPostProcessor.postProcessPropertyValues()
* **为Bean注入属性**
* BeanNameAware.setBeanName()
* BeanFactoryAware.setBeanFactory()
* **BeanPostProcessor.postProcessBeforeInitialization()**
* InitializingBean.afterPropertiesSet()
* **<bean>的init-method属性指定的初始化方法**
* **BeanPostProcessor.postProcessAfterInitialization()**
* **业务逻辑**
* DisposableBean.destroy()
* **<bean>的destroy-method**

## 8. Spring Bean的作用域

* **singleton**: 单例
* **prototype**: 多例
* **request**: 一次HTTP请求，一个bean定义对应一个实例
* **session**: 一次HTTP Session，一个bean定义对应一个实例
* **global session**: 在一个全局的HTTP Session中，一个bean定义对应一个实例

## 9. 如何开启Spring注解

现在Spring主配置文件中设置`<context:component-scan>`标签来开启注解

## 10. @Resource, @Autowired, @Qualifier区别

* **@Resource**: 默认按照name注入，找不到对应的name才按照type注入
* **@Autowired**: 默认按照type注入，若要按照name注入则要搭配@Qualifier，其参数是类名首字母小写

```java
@Component
public class Car{}

public class App{
    
    @Autowired
    @Qualifier("car")
    private Car car;
}
```

## 11. @Value

```java
@Value("normal")
private String normal; // 注入普通字符串

@Value("#{systemProperties['os.name']}")
private String systemPropertiesName; // 注入操作系统属性

@Value("#{ T(java.lang.Math).random() * 100.0 }")
private double randomNumber; //注入表达式结果

@Value("#{beanInject.another}")
private String fromAnotherBean; // 注入其他Bean属性：注入beanInject对象的属性another，类具体定义见下面

@Value("classpath:com/hry/spring/configinject/config.txt")
private Resource resourceFile; // 注入文件资源

@Value("http://www.baidu.com")
private Resource testUrl; // 注入URL资源
```

## 12. Spring AOP有什么作用

面向切面编程，将纵向重复的代码，横向抽取出来，例如日志等功能

举一个例子，没有Filter之前，解决Servlet的乱码问题需要在Servlet中一一写上`request.setCharacterEncoding("UTF-8")`，有了Filter之后。Filter就解决了所有Servlet的这个问题，因此，所有Servlet的切面就形成了

## 13. Spring AOP主要靠什么实现的

核心思想是动态代理，有JDK动态代理和CGLIB代理

* JDK动态代理面向接口的
* CGLib是通过底层字节码继承要代理的类，假如这个类被final修饰了，对不起，我代理不了
* 假如被代理的对象是接口的实现类，那么使用JDK动态代理
* 假如被代理的对象不是接口的实现类，强制使用CGLib

## 14. 解释一下Spring的一些名词

* **切面Aspect**: 被抽取的公共对象，一个切面可能包含多个连接点和切入点
* **连接点Join Point**: 一个连接点代表一个方法
* **通知Advice**: 包括`@Around`, `@Before`, `@After`
* **切入点Pointcut**: 定义了要对哪些Join Point拦截，比如拦截`add*`, `search*`
* **增强Advice**: 匹配到Join Point之后应该执行什么操作
* **织入Weaving**: 将增强织入到目标对象之后，形成代理对象
* **目标对象Target**: 被代理的对象

## 15. 介绍一下AOP注解的一些使用

```java
package com.wwh.aj;

public interface Interface{}

package com.wwh.aj;

public class Clazz{

    public void method1(){}
    public void method2(){}
    public void method3(){}
    public void method4(){}
}

@AspectJ
public class Aspect{
    /**匹配aj包下所有类的方法**/
    @Pointcut("within(com.wwh.aj)")
    public void matchAjAllMethod(){}

    /**匹配指定Clazz类aop代理对象的方法**/
    @Pointcut("this(com.wwh.aj.Clazz)")
    public void matchClazzAopProxy(){}

    /**匹配实现Interface接口的类的方法**/
    @Pointcut("target(com.wwh.aj.Interface)")
    public void matchInterface(){}

    /**匹配bean里面的方法**/
    @Pointcut("bean(*Car)")
    public void matchBean(){}

    /**匹配方法的执行**/
    @Pointcut("execution(public * com.wwh.aj.Clazz.method*())")
    public void matchExecutionMethod(){}

    /**使用Pointcut实现增强**/
    @Before("matchExecutionMethod()")
    public void before(){
        System.out.println("AOP增强了！！！！")
    }
}
```

## 16. 介绍一下AOP的xml使用

```xml
<!-- 配置切面的bean -->
<bean id="aspect1" class="com.wwh.Aspect1"/>
<bean id="aspect2" class="com.wwh.Aspect2"/>

<aop:config>
    <!-- 配置切面表达式 -->
    <aop:pointcut expression="execution(* com.wwh.aj.Clazz.method1())" id="pointcut"/>
    <!-- 配置切面Advice order可以表示Advice的优先级。越小越高 -->
    <!-- 对应@AspectJ -->
    <aop:aspect ref="aspect1" order="2">
        <aop:before method="match1" pointcut-ref="pointcut"/>
    </aop:aspect>
    <aop:aspect ref="aspect2" order="1">
        <aop:before method="match1" pointcut-ref="pointcut"/>
    </aop:aspect>
</aop:config>
```

## 17. Spring使用了哪些设计模式

* **工厂模式**: BeanFactory或者ApplicationContext
* **代理模式**: jdk动态代理或者cglib代理
* **单例模式**: bean默认为单例模式
* **模板模式**: 解决代码重复性问题，例如JdbcTemplate
* **观察者模式**: 当所有被观察者其中有一个发生变化的时候，其他的被观察者都被通知到变化从而执行逻辑代码

## 18. Spring事务

Spring对事务的支持本质上就是数据库对事务的支持，离开了数据库对事务的支持，就没有Spring对事务的支持

Spring事务有两种:

* **声明式事务**: 建立在AOP之上，通过AOP对方法进行拦截，将事务的处理功能编织到方法中，也就是目标方法开始之前加入一个事务，目标方法执行结束之后根据提交情况回滚事务
* **编程式事务**: 建立在TransactionTemplate上

Spring事务的传播:

|配置参数|解释|
|:---:|:---:|
|PROPAGATION_REQUIRED|如果当前没有事务，就新建一个事务，如果当前存在事务，就加入该事务|
|PROPAGATION_SUPPORTS|如果当前存在事务，就加入该事务，如果当前不存在事务，就以非事务的方式运行|
|PROPAGATION_MANDATORY|如果当前存在事务，就加入该事务，如果当前不存在事务，就抛出异常|
|PROPAGATION_REQUIRES_NEW|不管当前有没有事务，创建一个新的事务|
|PROPAGETION_NOT_SUPPORTED|如果当前不存在事务，继续运行，如果当前存在事务，就把事务挂起|
|PROPAGATION_NEVER|如果当前没有事务，运行，如果当前存在事务，抛出异常|
|PROPAGETION_NESTED|如果当前存在事务，在嵌套事务内运行，如果没有事务，以REQUIRED运行|

Spring事务隔离级别:

和MySQL事务隔离级别一样，即**DEFAULT, RAED_UNCOMMITED, READ_COMMITED, REPEATABLE_READ, SERIALIZABLE**

## 18. 谈谈一个标准的Spring应用的web.xml文件该如何配置

```xml
<!-- 事务管理器 -->
<bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
    <!-- 数据源 -->
    <property name="dataSource" ref="dataSource"/>
</bean>

<!-- 事务通知 -->
<tx:advice id="txAdvice" transaction-manager="transactionManager">
    <tx:attributes>
        <!-- 传播行为 -->
        <tx:method name="save" propagation="REQUIRED"/>
        <tx:method name="find" propagation="SUPPORTS" read-only="true/">
    </tx:attributes>
</tx:advice>

<!-- 事务性切面 -->
<!-- 同上面的aop xml配置 -->
```

## 19. 使用Spring整合SSM框架，需要配置什么东西

* 使用Spring整合Dao，需要数据库启动，配置SqlSessionFactory，配置Mapper动态代理
* 使用Spring整合Service，需要配置包扫描器(开启注解)，配置事务管理(事务管理器，事务通知，事务切面)
* Spring整合Web，需要配置包扫描器(扫描Controller)，配置处理器映射器和处理器适配器，配置视图解析器，并且配置一个SpringMVC前端控制器

```markdown
花了好几个小时整理，累死我了
```
