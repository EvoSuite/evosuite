package de.unisb.cs.st.evosuite.ui.run;

import java.awt.Component;
import java.awt.Container;
import java.util.*;

import javax.swing.*;

import org.uispec4j.Panel;
import org.uispec4j.Spinner;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;
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
			
			return policy.getComponentAfter(this.container, this.currentComponent) != this.firstComponent;
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
	
	private static final class UIComponentFocusIterator implements Iterator<UIComponent> {
		private ComponentFocusIterator baseIterator;
		private Map<Component, UIComponent> map;

		public UIComponentFocusIterator(Panel p) {
			this.baseIterator = new ComponentFocusIterator(p.getAwtComponent());
			this.map = new IdentityHashMap<Component, UIComponent>();
			
			for (UIComponent uiComp : p.getUIComponents(ComponentMatcher.ALL)) {
				if (uiComp != null) {
					this.map.put(uiComp.getAwtComponent(), uiCompFor(uiComp));
				}
			}
		}

		@Override
		public boolean hasNext() {
			return this.baseIterator.hasNext();
		}

		@Override
		public UIComponent next() {
			return this.map.get(this.baseIterator.next());
		}

		@Override
		public void remove() {
			this.baseIterator.remove();
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
	
	public static Iterable<UIComponent> children(final Panel p) {
		// Special handling for popups
		if (policy.getFirstComponent(p.getAwtComponent()) == null) {			
			try {
				JPopupMenu popup = (JPopupMenu) p.findSwingComponent(popupMatcher);

				if (popup != null) {
					return menuItemsFromPopUp(popup);
				}
			} catch (Exception e) { /* OK */ }
		}

		return new Iterable<UIComponent>() {
			@Override
			public Iterator<UIComponent> iterator() {
				return new UIComponentFocusIterator(p);
			}
		};
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

	private static UIComponent uiCompFor(UIComponent uiComp) {
		if (uiComp == null) {
			return null;
		}
		
		Component comp = uiComp.getAwtComponent();
		Container parent = comp.getParent();
		Container parentParent = parent != null ? parent.getParent() : null;
		
		if ((comp instanceof JFormattedTextField) && parentParent != null && (parentParent instanceof JSpinner)) {
			return new Spinner((JSpinner) parentParent);
		}
		
		return uiComp;
	}
}
