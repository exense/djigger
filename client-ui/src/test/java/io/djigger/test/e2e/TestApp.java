package io.djigger.test.e2e;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class TestApp {

    static Connection conn = null;


    private interface TEst {

    }

    private abstract class Test2 implements TEst {
    }

    private class Test3 extends Test2 {
    }

    public static void main(String[] args) {


//		try {
//			conn = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "");
//			conn.createStatement().execute("CREATE TABLE test (att1 VARCHAR(10))");
//		} catch (SQLException e) {
//		}
//		
//		(new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				while(true) {
//					testMethodRoot();
//				}
//			}
//		})).start();;
//		
        (new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    testMethod1000ms();
                    testMethod2000ms();
                }
            }
        })).start();
        ;

//		(new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				while(true) {
//					testMethod();
//				}
//			}
//		})).start();;
    }

    public static void testMethodRoot() {
        testMethod1();
        testMethod1();
    }

    public static void testMethod1() {
        testMethod2_1();
        testMethod2_2();
        testMethod2_3();
        testMethod2_4();
        testMethod_SQL();
    }

    public static void testMethod2_1() {
        testMethod1ms();
    }

    public static void testMethod2_2() {
        testMethod1ms();
    }

    public static String testMethod2_3() {
        testMethod1ms();
        return "testMethod2_3_result";
    }

    public static long testMethod2_4() {
        testMethod1ms();
        return 1;
    }

    public static long testMethod_SQL() {
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        PreparedStatement statement = null;
        try {
            statement = conn.prepareStatement("select * from test");
            //statement.execute();
            statement.executeQuery();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                statement.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        testMethod1ms();
        return 1;
    }


    public static void testMethod1ms() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }
    }

    public static void testMethod1000ms() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    public static void testMethod2000ms() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }

    public static void testMethod1000nanos() {
        long t1 = System.nanoTime();
        while (System.nanoTime() - t1 < 1000) {
            try {
                Thread.sleep(0, 10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private static long tRef = System.currentTimeMillis();
    private static long tRefNano = System.nanoTime();


    private static long convertToTime(long tNano) {
        return (tNano - tRefNano) / 1000000 + tRef;
    }

    public static void testMethod() {
        long t1 = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            //convertToTime(i);
            //System.nanoTime();
            testMethodAdd();
        }
        //System.out.println("TestMethod (ms)" + ((System.nanoTime()-t1)/1000000.0));
    }

    public static double testMethodAdd() {
        double sum = System.currentTimeMillis();
        for (long i = 0; i < 1000; i++) {
            sum += dummyCalc(i);
        }
        return sum;

    }

    public static double dummyCalc(long i) {
        return i * 10;
    }
}
