package io.djigger.samplig;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.bson.types.ObjectId;

import io.djigger.aggregation.Thread;
import io.djigger.aggregation.Thread.RealNodePathWrapper;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.NameBasedSubscription;
import io.djigger.monitoring.java.model.StackTraceElement;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.ui.model.NodeID;
import io.djigger.ui.model.PseudoInstrumentationEvent;

public class PseudoInstrumentationEventsGenerator {
	
	private Set<InstrumentSubscription> subscriptions;
	
	private Listener listener;
	
	public PseudoInstrumentationEventsGenerator(Listener listener, Set<InstrumentSubscription> subscriptions) {
		super();
		this.listener = listener;
		this.subscriptions = subscriptions;
	}
	
	public PseudoInstrumentationEventsGenerator(Listener listener) {
		super();
		this.listener = listener;
		this.subscriptions = null;
	}
	
	public void generateApproximatedEvents(List<Thread> threads) {
		for(Thread thread:threads) {
			List<NodeID> previousPath = null;
			ThreadInfo previousThreadInfo = null;
			for(RealNodePathWrapper realNodePath:thread.getRealNodePathSequence()) {
				List<NodeID> currentPath = realNodePath.getPath().getFullPath();
				ThreadInfo threadInfo = realNodePath.getThreadInfo();
				
				for(int level=0; level<Math.max(currentPath.size(),(previousPath!=null?previousPath.size():0)); level++) {
					NodeID currentNodeID = currentPath.size()>level?currentPath.get(level):null;					
					NodeID previousNodeID = (previousPath!=null&&previousPath.size()>level)?previousPath.get(level):null;

					if(currentNodeID!=null) {
						if(!currentNodeID.equals(previousNodeID)) {
							if(previousPath!=null) {
								leaveBranch(previousThreadInfo, previousPath, level);
							}
							enterBranch(threadInfo, currentPath, level);
							break;
						}
					} else {
						if(previousPath!=null) {
							leaveBranch(previousThreadInfo, previousPath, level);
							break;
						}
					}
				}
				
				previousPath = currentPath;
				previousThreadInfo = threadInfo;
			}
			
			leaveBranch(previousThreadInfo);
		}		
	}
	
	Stack<PseudoInstrumentationEvent> stack = new Stack<>();
	
	private void enterBranch(ThreadInfo threadInfo, List<NodeID> branch, int level) {
		
		for(int i=level;i<branch.size();i++) {
			NodeID nodeID = branch.get(i);
			
			Object attachment = nodeID.getAttachment();
			if(attachment!=null) {
				if(attachment instanceof StackTraceElement) {
					StackTraceElement element = (StackTraceElement) attachment;
					PseudoInstrumentationEvent event = new PseudoInstrumentationEvent(element.getClassName(), element.getMethodName());
					event.setId(new ObjectId());
					event.setStart(threadInfo.getTimestamp());
					event.setThreadID(threadInfo.getId());
					
					if(event.getTransactionID()==null && subscriptions!=null) {
						for(InstrumentSubscription subscription:subscriptions) {
							if(subscription instanceof NameBasedSubscription) {
								NameBasedSubscription subscription_ = ((NameBasedSubscription)subscription);
								if(subscription_.isRelatedToClass(element.getClassName()) &&
										subscription_.isRelatedToMethod(element.getMethodName())) {
									event.setTransactionID(UUID.randomUUID());
									break;
								}
							}
						}
					}
				
					if(!stack.isEmpty()) {
						PseudoInstrumentationEvent parent = stack.peek();
						if(parent!=null) {
							event.setParentID(parent.getId());
							if(parent.getTransactionID()!=null) {
								event.setTransactionID(parent.getTransactionID());
							}
						}
					}
					
					ThreadInfo clone = new ThreadInfo(Arrays.copyOfRange(threadInfo.getStackTrace(), threadInfo.getStackTrace().length-i-1, threadInfo.getStackTrace().length),threadInfo.getId(),
							threadInfo.getTimestamp());
					
					event.setThreadInfo(clone);
					stack.push(event);
				} else {
					throw new RuntimeException("Invalid argument. Attachment of node id "+nodeID.toString()+" is not an instance of "+StackTraceElement.class.getName() + " but of "+attachment.getClass().getName());
				}
			} else {
				throw new RuntimeException("Attachment of node id "+nodeID.toString()+" is null.");
			}	
			
		}
	}
	
	private void leaveBranch(ThreadInfo threadInfo, List<NodeID> branch, int level) {
		for(int i=branch.size()-1;i>=level;i--) {
			
			PseudoInstrumentationEvent event = stack.pop();
//			The following lines can be used to debug a suspect behaviour. Due to the performance overhead these lines are commented out
//			NodeID node = branch.get(i);
//			if(!event.getClassname().equals(((StackTraceElement)node.getAttachment()).getClassName())||
//					!event.getMethodname().equals(((StackTraceElement)node.getAttachment()).getMethodName())) {
//				throw new RuntimeException("Unexepected error while generating pseudo events");
//			}
			closeEventAndCallListener(threadInfo, event);
		}
	}
	
	private void leaveBranch(ThreadInfo threadInfo) {
		while(stack.size()>0) {
			PseudoInstrumentationEvent event = stack.pop();
			closeEventAndCallListener(threadInfo, event);
		}
	}

	private void closeEventAndCallListener(ThreadInfo threadInfo, PseudoInstrumentationEvent event) {
		event.setDuration(1000000*(threadInfo.getTimestamp()-event.getStart()));
		listener.onPseudoInstrumentationEvent(event);
	}
	
	public interface Listener {
		
		public void onPseudoInstrumentationEvent(PseudoInstrumentationEvent event);
	}

}
