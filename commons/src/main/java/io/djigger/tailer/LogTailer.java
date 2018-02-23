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
package io.djigger.tailer;

import java.io.File;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTailer extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(LogTailer.class);

	volatile boolean running = true;
	
	volatile long updateInterval = 1000;
	
	private final File file;
	
	private long filePointer;
	
	private final LogTailerListener listener;
	
	public LogTailer(File file, boolean startAtFileBegin, LogTailerListener listener) {
		super();
		this.file = file;
		this.listener = listener;
		
		if(startAtFileBegin) {
			filePointer = 0;
		} else {
			filePointer = file.length();
		}
		
		this.start();
	}

	public void run() {
	    try {
	        while (running) {
	            Thread.sleep(updateInterval);
	            long len = file.length();
	            if (len < filePointer) {
	                logger.debug("Log file was reset. Restarting logging from start of file.");
	                filePointer = len;
	            } else if (len > filePointer) {
	                RandomAccessFile raf = new RandomAccessFile(file, "r");
	                raf.seek(filePointer);
	                String line = null;
	                while ((line = raf.readLine()) != null) {
	                	listener.onNewLine(line);
	                }
	                filePointer = raf.getFilePointer();
	                raf.close();
	            }
	            listener.onEndOfFileReached();
	        }
	    } catch (Exception e) {
	        logger.error("Fatal error while reading log file, log tailing has been stopped.", e);
	    }
	}
	
	public static interface LogTailerListener {
		
		void onNewLine(String line);
		
		void onEndOfFileReached();
	}
	
	public void close() {
		running = false;
	}
}
