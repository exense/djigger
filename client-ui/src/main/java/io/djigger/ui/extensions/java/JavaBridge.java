package io.djigger.ui.extensions.java;

import io.djigger.aggregation.Thread.RealNodePathWrapper;
import io.djigger.monitoring.java.model.StackTraceElement;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.ui.model.NodeID;
import io.djigger.ui.model.RealNodePath;

import java.util.ArrayList;
import java.util.List;

public class JavaBridge {

    public static List<RealNodePathWrapper> toRealNodePathList(List<ThreadInfo> entry, boolean includeLineNumbers) {
        List<RealNodePathWrapper> realNodePaths = new ArrayList<>();
        for (ThreadInfo threadInfo : entry) {
            realNodePaths.add(toRealNodePath(threadInfo, includeLineNumbers));
        }
        return realNodePaths;
    }

    public static RealNodePathWrapper toRealNodePath(ThreadInfo entry, boolean includeLineNumbers) {
        RealNodePath path = fromStackTrace(entry.getStackTrace(), includeLineNumbers);
        return new RealNodePathWrapper(path, entry);
    }

    private static RealNodePath fromStackTrace(StackTraceElement[] stacktrace, boolean includeLineNumbers) {
        ArrayList<NodeID> nodeIDs = new ArrayList<NodeID>(stacktrace.length);
        for (int i = stacktrace.length - 1; i >= 0; i--) {
            StackTraceElement el = stacktrace[i];
            StringBuilder nodeIDBuilder = new StringBuilder();
            nodeIDBuilder.append(el.getClassName()).append(".").append(el.getMethodName());
            if (includeLineNumbers) {
                nodeIDBuilder.append("(").append(el.getLineNumber()).append(")");
            }
            NodeID nodeID = NodeID.getInstance(nodeIDBuilder.toString());
            nodeID.setAttachment(el);
            nodeIDs.add(nodeID);
        }
        return RealNodePath.getInstance(nodeIDs);
    }

}
