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
package io.djigger.ui.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class EnhancedTextField extends JTextField implements FocusListener {

    private static final long serialVersionUID = -3250378583105935424L;

    private final String label;

    private final Color initialColor;

    public EnhancedTextField(String label) {
        super();
        this.label = label;
        this.initialColor = getForeground();
        addFocusListener(this);

        setDefaultText();
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (getText() == null) {
            super.setText("");
            setForeground(initialColor);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        setDefaultText();
    }

    @Override
    public String getText() {
        String text = super.getText();
        if (text == null || text.equals("") ||
            text.equals(label)) {
            return null;
        } else {
            return text;
        }
    }

    private void setDefaultText() {
        String text = super.getText();

        if (text == null || text.equals("")) {
            super.setText(label);
            setForeground(Color.GRAY);
        }
    }

    @Override
    public void setText(String t) {
        setForeground(initialColor);
        super.setText(t);
        setDefaultText();
    }


}
