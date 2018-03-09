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
package io.djigger.monitoring.java.agent;

public class JavaAgentMessageType {

    public static String SUBSCRIBE_THREAD_SAMPLING = "SUBSCRIBE_THREAD_SAMPLING";

    public static String UNSUBSCRIBE_THREAD_SAMPLING = "UNSUBSCRIBE_THREAD_SAMPLING";

    public static String SUBSCRIBE_METRIC_COLLECTION = "SUBSCRIBE_METRIC_COLLECTION";

    public static String GET_RATE = "GET_RATE";

    public static String RESUME = "RESUME";

    public static String PAUSE = "PAUSE";

    public static String INSTRUMENT = "INSTRUMENT";

    public static String DEINSTRUMENT = "DEINSTRUMENT";

    public static String INSTRUMENT_BATCH_INTERVAL = "INSTRUMENT_BATCH_INTERVAL";

    public static String INSTRUMENT_SAMPLE = "INSTRUMENT_SAMPLE";

    public static String THREAD_SAMPLE = "THREAD_SAMPLE";

    public static String METRICS = "METRICS";

    public static String RESPONSE = "RESPONSE";

    public static String MAX_AGENT_SESSIONS_REACHED = "MAX_AGENT_SESSIONS_REACHED";
}
