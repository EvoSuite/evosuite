package org.evosuite.coverage.epa;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.runtime.LoopCounter;
import org.evosuite.testcase.execution.EvosuiteError;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class EPAMonitor {

	private static final String INIT = "<init>";

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
		Class<?> targetClass;
		try {
			targetClass = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
			this.epaStatesToMethodMap = createEpaStateToMethodMap(automata, targetClass);
			this.methodToActionMap = createMethodToActionMap(automata, targetClass);
			this.constructorToActionMap = createConstructorToActionMap(automata, targetClass);
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			throw new EvosuiteError(e);
		}

	}

	private static Map<String, String> createConstructorToActionMap(EPA automata, Class<?> targetClass) {
		final Map<String, String> constructorToActionMap = new HashMap<String, String>();
		for (String actionName : automata.getActions()) {
			// search methods
			Set<Constructor<?>> constructors = EPAUtils.getEpaActionConstructors(actionName, targetClass);
			for (Constructor<?> constructor : constructors) {
				final String constructorName = INIT;
				final String constructorDescriptor = Type.getConstructorDescriptor(constructor);
				final String constructorFullName = constructorName + constructorDescriptor;
				constructorToActionMap.put(constructorFullName, actionName);
			}
		}
		return constructorToActionMap;

	}

	private static Map<String, String> createMethodToActionMap(EPA automata, Class<?> targetClass) {
		Map<String, String> methodToActionMap = new HashMap<String, String>();
		for (String actionName : automata.getActions()) {
			// search methods
			Set<Method> methods = EPAUtils.getEpaActionMethods(actionName, targetClass);
			for (Method method : methods) {
				final String methodName = method.getName();
				final String methodDescriptor = Type.getMethodDescriptor(method);
				final String methodFullName = methodName + methodDescriptor;
				methodToActionMap.put(methodFullName, actionName);
			}
		}
		return methodToActionMap;
	}

	private static Map<EPAState, Method> createEpaStateToMethodMap(EPA automata, Class<?> targetClass)
			throws NoSuchMethodException {
		final HashMap<EPAState, Method> epaStateMethods = new HashMap<EPAState, Method>();
		for (EPAState state : automata.getStates()) {
			if (automata.getInitialState().equals(state)) {
				continue; // ignore initial states (always false)
			}
			Method method = EPAUtils.getEpaStateMethod(state, targetClass);
			if (method == null) {
				throw new NoSuchMethodException("Boolean query method for state " + state + " was not found in class "
						+ targetClass.getName() + " or any superclass");
			}
			epaStateMethods.put(state, method);
		}
		return epaStateMethods;
	}

	private final Map<EPAState, Method> epaStatesToMethodMap;

	private final Map<String, String> methodToActionMap;

	private final Map<String, String> constructorToActionMap;

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
				if (fullMethodName.startsWith(INIT)) {
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

	private void afterConstructor(String className, String fullConstructorName, Object object) {
		try {
			// is the methodStmt defined as an EPA Action ?
			if (this.constructorToActionMap.containsKey(fullConstructorName)) {
				final String actionName = this.constructorToActionMap.get(fullConstructorName);
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
			if (this.methodToActionMap.containsKey(fullMethodName)) {
				final String actionName = this.methodToActionMap.get(fullMethodName);
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
			if (this.automata.getInitialState().equals(epaState)) {
				continue; // discard initial states (always false)
			}

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
			if (!this.epaStatesToMethodMap.containsKey(epaState)) {
				throw new NoSuchMethodException("Boolean query method for state " + epaState
						+ " was not found in class " + calleeObject.getClass().getName() + " or any superclass");
			}
			final Method method = this.epaStatesToMethodMap.get(epaState);
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

	public Set<EPATrace> getTraces() {
		final HashSet<EPATrace> traces = new HashSet<EPATrace>();
		for (LinkedList<EPATransition> epaTransitionList : this.transitions.values()) {
			EPATrace trace = new EPATrace(new LinkedList<EPATransition>(epaTransitionList));
			traces.add(trace);
		}
		return traces;
	}

}
