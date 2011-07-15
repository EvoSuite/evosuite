package de.unisb.cs.st.evosuite.ma.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

/*
 *  Track the movement of the Caret by painting a background line at the
 *  current caret position.
 */
public class LinePainter {

	public static void main(String[] args) {

		JTextPane t = new JTextPane();
		t.setSelectionColor(new Color(1.0f, 1.0f, 1.0f, 0.0f));
		Highlighter hilite = new MyHighlighter();
		t.setHighlighter(hilite);
		t.setText("Line1\nLine2\nLine3\nLine4\nLine5\n");

		DefaultHighlightPainter whitePainter = new DefaultHighlighter.DefaultHighlightPainter(
				Color.WHITE);
		DefaultHighlightPainter grayPainter = new DefaultHighlighter.DefaultHighlightPainter(
				Color.GRAY);

		try {
			Document doc = t.getDocument();
			String text = doc.getText(0, doc.getLength());
			int start = 0;
			int end = 0;

			boolean even = true;

			// look for newline char, and then toggle between white and gray
			// painters.
			while ((end = text.indexOf('\n', start)) >= 0) {
				even = !even;
				DefaultHighlightPainter painter = even ? grayPainter
						: whitePainter;
				hilite.addHighlight(start, end + 1, painter);
				start = end + 1;
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		JPanel p = new JPanel(new BorderLayout());
		p.add(t, BorderLayout.CENTER);
		JFrame f = new JFrame();
		f.add(p);
		f.setSize(100, 100);
		f.setVisible(true);
	}
}