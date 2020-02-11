# JavaWeb面经

## 1. 启动项目时如何实现不在链接里输入项目名称就能启动

修改tomcat文件:

```xml
<Context docBase="userManager" path="/userManager" reloadable="true" .../>
```

改为:

```xml
<Context docBase="userManager" path="/" reloadable="true"/>
```

## 2. 一分钟只能只能处理1000个请求，怎么实现，手撕代码

```java
public class Main{
    //处理中的请求
    private List<Node> proessing = new ArrayList<Node>();
    //等待的请求
    private Queue<Node> waiting = new LinkedList<Node>();

    private volatile int servedUtilNow = 0;
    private static final long delay = 0;
    private static final long interval = 60 * 1000;

    TimerTask task = new TimerTask(){
        @Override
        public void run(){
            servedUtilNow = 0;
        }
    };

    public void start(){
        //接受请求

        {
            if(servedUtilNow > 1000){
                //放入等待队列
            }else{
                //正常处理
            }

            new Timer().scheduleAtFixedRate(task, delay, interval);
        }
    }
}
```

## 3. 说明一下jsp中的静态包含和动态包含的区别

静态包含是通过jsp的include指令包含页面的，而动态包含是通过`<jsp:forward>`包含的，静态包含为编译时包含，两个相关联的  `contentType`应该保持一致，并且如果包含的页面不存在的时候会产生编译错误，两个页面合二为一，只产生一个class文件，而动态包含是运行时包含，可以向被包含的页面传递参数，产生两个class文件，被包含的页面不存在也不会报错


```jsp
<!-- 静态包含 -->
<%@ include file="" %>

<!-- 动态包含 -->
<jsp:include page="">
    <jsp:param name="" value""/>
</jsp:include>
```

## 4. 谈谈jsp有哪些内置页面(9个)

* **request**: 封装客户端的请求，其中可能是GET或者POST
* **response**: 封装服务器端对客户端的响应
* **pageContext**: 通过该对象可以获得其他对象
* **session**: 封装用户会话
* **application**: 封装服务器运行环境对象
* **out**: 服务器响应的输出流对象
* **config**: web应用配置的对象
* **page**: jsp页面本身
* **exception**: 封装页面抛出异常的对象

## 5. 谈谈jsp工作原理

在MVC中，将Servlet和jsp的功能分开了，其实jsp本身就是一个Servlet，能够运行Servlet的容器通常也是一个jsp容器。容器会编译Servlet，之后容器会加载和实例化Java字节码并执行，如果jsp被修改过，就会被重新编译

## 6. 谈谈EL

* pageContext
* initParam（访问上下文参数）
* param（访问请求参数）
* paramValues
* header（访问请求头）
* headerValues
* cookie（访问cookie）
* applicationScope（访问application作用域）
* sessionScope（访问session作用域）
* requestScope（访问request作用域）
* pageScope（访问page作用域）

## 7. 说说jsp的动作

* **jsp:include**: 动态包含页面
* **jsp:useBean**: 寻找或者实例化一个javaBean
* **jsp:setProperty**: 设置javaBean的属性
* **jsp:getProperty**: 或者javaBean的属性
* **jsp:forward**: 转发一个请求
* **jsp:pluguin**: 根据浏览器的类型为一个javaBean设置object或者embed属性

## 8. web.xml可以配置哪些内容

```xml
<!-- Spring的配置，启动时创建Ioc容器 -->
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:applicationContext.xml</param-value>
</context-param>

<!-- 配置Spring上下文监听器 -->
<listener>
    <listener-class>
        org.springframework.web.context.ContextLoaderListener
    </listener-class>
</listener>

<!-- 配置Spring的OpenSessionInView来解决延迟加载和Hibernat绘会话关闭的矛盾 -->
<filter>
    <filter-name>openSessionInView</filter-name>
    <filter-class>
        org.springframework.or.hibernate3.support.OpenSessionInViewFilter
    <filter-class>
</filter>

<filter-mapping>
    <filter-name>openSessionInView</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>

<!-- 配置会话超时时间 -->
<session-config>
    <session-timeout>10</session-timeout>
<session-config>

<!-- 配置错误页面 -->
<error-page>
    <error-code>404<error-code>
    <location>/error.jsp</location>
</error-page>

<error-page>
    <error-type>java.lang.Exception</error-type>
    <location>/error.jsp</location>
</error-page>

<!-- 配置安全认证方式，略 -->
```

