# Hibernate知识点梳理

## 何为ORM

ORM(Object-Relational-Mapping)，对象关系映射，为了解决数据库的存储单元和对象之间的模型不匹配问题，本质是将数据从一种形式转换为另外一种形式

## SessionFactory和Session

SessionFactory一般用来创建Session，其中SessionFactory是Hibernate的一个存储概念，是**线程安全的**，SessionFactory可以被多个线程并发访问，一般在应用程序启动的时候进行创建，**一个应用程序应该改维护一个SessionFactory来构建单例模式**，因为SessionFactory的构建非常消耗资源。Session由SessionFactory构建，是**非线程安全的**，一般在Hibernate3之后就会将Session与ThreadLocal进行绑定，这样一个线程拥有一个Session(具体措施见Hibernate相关配置)

## Hibernate的load()与get()的区别

* 在hibernate3之前，get()方法首先查询一级缓存，一级缓存不存在的话，直接越过二级缓存查询数据库，而hiber3之后，get()不再对二级缓存只读不写，也可以访问二级缓存
* get()方法返回实体类对象，而load()返回实体对象的代理对象
* get()查询的数据可以不存在，而load()查询的数据(代理)一定要存在，否则就会抛出异常

那么何为代理对象呢?

**所谓的代理对象就是调用load()的时候，并不向数据库直接发起SQL查询，而是创建一个临时的对象来保存id，当我们使用到这个对象的时候，才会发起SQL查询，而get()是不管我们用到用不到这个对象，直接发起SQL查询**

## Session的save(),update(),merge(),saveOrUpdate(),persist()分别做什么，有什么区别

* save(): 瞬时态转化为持久态
* persist(): 瞬时态转化为持久态
* update(): 游离态转化为持久态
* saveOrUpdate(): 游离态/瞬时态转化为持久态
* lock(): 游离态转化为持久态
* merge()可以完成save()和update()的操作，目的是将新的状态转移到已经存在的持久化的对象之上或者创建新的对象

## 阐述Session加载实体对象的过程

* Session加载的时候首先会根据实体对象的类型和id在hibernate的一级缓存中寻找，如果找到则直接返回，如果没有找到，会查询NoExist记录，如果NoExist记录中存在同样的查询条件，那么直接返回null，宣告此次查询失败
* 如果一级缓存查找失败那么直接在二级缓存中查找，如果二级缓存命中那么直接返回，如果二级缓存没有命中，那么将会发出SQL语句，同时将此次查询的记录添加到NoExist中(相当于一个查询黑名单)，并且返回null
* 根据映射配置和SQL语句创建响应的实体对象
* 将对象纳入一级缓存Session的管理
* 如果由对应的拦截器，那么直接执行拦截器的onLoad()方法
* 如果hibernate开启了二级缓存，那么将对象纳入到二级缓存中进行管理
* 返回数据对象

## list()和iterator()方法之间的区别

* list()无法利用一二级缓存(对缓存只写不读)，他只能在开启缓存的前提之下利用缓存，list()不会引起N+1查询问题
* iterator()可以充分的利用一二级的缓存，当发出大量的读操作的时候，那么可以大大提高程序的性能，iterator()可能会引起查询问题

## Hibernate如何实现分页查询

