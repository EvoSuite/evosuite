package com.examples.with.different.packagename.mock.javax.swing;

import org.evosuite.runtime.gui.JOptionPane;

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
