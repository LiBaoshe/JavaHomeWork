# 3.（选做）实现一个 Spring XML 自定义配置，配置一组 Bean，例如：Student/Klass/School。

## Spring XML 配置原理

XML：Extensiable Markup Language，可扩展标记语言。

Json 缺乏 schema 本身的定义/复制的数据类型，一般来说 XML 描述能力比 Json 强。

XML 文件格式的定义有两种常见的不同的格式：XSD（XML 的 Schema 定义），Spring 里默认的是 XSD，还有一种叫 DTD （文档的各种类型的定义）。

Spring xml 配置中，XSD 统一汇总到 spring.schemas 文本文件里面，检查 XML 配置是否正确。spring.handlers 把从 DOM 节点 parse 的对象转换成 Bean 的类。

###  xml 文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context-3.2.xsd http://www.springframework.org/schema/aop https://www.springframework.org/schema/aop/spring-aop.xsd">
    
    <bean id="student123"
          class="io.kimmking.spring01.Student">
        <property name="id" value="123" />
        <property name="name" value="KK123" />
    </bean>   
    <context:component-scan base-package="io.kimmking.spring02" />
</beans>
```

第一行是文档声明，文档声明必须出现在xml文档的0行0列。

beans——xml 文件的根节点。

xmlns——是XML NameSpace 的缩写，因为XML文件的标签名称都是自定义的，自己写的和其他人定义的标签有可能会命名重复，所以需要加上一个 namespace 来区分 xml 文件，类似于 java 中的 package。

xmlns  不加冒号后缀表示是当前的 beans 定义的范围默认 namespace。

xmlns:xsi——是指 xml 文件遵守 xml 规范，xsi全名：xml schema instance

xmlns:schemaLocation——是指具体用到的 schema 资源（xsd、dtd）。

META-INF/spring.schemas 文件中，key = value 即  网络路径=本地 jar 包文件路径，这样就不用远程找了。

META-INF/spring.handlers 文件中定义了 schema 对应的处理 Handler，把 XML 里配置好的各种属性用 DOM 的方式读取过来，变成一个 XML 的 DOM 的对象结构，再把 DOM 结构转换成 Bean 的类。

### 总结 Spring XML 配置原理

通过 Spring 自带的 namespace 各种标签或者自定义的各种标签，定义一个 Spring 使用的 applicationContext.xml 文件，beans 根节点，里边包含了 bean 节点，namespace 前缀节点，都会被加载成 bean。

加载过程：首先 Spring 需要通过 schemaLocation 找到定义的 spring.schemas 定义的各种 Java 包里的 xsd 文件对象属性的定义，检查 xml 文件配置是否正确，接下来 Spring 程序被加载，容器初始化过程中，同样的会去找各种 namespace 对应的一些 NamespaceHandler，把 Spring 加载的 XML 配置文件 DOM 解析成 DOM 树对象，把对象树的内容交给 NamespaceHandler，由 NamespaceHandler 把它们最终变成我们的 Spring 的 Bean。

### 自动化 XML 配置工具

XmlBeans -> Spring-xbean 

项目中有自定义 namespace 的需求，推荐使用 xbean 插件。

原理：

1. 根据 Bean 的字段结构，自动生成 XSD。
2. 根据 Bean 的字段结构，配置 XML 文件。

思考：

1. 解析 XML 的工具有哪些，都有什么特点？

   DOM4J，STAX 或 SAX

   |          | DOM解析                                      | SAX解析                  |
   | -------- | -------------------------------------------- | ------------------------ |
   | 解析方式 | 把整个XML文档加载到内存中，封装形成一颗DOM树 | 逐行读取，事件驱动型解析 |
   | 优点     | 可以增、删、改                               | 速度快，可以读大XML文件  |
   | 缺点     | 可能内存溢出                                 | 复杂，不能增、删、改     |

   参考连接：https://blog.csdn.net/weixin_30525825/article/details/98105168

2. XML <-> Bean 的相互转换的工具，除了 xbean，还有什么？

   XStream

## Spring XML 自定义配置

参考连接：https://www.cnblogs.com/myitnews/p/14018916.html

在Spring中，我们定义一个自己的标签有如下步骤：

- 自己定义一个XSD文件。
- 定义一个和XSD文件所对应的实体类。
- 创建实现了BeanDefinitionParser的类(其实更好的做法是继承抽象类AbstractBeanDefinitionParser)，去解析我们的自定义标签。
- 创建一个继承了NamespaceHandlerSupport的类，去将我们创建的类注册到spring容器。
- 编写自己的Spring.handlers和Spring.schemas

### 一、定义一个XSD文件

创建resources/META-INF/model.xsd

```xml
<?xml version="1.0"?>
<xsd:schema
        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
        xmlns="http://demo.ibaoge.com/schema"
        targetNamespace="http://demo.ibaoge.com/schema">
    <!-- 学生类型 -->
    <xsd:complexType name="studentType">
        <xsd:attribute name="name" type="xsd:string" />
        <xsd:attribute name="age" type="xsd:int" />
    </xsd:complexType>
    <!-- 学生标签 -->
    <xsd:element name="student" type="studentType" />
</xsd:schema>
```

### 二、定义一个和XSD文件所对应的实体类

```java
@Data
@ToString
public class Student {
    private String name;
    private Integer age;
}
```

### 三、实现BeanDefinitionParser，解析标签

```java
public class BillBeanDefinitionParser implements BeanDefinitionParser {

    private final Class<?> beanClass;

    public BillBeanDefinitionParser(Class<?> beanClass){
        this.beanClass = beanClass;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
        genericBeanDefinition.setBeanClass(beanClass);
        genericBeanDefinition.setLazyInit(false);
        genericBeanDefinition.getPropertyValues().add("name", element.getAttribute("name"));
        genericBeanDefinition.getPropertyValues().add("age", element.getAttribute("age"));
        parserContext.getRegistry().registerBeanDefinition(beanClass.getName(), genericBeanDefinition);
        return null;
    }
}
```

### 四、继承NamespaceHandlerSupport，注册类

```java
public class BillNameSpaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("student",
                new BillBeanDefinitionParser(Student.class));
    }
}
```

### 五、编写自己的Spring.handlers和Spring.schemas

这两个文件都是 properties 格式的文件。

META-INF/spring.handlers

```java
http\://demo.ibaoge.com/schema=com.ibaoge.spring.demo.xml.BillNameSpaceHandler
```

META-INF/spring.schemas

```java
http\://demo.ibaoge.com/schema/model.xsd=META-INF/model.xsd
```

### 六、单元测试

```java
// 指定在单元测试启动的时候创建 spring 的工厂类对象
@ContextConfiguration(locations = {"classpath:myApplicationContext.xml"})
// RunWith 的 value 属性指定以 spring test 的 SpringJUnit4ClassRunner 作为启动类
// 如果不指定启动类，默认启用的 junit 中的默认启动类
@RunWith(SpringJUnit4ClassRunner.class)
public class SpringXMLTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testSpringXML(){
        Student student = applicationContext.getBean(Student.class.getName(), Student.class);
        System.err.println(student);
    }
}
```

