package com.examples.with.different.packagename.mock.javax.swing;

import javax.swing.JOptionPane;

public class JOptionPaneExample {

	public static void main(String[] args) {
		JOptionPane.showMessageDialog(null, "alert", "alert", JOptionPane.ERROR_MESSAGE);

		int ret_code0 = JOptionPane.showConfirmDialog(null, "choose one", "choose one", JOptionPane.YES_NO_OPTION);

		Object[] options = { "OK", "CANCEL" };
		int ret_code1 = JOptionPane.showOptionDialog(null, "Click OK to continue", "Warning",
				JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

		String inputValue = JOptionPane.showInputDialog("Please input a value");

		Object[] possibleValues = { "First", "Second", "Third" };
		Object selectedValue = JOptionPane.showInputDialog(null, "Choose one", "Input", JOptionPane.INFORMATION_MESSAGE,
				null, possibleValues, possibleValues[0]);
		
		Object[] possibleValues1 = { new Integer(0), new Integer(1), new Integer(3) };
		Object selectedValue1 = JOptionPane.showInputDialog(null, "Choose one", "Input", JOptionPane.INFORMATION_MESSAGE,
				null, possibleValues1, possibleValues1[0]);

	}
	
	
}
