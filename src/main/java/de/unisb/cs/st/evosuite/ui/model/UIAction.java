package de.unisb.cs.st.evosuite.ui.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.uispec4j.AbstractButton;
import org.uispec4j.Trigger;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;
import org.uispec4j.interception.handlers.InterceptionHandler;
import org.uispec4j.interception.toolkit.UISpecDisplay;
import org.uispec4j.utils.TriggerRunner;

import de.unisb.cs.st.evosuite.ui.AbstractUIEnvironment;
import de.unisb.cs.st.evosuite.ui.BoundUIAction;
import de.unisb.cs.st.evosuite.utils.SimpleCondition;

public abstract class UIAction<T extends UIComponent> implements Serializable {
	private static final long serialVersionUID = 1L;

	public static UIAction<AbstractButton> buttonClick = new UIAction<AbstractButton>() {
		private static final long serialVersionUID = 1L;

		@Override
		public void executeOn(final AbstractUIEnvironment env, final AbstractButton button) {
			this.checkTarget(button);

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
						button.click();
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

//			TriggerRunner.runInUISpecThread(button.triggerClick());

			//TriggerRunner.runInSwingThread(button.triggerClick());
			
/*			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						button.click();
					} finally {
						condition.signal();
					}
				}
			}, "Helper thread for ButtonClick").start();
			
			condition.awaitUninterruptibly();*/
		}

		@Override
		public String toString() {
			return "ButtonClick";
		}
	};
	
	public static List<UIAction<? extends UIComponent>> actionsForType(Class<? extends UIComponent> type) {
		List<UIAction<? extends UIComponent>> result = new LinkedList<UIAction<? extends UIComponent>>();
		
		if (AbstractButton.class.isAssignableFrom(type)) {
			result.add(buttonClick);
		}
		
		return result;
	}
	
	abstract public void executeOn(AbstractUIEnvironment env, T component);

	protected void checkTarget(T target) {
		if (target == null) {
			throw new IllegalUIStateException("Tried to execute action " + this + " without a target");
		}
	}

	@SuppressWarnings("unchecked")
	public BoundUIAction<T> bind(UIComponent component) {
		return component == null ? null : new BoundUIAction<T>(this, (T) component);
	}
	
	public DescriptorBoundUIAction<T> bind(UIActionTargetDescriptor target) {
		return target == null ? null : new DescriptorBoundUIAction<T>(this, target);
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