## 9. 过滤器filter有什么作用

* 对用户请求进行统一认证
* 对用户的访问请求进行记录和审核
* 对用户发送的数据进行过滤或替换
* 转换图象格式
* 对响应内容进行压缩以减少传输量
* 对请求或响应进行加解密处理
* 触发资源访问事件
* 对XML的输出应用XSLT等

## 10. 请问使用Servlet如何获取用户配置的初始化参数以及服务器上下文参数

* **ServletConfig.getInitParameter()**
* **ServletConfig.getServletContext().getInitParameter()**
* **request.getServletContext().getInitParameter()**


## 11. 如果获得用户请求参数

* **request.getParameter("name")**
* **request.getParameterValues()**
* **request.getParameterMap()**

## 12. 服务器收到了用户提交的表单数据，那么用了什么方法

doGet()和doPost()的其中任何一种或者全部，因为HttpServlet继承自GenericServlet，重写了`service()`，HttpServlet会首先处理用户请求的方法，根据处理的结果选择调用doGet()，doPost()，doPut()，doDelete()，但是大部分时候，我们都可以重写doGet()或者doPost()，也可以自定义Servlet，重写service()，因此这题并没有准确答案

## 13. 如何在基于Web的应用中实现文件上传和文件下载

Apache的commons-fileupload，利用Part实现

## 14. Servlet异步特性

异步特性和多线程并不互相矛盾，如果一个任务耗时特别长，那么会出现一个Servlet长时间占用请求处理线程不释放，当并发量增加的时候，就会出现web卡死的情况

异步特性非常适合用户需要长时间处理并且需要返回结果的情况，可以节省容器的资源

```java
@WebServlet(urlPatterns = "/async", asyncSupported = true)
public class MyAsyncServlet extends HttpServlet{
    @Override
    public void doGet(HttpServlet request, HttpServlet response) throws Exception{
        request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED");
        AsyncContext ctx = request.startAsync();
        ctx.start(new Runnable(){
            @Override
            public void run(){
                //逻辑
                ctx.complete();
            }
        });
    }
}
```

## 15. Servlet和CGI的区别

Servlet运行与服务器进程中，一个Servlet可以处理多个请求，而CGI为每一个请求创建一个线程，效率上来说，CGI效率要低于Servlet

## 16. Servlet生命周期

* **init**
* **service**
  * doGet
  * doPost
* **destroy**

## 17. forward和redirect区别

* forward是服务器请求资源，服务器直接访问该URL，并将内容抓取出来提交给用户，对于用户浏览器而言，根本不知道页面从哪里来的，因此地址栏还是原来的地址栏，**可以共享数据**，效率高
* redirect发给浏览器一个状态码，告诉浏览器应该重新访问这个地址，因此地址栏会发生变化，**不能共享数据**，效率低

## 18. 使用标签库有什么好处

* 分离页面和逻辑
* 开发者可以自定义标签来封装业务逻辑和显示逻辑
* 标签具有很好的移植性
* 标签一定程度上替代了脚本的使用

## 19. 你做过的项目中，哪些用到了JSTL

* `<c:if>`
* `<c:choose>`
* `<c:when>`
* `<c:otherwise>`
* `<c:forEach>`

## 20. Cookie和Session区别

* Cookie是服务器生成给客户端，存在于客户端的浏览器当中保存的
* Session是存放在服务器端用于标识用户的
* 浏览器禁用Cookie若要保持Session会话中的内容，要通过encodeURL或者encodeRedirectURL来保持数据的共享
