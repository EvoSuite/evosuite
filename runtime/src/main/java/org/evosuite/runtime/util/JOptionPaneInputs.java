package org.evosuite.runtime.util;

import java.util.LinkedList;

/**
 * This class is used by a Test Case to feed input values to the JOptionPane
 * mocking classes at runtime. It also stores if the SUT has issued a call to a
 * JOptionPane method.
 * 
 * @author galeotti
 *
 */
public class JOptionPaneInputs {

	public enum DialogType {
		STRING_INPUT, YES_NO_CONFIRM, YES_NO_CANCEL_CONFIRM, YES_CANCEL_CONFIRM
	}

	private JOptionPaneInputs() {
	}

	private static JOptionPaneInputs instance = null;

	public synchronized static JOptionPaneInputs getInstance() {
		if (instance == null) {
			instance = new JOptionPaneInputs();
		}
		return instance;
	}

	/**
	 * Resets the singleton
	 */
	public synchronized static void resetSingleton() {
		instance = null;
	}

	/**
	 * This method should clean all the input queues before test case execution
	 */
	public void initForTestCase() {
		strings.clear();
	}

	/**
	 * Enqueues a string in the queue for input strings. These strings will be
	 * consumed by those GUI elements that required a string input
	 * 
	 * @param data
	 *            the string to be queued
	 */
	public static void enqueueString(String data) {
		getInstance().enqueue(data);
	}

	/**
	 * Dequeues a string from the string input queue. If no string is in the
	 * queue, an IllegalStateException is signaled.
	 * 
	 * @return
	 */
	public String dequeueString() {
		if (strings.isEmpty()) {
			throw new IllegalStateException("dequeueString() should not be invoked if no string is contained!");
		}
		return strings.poll();
	}

	private final LinkedList<String> strings = new LinkedList<String>();

	private void enqueue(String str) {
		strings.add(str);
	}

	private boolean hasStringDialogs = false;
	private boolean hasYesCancelDialogs = false;
	private boolean hasYesNoCancelDialogs = false;
	private boolean hasYesNoDialogs = false;

	/**
	 * Report that the SUT has issued a call to a JOptionPane dialog
	 * 
	 * @param dialogType
	 *            the type of the JOptionPane dialog
	 */
	public void addDialog(DialogType dialogType) {
		switch (dialogType) {
		case STRING_INPUT: {
			hasStringDialogs = true;
		}
			break;
		case YES_CANCEL_CONFIRM: {
			hasYesCancelDialogs = true;
		}
			break;
		case YES_NO_CANCEL_CONFIRM: {
			hasYesNoCancelDialogs = true;
		}
			break;
		case YES_NO_CONFIRM: {
			hasYesNoDialogs = true;
		}
			break;
		default:
			throw new IllegalStateException("dialogType " + dialogType + " was not implemented");
		}

	}

	/**
	 * Returns if the SUT has issued a call to the corresponding dialog
	 * 
	 * @param dialogType
	 * @return
	 */
	public boolean hasDialog(DialogType dialogType) {
		switch (dialogType) {
		case STRING_INPUT: {
			return hasStringDialogs;
		}
		case YES_CANCEL_CONFIRM: {
			return hasYesCancelDialogs;
		}
		case YES_NO_CANCEL_CONFIRM: {
			return hasYesNoCancelDialogs;
		}
		case YES_NO_CONFIRM: {
			return hasYesNoDialogs;
		}
		default:
			throw new IllegalStateException("dialogType " + dialogType + " was not implemented");
		}
	}

	/**
	 * This method report if any JOptionPane dialog call was issued by the SUT
	 * 
	 * @return
	 */
	public boolean hasAnyDialog() {
		return hasStringDialogs || hasYesCancelDialogs || hasYesNoCancelDialogs || hasYesNoDialogs;
	}

	/**
	 * The string queue has a string
	 * 
	 * @return
	 */
	public boolean containsString() {
		return !strings.isEmpty();
	}
}
