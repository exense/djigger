package io.djigger.monitoring.java.instrumentation;

import io.djigger.monitoring.java.model.GlobalThreadId;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Guards the agent -> collector wire contract after the bson 3.x -> 5.x bump.
 *
 * <p>Note: {@link InstrumentationEvent} is the object actually serialized over the wire, and it carries
 * only {@link String}/{@link UUID} identifiers - the bson {@link ObjectId} is used agent-side purely to
 * <em>generate</em> a hex id and never crosses the wire as a bson type. This test therefore verifies both
 * that the event survives a Java serialization round-trip and that the {@code ObjectId} hex contract used
 * by the agent is stable across the driver upgrade.
 */
public class InstrumentationEventSerializationTest {

    @Test
    public void instrumentationEventSurvivesJavaSerialization() throws Exception {
        String id = new ObjectId().toHexString();
        String parentId = new ObjectId().toHexString();
        UUID transactionId = UUID.randomUUID();

        InstrumentationEvent event = new InstrumentationEvent("com.example.Service", "process");
        event.setId(id);
        event.setParentID(parentId);
        event.setTransactionID(transactionId);
        event.setStart(1_000L);
        event.setDuration(500_000L);
        event.setGlobalThreadId(new GlobalThreadId("runtime-1", 7L));
        event.addData(new StringInstrumentationEventData("payload-1"));

        InstrumentationEvent restored = roundTrip(event);

        assertEquals(id, restored.getId());
        assertEquals(parentId, restored.getParentID());
        assertEquals(transactionId, restored.getTransactionID());
        assertEquals("com.example.Service", restored.getClassname());
        assertEquals("process", restored.getMethodname());
        assertEquals(500_000L, restored.getDuration());
        assertEquals(7L, restored.getGlobalThreadId().getThreadId());
        assertEquals("runtime-1", restored.getGlobalThreadId().getRuntimeId());
        assertEquals(1, restored.getData().size());
        assertEquals("payload-1", ((StringInstrumentationEventData) restored.getData().get(0)).getPayload());
    }

    @Test
    public void objectIdHexContractIsStable() {
        String hex = new ObjectId().toHexString();
        // the agent stores ids as hex strings and re-parses tracer ids via new ObjectId(hex)
        assertEquals(hex, new ObjectId(hex).toHexString());
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundTrip(T object) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (T) ois.readObject();
        }
    }
}
