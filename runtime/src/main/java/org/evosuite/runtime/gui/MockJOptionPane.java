package org.evosuite.runtime.gui;

import java.awt.Component;
import java.awt.HeadlessException;

import javax.swing.Icon;

import org.evosuite.runtime.util.JOptionPaneInputs;
import org.evosuite.runtime.util.JOptionPaneInputs.GUIAction;

/**
 * These methods replace those from javax.swing.JOptionPane. This class is used
 * when the REPLACE_GUI option is enabled.
 * 
 * @author galeotti
 *
 */
public abstract class MockJOptionPane {

	/**
	 * Replaces method javax.swing.JOptionPane.showMessageDialog(Component
	 * parentComponent, Object message);
	 * 
	 * @param parentComponent
	 * @param message
	 */
	public static void showMessageDialog(Component parentComponent, Object message) {
		/* do nothing */
	}

	/**
	 * Replaces method javax.swing.JOptionPane.showMessageDialog(Component
	 * parentComponent, Object message, String title, int messageType)
	 * 
	 * @param parentComponent
	 * @param message
	 * @param title
	 * @param messageType
	 */
	public static void showMessageDialog(Component parentComponent, Object message, String title, int messageType) {
		/* do nothing */
	}

	/**
	 * Replaces method javax.swing.JOptionPane.showMessageDialog(Component
	 * parentComponent, Object message, String title, int messageType, Icon
	 * icon)
	 * 
	 * @param parentComponent
	 * @param message
	 * @param title
	 * @param messageType
	 * @param icon
	 */
	public static void showMessageDialog(Component parentComponent, Object message, String title, int messageType,
			Icon icon) {
		/* do nothing */
	}

	/**
	 * Replaces method JOptionPane.showConfirmDialog(Component parentComponent,
	 * Object message)
	 * 
	 * @param parentComponent
	 * @param message
	 * @return JOptionPane.YES_OPTION, JOptionPane.NO_OPTION,
	 *         JOptionPane.CANCEL_OPTION and JOptionPane.CLOSED_OPTION
	 */
	public static int showConfirmDialog(Component parentComponent, Object message) throws HeadlessException {
		return showConfirmDialog(javax.swing.JOptionPane.YES_NO_CANCEL_OPTION);
	}

	private static int showConfirmDialog(int optionType) {

		switch (optionType) {
		case javax.swing.JOptionPane.DEFAULT_OPTION:
		case javax.swing.JOptionPane.YES_NO_CANCEL_OPTION: {
			return getInputYesNoCancel();
		}
		case javax.swing.JOptionPane.YES_NO_OPTION: {
			return getInputYesNo();
		}
		case javax.swing.JOptionPane.OK_CANCEL_OPTION: {
			return getInputOkCancel();
		}
		default:
			throw new IllegalStateException(
					"Option number " + optionType + " does not match any known JOptionPane option");
		}

	}

	private static int getInputOkCancel() {
		// first, we record that the SUT has issued a call
		// to JOptionPane.showConfirmDialog()
		JOptionPaneInputs.getInstance().addDialog(GUIAction.OK_CANCEL_SELECTION);

		// second, we check if an input is specified for that GUI stimulus
		if (JOptionPaneInputs.getInstance().containsOkCancel()) {
			// return the specified input
			final int str = JOptionPaneInputs.getInstance().dequeueOkCancel();
			return str;
		} else {
			// return 0 by default if no input was specified
			return 0;
		}
	}

	private static int getInputYesNo() {
		// first, we record that the SUT has issued a call
		// to JOptionPane.showConfirmDialog()
		JOptionPaneInputs.getInstance().addDialog(GUIAction.YES_NO_SELECTION);

		// second, we check if an input is specified for that GUI stimulus
		if (JOptionPaneInputs.getInstance().containsYesNo()) {
			// return the specified input
			final int str = JOptionPaneInputs.getInstance().dequeueYesNo();
			return str;
		} else {
			// return 0 by default if no input was specified
			return 0;
		}
	}

