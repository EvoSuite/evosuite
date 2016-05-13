package com.examples.with.different.packagename.mock.javax.swing;

import javax.swing.JOptionPane;

public class AskUserShowInternalConfirmDialogs1 {

	public int askInput1() {
		int ret = JOptionPane.showInternalConfirmDialog(null, "message", "title", JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.YES_OPTION) {
			return 0;
		} else if (ret == JOptionPane.NO_OPTION) {
			return 1;
		} else if (ret == JOptionPane.CLOSED_OPTION) {
			return 2;
		} else {
			return 3;
		}
	}

}
