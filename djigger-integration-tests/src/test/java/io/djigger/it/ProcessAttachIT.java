package io.djigger.it;

import com.sun.tools.attach.VirtualMachine;
import io.djigger.client.Facade;
import io.djigger.client.ProcessAttachFacade;
import io.djigger.it.support.Await;
import io.djigger.it.support.CollectingFacadeListener;
import io.djigger.it.support.JvmLauncher;
import io.djigger.it.support.SampleApp;
import io.djigger.monitoring.java.instrumentation.subscription.SimpleSubscription;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Covers {@link ProcessAttachFacade}: attaches to an already-running JVM by PID (via the JVM attach API),
 * dynamically loads the djigger agent into it, then samples thread dumps and instruments a method - the
 * capabilities that only the agent-based facades provide.
 *
 * <p>Same-version attach: the target runs on the same (build) JVM as the test, which is the supported
 * configuration (target runtime &gt;= 17; see FOLLOWUPS.md). The test is skipped where the environment
 * forbids dynamic JVM attach, and where the shaded agent jar is not on the test classpath.
 *
 * <p>The target is launched with a <em>minimal</em> classpath ({@link JvmLauncher#codeSourceOf}) so that,
 * just like a real monitored application, its instrumented classes resolve djigger types exclusively from
 * the shaded agent jar (see djigger-integration-tests/README.md).
 */
@Tag("integration")
public class ProcessAttachIT {

    @Test
    public void attachesToARunningJvmSamplesAndInstruments() throws Exception {
        assumeTrue(ProcessAttachFacade.class.getClassLoader().getResource("agent.jar") != null,
                "agent.jar not on the test classpath - skipping process-attach test");

        Process app = JvmLauncher.launch(SampleApp.class, JvmLauncher.codeSourceOf(SampleApp.class),
                Collections.emptyList(), Collections.emptyList());

        ProcessAttachFacade facade = null;
        try {
            String pid = String.valueOf(app.pid());
            // Wait until the freshly started child VM is actually attachable, and skip the test if this
            // environment does not support attach at all - before exercising the facade's single attempt.
            awaitAttachable(pid, 30_000);

            Properties props = new Properties();
            props.setProperty(Facade.Parameters.PROCESS_ID, pid);
            props.setProperty(Facade.Parameters.CONNECTION_TIMEOUT, "30000");

            facade = new ProcessAttachFacade(props, false);
            CollectingFacadeListener listener = new CollectingFacadeListener();
            facade.addListener(listener);

            // attach to the child VM, load the agent, and wait for the agent to connect back
            facade.connect();
            assertTrue(facade.isConnected(), "process-attach facade should be connected after connect()");

            facade.setSamplingInterval(200);
            facade.setSampling(true);
            facade.addInstrumentation(new SimpleSubscription("io.djigger.it.support.SampleApp", "businessMethod", true));

            assertTrue(Await.until(() -> !listener.threadInfos.isEmpty(), 30_000),
                    "expected thread dumps sampled via the dynamically attached agent");
            assertTrue(Await.until(() -> !listener.instrumentationEvents.isEmpty(), 30_000),
                    "expected instrumentation events for SampleApp.businessMethod via the attached agent");

            // stop sampling and let the last in-flight batch drain before tearing the connection down
            facade.setSampling(false);
            Thread.sleep(500);
        } finally {
            if (facade != null) {
                facade.destroy();
            }
            JvmLauncher.stop(app);
        }
    }

    /**
     * Polls {@link VirtualMachine#attach(String)} until it succeeds (then immediately detaches), so the
     * subsequent real attach by {@link ProcessAttachFacade} does not race the child JVM's startup.
     *
     * <p>{@link AttachNotSupportedException} is treated as <em>transient</em>: while the child is still
     * coming up, the platform reports the target as not-yet-attachable (on Windows, e.g. "jvm.dll not
     * loaded by target process"), which clears once its JVM has finished initializing. Only if attach
     * never succeeds within the timeout - or the attach provider is missing entirely
     * ({@link UnsatisfiedLinkError}) - is the test skipped rather than failed.
     */
    private static void awaitAttachable(String pid, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        Throwable last = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                VirtualMachine vm = VirtualMachine.attach(pid);
                vm.detach();
                return;
            } catch (UnsatisfiedLinkError e) {
                assumeTrue(false, "JVM attach not available in this environment: " + e);
            } catch (Exception e) {
                // AttachNotSupportedException here is transient (target still starting up) or, rarely,
                // means attach is unsupported; either way retry until the deadline, then skip.
                last = e;
            }
            Thread.sleep(500);
        }
        assumeTrue(false, "target JVM (pid=" + pid + ") did not become attachable within "
                + timeoutMs + "ms: " + last);
    }
}