	public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType)
			throws HeadlessException {
		return showConfirmDialog(optionType);
	}

	public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType,
			int messageType) throws HeadlessException {
		return showConfirmDialog(optionType);
	}

	public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType,
			int messageType, Icon icon) throws HeadlessException {
		return showConfirmDialog(optionType);
	}

	public static String showInputDialog(Object message) throws HeadlessException {
		return getInputString();
	}

	private static String getInputString() {
		// first, we record that the SUT issued a call to
		// JOptionPane.showInputDialog()
		JOptionPaneInputs.getInstance().addDialog(GUIAction.STRING_INPUT);

		// second, we check if an input is specified for that GUI stimulus
		if (JOptionPaneInputs.getInstance().containsString()) {
			// return the specified input
			final String str = JOptionPaneInputs.getInstance().dequeueString();
			return str;
		} else {
			// return null by default if no input was specified
			return null;
		}
	}

	private static int getInputYesNoCancel() {
		// first, we record that the SUT has issued a call
		// to JOptionPane.showConfirmDialog()
		JOptionPaneInputs.getInstance().addDialog(GUIAction.YES_NO_CANCEL_SELECTION);

		// second, we check if an input is specified for that GUI stimulus
		if (JOptionPaneInputs.getInstance().containsYesNoCancel()) {
			// return the specified input
			final int str = JOptionPaneInputs.getInstance().dequeueYesNoCancel();
			return str;
		} else {
			// return 0 by default if no input was specified
			return 0;
		}
	}

	public static String showInputDialog(Object message, Object initialSelectionValue) {
		return getInputString();
	}

	public static String showInputDialog(Component parentComponent, Object message) throws HeadlessException {
		return getInputString();
	}

	public static String showInputDialog(Component parentComponent, Object message, Object initialSelectionValue) {
		return getInputString();
	}

	public static String showInputDialog(Component parentComponent, Object message, String title, int messageType)
			throws HeadlessException {
		return getInputString();
	}

	public static void showInternalMessageDialog(Component parentComponent, Object message) {
		/* do nothing */
	}

	public static void showInternalMessageDialog(Component parentComponent, Object message, String title,
			int messageType) {
		/* do nothing */
	}

	public static void showInternalMessageDialog(Component parentComponent, Object message, String title,
			int messageType, Icon icon) {
		/* do nothing */
	}

	public static int showInternalConfirmDialog(Component parentComponent, Object message) {
		return showConfirmDialog(javax.swing.JOptionPane.YES_NO_CANCEL_OPTION);
	}

	public static int showInternalConfirmDialog(Component parentComponent, Object message, String title,
			int optionType) {
		return showConfirmDialog(optionType);
	}

	public static int showInternalConfirmDialog(Component parentComponent, Object message, String title, int optionType,
			int messageType) {
		return showConfirmDialog(optionType);
	}

	public static int showInternalConfirmDialog(Component parentComponent, Object message, String title, int optionType,
			int messageType, Icon icon) {
		return showConfirmDialog(optionType);
	}

	public static String showInternalInputDialog(Component parentComponent, Object message) {
		return getInputString();
	}

	public static String showInternalInputDialog(Component parentComponent, Object message, String title,
			int messageType) {
		return getInputString();
	}

	public static int showInternalOptionDialog(Component parentComponent, Object message, String title, int optionType,
			int messageType, Icon icon, Object[] options, Object initialValue) throws HeadlessException {
		// TODO Complete the mocking of this method
		return 0;
	}

	public static int showOptionDialog(Component parentComponent, Object message, String title, int optionType,
			int messageType, Icon icon, Object[] options, Object initialValue) throws HeadlessException {
		// TODO Complete the mocking of this method
		return 0;
	}
	
	public static Object showInternalInputDialog(Component parentComponent, Object message, String title,
			int messageType, Icon icon, Object[] selectionValues, Object initialSelectionValue) {
		// TODO Complete the mocking of this method
		return null;
	}
	
	public static Object showInputDialog(Component parentComponent, Object message, String title, int messageType,
			Icon icon, Object[] selectionValues, Object initialSelectionValue) throws HeadlessException {
		// TODO Complete the mocking of this method
		return null;
	}

}