package de.unisb.cs.st.evosuite.ma.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import de.unisb.cs.st.evosuite.ma.Editor;

/**
 * @author Yury Pavlov
 */
public class SimpleGUITestEditor implements IGUI {

	private final Object lock = new Object();

	private final JFrame mainFrame = new JFrame("MA Editor");

	private final JButton btnSaveTestCaseButton = new JButton("Save");

	private final SimpleGUISourceCode sourceCodeWindow = new SimpleGUISourceCode();

	private final TitledBorder editorTitledBorder = new TitledBorder(null,
			"Test Editor", TitledBorder.LEADING, TitledBorder.TOP, null, null);

	private final JPanel testPanel = new JPanel();

	/**
	 * @wbp.parser.entryPoint
	 */
	public void createMainWindow(final Editor editor) {
		sourceCodeWindow.createWindow();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		mainFrame.setAlwaysOnTop(true);
		mainFrame.setLocation(new Point(dim.width - 585, dim.height - 600));
		mainFrame.setSize(new Dimension(800, 600));

		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 592, 0 };
		gridBagLayout.rowHeights = new int[] { 320, 100, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		mainFrame.getContentPane().setLayout(gridBagLayout);

		sourceCodeWindow.printSourceCode(editor);

		testPanel.setBorder(editorTitledBorder);
		GridBagConstraints gbc_testPanel = new GridBagConstraints();
		gbc_testPanel.fill = GridBagConstraints.BOTH;
		gbc_testPanel.insets = new Insets(0, 0, 5, 0);
		gbc_testPanel.gridx = 0;
		gbc_testPanel.gridy = 0;
		mainFrame.getContentPane().add(testPanel, gbc_testPanel);
		testPanel.setLayout(new BoxLayout(testPanel, BoxLayout.X_AXIS));

		JScrollPane testScrollPane = new JScrollPane();
		testPanel.add(testScrollPane);

		JPanel panel = new JPanel();
		testScrollPane.setViewportView(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 44, 428, 0 };
		gbl_panel.rowHeights = new int[] { 418, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		final JTextPane linesTextPane = new JTextPane();
		linesTextPane.setEditable(false);
		GridBagConstraints gbc_linesTextPane = new GridBagConstraints();
		gbc_linesTextPane.insets = new Insets(0, 0, 0, 5);
		gbc_linesTextPane.fill = GridBagConstraints.BOTH;
		gbc_linesTextPane.gridx = 0;
		gbc_linesTextPane.gridy = 0;
		panel.add(linesTextPane, gbc_linesTextPane);

		final JEditorPane testEditorPane = new JEditorPane();
		testEditorPane.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent arg0) {
				updateLinesTextPane(testEditorPane, linesTextPane);
				setTestCaseChanged();
			}
		});
		testEditorPane.setAlignmentY(0.1f);
		testEditorPane.setAlignmentX(0.1f);
		GridBagConstraints gbc_testEditorPane = new GridBagConstraints();
		gbc_testEditorPane.fill = GridBagConstraints.BOTH;
		gbc_testEditorPane.gridx = 1;
		gbc_testEditorPane.gridy = 0;
		panel.add(testEditorPane, gbc_testEditorPane);

		// print first TC
		testEditorPane.setText(editor.getCurrentTestCase().getTestCase()
				.toCode());
		updateLinesTextPane(testEditorPane, linesTextPane);
		updateTitle(editor);

		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(null);
		controlPanel.setSize(new Dimension(40, 100));
		controlPanel.setLayout(null);
		GridBagConstraints gbc_controlPanel = new GridBagConstraints();
		gbc_controlPanel.fill = GridBagConstraints.BOTH;
		gbc_controlPanel.gridx = 0;
		gbc_controlPanel.gridy = 1;
		mainFrame.getContentPane().add(controlPanel, gbc_controlPanel);

		JButton btnPrevTestButton = new JButton("Prev Test");
		btnPrevTestButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				editor.prevTest();
				sourceCodeWindow.printSourceCode(editor);
				testEditorPane.setText(editor.getCurrentTestCase()
						.getTestCase().toCode());
				setTestCaseUnchanged();
				updateTitle(editor);
			}
		});

		btnPrevTestButton.setMinimumSize(new Dimension(120, 25));
		btnPrevTestButton.setMaximumSize(new Dimension(120, 25));
		btnPrevTestButton.setPreferredSize(new Dimension(120, 25));
		btnPrevTestButton.setBounds(12, 12, 119, 25);
		controlPanel.add(btnPrevTestButton);

		JButton btnNextTestButton = new JButton("Next Test");
		btnNextTestButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				editor.nextTest();
				sourceCodeWindow.printSourceCode(editor);
				testEditorPane.setText(editor.getCurrentTestCase()
						.getTestCase().toCode());
				setTestCaseUnchanged();
				updateTitle(editor);
			}
		});
		btnNextTestButton.setBounds(143, 12, 119, 25);
		controlPanel.add(btnNextTestButton);

		JButton btnDeleteTest = new JButton("Delete Test");
		btnDeleteTest.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				editor.deleteCurrentTestCase();
				sourceCodeWindow.printSourceCode(editor);
				testEditorPane.setText(editor.getCurrentTestCase()
						.getTestCase().toCode());
				setTestCaseUnchanged();
				updateTitle(editor);
			}
		});
		btnDeleteTest.setBounds(661, 43, 119, 25);
		controlPanel.add(btnDeleteTest);

		JButton btnNewTestButton = new JButton("New Test");
		btnNewTestButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				editor.createNewTestCase();
				testEditorPane.setText("");
				setTestCaseChanged();
				updateTitle(editor);
			}
		});
		btnNewTestButton.setBounds(274, 12, 119, 25);
		controlPanel.add(btnNewTestButton);

		JButton btnInsertTestButton = new JButton("Clone Test");
		btnInsertTestButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				editor.createNewTestCase();
				setTestCaseChanged();
				updateTitle(editor);
			}
		});
		btnInsertTestButton.setBounds(274, 43, 119, 25);
		controlPanel.add(btnInsertTestButton);

		btnSaveTestCaseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// new TestCase was created and inserted in population
				if (editor.saveTest(testEditorPane.getText())) {
					sourceCodeWindow.printSourceCode(editor);
					testEditorPane.setText(editor.getCurrentTestCase()
							.getTestCase().toCode());
					setTestCaseUnchanged();
					updateTitle(editor);
					// there is some problem in creating of TestCase
				} else {

				}
			}
		});
		btnSaveTestCaseButton.setBounds(405, 12, 119, 25);
		controlPanel.add(btnSaveTestCaseButton);

		JButton btnQuitButton = new JButton("Quit");
		btnQuitButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				sourceCodeWindow.close();
				mainFrame.setVisible(false);
				synchronized (lock) {
					lock.notifyAll();
				}
				mainFrame.dispose();
			}
		});
		btnQuitButton.setBounds(405, 43, 119, 25);
		controlPanel.add(btnQuitButton);

		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				sourceCodeWindow.close();
				mainFrame.setVisible(false);
				synchronized (lock) {
					lock.notifyAll();
				}
				mainFrame.dispose();
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

	private void setTestCaseChanged() {
		btnSaveTestCaseButton.setText("Save*");
	}

	private void setTestCaseUnchanged() {
		btnSaveTestCaseButton.setText("Save");
	}

	private void updateTitle(Editor editor) {
		editorTitledBorder.setTitle("Test Editor     " + (editor.getNumOfCurrntTest() + 1)
				+ " / " + editor.getNumOfTestCases() + "     Coverage: " + editor.getSuiteCoveratgeVal() + "%");
		testPanel.repaint();
	}

	private void updateLinesTextPane(JEditorPane testEditorPane,
			JTextPane linesTextPane) {
		try {
			Document doc = testEditorPane.getDocument();
			String text = doc.getText(0, doc.getLength());
			int start = 0;
			int end = 0;

			int i = 1;
			String numLines = "";

			while ((end = text.indexOf('\n', start)) >= 0) {
				numLines += i++ + ":\n";
				start = end + 1;
			}
			numLines += i + ":\n";

			linesTextPane.setText(numLines);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void showParseException(String message) {
		JOptionPane.showMessageDialog(mainFrame, message, "Parsing error",
				JOptionPane.ERROR_MESSAGE);
	}
}
