package org.exsyst.run;

import java.awt.Component;
import java.awt.Container;
import java.util.*;

import javax.swing.*;

import org.uispec4j.*;
import org.uispec4j.finder.ComponentMatcher;
import org.uispec4j.utils.UIComponentFactory;

public abstract class FocusOrder {
	private static final ComponentMatcher popupMatcher = new ComponentMatcher() {
		@Override
		public boolean matches(Component component) {
			return component instanceof JPopupMenu;
		}
	};

	private static final class ComponentFocusIterator implements Iterator<Component> {
		private Container container;
		private Component currentComponent;
		private Component firstComponent;
		private boolean isFirst = true;

		public ComponentFocusIterator(Container container) {
			this.container = container;
			this.currentComponent = null;
			this.firstComponent = policy.getFirstComponent(container);
		}

		@Override
		public boolean hasNext() {
			if (this.isFirst) {
				return policy.getFirstComponent(this.container) != null;
			}
			
			Component nextComp = policy.getComponentAfter(this.container, this.currentComponent);
			
			return nextComp != this.firstComponent && nextComp != null;
		}

		@Override
		public Component next() {
			if (!this.hasNext()) {
				throw new NoSuchElementException();
			}

			this.currentComponent = this.isFirst ? this.firstComponent : policy.getComponentAfter(this.container, this.currentComponent);
			
			this.isFirst = false;
			return this.currentComponent;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	

	private static final LayoutFocusTraversalPolicy policy = new LayoutFocusTraversalPolicy();

	public static boolean isPopUpWindow(Window w) {
		try {
			return w.findSwingComponent(popupMatcher) != null;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean isPopUpWindowEnabled(Window w) {
		if (!w.isEnabled().isTrue() || !w.isVisible().isTrue()) {
			return false;
		}
		
		return !menuItemsFromPopUp(w.getAwtComponent()).isEmpty();
	}
	
	public static Iterable<Component> children(final Container c) {
		return new Iterable<Component>() {
			@Override
			public Iterator<Component> iterator() {
				return new ComponentFocusIterator(c);
			}
		};
	}
	
	public static void addMenuItemsTo(List<UIComponent> componentList) {
		ArrayList<UIComponent> copyList = new ArrayList<UIComponent>(componentList);

		for (UIComponent uiComp : copyList) {
			Component awtComp = uiComp.getAwtComponent();
			
			if (awtComp instanceof MenuElement) {
				addMenuItemsOfTo((MenuElement) awtComp, componentList);
			}
		}
	}
	
	private static void addMenuItemsOfTo(MenuElement menuElmt, List<UIComponent> componentList) {
		//System.out.println("FocusOrder: Processing menuElmt " + menuElmt);
		
		componentList.add(UIComponentFactory.createUIComponent((Component) menuElmt));
		
		// Optimization: JMenuItems (except for JMenu)
		// are leafs and never have any submenu items
		if ((menuElmt instanceof JMenu) || !(menuElmt instanceof JMenuItem)) {
			for (MenuElement e : menuElmt.getSubElements()) {
				addMenuItemsOfTo(e, componentList);
			}
		}
	}

	public static Iterable<UIComponent> children(final Panel p) {
		List<UIComponent> result = new ArrayList<UIComponent>(Arrays.asList(p.getUIComponents(new ComponentMatcher() {
			@Override
			public boolean matches(Component component) {
				return !(component instanceof JLabel) && component.isEnabled() && component.isVisible();
			}
		})));
		
		while (result.remove(null));
					
		addMenuItemsTo(result);
		
		ListIterator<UIComponent> iter = result.listIterator();
		while (iter.hasNext()) {
			UIComponent current = iter.next();
			Component awtComp = current.getAwtComponent();
			
			if (awtComp instanceof JMenu || awtComp instanceof JPopupMenu) {
				iter.remove();
			}
		}
		
		return result;
	}
	
	private static List<UIComponent> menuItemsFromPopUp(Container popup) {
		List<UIComponent> result = new ArrayList<UIComponent>(popup.getComponentCount());
		addMenuItemsFromPopUpTo(result, popup);
		return result;
	}
	
	private static void addMenuItemsFromPopUpTo(List<UIComponent> result, Container popup) {
		for (Component c : popup.getComponents()) {
			if (c instanceof JMenuItem && c.isEnabled() && c.isVisible()) {
				result.add(UIComponentFactory.createUIComponent(c));
			}
			
			if (c instanceof Container) {
				addMenuItemsFromPopUpTo(result, (Container)c);
			}
		}
	}
}
