package io.djigger.test.e2e;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;

public class TestSerialization {

	public void test()  {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(1111);
			Socket socket = serverSocket.accept();
			ObjectOutputStream o = new ObjectOutputStream(socket.getOutputStream());
			
			//String s = "Test";
			
			
			
			ClassPool pool = ClassPool.getDefault();
			CtClass c = pool.get("io.djigger.test.e2e.TestClass");
			c.addField(new CtField(pool.get("java.lang.String"), "testField", c));
			
			TestClass i = (TestClass) c.toClass().newInstance();
			i.getClass().getDeclaredField("testField").set(i, "djigger");
			o.writeObject(i);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
