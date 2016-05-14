package com.examples.with.different.packagename.mock.javax.swing;

import javax.swing.JOptionPane;

public class AskUserShowOptionDialog {


	public int showOptionDialog() {

		int ret = JOptionPane.showOptionDialog(null, "message0", "title0 ", JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE, null, new Object[] { "Hello", "Goodbye" }, "Goodbye");
		if (ret == -1) {
			return 0;
		} else if (ret == 0) {
			return 1;
		} else {
			return 3;
		}
	}
	
	public int showInternalOptionDialog() {

		int ret = JOptionPane.showInternalOptionDialog(null, "message0", "title0 ", JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE, null, new Object[] { "Hello", "Goodbye","Hallo" }, "Goodbye");
		if (ret == -1) {
			return 0;
		} else if (ret == 0) {
			return 1;
		} else if (ret == 1) {
			return 2;
		} else {
			return 3;
		}
	}
	
	public Object showInputDialog() {

		Object ret = JOptionPane.showInputDialog(null, "message0", "title0 ", JOptionPane.DEFAULT_OPTION,
				null, new Object[] { "Hello", "Goodbye","Hallo" }, "Goodbye");
		if (ret == null) {
			return null;
		} else if (ret.equals("Hello")) {
			return "Hello";
		} else if (ret.equals("Goodbye")) {
			return "Goodbye";
		} else {
			return null;
		}
	}
	
}
