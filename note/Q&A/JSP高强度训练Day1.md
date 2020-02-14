# JSP高强度训练Day1

1. HTTP/CSS/JavaScript属于静态资源,Jsp/Servlet属于动态资源
2. 当用request方法获得form中的元素的时候，默认的字符编码为`ISO-8859-1`
3. jsp不能直接访问数据库，和java程序一样，需要连接数据库
4. `/WEB-INF`是web程序的访问根目录
5. `<%@page%>`作用于整个jsp页面，一个jsp可以使用多个该标签，但是为了增强程序的健壮性，一般将该标签放在开头，但不是必须的
6. 使用jdbc访问数据库的时候，常见的接口有`Statement`,`ResultSet`,`PreparedStatement`
7. jsp应用程序的配置文件的根目录是`<web-app>`
8. jsp的隐式注释为`<%-- --%>`
9. jsp中要导入`java.util.*`需要`<%@page%>`
10. 动态网页就是和后台有交互的
11. 分页查询，可以减轻服务器的压力，提高用户的体验，对于不同的数据库有不同的SQL命令
12. jsp中，可以用`response.sendRediect()`实现网页的重定向
13. post请求解决乱码问题可以采用重建字符串
14. get请求采用`setCharacterEncoding()`来解决乱码问题
15. post请求比get请求更安全，并且承载量大
16. 重定向是一次新的请求
17. 发送一次请求会产生一个`request`和一个`response`对象
18. `application`内置对象的作用最大，最广
19. 设置cookie有效期的是`setMaxAge(int expire)`，一般expire是大于0的数字，但是如果expire为0就表示立即删除cookie，而当expire为负数的时候，表示当前窗口关闭，cookie失效
20. jsp四大域的大小范围排序是:`application > session > request > page`
21. 网页关闭的时候，session并不会立刻过期，当session到达过期时间的时候或者服务器关闭的时候，session才会过期
22. 释放session对象调用的是`session.invalidate()`
23. cookie不能在不同用户之间进行传播
24. 会话跟踪技术包括:`URL重写`,`隐藏表单域`,`Cookie`
25. Servlet主要在`web.xml`中进行配置
26. jsp获取复选框中的内容通过`request.getParameterValues()`
27. `<form>`中默认的表单提交方式为`GET`
28. `GenericServlet`实现了`Servlet`和`ServletConfig`接口
29. 在web.xml中使用`<filter>`和`<filter-mapping>`接口
30. jsp生成的class文件在tomcat目录下的`work`文件夹下
31. servlet中通过`web.xml`中指定`<load-on-startup>`指定容器加载的顺序
32. jsp中if-else语句使用下列标签:`<c:choose>`,`<c:when>`,`<c:otherwise>`
33. jstl中，条件标签有`<c:if>`,`<c:choose>`
34. 使用jstl首先要在jsp中添加`taglib`
35. request域中存放了一个name属性，通过el表达式`${requestScope.name}`获得该属性
