package de.unisb.cs.st.evosuite.ma.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import de.unisb.cs.st.evosuite.Properties;

/**
 * @author Yury Pavlov
 * 
 */
public class SettingsGUI extends JDialog {

	private static final long serialVersionUID = -5783288787954229512L;

	private final JPanel contentPanel = new JPanel();
	private JTextField deltaField;
	private JTextField iterField;
	private JCheckBox chckbxManualEditorActive;

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
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			chckbxManualEditorActive = new JCheckBox("Manual Editor active");
			chckbxManualEditorActive.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					setValueChanged();
				}
			});

			chckbxManualEditorActive.setBounds(71, 106, 169, 23);
			contentPanel.add(chckbxManualEditorActive);
		}
		{
			deltaField = new JFormattedTextField();
			deltaField.addCaretListener(new CaretListener() {
				public void caretUpdate(CaretEvent e) {
					setValueChanged();
				}
			});
			deltaField.setBounds(10, 31, 114, 19);
			contentPanel.add(deltaField);
			deltaField.setColumns(10);
		}
		{
			iterField = new JFormattedTextField();
			iterField.addCaretListener(new CaretListener() {
				public void caretUpdate(CaretEvent e) {
					setValueChanged();
				}
			});
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
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						String delta = deltaField.getText();
						String iter = iterField.getText();
						if (delta != "" && iter != "") {
							Properties.MIN_DELTA_COVERAGE = Double
									.parseDouble(delta);
							Properties.MAX_ITERATION = Integer.parseInt(iter);
							Properties.MA_ACTIVE = chckbxManualEditorActive
									.isSelected();
							setValueUnchanged();
						}
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Close");
				cancelButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						setVisible(false);
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}

		chckbxManualEditorActive.setSelected(Properties.MA_ACTIVE);
		deltaField.setText(String.valueOf(Properties.MIN_DELTA_COVERAGE));
		iterField.setText(String.valueOf(Properties.MAX_ITERATION));

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	private void setValueChanged() {
		setTitle("Settings*");
	}

	private void setValueUnchanged() {
		setTitle("Settings");
	}
}
