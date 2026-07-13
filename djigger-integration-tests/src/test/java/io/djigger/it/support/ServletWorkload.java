package io.djigger.it.support;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Proxy;

/**
 * Target workload for {@code ServletTracerIT}: a class that directly implements {@link Servlet} (so
 * djigger's {@code ServletTracer} matches it) and repeatedly invokes {@code service(...)} with a
 * {@link HttpServletRequest}, so the tracer has servlet requests to instrument. Runs until the launching
 * test destroys the JVM.
 *
 * <p>Adapted from the former {@code djigger-demo} {@code ServletTracerTest}, but with no servlet container:
 * the tracer instruments {@code Servlet.service} regardless of how it is invoked, so we call it directly
 * with a {@link Proxy}-based request (its only relevant call is {@code getHeader("djigger")}). Needs only
 * {@code jakarta.servlet-api} on its classpath - no djigger jars (see the module README).
 */
public class ServletWorkload {

    /** A servlet that directly implements jakarta.servlet.Servlet, which is what ServletTracer matches. */
    static final class DemoServlet implements Servlet {
        @Override
        public void service(ServletRequest req, ServletResponse res) {
            // no-op: the tracer instruments this method's entry/exit and reads the "djigger" header from req
        }

        @Override
        public void init(ServletConfig config) {
        }

        @Override
        public ServletConfig getServletConfig() {
            return null;
        }

        @Override
        public String getServletInfo() {
            return null;
        }

        @Override
        public void destroy() {
        }
    }

    public static void main(String[] args) throws Exception {
        // Wait for the launching test to create the "go" file (passed as args[0]) before first using
        // DemoServlet. The test creates it only after applying the ServletTracer subscription, so the
        // class is instrumented via the normal class-load path (as a servlet in a container would be),
        // rather than via retransformation of an already-loaded class.
        if (args.length > 0) {
            java.io.File go = new java.io.File(args[0]);
            while (!go.exists()) {
                Thread.sleep(50);
            }
        }
        runLoop();
    }

    private static void runLoop() throws Exception {
        DemoServlet servlet = new DemoServlet();
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                ServletWorkload.class.getClassLoader(),
                new Class[]{HttpServletRequest.class},
                (proxy, method, methodArgs) -> {
                    Class<?> returnType = method.getReturnType();
                    // getHeader("djigger") -> null is fine; supply harmless defaults for everything else
                    if (returnType == boolean.class) {
                        return false;
                    }
                    if (returnType == int.class) {
                        return 0;
                    }
                    if (returnType == long.class) {
                        return 0L;
                    }
                    return null;
                });

        System.out.println("ServletWorkload started");
        while (!Thread.currentThread().isInterrupted()) {
            servlet.service(request, null);
            Thread.sleep(200);
        }
    }
}
