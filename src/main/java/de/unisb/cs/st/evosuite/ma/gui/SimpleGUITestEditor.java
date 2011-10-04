package de.unisb.cs.st.evosuite.ma.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

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

	private final UndoManager undoManager = new UndoManager();

	private final JEditorPane testEditorPane = new JEditorPane();

	private Editor editor;

	/**
	 * @wbp.parser.entryPoint
	 */
	public void createMainWindow(final Editor editor) {
		this.editor = editor;
		sourceCodeWindow.createWindow();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		ActionListener menuListener = new MenuActionListener();

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

		testEditorPane.getDocument().addUndoableEditListener(undoManager);

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

		JButton btnPrevTestButton = new JButton("Prev test");
		btnPrevTestButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				prevTest();
			}
		});

		btnPrevTestButton.setMinimumSize(new Dimension(120, 25));
		btnPrevTestButton.setMaximumSize(new Dimension(120, 25));
		btnPrevTestButton.setPreferredSize(new Dimension(120, 25));
		btnPrevTestButton.setBounds(12, 12, 119, 25);
		controlPanel.add(btnPrevTestButton);

		JButton btnNextTestButton = new JButton("Next test");
		btnNextTestButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				nextTest();
			}
		});
		btnNextTestButton.setBounds(143, 12, 119, 25);
		controlPanel.add(btnNextTestButton);

		JButton btnDeleteTest = new JButton("Delete test");
		btnDeleteTest.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				deleteTest();
			}
		});
		btnDeleteTest.setBounds(661, 43, 119, 25);
		controlPanel.add(btnDeleteTest);

		JButton btnNewTestButton = new JButton("New test");
		btnNewTestButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				newTest();
			}
		});
		btnNewTestButton.setBounds(274, 12, 119, 25);
		controlPanel.add(btnNewTestButton);

		JButton btnCloneTestButton = new JButton("Clone test");
		btnCloneTestButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				cloneTest();
			}
		});
		btnCloneTestButton.setBounds(274, 43, 119, 25);
		controlPanel.add(btnCloneTestButton);

		btnSaveTestCaseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				saveTest();
			}
		});
		btnSaveTestCaseButton.setBounds(405, 12, 119, 25);
		controlPanel.add(btnSaveTestCaseButton);

		JButton btnQuitButton = new JButton("Quit");
		btnQuitButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				quit();
			}
		});
		btnQuitButton.setBounds(405, 43, 119, 25);
		controlPanel.add(btnQuitButton);

		JMenuBar menuBar = new JMenuBar();
		mainFrame.setJMenuBar(menuBar);

		JMenu mnTestCaseEditor = new JMenu("TestCase Editor");
		mnTestCaseEditor.setMnemonic(KeyEvent.VK_T);
		menuBar.add(mnTestCaseEditor);

		JMenuItem mntmPrevTest = new JMenuItem("Prev test", KeyEvent.VK_P);
		mntmPrevTest.addActionListener(menuListener);
		KeyStroke ctrlPKeyStroke = KeyStroke.getKeyStroke("control P");
		mntmPrevTest.setAccelerator(ctrlPKeyStroke);
		mnTestCaseEditor.add(mntmPrevTest);

		JMenuItem mntmNextTest = new JMenuItem("Next test", KeyEvent.VK_E);
		mntmNextTest.addActionListener(menuListener);
		KeyStroke ctrlEKeyStroke = KeyStroke.getKeyStroke("control E");
		mntmNextTest.setAccelerator(ctrlEKeyStroke);
		mnTestCaseEditor.add(mntmNextTest);

		mnTestCaseEditor.addSeparator();

		JMenuItem mntmNewTest = new JMenuItem("New test", KeyEvent.VK_N);
		mntmNewTest.addActionListener(menuListener);
		KeyStroke ctrlNKeyStroke = KeyStroke.getKeyStroke("control N");
		mntmNewTest.setAccelerator(ctrlNKeyStroke);
		mnTestCaseEditor.add(mntmNewTest);

		JMenuItem mntmCloneTest = new JMenuItem("Clone test", KeyEvent.VK_L);
		mntmCloneTest.addActionListener(menuListener);
		KeyStroke ctrlLKeyStroke = KeyStroke.getKeyStroke("control L");
		mntmCloneTest.setAccelerator(ctrlLKeyStroke);
		mnTestCaseEditor.add(mntmCloneTest);

		JMenuItem mntmSaveTest = new JMenuItem("Save", KeyEvent.VK_S);
		mntmSaveTest.addActionListener(menuListener);
		KeyStroke ctrlSKeyStroke = KeyStroke.getKeyStroke("control S");
		mntmSaveTest.setAccelerator(ctrlSKeyStroke);
		mnTestCaseEditor.add(mntmSaveTest);

		mnTestCaseEditor.addSeparator();

		JMenuItem mntmQuit = new JMenuItem("Quit", KeyEvent.VK_Q);
		mntmQuit.addActionListener(menuListener);
		KeyStroke ctrlQKeyStroke = KeyStroke.getKeyStroke("control Q");
		mntmQuit.setAccelerator(ctrlQKeyStroke);
		mnTestCaseEditor.add(mntmQuit);

		JMenu mnEditor = new JMenu("Editor");
		mnEditor.setMnemonic(KeyEvent.VK_E);
		menuBar.add(mnEditor);

		JMenuItem mntmCopy = new JMenuItem("Copy", KeyEvent.VK_C);
		mntmCopy.addActionListener(menuListener);
		KeyStroke ctrlCKeyStroke = KeyStroke.getKeyStroke("control C");
		mntmCopy.setAccelerator(ctrlCKeyStroke);
		mnEditor.add(mntmCopy);

		JMenuItem mntmCut = new JMenuItem("Cut", KeyEvent.VK_X);
		mntmCut.addActionListener(menuListener);
		KeyStroke ctrlXKeyStroke = KeyStroke.getKeyStroke("control X");
		mntmCut.setAccelerator(ctrlXKeyStroke);
		mnEditor.add(mntmCut);

		JMenuItem mntmPaste = new JMenuItem("Paste", KeyEvent.VK_V);
		mntmPaste.addActionListener(menuListener);
		KeyStroke ctrlVKeyStroke = KeyStroke.getKeyStroke("control V");
		mntmPaste.setAccelerator(ctrlVKeyStroke);
		mnEditor.add(mntmPaste);

		mnEditor.addSeparator();

		JMenuItem mntmUnDo = new JMenuItem("Undo", KeyEvent.VK_Z);
		mntmUnDo.addActionListener(menuListener);
		KeyStroke ctrlZKeyStroke = KeyStroke.getKeyStroke("control Z");
		mntmUnDo.setAccelerator(ctrlZKeyStroke);
		mnEditor.add(mntmUnDo);

		JMenuItem mntmReDo = new JMenuItem("Redo", KeyEvent.VK_Y);
		mntmReDo.addActionListener(menuListener);
		KeyStroke ctrlYKeyStroke = KeyStroke.getKeyStroke("control Y");
		mntmReDo.setAccelerator(ctrlYKeyStroke);
		mnEditor.add(mntmReDo);

		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				quit();
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
		editorTitledBorder.setTitle("Test Editor     "
				+ (editor.getNumOfCurrntTest() + 1) + " / "
				+ editor.getNumOfTestCases() + "     Coverage: "
				+ editor.getSuiteCoveratgeVal() + "%");
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

	public String showChooseFileMenu() {
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(mainFrame);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile().getName();
		}

		return null;
	}

	private void prevTest() {
		editor.prevTest();
		sourceCodeWindow.printSourceCode(editor);
		testEditorPane.setText(editor.getCurrentTestCase().getTestCase()
				.toCode());
		setTestCaseUnchanged();
		updateTitle(editor);
	}

	private void nextTest() {
		editor.nextTest();
		sourceCodeWindow.printSourceCode(editor);
		testEditorPane.setText(editor.getCurrentTestCase().getTestCase()
				.toCode());
		setTestCaseUnchanged();
		updateTitle(editor);
	}

	private void deleteTest() {
		editor.deleteCurrentTestCase();
		sourceCodeWindow.printSourceCode(editor);
		testEditorPane.setText(editor.getCurrentTestCase().getTestCase()
				.toCode());
		setTestCaseUnchanged();
		updateTitle(editor);
	}

	private void newTest() {
		editor.createNewTestCase();
		testEditorPane.setText("");
		setTestCaseChanged();
		updateTitle(editor);
	}

	private void cloneTest() {
		editor.createNewTestCase();
		setTestCaseChanged();
		updateTitle(editor);
	}

	private void saveTest() {
		if (editor.saveTest(testEditorPane.getText())) {
			sourceCodeWindow.printSourceCode(editor);
			testEditorPane.setText(editor.getCurrentTestCase().getTestCase()
					.toCode());
			setTestCaseUnchanged();
			updateTitle(editor);
		}
	}

	private void quit() {
		sourceCodeWindow.close();
		mainFrame.setVisible(false);
		synchronized (lock) {
			lock.notifyAll();
		}
		mainFrame.dispose();
	}

	private void undo() {
		if (undoManager.canUndo()) {
			undoManager.undo();
		}
	}

	private void redo() {
		if (undoManager.canRedo()) {
			undoManager.redo();
		}
	}

	class MenuActionListener implements ActionListener {
		public void actionPerformed(ActionEvent actionEvent) {
			System.out.println("Selected: " + actionEvent.getActionCommand());
			if (actionEvent.getActionCommand().equals("Prev test")) {
				prevTest();
			} else if (actionEvent.getActionCommand().equals("Next test")) {
				nextTest();
			} else if (actionEvent.getActionCommand().equals("Delete test")) {
				deleteTest();
			} else if (actionEvent.getActionCommand().equals("New test")) {
				newTest();
			} else if (actionEvent.getActionCommand().equals("Clone test")) {
				cloneTest();
			} else if (actionEvent.getActionCommand().equals("Save")) {
				saveTest();
			} else if (actionEvent.getActionCommand().equals("Quit")) {
				quit();
			} else if (actionEvent.getActionCommand().equals("Undo")) {
				undo();
			} else if (actionEvent.getActionCommand().equals("Redo")) {
				redo();
			}
		}
	}
}
