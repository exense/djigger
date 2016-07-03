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
package io.djigger.ui.analyzer;

import io.djigger.ui.analyzer.BlockColorer.Framework;
import io.djigger.ui.common.CloseButton;
import io.djigger.ui.common.CustomButton;
import io.djigger.ui.common.FileChooserHelper;
import io.djigger.ui.model.AnalysisNode;
import io.djigger.ui.model.AnalysisNodePath;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BlockView extends AnalyzerPane implements MouseListener, MouseMotionListener, AdjustmentListener {

	private static final long serialVersionUID = 7088753046913758857L;
	
	private static final Logger logger = LoggerFactory.getLogger(BlockView.class);

	private final BlockColorer blockColorer;

	private GraphPane graph;

	private JScrollPane scrollPane;

	private Block rootBlock;

	private final Stack<AnalysisNodePath> selectionHistory;

	private Block mouseOverBlock;

	private Block resizeBlock;

	private JTextArea methodTextArea;

	private FrameworkLegendPane legend;

	private TreePopupMenu popup;

	public BlockView(AnalyzerGroupPane parent, TreeType treeType) {
		super(parent, treeType);

		if(main.getOptions().hasOption("colorer")) {
			BlockColorer colorer;
			try {
				colorer = new BlockColorer(new File(main.getOptions().getOption("colorer")));
			} catch (Exception e) {
				logger.error("Error while initilizing block view ", e);
				colorer = new BlockColorer();
			}
			blockColorer = colorer;
		} else {
			blockColorer = new BlockColorer();
		}

		createViewComponents();

		rootBlock = new Block(workNode, this);

		selectionHistory = new Stack<AnalysisNodePath>();
		selectionHistory.push(rootBlock.getNode().getPath());
	}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {
		if(!popup.isVisible()) {
			if(mouseOverBlock!=null) {
				mouseOverBlock.mouseExited(e);
				mouseOverBlock = null;
			}
			if(this.resizeBlock!=null) {
				List<Block> blockOnLevels = new ArrayList<Block>();
				rootBlock.getBlocksOnLevel(this.resizeBlock.getLevel(), blockOnLevels);
				for(Block blockOnLevel:blockOnLevels) {
					blockOnLevel.mouseOverLineExited(e);
				}
				resizeBlock = null;
			}
			repaint();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {
		Block currentBlock = resizeBlock;
		if (currentBlock != null) {
			if (e.isControlDown()) {
				currentBlock.resize(e.getX());
			} else {
				List<Block> blockOnLevel = new ArrayList<Block>();
				rootBlock.getBlocksOnLevel(resizeBlock.getLevel(), blockOnLevel);
				for (Block block : blockOnLevel) {
					block.resize(e.getX());
				}
			}
		}
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		boolean repaintNeeded = false;
		Block block = rootBlock.getBlock(e.getX(), e.getY());
		if(block == null || block!=mouseOverBlock) {
			if(mouseOverBlock!=null) {
				mouseOverBlock.mouseExited(e);
				methodTextArea.setText("");
				repaintNeeded = true;
			}
			if(block!=null) {
				block.mouseEntered(e);
				methodTextArea.setText(getPresentationHelper().longLabel(block.getNode(),block.getRoot().getNode()));
				repaintNeeded = true;
			}
		}
		mouseOverBlock = block;

		Block resizeBlock = rootBlock.getBlockBorder(e.getX(), e.getY());
		if(isResizeBlockChanged(resizeBlock)) {
			if(this.resizeBlock!=null) {
				List<Block> blockOnLevels = new ArrayList<Block>();
				rootBlock.getBlocksOnLevel(this.resizeBlock.getLevel(), blockOnLevels);
				for(Block blockOnLevel:blockOnLevels) {
					blockOnLevel.mouseOverLineExited(e);
				}
			}
			if(resizeBlock!=null) {
				List<Block> blockOnLevels = new ArrayList<Block>();
				rootBlock.getBlocksOnLevel(resizeBlock.getLevel(), blockOnLevels);
				for(Block blockOnLevel:blockOnLevels) {
					blockOnLevel.mouseOverLineEntered(e);
				}
			}
			repaintNeeded = true;
		}
		this.resizeBlock = resizeBlock;

		if(resizeBlock == null) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		} else {
			setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
		}

		if(repaintNeeded) {
			repaint();
		}
	}

	private boolean isResizeBlockChanged(Block resizeBlock) {
		if(this.resizeBlock==null && resizeBlock!=null) {
			return true;
		} else if(this.resizeBlock!=null && this.resizeBlock!=resizeBlock) {
			return true;
		} else {
			return false;
		}
	}

	protected void createViewComponents() {

		contentPanel.setLayout(new BorderLayout());

		popup = new TreePopupMenu(this);

		graph = new GraphPane();
		graph.setPreferredSize(new Dimension(5000,0));
		graph.addMouseListener(this);
		graph.addMouseMotionListener(this);
		graph.setComponentPopupMenu(popup);



		scrollPane = new JScrollPane(graph, ScrollPaneLayout.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneLayout.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
		contentPanel.add(scrollPane,BorderLayout.CENTER);

		JPanel legendPane = new JPanel(new BorderLayout());

		methodTextArea = new JTextArea();
		methodTextArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		legendPane.add(methodTextArea, BorderLayout.PAGE_START);

		legend = new FrameworkLegendPane(blockColorer);
		legend.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		legend.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
		legendPane.add(legend, BorderLayout.CENTER);

		contentPanel.add(legendPane, BorderLayout.PAGE_END);
	}

	@Override
	public void refreshDisplay() {
		Block oldRoot = rootBlock;

		Block newRoot;

		if(selectionHistory.size()>0) {
			AnalysisNode newRootNode = workNode.find(selectionHistory.peek());
			if(newRootNode != null) {
				newRoot = new Block(newRootNode, this);
			} else {
				newRoot = new Block(workNode, this);
			}
		} else {
			newRoot = new Block(workNode, this);
		}

		for(Block block:oldRoot.getModifiedBlocks()) {
			Block newBlock = newRoot.find(block.getNode().getPath());
			if(newBlock != null) {
				newBlock.loadPropertiesFrom(block);
			}
		}

		rootBlock = newRoot;

		repaint();
	}

	protected void reloadLegend() {
		legend.reload();
		revalidate();
		repaint();
	}

	@Override
	protected AnalysisNode getSelectedNode() {
		return mouseOverBlock.getNode();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON1) {
			if (e.getX()<20) {
				if(selectionHistory.size() > 1) {
					selectionHistory.pop();
					refreshDisplay();
				}
			} else {
				Block block = rootBlock.getBlock(e.getX(), e.getY());
				if(block!=null) {
					selectionHistory.push(block.getNode().getPath());
					refreshDisplay();
				}
			}
		}
	}

	public void exportToPNG() {
		BufferedImage im = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
		paint(im.createGraphics());
		try {
			ImageIO.write(im, "PNG", new File("c:\\temp\\yourImageName.PNG"));
		} catch (IOException e1) {
			logger.error("Error while exporting block view",e1);
		}
	}

	private class GraphPane extends JPanel {

		private static final long serialVersionUID = 6316345813444302722L;

	    protected void paintComponent(Graphics g) {
	    	super.paintComponent(g);

	        Graphics2D g2 = (Graphics2D) g.create();

	        setBackground(Color.WHITE);

	        g2.setFont(new Font("Arial", Font.PLAIN,  10));


	        int rootBlockHeight = getSize().height;
	        rootBlock.position(20, 0, rootBlockHeight);
	        rootBlock.drawHist(g2);

	        g2.setColor(Color.GRAY);
	        g2.drawRect(0, 0, 20, rootBlockHeight);
	        if(selectionHistory.size()>1) {
		        int middleHeight = (int)(rootBlockHeight/2.0)+20;
		        g2.drawLine(5, middleHeight, 10, middleHeight-5);
		        g2.drawLine(5, middleHeight, 10, middleHeight+5);
	        }

	        g2.dispose();
	    }
	}

	private class FrameworkLegendPane extends JPanel implements ActionListener {

		private static final long serialVersionUID = 6316345813444302722L;

		private final BlockColorer blockColorer;

	    public FrameworkLegendPane(BlockColorer blockColorer) {
			super();
			this.blockColorer = blockColorer;

			setPreferredSize(new Dimension(Integer.MAX_VALUE,40));
			setLayout(new FlowLayout());;

			reload();
		}

	    public void reload() {
	    	this.removeAll();
			for(Framework framework:blockColorer.getFrameworks()) {
	        	LegendButton button = new LegendButton(framework);
	        	add(button);
			}

			CustomButton addButton = new CustomButton(new JLabel("Add framework"));
			addButton.addActionListener(this);
			add(addButton);

			CustomButton exportButton = new CustomButton(new JLabel("Export legend"));
			exportButton.addActionListener(this);
			exportButton.setActionCommand("export");
			add(exportButton);
			
			CustomButton importButton = new CustomButton(new JLabel("Import legend"));
			importButton.addActionListener(this);
			importButton.setActionCommand("import");
			add(importButton);
	    }

		@SuppressWarnings("serial")
		private class LegendButton extends CustomButton implements ActionListener {

			private final Framework framework;

			public LegendButton(Framework framework_) {
				super("Edit this framework");
				this.framework = framework_;

				JPanel panel = new JPanel();
				panel.setLayout(new FlowLayout(FlowLayout.LEFT,5,2));

		        JPanel legendPanel = new JPanel () {

		        	protected void paintComponent(Graphics g) {
				    	super.paintComponent(g);

				        Graphics2D g2 = (Graphics2D) g.create();

				        g2.setColor(framework.getColor());
				        g2.fillRect(0, 0, 20, 20);

				        g2.dispose();
					}
		        };

		        panel.add(legendPanel);
		        panel.add(new JLabel(framework_.getName()));
		        panel.add(new CloseButton(this));

		        add(panel);
		        addActionListener(this);
			}



			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals("close")) {
					blockColorer.frameworks.remove(framework);
					refreshDisplay();
					reloadLegend();
				} else {
					new EditFramework(framework);
				}
			}

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand().equals("export")) {
				File file = FileChooserHelper.selectFile("Export Legend", "Save");
				if(file!=null) {
	            	blockColorer.export(file);
	            }
			} else if(e.getActionCommand().equals("import")) {
				File file = FileChooserHelper.selectFile("Import Legend", "Open");
				if(file!=null) {
					blockColorer.clearFrameworks();
					blockColorer.loadFrameworks(file);
	            	
					refreshDisplay();
					reloadLegend();
	            }
			} else {
				new EditFramework(null);
			}
		}
	}

	@SuppressWarnings("serial")
	private class EditFramework extends JPanel implements ActionListener {

		private final Framework framework;

		private final JDialog frame;

		private final JTextField name;

		private final JTextField regexp;

		private final JButton colorSelectButton;

		private final JButton button;

		private final boolean newFramework;

		public EditFramework(Framework framework){
			super();

			newFramework = framework == null;

			if(newFramework) {
				name = new JTextField("",20);
				regexp = new JTextField("",20);
				Random random = new Random();
				Color randomColor = new Color(random.nextInt(200),random.nextInt(200),random.nextInt(200));
				this.framework = blockColorer.new Framework("",randomColor,"");

				button = new JButton("Add");
			} else {
				this.framework = framework;
				name = new JTextField(framework.getName(),20);
				regexp = new JTextField(framework.getMatchingPattern().pattern(),20);

				button = new JButton("OK");
			}

			setLayout(new GridLayout(0,1,0,2));

			button.addActionListener(this);

			colorSelectButton = new JButton("select");
			colorSelectButton.addActionListener(this);
			colorSelectButton.setBackground(this.framework.getColor());

			add(new JLabel("Name"));
			add(name);
			add(new JLabel("Matching pattern"));
			add(regexp);
			add(new JLabel("Color"));
			add(colorSelectButton);
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
			if(e.getActionCommand().equals("select")) {
				framework.setColor(JColorChooser.showDialog(this,"Choose color",framework.getColor()));
				colorSelectButton.setBackground(framework.getColor());
			} else  {
				framework.setMatchingPattern(this.regexp.getText());
				framework.setName(this.name.getText());
				if(newFramework) {
					blockColorer.frameworks.add(framework);
				}
				refreshDisplay();
				reloadLegend();
				frame.setVisible(false);
			}
		}
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		repaint();

	}

	public JTextArea getMethodTextArea() {
		return methodTextArea;
	}

	public BlockColorer getBlockColorer() {
		return blockColorer;
	}


}
