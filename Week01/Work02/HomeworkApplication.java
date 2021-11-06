package com.geek.homework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * 2.（必做）自定义一个 Classloader，加载一个 Hello.xlass 文件，执行 hello 方法，
 * 此文件内容是一个 Hello.class 文件所有字节（x=255-x）处理后的文件。文件群里提供。
 */
@SpringBootApplication
public class HomeworkApplication extends ClassLoader {

	public static void main(String[] args) throws Exception{
		SpringApplication.run(HomeworkApplication.class, args);
		// 加载类
		Class helloClass = new HomeworkApplication().findClass("Hello");
		// 获取方法
		Method helloMethod = helloClass.getMethod("hello");
		// 调用方法
		helloMethod.invoke(helloClass.newInstance());
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		// 文件名
		String fileName = name.replace(".", "/") + ".xlass";
		// 解码
		byte[] bytes = decodeXlass(fileName);
		// 返回定义类
		return defineClass(name, bytes,  0, bytes.length);
	}

	/**
	 * 读取文件并解码
	 * @param fileName
	 * @return 返回解码后的字节流
	 */
	private static byte[] decodeXlass(String fileName) {
		byte[] bytes = null;
		try (	// try 语句块中定义流可以自动关闭
				InputStream in = HomeworkApplication.class.getClassLoader().getResourceAsStream(fileName);
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
}
