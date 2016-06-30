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
package io.djigger.sequencetree;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Stack;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.ql.Filter;
import io.djigger.ql.FilterFactory;
import io.djigger.ql.OQLFilterBuilder;
import io.djigger.ui.Session;
import io.djigger.ui.Session.SessionType;
import io.djigger.ui.analyzer.AnalyzerGroupPane;
import io.djigger.ui.analyzer.TreeType;
import io.djigger.ui.common.EnhancedTextField;
import io.djigger.ui.common.NodePresentationHelper;


public abstract class SequenceTreePane extends JPanel implements ActionListener {

	private static final long serialVersionUID = -8452799701138552693L;

	protected final Session session;

	protected final AnalyzerGroupPane parent;
	
	protected final SequenceTreeService sequenceTreeService;

	private Filter<InstrumentationEvent> nodeFilter;

	protected SequenceTreeNode workNode;

	private EnhancedTextField filterTextField;

	private EnhancedTextField excludeTextField;

	protected final TreeType treeType;

	protected JPanel contentPanel;

	private final String STACKTRACE_FILTER = "Stacktrace filter (and, or, not operators allowed)";
	private final String NODE_FILTER = "Node filter (and, or, not operators allowed)";

	protected SequenceTreePane(AnalyzerGroupPane parent, TreeType treeType, UUID transactionID) {
		super(new BorderLayout());

		this.parent = parent;
		this.session = parent.getMain();
		this.treeType = treeType;
		
		this.sequenceTreeService = new SequenceTreeService(session.getStore());

		JPanel filterPanel = new JPanel(new GridLayout(0,1));

		filterTextField = new EnhancedTextField(STACKTRACE_FILTER);
		filterTextField.setToolTipText(STACKTRACE_FILTER);
		filterTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
		filterTextField.addActionListener(this);
		filterPanel.add(filterTextField);

		excludeTextField = new EnhancedTextField(NODE_FILTER);
		excludeTextField.setToolTipText(NODE_FILTER);
		excludeTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
		excludeTextField.addActionListener(this);
		filterPanel.add(excludeTextField);

		contentPanel = new JPanel();

		add(filterPanel, BorderLayout.PAGE_START);
		add(contentPanel, BorderLayout.CENTER);

		if(session.getSessionType()==SessionType.STORE) {
			Iterator<InstrumentationEvent> events = session.getStoreClient().getInstrumentationAccessor().getByTransactionId(transactionID);
			while(events.hasNext()) {
				session.getStore().getInstrumentationEvents().add(events.next());
			};
		}
		sequenceTreeService.load(transactionID, false);			
		transform();
	}

	public void refresh() {
		transform();
		refreshDisplay();
	}

	public void appendCurrentSelectionToBranchFilter(boolean negate) {
		appendFilter(negate, filterTextField);		
		refresh();
	}

	public void appendCurrentSelectionToNodeFilter(boolean negate) {
		appendFilter(negate, excludeTextField);
		refresh();
	}

	private void appendFilter(boolean negate, EnhancedTextField textField) {
		StringBuilder filter = new StringBuilder();
		String currentFilter = textField.getText();
		if(currentFilter!=null && currentFilter.trim().length()>0) {
			filter.append(currentFilter).append(" and ");			
		}
		if(negate) {
			filter.append("not ");
		}
		filter.append(getPresentationHelper().toString(getSelectedNode()));
		textField.setText(filter.toString().trim());
	}
	
	public void drillDown() {
		SequenceTreeNode node = getSelectedNode();
		if(node!=null) {
			parent.addDrilldownPane(node.getEvent().getId());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		refresh();
	}

	public String getStacktraceFilter() {
		return filterTextField.getText();
	}

	public void setStacktraceFilter(String filter) {
		filterTextField.setText(filter);
	}

	public String getNodeFilter() {
		return excludeTextField.getText();
	}

	public void setNodeFilter(String filter) {
		excludeTextField.setText(filter);
	}

	private Filter<Stack<InstrumentationEvent>> parseBranchFilter() {
		Filter<Stack<InstrumentationEvent>> complexFilter = null;
		String filter = getStacktraceFilter();
		if(filter!=null) {
			complexFilter = parseBranchFilter(filter);
		}
		return complexFilter;
	}

	private Filter<InstrumentationEvent> parseNodeFilter() {
		String excludeFilter = getNodeFilter();
		if(excludeFilter!=null) {
			nodeFilter = parseFilter(excludeFilter);
		} else {
			nodeFilter = null;
		}
		return nodeFilter;
	}

	private Filter<InstrumentationEvent> parseFilter(String excludeFilter) {
		final NodePresentationHelper presentationHelper = getPresentationHelper();
		try {
			 return OQLFilterBuilder.getFilter(excludeFilter, new FilterFactory<InstrumentationEvent>() {

				@Override
				public Filter<InstrumentationEvent> createFullTextFilter(final String expression) {
					return new Filter<InstrumentationEvent>() {
						@Override
						public boolean isValid(InstrumentationEvent input) {
							return presentationHelper.getFullname(input).contains(expression);
						}
					};
				}

				@Override
				public Filter<InstrumentationEvent> createAttributeFilter(String operator, String attribute,
						String value) {
					throw new RuntimeException("Attribute Filter not implemented in this context.");
				}
			});
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,	e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}
	
	private Filter<Stack<InstrumentationEvent>> parseBranchFilter(String excludeFilter) {
		final NodePresentationHelper presentationHelper = getPresentationHelper();
		try {
			 return OQLFilterBuilder.getFilter(excludeFilter, new FilterFactory<Stack<InstrumentationEvent>>() {

				@Override
				public Filter<Stack<InstrumentationEvent>> createFullTextFilter(final String expression) {
					return new Filter<Stack<InstrumentationEvent>>() {
						@Override
						public boolean isValid(Stack<InstrumentationEvent> input) {
							for (InstrumentationEvent instrumentationEvent : input) {
								if(presentationHelper.getFullname(instrumentationEvent).contains(expression)) {
									return true;
								}
							}
							return false;
						}
					};
				}

				@Override
				public Filter<Stack<InstrumentationEvent>> createAttributeFilter(String operator, String attribute,
						String value) {
					throw new RuntimeException("Attribute Filter not implemented in this context.");
				}
			});
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,	e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	private void transform() {
		Filter<Stack<InstrumentationEvent>> branchFilter = parseBranchFilter();
		Filter<InstrumentationEvent> nodeFilter = parseNodeFilter();

		workNode = sequenceTreeService.buildTree(branchFilter, nodeFilter, treeType);
	}

	public abstract void refreshDisplay();

	protected abstract SequenceTreeNode getSelectedNode();

	public Session getMain() {
		return session;
	}
	
	public NodePresentationHelper getPresentationHelper() {
		return parent.getPresentationHelper();
	}

	public void resetFocus() {
		requestFocusInWindow();
	}
}
