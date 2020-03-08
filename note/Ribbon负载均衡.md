## Ribbon负载均衡

### 什么是Ribbon

Ribbon是基于Netflix发布的开源项目，主要功能是提供客户端软件的负载均衡算法和服务调用。简单地说，就是在配置文件中列出Load Balancer后面所有的机器，Ribbon会自动基于某种规则(如简单轮询，随机连接)去连接这些机器，我们很容易使用Ribbon实现负载均衡算法

### Ribbon和Nginx的区别

* Ribbon是本地负载均衡
* Nginx是服务器端的负载均衡

### 使用Ribbon自定义负载均衡算法注意事项

不能放在包含`@ComponentScan`类的当前包和子包路径下，因为`@SpringBootApplication`自带`@ComponentScan`注解，否则自定义的Ribbon负载均衡算法能够被所有的Ribbon客户端共享