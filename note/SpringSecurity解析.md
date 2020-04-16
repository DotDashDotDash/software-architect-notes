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

实际生产中，我们常常会有多种认证方式，例如：

1. 用户名+密码
2. 用户邮箱+密码
3. 用户手机号+密码
4. 指纹认证

AuthenticationManager不直接参与认证，而是实现类内部维护了一个`List<AuthenticaitonProvider>`列表，验证的时候会依次按照列表顺序进行认证，该列表中存放着多种认证方式，这是一种**委托者**的设计模式。**默认情况下，只要一个AuthenticationProvider通过了验证，那么整个验证就通过了，当然，实际业务环境中也可能存在着多重认证的需要**

### 1.6 DaoAuthenticationProvider

DaoAuthenticationProvider是AuthenticationProvider最常见的一个实现类，其工作的流程大概如下:

**假定采用用户名+密码的验证模式，DaoAuthenticationProvider将其封装为UsernamePasswordAuthenticationToken，同时加载出正确的UserDetails，进行比对，完成密码的验证**

## 2. Spring Security如何完成依次身份验证

1. 程序捕捉用户输入的username和password，封装成Authentication的实现类UsernamePasswordAuthenticationToken
2. AuthenticationManager依次利用AuthenticaitonProvider实现验证
3. 验证成功，会去除掉用户的密码，返回一个包含了各种用户信息的Authentication
4. SecurityContextHolder.getContext().setAuthentication(...)

## 3. 核心配置

### 3.1 Spring Security入门指南中的配置项:

```java
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

    @Override 
    protected void configure(HttpSecurity http) throws Exception{
        http
            .authorizeRequests()
                .antMatchers("/", "/home").permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception{
        auth
            .inMemoryAuthentication()
                .withUser("admin").password("admin").roles("USER");
    }
}
```

### 3.2 @EnableWebSecurity

@EnbaleWebSecurity是一个组合注解:

```java
@Import({
    SpringMvcImportSelector.class, //(1)
    WebSecurityConfiguration.class //(2)
})
@EnableGlobalAuthentication  //(3)
@Configuration
public @interface EnableWebSecurity{
    boolean debug() default false;
}
```

(1) SpringMvcImportSelector判断当前的环境是否含有SpringMvc，因为Spring Security可以在非Spring环境中使用，此举是为了避免DispatcherServlet的重复配置<br>
(2) WebSecurityConfiguration用于配置Web安全，下面会讲<br>
(3) @EnableGlobalAuthentication注解代码如下:

```java
@Import(AuthenticationConfiguration.class) //用来配置认证的相关核心类
@Configuration
public @interface EnableGlobalAuthentication{}
```

@EnableWebSecurity把配置安全信息，配置认证组合在了一起

### 3.3 WebSecurityConfiguration

在这个配置类中，有一个十分重要的bean被注册了

```java
@Configuration
public class WebSecurityConfiguration {

	  //DEFAULT_FILTER_NAME = "springSecurityFilterChain"
	  @Bean(name = AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME)
    public Filter springSecurityFilterChain() throws Exception {
    	...
    }
}
```

SpringSecurityFilterChain是非常重要的一个类，是SpringSecurity的核心过滤器，是一切认证的入口，SpringSecurityFilterChain最后交给DelegatingFilterProxy代理拦截处理(这个类存在与web包而不是spring security包，**利用代理模式实现了安全过滤的解耦**)

关于SpringSecurityFilterChain的内容太多，这里单独放在一篇文章[《Spring Security FilterChain深度解析》]()

### 3.4 AuthenticationConfiguration

```java
@Configuration
@Import(ObjectPostProcessorConfiguration.class)
public class AuthenticationConfiguration {

  	@Bean
	public AuthenticationManagerBuilder authenticationManagerBuilder(
			ObjectPostProcessor<Object> objectPostProcessor) {
		return new AuthenticationManagerBuilder(objectPostProcessor);
	}

  	public AuthenticationManager getAuthenticationManager() throws Exception {
    	...
    }
}
```

AuthenticationConfiguration的主要任务，便是负责生成全局的身份认证管理者AuthenticationManager。AuthenticationManager十分重要的原因就是它是一切认证的入口

