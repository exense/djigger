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

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ThreadSelectionTimeAxis extends JPanel {

    private boolean timeBasedAxis;

    private final ThreadSelectionPane threadSelectionGraphPane;

    private AggregateDefinition rangeDefinition;

    public ThreadSelectionTimeAxis(ThreadSelectionPane threadSelectionGraphPane) {
        super();
        this.threadSelectionGraphPane = threadSelectionGraphPane;
        setPreferredSize(new Dimension(0, 20));
    }

    public void setTimeBasedAxis(boolean timeBasedAxis) {
        this.timeBasedAxis = timeBasedAxis;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (threadSelectionGraphPane.getCurrentRange() != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            setBackground(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, 10));

            if (timeBasedAxis) {
                drawTimeAxis(g2, threadSelectionGraphPane.getxOffset());
            } else {
                drawIdAxis(g2, threadSelectionGraphPane.getxOffset(), threadSelectionGraphPane.getWidth());
            }

            g2.dispose();
        }
    }

    public void setRangeDefinition(AggregateDefinition rangeDefinition) {
        this.rangeDefinition = rangeDefinition;
    }

    private void drawTimeAxis(Graphics2D graph, int xOffset) {
        long interval = threadSelectionGraphPane.getCurrentRange().end - threadSelectionGraphPane.getCurrentRange().start;
        SimpleDateFormat format;
        if (interval < 10000) {
            format = new SimpleDateFormat("HH:mm:ss.S");
        } else if (interval < 3600000) {
            format = new SimpleDateFormat("HH:mm:ss");
        } else if (interval < 432000000) {
            format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        } else {
            format = new SimpleDateFormat("yyyy.MM.dd");
        }

        String startDateStr = format.format(new Date(threadSelectionGraphPane.getCurrentRange().start));
        double labelWidth = graph.getFont()
            .getStringBounds(startDateStr, graph.getFontRenderContext())
            .getWidth();

        int wWidth = getSize().width - xOffset;
        int margin = 20;
        int numberOfLabels = (int) (wWidth / (labelWidth + margin));
        int xIncrement = wWidth / numberOfLabels;

        int x = xOffset;
        for (int i = 0; i < numberOfLabels; i++) {
            Date date = new Date(this.threadSelectionGraphPane.xToRange(x));
            String dateStr = format.format(date);
            graph.drawChars(dateStr.toCharArray(), 0, dateStr.length(), x + 5,
                15);
            graph.drawLine(x, 20, x, 15);
            x += xIncrement;
        }

    }

    private void drawIdAxis(Graphics2D graph, int xOffset, int width) {
        String id2Str = Long.toString(rangeDefinition.getEnd());
        double labelWidth = graph.getFont()
            .getStringBounds(id2Str, graph.getFontRenderContext())
            .getWidth();

        int wWidth = width - xOffset;
        int margin = 20;

        int maxNumberOfLabel = (int) (wWidth / (labelWidth + margin));

        if (rangeDefinition.getRangeNumber() < maxNumberOfLabel) {
            for (int i = 0; i < rangeDefinition.getRangeNumber(); i++) {
                String str = Long.toString(rangeDefinition.getCursor(i));
                int x = xOffset + (int) (i * 1.0 / rangeDefinition.getRangeNumber() * wWidth);
                graph.drawChars(str.toCharArray(), 0, str.length(), x, 15);
                graph.drawLine(x, 20, x, 15);
            }
        } else {
            int factor = rangeDefinition.getRangeNumber() / maxNumberOfLabel;
            for (int i = 0; i < rangeDefinition.getRangeNumber(); i++) {
                if (i % factor == 0) {
                    String str = Long.toString(rangeDefinition.getCursor(i));
                    int x = xOffset + (int) (i * 1.0 / rangeDefinition.getRangeNumber() * wWidth);
                    graph.drawChars(str.toCharArray(), 0, str.length(), x, 15);
                    graph.drawLine(x, 20, x, 15);
                }
            }
            System.err.println("number of label to high");
        }
    }

}
