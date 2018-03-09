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
import io.djigger.ui.common.NodePresentationHelper;
import io.djigger.ui.model.AnalysisNode;
import io.djigger.ui.model.AnalysisNodePath;
import io.djigger.ui.model.NodeID;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Block implements MouseListener {

    private boolean mouseOver = false;

    private boolean mouseOverLine = false;

    private boolean modified = false;

    private int x;

    private int y;

    private int width = WIDTH;

    private int height;

    private final AnalysisNode node;

    private final BlockView parentContainer;

    private Block parent;

    private final List<Block> children;

    private final float relativeWeight;

    private static int WIDTH = 20;

    private final Framework framework;

    private String label;

    public Block(AnalysisNode root, BlockView parentContainer) {
        this(root, 1.0, null, parentContainer);
    }

    private Block(AnalysisNode root, double parentWeight, Block parent, BlockView parentContainer) {
        this.node = root;
        this.parent = parent;

        if (parent != null) {
            this.relativeWeight = (float) (node.getWeight() / (1.0 * parent.node.getWeight()));
        } else {
            this.relativeWeight = 1;
        }
        this.parentContainer = parentContainer;

        NodePresentationHelper presentationHelper = parentContainer.getPresentationHelper();

        Framework matchedFramework = parentContainer.getBlockColorer().match(presentationHelper.getFullname(node));
        if (matchedFramework != null) {
            framework = matchedFramework;
        } else {
            framework = parent != null ? parent.framework : null;
        }

        if (!node.isLeaf()) {
            children = new ArrayList<Block>(root.getChildren().size());
            if (parentWeight > 0.01) {
                for (AnalysisNode child : root.getChildren()) {
                    Block newBlock = new Block(child, parentWeight * relativeWeight, this, parentContainer);

                    children.add(newBlock);
                }
            }
        } else {
            children = new ArrayList<Block>(0);
        }
    }

    public void position(int x, int y, int height) {
        this.x = x;
        this.y = y;
        this.height = height;

        int currentY = y;
        int childHeight;

        float relativeWeightSum = 0;
        for (int i = 0; i < children.size(); i++) {
            Block child = children.get(i);

            relativeWeightSum += child.relativeWeight;

            if (i == children.size() - 1 && relativeWeightSum > 0.95) {
                childHeight = y + height - currentY;
            } else {
                childHeight = (int) (child.relativeWeight * height);
            }
            child.position(x + width, currentY, childHeight);
            currentY += childHeight;
        }
    }

    public void drawHist(Graphics2D graph) {
        for (int i = 0; i < children.size(); i++) {
            Block child = children.get(i);
            child.drawHist(graph);
        }

        Color color = framework != null ? framework.getColor() : Color.LIGHT_GRAY;
        graph.setColor(color);
        graph.fillRect(x, y, width, height);
        graph.setPaintMode();

        if (mouseOver) {
            graph.setColor(color.darker());
            graph.fillRect(x, y, width, height);
            graph.setColor(Color.BLACK);
//			if(framework!=null) {
//				String label = framework.getName() + ": " + node.toString();
//				graph.drawChars(label.toCharArray(), 0, label.length(), 5, 15);
//			} else {
//				graph.drawChars(node.toString().toCharArray(), 0, node.toString().length(), 5, 15);
//
//			}
        }

        if (mouseOverLine) {
            graph.setColor(Color.GRAY);
            graph.drawRect(x, y, width, height);
        }

        graph.setColor(Color.BLACK);
        if (height > 20) {
            label = getLabel();
            if (height > 20 && width > (graph.getFont().getStringBounds(label, graph.getFontRenderContext()).getWidth() + 5)) {
                graph.drawChars(label.toCharArray(), 0, label.length(), x + 5, y + 15);
                graph.setColor(Color.GRAY);
                graph.drawRect(x, y, width, height);
            } else if (height > 40) {
                String[] split = label.split("\\.");
                if (split.length == 2) {
                    if (width > (graph.getFont().getStringBounds(split[0], graph.getFontRenderContext()).getWidth() + 5) &&
                        width > (graph.getFont().getStringBounds(split[1], graph.getFontRenderContext()).getWidth() + 5)) {
                        graph.drawChars(split[0].toCharArray(), 0, split[0].length(), x + 5, y + 15);
                        graph.drawChars(split[1].toCharArray(), 0, split[1].length(), x + 5, y + 35);
                        graph.setColor(Color.GRAY);
                        graph.drawRect(x, y, width, height);
                    }
                }
            }
        }

        //graph.drawRect(x, y, width, height);
        //graph.setStroke(new BasicStroke(10*height/currentStack.get(0).height));
        graph.setColor(Color.GRAY);
        graph.drawLine(x, y, x + width, y);
        graph.drawLine(x, y + height, x + width, y + height);
        if (node.getChildren().size() > 1 || node.getOwnWeight() > 0) {
            graph.drawLine(x + width, y, x + width, y + height);
        }

//		if(parentContainer.getMain().getInstrumentationPane().isSelected(node.getRealNodePath())) {
//			Color borderColor = parentContainer.getMain().getInstrumentationPane().getColor(node.getRealNodePath());
//			if(borderColor != null) {
//				graph.setColor(borderColor);
//			} else {
//				graph.setColor(Color.RED);
//			}
//			graph.drawRect(x+1, y+1, width-2, height-2);
//		}
    }

    public Block getBlock(int x, int y) {
        if (x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height) {
            return this;
        } else {
            for (Block child : children) {
                Block block = child.getBlock(x, y);
                if (block != null) {
                    return block;
                }
            }
        }
        return null;
    }

    public Block getBlockBorder(int x, int y) {
        int delta = 2;
        if (x >= (this.x + this.width) - delta && x <= (this.x + this.width) + delta &&
            y > this.y && y < (this.y + this.height)) {
            return this;
        } else {
            for (Block child : children) {
                Block block = child.getBlockBorder(x, y);
                if (block != null) {
                    return block;
                }
            }
        }
        return null;
    }

    public int getLevel() {
        if (parent == null) {
            return 0;
        } else {
            return parent.getLevel() + 1;
        }
    }

    public void getBlocksOnLevel(int level, List<Block> result) {
        if (getLevel() == level) {
            result.add(this);
        } else {
            for (Block child : children) {
                child.getBlocksOnLevel(level, result);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mouseOver = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseOver = false;
    }

    public AnalysisNode getNode() {
        return node;
    }

    public void mouseOverLineEntered(MouseEvent e) {
        mouseOverLine = true;
    }

    public void mouseOverLineExited(MouseEvent e) {
        mouseOverLine = false;
    }

    public void resize(int newX) {
        width = newX - x;
        modified = true;
    }

    public int getHeight() {
        return height;
    }

    public int getPercentage() {
        return (int) (100 * node.getWeight() / (1.0 * getRoot().node.getWeight()));
    }

    public Block getRoot() {
        if (parent == null) {
            return this;
        } else {
            return parent.getRoot();
        }
    }

    public List<NodeID> getPath() {
        return null;
    }

    public boolean isModified() {
        return modified;
    }

    public List<Block> getModifiedBlocks() {
        LinkedList<Block> modifiedBlocks = new LinkedList<Block>();
        getModifiedBlocks(modifiedBlocks);
        return modifiedBlocks;
    }

    private void getModifiedBlocks(List<Block> modifiedBlocks) {
        if (modified) {
            modifiedBlocks.add(this);
        }
        if (children.size() > 0) {
            for (Block child : children) {
                child.getModifiedBlocks(modifiedBlocks);
            }
        }
    }

    public LinkedList<Block> getFullPath() {
        if (parent != null) {
            LinkedList<Block> path = parent.getFullPath();
            path.addLast(this);
            return path;
        } else {
            LinkedList<Block> result = new LinkedList<Block>();
            result.add(this);
            return result;
        }
    }

    public Block find(AnalysisNodePath path) {
        if (node.getPath().equals(path)) {
            return this;
        } else {
            if (children.size() > 0) {
                for (Block child : children) {
                    Block block = child.find(path);
                    if (block != null) {
                        return block;
                    }
                }
                return null;
            } else {
                return null;
            }
        }
    }

    public Block get(LinkedList<Block> branch) {
        Block currentID = branch.pop();
        try {
            if (node.getId().equals(currentID.node.getId())) {
                return findInChildren(branch);
            } else {
                return null;
            }
        } finally {
            branch.push(currentID);
        }
    }

    Block findInChildren(LinkedList<Block> branch) {
        if (branch.size() > 0) {
            Block currentID = branch.pop();
            try {
                Block block = get(currentID);
                if (block == null) {
                    return null;
                } else {
                    return block.findInChildren(branch);
                }
            } finally {
                branch.push(currentID);
            }
        } else {
            return this;
        }
    }

    private Block get(Block block) {
        for (Block child : children) {
            if (child.node.getId().equals(block.node.getId())) {
                return child;
            }
        }
        return null;
    }

    public void loadPropertiesFrom(Block block) {
        width = block.width;
        modified = true;
    }

    public void defineAsRoot() {
        parent = null;
        onRootChanged();
    }

    public String getLabel() {
        if (label == null) {
            label = parentContainer.getPresentationHelper().shortLabel(getNode(), getRoot().getNode());
        }
        return label;
    }

    public void onRootChanged() {
        label = null;
        for (Block child : children) {
            child.onRootChanged();
        }
    }

}
