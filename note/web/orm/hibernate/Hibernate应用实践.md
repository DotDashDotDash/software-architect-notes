# Hibernate应用实践

## 一个Hibernate的基本配置

* jar包的导入(maven管理)

```xml
<dependencies>
        <!-- 添加Hibernate依赖 -->
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>3.6.10.Final</version>
        </dependency>

        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-nop</artifactId>
            <version>1.6.4</version>
        </dependency>
        <!-- 其他配置 -->
    </dependencies>
```

* 一个实体类User，设置好getter和setter，实现Serializable接口

```java
public class User implements Serializable {
    private Long id;
    private String name;
    private String birthday;

    public User(){}

    public User(Long id, String name){
        this.id = id;
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public String getBirthday() {
        return birthday;
    }
}
```

* hibernate.cfg.xml全局配置文件，存放在classpath路径下

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-configuration PUBLIC
        "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
        "http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd">
<hibernate-configuration>
    <session-factory>
        <!-- 连接数据库配置信息 -->
        <property name="hibernate.connection.driver_class">com.mysql.cj.jdbc.Driver</property>
        <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/hibernate?serverTimezone=UTC</property>
        <property name="hibernate.connection.username">root</property>
        <property name="hibernate.connection.password">123456</property>
        <property name="hibernate.dialect">org.hibernate.dialect.MySQLDialect</property>
        <!-- 配置数据库的可选信息 -->
        <property name="show_sql">true</property> <!-- 是否显示hibernate生成的sql语句 -->
        <property name="format_sql">true</property> <!-- 是否规格化输出 -->
        <property name="hbm2ddl.auto">update</property> <!-- hibernate以何种方式生成DDL -->
        <property name="current_session_context_class">thread</property>
        <!-- 配置映射文件的位置 -->
        <mapping resource="User.hbm.xml"/>
        <!-- 其他的class -->
        <!-- <mapping ...> -->
    </session-factory>
</hibernate-configuration>
```

* 映射的对象数据库配置文件，将数据库对象与实体对象的属性进行匹配

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-mapping PUBLIC
        "-//Hibernate/Hibernate Mapping DTD 3.0//EN"
        "http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd">

<hibernate-mapping package="com.ryan">
    <class name="User" table="user">
        <id name="id" column="id">
            <!-- 使用本地数据库的自动增长能力 -->
            <generator class="native"/>
        </id>
        <property name="name" column="name"></property>
        <property name="birthday" column="birthday"></property>
    </class>
</hibernate-mapping>
```

* 将对数据库的操作转化为对实体类的操作

```java
public class HibernateDemo1 {

    @Test
    public void test1(){
        Configuration cfg = new Configuration();
        cfg.configure();
        SessionFactory factory = cfg.buildSessionFactory();

        //瞬时态
        User user = new User();
        user.setId(2L);
        user.setName( "wwh");
        user.setBirthday("19980111");

        Session session = factory.openSession();
        Transaction transaction = session.beginTransaction();
        session.save(user);
        transaction.commit();
        session.close();
        factory.close();
    }
}
```

## 一对多的注解配置

* 商品(一的一方)

```java
@Entity
@Table(name="t_goods")
public class GoodsBean {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="g_id")
    private int id;
    @Column(name="g_name")
    private String name;
    @Column(name="g_price")
    private double price;
    @Column(name="g_type")
    private String type;

    /*
     * mappedBy：在一对多关系中，一的一方写，表示有对方维护关联关系，值应该为对方的对象变量
     * cascade：开启级联
     */
    @OneToMany(mappedBy="goods", cascade=CascadeType.ALL)
    private Set<CommentBean> comSet;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<CommentBean> getComSet() {
        return comSet;
    }

    public void setComSet(Set<CommentBean> comSet) {
        this.comSet = comSet;
    }

    @Override
    public String toString() {
        return "GoodsBean [id=" + id + ", name=" + name + ", price=" + price + ", type=" + type + ", comSet=" + comSet
                + "]";
    }

}

```

* 评论(多的一方)

```java
@Entity
@Table(name="t_stu")
public class StudentBean {

    private int id;
    private String name;
    private ClassBean cla = new ClassBean();;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="s_id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name="s_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch=FetchType.LAZY,cascade=CascadeType.ALL)
    //指定外键的名称
    @JoinColumn(name="o_u_id")
    public ClassBean getCla() {
        return cla;
    }

    public void setCla(ClassBean cla) {
        this.cla = cla;
    }

    @Override
    public String toString() {
        return "StudentBean [id=" + id + ", name=" + name + "]";
    }
}
```

