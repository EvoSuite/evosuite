/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.ma.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

import jsyntaxpane.DefaultSyntaxKit;
import de.unisb.cs.st.evosuite.ma.Editor;

/**
 * @author Yury Pavlov
 * 
 */
public class SourceCodeGUI {

	private final static Color LIGHT_GREEN = new Color(200, 255, 200);

	private final static Color LIGHT_RED = new Color(255, 200, 200);

	private JFrame frmSourceCode;

	private JEditorPane sourceCodeTextPane;

	private Editor editor;

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @param editor
	 * @wbp.parser.entryPoint
	 */
	public void createWindow(Editor editor) {
		this.editor = editor;
		frmSourceCode = new JFrame();
		frmSourceCode.setTitle("Source Code");
		frmSourceCode.setBounds(0, 0, 800, 600);
		frmSourceCode.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frmSourceCode.setExtendedState(Frame.MAXIMIZED_BOTH);

		final Container cont = frmSourceCode.getContentPane();
		cont.setLayout(new BorderLayout());

		DefaultSyntaxKit.initKit();

		sourceCodeTextPane = new JEditorPane();
		sourceCodeTextPane.setEditable(false);
		JScrollPane sourceCodeScrollPane = new JScrollPane(sourceCodeTextPane);
		sourceCodeTextPane.setContentType("text/java");
		cont.add(sourceCodeScrollPane, BorderLayout.CENTER);
		cont.doLayout();

		DefaultCaret caret = (DefaultCaret) sourceCodeTextPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

		printSourceCode();
		frmSourceCode.setVisible(true);
	}

	/**
	 * @param sourceCodeTextPane
	 * @param editor
	 * @param sourceCodeScrollPane
	 * 
	 */
	public void printSourceCode() {
		// Print Source code
		int i = 1;
		String formatSourceCode = new String();
		for (String tmpString : editor.getSourceCode()) {
			formatSourceCode += (tmpString + "\n");
		}

		sourceCodeTextPane.setText(formatSourceCode);

		// Set highlights for covered lines
		Highlighter hilite = new MyHighlighter();
		sourceCodeTextPane.setHighlighter(hilite);
		DefaultHighlightPainter coveredPainter = new DefaultHighlighter.DefaultHighlightPainter(LIGHT_GREEN);
		DefaultHighlightPainter coveredCurrentPainter = new DefaultHighlighter.DefaultHighlightPainter(LIGHT_RED);

		try {
			Document doc = sourceCodeTextPane.getDocument();
			String text = doc.getText(0, doc.getLength());
			int start = 0;
			int end = 0;

			// look for newline char, and then toggle between white, green or
			// red painters.
			i = 1;
			DefaultHighlightPainter painter;
			while ((end = text.indexOf('\n', start)) >= 0) {
				if (editor.getSuiteCoveredLines().contains(i) && !editor.getCurrCoverage().contains(i)) {
					painter = coveredPainter;
					hilite.addHighlight(start, end + 1, painter);
				}
				if (editor.getCurrCoverage().contains(i)) {
					painter = coveredCurrentPainter;
					hilite.addHighlight(start, end + 1, painter);
				}
				start = end + 1;
				i++;
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void close() {
		if (frmSourceCode != null) {
			frmSourceCode.setVisible(false);
			frmSourceCode.dispose();
		}
	}

}
