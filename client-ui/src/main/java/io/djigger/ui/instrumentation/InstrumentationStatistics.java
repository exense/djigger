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
package io.djigger.ui.instrumentation;

import io.djigger.monitoring.java.instrumentation.InstrumentationAttributesHolder;
import io.djigger.monitoring.java.instrumentation.InstrumentationSample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InstrumentationStatistics implements Serializable {

	private static final long serialVersionUID = -6506453302335513463L;
	
	private static final Logger logger = LoggerFactory.getLogger(InstrumentationStatistics.class);
	
	private final DateFormat format = new SimpleDateFormat("hh:mm:ss");

	private long start;
	
	private long end;

	private final ArrayList<Sample> samples;

	private final HashSet<Long> threadIds;

	private long totalTimeSpent;

	private int realCount;

	private Integer averageResponseTime;
	
	private Integer throughput;

	public InstrumentationStatistics() {
		super();
		start = Long.MAX_VALUE;
		samples = new ArrayList<Sample>();
		threadIds = new HashSet<Long>();
	}

	public InstrumentationStatistics(List<Sample> samples) {
		this();
		this.samples.addAll(samples);

		long totalTimeSpent = 0;
		for(Sample s:samples) {
			realCount++;
			totalTimeSpent += s.getElapsed();
		}

		if(realCount>0) {
			averageResponseTime = (int)(totalTimeSpent/realCount);
		}
	}

	public void update(InstrumentationSample sample) {
		start = Math.min(start, sample.getStart());
		end = Math.max(end, sample.getEnd());
		realCount++;
		int duration = (int)(sample.getEnd()-sample.getStart());
		totalTimeSpent += duration;
		synchronized(samples) {
			samples.add(new Sample(sample.getAtributesHolder().getThreadID(), sample.getStart(), duration, sample.getAtributesHolder()));
		}

		threadIds.add(sample.getAtributesHolder().getThreadID());

		averageResponseTime = null;
		throughput = null;
	}

	public Integer getRealCount() {
		return realCount;
	}

	public Integer getAverageResponseTime() {
		if(averageResponseTime==null) {
			if(realCount>0) {
				averageResponseTime = (int)(totalTimeSpent/realCount);
			} else {
				return 0;
			}
		}
		return averageResponseTime;
	}

	public Integer getThroughput() {
		if(throughput==null) {
			if(realCount>1 && end-start>0) {
				throughput = (int)(60000L*realCount/(end-start));
			} else {
				return 0;
			}
		}
		return throughput;
	}

	public Long getTotalTimeSpent() {
		return totalTimeSpent;
	}

	public class Sample implements Serializable {

		private static final long serialVersionUID = 9177464584885284437L;
		

		private final long threadId;

		private final long time;

		private final int elapsed;
		
		private final InstrumentationAttributesHolder attributes;

		public Sample(long threadId, long time, int elapsed, InstrumentationAttributesHolder attributes) {
			super();
			this.threadId = threadId;
			this.time = time;
			this.elapsed = elapsed;
			this.attributes = attributes;
		}

		public long getTime() {
			return time;
		}

		public int getElapsed() {
			return elapsed;
		}

		public long getThreadId() {
			return threadId;
		}

		public InstrumentationAttributesHolder getAttributes() {
			return attributes;
		}

		@Override
		public String toString() {
			return "Sample [threadId=" + threadId + ", time=" + time
					+ ", elapsed=" + elapsed + "]";
		}
	}

	public List<Sample> getSamples() {
		return samples;
	}

	public void export(File file, String label) {
		PrintWriter writer = null;
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
			writer = new PrintWriter(new BufferedWriter(new FileWriter(file,true)));
			synchronized(samples) {
				for(Sample s:samples) {
					writer.println(format.format(new Date(s.time)) + "\t" + label + "\t" + s.elapsed);
				}
			}
		} catch (IOException e) {
			logger.error("Error while exporting samples to file " + file, e);
		} finally {
			writer.close();
		}
	}

	public HashSet<Long> getThreadIds() {
		return threadIds;
	}

}
