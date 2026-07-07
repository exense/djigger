package io.djigger.monitoring.java.instrumentation.subscription;

import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.model.StackTraceElement;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubscriptionsTest {

    @Test
    public void test1() {
        Set<InstrumentSubscription> subscription = new HashSet<InstrumentSubscription>();

        StackTraceElement el = new StackTraceElement("Class1", "Method1", null, -1);
        StackTraceElement[] els = new StackTraceElement[]{el};
        subscription.add(new RealNodePathSubscription(els, false));

        StackTraceElement el2 = new StackTraceElement("Class1", "Method1", null, -1);
        StackTraceElement[] els2 = new StackTraceElement[]{el2};
        subscription.remove(new RealNodePathSubscription(els2, false));

        assertEquals(0, subscription.size());

    }
}
