package de.unisb.cs.st.evosuite.ma.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import de.unisb.cs.st.evosuite.ma.Editor;
import javax.swing.ScrollPaneConstants;

/**
 * @author Yury Pavlov
 */
public class SimpleGUI implements IGUI {
	private final static Color LIGHT_GREEN = new Color(200, 255, 200);
	protected final Object lock = new Object();

	/**
	 * @wbp.parser.entryPoint
	 */
	public void createWindow(final Editor editor) {
		final JFrame mainFrame = new JFrame("Editot");
		mainFrame.setLocation(new Point(20, 20));
		mainFrame.setSize(new Dimension(600, 800));

		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		mainFrame.setJMenuBar(menuBar);

		JMenu mnTests = new JMenu("Tests");
		menuBar.add(mnTests);

		JMenuItem mntmNew = new JMenuItem("New");
		mnTests.add(mntmNew);

		JMenuItem mntmNext = new JMenuItem("Next");
		mnTests.add(mntmNext);

		JMenuItem mntmPrev = new JMenuItem("Prev");
		mnTests.add(mntmPrev);

		JMenuItem mntmSave = new JMenuItem("Save");
		mnTests.add(mntmSave);

		JMenuItem mntmReset = new JMenuItem("Reset");
		mnTests.add(mntmReset);

		JMenu mnSourceCode = new JMenu("Source Code");
		menuBar.add(mnSourceCode);

		JMenuItem mntmNextBranch = new JMenuItem("Next Branch");
		mnSourceCode.add(mntmNextBranch);

		JMenuItem mntmPrevBranch = new JMenuItem("Prev Branch");
		mnSourceCode.add(mntmPrevBranch);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 592, 0 };
		gridBagLayout.rowHeights = new int[] { 320, 320, 100, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, 1.0, 0.0,
				Double.MIN_VALUE };
		mainFrame.getContentPane().setLayout(gridBagLayout);

