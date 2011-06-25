package de.unisb.cs.st.evosuite.ui;

import java.util.List;

import org.uispec4j.Window;
import org.uispec4j.interception.handlers.InterceptionHandler;

import de.unisb.cs.st.evosuite.ui.model.UIState;
import de.unisb.cs.st.evosuite.ui.model.UIStateGraph;

abstract public class AbstractUIEnvironment {
	abstract public List<Window> getTargetableWindows();

	abstract public UIState waitGetNewState(UIStateGraph stateGraph);
	
	abstract public void registerModalWindowHandler(InterceptionHandler handler);
	abstract public void unregisterModalWindowHandler(InterceptionHandler handler);
	
	public void dispose() {}
}
