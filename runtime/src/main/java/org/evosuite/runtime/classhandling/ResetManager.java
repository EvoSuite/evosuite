package org.evosuite.runtime.classhandling;

import java.util.LinkedList;
import java.util.List;

public class ResetManager {

	private static final ResetManager instance = new ResetManager();

	private boolean resetAllClasses = false;
	private boolean tracingIsEnabled = true;
	private boolean resetFinalFields = false;
	private final List<String> classInitializationOrder = new LinkedList<>();

	private ResetManager() {
	}

	public synchronized static ResetManager getInstance() {
		return instance;
	}

	public void clearManager() {
		resetAllClasses = false;
		tracingIsEnabled = true;
		classInitializationOrder.clear();;
		resetFinalFields = false;
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

	/**
	 * This method is added in the transformed bytecode
	 *
	 * @param className
	 */
	public static void exitClassInit(String className) {
		String classNameWithDots = className.replace("/", ".");

		if (getInstance().isTracingEnabled()) {
			getInstance().addClassInitialization(classNameWithDots);
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