## 一对多的XML配置

* 一的一方xml配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-mapping PUBLIC "-//Hibernate/Hibernate Mapping DTD 3.0//EN"
"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd">
<hibernate-mapping>
    <class name="com.mengma.onetomany.Grade" table="grade">
        <id name="id" column="id">
            <generator class="native" />
        </id>
        <property name="name" column="name" length="40" />
        <!-- 一对多的关系使用set集合映射 -->
        <set name="students">
            <!-- 确定关联的外键列 -->
            <key column="gid" />
            <!-- 映射到关联类属性 -->
            <one-to-many class="com.mengma.onetomany.Student" />
        </set>
    </class>
</hibernate-mapping>
```

* 多的一方xml配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-mapping PUBLIC "-//Hibernate/Hibernate Mapping DTD 3.0//EN"
"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd">
<hibernate-mapping>
    <class name="com.mengma.onetomany.Student" table="student">
        <id name="id" column="id">
            <generator class="native" />
        </id>
        <property name="name" column="name" length="40" />
        <!-- 多对一关系映射 -->
        <many-to-one name="grade" class="com.mengma.onetomany.Grade"></many-to-one>
    </class>
</hibernate-mapping>
```

## 双向一对多

* Product类

```java
@Entity
@Table(name = "t_product")
public class Product {
  @Id
  @GeneratedValue
  private Long id;
  private String name;
  // 多对一
  // optional=false表示外键type_id不能为空
  @ManyToOne(optional = true)
  @JoinColumn(name = "type_id")
  private ProductType type;

  public Product() {

  }

  public Product(String name, ProductType type) {
    this.name = name;
    this.type = type;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ProductType getType() {
    return type;
  }

  public void setType(ProductType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "Product [id=" + id + ", name=" + name + "]";
  }

}
```

* ProductType

```java
@Entity
@Table(name = "t_product_type")
public class ProductType {
  @Id
  @GeneratedValue
  private Long id;
  private String name;
  // 一对多:集合Set
  @OneToMany(mappedBy = "type", orphanRemoval = true)
  private Set<Product> products = new HashSet<Product>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<Product> getProducts() {
    return products;
  }

  public void setProducts(Set<Product> products) {
    this.products = products;
  }

  @Override
  public String toString() {
    return "ProductType [id=" + id + ", name=" + name + "]";
  }

}
```

## 单向多对多

* Student

```java
@Entity
@Table(name = "t_student")
public class Student {
  @Id
  @GeneratedValue
  private Long id;
  private String sname;

  public Student() {

  }

  public Student(String sname) {
    this.sname = sname;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getSname() {
    return sname;
  }

  public void setSname(String sname) {
    this.sname = sname;
  }

  @Override
  public String toString() {
    return "Student [id=" + id + ", sname=" + sname + "]";
  }
}
```

* Teacher

```java
@Entity
@Table(name = "t_teacher")
public class Teacher {
  @Id
  @GeneratedValue
  private Long id;
  private String tname;
  // @ManyToMany注释表示Teacher是多对多关系的一端。
  // @JoinTable描述了多对多关系的数据表关系。name属性指定中间表名称，joinColumns定义中间表与Teacher表的外键关系。
  // 中间表Teacher_Student的Teacher_ID列是Teacher表的主键列对应的外键列，inverseJoinColumns属性定义了中间表与另外一端(Student)的外键关系。
  @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  @JoinTable(name = "t_teacher_student", joinColumns = { @JoinColumn(name = "teacher_id") }, inverseJoinColumns = {
        @JoinColumn(name = "student_id") })
  private Set<Student> students = new HashSet<Student>();

  public Teacher() {

  }

  public Teacher(String tname) {
    this.tname = tname;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTname() {
    return tname;
  }

  public void setTname(String tname) {
    this.tname = tname;
  }

  public Set<Student> getStudents() {
    return students;
  }

  public void setStudents(Set<Student> students) {
    this.students = students;
  }

  @Override
  public String toString() {
    return "Teacher [id=" + id + ", tname=" + tname + "]";
  }
}
```

## 双向多对多

* Student

