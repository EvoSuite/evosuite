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

	/**
	 * The type of GUI action that is fed to the JOptionPane mock
	 * 
	 * @author galeotti
	 *
	 */
	public enum GUIAction {
		// This action represents entering a unconstrained String
		STRING_INPUT,
		// Represents a YES/NO/CLOSED answer to a dialog
		YES_NO_SELECTION,
		// Represents a YES/NO/CANCEL/CLOSED answer to a dialog
		YES_NO_CANCEL_SELECTION,
		// Represents a OK/CANCEL/CLOSED answer to a dialog
		OK_CANCEL_SELECTION,
		// Represents a {ANY OPTION}/CLOSED answer to a dialog
		OPTION_SELECTION
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
		this.optionSelections.clear();
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

	public static void enqueueOptionSelection(int selection) {
		getInstance().enqueueOptionSelection0(selection);
	}

	private void enqueueOptionSelection0(int selection) {
		this.optionSelections.add(selection);
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
	public String dequeueStringInput() {
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
	private boolean hasOptionDialogs = false;

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
		case OPTION_SELECTION: {
			hasOptionDialogs = true;
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
		case OPTION_SELECTION: {
			return hasOptionDialogs;
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
		return hasStringDialogs || hasYesCancelDialogs || hasYesNoCancelDialogs || hasYesNoDialogs || hasOptionDialogs;
	}

	/**
	 * The string queue has a string
	 * 
	 * @return
	 */
	public boolean containsStringInput() {
		return !stringInputs.isEmpty();
	}

	private final LinkedList<Integer> yesNoCancelSelections = new LinkedList<Integer>();

	public boolean containsYesNoCancelSelection() {
		return !yesNoCancelSelections.isEmpty();
	}

	public int dequeueYesNoCancelSelection() {
		if (yesNoCancelSelections.isEmpty()) {
			throw new IllegalStateException(
					"The input queue for" + GUIAction.YES_NO_CANCEL_SELECTION + " selections is empty");
		}
		return yesNoCancelSelections.poll();
	}

	private final LinkedList<Integer> yesNoSelections = new LinkedList<Integer>();

	private final LinkedList<Integer> optionSelections = new LinkedList<Integer>();

	public boolean containsYesNoSelection() {
		return !yesNoSelections.isEmpty();

	}

	public int dequeueYesNoSelection() {
		if (yesNoSelections.isEmpty()) {
			throw new IllegalStateException(
					"The input queue for " + GUIAction.YES_NO_SELECTION + " selections is empty");
		}
		return yesNoSelections.poll();
	}

	private final LinkedList<Integer> okCancelSelections = new LinkedList<Integer>();

	public boolean containsOkCancelSelection() {
		return !okCancelSelections.isEmpty();

	}

	public int dequeueOkCancelSelection() {
		if (okCancelSelections.isEmpty()) {
			throw new IllegalStateException(
					"The input queue for " + GUIAction.OK_CANCEL_SELECTION + " selections is empty");
		}
		return okCancelSelections.poll();
	}

	public int dequeueOptionSelection() {
		if (optionSelections.isEmpty()) {
			throw new IllegalStateException(
					"The input queue for " + GUIAction.OPTION_SELECTION + " selections is empty");
		}
		return optionSelections.poll();
	}

	public boolean containsOptionSelection() {
		return !optionSelections.isEmpty();
	}
}
