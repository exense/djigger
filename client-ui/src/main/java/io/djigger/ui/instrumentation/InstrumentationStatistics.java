/*******************************************************************************
 * (C) Copyright  2016 Jérôme Comte and others.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *    - Jérôme Comte
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


public class InstrumentationStatistics implements Serializable {

	private static final long serialVersionUID = -6506453302335513463L;
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}

	public HashSet<Long> getThreadIds() {
		return threadIds;
	}

}
