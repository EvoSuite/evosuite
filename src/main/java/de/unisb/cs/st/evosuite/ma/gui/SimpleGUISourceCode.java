package de.unisb.cs.st.evosuite.ma.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

import de.unisb.cs.st.evosuite.ma.Editor;

/**
 * @author Yury Pavlov
 *
 */
public class SimpleGUISourceCode {
	private final static Color LIGHT_GREEN = new Color(200, 255, 200);
	private final static Color LIGHT_RED = new Color(255, 200, 200);
	private JFrame frmSourceCode;
	JTextPane sourceCodeTextPane = new NonWrappingTextPane();

	/**
	 * Launch the application.
	 */
	public void createWindow() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frmSourceCode.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SimpleGUISourceCode() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmSourceCode = new JFrame();
		frmSourceCode.setTitle("Source Code");
		frmSourceCode.setBounds(0, 0, 800, 600);
		frmSourceCode.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frmSourceCode.setExtendedState(Frame.MAXIMIZED_BOTH);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{792, 0};
		gridBagLayout.rowHeights = new int[]{10, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		frmSourceCode.getContentPane().setLayout(gridBagLayout);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		frmSourceCode.getContentPane().add(panel, gbc_panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	
		JScrollPane sourceCodeScrollPane = new JScrollPane();
		panel.add(sourceCodeScrollPane);
		
		sourceCodeScrollPane.setViewportView(sourceCodeTextPane);
		
		DefaultCaret caret = (DefaultCaret)sourceCodeTextPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
	}
	
	/**
	 * @param sourceCodeTextPane
	 * @param editor
	 * @param sourceCodeScrollPane
	 * 
	 */
	public void printSourceCode(Editor editor) {
		// Print Source code
		int i = 1;
		String formatSourceCode = new String();
		for (String tmpString : editor.getSourceCode()) {
			formatSourceCode += (i++ + ":\t" + tmpString + "\n");
		}
		sourceCodeTextPane.setText(formatSourceCode);

		// Set highlights for covered lines
		Highlighter hilite = new MyHighlighter();
		sourceCodeTextPane.setHighlighter(hilite);
		DefaultHighlightPainter coveredPainter = new DefaultHighlighter.DefaultHighlightPainter(
				LIGHT_GREEN);
		DefaultHighlightPainter coveredCurrentPainter = new DefaultHighlighter.DefaultHighlightPainter(
				LIGHT_RED);

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
				if (editor.getSuiteCoverage().contains(i) && !editor.getCurrentCoverage().contains(i)) {
					painter = coveredPainter;
					hilite.addHighlight(start, end + 1, painter);
				}
				if (editor.getCurrentCoverage().contains(i)) {
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
		this.frmSourceCode.setVisible(false);
		this.frmSourceCode.dispose();		
	}

}
