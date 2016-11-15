package io.djigger.samplig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.djigger.aggregation.Thread;
import io.djigger.aggregation.Thread.RealNodePathWrapper;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.ui.extensions.java.JavaBridge;

public class SequenceGenerator {

	public List<Thread> queryThreads(List<ThreadInfo> threadInfos) {
		Map<Long, List<ThreadInfo>> threadInfoMap = groupByThreadId(threadInfos);
		return createThreadList(threadInfoMap);
	}
	
	private List<Thread> createThreadList(Map<Long, List<ThreadInfo>> threadInfoMap) {
		List<Thread> threads = new ArrayList<>();
		for(Long threadId:threadInfoMap.keySet()) {
			List<ThreadInfo> entry = threadInfoMap.get(threadId);
			sortThreadInfosByTime(entry);
			Thread thread = transformThreadInfoListToThread(threadId, entry);
			threads.add(thread);
		}
		return threads;
	}

	private Thread transformThreadInfoListToThread(long threadId, List<ThreadInfo> entry) {
		List<RealNodePathWrapper> realNodePaths = JavaBridge.toRealNodePathList(entry, false);
		Thread thread = new Thread(threadId, realNodePaths);
		return thread;
	}
	
	private void sortThreadInfosByTime(List<ThreadInfo> entry) {
		Collections.sort(entry, new Comparator<ThreadInfo>() {
			@Override
			public int compare(ThreadInfo arg0, ThreadInfo arg1) {
				return Long.compare(arg0.getTimestamp(), arg1.getTimestamp());
			}
		});
	}

	private Map<Long, List<ThreadInfo>> groupByThreadId(List<ThreadInfo> threadInfos) {
		Map<Long, List<ThreadInfo>> threadInfoMap = new HashMap<>();
		
		for(ThreadInfo threadInfo:threadInfos) {
			List<ThreadInfo> entry = threadInfoMap.get(threadInfo.getId());
			if(entry==null) {
				entry = new ArrayList<>();
				threadInfoMap.put(threadInfo.getId(), entry);
			}
			entry.add(threadInfo);
		}
		return threadInfoMap;
	}
}
