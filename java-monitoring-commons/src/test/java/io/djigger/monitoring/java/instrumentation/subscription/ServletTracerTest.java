/*******************************************************************************
 * (C) Copyright 2016 Jérôme Comte and Dorian Cransac
 *
 *  This file is part of djigger
 *
 *  djigger is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  djigger is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with djigger.  If not, see <http://www.gnu.org/licenses/>.
 *
 *******************************************************************************/
package io.djigger.monitoring.java.instrumentation.subscription;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class ServletTracerTest {

    public static void main(String[] args) throws Exception {
        ServletTracerTest test = new ServletTracerTest();
        test.test();
    }

    public void test() throws Exception {
        ServletHolder sh = new ServletHolder();

        sh.setServlet(new Servlet() {

            @Override
            public void service(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
                sleep();
                HttpServletRequest httpRequest = (HttpServletRequest) arg0;
                arg1.getWriter().write("Hello World ! This is a " + httpRequest.getMethod());
            }


            @Override
            public void init(ServletConfig arg0) throws ServletException {
                System.out.println("Initialized test servlet.");
            }

            @Override
            public String getServletInfo() {
                return null;
            }

            @Override
            public ServletConfig getServletConfig() {
                return null;
            }

            @Override
            public void destroy() {
            }
        });

        Server server = new Server(12298);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.addServlet(sh, "/*");
        server.setHandler(context);
        try {
            server.start();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        synchronized (server) {
            server.wait();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
