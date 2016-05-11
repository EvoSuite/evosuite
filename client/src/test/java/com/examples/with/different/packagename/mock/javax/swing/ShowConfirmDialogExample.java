package com.examples.with.different.packagename.mock.javax.swing;

import javax.swing.JOptionPane;

public class ShowConfirmDialogExample {

	public ShowConfirmDialogExample() {
		
	}
	
	public int showConfirmDialogs() {
		int count = 0;
		final int ret_code0 = JOptionPane.showConfirmDialog(null, "message0");
		if (ret_code0 == JOptionPane.YES_OPTION) {
			count++;
		} else {
			count--;			
		}

		final int ret_code1 = JOptionPane.showConfirmDialog(null, "message0", "title0",
				JOptionPane.YES_NO_CANCEL_OPTION);
		if (ret_code1 == JOptionPane.YES_OPTION) {
			count++;
		} else {
			count--;			
		}

		final int ret_code2 = JOptionPane.showConfirmDialog(null, "message0", "title0",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
		if (ret_code2 == JOptionPane.YES_OPTION) {
			count++;
		} else {
			count--;			
		}

		final int ret_code3 = JOptionPane.showConfirmDialog(null, "message0", "title0",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null);
		if (ret_code3 == JOptionPane.YES_OPTION) {
			count++;
		} else {
			count--;			
		}

		return count;
	}

}
