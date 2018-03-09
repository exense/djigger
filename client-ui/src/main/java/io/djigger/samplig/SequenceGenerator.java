package io.djigger.samplig;

import io.djigger.aggregation.Thread;
import io.djigger.aggregation.Thread.RealNodePathWrapper;

import java.util.*;

public class SequenceGenerator {

    public List<Thread> buildThreads(List<RealNodePathWrapper> threadInfos) {
        Map<Long, List<RealNodePathWrapper>> threadInfoMap = groupByThreadId(threadInfos);
        return createThreadList(threadInfoMap);
    }

    private List<Thread> createThreadList(Map<Long, List<RealNodePathWrapper>> threadInfoMap) {
        List<Thread> threads = new ArrayList<>();
        for (Long threadId : threadInfoMap.keySet()) {
            List<RealNodePathWrapper> entry = threadInfoMap.get(threadId);
            sortThreadInfosByTime(entry);
            Thread thread = transformThreadInfoListToThread(threadId, entry);
            threads.add(thread);
        }
        return threads;
    }

    private Thread transformThreadInfoListToThread(long threadId, List<RealNodePathWrapper> realNodePaths) {
        Thread thread = new Thread(threadId, realNodePaths);
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

    private Map<Long, List<RealNodePathWrapper>> groupByThreadId(List<RealNodePathWrapper> paths) {
        Map<Long, List<RealNodePathWrapper>> pathMap = new HashMap<>();

        for (RealNodePathWrapper path : paths) {
            List<RealNodePathWrapper> entry = pathMap.get(path.getThreadInfo().getId());
            if (entry == null) {
                entry = new ArrayList<>();
                pathMap.put(path.getThreadInfo().getId(), entry);
            }
            entry.add(path);
        }
        return pathMap;
    }
}
