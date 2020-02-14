MyBatis应用实践
===

[TOC]

## MyBatis环境搭建及入门案例

* pom.xml

```xml
<dependencies>
        <!-- mysql依赖 -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector</artifactId>
            <version>8.0.13</version>
        </dependency>

        <!-- junit测试 -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>

        <!-- mybatis -->
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>3.4.1</version>
        </dependency>

        <!-- log4j -->
        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>1.2.17</version>
        </dependency>
    </dependencies>
```

* 配置SqlMapConfig.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">

<configuration>
    <!-- 配置环境 -->
    <environments default="mysql">
        <!-- 配置mysql -->
        <environment id="mysql">
            <!-- 配置事务的类型 -->
            <transactionManager type="JDBC"/>
            <!-- 配置数据库使用连接池 -->
            <dataSource type="POOLED">
                <!-- 配置数据库连接的基本信息 -->
                <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://localhost:3306/hibernate?serverTimezone=UTC"/>
                <property name="username" value="root"/>
                <property name="password" value="123456"/>
            </dataSource>
        </environment>
    </environments>

    <!-- 指定映射文件的配置位置 -->
    <mappers>
        <mapper resource="mapper/UserMapper.xml"/>
    </mappers>
</configuration>
```

* 配置第一个Mapper, UserMapper.xml

```xml
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="dao">
    <select id="findAllUsers" resultType="dao.User">
        SELECT * FROM t_person
    </select>
</mapper>
```

* UserMapper接口设计

```java
public interface UserMapper {
    List<User> findAllUsers();
}
```

* 封装类型User

```java
public class User implements Serializable {
    private Integer id;
    private String name;
    private Integer age;
    /*getter and setter*/
}
```

* 注解配置运行MyBatis

其本质就是通过注解完成了XXXMapper.xml的功能

```java
public interface UserMapperAnnotation {
    @Select("select * from user")
    List<User> findAllUsers();
}
```

## MyBatis CRUD操作

### 单表保存

在接口中声明保存方法

```java
public interface UserMapper {
    List<User> findAllUsers();
    List<User> findOneUser(String username);
    void saveUser(User user);
}
```

更新Mapper拦截的接口方法

```xml
<insert id="saveUser" parameterType="dao.User">
    INSERT INTO user VALUES(#{id}, #{name}, #{age})
</insert>
```

执行save操作

```java
public void $save() throws IOException{
        InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        SqlSessionFactory factory = builder.build(in);
        SqlSession session = factory.openSession();

        UserMapper userMapper = session.getMapper(UserMapper.class);

        User user = new User();
        user.setId(5);
        user.setName("王焱");
        user.setAge(23);

        userMapper.saveUser(user);

        //事务一定要提交，不然写入不进数据库
        session.commit();
    }
```

### 模糊查询

```xml
<select id="findByLikeName" parameterType="string" resultType="dao.User">
        SELECT * FROM user WHERE username LIKE #{name}
    </select>
```