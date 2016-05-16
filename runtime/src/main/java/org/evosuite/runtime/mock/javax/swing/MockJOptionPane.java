package org.evosuite.runtime.mock.javax.swing;

import java.awt.Component;
import java.awt.HeadlessException;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.util.JOptionPaneInputs;
import org.evosuite.runtime.util.JOptionPaneInputs.GUIAction;

/**
 * These methods replace those from javax.swing.JOptionPane. This class is used
 * when the REPLACE_GUI option is enabled.
 * 
 * @author galeotti
 *
 */
public abstract class MockJOptionPane extends JOptionPane implements OverrideMock {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1531475063681545845L;

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
			return getInputYesNoCancelSelection();
		}
		case javax.swing.JOptionPane.YES_NO_OPTION: {
			return getInputYesNoSelection();
		}
		case javax.swing.JOptionPane.OK_CANCEL_OPTION: {
			return getInputOkCancelSelection();
		}
		default:
			throw new IllegalStateException(
					"Option number " + optionType + " does not match any known JOptionPane option");
		}

	}

	private static int getInputOkCancelSelection() {
		// first, we record that the SUT has issued a call
		// to JOptionPane.showConfirmDialog()
		JOptionPaneInputs.getInstance().addDialog(GUIAction.OK_CANCEL_SELECTION);

		// second, we check if an input is specified for that GUI stimulus
		if (JOptionPaneInputs.getInstance().containsOkCancelSelection()) {
			// return the specified input
			final int str = JOptionPaneInputs.getInstance().dequeueOkCancelSelection();
			return str;
		} else {
			// return -1 by default if no input was specified
			return JOptionPane.CLOSED_OPTION;
		}
	}

	private static int getInputYesNoSelection() {
		// first, we record that the SUT has issued a call
		// to JOptionPane.showConfirmDialog()
		JOptionPaneInputs.getInstance().addDialog(GUIAction.YES_NO_SELECTION);

		// second, we check if an input is specified for that GUI stimulus
		if (JOptionPaneInputs.getInstance().containsYesNoSelection()) {
			// return the specified input
			final int str = JOptionPaneInputs.getInstance().dequeueYesNoSelection();
			return str;
		} else {
			// return -1 by default if no input was specified
			return JOptionPane.CLOSED_OPTION;
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
		return getStringInput();
	}

	private static String getStringInput() {
		// first, we record that the SUT issued a call to
		// JOptionPane.showInputDialog()
		JOptionPaneInputs.getInstance().addDialog(GUIAction.STRING_INPUT);

		// second, we check if an input is specified for that GUI stimulus
		if (JOptionPaneInputs.getInstance().containsStringInput()) {
			// return the specified input
			final String str = JOptionPaneInputs.getInstance().dequeueStringInput();
			return str;
		} else {
			// return null by default if no input was specified
			return null;
		}
	}

	private static int getInputYesNoCancelSelection() {
		// first, we record that the SUT has issued a call
		// to JOptionPane.showConfirmDialog()
		JOptionPaneInputs.getInstance().addDialog(GUIAction.YES_NO_CANCEL_SELECTION);

		// second, we check if an input is specified for that GUI stimulus
		if (JOptionPaneInputs.getInstance().containsYesNoCancelSelection()) {
			// return the specified input
			final int str = JOptionPaneInputs.getInstance().dequeueYesNoCancelSelection();
			return str;
		} else {
			// return -1 by default if no input was specified
			return JOptionPane.CLOSED_OPTION;
		}
	}

	public static String showInputDialog(Object message, Object initialSelectionValue) {
		return getStringInput();
	}

	public static String showInputDialog(Component parentComponent, Object message) throws HeadlessException {
		return getStringInput();
	}

	public static String showInputDialog(Component parentComponent, Object message, Object initialSelectionValue) {
		return getStringInput();
	}

	public static String showInputDialog(Component parentComponent, Object message, String title, int messageType)
			throws HeadlessException {
		return getStringInput();
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
		return getStringInput();
	}

	public static String showInternalInputDialog(Component parentComponent, Object message, String title,
			int messageType) {
		return getStringInput();
	}

	public static int showInternalOptionDialog(Component parentComponent, Object message, String title, int optionType,
			int messageType, Icon icon, Object[] options, Object initialValue) throws HeadlessException {
		return getOptionSelectionInt(options == null, options == null ? 0 : options.length);
	}

	public static int showOptionDialog(Component parentComponent, Object message, String title, int optionType,
			int messageType, Icon icon, Object[] options, Object initialValue) throws HeadlessException {
		return getOptionSelectionInt(options == null, options == null ? 0 : options.length);
	}

	/**
	 * 
	 * @param optionsIsNull
	 *            if the option is null or not
	 * @param optionsLength
	 *            the length of the options (if non null)
	 * @return the index selection or -1 if CLOSED or 0 if no options
	 */
	private static int getOptionSelectionInt(final boolean optionsIsNull, final int optionsLength) {
		// first, we record that the SUT has issued a call
		// to JOptionPane.showOptionDialog()
		JOptionPaneInputs.getInstance().addDialog(GUIAction.OPTION_SELECTION);

		// second, we check if an input is specified for that GUI stimulus
		if (JOptionPaneInputs.getInstance().containsOptionSelection()) {
			// return the specified input
			final int selection = JOptionPaneInputs.getInstance().dequeueOptionSelection();
			if (selection < JOptionPane.CLOSED_OPTION) {
				// truncate lower
				return JOptionPane.CLOSED_OPTION;
			} else if (optionsIsNull) {
				// if no options, returns OK
				return JOptionPane.OK_OPTION;
			} else {
				if (selection >= optionsLength) {
					// truncate upper
					return optionsLength - 1;
				} else {
					return selection;
				}
			}
		} else {
			// return -1 by default if no input was specified
			return JOptionPane.CLOSED_OPTION;
		}
	}

	private static Object getOptionSelectionInt(final Object[] options) {
		// first, we record that the SUT has issued a call
		// to JOptionPane.showOptionDialog()
		JOptionPaneInputs.getInstance().addDialog(GUIAction.OPTION_SELECTION);

		// second, we check if an input is specified for that GUI stimulus
		if (JOptionPaneInputs.getInstance().containsOptionSelection()) {
			// return the specified input
			final int selection = JOptionPaneInputs.getInstance().dequeueOptionSelection();
			if (selection < 0 || options == null) {
				// truncate lower
				return null;
			} else {
				if (selection >= options.length) {
					// truncate upper
					return options[options.length - 1];
				} else {
					return options[selection];
				}
			}
		} else {
			// return null by default if no input was specified
			return null;
		}
	}

	public static Object showInternalInputDialog(Component parentComponent, Object message, String title,
			int messageType, Icon icon, Object[] options, Object initialSelectionValue) {
		return getOptionSelectionInt(options);
	}

	public static Object showInputDialog(Component parentComponent, Object message, String title, int messageType,
			Icon icon, Object[] options, Object initialSelectionValue) throws HeadlessException {
		return getOptionSelectionInt(options);
	}

}