HQL语句(Session#createQuery()或者查询条件Session#createCriteria()方法，设置起始行数Query/Criteria#setFirstResult()以及最大查询行数setMaxResults())，并调用Query或者Criteria接口的list()方法，此时hibernate会自动分页

```java
Configuration cfg = new Configuration();
cfg.configure();
SessionFactory factory = cfg.buildSessionFactory();
Session session = factory.openSession();
Transaction tx = session.beginTransaction();

Query query = session.createQuery("from User");
//分页
query.setFirstResult(0);
query.setMaxResults(1);

List r = query.list();
for(Object o : r){
    System.out.println(o);
}
tx.commit();
```

## 阐述Hibernate的乐观锁和悲观锁的机制

Hibernate中通过Session的get()和load()方法从数据库中加载对象时可以通过参数指定使用悲观锁；而乐观锁可以通过给实体类加整型的版本字段再通过XML或@Version注解进行配置

## 如何理解hibernate的延迟加载?延迟加载和Session关闭的矛盾如何处理

延迟加载就是并不是在读取的时候就把数据加载进来，而是等到使用时再加载。Hibernate使用了虚拟代理机制实现延迟加载，我们使用Session的load()方法加载数据或者一对多关联映射在使用延迟加载的情况下从一的一方加载多的一方，得到的都是虚拟代理，简单的说返回给用户的并不是实体本身，而是实体对象的代理。代理对象在用户调用getter方法时才会去数据库加载数据。但加载数据就需要数据库连接。而当我们把会话关闭时，数据库连接就同时关闭了

**延迟加载与session关闭的矛盾一般可以这样处理**：

* 关闭延迟加载特性。这种方式操作起来比较简单，因为Hibernate的延迟加载特性是可以通过映射文件或者注解进行配置的，但这种解决方案存在明显的缺陷。首先，出现"no session or session was closed"通常说明系统中已经存在主外键关联，如果去掉延迟加载的话，每次查询的开销都会变得很大。
* 在session关闭之前先获取需要查询的数据，可以使用工具方法Hibernate.isInitialized()判断对象是否被加载，如果没有被加载则可以使用Hibernate.initialize()方法加载对象。
* 使用拦截器或过滤器延长Session的生命周期直到视图获得数据。Spring整合Hibernate提供的OpenSessionInViewFilter和OpenSessionInViewInterceptor就是这种做法。

## 如何实现多对多

Hibernate基于xml的配置仅仅支持one-to-one或者one-to-many或者many-to-one而不支持many-to-many，需要在多对多的实体类上配置@ManyToMany注解，但是在实际项目开发中，基本上都是将多对多关系拆解成一对多或者多对一来配置

## 谈一下你对继承映射的理解

* 每个继承结构一张表，不管多少个子类都是一张表
* 每个子类一张表，公共信息一张表，特殊信息一张表
* 每个具体类一张表，有多少个子类就有多少个表

## Hibernate的缓存结构

Hibernate的Session提供了一级缓存的功能，默认总是有效的，当应用程序保存持久化实体、修改持久化实体时，Session并不会立即把这种改变提交到数据库，而是缓存在当前的Session中，除非显示调用了Session的flush()方法或通过close()方法关闭Session。通过一级缓存，可以减少程序与数据库的交互，从而提高数据库访问性能。

SessionFactory级别的二级缓存是全局性的，所有的Session可以共享这个二级缓存。不过二级缓存默认是关闭的，需要显示开启并指定需要使用哪种二级缓存实现类（可以使用第三方提供的实现）。一旦开启了二级缓存并设置了需要使用二级缓存的实体类，SessionFactory就会缓存访问过的该实体类的每个对象，除非缓存的数据超出了指定的缓存空间。

一级缓存和二级缓存都是对整个实体进行缓存，不会缓存普通属性，如果希望对普通属性进行缓存，可以使用查询缓存。**查询缓存是将HQL或SQL语句以及它们的查询结果作为键值对进行缓存，对于同样的查询可以直接从缓存中获取数据**。查询缓存默认也是关闭的，需要显示开启

## Hibernate的DetachedCriteria有什么用

DetachedCriteria和Criteria的用法基本上是一致的，但Criteria是由Session的createCriteria()方法创建的，也就意味着离开创建它的Session，Criteria就无法使用了。DetachedCriteria不需要Session就可以创建（使用DetachedCriteria.forClass()方法创建），所以通常也称其为离线的Criteria，在需要进行查询操作的时候再和Session绑定（调用其getExecutableCriteria(Session)方法），这也就意味着一个DetachedCriteria可以在需要的时候和不同的Session进行绑定。

## @OneToMany注解的mappedBy属性有什么作用

@OneToMany用来配置一对多关联映射，但通常情况下，一对多关联映射都由多的一方来维护关联关系，例如学生和班级，应该在学生类中添加班级属性来维持学生和班级的关联关系（在数据库中是由学生表中的外键班级编号来维护学生表和班级表的多对一关系），如果要使用双向关联，在班级类中添加一个容器属性来存放学生，并使用@OneToMany注解进行映射，此时mappedBy属性就非常重要。如果使用XML进行配置，可以用标签的inverse="true"设置来达到同样的效果
