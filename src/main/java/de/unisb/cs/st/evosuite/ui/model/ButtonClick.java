/**
 * 
 */
package de.unisb.cs.st.evosuite.ui.model;

import org.uispec4j.AbstractButton;

import de.unisb.cs.st.evosuite.ui.run.AbstractUIEnvironment;

final class ButtonClick extends UIAction<AbstractButton> {
	private static final long serialVersionUID = 1L;

	@Override
	public void executeOn(final AbstractUIEnvironment env, final AbstractButton button) {
		this.checkTarget(button);

		this.run(env, new Runnable() {
			@Override
			public void run() {
				button.click();
			}
		});

		// TriggerRunner.runInUISpecThread(button.triggerClick());

		// TriggerRunner.runInSwingThread(button.triggerClick());

		/*
		 * new Thread(new Runnable() {
		 * 
		 * @Override public void run() { try { button.click(); } finally {
		 * condition.signal(); } } }, "Helper thread for ButtonClick").start();
		 * 
		 * condition.awaitUninterruptibly();
		 */
	}

	@Override
	public String toString() {
		return "ButtonClick";
	}
}