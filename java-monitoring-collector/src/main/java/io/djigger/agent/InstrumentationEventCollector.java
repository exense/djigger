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
package io.djigger.agent;

import io.djigger.monitoring.eventqueue.EventQueue;
import io.djigger.monitoring.java.instrumentation.*;
import io.djigger.monitoring.java.model.GlobalThreadId;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.monitoring.java.sampling.ThreadDumpHelper;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InstrumentationEventCollector {

    private static EventQueue<InstrumentationEvent> eventCollector;

    private static long tRef = System.currentTimeMillis();
    private static long tRefNano = System.nanoTime();

    private static ThreadLocal<Transaction> transactions = new ThreadLocal<Transaction>();

    private static Map<Long, Transaction> transactionMap = new ConcurrentHashMap<Long, Transaction>();

    private static long convertToTime(long tNano) {
        return (tNano - tRefNano) / 1000000 + tRef;
    }

    public static void setEventCollector(EventQueue<InstrumentationEvent> eventCollector) {
        InstrumentationEventCollector.eventCollector = eventCollector;
    }

    public static String getCurrentTracer() {
        Transaction tr = getCurrentTransaction();
        return tr != null ? tr.peekEvent().getId().toString() : null;
    }

    public static void applyTracer(String tracer) {
        if (tracer != null) {
            ObjectId parentId = new ObjectId(tracer);
            Transaction tr = getCurrentTransaction();
            if (tr != null) {
                tr.setParentId(parentId);
            } else {
                // TODO warning
            }
        }
    }

    public static void attachData(Object object, InstrumentationEventData data) {
        Transaction tr = getCurrentTransaction();
        if (tr != null) {
            tr.attachData(object, data);
        }
    }

    public static InstrumentationEventData getAttachedData(Object object) {
        Transaction tr = getCurrentTransaction();
        if (tr != null) {
            return tr.getAttachedData(object);
        } else {
            return null;
        }
    }

    public static void addDataToCurrentTransaction(InstrumentationEventData data) {
        Transaction tr = getCurrentTransaction();
        if (tr != null) {
            tr.addData(data);
        }
    }

    public static void leaveTransaction() {
        transactions.remove();
        transactionMap.remove(Thread.currentThread().getId());
    }

    public static void enterMethod(String classname, String method, boolean addThreadInfo, int subscriptionId) {
        InstrumentationEvent event;

        if (addThreadInfo) {
            event = new InstrumentationEventWithThreadInfo(classname, method);
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            ((InstrumentationEventWithThreadInfo) event).setThreadInfo(new ThreadInfo(ThreadDumpHelper.toStackTraceElement(stackTrace, 2)));
        } else {
            event = new InstrumentationEvent(classname, method);
        }

        event.setSubscriptionID(subscriptionId);
        event.setId(new ObjectId());

        Transaction transaction = getCurrentTransaction();
        if (transaction == null) {
            transaction = createNewTransaction();
        } else {
            InstrumentationEvent currentEvent = transaction.peekEvent();
            event.setParentID(currentEvent.getId());
        }

        // TODO set the runtime ID on the collector side?
        GlobalThreadId globalThreadId = new GlobalThreadId(null, Thread.currentThread().getId());
        event.setGlobalThreadId(globalThreadId);

        transaction.pushEvent(event);

        long startNano = System.nanoTime();
        event.setStartNano(System.nanoTime());
        event.setStart(convertToTime(startNano));
    }

    private static Transaction createNewTransaction() {
        Transaction transaction = new Transaction();
        setCurrentTransaction(transaction);
        return transaction;
    }

    private static Transaction getCurrentTransaction() {
        return transactions.get();
    }

    private static void setCurrentTransaction(Transaction transaction) {
        transactions.set(transaction);
        transactionMap.put(Thread.currentThread().getId(), transaction);
    }

    public static void leaveMethod() {
        leaveMethod(null);
    }

    public static void leaveMethodAndCaptureToString(Object data) {
        if (data != null) {
            leaveMethod(new StringInstrumentationEventData(data.toString()));
        } else {
            leaveMethod();
        }
    }

    public static void leaveMethodAndCaptureToString(Object data, Integer maxCaptureSize) {
        if (data != null) {
            leaveMethod(new StringInstrumentationEventData(data.toString(), maxCaptureSize));
        } else {
            leaveMethod();
        }
    }

    public static void leaveMethod(InstrumentationEventData data) {
        long endNano = System.nanoTime();

        Transaction transaction = getCurrentTransaction();
        InstrumentationEvent event = transaction.popEvent();
        event.setDuration(endNano - event.getStartNano());

        event.setTransactionID(transaction.getId());

        if (data != null) {
            event.addData(data);
        }

        eventCollector.add(event);

        if (transaction.isStackEmpty()) {
            leaveTransaction();

            if (transaction.getParentId() != null) {
                event.setParentID(transaction.getParentId());
            }

            List<InstrumentationEventData> trDataList = transaction.collectData();
            if (trDataList.size() > 0) {
                for (InstrumentationEventData trData : trDataList) {
                    event.addData(trData);
                }
            }
        }
    }

    public static Transaction getCurrentTransaction(long threadID) {
        return transactionMap.get(threadID);
    }


}
