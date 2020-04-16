# Spring Security OAuth最详细的解析

## 1. 核心组件

### 1.1 SecurityContextHolder

SecurityContextHolder用于存储安全上下文信息。当前操作的用户是谁，该用户是否已经被认证，用户拥有哪些权限角色等都会被保存在这里。SecurityContextHolder默认使用ThreadLocal存储认证信息，这就表明了SecurityContextHolder与线程是相关的，这是一种和线程绑定的策略。

**利用SecurityContextHolder取得用户的认证信息:**

```java
Object principle = SecurityContextHolder.getContext().getAuthentication().getPrinciple();

if(principle instanceof UserDetails){
    String username = ((UserDetails) principle).getUsername();
}else{
    String username = principle.toString();
}
```

getAuthentication()返回了认证信息Authentication，getPrinciple()返回了身份信息。UserDetails是Spring对身份信息的一个包装的接口。

### 1.2 Authentication

Authentication的源码如下:

```java
public interface Authentication extends Principle, Serializable{
    //<1>
    Collection<? extends GrantedAuthority> getAuthorities();
    //<2>
    Object getCredentials();
    //<3>
    Object getDetails();
    //<4>
    Object getPrinciple();
    //<5>
    boolean isAuthenticated();
    //<6>
    void setAuthenticated(boolean var1) throws ...;
}
```

<1> 权限信息列表<br>
<2> 密码信息，用户输入的密码字符串，在认证过后会被移除，用来减少不必要的安全隐患信息<br>
<3> 细节信息，Web应用中实现的接口通常为WebAuthenticationDetails，记录了用户访问的ip地址和sessionId信息等<br>
<4> 最重要的身份信息，大部分情况返回的是UserDetails接口的实现类<br>

### 1.3 UserDetails

UserDetails代表了最详细的用户的信息，大致源码如下:

```java
public interface UserDetails extends Serializable{
    //<1>
    Collection<? extends GrantedAuthority> getAuthorities();
    //<2>
    String getUsername();
    //<3>
    String getPassword();
    //<4>
    boolean isAccountNonExpired();
    //<5>
    boolean isAccountNonLocked();
    //<6>
    boolean isCredentialsNonExpired();
    //<7>
    boolean isEnabled();
}
```

**注意区分Authentication#getCredentials()与UserDetails#getPassword()，二者虽然都表示密码password，但是前者表示的是用户输入的密码，后者表示的是用户正确的密码**

可以注意到Authentication和UserDetails中都含有getAuthorities()方法，实际上Authentication中的getAuthorities()是通过UserDetails#getAuthorities()获得的，Authentication中有一个方法getUserDetails()，其中的UserDetails是经过AuthenticationProvider之后被填充的。

### 1.4 UserDetailsService

```java
public interface UserDetailsService{
    UserDetails loadUserByUsername(String username) throws ...;
}
```

UserDetailsService只从特定的位置加载用户的信息(通常是数据库)，UserDetailsService常见的实现类有如下两种:

* JdbcDaoImpl: 从数据库加载用户信息
* InMemoryUserDetailsaManager: 从内存中加载用户信息

但是实际生产中最常见的是用户自定义loadUserByUsername()实现用户信息的加载

### 1.5 AuthenticationManager

**AuthenticationManager是用户认证的核心接口！！！！无论何种验证方式，一定会以这个接口为入口点！！！**

实际生产中，我们常常会有多种