```java
@Entity
@Table(name = "t_student")
public class Student {
  @Id
  @GeneratedValue
  private Long id;
  private String sname;
  @ManyToMany(mappedBy = "students")
  private Set<Teacher> teachers = new HashSet<Teacher>();

  public Student() {

  }

  public Student(String sname) {
    this.sname = sname;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getSname() {
    return sname;
  }

  public void setSname(String sname) {
    this.sname = sname;
  }

  public Set<Teacher> getTeachers() {
    return teachers;
  }

  public void setTeachers(Set<Teacher> teachers) {
    this.teachers = teachers;
  }

  @Override
  public String toString() {
    return "Student [id=" + id + ", sname=" + sname + "]";
  }

}
```

* Teacher

```java
@Entity
@Table(name = "t_teacher")
public class Teacher {
  @Id
  @GeneratedValue
  private Long id;
  private String tname;
  // @ManyToMany注释表示Teacher是多对多关系的一端。
  // @JoinTable描述了多对多关系的数据表关系。name属性指定中间表名称，joinColumns定义中间表与Teacher表的外键关系。
  // 中间表Teacher_Student的Teacher_ID列是Teacher表的主键列对应的外键列，inverseJoinColumns属性定义了中间表与另外一端(Student)的外键关系。
  @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  @JoinTable(name = "t_teacher_student", joinColumns = { @JoinColumn(name = "teacher_id") }, inverseJoinColumns = {
      @JoinColumn(name = "student_id") })
  private Set<Student> students = new HashSet<Student>();

  public Teacher() {

  }

  public Teacher(String tname) {
    this.tname = tname;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTname() {
    return tname;
  }

  public void setTname(String tname) {
    this.tname = tname;
  }

  public Set<Student> getStudents() {
    return students;
  }

  public void setStudents(Set<Student> students) {
    this.students = students;
  }

  @Override
  public String toString() {
    return "Teacher [id=" + id + ", tname=" + tname + "]";
  }
}
```

## 双向一对一共享主键

* Person

```java
@Entity
@Table(name = "t_person")
public class Person {
  @Id
  @GeneratedValue
  private Long id;
  private String name;
  // mappedBy配置映射关系:当前对象IdCard属于哪个person对象
  @OneToOne(optional = false, mappedBy = "person", fetch = FetchType.LAZY)
  private IdCard idCard;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public IdCard getIdCard() {
    return idCard;
  }

  public void setIdCard(IdCard idCard) {
    this.idCard = idCard;
  }

}
```

* IdCard

```java
@Entity
@Table(name = "t_idcard")
public class IdCard {
  @Id
  @GeneratedValue(generator = "pkGenerator")
  @GenericGenerator(name = "pkGenerator", strategy = "foreign", parameters = @Parameter(name = "property", value = "person"))
  private Long id;
  @Column(length = 18)
  private String cardNo;
  @OneToOne(optional = false, fetch = FetchType.LAZY)
  // 如果不加这个注解，添加t_idcard信息时，就会自动在t_idcard表中增加了一个外键person_id
  @PrimaryKeyJoinColumn
  private Person person;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCardNo() {
    return cardNo;
  }

  public void setCardNo(String cardNo) {
    this.cardNo = cardNo;
  }

  public Person getPerson() {
    return person;
  }

  public void setPerson(Person person) {
    this.person = person;
  }

}
```

## 双向一对一唯一外键

* Person

```java
@Entity
@Table(name = "t_person")
public class Person {
  @Id
  @GeneratedValue
  private Long id;
  private String name;
  // mappedBy配置映射关系:当前对象IdCard属于哪个person对象
  @OneToOne(optional = false, mappedBy = "person", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private IdCard idCard;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public IdCard getIdCard() {
    return idCard;
  }

  public void setIdCard(IdCard idCard) {
    this.idCard = idCard;
  }

}
```

* IdCard

```java
@Entity
@Table(name = "t_idcard")
public class IdCard {
  @Id
  @GeneratedValue
  private Long id;
  @Column(length = 18)
  private String cardNo;
  // 默认值optional = true表示idcard_id可以为空;反之。。。
  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "person_id", unique = true)
  // unique=true确保了一对一关系
  private Person person;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCardNo() {
    return cardNo;
  }

  public void setCardNo(String cardNo) {
    this.cardNo = cardNo;
  }

  public Person getPerson() {
    return person;
  }

  public void setPerson(Person person) {
    this.person = person;
  }
}
```

## 参考链接

* [JeGe博客](https://www.jianshu.com/u/d54cf1707fd8)
