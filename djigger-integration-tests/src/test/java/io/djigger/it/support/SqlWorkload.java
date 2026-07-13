package io.djigger.it.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Target workload for {@code SqlTracerIT}: opens an in-memory HSQLDB connection and repeatedly runs a
 * {@link PreparedStatement} and a plain {@link Statement} query so djigger's SQL tracers have JDBC calls
 * to instrument. Runs until the launching test destroys the JVM.
 *
 * <p>Adapted from the former {@code djigger-demo} {@code SQLTracerTest}. It needs only {@code java.sql}
 * plus the HSQLDB driver on its classpath - no djigger jars - so the shaded agent can instrument it
 * without a classpath collision (see the module README).
 */
public class SqlWorkload {

    public static void main(String[] args) throws Exception {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:djiggerit", "SA", "");
        try (Statement ddl = connection.createStatement()) {
            ddl.execute("CREATE TABLE test (att1 VARCHAR(10))");
        }

        System.out.println("SqlWorkload started");
        while (!Thread.currentThread().isInterrupted()) {
            // PreparedStatement.execute() -> SQLPreparedStatementTracer
            try (PreparedStatement ps = connection.prepareStatement("select * from test")) {
                ps.execute();
            }
            // Statement.executeQuery(String) -> SQLStatementTracer (captures the SQL text "$1")
            try (Statement st = connection.createStatement()) {
                st.executeQuery("select * /*djigger-it*/ from test");
            }
            Thread.sleep(200);
        }
    }
}
