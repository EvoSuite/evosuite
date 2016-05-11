package org.evosuite.runtime.gui;

import java.awt.Component;
import java.awt.HeadlessException;

import javax.swing.Icon;

/**
 * These methods replace those from javax.swing.JOptionPane. This class is used
 * when the REPLACE_GUI option is enabled.
 * 
 * @author galeotti
 *
 */
public abstract class JOptionPane {

	/**
	 * Replaces method javax.swing.JOptionPane.showMessageDialog(Component
	 * parentComponent, Object message);
	 * 
	 * @param parentComponent
	 * @param message
	 */
	public static void showMessageDialog(Component parentComponent, Object message) {
		/* skip */
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
		/* skip */
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
		/* skip */
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
		return 0;
	}

	public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType)
			throws HeadlessException {
		return 0;
	}

	public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType,
			int messageType) throws HeadlessException {
		return 0;
	}

	public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType,
			int messageType, Icon icon) throws HeadlessException {
		return 0;
	}

	public static String showInputDialog(Object message) throws HeadlessException {
		return null;
	}

	public static String showInputDialog(Object message, Object initialSelectionValue) {
		return null;
	}

	public static String showInputDialog(Component parentComponent, Object message) throws HeadlessException {
		return null;
	}

	public static String showInputDialog(Component parentComponent, Object message, Object initialSelectionValue) {
		return null;
	}

	public static String showInputDialog(Component parentComponent, Object message, String title, int messageType)
			throws HeadlessException {
		return null;
	}

	public static Object showInputDialog(Component parentComponent, Object message, String title, int messageType,
			Icon icon, Object[] selectionValues, Object initialSelectionValue) throws HeadlessException {
		return null;
	}

	public static int showOptionDialog(Component parentComponent, Object message, String title, int optionType,
			int messageType, Icon icon, Object[] options, Object initialValue) throws HeadlessException {
		return 0;
	}

	public static void showInternalMessageDialog(Component parentComponent, Object message) {
	}

	public static void showInternalMessageDialog(Component parentComponent, Object message, String title,
			int messageType) {

	}

	public static void showInternalMessageDialog(Component parentComponent, Object message, String title,
			int messageType, Icon icon) {
	}
	
	public static int showInternalConfirmDialog(Component parentComponent, Object message) throws HeadlessException {
		return 0;
	}

	public static int showInternalConfirmDialog(Component parentComponent, Object message, String title, int optionType)
			throws HeadlessException {
		return 0;
	}

	public static int showInternalConfirmDialog(Component parentComponent, Object message, String title, int optionType,
			int messageType) throws HeadlessException {
		return 0;
	}

	public static int showInternalConfirmDialog(Component parentComponent, Object message, String title, int optionType,
			int messageType, Icon icon) throws HeadlessException {
		return 0;
	}

}