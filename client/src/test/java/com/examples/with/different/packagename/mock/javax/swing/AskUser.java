package com.examples.with.different.packagename.mock.javax.swing;

import javax.swing.JOptionPane;

public class AskUser {

	public boolean ask() {
		String message = JOptionPane.showInputDialog("message0");
		if (message == null) {
			return false;
		} else {
			return true;
		}
	}
}
