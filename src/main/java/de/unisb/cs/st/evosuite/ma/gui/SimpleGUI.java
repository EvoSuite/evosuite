package de.unisb.cs.st.evosuite.ma.gui;

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

import de.unisb.cs.st.evosuite.ma.Editor;
import de.unisb.cs.st.evosuite.ma.IGUI;

/**
 * @author Yury Pavlov
 */
public class SimpleGUI implements IGUI {

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

		JPanel branchPanel = new JPanel();
		branchPanel.setBorder(new TitledBorder(null, "Branch Code",
				TitledBorder.LEFT, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_branchPanel = new GridBagConstraints();
		gbc_branchPanel.fill = GridBagConstraints.BOTH;
		gbc_branchPanel.insets = new Insets(0, 0, 5, 0);
		gbc_branchPanel.gridx = 0;
		gbc_branchPanel.gridy = 0;
		mainFrame.getContentPane().add(branchPanel, gbc_branchPanel);
		branchPanel.setLayout(new BoxLayout(branchPanel, BoxLayout.X_AXIS));

		JScrollPane scrollPane = new JScrollPane();
		branchPanel.add(scrollPane);
		
		JTextPane sourceCodeTextPane = new JTextPane();
		sourceCodeTextPane.setEditable(false);
		scrollPane.setViewportView(sourceCodeTextPane);

		int i = 0;
		String fomatSourceCode = new String();
		for (String tmpString : editor.getSourceCode()) {
			fomatSourceCode += (i++ + ":\t" + tmpString + "\n");
		}
		sourceCodeTextPane.setText(fomatSourceCode);

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

		JScrollPane scrollPane_1 = new JScrollPane();
		testPanel.add(scrollPane_1);

		final JEditorPane testEditorPane = new JEditorPane();
		scrollPane_1.setViewportView(testEditorPane);

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

		JButton btnPrevBranch = new JButton("Prev Branch");
		btnPrevBranch.setPreferredSize(new Dimension(120, 25));
		btnPrevBranch.setMinimumSize(new Dimension(120, 25));
		btnPrevBranch.setMaximumSize(new Dimension(120, 25));
		btnPrevBranch.setBounds(12, 43, 119, 25);
		controlPanel.add(btnPrevBranch);

		JButton btnNextTestButton = new JButton("Next Test");
		btnNextTestButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				testEditorPane.setText(editor.getNextTest().toCode());
			}
		});
		btnNextTestButton.setBounds(160, 12, 119, 25);
		controlPanel.add(btnNextTestButton);

		JButton btnNewButton_1 = new JButton("Next Branch");
		btnNewButton_1.setBounds(160, 43, 119, 25);
		controlPanel.add(btnNewButton_1);

		JButton btnNewTest = new JButton("New Test");
		btnNewTest.setBounds(305, 12, 119, 25);
		controlPanel.add(btnNewTest);

		JButton btnInsertTest = new JButton("Insert Test");
		btnInsertTest.setBounds(305, 43, 119, 25);
		controlPanel.add(btnInsertTest);

		JButton btnNewButton_2 = new JButton("Save");
		btnNewButton_2.setBounds(447, 12, 119, 25);
		controlPanel.add(btnNewButton_2);

		JButton btnQuit = new JButton("Quit");
		btnQuit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				mainFrame.setVisible(false);
				synchronized (lock) {
					lock.notifyAll();
				}
				mainFrame.dispose();
			}
		});
		btnQuit.setBounds(447, 43, 119, 25);
		controlPanel.add(btnQuit);

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
