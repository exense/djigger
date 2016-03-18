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

package io.djigger.ui.instrumentation;

import io.djigger.model.RealNodePath;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.subscription.NodeSubscription;
import io.djigger.store.filter.StoreFilter;
import io.djigger.ui.Session;
import io.djigger.ui.common.NodePresentationHelper;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;


public class InstrumentationPane extends JPanel {

	private final Session session;

	private final TransactionTable transactionTable;
	
	private final TransactionList transactionList;
	
	private final List<InstrumentationPaneListener> listeners;
	
	private final NodePresentationHelper presentationHelper;
	
	public InstrumentationPane(final Session parent, final NodePresentationHelper presentationHelper) {
		super(new GridLayout(1,1));
		this.session = parent;
		this.presentationHelper = presentationHelper;

		listeners = new ArrayList<InstrumentationPaneListener>();
		
		transactionTable = new TransactionTable(parent.getStatisticsCache(), this);
		transactionList = new TransactionList(parent, this, parent.getStatisticsCache());
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(transactionTable);
		splitPane.setRightComponent(transactionList);
		splitPane.setDividerLocation(0.6);
		
		add(splitPane);
	}

	public Session getSession() {
		return session;
	}

	public TransactionTable getTransactionTable() {
		return transactionTable;
	}

	public void setStoreFilter(StoreFilter storeFilter) {
		refresh();
	}

	public void refresh() {
//		fireSelectionChanged();
		transactionTable.load();
	}
	
	protected void subscriptionSelected() {
		transactionList.reloadSamples();
	}

//	@Override
//	public void actionPerformed(ActionEvent e) {
//		if(e.getActionCommand().equals("Export as CSV")) {
//			JFileChooser chooser = new JFileChooser();
//            chooser.setDialogTitle("Export Instrument Samples");
//            chooser.setApproveButtonText("Export");
//
//            int returnVal = chooser.showOpenDialog(null);
//            if(returnVal == JFileChooser.APPROVE_OPTION) {
//            	File outputFile = chooser.getSelectedFile();
//            	if(outputFile!=null) {
//	            	for(SubscriptionWrapper subscription:getSelectedSubscriptions()) {
//	            		InstrumentationStatistics statistics = getStatistics(subscription);
//	            		statistics.export(outputFile, subscription.toString());
//	            	}
//            	}
//            }
//		} else if (e.getActionCommand().equals("Stop instrumentation")) {
//			for(SubscriptionWrapper subscription:getSelectedSubscriptions()) {
//        		facade.deinstrument(subscription.subscription);
//        	}
//		} else if (e.getActionCommand().equals("Start instrumentation")) {
//			for(SubscriptionWrapper subscription:getSelectedSubscriptions()) {
//        		facade.instrument(subscription.subscription);
//        	}
//		} else if (e.getActionCommand().equals("New instrumentation")) {
//			new NewInstrumentationPane();
//		} else {
//			//analyzer.closeMethodNodeDetailsPane(this);
//		}
//	}


//	private void fireSelectionChanged() {
//		for(InstrumentationPaneListener listener:listeners) {
//			listener.onSelection(selection);
//		}
//	}

	public void addListener(InstrumentationPaneListener listener) {
		listeners.add(listener);
	}

	public boolean isSelected(RealNodePath path) {
//		// TODO: do this in a more rigorous way
//		InstrumentationAttributesHolder holder = new InstrumentationAttributesHolder();
//		holder.setStacktrace(path);
//		InstrumentationSample dummSample = new InstrumentationSample(null, null, 0, 0, holder);
//		for(SubscriptionWrapper wrapper:getSelectedSubscriptions()) {
//			if(wrapper.subscription.match(dummSample)) {
//				return true;
//			}
//		}
		return false;
	}

	public Color getColor(RealNodePath path) {
//		LegendItemCollection collection = chartPanel.getChart().getXYPlot().getRendererForDataset(dataset).getLegendItems();
//		for(int i=0; i<collection.getItemCount();i++) {
//			// TODO: do this in a more rigorous way
//			InstrumentationAttributesHolder holder = new InstrumentationAttributesHolder();
//			holder.setStacktrace(path);
//			InstrumentationSample dummSample = new InstrumentationSample(null, null, 0, 0, holder);
//			if(((SubscriptionWrapper)collection.get(i).getSeriesKey()).subscription.match(dummSample)) {
//				Paint paint = collection.get(i).getLinePaint();
//				if(paint instanceof Color) {
//					return (Color)paint;
//				}
//			}
//		}
		return null;
	}


	public class NewInstrumentationPane extends JPanel implements ActionListener {

		private final JDialog frame;

		private final JTextField classname;

		private final JTextField methodname;

		private final JButton button;

		public NewInstrumentationPane(){
			super();

			classname = new JTextField("",20);
			methodname = new JTextField("",20);
			button = new JButton("Add");

			setLayout(new GridLayout(0,1,0,2));

			button.addActionListener(this);

			add(new JLabel("Class name pattern (regex)"));
			add(classname);
			add(new JLabel("Method name pattern (regex)"));
			add(methodname);
			add(new JSeparator());
			add(button);


			frame = new JDialog();
	        frame.add(this);
	        frame.pack();
	        frame.setResizable(false);
	        frame.setVisible(true);

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand().equals("Add")) {
				InstrumentSubscription subscription = new NodeSubscription(classname.getText(), methodname.getText(), false);
				session.addSubscription(subscription);
				frame.setVisible(false);
			}
		}
	}
}
