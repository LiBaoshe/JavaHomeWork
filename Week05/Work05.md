# 5.（选做）总结一下，单例的各种写法，比较它们的优劣。

- 饿汉式

  饿汉式单例，在类初始化时就自行实例化，这样可以避免线程安全问题，但是浪费内存空间，不推荐。

- 懒汉式

  可以延迟加载，简单写法线程不安全，synchronized 同步方法线程安全，但是影响并发性能，双重检查、静态内部类两种写法线程安全，可以使用。懒汉式中需要使用 volatile 关键字禁止指令重排序。 

- 枚举

  枚举是一种特殊的懒汉式单例，上面的饿汉式和懒汉式都不能防止反射、序列化对单例的破坏，枚举不仅线程安全，还可以防止序列化和反序列化破坏单例，推荐使用。

## 饿汉式单例写法

饿汉式单例模式比较简单，类加载的时候对象就初始化好了，是线程安全的。

如果不是经常使用的单例，用饿汉式会造成内存浪费。

```java
// 饿汉单例模式 1 静态常量
public class Singleton {
    private static final INSTANCE = new Singleton();
    private Singleton(){} // 构造方法私有化
    public static Singleton getInstance(){
        return INSTANCE;
    }
}
```

```java
// 饿汉单例模式 2 静态代码块
public class Singleton {
    private static final INSTANCE;
    private Singleton(){} // 构造方法私有化
    static{
        INSTANCE = new Singleton();
    }
    public static Singleton getInstance(){
        return INSTANCE;
    }
}
```

## 懒汉式单例写法

懒汉式单例模式将对象的初始化延迟的第一次调用的时候，需要解决线程安全问题。

```java
// 1.简单懒汉式 线程不安全
public class Singleton {
    private volatile static Singleton instance;
    private Singleton(){} // 构造方法私有化
    public static Singleton getInstance(){
        if(instance == null){
            instance = new Singleton();
        }
        return instance;
    }
}
```

```java
// 2.同步方法懒汉式，线程安全，但是 同步方法 降低了多线程的并发性能。
public class Singleton {
    private volatile static Singleton instance;
    private Singleton(){} // 构造方法私有化
    public synchronized static Singleton getInstance(){
        if(singleton == null){
            instance = new Singleton();
        }
        return instance;
    }
}
```

```java
// 3.双重检查懒汉式，线程安全，不能防止反射和序列化
public class Singleton {
    private volatile static Singleton instance;
    private Singleton(){} // 构造方法私有化
    public static Singleton getInstance(){
        if(instance == null){
            synchronized(Singleton.class){
                if(instance == null){
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

```java
// 4.静态内部类懒汉式 线程安全，不能防止反射和序列化
public class Singleton {
    private Singleton(){} // 构造方法私有化.
    private static Inner {
        private static final Singleton INSTANCE = new Singleton();
    }
    public static Singleton getInstance(){
        return Inner.INSTANCE;
    }
}
```

```java
// 5.枚举懒汉式，线程安全,可以防止序列化、反射破坏单例。
public enum Singleton {
    INSTANCE;
    public Singleton getInstance(){
        return INSTANCE;
    }
}
```

