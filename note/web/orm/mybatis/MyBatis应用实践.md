# MyBatis应用实践

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

## MyBatis事务配置

MyBatis是通过SqlSession的`commit()`和`rollback()`方法

打开自动事务提交功能(平常开发不推荐使用)

```java
Session sqlSession = factory.openSession(true);
```

## MyBatis ORM问题

场景: 表的列名和需要封装的实体类的属性名称不一致

假定ModifiedUser定义如下:

```java
public class ModifiedUser implements Serializable {
    private Integer userId;
    private String userName;
    private Integer userAge;
    /*getter and setter*/
}
```

```xml
<!-- 设定别名，匹配表的列名和实体类的属性名一致 -->
<select id="findAllModifiedUsers" resultType="dao.ModifiedUser">
        SELECT id as userId, name as userName, age as userAge
        FROM user
</select>

<!-- 或者设置resultMap一次匹配完 -->
<resultMap id="modifiedUserMap" type="dao.ModifiedUser">
        <!-- 主键对应的字段 -->
        <id property="userId" column="id"/>
        <!-- 非主键对应的字段 -->
        <result property="userName" column="name"/>
        <result property="userAge" column="age"/>
</resultMap>
```

### MyBatis标签

* `if`标签

```xml
<select id="findOneUser" parameterType="dao.User" resultType="dao.User">
        SELECT * FROM user  WHERE 1 = 1
        <if test="name != null">
            AND name = #{name}
        </if>
</select>
```

* `where`标签

```xml

```