# JavaWeb三大组件之Servlet

## Servlet概述

### 实现Servlet的方式

* 实现javax.servlet.Servlet接口
* 继承javax.servlet.GenericServlet类
* 继承javax.servlet.HttpServlet类(最常见)

下面看javax.servlet.Servlet接口:

```java
//Servlet.java
public interface Servlet{
    /**
     * ServletConfig是由服务器创建的，对应的是web.xml有关Servlet的定义
     * 服务器会根据web.xml的<servlet>...</servlet>中的定义来生
     * ServletConfig实例
     */
    public void init(ServletConfig config) throws ServletException;
    public ServletConfig getServletConfig();
    public String getServletInfo();
    public void destroy();

    //重要，用来处理请求的方法
    public void service(ServletRequest request, ServletResponse response)
        throws ServletException, IOException;
}
```

下面是web.xml中关于Servlet的配置:

```xml
<!-- web.xml -->
<!-- 下面的配置是为了将访问路径和一个Servlet绑定 -->
<servlet>
    <!-- 将Servlet的名称定义为hello -->
    <servlet-name>hello</servlet-name>
    <!-- 指定使用的Servlet -->
    <servlet-class>com.pkt.AServlet</servlet-class>
    <!-- 指定是否在服务器启动的时候创建该实例 -->
    <!-- 下面的参数表示启动的顺序1,2,3,4,5... -->
    <load-on-startup>1</load-on-startup>
    <!-- 指定servlet初始化参数 -->
    <init-param>
        <param-name>paramName1</param-name>
        <param-value>paramValue1</param-value>
    </init-param>
    <!-- 初始化参数可以不为一个 -->
</servlet>
<servlet-mapping>
    <servlet-name>hello</servlet-name>
    <url-pattern>/helloservlet</url-pattern>
</servlet-mapping>
```

### Servlet创建的流程

* 服务器会在Servlet第一次被访问的时候创建Servlet，或是在服务器启动的时候创建(这需要在web.xml中配置)，默认情况下，Servlet是在第一次被访问的时候被创建，并且一个Servlet类只有一个实例，Servlet由服务器创建！！(例如tomcat)
* 服务器调用init()来创建实例，并且init()只会调用一次
* 服务器每收到一次请求，就会调用一次service()来进行服务
* Servlet的卸载是通过destroy()来进行的，在这个方法中会进行资源的释放等

## web.xml

web.xml继承自${CATALINA_HOME}/conf/web.xml中的内容，它是所有web.xml的父文件，下面是web.xml的配置

```xml
<!-- 其余略 -->
<!-- web.xml -->
<servlet>
    <!-- 优先级最低，如果路径不匹配任何一个Servlet，将执行它 -->
    <servlet-name>default</servlet-name>
    <servlet-class>org.apache.catalina.servlets.DefaultServlet</servlet-class>
    <!-- ... -->
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>default</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>

<!-- jsp -->

<!-- session -->

<!-- MIME -->
<mime-mapping>
    <extension>bmp</extension>
    <mime-type>image/bmp</mime-type>
<mime-mapping>
<mime-mapping>
    <extension>htm</extension>
    <mime-type>text/html</mime-type>
</mime-mapping>

<!-- 欢迎界面 -->
<welcome-file-list>
    <welcome-file>index.html</welcome-file>
    <welcome-file>index.htm</welcome-file>
    <welcome-file>index.jsp</welcome-file>
</welcome-file-list>
```

## ServletContext

**一个项目只有一个ServletContext对象!**，但是所有的Servlet都能获得到这个ServletContext，它随服务器启动而产生，随服务器关闭而消亡

它的作用就是在整个Web应用的动态资源之间共享数据

```java
//获取ServletContext的方式
ServletConfig.getServletContext()
GenericServlet.getServletContext()
HttpSession.getServletContext()
ServletContextEvent.getServletContext()
```

## 域对象的功能

域对象就是在多个Servlet中传递数据的

* 域对象必须要有存数据功能
* 域对象必须要有取数据功能

域对象内部其实有一个Map，ServletContext是JavaWeb四大域对象之一:

* PageContext
* ServletRequest
* HttpSession
* ServletContext

下面是web.xml中ServletContext的配置

```xml
<!-- web.xml -->
<web-app>
    <conetxt-param>
        <param-name>paramName1</param-name>
        <param-value>paramValue1</param-value>
    </context-param>
    <context-param>
        <param-name>paramName2</param-name>
        <param-value>paramValue2</param-value>
    </context-param>
</web-app>
```

在java程序中获取配置信息

```java
ServletContext context = this.getServletContext();
String value1 = context.getInitParameter("paramName1");
String value2 = context.getInitParameter("paramName2");
```

## 参考链接

* [javaweb之servlet全解](https://www.cnblogs.com/duscl/p/4899781.html)
