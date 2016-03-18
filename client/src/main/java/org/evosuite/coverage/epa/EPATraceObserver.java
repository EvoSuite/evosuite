package org.evosuite.coverage.epa;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;

public class EPATraceObserver extends ExecutionObserver {

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

	public EPATraceObserver(EPA automata) {
		this.automata = automata;
	}

	// where do I get the callObjectIDs? I could use identityHashCode instead
	// where do I get the EPA actions and EPA states? the EPA is defined in the
	// constructor
	// where do I store the EPA traces?

	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeStatement(Statement statement, Scope scope) {
		// do nothing
	}

	/**
	 * Appends a new EPA transition if necessary.
	 * 
	 * @param statement
	 * @param scope
	 * @param exception
	 */
	@Override
	public void afterStatement(Statement statement, Scope scope, Throwable exception) {
		if (exception != null) {
			// discard transition that led to exception
			return;
		}
		if (statement instanceof MethodStatement) {
			MethodStatement methodStmt = (MethodStatement) statement;
			afterMethodStatement(methodStmt, scope);
		} else if (statement instanceof ConstructorStatement) {
			ConstructorStatement constructorStatement = (ConstructorStatement) statement;
			afterConstructorStatement(constructorStatement, scope);
		} else {
			// we do not monitor any other statement except constructor and
			// method statements
		}

	}

	private void afterConstructorStatement(ConstructorStatement constructorStatement, Scope scope) {
		try {
			Object newObject = constructorStatement.getReturnValue().getObject(scope);
			if (isObjectInstanceOfTargetClass(newObject)) {
				// is the methodStmt defined as an EPA Action ?
				final String actionName = constructorStatement.getMethodName() + constructorStatement.getDescriptor();
				if (isEpaAction(actionName)) {

					if (hasPreviousEpaState(newObject)) {
						final EPAState previousEpaState = getPreviousEpaState(newObject);
						throw new MalformedEPATraceException(
								"New object cannot have a previous EPA State: " + previousEpaState);
					}

					EPAState initialEpaState = this.automata.getInitialState();
					final EPAState currentEpaState = getCurrentState(newObject, scope);
					final EPATransition transition = new EPATransition(initialEpaState, actionName, currentEpaState);
					this.appendNewEpaTransition(newObject, transition);
				}
			}
		} catch (CodeUnderTestException | ClassNotFoundException | MalformedEPATraceException e) {
			throw new EvosuiteError(e);
		}
	}

	private void afterMethodStatement(MethodStatement methodStmt, Scope scope) throws EvosuiteError {
		try {
			if (methodStmt.getCallee() != null
					&& isObjectInstanceOfTargetClass(methodStmt.getCallee().getObject(scope))) {
				final Object calleeObject = methodStmt.getCallee().getObject(scope);
				// is the methodStmt defined as an EPA Action ?
				final String actionName = methodStmt.getMethodName() + methodStmt.getDescriptor();
				if (isEpaAction(actionName)) {
					if (!hasPreviousEpaState(calleeObject)) {
						// this object should have been seen previously!
						throw new MalformedEPATraceException("Object has no previous EPA State!");
					}
					final EPAState previousEpaState = getPreviousEpaState(calleeObject);
					final EPAState currentEpaState = getCurrentState(calleeObject, scope);
					final EPATransition transition = new EPATransition(previousEpaState, actionName, currentEpaState);
					this.appendNewEpaTransition(calleeObject, transition);
				}

			} else {
				// check if the return value is a previously unseen target class
				// object
				if (methodStmt.getReturnValue() != null && methodStmt.getReturnValue().getObject(scope) != null
						&& isObjectInstanceOfTargetClass(methodStmt.getReturnValue().getObject(scope))) {
					final Object returned_object = methodStmt.getReturnValue().getObject(scope);
					if (!hasPreviousEpaState(returned_object)) {
						// if no last state, previous state should be
						// initial state
						final EPAState previousEpaState = this.automata.getInitialState();
						final String actionName = this.automata.getInitialAction();
						final EPAState currentEpaState = getCurrentState(returned_object, scope);
						final EPATransition transition = new EPATransition(previousEpaState, actionName,
								currentEpaState);
						this.appendNewEpaTransition(returned_object, transition);
					}
				}
			}
		} catch (CodeUnderTestException | ClassNotFoundException | MalformedEPATraceException e) {
			throw new EvosuiteError(e);
		}
	}

	private static boolean isObjectInstanceOfTargetClass(Object calleeObject) throws ClassNotFoundException {
		Class<?> targetClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(Properties.TARGET_CLASS);
		final boolean objectIsTargetClass = targetClass.isInstance(calleeObject);
		return objectIsTargetClass;
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
	 */
	private EPAState getCurrentState(Object calleeObject, Scope scope) throws MalformedEPATraceException {
		EPAState currentState = null;
		for (EPAState epaState : this.automata.getStates()) {
			boolean executionResult = executeEpaStateMethod(epaState, calleeObject, scope);
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

	private boolean executeEpaStateMethod(EPAState epaState, Object calleeObject, Scope scope) {
		final String name = epaState.getName();
		try {
			Method method = calleeObject.getClass().getDeclaredMethod(isStateMethodName(name));
			boolean isAccessible = method.isAccessible();
			method.setAccessible(true);
			Boolean result = (Boolean) method.invoke(calleeObject);
			method.setAccessible(isAccessible);
			return result.booleanValue();
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new EvosuiteError(e);
		}
	}

	private String isStateMethodName(String name) {
		return "isState" + name;
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
	 * @param actionName
	 * @return
	 */
	private boolean isEpaAction(String actionName) {
		return this.automata.containsAction(actionName);
	}

	/**
	 * Copies the observed EPA Transitions as EPA traces into the execution
	 * result
	 * 
	 * @param r
	 * @param s
	 */
	@Override
	public void testExecutionFinished(ExecutionResult r, Scope s) {
		final HashSet<EPATrace> traces = new HashSet<EPATrace>();
		for (LinkedList<EPATransition> epaTransitionList : this.transitions.values()) {
			EPATrace trace = new EPATrace(new LinkedList<EPATransition>(epaTransitionList));
			traces.add(trace);
		}
		r.addEPATraces(traces);
		this.clear();
	}

	/**
	 * Clears all observed EPA Transitions
	 */
	@Override
	public void clear() {
		transitions.clear();
	}

}
