package com.examples.with.different.packagename.mock.javax.swing;

import javax.swing.JOptionPane;

public class ShowInputDialogExample {

	public int showInputDialogs() {
		int count = 0;

		final String string3 = JOptionPane.showInputDialog("message0");
		if (string3 == null) {
			count++;
		}

		final String string0 = JOptionPane.showInputDialog(null, "message0");
		if (string0 == null) {
			count++;
		}

		String string4 = JOptionPane.showInputDialog("message0", "initialValue0");
		if (string4 == null) {
			count++;
		}

		String string1 = JOptionPane.showInputDialog(null, "message0", "initialValue0");
		if (string1 == null) {
			count++;
		}

		String string2 = JOptionPane.showInputDialog(null, "message0", "title0", JOptionPane.ERROR_MESSAGE);
		if (string2 == null) {
			count++;
		}

		Object object0 = JOptionPane.showInputDialog(null, "message0", "title0", JOptionPane.ERROR_MESSAGE, null,
				new Object[] { "val0", "val1" }, "val0");
		if (object0 == null) {
			count++;
		}

		return count;

	}
}
