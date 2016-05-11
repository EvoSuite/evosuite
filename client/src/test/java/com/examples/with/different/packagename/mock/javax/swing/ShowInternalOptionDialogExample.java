package com.examples.with.different.packagename.mock.javax.swing;

import javax.swing.JOptionPane;

public class ShowInternalOptionDialogExample {

	public boolean showInternalOptionDialog() {

		int retval = JOptionPane.showInternalOptionDialog(null, "message0", "title0", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.ERROR_MESSAGE, null, new Object[] { "Hello", "Goodbye" }, "Goodbye");
		if (retval == 0) {
			return true;
		} else {
			return false;
		}
	}
}
