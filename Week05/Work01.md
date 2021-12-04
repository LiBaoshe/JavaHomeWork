# 1.（选做）使 Java 里的动态代理，实现一个简单的 AOP。

Java动态代理类位于java.lang.reflect包下，一般主要涉及到以下两个类：

- Interface InvocationHandler：该接口中仅定义了一个方法

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable;

- Proxy：该类即为动态代理类，实现动态代理主要使用 newProxyInstance 方法：

  static Object newProxyInstance(ClassLoaderloader, Class[] interfaces, InvocationHandler h)：返回代理类的一个实例，返回后的代理类可以当作被代理类使用(可使用被代理类的在Subject接口中声明过的方法)

————————————————
原文链接：https://blog.csdn.net/jiankunking/article/details/52143504

实现步骤：

定义接口 IShool：

```java
public interface ISchool {
    void ding();
}
```

接口实现类 School：

```java
public class School implements ISchool{
    @Override
    public void ding() {
        System.out.println("道家学院 === 老子西出函谷关，紫气东来 ~ ");
    }
}
```

动态代理类 ProxyFactory：

```java
/**
 * JDK 动态代理实现类
 */
public class ProxyFactory {

    // 代理对象
    final private Object target;

    public ProxyFactory(Object target) {
        this.target = target;
    }

    public Object getProxyInstance(){
        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                ((proxy, method, args) -> {
                    before(); // 前置通知
                    Object invoke = method.invoke(target, args);
                    after(); // 后置通知
                    return invoke;
                }));
    }

    public void before(){
        System.out.println("动态代理：前置通知.");
    }

    public void after(){
        System.out.println("动态代理：后置通知.");
    }
}
```

动态代理测试类 ProxyTest：

```java
public class ProxyTest {

    public static void main(String[] args) {
        // 动态代理测试
        ISchool school = new School();
        ProxyFactory factory = new ProxyFactory(school);
        ISchool proxySchool = (ISchool) factory.getProxyInstance();
        proxySchool.ding();
    }
}
```

运行结果：

![image-20211201213102927](https://raw.githubusercontent.com/LiBaoshe/images/master/imgs/image-20211201213102927.png)