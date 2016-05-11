package com.examples.with.different.packagename.mock.javax.swing;

import javax.swing.JOptionPane;

public class ShowInternalInputDialogExample {

	public int showInternalInputDialogs() {
		int count = 0;

		final String string0 = JOptionPane.showInternalInputDialog(null, "message0");
		if (string0 == null) {
			count++;
		}

		String string2 = JOptionPane.showInternalInputDialog(null, "message0", "title0", JOptionPane.ERROR_MESSAGE);
		if (string2 == null) {
			count++;
		}

		Object object0 = JOptionPane.showInternalInputDialog(null, "message0", "title0", JOptionPane.ERROR_MESSAGE,
				null, new Object[] { "val0", "val1" }, "val0");
		if (object0 == null) {
			count++;
		}

		return count;

	}
}
