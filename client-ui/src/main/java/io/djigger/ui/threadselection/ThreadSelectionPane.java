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

import io.djigger.model.Capture;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.model.Metric;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.ql.Filter;
import io.djigger.ql.FilterFactory;
import io.djigger.ql.OQLFilterBuilder;
import io.djigger.store.Store;
import io.djigger.store.filter.StoreFilter;
import io.djigger.ui.Session;
import io.djigger.ui.analyzer.AnalyzerPaneListener;
import io.djigger.ui.common.EnhancedTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


public class ThreadSelectionPane extends JPanel implements MouseMotionListener, MouseListener, KeyListener, ComponentListener, AnalyzerPaneListener {

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

    private final JLabel hintLabel;

    private int xOffset = 200;

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

        hintLabel = new JLabel();
        hintLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        hintLabel.setOpaque(true);

        //subtle visual hint to indicate a dynamic (changing) value
        hintLabel.setForeground(Color.BLUE.darker());

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
        threadnameFilterTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
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

        JPanel hintPanel = new JPanel(new BorderLayout());

        Border bo = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        Border bi = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        hintPanel.setBorder(BorderFactory.createCompoundBorder(bo, bi));


        hintPanel.add(hintLabel, BorderLayout.CENTER);
        hintPanel.add(createThreadColorLegendPanel(), BorderLayout.LINE_END);

        threadTimelinePane.add(hintPanel, BorderLayout.PAGE_END);
        add(threadTimelinePane, BorderLayout.CENTER);

