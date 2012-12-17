package org.exsyst.run;

import java.util.List;

import org.uispec4j.Window;
import org.uispec4j.interception.handlers.InterceptionHandler;

import org.exsyst.model.states.UIState;
import org.exsyst.model.states.UIStateGraph;

abstract public class AbstractUIEnvironment {
	abstract public List<Window> getTargetableWindows();

	abstract public UIState waitGetNewState(UIStateGraph stateGraph);
	
	abstract public void registerModalWindowHandler(InterceptionHandler handler);
	abstract public void unregisterModalWindowHandler(InterceptionHandler handler);
	
	public void dispose() {}
}
