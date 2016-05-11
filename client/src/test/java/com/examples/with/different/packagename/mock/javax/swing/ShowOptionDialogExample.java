package com.examples.with.different.packagename.mock.javax.swing;

import javax.swing.JOptionPane;

public class ShowOptionDialogExample {

	public boolean showOptionDialog() {

		int retval = JOptionPane.showOptionDialog(null, "message0", "title0", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.ERROR_MESSAGE, null, new Object[] { "Hello", "Goodbye" }, "Goodbye");
		if (retval == 0) {
			return true;
		} else {
			return false;
		}
	}
}