        refresh();
    }

    public void initialize() {
        main.getAnalyzerGroupPane().addListener(this);
//		main.getInstrumentationPane().addListener(this);
    }

    public void refresh() {
        try {
            buildBlocks();
        } catch (Exception e) {
            logger.error("Error while refreshing blocks ", e);
            throw new RuntimeException(e);
        }
        revalidate();
        repaint();
    }

    private void buildBlocks() {
        List<ThreadInfo> dumps = store.getThreadInfos().query(getFilterForCurrentSelection(false).getThreadInfoFilter());

        currentNumberOfThreadDumps = dumps.size();
        currentCaptures = null;

        blocks.clear();
        currentRange = null;
        if (dumps.size() > 0) {
            // TODO
            axis.setTimeBasedAxis(true);

            final Selection currentSelection = selectionHistory.peek();
            if (currentSelection.selectWholeRange) {
                ThreadInfo threadDump1 = null;
                ThreadInfo threadDump2 = null;
                for (ThreadInfo dump : dumps) {
                    if (threadDump1 == null || dump.getTimestamp() < threadDump1.getTimestamp()) {
                        threadDump1 = dump;
                    }
                    if (threadDump2 == null || dump.getTimestamp() > threadDump2.getTimestamp()) {
                        threadDump2 = dump;
                    }
                }
                currentRange = new Range(threadDump1.getTimestamp(), threadDump2.getTimestamp());
            } else {
                currentRange = new Range(currentSelection.start, currentSelection.end);
            }

            currentCaptures = store.getCaptures().query(new Filter<Capture>() {

                @Override
                public boolean isValid(Capture capture) {
                    // TODO Auto-generated method stub
                    return capture.getStart() < currentSelection.end &&
                        (capture.getEnd() == null || capture.getEnd() > currentSelection.start);
                }
            });

            Set<Long> selectedIds = getSelectedIds();

            HashMap<Long, ThreadBlock> blockMap = new HashMap<Long, ThreadBlock>();

            AggregateDefinition rangeDefinition;
            if (dumps.size() < 1000) {
                rangeDefinition = new AggregateDefinition(currentRange.start, currentRange.end, dumps.size(), false);
            } else {
                rangeDefinition = new AggregateDefinition(currentRange.start, currentRange.end, 100, false);
            }

            axis.setRangeDefinition(rangeDefinition);

            int c = 0;
            for (ThreadInfo thread : dumps) {
                c++;
                Long threadId = thread.getId();
                ThreadBlock block = blockMap.get(threadId);
                if (block == null) {
                    block = new ThreadBlock(this, threadId, thread.getName(), rangeDefinition);
                    blockMap.put(threadId, block);
                }
                block.add(thread.getTimestamp(), thread);
            }

            System.out.println("ThreadSelectionPane: rendering " + c + " threads.");

            //int wHeight = getSize().height;
            int wWidth = getSize().width - xOffset;

            int numberOfThreads = blockMap.size();

            int margin = 4;
            int blockHeight = 15;


            if (numberOfThreads > 0) {
                int currentY = 0;
                ArrayList<Long> sortedIds = new ArrayList<Long>(blockMap.size());
                sortedIds.addAll(blockMap.keySet());
                Collections.sort(sortedIds);

                for (Long id : sortedIds) {
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

            for (ThreadBlock block : blocks) {
                if (threadSelectionType == ThreadSelectionType.ALL ||
                    (threadSelectionType == ThreadSelectionType.INDIVIDUAL && selectedIds.contains(block.id))) {
                    block.selected = true;
                } else {
                    block.selected = false;
                }
                block.afterBuild();
            }

            blocksPane.setPreferredSize(new Dimension(0, numberOfThreads * (blockHeight + margin)));
            blocksPane.invalidate();
        }
    }

    private class BlocksPane extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (currentRange != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                setBackground(Color.WHITE);
                g2.setFont(new Font("Arial", Font.PLAIN, 10));

                for (ThreadBlock block : blocks) {
                    block.draw(g2, xOffset);
                }

                if (draggingState == DraggingType.ZOOM_IN) {
                    g2.setColor(new Color(0, 100, 200, 50));
                    g2.fillRect(Math.min(dragInitialX, dragX), 0,
                        Math.abs(dragX - dragInitialX), getSize().height);
                    String label = formatDate(xToRange(dragX));
                    g2.setColor(new Color(0, 100, 200));
                    g2.drawChars(label.toCharArray(), 0, label.length(), dragX, dragY);
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
        for (ThreadBlock block : blocks) {
            block.selected = false;
        }
        repaint();
        selectionChanged();
    }

    protected void selectAll() {
        threadSelectionType = ThreadSelectionType.ALL;
        for (ThreadBlock block : blocks) {
            block.selected = true;
        }
        repaint();
        selectionChanged();
    }

    protected boolean isMouseOverBlock() {
        for (ThreadBlock block : blocks) {
            if (block.mouseOver) {
                return true;
            }
        }
        return false;
    }

    public void selectThisOnly() {
        threadSelectionType = ThreadSelectionType.INDIVIDUAL;
        for (ThreadBlock block : blocks) {
            if (block.mouseOver) {
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

        if (dragPreviousX != null && dragPreviousY != null) {
            if (draggingState == null) {
                if (e.getX() > xOffset + 20 && Math.abs((e.getX() - dragPreviousX)) > 2 * Math.abs((e.getY() - dragPreviousY))) {
                    draggingState = DraggingType.ZOOM_IN;
                } else if (e.getX() > xOffset + 20 && Math.abs((e.getX() - dragPreviousX)) < 3 * Math.abs((e.getY() - dragPreviousY))) {
                    draggingState = DraggingType.ZOOM_OUT;
                }
            }
        }

        if (draggingState == DraggingType.RESIZE) {
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
        if (mouseOverBlock != null) {
            for (ThreadBlock block : blocks) {
                if (block == mouseOverBlock && !block.mouseOver) {
                    block.mouseOver = true;
                    repaint = true;
                } else if (block.mouseOver && block != mouseOverBlock) {
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

        StringBuilder labelStr = new StringBuilder();
        if (mouseOverBlock != null) {
            String captureInfos = "";
            long time = xToRange(e.getX());
            if (currentCaptures != null) {
                Capture capture = getCapture(time);
                if (capture != null) {
                    captureInfos = ", Sampling interval (ms): " + capture.getSamplingInterval();
                }
            }
            labelStr.append(mouseOverBlock.label + " [" + currentNumberOfThreadDumps + " thread dumps" + captureInfos + "]");
            if (!hintLabel.getText().equals(labelStr)) {
                repaint = true;
            }
            labelStr.append(" - ").append(formatDate(time));
        } else {
            labelStr.append(" ");
        }


        hintLabel.setText(labelStr.toString());

        if (repaint) {
            repaint();
        }
    }

    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss.S";

    private String formatDate(long time) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        return format.format(new Date(time));
    }

    private ThreadBlock getBlock(int x, int y) {
        for (ThreadBlock block : blocks) {
            if (y >= block.y && y <= block.y + block.height) {
                return block;
            }
        }
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        requestFocusInWindow();
        if (e.getButton() == e.BUTTON1) {
            ThreadBlock mouseOverBlock = getBlock(e.getX(), e.getY());
            if (mouseOverBlock != null) {
                if (threadSelectionType == ThreadSelectionType.ALL) {
                    for (ThreadBlock block : blocks) {
                        block.selected = false;
                    }
                }
                threadSelectionType = ThreadSelectionType.INDIVIDUAL;
                mouseOverBlock.selected = !mouseOverBlock.selected;
                repaint();
                if (!e.isControlDown()) {
                    selectionChanged();
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        dragInitialX = e.getX();
        dragInitialY = e.getY();
        if (draggingState == null) {
            if (e.getX() > xOffset - 20 && e.getX() < xOffset + 20) {
                draggingState = DraggingType.RESIZE;
            }
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (draggingState == DraggingType.RESIZE) {
            refresh();
        } else if (draggingState == DraggingType.ZOOM_IN) {
            if (Math.abs(dragX - dragInitialX) > 10) {
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
        return (long) (currentRange.start + (currentRange.end - currentRange.start + 1) * (1.0 * (x - xOffset)) / (getSize().width - xOffset));
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (!popup.isVisible()) {
            for (ThreadBlock block : blocks) {
                block.mouseOver = false;
            }
            repaint();
        }

    }

    private Set<Long> getSelectedIds() {
        Set<Long> selectedIds = new HashSet<Long>();
        for (ThreadBlock block : blocks) {
            if (block.selected) {
                selectedIds.add(block.id);
            }
        }
        return selectedIds;
    }

    private StoreFilter getFilterForCurrentSelection(boolean filterThreads) {
        StoreFilter filter;
        Selection currentSelection = selectionHistory.peek();

        Set<Long> threadIdsFilter;
        if (filterThreads) {
            if (threadSelectionType == ThreadSelectionType.ALL) {
                threadIdsFilter = null;
            } else {
                threadIdsFilter = getSelectedIds();
            }
        } else {
            threadIdsFilter = null;
        }

        Filter<ThreadInfo> threadnameFilter = parseThreadnameFilter();
        if (currentSelection.selectWholeRange) {
            filter = timeStoreFilter(threadnameFilter, threadIdsFilter, null, null);
        } else {
            filter = timeStoreFilter(threadnameFilter, threadIdsFilter, currentSelection.start, currentSelection.end);
        }
        return filter;
    }

    private StoreFilter timeStoreFilter(final Filter<ThreadInfo> threadnameFilter, final Set<Long> threadIds, final Long startDate, final Long endDate) {
        return new StoreFilter(new Filter<ThreadInfo>() {

            @Override
            public boolean isValid(ThreadInfo thread) {
                if ((startDate == null || thread.getTimestamp() > startDate)
                    && (endDate == null || thread.getTimestamp() < endDate)) {
                    return ((threadIds == null || threadIds.contains(thread.getId())) &&
                        (threadnameFilter == null || threadnameFilter.isValid(thread)));
                } else {
                    return false;
                }
            }
        }, new Filter<InstrumentationEvent>() {

            @Override
            public boolean isValid(InstrumentationEvent sample) {
                return (threadIds == null || threadIds.contains(sample.getThreadID()))
                    && (startDate == null || sample.getStart() >= startDate)
                    && (endDate == null || sample.getEnd() <= endDate);
            }
        }, new Filter<Metric<?>>() {

            @Override
            public boolean isValid(Metric<?> sample) {
                return (startDate == null || sample.getTime() >= startDate)
                    && (endDate == null || sample.getTime() <= endDate);
            }
        });
    }


    private Filter<ThreadInfo> parseThreadnameFilter() {
        Filter<ThreadInfo> complexFilter = null;
        final String filter = threadnameFilterTextField.getText();
        if (filter != null) {
            FilterFactory<ThreadInfo> factory = new FilterFactory<ThreadInfo>() {

                @Override
                public Filter<ThreadInfo> createFullTextFilter(final String expression) {
                    return new Filter<ThreadInfo>() {
                        @Override
                        public boolean isValid(ThreadInfo input) {
                            return input.getName().contains(expression);
                        }

                    };
                }

                @Override
                public Filter<ThreadInfo> createAttributeFilter(
                    String operator, String attribute, String value) {
                    // TODO Auto-generated method stub
                    return null;
                }
            };
            try {
                complexFilter = OQLFilterBuilder.getFilter(filter, factory);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return complexFilter;
    }

    private void selectionChanged() {
        main.onThreadSelection(getFilterForCurrentSelection(true));
    }

    private void zoomIn() {
        long t1 = xToRange(Math.min(dragX, dragInitialX)) + 1;
        long t2 = xToRange(Math.max(dragX, dragInitialX)) - 1;
        Selection selection = new Selection(t1, t2);

        selectionHistory.push(selection);

        List<ThreadInfo> dumps = store.getThreadInfos().query(getFilterForCurrentSelection(false).getThreadInfoFilter());
        if (dumps == null || dumps.size() == 0) {
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
        if (selectionHistory.size() > 1) {
            selectionHistory.pop();
            refresh();
            selectionChanged();
        }
    }

    private Capture getCapture(long time) {
        for (Capture capture : currentCaptures) {
            if (time > capture.getStart() && (capture.getEnd() == null || time < capture.getEnd())) {
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
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            selectionChanged();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void componentResized(ComponentEvent e) {
        refresh();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
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
    public void onSelection(Set<Long> selectedThreadIds) {
        for (ThreadBlock block : blocks) {
            block.selectedInAnalyzerPane = false;
        }

        for (ThreadBlock block : blocks) {
            if (selectedThreadIds.contains(block.id)) {
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

    private static JPanel createThreadColorLegendPanel() {
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.LINE_AXIS));

        legend.add(new JSeparator(SwingConstants.VERTICAL));
        legend.add(Box.createHorizontalStrut(4));
        legend.add(new JLabel("Thread state:"));

        Thread.State[] states = new Thread.State[]{Thread.State.RUNNABLE, Thread.State.BLOCKED, Thread.State.WAITING, null};

        for (Thread.State state : states) {
            legend.add(Box.createHorizontalStrut(8));
            ImageIcon icon = createThreadLegendIcon(ThreadBlock.getColorForThreadState(state));
            String text = "other";
            if (state != null) {
                text = state.toString().toLowerCase();
            }
            legend.add(new JLabel(text, icon, SwingConstants.LEADING));
        }

        return legend;
    }

    private static ImageIcon createThreadLegendIcon(Color color) {
        int w = 16;
        int h = 12;
        int b = 1; // border
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // draw border
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);

        g.setColor(color);
        g.fillRect(b, b, w - b * 2, h - b * 2);
        g.dispose();
        return new ImageIcon(image);
    }
}
