package io.djigger.collector.accessors;

import io.djigger.model.TaggedInstrumentationEvent;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.instrumentation.InstrumentationEventData;
import io.djigger.monitoring.java.instrumentation.StringInstrumentationEventData;
import io.djigger.monitoring.java.model.GlobalThreadId;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises {@link InstrumentationEventAccessor} end-to-end against a real MongoDB. The save/get
 * round-trip covers the {@code toDocument}/{@code fromDocument} mapping, including the {@code ObjectId}
 * handling and the {@code data} (BsonArray) payload - the areas most sensitive to the bson 3.x -> 5.x bump.
 */
public class InstrumentationEventAccessorIntegrationTest extends AbstractMongoIntegrationTest {

    private InstrumentationEventAccessor accessor;

    @BeforeEach
    void initAccessor() {
        accessor = new InstrumentationEventAccessor(db);
        accessor.createIndexesIfNeeded(3600L);
    }

    @Test
    public void savesAndReadsBackEventWithData() {
        long now = System.currentTimeMillis();
        UUID transactionId = UUID.randomUUID();
        String eventId = new ObjectId().toHexString();

        InstrumentationEvent event = new InstrumentationEvent("com.example.Service", "process");
        event.setId(eventId);
        event.setStart(now);
        event.setDuration(123_000L);
        event.setGlobalThreadId(new GlobalThreadId("runtime-1", 7L));
        event.setTransactionID(transactionId);
        List<InstrumentationEventData> data = new ArrayList<>();
        data.add(new StringInstrumentationEventData("payload-1"));
        event.setData(data);

        Map<String, String> tags = new HashMap<>();
        tags.put("env", "S01");

        accessor.save(new TaggedInstrumentationEvent(tags, event));

        Date from = new Date(now - 60_000);
        Date to = new Date(now + 60_000);

        List<InstrumentationEvent> read = new ArrayList<>();
        Iterator<InstrumentationEvent> it = accessor.get(null, from, to);
        it.forEachRemaining(read::add);

        assertEquals(1, read.size());
        InstrumentationEvent actual = read.get(0);
        assertEquals("com.example.Service", actual.getClassname());
        assertEquals("process", actual.getMethodname());
        assertEquals(123_000L, actual.getDuration());
        assertEquals(eventId, actual.getId());
        assertEquals(transactionId, actual.getTransactionID());
        assertEquals(7L, actual.getGlobalThreadId().getThreadId());
        assertEquals(1, actual.getData().size());
        assertEquals("payload-1", ((StringInstrumentationEventData) actual.getData().get(0)).getPayload());
    }

    @Test
    public void getByTransactionIdReturnsMatchingEvent() {
        long now = System.currentTimeMillis();
        UUID transactionId = UUID.randomUUID();

        InstrumentationEvent event = new InstrumentationEvent("com.example.Service", "process");
        event.setId(new ObjectId().toHexString());
        event.setStart(now);
        event.setDuration(1_000L);
        event.setGlobalThreadId(new GlobalThreadId("runtime-1", 7L));
        event.setTransactionID(transactionId);

        accessor.save(new TaggedInstrumentationEvent(null, event));

        Iterator<InstrumentationEvent> it = accessor.getByTransactionId(transactionId);
        assertTrue(it.hasNext(), "expected to find the event by its transaction id");
        assertEquals(transactionId, it.next().getTransactionID());
    }
}
