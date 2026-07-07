package io.djigger.test.e2e;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Verifies that a {@link TestClass} instance survives a Java serialization round-trip.
 * This used to rely on an external socket server ({@code TestSerialization}) and swallowed
 * all exceptions, so it never actually asserted anything. It is now self-contained.
 */
public class TestSerializationClient {

    @Test
    public void test() throws Exception {
        TestClass original = new TestClass("djigger");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(original);
        }

        Object deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            deserialized = ois.readObject();
        }

        assertInstanceOf(TestClass.class, deserialized);
        assertEquals("djigger", ((TestClass) deserialized).att1);
    }
}
