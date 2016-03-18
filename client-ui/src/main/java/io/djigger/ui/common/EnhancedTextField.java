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

package io.djigger.ui.common;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

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
		if(getText()==null) {
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
		if(text == null || text.equals("") ||
				text.equals(label)) {
			return null;
		} else {
			return text;
		}
	}

	private void setDefaultText() {
		String text = super.getText();

		if(text == null || text.equals("")) {
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
