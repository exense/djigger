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

package io.djigger.parser;

import io.djigger.monitoring.java.model.StackTraceElement;
import io.djigger.monitoring.java.model.ThreadInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Parser {

	private final Format format;
	
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public Parser(Format format) {
		super();
		this.format = format;
	}

	public List<ThreadInfo> parse(BufferedReader reader) throws IOException {
		long t1 = System.currentTimeMillis();
		List<StackTraceElement> stackTrace = null;

		Matcher threadDumpStartMatcher = format.getThreadDumpStartPattern().matcher("");
		Matcher threadDumpEndMatcher = format.getThreadDumpEndPattern().matcher("");

		Matcher startMatcher = format.getStartPattern().matcher("");
		Matcher stateMatcher = format.getStatePattern().matcher("");
		Matcher matcher = format.getMethodPattern().matcher("");
		Matcher separatorMatcher = format.getSeparatorPattern().matcher("");

		List<ThreadInfo> threads = new ArrayList<>();

		String line; String previousLine = null;
		boolean inThread = false;
		boolean threadStateFound = false;
		Date date = null;
		while((line=reader.readLine())!=null) {
			threadDumpStartMatcher.reset(line);
			threadDumpEndMatcher.reset(line);
			matcher.reset(line);
			separatorMatcher.reset(line);
			if(threadDumpEndMatcher.find()) {
				
			}
			if(threadDumpStartMatcher.find()) {
				if(previousLine!=null) {
					try {
						date = dateFormat.parse(previousLine);
					} catch (ParseException e) {
						date = null;
					}
				}
			}

			if(!inThread) {
				startMatcher.reset(line);
				if(startMatcher.find()) {
					inThread = true;
					stackTrace = new LinkedList<StackTraceElement>();
				}
			} else {
				if(matcher.find()) {
					stackTrace.add(new StackTraceElement(matcher.group(1), matcher.group(2), "", 0));
				} else if(separatorMatcher.find()) {
					if(stackTrace.size()>0) {
						threads.add(toThread(startMatcher, threadStateFound, stateMatcher, stackTrace, date));
					}
					inThread = false;
					threadStateFound = false;

					startMatcher.reset(line);
					if(startMatcher.find()) {
						inThread = true;
						stackTrace.clear();
					}
				}
			}

			if(inThread && !threadStateFound) {
				stateMatcher.reset(line);
				threadStateFound = stateMatcher.find();
			}
			
			previousLine = line;
		}

		System.out.println("Parser (ms): " + (System.currentTimeMillis()-t1));

		return threads;
	}

	public static ThreadInfo toThread(Matcher startMatcher, boolean stateFound, Matcher stateMatcher, List<StackTraceElement> stackTrace, Date timestamp) {
		Long id;
		try {
			id = Long.decode(startMatcher.group(2));
		} catch (NumberFormatException e) {
			id = (long) startMatcher.group(2).hashCode();
		}
		String name = startMatcher.group(1);

		Thread.State state = Thread.State.RUNNABLE;
		if(stateFound && stateMatcher.groupCount()>0) {
			String stateStr = stateMatcher.group(1);
			try{
				state = Thread.State.valueOf(stateStr);
			} catch (Exception e) {

			}
		}
		
		ThreadInfo thread = new ThreadInfo((StackTraceElement[]) stackTrace.toArray(new StackTraceElement[stackTrace.size()]));
		thread.setId(id);
		thread.setName(name);
		thread.setState(state);
		thread.setTimestamp(timestamp);
		return thread;
	}

	public static Format detectFormat(File file) {
		String line;
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			while((line=reader.readLine())!=null) {
				if(line.startsWith("Full thread dump")) {
					return Format.STANDARD_OUTPUT;
				}
				/*if(line.startsWith("Thread t@")) {
					return Format.JSTACK;
				} */
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return Format.WLS;
	}

	public enum Format {

		STANDARD_OUTPUT(
				Pattern.compile("Full thread dump"),
				Pattern.compile("JNI global references"),
				Pattern.compile("\"(.+?)\".+prio=[0-9]+ tid=(.+?) nid=.+"),
				Pattern.compile("java\\.lang\\.Thread\\.State: (.+?) "),
				Pattern.compile("at (.*)\\.(.+?)\\(.*\\)"), //(((.*):([0-9]*))*
				Pattern.compile("^[ \t]*$")),

		/* JSTACK(
				Pattern.compile("Deadlock Detection"),
				Pattern.compile("(Thread) t@([0-9]+)"),
				Pattern.compile("\\(state = (.+?)\\)"),
				Pattern.compile(" - (.*)\\.(.+?)\\(.*\\)"), //(((.*):([0-9]*))*
				Pattern.compile("^[ \t]*$")), */

		WLS(
				Pattern.compile("Current thread stacks for server"),
				Pattern.compile("Current thread stacks for server"),
				Pattern.compile("\"((.+?))\""),
				Pattern.compile("\".+?\".* ([A-Z_]+)$"),
				Pattern.compile("^[\t ]*(.*)\\.(.+?)\\(.+\\)$"),
				Pattern.compile("\".+?\""));

		private final Pattern threadDumpStartPattern;

		private final Pattern threadDumpEndPattern;

		private final Pattern startPattern;

		private final Pattern statePattern;

		private final Pattern methodPattern;

		private final Pattern separatorPattern;

		private Format(Pattern threadDumpStartPattern,
				Pattern threadDumpEndPattern, Pattern startPattern,
				Pattern statePattern, Pattern methodPattern,
				Pattern separatorPattern) {
			this.threadDumpStartPattern = threadDumpStartPattern;
			this.threadDumpEndPattern = threadDumpEndPattern;
			this.startPattern = startPattern;
			this.statePattern = statePattern;
			this.methodPattern = methodPattern;
			this.separatorPattern = separatorPattern;
		}

		public Pattern getStartPattern() {
			return startPattern;
		}

		public Pattern getMethodPattern() {
			return methodPattern;
		}

		public Pattern getSeparatorPattern() {
			return separatorPattern;
		}

		public Pattern getStatePattern() {
			return statePattern;
		}

		public Pattern getThreadDumpStartPattern() {
			return threadDumpStartPattern;
		}

		public Pattern getThreadDumpEndPattern() {
			return threadDumpEndPattern;
		}
	}

}
