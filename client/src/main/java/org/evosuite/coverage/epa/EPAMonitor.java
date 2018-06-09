package org.evosuite.coverage.epa;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.runtime.LoopCounter;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * This monitor maintains the information regarding from all callbacks regarding
 * EPA trace information and construction.
 * 
 * @author galeotti
 *
 */
public class EPAMonitor {

	private static final String INIT = "<init>";

	/**
	 * The EPA automata used to inspect coverage
	 */
	private final EPA automata;

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


	/**
	 * Populates a mapping from constructor names to EPA Actions using the
	 * annotations on each Java constructor
	 * 
	 * @param automata
	 * @param targetClass
	 * @return
	 */
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

	/**
	 * Populates a mapping from method names to EPA Actions using the annotations on
	 * each Java method
	 * 
	 * @param automata
	 * @param targetClass
	 * @return
	 */
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

	/**
	 * Creates a mapping from EPA states to Java boolean queries using the @EpaState
	 * annotation
	 * 
	 * @param automata
	 * @param targetClass
	 * @return
	 * @throws NoSuchMethodException
	 */
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

	/**
	 * A mapping from EPA states to boolean query methods to check if the object is
	 * in the corresponding state.
	 */
	private final Map<EPAState, Method> epaStatesToMethodMap;

	/**
	 * A mapping from Java method names to EPA actions following the @EpaAction
	 * annotations on the target class.
	 */
	private final Map<String, String> methodToActionMap;

	/**
	 * A mapping from Java constructor names to EPA actions based on the @EpaAction
	 * annotations on the target class.
	 */
	private final Map<String, String> constructorToActionMap;

	private EPAState previousEpaState;

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

	private static boolean wasLoopCounterActive;

	private static boolean wasExecutionTracerEnabled;

	private static boolean wasTraceCallsEnabled;

	public static void enteredMethod(String className, String fullMethodName, Object object) {
		logger.debug("Entering method " + className + "." + fullMethodName);
		if (getInstance().isMonitorEnabled()) {
			disableCallBacks();
			try {
				if (fullMethodName.startsWith(INIT)) {
					getInstance().beforeConstructor(className, fullMethodName, object);
				} else {
					getInstance().beforeMethod(className, fullMethodName, object);
				}
			} catch (EvosuiteError e) {
				throw e;
			} finally {
				enableCallBacks();
			}
		}

	}

	private void beforeMethod(String className, String fullMethodName, Object calleeObject) {
		if (this.methodToActionMap.containsKey(fullMethodName)) {
			call_stack.push(className + "." + fullMethodName);
			try {
				previousEpaState = getCurrentState(calleeObject);
			} catch (MalformedEPATraceException e) {
				throw new EvosuiteError(e);
			}
		}
	}

	/**
	 * This set stores those objects that executed a method that is labelled as an
	 * EpaAction but the execution resulted in an exception thrown listed as a
	 * notEnabledException (i.e. those exceptions that result from executing actions
	 * that are not enabled)
	 */
	private final HashSet<Object> invalidCalleeObjects = new HashSet<Object>();

	private final Stack<String> call_stack = new Stack<>();

	private void beforeConstructor(String className, String fullMethodName, Object object) {
		if (this.constructorToActionMap.containsKey(fullMethodName)) {
			call_stack.push(className + "." + fullMethodName);
			previousEpaState = this.automata.getInitialState();
		}
	}

	public static void exitMethod(Exception exceptionToBeThrown, String className, String fullMethodName,
			Object object) {
		if (exceptionToBeThrown == null) {
			logger.debug("Exiting method " + className + "." + fullMethodName + " with no exception");
		} else {
			logger.debug("Exiting method " + className + "." + fullMethodName + " with exception of type "
					+ exceptionToBeThrown.getClass().getName());
		}

		if (getInstance().isMonitorEnabled()) {
			disableCallBacks();
			try {
				if (fullMethodName.startsWith(INIT)) {
					getInstance().afterConstructor(className, fullMethodName, object, exceptionToBeThrown);
				} else {
					getInstance().afterMethod(className, fullMethodName, object, exceptionToBeThrown);
				}
			} catch (EvosuiteError e) {
				throw e;
			} finally {
				enableCallBacks();
			}
		}
	}

	private static void enableCallBacks() {
		EPAMonitor.getInstance().setEnabled(true);
		LoopCounter.getInstance().setActive(wasLoopCounterActive);
		if (wasExecutionTracerEnabled) {
			ExecutionTracer.enable();
		}
		if (wasTraceCallsEnabled) {
			ExecutionTracer.enableTraceCalls();
		}
	}

