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

package io.djigger.ui.threadselection;

import io.djigger.monitoring.java.model.ThreadInfo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.lang.Thread.State;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


class ThreadBlock {

	private final ThreadSelectionPane threadSelectionGraphPane;

	final Long id;

	private final String name;

	String label;

	int x;

	int y;

	int width;

	int height;

	boolean mouseOver = false;

	boolean selected = false;

	boolean selectedInAnalyzerPane = false;

	boolean selectedInInstrumentationPane = false;

	private final Map<Thread.State, Integer> states = new HashMap<Thread.State, Integer>();
	
	private final AggregateDefinition rangeDefinition;
		
	private final Aggregate[] rangeTable;

	private int sampleCount = 0;

	ThreadBlock(ThreadSelectionPane threadSelectionGraphPane, Long id, String name, AggregateDefinition rangeDefinition) {
		super();
		this.threadSelectionGraphPane = threadSelectionGraphPane;
		this.id = id;
		this.name = name;
		this.label = name;
		
		this.rangeDefinition = rangeDefinition;
		
		rangeTable = new Aggregate[rangeDefinition.getRangeNumber()];
		
		for(int i=0;i<rangeDefinition.getRangeNumber();i++) {
			rangeTable[i]=new Aggregate();
		}
	}

	public void draw(Graphics2D graph, int xOffset) {

		int lastX=0;
		for(int i=0;i<rangeTable.length;i++) {
			Aggregate range = rangeTable[i];
			Color color;
			State averageState = range.getAverageState();
			if(averageState == Thread.State.RUNNABLE) {
				color = Color.GREEN;
			} else if (averageState == Thread.State.BLOCKED) {
				color = Color.RED;
			} else if (averageState == Thread.State.WAITING || averageState == Thread.State.TIMED_WAITING) {
				color = Color.ORANGE;
			} else {
				color = Color.LIGHT_GRAY;
			}

			graph.setColor(color);
			int x1 = xOffset + lastX;
			lastX = (int) ((i+1)*1.0/rangeTable.length*width);
			int x2 = xOffset + lastX;
			graph.fillRect(x1, y, x2-x1, height);
		}

		if(mouseOver) {
			graph.setColor(new Color(0,100,200,50));
			graph.fillRect(0, y, width + xOffset, height);
		}

		if(selected) {
			graph.setColor(new Color(0,100,200,30));
			graph.fillRect(0, y, width + xOffset, height);

			graph.setColor((new Color(0,100,200,120)));
			graph.drawRect(0, y, width + xOffset, height);
		}

		if(selectedInAnalyzerPane) {
			graph.setColor(Color.RED);
			graph.drawRect(0, y, width + xOffset, height);
		}

		if(selectedInInstrumentationPane) {
			graph.setColor(Color.MAGENTA);
			graph.drawRect(0, y, width + xOffset, height);
		}

		if(height>10 && width>(graph.getFont().getStringBounds(name, graph.getFontRenderContext()).getWidth()+5)) {
			graph.setColor(Color.BLACK);
			graph.drawChars(name.toCharArray(), 0, name.length(), 5, y+11);
		}

	}
	
	public void add(long time, ThreadInfo thread) {
		int rangeId = rangeDefinition.getRangeId(time);
		//new Date(time)
		rangeTable[rangeId].add(thread);
	}
	
	public void afterBuild() {
		calculateLabel();
		
		for(Aggregate range:rangeTable) {
			range.calculate();
		}
	}

	private void calculateLabel() {

		StringBuilder str = new StringBuilder();
		str.append("[");
		for(Entry<Thread.State, Integer> entry:states.entrySet()) {
			str.append(entry.getKey());
			str.append(" ");
			try {
				BigDecimal percentage = new BigDecimal((100.0*entry.getValue())/sampleCount);
				percentage = percentage.setScale(0,RoundingMode.HALF_EVEN);
				str.append(percentage);
			} catch (Exception e) {
				e.printStackTrace();
			}
			str.append("%  ");
		}
		str.append("]");
		label = name + str.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getOuterType().hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThreadBlock other = (ThreadBlock) obj;
		if (!getOuterType().equals(other.getOuterType()))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	private ThreadSelectionPane getOuterType() {
		return this.threadSelectionGraphPane;
	}
}
