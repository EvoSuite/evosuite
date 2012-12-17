package org.exsyst.model;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.uispec4j.MenuItem;
import org.uispec4j.UIComponent;

import org.exsyst.run.AbstractUIEnvironment;

final class MenuItemClick extends UIAction<MenuItem> {
	private static final long serialVersionUID = 1L;
	private static final UIAction<MenuItem> instance = new MenuItemClick();

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

	public static void addActions(Class<?> type, List<UIAction<? extends UIComponent>> result) {
		if (!type.equals(JMenu.class) || (!type.equals(JPopupMenu.class))) {
			result.add(instance);
		}
	}
}
