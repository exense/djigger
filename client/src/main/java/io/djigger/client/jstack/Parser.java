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
package io.djigger.client.jstack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.djigger.monitoring.java.model.GlobalThreadId;
import io.djigger.monitoring.java.model.StackTraceElement;
import io.djigger.monitoring.java.model.ThreadInfo;


public class Parser {

    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private final Format format;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Matcher threadDumpStartMatcher;
    Matcher threadDumpEndMatcher;

    Matcher startMatcher;
    Matcher stateMatcher;
    Matcher matcher;
    Matcher separatorMatcher;

    List<StackTraceElement> stackTrace = null;

    String previousLine = null;

    Date date = null;

    boolean inThread = false;
    boolean threadStateFound = false;

    ParserEventListener listener;

    public Parser(Format format, ParserEventListener listener) {
        super();
        this.format = format;
        this.listener = listener;

        threadDumpStartMatcher = format.getThreadDumpStartPattern().matcher("");
        threadDumpEndMatcher = format.getThreadDumpEndPattern().matcher("");

        startMatcher = format.getStartPattern().matcher("");
        stateMatcher = format.getStatePattern().matcher("");
        matcher = format.getMethodPattern().matcher("");
        separatorMatcher = format.getSeparatorPattern().matcher("");
    }

    public void consumeLine(String line) throws IOException {

        threadDumpStartMatcher.reset(line);
        threadDumpEndMatcher.reset(line);
        matcher.reset(line);
        separatorMatcher.reset(line);
        if (threadDumpEndMatcher.find()) {

        }
        if (threadDumpStartMatcher.find()) {
            if (previousLine != null) {
                try {
                    date = dateFormat.parse(previousLine);
                } catch (ParseException e) {
                    date = null;
                }
            }
        }

        if (!inThread) {
            startMatcher.reset(line);
            if (startMatcher.find()) {
                inThread = true;
                stackTrace = new LinkedList<StackTraceElement>();
            }
        } else {
            if (matcher.find()) {
                final int lineNumber = Optional.ofNullable(matcher.group(3)).map(Integer::parseInt).orElse(0);
                stackTrace.add(new StackTraceElement(matcher.group(1), matcher.group(2), "", lineNumber));
            } else if (separatorMatcher.find()) {
                if (stackTrace.size() > 0) {
                    listener.onThreadParsed(toThread(startMatcher, threadStateFound, stateMatcher, stackTrace, date));
                }
                inThread = false;
                threadStateFound = false;

                startMatcher.reset(line);
                if (startMatcher.find()) {
                    inThread = true;
                    stackTrace.clear();
                }
            }
        }

        if (inThread && !threadStateFound) {
            stateMatcher.reset(line);
            threadStateFound = stateMatcher.find();
        }

        previousLine = line;
    }

    public interface ParserEventListener {

        public void onThreadParsed(ThreadInfo thread);
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
        if (stateFound && stateMatcher.groupCount() > 0) {
            String stateStr = stateMatcher.group(1);
            try {
                state = Thread.State.valueOf(stateStr);
            } catch (Exception e) {

            }
        }

        ThreadInfo thread = new ThreadInfo((StackTraceElement[]) stackTrace.toArray(new StackTraceElement[stackTrace.size()]));
        thread.setGlobalId(new GlobalThreadId(null, id));
        thread.setName(name);
        thread.setState(state);
        thread.setTimestamp(timestamp.getTime());
        return thread;
    }

    public static Format detectFormat(File file) {
        String line;
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Full thread dump")) {
                    return Format.STANDARD_OUTPUT;
                }
				/*if(line.startsWith("Thread t@")) {
					return Format.JSTACK;
				} */
            }
        } catch (IOException e) {
            logger.error("IO error while detecting format of file " + file, e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
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
            Pattern.compile("at (.*)\\.(.+?)\\(.*?(?::(\\d+))?\\)"),
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
            Pattern.compile("^[\t ]*(.*)\\.(.+?)\\(.*?(?::(\\d+))?\\)$"),
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
