package com.examples.with.different.packagename.mock.javax.swing;

import javax.swing.JOptionPane;

public class ShowInternalConfirmDialogExample {

	public ShowInternalConfirmDialogExample() {

	}

	public boolean showInternalConfirmDialog(int x) {
		JOptionPane.showInternalConfirmDialog(null, "message0");
		JOptionPane.showInternalConfirmDialog(null, "message0", "title0", JOptionPane.YES_NO_CANCEL_OPTION);
		JOptionPane.showInternalConfirmDialog(null, "message0", "title0", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.ERROR_MESSAGE);
		JOptionPane.showInternalConfirmDialog(null, "message0", "title0", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.ERROR_MESSAGE, null);
		if (x == 0) {
			return true;
		} else {
			return false;
		}
	}

}
