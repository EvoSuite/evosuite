package de.unisb.cs.st.evosuite.ui.model;

import org.uispec4j.MenuItem;

import de.unisb.cs.st.evosuite.ui.run.AbstractUIEnvironment;

final class MenuItemClick extends UIAction<MenuItem> {
	private static final long serialVersionUID = 1L;

	@Override
	public void executeOn(final AbstractUIEnvironment env, final MenuItem menuItem) {
		this.checkTarget(menuItem);

		this.run(env, new Runnable() {
			@Override
			public void run() {
				menuItem.click();
			}
		});
	}

	@Override
	public String toString() {
		return "MenuItemClick";
	}
}