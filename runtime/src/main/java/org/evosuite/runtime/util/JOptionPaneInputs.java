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

	public enum GUIAction {
		STRING_INPUT, YES_NO_SELECTION, YES_NO_CANCEL_SELECTION, OK_CANCEL_SELECTION
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
		this.stringInputs.clear();
		this.yesNoCancelSelections.clear();
		this.yesNoSelections.clear();
		this.okCancelSelections.clear();
	}

	/**
	 * Enqueues a string in the queue for input strings. These strings will be
	 * consumed by those GUI elements that required a string input
	 * 
	 * @param data
	 *            the string to be queued
	 */
	public static void enqueueInputString(String data) {
		getInstance().enqueueInputString0(data);
	}

	public static void enqueueYesNoCancelSelection(int selection) {
		getInstance().enqueueYesNoCancelSelection0(selection);
	}

	public static void enqueueYesNoSelection(int selection) {
		getInstance().enqueueYesNoSelection0(selection);
	}
	

	public static void enqueueOkCancelSelection(int selection) {
		getInstance().enqueueOkCancelSelection0(selection);
	}
	
	private void enqueueOkCancelSelection0(int selection) {
		this.okCancelSelections.add(selection);
	}

	private void enqueueYesNoSelection0(int selection) {
		this.yesNoSelections.add(selection);
	}

	private void enqueueYesNoCancelSelection0(int selection) {
		this.yesNoCancelSelections.add(selection);
	}

	/**
	 * Dequeues a string from the string input queue. If no string is in the
	 * queue, an IllegalStateException is signaled.
	 * 
	 * @return
	 */
	public String dequeueString() {
		if (stringInputs.isEmpty()) {
			throw new IllegalStateException("dequeueString() should not be invoked if no string is contained!");
		}
		return stringInputs.poll();
	}

	private final LinkedList<String> stringInputs = new LinkedList<String>();

	private void enqueueInputString0(String str) {
		stringInputs.add(str);
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
	public void addDialog(GUIAction dialogType) {
		switch (dialogType) {
		case STRING_INPUT: {
			hasStringDialogs = true;
		}
			break;
		case OK_CANCEL_SELECTION: {
			hasYesCancelDialogs = true;
		}
			break;
		case YES_NO_CANCEL_SELECTION: {
			hasYesNoCancelDialogs = true;
		}
			break;
		case YES_NO_SELECTION: {
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
	public boolean hasDialog(GUIAction dialogType) {
		switch (dialogType) {
		case STRING_INPUT: {
			return hasStringDialogs;
		}
		case OK_CANCEL_SELECTION: {
			return hasYesCancelDialogs;
		}
		case YES_NO_CANCEL_SELECTION: {
			return hasYesNoCancelDialogs;
		}
		case YES_NO_SELECTION: {
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
		return !stringInputs.isEmpty();
	}

	private final LinkedList<Integer> yesNoCancelSelections = new LinkedList<Integer>();

	public boolean containsYesNoCancel() {
		return !yesNoCancelSelections.isEmpty();
	}

	public int dequeueYesNoCancel() {
		if (yesNoCancelSelections.isEmpty()) {
			throw new IllegalStateException("The input queue for YES/NO/CANCEL selections is empty");
		}
		return yesNoCancelSelections.poll();
	}

	private final LinkedList<Integer> yesNoSelections = new LinkedList<Integer>();

	public boolean containsYesNo() {
		return !yesNoSelections.isEmpty();

	}

	public int dequeueYesNo() {
		if (yesNoSelections.isEmpty()) {
			throw new IllegalStateException("The input queue for YES/NO selections is empty");
		}
		return yesNoSelections.poll();
	}

	private final LinkedList<Integer> okCancelSelections = new LinkedList<Integer>();

	public boolean containsOkCancel() {
		return !okCancelSelections.isEmpty();

	}

	public int dequeueOkCancel() {
		if (okCancelSelections.isEmpty()) {
			throw new IllegalStateException("The input queue for OK/CANCEL selections is empty");
		}
		return okCancelSelections.poll();
	}
}
