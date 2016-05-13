package com.examples.with.different.packagename.mock.javax.swing;

import javax.swing.JOptionPane;

public class AskUserShowInternalConfirmDialogs0 {

	public int askInput0() {
		int ret = JOptionPane.showInternalConfirmDialog(null, "message");
		if (ret == JOptionPane.YES_OPTION) {
			return 0;
		} else if (ret == JOptionPane.NO_OPTION) {
			return 1;
		} else if (ret == JOptionPane.CANCEL_OPTION) {
			return 2;
		} else if (ret == JOptionPane.CLOSED_OPTION) {
			return 3;
		} else {
			return 4;
		}
	}

}
