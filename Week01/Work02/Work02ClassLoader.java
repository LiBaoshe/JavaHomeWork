
import java.io.*;
import java.lang.reflect.Method;

/**
 * 2.（必做）自定义一个 Classloader，加载一个 Hello.xlass 文件，执行 hello 方法，
 * 此文件内容是一个 Hello.class 文件所有字节（x=255-x）处理后的文件。文件群里提供。
 */
public class Work02ClassLoader extends ClassLoader{

    public static void main(String[] args) throws Exception {

        Class helloClass = new Work02ClassLoader().findClass("Hello");

        Method helloMethod = helloClass.getMethod("hello");

        helloMethod.invoke(helloClass.newInstance());
    }


    /**
     * 读取文件并解码
     * @param fileName
     * @return 返回解码后的字节流
     */
    private static byte[] decodeXlass(String fileName) {
        byte[] bytes = null;
        try (
                InputStream in = new FileInputStream(fileName);
                ){
            bytes = new byte[in.available()];
            in.read(bytes);
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (255 - bytes[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = decodeXlass("Hello.xlass");
        return defineClass(name, bytes,  0, bytes.length);
    }
}
