package io.djigger.samplig;

import io.djigger.aggregation.Thread;
import io.djigger.aggregation.Thread.RealNodePathWrapper;
import io.djigger.monitoring.java.model.GlobalThreadId;
import io.djigger.monitoring.java.model.ThreadInfo;

import java.util.*;

public class SequenceGenerator {

    public List<Thread> buildThreads(List<RealNodePathWrapper> threadInfos) {
        Map<GlobalThreadId, List<RealNodePathWrapper>> threadInfoMap = groupByGlobalThreadId(threadInfos);
        return createThreadList(threadInfoMap);
    }

    private List<Thread> createThreadList(Map<GlobalThreadId, List<RealNodePathWrapper>> threadInfoMap) {
        List<Thread> threads = new ArrayList<>();
        for (GlobalThreadId globalThreadId : threadInfoMap.keySet()) {
            List<RealNodePathWrapper> entry = threadInfoMap.get(globalThreadId);
            sortThreadInfosByTime(entry);
            Thread thread = transformThreadInfoListToThread(globalThreadId, entry);
            threads.add(thread);
        }
        return threads;
    }

    private Thread transformThreadInfoListToThread(GlobalThreadId globalThreadId, List<RealNodePathWrapper> realNodePaths) {
        Thread thread = new Thread(globalThreadId, realNodePaths);
        return thread;
    }

    private void sortThreadInfosByTime(List<RealNodePathWrapper> entry) {
        Collections.sort(entry, new Comparator<RealNodePathWrapper>() {
            @Override
            public int compare(RealNodePathWrapper arg0, RealNodePathWrapper arg1) {
                return Long.compare(arg0.getThreadInfo().getTimestamp(), arg1.getThreadInfo().getTimestamp());
            }
        });
    }

    private Map<GlobalThreadId, List<RealNodePathWrapper>> groupByGlobalThreadId(List<RealNodePathWrapper> paths) {
        Map<GlobalThreadId, List<RealNodePathWrapper>> pathMap = new HashMap<>();

        for (RealNodePathWrapper path : paths) {
            ThreadInfo threadInfo = path.getThreadInfo();
            GlobalThreadId globalThreadId = threadInfo.getGlobalId();
			List<RealNodePathWrapper> entry = pathMap.get(globalThreadId);
            if (entry == null) {
                entry = new ArrayList<>();
                pathMap.put(globalThreadId, entry);
            }
            entry.add(path);
        }
        return pathMap;
    }
}
