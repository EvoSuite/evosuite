package org.evosuite.reset;

import java.util.LinkedList;
import java.util.List;

public class ResetManager {

	private static ResetManager instance;

	public synchronized static ResetManager getInstance() {
		if (instance == null) {
			instance = new ResetManager();
		}
		return instance;
	}

	public void clearManager() {
		resetAllClasses = false;
		tracingIsEnabled = true;
		classInitializationOrder.clear();;
		resetFinalFields = false;
	}
	
	private boolean resetAllClasses = false;
	private boolean tracingIsEnabled = true;
	private final List<String> classInitializationOrder = new LinkedList<String>();
	private boolean resetFinalFields = false;

	private ResetManager() {
		
	}
	
	public boolean isTracingEnabled() {
		return tracingIsEnabled;
	}

	public void enableTracing() {
		tracingIsEnabled = true;
	}

	public void disableTracing() {
		tracingIsEnabled = false;
	}

	public static void exitClassInit(String className) {
		String classNameWithDots = className.replace("/", ".");

		ResetManager classInitOrder = getInstance();
		if (classInitOrder.isTracingEnabled()) {
			classInitOrder.addClassInitialization(classNameWithDots);
		}
	}

	private void addClassInitialization(String classNameWithDots) {
		if (!classInitializationOrder.contains(classNameWithDots)) {
			classInitializationOrder.add(classNameWithDots);
		}
	}
	
	public boolean getResetAllClasses() {
		return resetAllClasses;
	}
	
	public List<String> getClassResetOrder() {
		return classInitializationOrder;
	}

	public void setResetAllClasses(boolean b) {
		this.resetAllClasses = b;
	}

	public void setResetFinalFields(boolean b) {
		resetFinalFields = b;
	}
	public boolean getResetFinalFields() {
		return resetFinalFields;
	}

}
