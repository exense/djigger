package io.djigger.collector.accessors;

import io.djigger.model.TaggedMetric;
import io.djigger.monitoring.java.model.GenericObject;
import io.djigger.monitoring.java.model.Metric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Exercises {@link MetricAccessor} end-to-end against a real MongoDB: index/TTL creation, {@code save}
 * (single and batch) and the {@code get} read-back, covering the Metric &lt;-&gt; Document mapping including
 * nested {@link GenericObject} values and the {@code .}/{@code $} key encoding used to keep keys BSON-valid.
 */
public class MetricAccessorIntegrationTest extends AbstractMongoIntegrationTest {

    private MetricAccessor accessor;

    @BeforeEach
    void initAccessor() {
        accessor = new MetricAccessor(connection);
        accessor.createIndexesIfNeeded(3600L);
    }

    @Test
    public void savesAndReadsSimpleMetricWithTags() {
        long now = System.currentTimeMillis();
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "S01");
        accessor.save(new TaggedMetric(tags, new Metric<>(now, "cpu.load", 0.42d)));

        List<Metric<?>> result = read(now);
        assertEquals(1, result.size());
        Metric<?> metric = result.get(0);
        assertEquals("cpu.load", metric.getName());
        assertEquals(0.42d, ((Number) metric.getValue()).doubleValue());
        assertEquals("S01", metric.getAttributes().get("env"));
    }

    @Test
    public void savesAndReadsGenericObjectValueWithDottedKeys() {
        long now = System.currentTimeMillis();
        GenericObject value = new GenericObject();
        value.put("HeapMemoryUsage.used", 1234L); // '.' is not a valid BSON key and must be encoded/decoded
        accessor.save(new TaggedMetric(null, new Metric<>(now, "java.lang/type=Memory", value)));

        List<Metric<?>> result = read(now);
        assertEquals(1, result.size());
        Metric<?> metric = result.get(0);
        assertEquals("java.lang/type=Memory", metric.getName());
        GenericObject readValue = assertInstanceOf(GenericObject.class, metric.getValue());
        assertEquals(1234L, ((Number) readValue.get("HeapMemoryUsage.used")).longValue());
    }

    @Test
    public void saveListInsertsAllMetrics() {
        long now = System.currentTimeMillis();
        accessor.save(Arrays.asList(
                new TaggedMetric(null, new Metric<>(now, "m1", 1)),
                new TaggedMetric(null, new Metric<>(now, "m2", 2))));

        assertEquals(2, read(now).size());
    }

    private List<Metric<?>> read(long around) {
        Date from = new Date(around - 60_000);
        Date to = new Date(around + 60_000);
        List<Metric<?>> result = new ArrayList<>();
        accessor.get(null, from, to).forEachRemaining(result::add);
        return result;
    }
}
