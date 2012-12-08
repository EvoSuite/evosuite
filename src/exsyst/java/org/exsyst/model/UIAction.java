package org.exsyst.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.evosuite.utils.SimpleCondition;
import org.uispec4j.*;
import org.uispec4j.interception.handlers.InterceptionHandler;
import org.uispec4j.interception.toolkit.UISpecDisplay;
import org.uispec4j.utils.TriggerRunner;

import org.exsyst.model.states.IllegalUIStateException;
import org.exsyst.run.AbstractUIEnvironment;
import org.exsyst.run.BoundUIAction;

public abstract class UIAction<T extends UIComponent> implements Serializable {
	private static final long serialVersionUID = 1L;

	public static List<UIAction<? extends UIComponent>> actionsForDescriptor(WindowlessUIActionTargetDescriptor targetDescriptor) {
		List<UIAction<? extends UIComponent>> result = new LinkedList<UIAction<? extends UIComponent>>();
		Class<?> type = targetDescriptor.getType();
		
		if (AbstractButton.class.isAssignableFrom(type)) {
			ButtonClick.addActions(result);
		}
		
		if (MenuItem.class.isAssignableFrom(type)) {
			MenuItemClick.addActions(type, result);
		}
		
		if (Table.class.isAssignableFrom(type)) {
			TableClick.addActions(result);
		}
		
		if (TextBox.class.isAssignableFrom(type)) {
			EnterText.addActions(result);
		}
		
		if (ComboBox.class.isAssignableFrom(type)) {
			ComboBoxSelect.addActions(targetDescriptor, result);
		}
		
		if (ListBox.class.isAssignableFrom(type)) {
			ListClick.addActions(result);
		}
		
		if (Tree.class.isAssignableFrom(type)) {
			TreeClick.addActions(result);
		}
		
		return result;
	}
	
	abstract public void executeOn(AbstractUIEnvironment env, T component);

	public UIAction() {
		this.randomize();
	}
	
	public boolean randomize() {
		/* Default implementation does nothing */
		return false;
	}
	
	protected void checkTarget(T target) {
		if (target == null) {
			throw new IllegalUIStateException("Tried to execute action " + this + " without a target");
		}
	}
	
	protected void run(final AbstractUIEnvironment env, final Runnable runnable) {
		final SimpleCondition condition = new SimpleCondition();

		TriggerRunner.runInUISpecThread(new Trigger() {
			@Override
			public void run() throws Exception {
				InterceptionHandler handler = new InterceptionHandler() {
					@Override
					public void process(Window window) {
						condition.signal();
					}
				};
				
				env.registerModalWindowHandler(handler);

				try {
					runnable.run();
				} finally {
					if (!condition.wasSignaled()) {
						env.unregisterModalWindowHandler(handler);
						condition.signal();
					}
				}
			}
		});
		
		condition.awaitUninterruptibly();
		
		UISpecDisplay.instance().rethrowIfNeeded();	
	}

	@SuppressWarnings("unchecked")
	public BoundUIAction<T> bind(UIComponent component) {
		return component == null ? null : new BoundUIAction<T>(this, (T) component);
	}
	
	public DescriptorBoundUIAction<T> bind(UIActionTargetDescriptor target) {
		return target == null ? null : new DescriptorBoundUIAction<T>(this, target);
	}	

	public String graphVizString() {
		return this.toString();
	}
	
	/**
	 * Default implementation comparing own class to other object's class.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		
		return obj.getClass().equals(this.getClass());
	}

	/**
	 * Default implementation returning hash code of own class.
	 */
	@Override
	public int hashCode() {
		return this.getClass().hashCode();
	}
}
