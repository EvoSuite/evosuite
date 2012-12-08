package org.exsyst.run;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import org.evosuite.utils.Utils;
import org.uispec4j.Trigger;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;
import org.uispec4j.interception.handlers.InterceptionHandler;
import org.uispec4j.interception.toolkit.UISpecDisplay;

import org.exsyst.genetics.ActionSequence;
import org.exsyst.genetics.ActionSequence.ActionError;
import org.exsyst.model.DescriptorBoundUIAction;
import org.exsyst.model.UIActionTargetDescriptor;
import org.exsyst.model.states.AbstractUIState;
import org.exsyst.model.states.UIState;
import org.exsyst.model.states.UIStateGraph;

public class UIRunner implements InterceptionHandler {
	private static final int MAX_ACTION_COUNT = 1000;
	
	private UIStateGraph stateGraph = new UIStateGraph();
	private UIController controller;
	private UIEnvironment env;
	private ActionSequence actionSequence;
	private ActionSequence unsharpActionSequence;
	private CyclicBarrier barrier = null;

	private int actionCount = 0;
	private boolean firstWindow = true;
	private boolean finished;
	
	private static PrintStream transitionLog;

	static {
		//UISpec4J.init();
	}

	static {
		try {
			transitionLog = new PrintStream("transitionLog.txt");
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private UIRunner(UIStateGraph stateGraph, UIController controller, CyclicBarrier barrier) {
		this.stateGraph = stateGraph;
		this.controller = controller;
		this.env = new UIEnvironment(UISpecDisplay.instance(), this);
		this.barrier = barrier;
		
		if (!this.env.getWindows().isEmpty()) {
			throw new IllegalStateException("UIRunner.run() invoked with left-over windows");
		}
	}

	public static UIRunner run(UIStateGraph stateGraph, UIController controller, final Trigger mainMethodTrigger) {
		CyclicBarrier barrier = new CyclicBarrier(2);
		
		UIRunner result = new UIRunner(stateGraph, controller, barrier);

		try {
			mainMethodTrigger.run();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		// We don't want the process window logic to handle the first window
		// before we are finished running the main method trigger...
		while (true) {
			try {
				barrier.await();
				break;
			} catch (Throwable t) { /* OK */ }
		}

		return result;
	}

	@Override
	public void process(Window window) {
		// System.out.println("UIRunner::process: window = " + window.getAwtComponent() + ": isVisible = " + window.getAwtComponent().isVisible());

		if (this.finished) return;

		boolean wasFirstWindow;
		
		synchronized (this) {
			wasFirstWindow = this.firstWindow;
		}

		if (!window.isVisible().isTrue()) {
			int retryCount = 20;
		
			while (retryCount > 0 && !window.isVisible().isTrue()) {
				System.out.println("Waiting for window to turn visible...");

				Utils.sleepFor(1000);
				retryCount--;
			}
		} 
		 
		if (window.isVisible().isTrue()) {
			synchronized (this) {
				this.firstWindow = false;
			}
		
			if (wasFirstWindow) {
				while (true) {
					try {
						barrier.await();
						break;
					} catch (Throwable t) { /* OK */ }
				}

				UIState state = this.env.waitGetNewState(this.stateGraph);
				state.increaseTimesVisited();
			
				try {
					this.actionSequence = new ActionSequence(state);
					this.unsharpActionSequence = new ActionSequence(state);
	
					this.processState(state);
				// TODO: Would be nice to catch exceptions here and reraise them in finished()
				} finally {
					this.finished();
				}
			}
		}
	}

	private void processState(AbstractUIState state) {
		if (this.finished) return;
		
		this.controller.processState(this, state);
	}

	private void finished() {
		this.finished = true;
		this.env.dispose();
		this.controller.finished(this);
	}

	public void executeAction(AbstractUIState state, DescriptorBoundUIAction<?> action) {
		if (this.finished) return;

		if (this.actionCount > MAX_ACTION_COUNT) {
			System.out.println("UIRunner: Executed more than " + MAX_ACTION_COUNT + " actions, stopping.");
			return;
		}

		this.actionCount++;

		if (this.actionCount > 500) {
			System.out.println("UIRunner: new action count = " + this.actionCount);
		}
		
		//System.out.println("UIRunner::executeAction(): Before execute of " + action.shortString() + ": " + state.shortString());

		action.increaseTimesExecuted();
		state.execute(action, this.stateGraph, this.env);
		UIState newState = this.env.waitGetNewState(this.stateGraph);
		newState.increaseTimesVisited();

		// System.out.println("UIRunner::executeAction(): After execute of " + action.shortString() + ": " + newState.shortString());

		this.actionSequence.addAction(action, newState);
		this.unsharpActionSequence.repair();
		
		try {
			this.unsharpActionSequence.addAction(action);
		} catch (ActionError e) {
			this.unsharpActionSequence.addAction(action, newState);
		}

		transitionLog.println(String.format("%s: %s -> %s (%s)\n  %s\n  %s",
				newState.shortString(), action.shortString(), newState.shortString(),
				this.controller, this.actionSequence.shortString(),
				this.unsharpActionSequence.shortString()));	

		this.processState(newState);
	}

	public ActionSequence getUnsharpActionSequence() {
		return actionSequence;
	}

	public ActionSequence getActionSequence() {
		return actionSequence;
	}
	
	private Map<UIActionTargetDescriptor, Map<AbstractUIState, UIComponent>> resolveCache =
			new HashMap<UIActionTargetDescriptor, Map<AbstractUIState, UIComponent>>();
	
	private Map<AbstractUIState, UIComponent> cacheForActionTargetDescriptor(UIActionTargetDescriptor atd) {
		if (!this.resolveCache.containsKey(atd)) {
			this.resolveCache.put(atd, new HashMap<AbstractUIState, UIComponent>());
		}
		
		return this.resolveCache.get(atd);
	}
	
	public UIComponent resolve(UIActionTargetDescriptor atd) {		
		AbstractUIState state = this.actionSequence.getFinalState();
		Map<AbstractUIState, UIComponent> rMap = this.cacheForActionTargetDescriptor(atd);
		
		if (!rMap.containsKey(state)) {
			rMap.put(state, atd.resolve(this.env));
		}
		
		return rMap.get(state);
	}

	public AbstractUIState getCurrentState() {
		return this.finished ? null : this.actionSequence.getFinalState();
	}
}
