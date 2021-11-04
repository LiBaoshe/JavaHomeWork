
/**
 * 1.（选做）自己写一个简单的 Hello.java，里面需要涉及基本类型，
 * 四则运行，if 和 for，然后自己分析一下对应的字节码，有问题群里讨论。
 *
 * javac Work01Hello.java
 * javac -g Work01Hello.java
 * javap -c Work01Hello
 * javap -c -verbose Hello
 */
public class Work01Hello {

    public static void main(String[] args) {
        int x = 4;
        int y = 5;
        int z = 3;
        int sum = sum(x, y, z);
        System.out.println(sum);

        if(sum % 2 == 0){
            System.out.println(sum + "\t是偶数");
        } else {
            System.out.println(sum + "\t是奇数");
        }

        Work01Hello work01Hello = new Work01Hello();
        System.out.println(work01Hello.add(4, 5));
    }

    public static int sum(int... args){
        int sum = 0;
        for (int arg : args) {
            sum += arg;
        }
        return sum;
    }

    public int add(int x, int y){
        return x + y;
    }
}
