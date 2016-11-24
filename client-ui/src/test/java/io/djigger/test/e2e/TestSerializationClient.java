package io.djigger.test.e2e;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import org.junit.Test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;

public class TestSerializationClient {

	@Test
	public void test() throws IOException, ClassNotFoundException {

		try {
			ClassPool pool = ClassPool.getDefault();
			CtClass c = pool.get("io.djigger.test.e2e.TestClass");
			c.addField(new CtField(pool.get("java.lang.String"), "testField", c));
			c.toClass().getDeclaredFields();

			Socket socket = new Socket("localhost", 1111);
			ObjectInputStream o = new ObjectInputStream(socket.getInputStream());

			Object o2 = o.readObject();

			if(o2 instanceof TestClass) {
				System.out.println(((TestClass) o2).att1);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
