package de.unisb.cs.st.evosuite.ui.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.uispec4j.*;
import org.uispec4j.interception.handlers.InterceptionHandler;
import org.uispec4j.interception.toolkit.UISpecDisplay;
import org.uispec4j.utils.TriggerRunner;

import de.unisb.cs.st.evosuite.ui.model.states.IllegalUIStateException;
import de.unisb.cs.st.evosuite.ui.run.AbstractUIEnvironment;
import de.unisb.cs.st.evosuite.ui.run.BoundUIAction;
import de.unisb.cs.st.evosuite.utils.SimpleCondition;

public abstract class UIAction<T extends UIComponent> implements Serializable {
	private static final long serialVersionUID = 1L;

	public static UIAction<AbstractButton> buttonClick = new ButtonClick();
	public static UIAction<MenuItem> menuClick = new MenuItemClick();
	
	public static List<UIAction<? extends UIComponent>> actionsForType(Class<? extends UIComponent> type) {
		List<UIAction<? extends UIComponent>> result = new LinkedList<UIAction<? extends UIComponent>>();
		
		if (AbstractButton.class.isAssignableFrom(type)) {
			result.add(buttonClick);
		}
		
		if (MenuItem.class.isAssignableFrom(type)) {
			result.add(menuClick);
		}
		
		if (Table.class.isAssignableFrom(type)) {
			result.add(TableClick.newLeftClick());
			result.add(TableClick.newRightClick());
		}
		
		return result;
	}
	
	abstract public void executeOn(AbstractUIEnvironment env, T component);

	public UIAction() {
		this.randomize();
	}
	
	public void randomize() {
		/* Default implementation does nothing */
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
