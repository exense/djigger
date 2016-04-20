package io.djigger.test.e2e;

public class TestApp {

	public static void main(String[] args) {
		(new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					testMethod1ms();
				}
			}
		})).start();;
		
		(new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					testMethod1000nanos();
				}
			}
		})).start();;
	}
	
	public static void testMethod1ms() {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {}
	}
	
	public static void testMethod1000nanos() {
		long t1 = System.nanoTime();
		while(System.nanoTime()-t1<1000) {
			try {
				Thread.sleep(0, 10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
