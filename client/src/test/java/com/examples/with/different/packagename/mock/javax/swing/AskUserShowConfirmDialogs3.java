package com.examples.with.different.packagename.mock.javax.swing;

import javax.swing.JOptionPane;

public class AskUserShowConfirmDialogs3 {

	public int askInput3() {
		int ret = JOptionPane.showConfirmDialog(null, "message", "title", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.ERROR_MESSAGE, null);
		if (ret == JOptionPane.OK_OPTION) {
			return 0;
		} else if (ret == JOptionPane.CANCEL_OPTION) {
			return 1;
		} else if (ret == JOptionPane.CLOSED_OPTION) {
			return 2;
		} else {
			return 3;
		}
	}

}
