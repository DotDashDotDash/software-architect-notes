# JavaWeb三大组件之Listener

没什么好说的，一般listener用的形式很规格化，没什么花里胡哨的，但是下面还是说一下session的钝化和活化

## session的钝化和活化

### xml配置

* 全局配置: tomcat/conf/web.xml的`<context>`标签中配置
* 在IDEA与/WEB-INF同级创建一个/META-INF文件夹，下面创建一个context.xml文件

```xml
<context>
    <!-- 钝化的时间是maxIdleSwap -->
    <Manager className="org.apache.catalina.session.PersistentManager" maxIdleSwap="1">
        <!-- 钝化后存放的文件夹是gac1 -->
        <Store className="org.apache.catalina.session.FileStore" directory="gacl"/>
    </Manager>
</context>
```

## 代码流程

* 创建一个类实现HttpSessionActivationListener接口A
* 创建一个context.xml或者全局配置
* jsp文件或者其他的文件中通过session.setAttribute("activation", new A())

## 参考链接

* [JavaWeb之Listener](https://www.jb51.net/article/92032.htm)
