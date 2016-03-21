package org.evosuite.coverage.epa;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.runtime.LoopCounter;
import org.evosuite.testcase.execution.EvosuiteError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class EPAMonitor {

	/**
	 * The EPA automata used to inspect coverage
	 */
	private final EPA automata;

	/**
	 * The observed transitions for each distinct object of the target class so
	 * far (at the test case level). We use identityHashCode to identify those
	 * transitions.
	 */
	private final IdentityHashMap<Object, LinkedList<EPATransition>> transitions = new IdentityHashMap<Object, LinkedList<EPATransition>>();

	private static EPAMonitor instance = null;

	private boolean isEnabled = true;

	public void setEnabled(boolean enabled) {
		this.isEnabled = enabled;
	}

	public boolean isMonitorEnabled() {
		return isEnabled;
	}

	private EPAMonitor(EPA automata) {
		if (automata == null) {
			throw new IllegalArgumentException("EPA XML Path cannot be null!");
		}
		this.automata = automata;
	}

	public static EPAMonitor getInstance() {
		if (instance == null) {
			if (Properties.EPA_XML_PATH == null) {
				throw new IllegalStateException("EPA_XML_PATH should be configured before creating EPAMonitor!");
			}
			try {
				final EPA automata = EPAFactory.buildEPA(Properties.EPA_XML_PATH);
				instance = new EPAMonitor(automata);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				throw new EvosuiteError(e);
			}
		}
		return instance;
	}

	public static void reset() {
		instance = null;
	}

	private static Logger logger = LoggerFactory.getLogger(EPAMonitor.class);

	public static void enteredMethod(String className, String fullMethodName, Object object) {
		logger.debug("Entering method " + className + "." + fullMethodName);
	}

	public static void leftMethod(String className, String fullMethodName, Object object) {
		logger.debug("Exiting method " + className + "." + fullMethodName);
		if (getInstance().isMonitorEnabled()) {
			getInstance().setEnabled(false); // disable call-back
			final boolean loopCounterIsActive = LoopCounter.getInstance().isActivated();
			LoopCounter.getInstance().setActive(false);
			try {
				if (fullMethodName.startsWith("<init>")) {
					getInstance().afterConstructor(className, fullMethodName, object);
				} else {
					getInstance().afterMethod(className, fullMethodName, object);
				}
			} finally {
				getInstance().setEnabled(true);
				LoopCounter.getInstance().setActive(loopCounterIsActive);
			}
		}
	}

	private void afterConstructor(String className, String fullMethodName, Object object) {
		try {
			// is the methodStmt defined as an EPA Action ?
			if (getEpaAction(fullMethodName) != null) {
				final String actionName = getEpaAction(fullMethodName);
				if (hasPreviousEpaState(object)) {
					final EPAState previousEpaState = getPreviousEpaState(object);
					throw new MalformedEPATraceException(
							"New object cannot have a previous EPA State: " + previousEpaState);
				}

				EPAState initialEpaState = this.automata.getInitialState();
				final EPAState currentEpaState = getCurrentState(object);
				final EPATransition transition = new EPATransition(initialEpaState, actionName, currentEpaState);
				this.appendNewEpaTransition(object, transition);
			}
		} catch (MalformedEPATraceException | InvocationTargetException e) {
			throw new EvosuiteError(e);
		}

	}

	private void afterMethod(String className, String fullMethodName, Object calleeObject) throws EvosuiteError {
		try {
			if (getEpaAction(fullMethodName) != null) {
				final String actionName = getEpaAction(fullMethodName);
				if (!hasPreviousEpaState(calleeObject)) {
					// this object should have been seen previously!
					throw new MalformedEPATraceException("Object has no previous EPA State!");
				}
				final EPAState previousEpaState = getPreviousEpaState(calleeObject);
				final EPAState currentEpaState = getCurrentState(calleeObject);
				final EPATransition transition = new EPATransition(previousEpaState, actionName, currentEpaState);
				this.appendNewEpaTransition(calleeObject, transition);
			}

		} catch (MalformedEPATraceException | InvocationTargetException e) {
			throw new EvosuiteError(e);
		}
	}

	/**
	 * Appends the transition at the end of the trace for the given object.
	 * 
	 * @param obj
	 * @param transition
	 */
	private void appendNewEpaTransition(Object obj, EPATransition transition) {
		if (!this.transitions.containsKey(obj)) {
			this.transitions.put(obj, new LinkedList<EPATransition>());
		}
		this.transitions.get(obj).add(transition);
	}

	/**
	 * In order to obtain the current state, we invoke all the EPA state methods
	 * within the object. We throw an exception if the object has an invalid
	 * state
	 * 
	 * @param calleeObject
	 *            the object instance
	 * @param scope
	 *            the current execution scope
	 * @return the current EPA state of the given object
	 * @throws MalformedEPATraceException
	 *             if object has multiple EPA states or no EPA state at all
	 * @throws InvocationTargetException
	 */
	private EPAState getCurrentState(Object calleeObject) throws MalformedEPATraceException, InvocationTargetException {
		EPAState currentState = null;
		for (EPAState epaState : this.automata.getStates()) {
			boolean executionResult = executeEpaStateMethod(epaState, calleeObject);
			if (executionResult == true) {
				if (currentState != null) {
					throw new MalformedEPATraceException("Object found in multiple EPA states: " + currentState
							+ " and " + epaState + " simultaneously");
				} else {
					currentState = epaState;
				}
			}
		}
		if (currentState == null) {
			throw new MalformedEPATraceException("Object has no EPA state!");
		}
		return currentState;
	}

	private boolean executeEpaStateMethod(EPAState epaState, Object calleeObject) throws InvocationTargetException {
		try {
			final Method method = EPAUtils.getEpaStateMethod(epaState, calleeObject.getClass());
			boolean isAccessible = method.isAccessible();
			method.setAccessible(true);
			Boolean result = (Boolean) method.invoke(calleeObject);
			method.setAccessible(isAccessible);
			return result.booleanValue();
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
			throw new EvosuiteError(e);
		}
	}

	/**
	 * Previous state is the destination state in the most recently added
	 * transition for the calleeObject.
	 * 
	 * @param calleeObject
	 * @return
	 */
	private EPAState getPreviousEpaState(Object obj) {
		if (!hasPreviousEpaState(obj)) {
			throw new IllegalStateException(
					"getPreviousEpaState() should not be invoked unless the callee Object has a stored previous state");
		}
		final EPATransition lastTransition = this.transitions.get(obj).getLast();
		final EPAState lastState = lastTransition.getDestinationState();
		return lastState;
	}

	/**
	 * Returns tru if the given object has
	 * 
	 * @param obj
	 * @return
	 */
	private boolean hasPreviousEpaState(Object obj) {
		return this.transitions.containsKey(obj);
	}

	/**
	 * Checks if a given method is defined as an EPA action in the EPA automata
	 * 
	 * @param fullMethodName
	 * @return
	 */
	private String getEpaAction(String fullMethodName) {
		if (this.automata.containsAction(fullMethodName)) {
			return fullMethodName;
		} else {
			return null;
		}
	}

	public Set<EPATrace> getTraces() {
		final HashSet<EPATrace> traces = new HashSet<EPATrace>();
		for (LinkedList<EPATransition> epaTransitionList : this.transitions.values()) {
			EPATrace trace = new EPATrace(new LinkedList<EPATransition>(epaTransitionList));
			traces.add(trace);
		}
		return traces;
	}

}
