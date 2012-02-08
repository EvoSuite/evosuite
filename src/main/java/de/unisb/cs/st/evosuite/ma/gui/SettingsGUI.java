package de.unisb.cs.st.evosuite.ma.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import de.unisb.cs.st.evosuite.Properties;

/**
 * @author Yury Pavlov
 * 
 */
public class SettingsGUI extends JDialog implements ActionListener {

	private static final long serialVersionUID = -5783288787954229512L;

	private final JPanel contentPanel = new JPanel();
	private JTextField deltaField;
	private JTextField iterField;
	private JCheckBox chckbxManualEditorActive;
	private JButton saveButton;

	/**
	 * Create the dialog.
	 * 
	 * @param frame
	 */
	public SettingsGUI(final JFrame frame) {
		super(frame);
		setModal(true);
		setAlwaysOnTop(true);
		setTitle("Settings");
		setBounds(100, 100, 300, 200);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setFocusable(false);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			chckbxManualEditorActive = new JCheckBox("Manual Editor active");
			chckbxManualEditorActive.setActionCommand("Check");
			chckbxManualEditorActive.addActionListener(this);
			chckbxManualEditorActive.setBounds(71, 106, 169, 23);
			contentPanel.add(chckbxManualEditorActive);
		}
		{
			deltaField = new JFormattedTextField();
			deltaField.setBounds(10, 31, 114, 19);
			contentPanel.add(deltaField);
			deltaField.setColumns(10);
		}
		{
			iterField = new JFormattedTextField();
			iterField.setBounds(10, 79, 114, 19);
			contentPanel.add(iterField);
			iterField.setColumns(10);
		}

		JLabel lblDelta = new JLabel("Delta:");
		lblDelta.setBounds(10, 12, 70, 15);
		contentPanel.add(lblDelta);

		JLabel lblNum = new JLabel("Number of Iterations:");
		lblNum.setBounds(10, 62, 169, 15);
		contentPanel.add(lblNum);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setFocusTraversalPolicyProvider(true);
			buttonPane.setFocusable(false);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				saveButton = new JButton("Save");
				saveButton.setFocusCycleRoot(true);
				saveButton.setFocusable(true);
				saveButton.setActionCommand("Save");
				buttonPane.add(saveButton);
				getRootPane().setDefaultButton(saveButton);
				saveButton.addActionListener(this);
			}
			{
				JButton closeButton = new JButton("Close");
				closeButton.setActionCommand("Close");
				buttonPane.add(closeButton);
				closeButton.addActionListener(this);
			}
		}

		chckbxManualEditorActive.setSelected(Properties.MA_ACTIVE);
		deltaField.setText(String.valueOf(Properties.MA_MIN_DELTA_COVERAGE));
		iterField.setText(String.valueOf(Properties.MA_MAX_ITERATIONS));

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if ("Save".equals(e.getActionCommand())) {
			String delta = deltaField.getText();
			String iter = iterField.getText();
			if (delta != "" && iter != "") {
				Properties.MA_MIN_DELTA_COVERAGE = Double.parseDouble(delta);
				Properties.MA_MAX_ITERATIONS = Integer.parseInt(iter);
				Properties.MA_ACTIVE = chckbxManualEditorActive.isSelected();
				setVisible(false);
				dispose();
			}
		} else if ("Close".equals(e.getActionCommand())) {
			setVisible(false);
			dispose();
		}

	}
	
}