	private static void disableCallBacks() {
		wasLoopCounterActive = LoopCounter.getInstance().isActivated();
		wasExecutionTracerEnabled = ExecutionTracer.isEnabled();
		wasTraceCallsEnabled = ExecutionTracer.isTraceCallsEnabled();

		LoopCounter.getInstance().setActive(false);
		ExecutionTracer.disable();
		ExecutionTracer.disableTraceCalls();
		EPAMonitor.getInstance().setEnabled(false);
	}

	private void afterConstructor(String className, String fullConstructorName, Object object,
			Exception exceptionToBeThrown) {
		try {
			// is the methodStmt defined as an EPA Action ?
			if (exceptionToBeThrown == null && this.constructorToActionMap.containsKey(fullConstructorName)) {

				String top = call_stack.pop();
				final String classNameAndFullConstructorName = className + "." + fullConstructorName;
				if (!top.equals(classNameAndFullConstructorName)) {
					throw new EvosuiteError("afterConstructor() for " + classNameAndFullConstructorName
							+ " but last call on stack was " + top);
				}

				final String actionName = this.constructorToActionMap.get(fullConstructorName);
				if (!getPreviousEpaState(object).equals(automata.getInitialState())) {
					final EPAState previousEpaState = getPreviousEpaState(object);
					throw new MalformedEPATraceException(
							"New object cannot have a previous EPA State different than initial: " + previousEpaState);
				}

				EPAState initialEpaState = getPreviousEpaState(object);
				final EPAState currentEpaState = getCurrentState(object);
				final EPATransition transition = new NormalEPATransition(initialEpaState, actionName, currentEpaState);
				this.appendNewEpaTransition(object, transition);
			}
		} catch (MalformedEPATraceException e) {
			throw new EvosuiteError(e);
		}

	}

	private void appendNewEpaTransition(Object object, EPATransition transition) {
		ExecutionTracer.getExecutionTracer().getTraceNoFinishCalls().appendNewEpaTransition(object, transition);
	}

	private void afterMethod(String className, String fullMethodName, Object calleeObject,
			Exception exceptionToBeThrown) throws EvosuiteError {
		// If method is not an action, then we don't consider it
		if (this.methodToActionMap.containsKey(fullMethodName)) {
			try {

				String top = call_stack.pop();
				final String classNameAndFullMethodName = className + "." + fullMethodName;
				if (!top.equals(classNameAndFullMethodName)) {
					throw new EvosuiteError(
							"afterMethod() for " + classNameAndFullMethodName + " but last call on stack was " + top);
				}

				if (invalidCalleeObjects.contains(calleeObject)) {
					// clean the previous EpaState associated with this object
					previousEpaState = null;
					// do not update any trace
					return;
				}
				if (!hasPreviousEpaState(calleeObject)) {
					// this object should have been seen previously!
					throw new MalformedEPATraceException(
							"Object has no previous EPA State! Class " + className + " action " + fullMethodName);
				}

				final String actionName = this.methodToActionMap.get(fullMethodName);

				final EPAState previousEpaState = getPreviousEpaState(calleeObject);
				final EPAState currentEpaState = getCurrentState(calleeObject);
				final EPATransition transition;
				if (exceptionToBeThrown == null) {
					transition = new NormalEPATransition(previousEpaState, actionName, currentEpaState);
				} else {
					String exceptionClassName = exceptionToBeThrown.getClass().getName();
					transition = new ExceptionalEPATransition(previousEpaState, actionName, currentEpaState,
							exceptionClassName);
				}
				this.appendNewEpaTransition(calleeObject, transition);

			} catch (MalformedEPATraceException e) {
				throw new EvosuiteError(e);
			}
		}
	}

	/**
	 * In order to obtain the current state, we invoke all the EPA state methods
	 * within the object. We throw an exception if the object has an invalid state
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
	private EPAState getCurrentState(Object calleeObject) throws MalformedEPATraceException {
		try {
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
			// if (currentState == null) {
			// throw new MalformedEPATraceException("Neither EPA state query has returned
			// true for this object!");
			// }
			if (currentState == null) {
				// No EPA state query has returned true for this object
				return EPAState.INVALID_OBJECT_STATE;
			} else {
				return currentState;
			}
		} catch (InvocationTargetException ex) {
			// An EPA state query has signalled an exception while executing
			return EPAState.INVALID_OBJECT_STATE;
		}
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
	 * Previous state is the destination state in the most recently added transition
	 * for the calleeObject.
	 * 
	 * @param calleeObject
	 * @return
	 */
	private EPAState getPreviousEpaState(Object obj) {
		return this.previousEpaState;
	}

	/**
	 * Returns tru if the given object has
	 * 
	 * @param obj
	 * @return
	 */
	private boolean hasPreviousEpaState(Object obj) {
		return this.previousEpaState != null;
	}

}
