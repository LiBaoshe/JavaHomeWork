# 10.（必做）研究一下 JDBC 接口和数据库连接池，掌握它们的设计和用法：

Java数据库连接，（Java Database Connectivity，简称JDBC）是Java语言中用来规范客户端程序如何来访问数据库的应用程序接口，提供了诸如查询和更新数据库中数据的方法。

JDBC 架构分为双层架构（C/S）和三层架构（B/S）。

JDBC提供了数据库batch处理的能力，在数据大批量操作(新增、删除等)的情况下可以大幅度提升系统的性能。

批处理中，禁用自动执行模式，从而在调用 Statement.executeBatch() 时可以防止 JDBC 执行事务处理。禁用自动执行使得应用程序能够在发生错误及批处理中的某些命令不能执行时决定是否执行事务处理。因此，当进行批处理更新时，通常应该关闭自动执行。

如果数据库访问异常或驱动不支持批处理命令，或者如果一个命令发送到数据库时失败或尝试取得结果，即使失败，都会抛一个异常BatchUpdateException 它是SQLException的子类。

JDBC 编程步骤：

- 引入驱动包（例如 mysql-connector-java）

  ```java
  <!-- Spring Boot 中通过 maven 方式引入 JDBC 驱动包 -->
  <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
  </dependency>
  ```

- 加载驱动程序

  ```java
  Class.forName(driverClass)
  ```

- 获得数据库连接

  ```java
  DriverManager.getConnection(url, user, password)
  ```

- 创建 Statement/PreparedStatement 对象执行 sql 语句

  ```java
  statement = conn.createStatement()
  statement.excuteQuery(sql)
  // 预编译SQL
  prepareStatement = conn.parpareStatement(sql);
  prepareStatement.execute();
  ```

## 1）使用 JDBC 原生接口，实现数据库的增删改查操作。

先在 mysql 数据库创建一个数据库 test_tb。

定义工具类 DbUtil

```java
/**
 * JDBC 数据库操作工具类
 */
public class DbUtil {

    public static final String URL = "jdbc:mysql://192.168.10.100:3306/test_db";
    public static final String USER = "root";
    public static final String PASSWORD = "MySql.root888";

    static {
        try {
            // 加载驱动程序
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接
     * @return Connection
     */
    public static Connection getConnection(){
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}
```

定义实体类 Student：

```java
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Student implements Serializable {

    private Integer id; // 自增主键
    private Integer sno; // 学号
    private String name; // 姓名
    private Integer age; // 年龄

    public Student(Integer sno, String name, Integer age) {
        this.sno = sno;
        this.name = name;
        this.age = age;
    }
}
```

建表、增删改查操作类 JDBCOperation：

