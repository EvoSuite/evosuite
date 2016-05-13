package com.examples.with.different.packagename.mock.javax.swing;

import javax.swing.JOptionPane;

public class AskUserShowInputDailogs {

	public boolean askInput0() {
		String message = JOptionPane.showInputDialog("message0");
		if (message == null) {
			return false;
		} else {
			return true;
		}
	}

	public boolean askInput1() {
		String message = JOptionPane.showInputDialog(null, "message0");
		if (message == null) {
			return false;
		} else {
			return true;
		}
	}

	public boolean askInput2() {
		String message = JOptionPane.showInputDialog("message0", "initialSelectionValue");
		if (message == null) {
			return false;
		} else {
			return true;
		}
	}

	public boolean askInput3() {
		String message = JOptionPane.showInputDialog(null, "message0", "initialSelectionValue0");
		if (message == null) {
			return false;
		} else {
			return true;
		}
	}

	public boolean askInput4() {
		String message = JOptionPane.showInputDialog(null, "messag0", "title0", javax.swing.JOptionPane.ERROR_MESSAGE);
		if (message == null) {
			return false;
		} else {
			return true;
		}
	}

	public boolean askInput5() {
		String message = JOptionPane.showInternalInputDialog(null, "message0");
		if (message == null) {
			return false;
		} else {
			return true;
		}
	}

	public boolean askInput6() {
		String message = JOptionPane.showInternalInputDialog(null, "messag0", "title0",
				javax.swing.JOptionPane.ERROR_MESSAGE);
		if (message == null) {
			return false;
		} else {
			return true;
		}
	}

}
