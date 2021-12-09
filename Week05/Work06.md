# 6.（选做）maven/spring 的 profile 机制，都有什么用法？

profile 是一种条件化的配置，在运行时，根据哪些 profile 处于激活状态，可以使用或忽略不同的 bean、配置类和配置属性。

## Spring profile 用法

### 一、区分不同环境的配置

项目开发通常有开发环境、测试环境、生产环境等多种环境，可以使用 Spring profile 机制来区分不同的环境，对每种环境创建一个配置文件，文件名要遵守约定：application-{profile 名}.yml 或 application-{profile 名}.properties。

### 二、条件化的创建 bean 对象

通过 @Profile 注解可以条件化地创建 bean 对象：

```java
@Bean
@Profile("dev") // 只在 dev 环境中生效
public TestBean getTestBean(){...}
```

```java
@Bean
@Profile({"dev", "test"}) // 在 dev 或 test 环境中生效
public TestBean getTestBean(){...}
```

```java
@Bean
@Profile("!prod") // prod 环境中不生效
public TestBean getTestBean(){...}
```

### 三、激活 profile

#### 3.1 WebApplicationInitializer 接口

通过注入spring.profiles.active变量可以为Spring上下文指定当前的 profile：

```java
@Configuration
public class MyWebApplicationInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        servletContext.setInitParameter("spring.profiles.active", "dev");
    }
}
```

#### 3.2 通过 web.xml 定义

与上面的方法类似，在web.xml中通过context-param元素也可以设置profile。

```xml
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>/WEB-INF/app-config.xml</param-value>
</context-param>
<context-param>
    <param-name>spring.profiles.active</param-name>
    <param-value>dev</param-value>
</context-param>
```

#### 3.3 JVM 启动参数

```java
java -jar application.jar -Dspring.profiles.active=dev;
java -jar application.jar --spring.profiles.active=dev,test;
```

#### 3.4 环境变量

在Unix/Linux环境中，可以通过环境变量注入profile的值：

```java
export spring_profiles_active=dev
java -jar application.jar 
```

#### 3.5 application 配置文件

```java
spring.profiles.active=dev
```

#### 3.6 Maven Profile

Maven本身也提供了Profile的功能，可以通过Maven的Profile配置来指定Spring的Profile。这种做法稍微有点复杂，需要先在pom.xml中设定不同的 maven profile，如下：

```java
<profiles>
    <profile>
        <id>dev</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <spring.profiles.active>dev</spring.profiles.active>
        </properties>
    </profile>
    <profile>
        <id>prod</id>
        <properties>
            <spring.profiles.active>prod</spring.profiles.active>
        </properties>
    </profile>
</profiles>
```

这里，分别声明了dev和prod两个profile，每个profile都包含了一个spring.profiles.active属性，这个属性用来注入到 Spring中的profile入参。在SpringBoot的配置文件application.properties中，需要替换为这个maven传入的property：

```java
## 使用 Maven 的属性进行替换
spring.profiles.active=@spring.profiles.active@
```

接下来，需要让Maven在打包时能将application.properties进行过滤处理，同时替换掉变量，需编辑pom.xml如下：

```java
<build>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
            <filtering>true</filtering>
        </resource>
    </resources>
</build>
```

这里定义了filtering=true，因此Resource打包插件会对配置文件执行过滤。

最后，在maven打包时指定参数如下：

```java
mvn clean package -Pprod
```

#### 3.7 使用 @ActiveProfiles

@ActiveProfile 是用于单元测试场景的注解，可以为测试代码指定一个隔离的profile，如下：

```java
@ActiveProfiles("test")
public void ApiTest{ ... }
```

#### 3.8 使用 ConfigurableEnvironment

ConfigurableEnvironment 这个Bean封装了当前环境的配置信息，你可以在启动应用前进行设定操作：

```java
SpringApplication application = new SpringApplication(MyApplication.class);

//设置environment中的profiler
ConfigurableEnvironment environment = new StandardEnvironment();
environment.setActiveProfiles("dev","join_dev");

application.setEnvironment(environment);
application.run(args)
```

#### 3.9 SpringApplication.setAdditionalProfiles

SpringApplication这个类还提供了setAdditionalProfiles方法，用来让我们实现"附加"式的profile。这些profile会同时被启用，而不是替换原来的active profile，如下：

```java
SpringApplication application = new SpringApplication(MyApplication.class);
application.setAdditionalProfiles("new_dev");
```

### 四、优先级

至此，我们已经提供了很多种方法来设定 Spring应用的profile，当它们同时存在时则会根据一定优先级来抉择，参考如下：

1. SpringApplication.setAdditionalProfiles
2. ConfigurableEnvironment、@ActiveProfiles
3. Web.xml的 context-param
4. WebApplicationInitializer
5. JVM 启动参数
6. 环境变量
7. Maven profile、application.properties

