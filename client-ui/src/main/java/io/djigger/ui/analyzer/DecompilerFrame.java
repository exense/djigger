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

import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.text.StringEscapeUtils;
import org.jd.core.v1.ClassFileToJavaSourceDecompiler;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.loader.LoaderException;
import org.jd.core.v1.api.printer.Printer;

import io.djigger.ui.Session;

@SuppressWarnings("serial")
public class DecompilerFrame extends JPanel implements HyperlinkListener {

	private Session main;
	private JFrame frame;
	private JEditorPane textArea;

	public DecompilerFrame(Session main, String classname) {
		super();
		this.main = main;

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Couldn't use system look and feel.");
		}

		frame = new JFrame("djigger - Decompiler " + classname);
		frame.setPreferredSize(new Dimension(1300, 700));

		textArea = new JEditorPane();
		textArea.setContentType("text/html");
		textArea.setEditable(false);
		textArea.addHyperlinkListener(this);

		final JScrollPane scrollpane = new JScrollPane(textArea);
		frame.add(scrollpane);
		frame.pack();
		frame.setVisible(true);

		byte[] classBytecode;
		try {
			classBytecode = main.getFacade().getClassBytecode(classname);

			Printer printer = getPrinter();
			Loader loader = getLoader(classBytecode, classname);

			ClassFileToJavaSourceDecompiler decompiler = new ClassFileToJavaSourceDecompiler();
			decompiler.decompile(loader, printer, classname.replaceAll("\\.", "/"));

			final String source = printer.toString();
			textArea.setText(source);
			// Scroll to the top (this has to be invoked after setting the text.
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					scrollpane.getVerticalScrollBar().setValue(0);
				}
			});

		} catch (Exception e1) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e1.printStackTrace(pw);
			textArea.setText("Error while loading/decompiling class:\n" + sw.toString());
		}

	}

	protected Loader getLoader(final byte[] bytecode, final String classname) {
		return new Loader() {
			@Override
			public byte[] load(String internalName) throws LoaderException {
				return bytecode;
			}

			@Override
			public boolean canLoad(String internalName) {

				return classname.equals(internalName);
			}
		};
	}

	protected Printer getPrinter() {
		return new Printer() {
			protected static final String TAB = "&nbsp;";
			protected static final String NEWLINE = "<br>";

			protected int indentationCount = 0;
			protected StringBuilder sb = new StringBuilder();

			@Override
			public String toString() {
				return sb.toString();
			}

			@Override
			public void start(int maxLineNumber, int majorVersion, int minorVersion) {
			}

			@Override
			public void end() {
			}

			@Override
			public void printText(String text) {
				sb.append(StringEscapeUtils.escapeHtml3(text));
			}

			@Override
			public void printNumericConstant(String constant) {
				sb.append(constant);
			}

			@Override
			public void printStringConstant(String constant, String ownerInternalName) {
				sb.append("<span style=\"color:rgb(128, 0, 128);\">" + constant + "</span>");
			}

			@Override
			public void printKeyword(String keyword) {
				sb.append("<span style=\"color:blue;\">" + keyword + "</span>");
			}

			@Override
			public void printDeclaration(int type, String internalTypeName, String name, String descriptor) {
				sb.append(name);
			}

			@Override
			public void printReference(int type, String internalTypeName, String name, String descriptor,
					String ownerInternalName) {
				if (type == 1) {
					sb.append("<a style=\"color: rgb(192, 0, 0)\" href=\"http://"
							+ internalTypeName.replaceAll("/", "\\.") + "\">");
				}
				sb.append(name);
				if (type == 1) {
					sb.append("</a>");
				}
			}

			@Override
			public void indent() {
				this.indentationCount++;
			}

			@Override
			public void unindent() {
				this.indentationCount--;
			}

			@Override
			public void startLine(int lineNumber) {
				for (int i = 0; i < indentationCount; i++)
					sb.append(TAB);
			}

			@Override
			public void endLine() {
				sb.append(NEWLINE);
			}

			@Override
			public void extraLine(int count) {
				while (count-- > 0)
					sb.append(NEWLINE);
			}

			@Override
			public void startMarker(int type) {
			}

			@Override
			public void endMarker(int type) {
			}
		};
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType().equals(EventType.ACTIVATED)) {
			new DecompilerFrame(main, e.getURL().getHost());
		}
	}
}