		JPanel sourceCodePanel = new JPanel();
		sourceCodePanel.setBorder(new TitledBorder(null, "Branch Code",
				TitledBorder.LEFT, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_sourceCodePanel = new GridBagConstraints();
		gbc_sourceCodePanel.fill = GridBagConstraints.BOTH;
		gbc_sourceCodePanel.insets = new Insets(0, 0, 5, 0);
		gbc_sourceCodePanel.gridx = 0;
		gbc_sourceCodePanel.gridy = 0;
		mainFrame.getContentPane().add(sourceCodePanel, gbc_sourceCodePanel);
		sourceCodePanel.setLayout(new BoxLayout(sourceCodePanel, BoxLayout.X_AXIS));

		JScrollPane sourceCodeScrollPane = new JScrollPane();
		sourceCodePanel.add(sourceCodeScrollPane);

		JTextPane sourceCodeTextPane = new JTextPane();
		sourceCodeTextPane.setEditable(false);
		sourceCodeScrollPane.setViewportView(sourceCodeTextPane);

		// Print Source code
		int i = 1;
		String fomatSourceCode = new String();
		for (String tmpString : editor.getSourceCode()) {
			fomatSourceCode += (i++ + ":\t" + tmpString + "\n");
		}
		sourceCodeTextPane.setText(fomatSourceCode);

		// Set highlights for covered lines
		Highlighter hilite = new MyHighlighter();
		sourceCodeTextPane.setHighlighter(hilite);
		DefaultHighlightPainter coveredPainter = new DefaultHighlighter.DefaultHighlightPainter(
				LIGHT_GREEN);

		try {
			Document doc = sourceCodeTextPane.getDocument();
			String text = doc.getText(0, doc.getLength());
			int start = 0;
			int end = 0;

			// look for newline char, and then toggle between white and gray
			// painters.
			i = 1;
			while ((end = text.indexOf('\n', start)) >= 0) {
				if (editor.getCoverage().contains(i)) {
					DefaultHighlightPainter painter = coveredPainter;
					hilite.addHighlight(start, end + 1, painter);
				}
				start = end + 1;
				i++;
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		JPanel testPanel = new JPanel();
		testPanel.setBorder(new TitledBorder(null, "Test Editor",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_testPanel = new GridBagConstraints();
		gbc_testPanel.fill = GridBagConstraints.BOTH;
		gbc_testPanel.insets = new Insets(0, 0, 5, 0);
		gbc_testPanel.gridx = 0;
		gbc_testPanel.gridy = 1;
		mainFrame.getContentPane().add(testPanel, gbc_testPanel);
		testPanel.setLayout(new BoxLayout(testPanel, BoxLayout.X_AXIS));

		JScrollPane testScrollPane = new JScrollPane();
		testPanel.add(testScrollPane);

		final JEditorPane testEditorPane = new JEditorPane();
		testScrollPane.setViewportView(testEditorPane);

		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(null);
		controlPanel.setSize(new Dimension(40, 100));
		controlPanel.setLayout(null);
		GridBagConstraints gbc_controlPanel = new GridBagConstraints();
		gbc_controlPanel.fill = GridBagConstraints.BOTH;
		gbc_controlPanel.gridx = 0;
		gbc_controlPanel.gridy = 2;
		mainFrame.getContentPane().add(controlPanel, gbc_controlPanel);

		JButton btnPrevTestButton = new JButton("Prev Test");
		btnPrevTestButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				testEditorPane.setText(editor.getPrevTest().toCode());
			}
		});

		btnPrevTestButton.setMinimumSize(new Dimension(120, 25));
		btnPrevTestButton.setMaximumSize(new Dimension(120, 25));
		btnPrevTestButton.setPreferredSize(new Dimension(120, 25));
		btnPrevTestButton.setBounds(12, 12, 119, 25);
		controlPanel.add(btnPrevTestButton);

		JButton btnPrevBranchButton = new JButton("Prev Branch");
		btnPrevBranchButton.setPreferredSize(new Dimension(120, 25));
		btnPrevBranchButton.setMinimumSize(new Dimension(120, 25));
		btnPrevBranchButton.setMaximumSize(new Dimension(120, 25));
		btnPrevBranchButton.setBounds(12, 43, 119, 25);
		controlPanel.add(btnPrevBranchButton);

		JButton btnNextTestButton = new JButton("Next Test");
		btnNextTestButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				testEditorPane.setText(editor.getNextTest().toCode());
			}
		});
		btnNextTestButton.setBounds(160, 12, 119, 25);
		controlPanel.add(btnNextTestButton);

		JButton btnNextBranchButton = new JButton("Next Branch");
		btnNextBranchButton.setBounds(160, 43, 119, 25);
		controlPanel.add(btnNextBranchButton);

		JButton btnNewTestButton = new JButton("New Test");
		btnNewTestButton.setBounds(305, 12, 119, 25);
		controlPanel.add(btnNewTestButton);

		JButton btnInsertTestButton = new JButton("Insert Test");
		btnInsertTestButton.setBounds(305, 43, 119, 25);
		controlPanel.add(btnInsertTestButton);

		JButton btnSaveTestCaseButton = new JButton("Save");
		btnSaveTestCaseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				editor.parseTest(testEditorPane.getText());
			}
		});
		btnSaveTestCaseButton.setBounds(447, 12, 119, 25);
		controlPanel.add(btnSaveTestCaseButton);

		JButton btnQuitButton = new JButton("Quit");
		btnQuitButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				mainFrame.setVisible(false);
				synchronized (lock) {
					lock.notifyAll();
				}
				mainFrame.dispose();
			}
		});
		btnQuitButton.setBounds(447, 43, 119, 25);
		controlPanel.add(btnQuitButton);

		mainFrame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent arg0) {
				mainFrame.setVisible(false);
				synchronized (lock) {
					lock.notifyAll();
				}
			}

		});

		mainFrame.setVisible(true);

		synchronized (lock) {
			while (mainFrame.isVisible())
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}

	}

}
