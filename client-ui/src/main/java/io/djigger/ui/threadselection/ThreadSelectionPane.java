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
package io.djigger.ui.threadselection;

import io.djigger.aggregation.filter.AtomicFilterFactory;
import io.djigger.aggregation.filter.Filter;
import io.djigger.aggregation.filter.FilterFactory;
import io.djigger.aggregation.filter.ParsingException;
import io.djigger.model.Capture;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.store.Store;
import io.djigger.store.filter.IdStoreFilter;
import io.djigger.store.filter.StoreFilter;
import io.djigger.store.filter.TimeStoreFilter;
import io.djigger.ui.Session;
import io.djigger.ui.analyzer.AnalyzerPaneListener;
import io.djigger.ui.analyzer.TreeView;
import io.djigger.ui.common.EnhancedTextField;
import io.djigger.ui.instrumentation.InstrumentationPaneListener;
import io.djigger.ui.instrumentation.InstrumentationStatistics;
import io.djigger.ui.model.AnalysisNode;
import io.djigger.ui.model.RealNodeAggregation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ThreadSelectionPane extends JPanel implements MouseMotionListener, MouseListener, KeyListener, ComponentListener, InstrumentationPaneListener, AnalyzerPaneListener {

	private static final Logger logger = LoggerFactory.getLogger(ThreadSelectionPane.class);
	
	private final Session main;

	private final Store store;

	private ThreadSelectionType threadSelectionType;

	private final Stack<Selection> selectionHistory;

	private final List<ThreadBlock> blocks;

	private Range currentRange;

	private List<Capture> currentCaptures;

	private int currentNumberOfThreadDumps;

	private DraggingType draggingState;
	private int dragInitialX, dragInitialY;
	private Integer dragX, dragY;

	private ThreadSelectionTimeAxis axis;
	
	private final BlocksPane blocksPane;

	private final ThreadSelectionPanePopupMenu popup;

	private final JLabel label;

	private int xOffset = 200;

	private boolean timeBasedAxis;
	
	private final JScrollPane scrollPane;
	
	private final String THREADNAME_FILTER = "Thread name filter (and, or, not operators allowed)";
	
	private EnhancedTextField threadnameFilterTextField;

    @SuppressWarnings("serial")
	public ThreadSelectionPane(Session main) {
		super(new BorderLayout());
		this.main = main;
		this.store = main.getStore();
		this.selectionHistory = new Stack<Selection>();
		this.blocks = new ArrayList<ThreadBlock>();
		this.popup = new ThreadSelectionPanePopupMenu(this);

		threadSelectionType = ThreadSelectionType.ALL;
		selectionHistory.push(new Selection());

		label = new JLabel();
		label.setFont(new Font("Arial", Font.PLAIN,  10));
		label.setOpaque(true);
		label.setBackground(Color.WHITE);
		
		axis = new ThreadSelectionTimeAxis(this);
		blocksPane = new BlocksPane();		
		
		blocksPane.addMouseMotionListener(this);
		blocksPane.addMouseListener(this);
		blocksPane.setFocusable(true);
		blocksPane.addKeyListener(this);
		blocksPane.addComponentListener(this);
		blocksPane.setComponentPopupMenu(popup);
		
		threadnameFilterTextField = new EnhancedTextField(THREADNAME_FILTER);
		threadnameFilterTextField.setToolTipText(THREADNAME_FILTER);
		threadnameFilterTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
		threadnameFilterTextField.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				refresh();
				selectionChanged();
			}
		});
		add(threadnameFilterTextField, BorderLayout.PAGE_START);
		
		JPanel threadTimelinePane = new JPanel(new BorderLayout());
		threadTimelinePane.add(axis, BorderLayout.PAGE_START);
		scrollPane = new JScrollPane(blocksPane, ScrollPaneLayout.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneLayout.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
		threadTimelinePane.add(scrollPane, BorderLayout.CENTER);
		threadTimelinePane.add(label, BorderLayout.PAGE_END);
		add(threadTimelinePane, BorderLayout.CENTER);
		
		refresh();
	}
    
    public void initialize() {
		main.getAnalyzerGroupPane().addListener(this);
		main.getInstrumentationPane().addListener(this);
    }

    public void refresh() {
    	try {
    		buildBlocks();
    	} catch (Exception e) {
    		logger.error("Error while refreshing blocks ",e);
    		throw new RuntimeException(e);
    	}
    	scrollPane.repaint();
    	repaint();
    	//axis.repaint();
    }

    private void buildBlocks() {
    	timeBasedAxis = true;
    	List<ThreadInfo> dumps = store.queryThreadDumps(getFilterForCurrentSelection(false));
    	
    	currentNumberOfThreadDumps = dumps.size();
    	currentCaptures = null;
    	
    	blocks.clear();
    	currentRange = null;
    	if(dumps.size()>0) {
    		// TODO
    		axis.setTimeBasedAxis(timeBasedAxis);
    		
	    	Selection currentSelection = selectionHistory.peek();
	    	if(currentSelection.selectWholeRange) {
	    		ThreadInfo threadDump1 = null;
	    		ThreadInfo threadDump2 = null;
	        	for(ThreadInfo dump:dumps) {
	        		if(timeBasedAxis) {
		    			if(threadDump1 == null || dump.getTimestamp().before(threadDump1.getTimestamp())) {
		    				threadDump1 = dump;
		    			}
		    			if(threadDump2 == null || dump.getTimestamp().after(threadDump2.getTimestamp())) {
		    				threadDump2 = dump;
		    			}
	        		} else {
	        			if(threadDump1 == null || dump.getId()<threadDump1.getId()) {
		    				threadDump1 = dump;
		    			}
		    			if(threadDump2 == null || dump.getId()>threadDump2.getId()) {
		    				threadDump2 = dump;
		    			}
	        		}
	        	}
	        	if(timeBasedAxis) {
	        		currentRange = new Range(threadDump1.getTimestamp().getTime(),threadDump2.getTimestamp().getTime());
	        	} else {
	        		currentRange = new Range(threadDump1.getId(),threadDump2.getId());
	        	}
	    	} else {
	    		currentRange = new Range(currentSelection.start,currentSelection.end);
	    	}

	    	if(timeBasedAxis) {
        		currentCaptures = store.queryCaptures(currentRange.start, currentRange.end);
        	}

	    	Set<Long> selectedIds = getSelectedIds();

	    	HashMap<Long, ThreadBlock> blockMap = new HashMap<Long, ThreadBlock>();
	    	
	    	AggregateDefinition rangeDefinition;
	    	if(timeBasedAxis) {
	    		if(dumps.size()<1000) {
	    			rangeDefinition = new AggregateDefinition(currentRange.start, currentRange.end, dumps.size(),false);
	    		} else {
		    		rangeDefinition = new AggregateDefinition(currentRange.start, currentRange.end, 100, false);
	    		}
	    	} else {
	    		rangeDefinition = new AggregateDefinition(currentRange.start, currentRange.end, dumps.size(),true);
	    	}
	    	
	    	axis.setRangeDefinition(rangeDefinition);
	    	
	    	int c = 0;
    		for(ThreadInfo thread:dumps) {
    			c++;
    			Long threadId = thread.getId();
    			ThreadBlock block = blockMap.get(threadId);
    			if(block==null) {
    				block = new ThreadBlock(this, threadId, thread.getName(), rangeDefinition);
    				blockMap.put(threadId, block);
    			}
    			if(timeBasedAxis) {
    				block.add(thread.getTimestamp().getTime(), thread);
    			} else {
    				// TODO
    			}
	    	}
	    	
	    	System.out.println("ThreadSelectionPane: rendering " + c + " threads.");

	    	//int wHeight = getSize().height;
	    	int wWidth = getSize().width - xOffset;

	    	int numberOfThreads = blockMap.size();

	    	int margin = 4;
	    	int blockHeight = 15;


	    	if(numberOfThreads>0) {
    			int currentY = 0;
    			ArrayList<Long> sortedIds = new ArrayList<Long>(blockMap.size());
    			sortedIds.addAll(blockMap.keySet());
    			Collections.sort(sortedIds);

    			for(Long id:sortedIds) {
    				ThreadBlock block = blockMap.get(id);
    				block.height = blockHeight;
    				block.width = wWidth;
    				block.x = xOffset;
    				block.y = currentY;
    				currentY += blockHeight + margin;
    			}
	    	}

	    	blocks.clear();
	    	blocks.addAll(blockMap.values());

	    	for(ThreadBlock block:blocks) {
	    		if(threadSelectionType == ThreadSelectionType.ALL ||
						(threadSelectionType == ThreadSelectionType.INDIVIDUAL && selectedIds.contains(block.id))) {
					block.selected = true;
				} else {
					block.selected = false;
				}
	    		block.afterBuild();
	    	}

	    	blocksPane.setPreferredSize(new Dimension(0,numberOfThreads*(blockHeight+margin)));
    	}
    }

    private class BlocksPane extends JPanel {
	    protected void paintComponent(Graphics g) {
			super.paintComponent(g);
	
			if(currentRange != null) {
		        Graphics2D g2 = (Graphics2D) g.create();
		        setBackground(Color.WHITE);
		        g2.setFont(new Font("Arial", Font.PLAIN,  10));
	
		        for(ThreadBlock block:blocks) {
		        	block.draw(g2, xOffset);
		        }
	
		        if(draggingState == DraggingType.ZOOM_IN) {
		        	g2.setColor(new Color(0,100,200,50));
		        	g2.fillRect(Math.min(dragInitialX,dragX), 0,
		        			Math.abs(dragX-dragInitialX), getSize().height);
		        	if(timeBasedAxis) {
			        	SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.S");
		        		String label = (format.format(new Date(xToRange(dragX)))).toString();
		        		g2.setColor(new Color(0,100,200));
		        		g2.drawChars(label.toCharArray(), 0, label.length(), dragX, dragY);
		        	}
	
		        } else if (draggingState == DraggingType.RESIZE) {
		        	g2.setColor(Color.GRAY);
		        	g2.drawLine(xOffset, 0, xOffset, getSize().height);
		        }
	
		        g2.setColor(Color.BLACK);
		        g2.drawLine(xOffset, 0, xOffset, getSize().height);
	
		        g2.dispose();
			}
	    }
    }

	protected void unselectAll() {
		threadSelectionType = ThreadSelectionType.NONE;
		for(ThreadBlock block:blocks) {
			block.selected = false;
		}
		repaint();
		selectionChanged();
	}

	protected void selectAll() {
		threadSelectionType = ThreadSelectionType.ALL;
		for(ThreadBlock block:blocks) {
			block.selected = true;
		}
		repaint();
		selectionChanged();
	}

	protected boolean isMouseOverBlock() {
		for(ThreadBlock block:blocks) {
			if(block.mouseOver) {
				return true;
			}
		}
		return false;
	}

	public void selectThisOnly() {
		threadSelectionType = ThreadSelectionType.INDIVIDUAL;
		for(ThreadBlock block:blocks) {
			if(block.mouseOver) {
				block.selected = true;
			} else {
				block.selected = false;
			}
		}
		repaint();
		selectionChanged();
	}

    private class Selection {

    	private long start;

    	private long end;

    	private boolean selectWholeRange;

		protected Selection() {
			super();
			selectWholeRange = true;
		}

		protected Selection(long start, long end) {
			super();
			this.start = start;
			this.end = end;
			selectWholeRange = false;
		}
    }

	@Override
	public void mouseDragged(MouseEvent e) {
		Integer dragPreviousX = dragX;
		Integer dragPreviousY = dragY;

		if(dragPreviousX != null && dragPreviousY !=null) {
			if(draggingState == null) {
				if(e.getX()>xOffset+20 && Math.abs((e.getX()-dragPreviousX))>2*Math.abs((e.getY()-dragPreviousY))) {
					draggingState = DraggingType.ZOOM_IN;
				} else if(e.getX()>xOffset+20 && Math.abs((e.getX()-dragPreviousX))<3*Math.abs((e.getY()-dragPreviousY))){
					draggingState = DraggingType.ZOOM_OUT;
				}
			}
		}

		if(draggingState == DraggingType.RESIZE) {
			xOffset = e.getX();
		}
		repaint();

		dragX = e.getX();
		dragY = e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		boolean repaint = false;
		ThreadBlock mouseOverBlock = getBlock(e.getX(), e.getY());
		if(mouseOverBlock!=null) {
			for(ThreadBlock block:blocks) {
				if(block == mouseOverBlock && !block.mouseOver) {
					block.mouseOver = true;
					repaint = true;
				} else if(block.mouseOver && block != mouseOverBlock) {
					block.mouseOver = false;
					repaint = true;
				}
			}
		}
		if (e.getX() > xOffset - 10 && e.getX() < xOffset + 10) {
			setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
		} else {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		if(mouseOverBlock != null) {
			String captureInfos = "";
			if(currentCaptures!=null) {
				long time = xToRange(e.getX());
				Capture capture = getCapture(time);
				if(capture!=null) {
					captureInfos = ", Sampling interval (ms): " + capture.getSamplingInterval();
				}
			}
			String labelStr = mouseOverBlock.label + " [" + currentNumberOfThreadDumps + " thread dumps" + captureInfos + "]";
			if(!label.getText().equals(labelStr)) {
				repaint = true;
			}
			label.setText(labelStr);
		} else {
			label.setText(" ");
		}

		if(repaint) {
			repaint();
		}
	}

	private ThreadBlock getBlock(int x, int y) {
		for(ThreadBlock block:blocks) {
			if(y>=block.y && y<=block.y+block.height) {
				return block;
			}
		}
		return null;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		requestFocusInWindow();
		if (e.getButton()==e.BUTTON1) {
			ThreadBlock mouseOverBlock = getBlock(e.getX(), e.getY());
			if(mouseOverBlock != null) {
				if(threadSelectionType == ThreadSelectionType.ALL) {
					for(ThreadBlock block:blocks) {
						block.selected = false;
					}
				}
				threadSelectionType = ThreadSelectionType.INDIVIDUAL;
				mouseOverBlock.selected = !mouseOverBlock.selected;
				repaint();
				if(!e.isControlDown()) {
					selectionChanged();
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		dragInitialX = e.getX();
		dragInitialY = e.getY();
		if(draggingState == null) {
			if(e.getX()>xOffset-20 && e.getX()<xOffset+20) {
				draggingState = DraggingType.RESIZE;
			}
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(draggingState == DraggingType.RESIZE) {
			refresh();
		} else if (draggingState == DraggingType.ZOOM_IN) {
			if(Math.abs(dragX-dragInitialX)>10) {
				zoomIn();
			}
		} else if (draggingState == DraggingType.ZOOM_OUT) {
			zoomOut();
		}

		draggingState = null;
		dragX = null;
		dragY = null;
	}

	long xToRange(int x) {
		return (long) (currentRange.start+(currentRange.end-currentRange.start+1)*(1.0*(x-xOffset))/(getSize().width-xOffset));
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {
		if(!popup.isVisible()) {
			for(ThreadBlock block:blocks) {
				block.mouseOver = false;
			}
			repaint();
		}

	}

	private Set<Long> getSelectedIds() {
		Set<Long> selectedIds = new HashSet<Long>();
		for(ThreadBlock block:blocks) {
			if(block.selected) {
				selectedIds.add(block.id);
			}
		}
		return selectedIds;
	}

	private StoreFilter getFilterForCurrentSelection(boolean filterThreads) {
		StoreFilter filter;
		Selection currentSelection = selectionHistory.peek();

		Set<Long> threadIdsFilter;
		if(filterThreads) {
			if(threadSelectionType==ThreadSelectionType.ALL) {
				threadIdsFilter = null;
			} else {
				threadIdsFilter = getSelectedIds();
			}
		} else {
			threadIdsFilter = null;
		}

		if(timeBasedAxis) {
			Filter<ThreadInfo> threadnameFilter = parseThreadnameFilter();
			if(currentSelection.selectWholeRange) {
				filter = new TimeStoreFilter(threadnameFilter, threadIdsFilter, null, null);
			} else {
				filter = new TimeStoreFilter(threadnameFilter, threadIdsFilter, currentSelection.start, currentSelection.end);
			}
		} else {
			if(currentSelection.selectWholeRange) {
				filter = new IdStoreFilter(threadIdsFilter, null, null);
			} else {
				filter = new IdStoreFilter(threadIdsFilter, currentSelection.start, currentSelection.end);
			}
		}
    	return filter;
	}
	
	private Filter<ThreadInfo> parseThreadnameFilter() {
		Filter<ThreadInfo> complexFilter = null;
		String filter = threadnameFilterTextField.getText();
		if(filter!=null) {
			FilterFactory<ThreadInfo> factory = new FilterFactory<>(new AtomicFilterFactory<ThreadInfo>() {
				@Override
				public Filter<ThreadInfo> createFilter(final String expression) {
					return new Filter<ThreadInfo>() {
						@Override
						public boolean isValid(ThreadInfo input) {
							return input.getName().contains(expression);
						}
						
					};
				}
			});
			try {
				complexFilter = factory.getCompositeFilter(filter);
			} catch (ParsingException e) {
				JOptionPane.showMessageDialog(this,	e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		return complexFilter;
	}

	private void selectionChanged() {
		main.onThreadSelection(getFilterForCurrentSelection(true));
	}

	private void zoomIn() {
		long t1 = xToRange(Math.min(dragX,dragInitialX))+1;
		long t2 = xToRange(Math.max(dragX,dragInitialX))-1;
		Selection selection = new Selection(t1,t2);

		selectionHistory.push(selection);

    	List<ThreadInfo> dumps = store.queryThreadDumps(getFilterForCurrentSelection(false));
		if(dumps == null || dumps.size() == 0) {
			JOptionPane.showMessageDialog(this,
				    "No sample available in the selected timeslot. Please select a larger timeslot.",
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
			selectionHistory.pop();
		} else {
			refresh();
			selectionChanged();
		}
	}

	protected void zoomOut() {
		if(selectionHistory.size()>1) {
			selectionHistory.pop();
			refresh();
			selectionChanged();
		}
	}

	private Capture getCapture(long time) {
		for(Capture capture:currentCaptures) {
			if(time>capture.getStart() && (capture.getEnd()==null || time<capture.getEnd())) {
				return capture;
			}
		}
		return null;
	}

	public class Range {
		final long start;

		final long end;

		protected Range(long start, long end) {
			super();
			this.start = start;
			this.end = end;
		}
	}

	public ThreadSelectionTimeAxis getAxis() {
		return axis;
	}

	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_CONTROL) {
			selectionChanged();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void componentResized(ComponentEvent e) {
		refresh();
	}

	@Override
	public void componentMoved(ComponentEvent e) {}

	@Override
	public void componentShown(ComponentEvent e) {}

	@Override
	public void componentHidden(ComponentEvent e) {}

	public JLabel getLabel() {
		return label;
	}

	private enum DraggingType {
		ZOOM_IN,

		ZOOM_OUT,

		RESIZE;
	}

	private enum ThreadSelectionType {
		ALL,

		NONE,

		INDIVIDUAL;
	}

	@Override
	public void onSelection(Set<InstrumentSubscription> selectedSubscriptions) {
    	for(ThreadBlock block:blocks) {
			block.selectedInInstrumentationPane = false;
    	}

    	Set<Long> selectedInAnalyzerPane = getThreadsByInstrumentSubscription(selectedSubscriptions);
    	for(ThreadBlock block:blocks) {
    		if(selectedInAnalyzerPane.contains(block.id)) {
    			block.selectedInInstrumentationPane = true;
    		}
    	}

    	repaint();
	}

	private Set<Long> getAnalyzerPaneThreads() {
		HashSet<Long> result = new HashSet<Long>();
		Component _pane = main.getAnalyzerGroupPane().getCurrentTab();
		if(_pane!=null && _pane instanceof TreeView) {
			TreeView pane = (TreeView) _pane;
			AnalysisNode selectedNode = pane.getSelectedNode();
			if(selectedNode!=null) {
				for(RealNodeAggregation aggregation:selectedNode.getAggregations()) {
					for(io.djigger.aggregation.Thread thread:aggregation.getAggregation().getSamples()) {
						result.add(thread.getId());
					}
				}
				/*
				RealNodePath path = selectedNode.getPath();
				List<ThreadDump> dumps = store.getThreadDumps();
				for(ThreadDump dump:dumps) {
					for(ThreadSnapshot stacktrace:dump.getStackTraces()) {
						if(stacktrace.getPath().containsPath(path)!=-1) {
						}
					}
				}
				*/
			}
		}
		return result;
	}

	private Set<Long> getThreadsByInstrumentSubscription(Set<InstrumentSubscription> subscriptions) {
		HashSet<Long> result = new HashSet<Long>();
		if(subscriptions!=null) {
			for(InstrumentSubscription subscription:subscriptions) {
				InstrumentationStatistics stats = main.getStatisticsCache().getInstrumentationStatistics(subscription);
				result.addAll(stats.getThreadIds());
			}
		}
		return result;
	}


	@Override
	public void onSelection(AnalysisNode selectedNode) {
		Set<Long> selectedInAnalyzerPane = getAnalyzerPaneThreads();

    	for(ThreadBlock block:blocks) {
			block.selectedInAnalyzerPane = false;
    	}

    	for(ThreadBlock block:blocks) {
    		if(selectedInAnalyzerPane.contains(block.id)) {
    			block.selectedInAnalyzerPane = true;
    		}
    	}

    	repaint();
	}

	public Range getCurrentRange() {
		return currentRange;
	}

	public int getxOffset() {
		return xOffset;
	}
}
