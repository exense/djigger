package io.djigger.it.support;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * Launches a child JVM reusing the current JVM's java executable and classpath. Used by the integration
 * tests to start target applications (with a djigger agent or with JMX enabled) in a separate process.
 */
public final class JvmLauncher {

    private JvmLauncher() {
    }

    /** Launches the main class using the current JVM's full classpath. */
    public static Process launch(Class<?> mainClass, List<String> jvmArgs, List<String> appArgs) throws IOException {
        return launch(mainClass, System.getProperty("java.class.path"), jvmArgs, appArgs);
    }

    /**
     * The filesystem location a class was loaded from, usable as a minimal classpath. Used to launch an
     * agent target with only its own code on the classpath - just like a real monitored application, whose
     * classpath does not contain djigger's (unshaded) jars, so instrumented classes resolve djigger types
     * exclusively from the shaded {@code -javaagent} jar.
     */
    public static String codeSourceOf(Class<?> clazz) {
        try {
            return new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("Unable to resolve the code source of " + clazz.getName(), e);
        }
    }

    /**
     * Builds a minimal classpath from the code sources of the given classes (order-preserving, de-duplicated).
     * Used to launch a monitored target with only its own code and its workload libraries - and crucially
     * <em>without</em> djigger's unshaded jars, which would collide with the shaded agent (see the module README).
     */
    public static String classpathOf(Class<?>... classes) {
        java.util.LinkedHashSet<String> entries = new java.util.LinkedHashSet<>();
        for (Class<?> clazz : classes) {
            entries.add(codeSourceOf(clazz));
        }
        return String.join(File.pathSeparator, entries);
    }

    public static Process launch(Class<?> mainClass, String classpath, List<String> jvmArgs, List<String> appArgs) throws IOException {
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.addAll(jvmArgs);
        command.add("-cp");
        command.add(classpath);
        command.add(mainClass.getName());
        command.addAll(appArgs);

        // Redirect the child's output to a log file rather than inheriting the failsafe forked JVM's
        // native stream (which corrupts failsafe's IPC channel). The log stays available for debugging.
        File logDir = new File("target", "child-jvm-logs");
        logDir.mkdirs();
        File log = new File(logDir, mainClass.getSimpleName() + "-" + System.currentTimeMillis() + ".log");

        return new ProcessBuilder(command)
                .redirectOutput(log)
                .redirectErrorStream(true)
                .start();
    }

    /**
     * Reserves an ephemeral TCP port and releases it immediately so it can be handed to a child JVM.
     * There is an inherent (small) race between release and re-bind, which is acceptable for tests.
     */
    public static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Unable to allocate a free port", e);
        }
    }

    public static void stop(Process process) {
        if (process == null) {
            return;
        }
        process.destroy();
        try {
            if (!process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
    }
}
