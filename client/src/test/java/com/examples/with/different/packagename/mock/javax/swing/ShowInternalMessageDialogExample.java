package com.examples.with.different.packagename.mock.javax.swing;

import javax.swing.JOptionPane;

public class ShowInternalMessageDialogExample {

	public boolean showInternalMessageDialog(int x) {
		JOptionPane.showInternalMessageDialog(null, "message0");
		JOptionPane.showInternalMessageDialog(null, "message0", "title0", JOptionPane.ERROR_MESSAGE);
		JOptionPane.showInternalMessageDialog(null, "message0", "title0", JOptionPane.ERROR_MESSAGE, null);
		if (x == 0) {
			return true;
		} else {
			return false;
		}
	}

}
