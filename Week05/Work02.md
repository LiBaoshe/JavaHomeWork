# 2.（必做）写代码实现 Spring Bean 的装配，方式越多越好（XML、Annotation 都可以）, 提交到 GitHub。



## Spring中装配bean的三种主要方式：

- ### 通过XML装配bean

  基于 XML 配置的方式很好的完成了对象生命周期的描述和管理，但是随着项目规模不断扩大，XML 的配置也逐渐增多，使配置文件难以管理。另一方面，项目中依赖关系越来越复杂，配置文件变得难以理解。这个时候迫切需要一种方式来解决这类问题。

- ### 自动化装配bean

  自动化装配需要开启包扫描和指定要装配的类。

  开启包扫描的两种方式：@ComponentScan 或 XML 形式的 <context:component-scan base-backage="" /> ，它会扫描指定包路径下带有@Service、@Repository、@Controller、@Component 等注解的类。

  @Import 对应 XML 形式的 <import resource="" />，可以导入其它的配置文件。

- ### 通过Java代码装配bean

  基于 JavaConfig 的配置形式，使用 @Configuration 注解代替 XML 的配置形式，可以通过 @Bean 注解来将一个对象注入 IoC 容器中，默认情况下采用方法名称作为该 Bean 的 id。

  

## 装配实现代码

### XML装配bean

定义要装配的类：

```java
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String name;
    private Integer age;

}
```

xml配置：（applicationContext.xml 文件中）

```java
    <bean id="user01" class="com.ibaoge.spring.demo.work02.User">
        <property name="name" value="张三" />
        <property name="age" value="18" />
    </bean>
```

main 方法中通过 ApplicationContext 获取bean：

```java
    public static void main(String[] args) {
        ApplicationContext context = 
            new ClassPathXmlApplicationContext("applicationContext.xml");
        User user = (User) context.getBean("user01");
        System.out.println(user);
    }
```

### 自动化装配

定义要装配的类：（添加注解@Component）

```java
@Component
public class TestService {

    public void doService(){
        System.out.println("测试服务.");
    }
}
```

使用 @ComponentScan 开启包扫描（springboot默认会扫描启动类所在的包及其子包，不在自动扫描路径下，需要修改自定义扫描包路径。），以 SpringBoot 方式启动，通过实现 ApplicationContextAware 接口获取 bean （参考连接：https://www.jianshu.com/p/4c0723615a52）

```java
@ComponentScan(basePackages = {"com.ibaoge.spring.demo.work02"})
@SpringBootApplication
public class TestAutoApplication implements ApplicationContextAware {

    private final TestService testService;

    // 注入 TestService
    @Autowired
    public TestAutoApplication(TestService testService) {
        this.testService = testService;
    }

    public static void main(String[] args) {
        SpringApplication.run(TestAutoApplication.class, args);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 测试注入的 TestService
        testService.doService();
    }
}
```

### Java代码装配

定义 @Configuration 配置类：（需要在 springboot 的包扫描路径下）

```java
@Configuration
public class TestConfig {

    @Bean
    public User getUser(){
        return new User("李四", 20);
    }
}
```

以 SpringBoot 方式启动，通过实现 ApplicationContextAware 接口获取 bean:

```java
@SpringBootApplication
public class TestConfigApplication implements ApplicationContextAware {
    
    private final User user;

    // 注入 user
    @Autowired
    public TestConfigApplication(User user) {
        this.user = user;
    }

    public static void main(String[] args) {
        SpringApplication.run(TestConfigApplication.class, args);
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 输出注入的 user
        System.out.println(user);
    }
}
```
