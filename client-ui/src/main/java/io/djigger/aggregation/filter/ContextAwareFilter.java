package io.djigger.aggregation.filter;

public interface ContextAwareFilter {

	public void startIteration();
	
	public void stopIteration();
}
