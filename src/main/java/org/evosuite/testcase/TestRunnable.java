/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcase;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.evosuite.Properties;
import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.System.SystemExitException;
import org.evosuite.sandbox.EvosuiteFile;
import org.evosuite.sandbox.Sandbox;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uta.cse.dsc.VMError;

/**
 * <p>
 * TestRunnable class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class TestRunnable implements InterfaceTestRunnable {

	private static final Logger logger = LoggerFactory.getLogger(TestRunnable.class);

	private static ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

	private final TestCase test;

	private final Scope scope;

	protected boolean runFinished;

	/**
	 * Map a thrown exception ('value') with the the position ('key') in the test sequence in which it was thrown from.
	 */
	protected Map<Integer, Throwable> exceptionsThrown = new HashMap<Integer, Throwable>();

	protected Set<ExecutionObserver> observers;

	protected transient long startTime;

	protected transient Set<Thread> currentRunningThreads;

	/**
	 * <p>
	 * Constructor for TestRunnable.
	 * </p>
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param scope
	 *            a {@link org.evosuite.testcase.Scope} object.
	 * @param observers
	 *            a {@link java.util.Set} object.
	 */
	public TestRunnable(TestCase tc, Scope scope, Set<ExecutionObserver> observers) {
		test = tc;
		this.scope = scope;
		this.observers = observers;
		runFinished = false;
	}

	/**
	 * <p>
	 * After the test case is executed, if any SUT thread is still running, we will wait for their termination. 
	 * To identify which thread belong to
	 * SUT, before test case execution we should check which are the threads that are running.
	 * </p>
	 * <p>
	 * WARNING: The sandbox might prevent accessing thread informations, so best to call this method from outside this class
	 * </p>
	 */
	public void storeCurrentThreads() {
		if (currentRunningThreads == null) {
			currentRunningThreads = new HashSet<Thread>();
		} else {
			currentRunningThreads.clear();
		}

		Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();
		for (Thread t : threadMap.keySet()) {
			if (t.isAlive()) {
				currentRunningThreads.add(t);
			}
		}
	}

	/**
	 * Try to kill (and then join) the SUT threads.
	 * Killing the SUT threads is important, because some spawn threads could just wait on objects/locks,
	 * and so make the test case executions always last TIMEOUT ms.
	 */
	public void killAndJoinClientThreads() throws IllegalStateException{
		
		if(currentRunningThreads==null){
			throw new IllegalStateException("The current threads are not set. You need to call storeCurrentThreads() first");
		}
		
		/*
		 * First we set the kill switch in the instrumented bytecode, this
		 * to prevent issues with code that do not handle interrupt 
		 */
		ExecutionTracer.setKillSwitch(true);
		
		Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();

		/*
		 * try to interrupt the SUT threads
		 */
		for (Thread t : threadMap.keySet()) {
			/*
			 * the TestCaseExecutor threads are executing the SUT, so they are not privileged.
			 * But we don't want to stop/join them, as they just execute Runnable objects, and
			 * stay in a pool in an execution service.  
			 */
			if(t.getName().startsWith(TestCaseExecutor.TEST_EXECUTION_THREAD)){
				continue;
			}
			
			if (t.isAlive() && !currentRunningThreads.contains(t)) {
				t.interrupt();
			}
		}
		
		/*
		 * now, join up to a total of TIMEOUT ms. 
		 * 
		 */
		for (Thread t : threadMap.keySet()) {
			if(t.getName().startsWith(TestCaseExecutor.TEST_EXECUTION_THREAD)){
				continue;
			}
			
			if (t.isAlive() && !currentRunningThreads.contains(t)) {

				logger.info("Thread " + t + ". This looks like the new thread");
				try {
					/*
					 * In total the test case should not run for more than Properties.TIMEOUT ms
					 */
					long delta = System.currentTimeMillis() - startTime;
					long waitingTime = Properties.TIMEOUT - delta;
					if (waitingTime > 0) {
						t.join(waitingTime); 
					}
				} catch (InterruptedException e) {
					// What can we do?
					break;
				}
				if (t.isAlive()) {
					logger.info("Thread is still alive: " + t.getName());
				}
			}
		}
		
		/*
		 * we need it, otherwise issue during search in which accessing enum in SUT would call toString,
		 * and so throw a TimeoutExceeded exception 
		 */
		ExecutionTracer.setKillSwitch(false);
		
		/*
		 * important. this is used to later check if current threads are set
		 */
		currentRunningThreads = null;
	}

	/**
	 * Going to join SUT threads if active threads are more than numThreads. In other words, we are trying to join till all SUT threads are done
	 * within the defined time threshold
	 * 
	 * @param numThreads
	 */
	@Deprecated
	private void checkClientThreads(int numThreads) {
		if (Thread.activeCount() > numThreads) {
			try {
				killAndJoinClientThreads();
			} catch (Throwable t) {
				logger.debug("Error while tyring to join thread: {}", t);
			}
		}
	}

	/**
	 * Inform all observers that we are going to execute the input statement
	 * 
	 * @param s
	 *            the statement to execute
	 */
	protected void informObservers_before(StatementInterface s) {
		ExecutionTracer.disable();
		try {
			for (ExecutionObserver observer : observers) {
				observer.beforeStatement(s, scope);
			}
		} finally {
			ExecutionTracer.enable();
		}
	}

	/**
	 * Inform all observers that input statement has been executed
	 * 
	 * @param s
	 *            the executed statement
	 * @param exceptionThrown
	 *            the exception thrown when executing the statement, if any (can be null)
	 */
	protected void informObservers_after(StatementInterface s, Throwable exceptionThrown) {
		ExecutionTracer.disable();
		try {
			for (ExecutionObserver observer : observers) {
				observer.afterStatement(s, scope, exceptionThrown);
			}
		} finally {
			ExecutionTracer.enable();
		}
	}

	/** {@inheritDoc} */
	@Override
	public ExecutionResult call() {

		exceptionsThrown.clear();

		runFinished = false;
		ExecutionResult result = new ExecutionResult(test, null);
		Sandbox.setUpMocks();
		Runtime.resetRuntime();
		ExecutionTracer.enable();

		PrintStream out = (Properties.PRINT_TO_SYSTEM ? System.out : new PrintStream(byteStream));
		byteStream.reset();

		if (!Properties.PRINT_TO_SYSTEM) {
			LoggingUtils.muteCurrentOutAndErrStream();
		}

		startTime = System.currentTimeMillis();

		int num = 0;
		try {
			for (StatementInterface s : test) {

				if (Thread.currentThread().isInterrupted() || Thread.interrupted()) {
					logger.info("Thread interrupted at statement " + num + ": " + s.getCode());
					throw new TimeoutException();
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Executing statement " + s.getCode());
				}

				ExecutionTracer.statementExecuted();
				informObservers_before(s);

				/*
				 * Here actually execute a statement of the SUT
				 */
				Throwable exceptionThrown = s.execute(scope, out);

				if (exceptionThrown != null) {
					// if internal error, than throw exception
					// -------------------------------------------------------
					if (exceptionThrown instanceof VMError) {
						throw (VMError) exceptionThrown;
					}
					if (exceptionThrown instanceof EvosuiteError) {
						throw (EvosuiteError) exceptionThrown;
					}
					// -------------------------------------------------------

					// FIXME: why???
					if (exceptionThrown instanceof TestCaseExecutor.TimeoutExceeded) {
						logger.debug("Test timed out!");
						exceptionsThrown.put(test.size(), exceptionThrown);
						result.setThrownExceptions(exceptionsThrown);
						result.reportNewThrownException(test.size(), exceptionThrown);
						result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
						break;
					}

					// keep track if the exception and where it was thrown
					exceptionsThrown.put(num, exceptionThrown);

					// check if it was an explicit exception
					// --------------------------------------------------------
					if (ExecutionTracer.getExecutionTracer().getLastException() == exceptionThrown) {
						result.explicitExceptions.put(num, true);
					} else {
						result.explicitExceptions.put(num, false);
					}
					// --------------------------------------------------------

					// some debugging info
					// --------------------------------------------------------

					if (logger.isDebugEnabled()) {
						logger.debug("Exception thrown in statement: " + s.getCode() + " - " + exceptionThrown.getClass().getName() + " - "
								+ exceptionThrown.getMessage());
						for (StackTraceElement elem : exceptionThrown.getStackTrace()) {
							logger.debug(elem.toString());
						}
						if (exceptionThrown.getCause() != null) {
							logger.debug("Cause: " + exceptionThrown.getCause().getClass().getName() + " - "
									+ exceptionThrown.getCause().getMessage());
							for (StackTraceElement elem : exceptionThrown.getCause().getStackTrace()) {
								logger.debug(elem.toString());
							}
						} else {
							logger.debug("Cause is null");
						}
					}
					// --------------------------------------------------------

					/*
					 * If an exception is thrown, we stop the execution of the test case, because the internal state could be corrupted, and not
					 * possible to verify the behavior of any following function call. Predicate should be true by default
					 */
					if (Properties.BREAK_ON_EXCEPTION || exceptionThrown instanceof SystemExitException) {
						break;
					}
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Done statement " + s.getCode());
				}

				informObservers_after(s, exceptionThrown);

				num++;
			} // end of loop
		} catch (ThreadDeath e) {// can't stop these guys
			logger.info("Found error in " + test.toCode(), e);
			throw e; // this needs to be propagated
		} catch (TimeoutException e) {
			logger.info("Test timed out!");
		} catch (TestCaseExecutor.TimeoutExceeded e) {
			logger.info("Test timed out!");
		} catch (Throwable e) {
			if (e instanceof EvosuiteError) {
				logger.info("Evosuite Error!", e);
				throw (EvosuiteError) e;
			}
			if (e instanceof VMError) {
				logger.info("VM Error!", e);
				throw (VMError) e;
			}
			logger.info("Exception at statement " + num + "! " + e);
			for (StackTraceElement elem : e.getStackTrace()) {
				logger.info(elem.toString());
			}
			if (e instanceof java.lang.reflect.InvocationTargetException) {
				logger.info("Cause: " + e.getCause().toString(), e);
				e = e.getCause();
			}
			if (e instanceof AssertionError && e.getStackTrace()[0].getClassName().contains("org.evosuite")) {
				logger.error("Assertion Error in evosuitecode, for statement \n" + test.getStatement(num).getCode() + " \n which is number: " + num
						+ " testcase \n" + test.toCode(), e);
				throw (AssertionError) e;
			}
			// FIXME: why this "clear()"?
			// ExecutionTracer.getExecutionTracer().clear();

			logger.error("Suppressed/ignored exception during test case execution on class "+Properties.TARGET_CLASS+": " + e.getMessage(), e);
		} finally {
			if (!Properties.PRINT_TO_SYSTEM) {
				LoggingUtils.restorePreviousOutAndErrStream();
			}
			runFinished = true;
		}

		result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
		result.setExecutionTime(System.currentTimeMillis() - startTime);
		result.setExecutedStatements(num);
		result.setThrownExceptions(exceptionsThrown);

		// FIXME: what is this for? - 17.10.12: JavaDoc added by Daniel
		Runtime.handleRuntimeAccesses(test);

		if (Sandbox.canUseFileContentGeneration()) {
			try {
				logger.debug("Enabling file handling");
				Method m = Sandbox.class.getMethod("generateFileContent", EvosuiteFile.class, String.class);
				// TODO: Re-insert!
				// if (!TestCluster.getInstance().test_methods.contains(m))
				// TestCluster.getInstance().test_methods.add(m);
			} catch (SecurityException e) {
				logger.error(e.getMessage(), e);
			} catch (NoSuchMethodException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Map<Integer, Throwable> getExceptionsThrown() {
		HashMap<Integer, Throwable> copy = new HashMap<Integer, Throwable>();
		copy.putAll(exceptionsThrown);
		return copy;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isRunFinished() {
		return runFinished;
	}

}
