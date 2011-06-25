package de.unisb.cs.st.evosuite.ui;

import java.awt.Component;
import java.awt.Container;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.LayoutFocusTraversalPolicy;

import org.uispec4j.Panel;
import org.uispec4j.Spinner;
import org.uispec4j.UIComponent;
import org.uispec4j.finder.ComponentMatcher;

public abstract class FocusOrder {
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

	public static Iterable<Component> children(final Container c) {
		return new Iterable<Component>() {
			@Override
			public Iterator<Component> iterator() {
				return new ComponentFocusIterator(c);
			}
		};
	}
	
	public static Iterable<UIComponent> children(final Panel p) {
		return new Iterable<UIComponent>() {
			@Override
			public Iterator<UIComponent> iterator() {
				return new UIComponentFocusIterator(p);
			}
		};
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
