package io.djigger.it;

import io.djigger.client.Facade;
import io.djigger.client.JstackLogTailFacade;
import io.djigger.it.support.CollectingFacadeListener;
import io.djigger.monitoring.java.model.StackTraceElement;
import io.djigger.monitoring.java.model.ThreadInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link JstackLogTailFacade}: it tails a file containing a {@code jstack}-format thread dump and
 * emits the parsed {@link ThreadInfo}s to its listeners. This test writes a small standard-output thread
 * dump to a temporary file, points the facade at it (reading from the beginning of the file), and asserts
 * that the expected thread and its stack frame are parsed.
 *
 * <p>Pure parsing test - no child JVM, no MongoDB, no external dependency - so it always runs on CI.
 */
@Tag("integration")
public class JstackLogTailIT {

    // A minimal but valid "Full thread dump" (Format.STANDARD_OUTPUT). The date line immediately before
    // "Full thread dump" is required: the parser stamps each thread with it. Each thread is terminated by
    // a blank separator line (including the last one) so that it is flushed to the listener.
    private static final String JSTACK = String.join("\n",
            "2024-01-01 12:00:00",
            "Full thread dump Java HotSpot(TM) 64-Bit Server VM (17+35):",
            "",
            "\"djigger-sample-worker\" #12 prio=5 os_prio=0 tid=0x0a nid=0x0b waiting on condition [0x0c]",
            "   java.lang.Thread.State: TIMED_WAITING (sleeping)",
            "\tat java.lang.Thread.sleep(Native Method)",
            "\tat io.djigger.it.support.SampleApp.businessMethod(SampleApp.java:31)",
            "",
            "\"main\" #1 prio=5 os_prio=0 tid=0x01 nid=0x02 runnable [0x03]",
            "   java.lang.Thread.State: RUNNABLE",
            "\tat io.djigger.it.support.SampleApp.main(SampleApp.java:27)",
            "",
            "");

    @Test
    public void tailsAJstackFileAndParsesThreadDumps() throws Exception {
        File file = File.createTempFile("djigger-jstack", ".txt");
        Files.write(file.toPath(), JSTACK.getBytes(StandardCharsets.UTF_8));

        JstackLogTailFacade facade = new JstackLogTailFacade(props(file), false);
        CollectingFacadeListener listener = new CollectingFacadeListener();
        facade.addListener(listener);
        try {
            // startAtFileBegin=true: connect() reads the whole file and returns once end-of-file is reached,
            // by which point every thread in the file has been parsed and delivered to the listener.
            facade.connect();

            assertFalse(listener.threadInfos.isEmpty(), "expected thread dumps to be parsed from the jstack file");

            ThreadInfo worker = listener.threadInfos.stream()
                    .filter(t -> "djigger-sample-worker".equals(t.getName()))
                    .findFirst().orElse(null);
            assertNotNull(worker, "expected the sample worker thread to be parsed from the jstack file");
            assertEquals(Thread.State.TIMED_WAITING, worker.getState(), "unexpected parsed thread state");

            boolean businessMethodSeen = false;
            for (StackTraceElement e : worker.getStackTrace()) {
                if ("io.djigger.it.support.SampleApp".equals(e.getClassName())
                        && "businessMethod".equals(e.getMethodName())) {
                    businessMethodSeen = true;
                }
            }
            assertTrue(businessMethodSeen, "expected SampleApp.businessMethod in the parsed stack trace");
        } finally {
            facade.destroy();
            file.delete();
        }
    }

    private static Properties props(File file) {
        Properties p = new Properties();
        p.setProperty(Facade.Parameters.FILE, file.getAbsolutePath());
        p.setProperty(Facade.Parameters.START_AT_FILE_BEGIN, "true");
        return p;
    }
}