```java
public class JDBCOperation {

    public static void main(String[] args) {
        try (Connection conn = DbUtil.getConnection();
             Scanner scanner = new Scanner(System.in);
        ){
            Statement stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS t_student( \n" +
                    "id INT UNSIGNED AUTO_INCREMENT, \n" +
                    "sno INT UNIQUE NOT NULL, \n" +
                    "name VARCHAR(20) NOT NULL, \n" +
                    "age INT, \n" +
                    "PRIMARY KEY(id) \n" +
                    ")ENGINE=InnoDB DEFAULT CHARSET=utf8;";
            System.out.println("建表语句：\n" + sql);
            // stmt.execute(sql) 返回值 true 表示第一个返回值是一个
            // ResultSet 对象，false表示这是一个更新个数或者没有结果集。
            stmt.execute(sql);
            System.out.println("建表成功.");
            while (true){
                System.out.print("请输入操作【1添加，2删除，3修改，4查询，0退出】：");
                int op = scanner.nextInt();
                switch (op){
                    case 1: addStudent(scanner); break;
                    case 2: delStudent(scanner); break;
                    case 3: updateStudent(scanner); break;
                    case 4: queryStudent(scanner); break;
                    case 0: return;
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private static void queryStudent(Scanner scanner) {
        System.out.print("请输入学号（输入0查询全部数据）：");
        int sno = scanner.nextInt();
        String sql = "SELECT * FROM t_student";
        if(sno != 0){
            sql += " WHERE sno = " + sno;
        }
        try (Connection conn = DbUtil.getConnection()){
            Statement stmt = conn.createStatement();
            System.out.println("执行SQL语句：" + sql);
            ResultSet resultSet = stmt.executeQuery(sql);
            System.out.println("执行成功！查询结果：");
            List<Student> students = new ArrayList<>();
            while (resultSet.next()){
                students.add(new Student(
                        resultSet.getInt("id"),
                        resultSet.getInt("sno"),
                        resultSet.getString("name"),
                        resultSet.getInt("age")));
            }
            System.out.println("共查询到 " + students.size() + " 条数据：");
            System.out.println(students);
        } catch (SQLException e) {
            System.out.println("执行失败！");
            e.printStackTrace();
        }
    }

    private static void updateStudent(Scanner scanner) {
        System.out.print("请输入学号：");
        int sno = scanner.nextInt();
        System.out.print("请输入姓名：");
        String name = scanner.next();
        System.out.print("请输入年龄：");
        int age = scanner.nextInt();
        String sql = "UPDATE t_student set name = '" + name + "'" +
                ",age = " + age +
                " WHERE sno = " + sno;
        try (Connection conn = DbUtil.getConnection()){
            Statement stmt = conn.createStatement();
            System.out.println("执行SQL语句：" + sql);
            stmt.execute(sql);
            System.out.println("修改成功！");
        } catch (SQLException e) {
            System.out.println("修改失败！");
            e.printStackTrace();
        }
    }

    private static void delStudent(Scanner scanner){
        System.out.print("请输入学号（输入0清空数据）：");
        int sno = scanner.nextInt();
        String sql = "DELETE FROM t_student ";
        if(sno != 0){
            sql += "WHERE sno = " + sno;
        }
        try (Connection conn = DbUtil.getConnection()){
            Statement stmt = conn.createStatement();
            System.out.println("执行SQL语句：" + sql);
            stmt.execute(sql);
            System.out.println("删除成功！");
        } catch (SQLException e) {
            System.out.println("删除失败！");
            e.printStackTrace();
        }
    }

    private static void addStudent(Scanner scanner){
        System.out.print("请输入学号：");
        int sno = scanner.nextInt();
        System.out.print("请输入姓名：");
        String name = scanner.next();
        System.out.print("请输入年龄：");
        int age = scanner.nextInt();
        addByStatement(new Student(sno, name, age));
    }

    private static void addByStatement(Student student) {
        String sql = "INSERT INTO t_student(sno, name, age) values (" +
                student.getSno() +
                ",'" + student.getName() + "'" +
                "," + student.getAge() +
                ");";
        try (Connection conn = DbUtil.getConnection()){
            Statement stmt = conn.createStatement();
            System.out.println("执行SQL语句：" + sql);
            stmt.execute(sql);
            System.out.println("执行成功！");
        } catch (SQLException e) {
            System.out.println("执行失败！");
            e.printStackTrace();
        }
    }
}
```

## 2）使用事务，PrepareStatement 方式，批处理方式，改进上述操作。

上面的添加方式修改为预编译批量添加 student，修改部分代码：

```java
    /**
     * 输入、添加学生
     * @param scanner
     */
    private static void addStudent(Scanner scanner){
        List<Student> students = new ArrayList<>();
        while (true){
            System.out.print("请输入学号：");
            int sno = scanner.nextInt();
            System.out.print("请输入姓名：");
            String name = scanner.next();
            System.out.print("请输入年龄：");
            int age = scanner.nextInt();
            students.add(new Student(sno, name, age));
            System.out.print("是否继续添加（y/n）：");
            String op = scanner.next();
            if(!"y".equalsIgnoreCase(op)){
                break;
            }
        }
        // 调用批量添加
        addByPreparedStatement(students);
    }

    /**
     * 使用事务，预编译方式批量添加 student
     * @param students
     */
    private static void addByPreparedStatement(List<Student> students) {
        // 定义 sql
        String sql = "INSERT INTO t_student(sno, name, age) values (?, ?, ?)";

        try (Connection conn = DbUtil.getConnection()){
            // 关闭自动提交
            conn.setAutoCommit(false);
            // 预编译 sql
            PreparedStatement ps = conn.prepareStatement(sql);
            // 设置参数
            for (Student student : students) {
                ps.setInt(1, student.getSno());
                ps.setString(2, student.getName());
                ps.setInt(3, student.getAge());
                ps.addBatch(); // 添加到批处理
            }
            // 批量添加
            int[] batch = ps.executeBatch();
            int count = 0;
            for (int n : batch) {
                count += n;
            }
            System.out.println("添加成功！添加了 " + count + " 条数据。");
            conn.commit(); // 提交事务
        } catch (SQLException e) {
            System.out.println("添加失败！");
            e.printStackTrace();
        }
    }
```

## 3）配置 Hikari 连接池，改进上述操作。提交代码到 GitHub。

