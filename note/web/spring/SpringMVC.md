# SpringMVC

## MVC流程

<div align=center><img src="../../../assets/mvc.png"></div>

## MVC注解

<div align=center><img src="../../../assets/SpringMVC注解.png"></div>

## ContextLoaderListener

* web应用可以没有web.xml，web.xml主要是用来初始化配置信息，如果没有下面的配置可以不要web.xml
  * Welcome页面
  * servlet
  * servlet-mapping
  * filter
  * listener
  * 启动级加载级别
* 当要启动web项目时，服务器软件或者容器(tomcat等)会首先加载web.xml，通过其中的配置来启动项目，只有各种配置均无误的时候，项目才能正确启动。web.xml有多个标签，在加载中的顺序为:
  * context-param
  * listener
  * filter
  * servlet

<div align=center><img src="../../../assets/web.png"></div>

* ContextLoaderListener在web.xml中的配置

```xml
<!-- 配置contextConfigLocation初始化参数 -->
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>/WEB-INF/applicationContext.xml</param-value>
</context-param>
<!-- 配置ContextLoaderListener-->
<listener>
    <listener-class>
        org.springframework.web.context.ContextLoaderListener
    </listener-class>
</listener>
```

## DispatcherServlet

DispathcerServlet应用了设计模式的“前端控制器”的模式，其在web.xml中的定义为:

```xml
<servlet>
    <servlet-name>dispatcher</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <!-- 容器启动时初始化该Servet -->
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>dispatcher</servlet-name>
    <!-- 用来定义默认的servlet映射，也可以用如*.html来映射，表示拦截所有html为拓展名的请求 -->
    <url-pattern>/</url-pattern>
</servlet-mapping>
```

每个DispatcherServlet都有自己的WebApplicationContext(这里不是很懂，难道DispatcherServlet不应该是单例模式吗)，又继承了(root)WebApplicationContext对象中已经定义的所有的bean。这些继承可以在具体的Servlet实例中被重载，也可以在其scope下定义新的bean。

<div align=center><img src="../../../assets/webapp.png"></div>
