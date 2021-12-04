# 8.（必做）给前面课程提供的 Student/Klass/School 实现自动配置和 Starter。

实现过程中遇到的一些问题：

- 使用 @Bean 装配时，如果给对象属性添加了赋值注解（@Autowired、@Resource、@Value 等），则对象配置的属性优先，@Bean 方法中的值会被覆盖掉。
- 如果 bean 的实例名字相同，则会注入 @ImportResource 导入的 xml 配置文件中的实例。
- @Resource(name = "student100") 是JDK的注解，@Autowired 与 @Qualifier("student100") 配合使用，是 Spring 的注解。
- @Autowired / @Resource 注解在 List<Bean> 属性时，会将 spring 容器中的所有 bean 添加到这个集合中。

## 实现自动配置：

- 定义配置类，添加注解 @Configuration。

- 导入配置文件，通过注解 @ImportResource 导入。
- 定义需要的 Bean，通过 @Bean 注解实现。

关键代码如下：

```java
@Configuration
@ImportResource(locations = {"classpath:applicationContext.xml"})
public class MySchoolAutoConfig {

    @Bean
    public Klass class2(){
        return new Klass();
    }

    @Bean("schoolGeek")
    public ISchool getSchool(){
        return new School();
    }

    @Bean
    public Student student456(){
        return new Student(456, "ibaoge", null, null);
    }

    @Bean("ZhangSan")
    public Student getStudent(){
        return new Student(789, "张三", null, null);
    }
}
```

测试配置：

自动装配在 Spring Boot 中是通过 @EnableAutoConfiguration 注解来开启的，这个注解的声明在启动类注解 @SpringBootApplication 内。

定义启动类，使用 @ComponentScan 注解扫描配置包：

```java
/**
 * 8.（必做）给前面课程提供的 Student/Klass/School 实现自动配置和 Starter。
 */
@ComponentScan(basePackages = {
        "io.kimmking.config",
        "io.kimmking.runner"
})
@SpringBootApplication
public class HomeWork08 {

    public static void main(String[] args) {
        SpringApplication.run(HomeWork08.class, args);
    }

}
```

通过 ApplicationRunner 输出自动装配的结果：

```java
@Component
public class TestRunner implements ApplicationRunner {

    @Autowired
    private Student student123;
    @Autowired
    private Student student100;
    @Autowired
    private Student student456;
    @Resource
    private Klass class1;
    @Autowired
    @Qualifier("class2")
    private Klass class2;
    @Autowired
    private ISchool school;
    @Autowired
    @Qualifier("schoolGeek")
    private ISchool schoolGeek;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        System.out.println("MyRunner run ...");
        System.out.println("-----student100----------------------------------");
        student100.print();
        System.out.println("-----student123----------------------------------");
        student123.print();
        System.out.println("-----student456----------------------------------");
        student456.print();
        System.out.println("-----class1.dong()----------------------------------");
        class1.dong();
        System.out.println("-----class2.dong()----------------------------------");
        class2.dong();
        System.out.println("-----school.ding()----------------------------------");
        school.ding();
        System.out.println("------schoolGeek.ding()---------------------------------");
        schoolGeek.ding();
    }
}
```

## 实现 Starter

Spring的SpringFactoriesLoader工厂的加载机制类似 Java  提供的 SPI 机制一样，是Spring提供的一种加载方式。只需要在classpath路径下新建一个文件 META-INF/spring.factories，并在里面按照 Properties 格式填写好jj接口和实现类即可通过 SpringFactoriesLoader 来实例化相应的 Bean。其中 key 可以是接口、注解、或者抽象类的全名。value为相应的实现类，当存在多个实现类时，用“,”进行分割。

META-INF/spring.factories 文件中添加配置：

```java
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  io.kimmking.config.MySchoolAutoConfig
```

（可选）META-INF/spring.provides 文件中设置名称：

```java
provides: my-school-spring-boot-starter
```

其它项目中添加 maven 依赖即可使用 starter：

```java
<dependency>
    <groupId>io.kimmking</groupId>
    <artifactId>my-school-spring-boot-starter</artifactId>
    <version>1.0</version>
</dependency>
```

