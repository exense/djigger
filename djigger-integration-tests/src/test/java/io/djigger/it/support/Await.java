package io.djigger.it.support;

import java.util.function.BooleanSupplier;

/** Small polling helper for the (asynchronous) integration tests. */
public final class Await {

    private Await() {
    }

    public static boolean until(BooleanSupplier condition, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            Thread.sleep(100);
        }
        return condition.getAsBoolean();
    }
}
