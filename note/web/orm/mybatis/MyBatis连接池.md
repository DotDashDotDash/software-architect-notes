# MyBatis连接池

## MyBatis连接池的分类

* **POOLED**: 采用传统的javax.sql.DataSource规范中的连接池
* **UNPOOLED**: 采用传统的获取连接的方式，虽然也采用了javax.sql.DataSource，但是没有池的技术，每次都需要重新连接
* **JNDI**: 采取服务器的JNDI技术来获取DataSource，不同的服务器所能拿到的是不一样的，**如果不是web或者maven的war工程，是不能使用的**，采用tomcat服务器，采用的连接池就是dbcp

## UNPOOLED

MyBatis以UNPOOLED方式获得JDBC连接的时候，使用的是传统的JDBC连接方式，大概流程如下:

```java
//加载驱动
Class.forName(...)

//获得连接
Connection getConnection(){
    DriverManager.getConnection();
}
```

## POOLED

POOLED本质是一个`ArrayList<PooledConnection>`，以集合的方式存储JDBC连接，核心方法是`popConnection(username, password)`方法

```java
private PooledConnection popConnection(String username, String password){
    //需要返回的对象
    PooledConection conn = null;
    //这里应该是如果获取不到，那么就一直阻塞
    while(conn == null){
        synchronized(state){    //连接池必须是线程安全的，两次请求不能获得同一个连接
            if(!state.idleConnections.isEmpty()){
                conn = state.idleConnections.remove(0); //每次从首位取
            }else{
                if(state.activeConnections.size() < poolMaximumActiveConnnections>){
                    //活动的连接池数量小于最大数量
                    conn = new PooledConnection(dataSource.getConnection(), this);
                    //可以看出来，连接池如果还能接着容纳新的连接
                    //就创建一个新的连接
                }else{
                    //活动的不足
                    PooledConnection oldest = state.idleConnections.get(0); //FIFO，第一个最老
                    //等待一段时间
                    //可以利用，重置该线程
                }
            }
        }
    }
}
```