从上至下，优先级从高到低排列。 
其中，Maven profile与配置文件的方式相同，环境变量以及JVM启动参数会覆盖配置文件的内容。1和2则属于进程内的控制逻辑，优先级更高。 如果在启动SpringBoot应用前对当前ConfigurableEnvironment对象注入了profile，则会优先使用这个参数， ActiveProfiles用于测试环境，其原理与此类似。SpringApplication.setAdditionalProfiles则是无论如何都会附加的profile，优先级最高。

参考连接：https://www.cnblogs.com/huahua-test/p/11576907.html

## Maven Profile

### 一、pom 文件中定义 profile

```java
    <profiles>
        <profile>
            <!--不同环境Profile的唯一id-->
            <id>dev</id>
            <properties>
                <!--profiles.active是自定义的字段（名字随便起），自定义字段可以有多个-->
                <profiles.active>dev</profiles.active>
            </properties>
        </profile>
        <profile>
            <id>prod</id>
            <properties>
                <profiles.active>prod</profiles.active>
            </properties>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
        </profile>
        <profile>
            <id>test</id>
            <properties>
                <profiles.active>test</profiles.active>
            </properties>
        </profile>
    </profiles>
```

定义了多个profile，每个profile都有唯一的id，也包含properties属性。这里为每个profile都定义一个名为profiles.active的properties，每个环境的值不同。当我们打包项目时，激活不同的环境，profiles.active字段就会被赋予不同的值。

参考链接：https://blog.csdn.net/java_collect/article/details/83870215

### 二、激活 profile 配置

#### 2.1 命令激活

用户可以在 mvn 命令行中添加参数“-P”，指定要激活的 profile 的 id。如果一次要激活多个 profile，可以用逗号分开一起激活。例如：

```java
mvn clean install -Pdev,test
```

#### 2.2 settings 文件显示激活

如果希望某个 profile 默认一直处于激活状态，可以在 settings.xml 中配置 activeProfiles 元素，指定某个 profile 为默认激活状态，样例配置代码如下：

```xml
<settings>
    ...
    <activeProfiles>
        <activeProfile>dev_evn</activeProfile>
    </activeProfiles>
    ...
</settings>
```

#### 2.3 系统属性激活

可以配置当某个系统属性存在时激活 profile，代码如下：

```xml
<profiles>
    <profile>
        ...
        <activation>
            <property>
                <name>profileProperty</name>
            </property>
        </activation>
    </profile>
</profiles>
```

甚至还可以进一步配置某个属性的值是什么时候激活，例如：

```xml
<profiles>
    <profile>
        ...
        <activation>
            <property>
                <name>profileProperty</name>
                <value>dev</value>
            </property>
        </activation>
    </profile>
</profiles>
```

这样就可以在 mvn 中用“-D”参数来指定激活，例如：

```java
mvn clean install -DprofileProperty=dev
```

表示激活属性名称为 profileProperty，值为 dev 的 profile。

实际上这也是一种命令激活 profile 的方法，只是用的是“-D”参数指定激活的属性和值，而前面的是用的“-P”参数指定激活的 profile 的 id 而已。

#### 2.4 操作系统环境激活

用户可以通过配置指定不同操作系统的信息，实现不同操作系统做不同的构建。例如：

```xml
<profiles>
    <profile>
        <activation>
            <os>
                <name>Window XP</name>
                <family>Windows</family>
                <arch>x86</arch>
                <version>5.1.2600</version>
            </os>
        </activation>
    </profile>
</profiles>
```

family 的值是 Windows、UNIX 或 Mac。name 为操作系统名称。arch为操作系统的架构。version为操作系统的版本。具体的值可以通过查看环境中的系统属性“os.name”“os.arch”和“os.version”获取。

#### 2.5 文件存在与否激活

也可以通过配置判断某个文件存在与否来决定是否激活 profile，样例配置代码如下：

```xml
<profiles>
    <profile>
        <activation>
            <file>
                <missing>t1.properties</missing>
                <exists>t2.properties</exists>
            </file>
        </activation>
    </profile>
</profiles>
```

#### 2.6 默认激活

还可以配置一个默认的激活 profile，例如：

```xml
<profiles>
    <profile>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
    </profile>
</profiles>
```

需要注意的是，如果 pom 中有任何一个 profile 通过其他方式被激活的话，所有配置成默认激活的 profile 都会自动失效。 

#### 2.7 查看当前的 profile

```java
mvn help:active-profiles // 查看当前激活的 profile
mvn help:all-profiles // 查看所有的 profile
```

### 三、profile 的种类

根据 profile 配置的位置不同，可以将 profile 分成如下几种。

#### 1）pom.xml

pom.xml 中声明的 profile 只对当前项目有效。

#### 2）用户 settings.xml

在用户目录下的“.m2/settings.xml”中的 profile，对本机上的该用户的所有 Maven 项目有效。

#### 3）全局 settings.xml

在 Maven 安装目录下 conf/settings.xml 中配置的 profile，对本机上所有项目都有效。

为了不影响其他用户且方便升级 Maven，一般配置自己的 settings.xml，不要轻易修改全局的 settings.xml。同样的道理，一般不需要修改全局 settings.xml 中的 profile。

参考连接：http://c.biancheng.net/view/5286.html

