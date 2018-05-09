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
package io.djigger.client;

import io.djigger.client.jstack.Parser;
import io.djigger.client.jstack.Parser.Format;
import io.djigger.client.jstack.Parser.ParserEventListener;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.tailer.LogTailer;
import io.djigger.tailer.LogTailer.LogTailerListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JstackLogTailFacade extends Facade {

    private LogTailer logTailer;

    public JstackLogTailFacade(Properties properties, boolean autoReconnect) {
        super(properties, autoReconnect);
    }

    private final Object lock = new Object();

    @Override
    protected void connect_() throws Exception {

        String fileParam = properties.getProperty(Parameters.FILE);
        if (fileParam != null) {
            final List<ThreadInfo> threadInfos = new ArrayList<>(1);
            final Parser parser = new Parser(Format.STANDARD_OUTPUT, new ParserEventListener() {
                @Override
                public void onThreadParsed(ThreadInfo thread) {
                    threadInfos.clear();
                    threadInfos.add(thread);
                    for (FacadeListener listener : listeners) {
                        listener.threadInfosReceived(threadInfos);
                    }
                }
            });

            boolean startAtFileBegin = Boolean.parseBoolean(properties.getProperty(Parameters.START_AT_FILE_BEGIN, "false"));

            File file = new File(fileParam);
            logTailer = new LogTailer(file, startAtFileBegin, new LogTailerListener() {
                @Override
                public void onNewLine(String line) {
                    try {
                        parser.consumeLine(line);
                    } catch (IOException e) {
                        //logger.
                    }
                }

                @Override
                public void onEndOfFileReached() {
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            });

            if (startAtFileBegin) {
                synchronized (lock) {
                    lock.wait();
                }
            }
        } else {
            throw new RuntimeException("File not specified. Please specify the file to be read by setting the parameter '" + Parameters.FILE + "'");
        }
    }


    @Override
    protected void destroy_() {
        logTailer.close();
    }

    @Override
    protected void addInstrumentation_(InstrumentSubscription subscription) {

    }

    @Override
    protected void removeInstrumentation_(InstrumentSubscription subscription) {

    }

    @Override
    protected void startSampling() {

    }

    @Override
    protected void stopSampling() {

    }

    @Override
    public boolean hasStartStopSupport() {
        return false;
    }


    @Override
    public boolean hasInstrumentationSupport() {
        return false;
    }

}
