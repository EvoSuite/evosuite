package com.examples.with.different.packagename.mock.javax.swing;

import javax.swing.JOptionPane;

public class ShowMessageDialogExample {

	public boolean showMessageDialog0(int x) {
		JOptionPane.showMessageDialog(null, "alert");
		if (x == 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean showMessageDialog1(int x) {
		JOptionPane.showMessageDialog(null, "alert", "alert", JOptionPane.ERROR_MESSAGE);
		if (x == 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean showMessageDialog2(int x) {
		JOptionPane.showMessageDialog(null, "alert", "alert", JOptionPane.ERROR_MESSAGE, null);
		if (x == 0) {
			return true;
		} else {
			return false;
		}
	}

}
