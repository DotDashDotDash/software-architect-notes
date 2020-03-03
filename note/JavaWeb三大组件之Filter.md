# JavaWeb三大组件之Filter

## Filter简介

WEB开发人员通过Filter技术，对web服务器管理的所有web资源：例如Jsp,
Servlet, 静态图片文件或静态html文件等进行拦截，从而实现一些特殊的功能。例如实现URL级别的权限访问控制、过滤敏感词汇、压缩响应信息等一些高级功能。

Servlet API中提供了一个Filter接口，开发web应用时，如果编写的Java类实现了这个接口，则把这个java类称之为过滤器Filter。通过Filter技术，开发人员可以实现用户在访问某个目标资源之前，对访问的请求和响应进行拦截，Filter接口源代码:

```java
public abstract interface Filter{
    public abstract void init(FilterConfig paramFilterConfig) throws ServletException;
    public abstract void doFilter(ServletRequest paramServletRequest, ServletResponse paramServletResponse, FilterChain 
        paramFilterChain) throws IOException, ServletException;
    public abstract void destroy();
}
```

## Filter的工作流程

Filter接口中有一个doFilter方法，当我们编写好Filter，并配置对哪个web资源进行拦截后，WEB服务器每次在调用web资源的service方法之前，都会先调用一下filter的doFilter方法，因此，在该方法内编写代码可达到如下目的：

* 调用目标资源之前，让一段代码执行。
* 是否调用目标资源（即是否让用户访问web资源）。
* 调用目标资源之后，让一段代码执行。

web服务器在调用doFilter方法时，会传递一个filterChain对象进来，filterChain对象是filter接口中最重要的一个对象，它也提供了一个doFilter方法，开发人员可以根据需求决定是否调用此方法，调用该方法，则web服务器就会调用web资源的service方法，即web资源就会被访问，否则web资源不会被访问。

## Filter部署

```xml
<filter>
    <description>过滤器名称</description>
    <filter-name>自定义的名字</filter-name>
    <filter-class>com.yangcq.filter.FilterTest</filter-class>
    <!--配置FilterTest过滤器的初始化参数-->
    <init-param>
        <description>配置过滤器的初始化参数</description>
        <param-name>name</param-name>
        <param-value>gacl</param-value>
    </init-param>
    <init-param>
        <description>配置FilterTest过滤器的初始化参数</description>
        <param-name>like</param-name>
        <param-value>java</param-value>
    </init-param>
</filter>
<filter-mapping>
    <filter-name>testFilter</filter-name>
    <url-pattern>/index.jsp</url-pattern>
    <dispatcher>REQUEST</dispatcher>
    <dispatcher>FORWARD</dispatcher>
</filter-mapping>
```

`<dispatcher>`子元素可以设置的值及其意义：

* REQUEST：当用户直接访问页面时，Web容器将会调用过滤器。如果目标资源是通过RequestDispatcher的include()或forward()方法访问时，那么该过滤器就不会被调用。
* INCLUDE：如果目标资源是通过RequestDispatcher的include()方法访问时，那么该过滤器将被调用。除此之外，该过滤器不会被调用。
* FORWARD：如果目标资源是通过RequestDispatcher的forward()方法访问时，那么该过滤器将被调用，除此之外，该过滤器不会被调用。
* ERROR：如果目标资源是通过声明式异常处理机制调用时，那么该过滤器将被调用。除此之外，过滤器不会被调用。

## 参考链接

* [Java三大器之Filter](https://blog.csdn.net/reggergdsg/article/details/52821502)
