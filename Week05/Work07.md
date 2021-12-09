# 7.（选做）总结 Hibernate 与 MyBatis 的各方面异同点。

## Hibernate

W3C school 教程：https://www.w3cschool.cn/hibernate/skzl1idz.html

Hibernate 的概念中Java对象拥有三种状态。瞬时状态，游离状态，持久状态，当这些状态发生改变时，Hibernate 框架会自动的对数据库发送sql语句完成对数据库的操作。

## MyBatis

W3C school 教程：https://www.w3cschool.cn/mybatis/mybatis-dyr53b5w.html

mybatis属于半orm，因为sql语句需要自己写。

与其他比较标准的 ORM 框架（比如 Hibernate ）不同， mybatis 并没有将 java 对象与数据库关联起来，而是将 java 方法与 sql 语句关联起来，mybatis 允许用户充分利用数据库的各种功能，例如存储、视图、各种复杂的查询以及某些数据库的专有特性。

自己写 sql 语句的好处是，可以根据自己的需求，写出最优的 sql 语句。灵活性高。但是，由于是自己写 sql 语句，导致平台可移植性不高。MySQL 语句和 Oracle 语句不同。

## Hibernate & Mybatis

- MyBatis可以进行更为细致的SQL优化，可以减少查询字段。
- MyBatis容易掌握，而Hibernate门槛较高。

- Hibernate的DAO层开发比MyBatis简单，Mybatis需要维护SQL和结果映射。
- Hibernate对对象的维护和缓存要比MyBatis好，对增删改查的对象的维护要方便。
- Hibernate数据库移植性很好，MyBatis的数据库移植性不好，不同的数据库需要写不同SQL。
- Hibernate有更好的二级缓存机制，可以使用第三方缓存。MyBatis本身提供的缓存机制不佳。

- Hibernate功能强大，数据库无关性好，O/R映射能力强，如果你对Hibernate相当精通，而且对Hibernate进行了适当的封装，那么你的项目整个持久层代码会相当简单，需要写的代码很少，开发速度很快，非常爽。
- Hibernate的缺点就是学习门槛不低，要精通门槛更高，而且怎么设计O/R映射，在性能和对象模型之间如何权衡取得平衡，以及怎样用好Hibernate方面需要你的经验和能力都很强才行。
- iBATIS入门简单，即学即用，提供了数据库查询的自动对象绑定功能，而且延续了很好的SQL使用经验，对于没有那么高的对象模型要求的项目来说，相当完美。
- iBATIS的缺点就是框架还是比较简陋，功能尚有缺失，虽然简化了数据绑定代码，但是整个底层数据库查询实际还是要自己写的，工作量也比较大，而且不太容易适应快速数据库修改。

参考地址：[HIBERNATE与 MYBATIS的对比，在这里做一下总结 ](https://www.cnblogs.com/williamjie/p/9342117.html)

