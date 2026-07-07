package io.djigger.it.support;

/**
 * A trivial target application launched in a child JVM by the integration tests. It keeps a worker thread
 * busy calling named methods so that:
 * <ul>
 *   <li>JMX thread sampling has something to observe, and</li>
 *   <li>a djigger agent can instrument the {@link #businessMethod()} call.</li>
 * </ul>
 * It runs until the JVM is destroyed by the test.
 */
public class SampleApp {

    public static void main(String[] args) throws Exception {
        Thread worker = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                businessMethod();
                sleep(50);
            }
        }, "djigger-sample-worker");
        worker.setDaemon(true);
        worker.start();

        System.out.println("SampleApp started (pid=" + ProcessHandle.current().pid() + ")");

        // keep the JVM alive until it is destroyed by the launching test
        Thread.currentThread().join();
    }

    public static void businessMethod() {
        nestedMethod();
        sleep(10);
    }

    private static void nestedMethod() {
        sleep(5);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
