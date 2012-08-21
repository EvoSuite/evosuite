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
/**
 * 
 */
package org.evosuite.testcase;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.evosuite.Properties;
import org.evosuite.runtime.FileSystem;
import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.System.SystemExitException;
import org.evosuite.sandbox.EvosuiteFile;
import org.evosuite.sandbox.Sandbox;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.io.IOWrapper;

/**
 * <p>
 * TestRunnable class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class TestRunnable implements InterfaceTestRunnable {

	private static final Logger logger = LoggerFactory.getLogger(TestRunnable.class);

	private final TestCase test;

	private Scope scope = null;

	public boolean runFinished;

	private static ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

	//private static PrintStream out = (Properties.PRINT_TO_SYSTEM ? System.out
	//      : new PrintStream(byteStream));

	public Map<Integer, Throwable> exceptionsThrown = new HashMap<Integer, Throwable>();

	public Set<ExecutionObserver> observers;

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

	private void joinClientThreads() {
		Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();

		for (Thread t : threadMap.keySet()) {
			if (t.isAlive())
				if (TestCaseExecutor.TEST_EXECUTION_THREAD_GROUP.equals(t.getThreadGroup().getName())) {
					boolean hasEvoSuite = false;
					for (StackTraceElement elem : threadMap.get(t)) {
						if (elem.getClassName().contains("evosuite"))
							hasEvoSuite = true;
					}
					if (!hasEvoSuite) {

						logger.info("Thread " + t);
						logger.info("This looks like the new thread");
						try {
							t.join(Properties.TIMEOUT);
						} catch (InterruptedException e) {
							// What can we do?
						}
						if (t.isAlive()) {
							logger.info("Thread is still alive");
						}
					}
				}
		}
	}

	private void checkClientThreads(int numThreads) {
		if (Thread.activeCount() > numThreads) {
			try {
				joinClientThreads();
			} catch (Throwable t) {
				logger.debug("Error while tyring to join thread: {}", t);
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	/** {@inheritDoc} */
	@Override
	public ExecutionResult call() {

		exceptionsThrown.clear();

		runFinished = false;
		ExecutionResult result = new ExecutionResult(test, null);
		Sandbox.setUpMocks();
		Runtime.resetRuntime();
		ExecutionTracer.enable();

		int numThreads = Thread.activeCount();
		PrintStream out = (Properties.PRINT_TO_SYSTEM ? System.out : new PrintStream(
		        byteStream));
		//out.flush();
		byteStream.reset();

		//PrintStream old_out = System.out;
		//PrintStream old_err = System.err;
		if (!Properties.PRINT_TO_SYSTEM) {
			//System.setOut(out);
			//System.setErr(out);
			LoggingUtils.muteCurrentOutAndErrStream();
		}

		long startTime = System.currentTimeMillis();

		int num = 0;
		try {
			// exceptionsThrown = test.execute(scope, observers, !log);
			for (StatementInterface s : test) {
				if (Thread.currentThread().isInterrupted() || Thread.interrupted()) {
					logger.info("Thread interrupted at statement " + num + ": "
					        + s.getCode());
					throw new TimeoutException();
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Executing statement " + s.getCode());
				}
				ExecutionTracer.statementExecuted();
				ExecutionTracer.disable();
				for (ExecutionObserver observer : observers) {
					observer.beforeStatement(s, scope);
				}
				ExecutionTracer.enable();

				Sandbox.setUpMockedSecurityManager();
				Throwable exceptionThrown = s.execute(scope, out);
				Sandbox.tearDownMockedSecurityManager();

				if (exceptionThrown != null) {
					if (exceptionThrown instanceof SystemExitException) {
						// This exception is raised when the test tried to call System.exit
						// We simply stop execution at this point
						break;
					}
					if (exceptionThrown instanceof TestCaseExecutor.TimeoutExceeded) {
						logger.debug("Test timed out!");
						exceptionsThrown.put(test.size(), exceptionThrown);
						result.setThrownExceptions(exceptionsThrown);
						result.reportNewThrownException(test.size(), exceptionThrown);
						result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
						break;
					}

					exceptionsThrown.put(num, exceptionThrown);
					if (ExecutionTracer.getExecutionTracer().getLastException() == exceptionThrown) {
						//logger.info("Exception " + exceptionThrown + " is explicit");
						result.explicitExceptions.put(num, true);
					} else {
						//logger.info("Exception " + exceptionThrown + " is implicit");
						result.explicitExceptions.put(num, false);
					}

					ExecutionTracer.disable();
					for (ExecutionObserver observer : observers) {
						observer.afterStatement(s, scope, exceptionThrown);
					}
					ExecutionTracer.enable();

					//FIXME: this might be removed
					if (exceptionThrown instanceof SecurityException) {
						logger.debug("Security exception found: " + exceptionThrown);
						break;
					}

					if (logger.isDebugEnabled()) {
						logger.debug("Exception thrown in statement: " + s.getCode()
						        + " - " + exceptionThrown.getClass().getName() + " - "
						        + exceptionThrown.getMessage());
						for (StackTraceElement elem : exceptionThrown.getStackTrace()) {
							logger.debug(elem.toString());
						}
						if (exceptionThrown.getCause() != null) {
							logger.debug("Cause: "
							        + exceptionThrown.getCause().getClass().getName()
							        + " - " + exceptionThrown.getCause().getMessage());
							for (StackTraceElement elem : exceptionThrown.getCause().getStackTrace()) {
								logger.debug(elem.toString());
							}
						} else {
							logger.debug("Cause is null");
						}
					}

					/*
					 * If an exception is thrown, we stop the execution of the test case, because the
					 * internal state could be corrupted, and not possible to verifyt the behaivor of
					 * any following function call
					 */
					if (Properties.BREAK_ON_EXCEPTION) {
						break;
					}
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Done statement " + s.getCode());
				}

				ExecutionTracer.disable();
				for (ExecutionObserver observer : observers) {
					observer.afterStatement(s, scope, exceptionThrown);
				}
				ExecutionTracer.enable();

				num++;
			} //end of loop

			checkClientThreads(numThreads);
			result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());

		} catch (ThreadDeath e) {// can't stop these guys
			Sandbox.tearDownEverything();
			//logger.info("Found error:");
			//logger.info(test.toCode());
			logger.info("Found error in " + test.toCode(), e);
			runFinished = true;
			throw e;
		} catch (TimeoutException e) {
			Sandbox.tearDownEverything();
			logger.info("Test timed out!");
			result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
		} catch (TestCaseExecutor.TimeoutExceeded e) {
			Sandbox.tearDownEverything();
			logger.info("Test timed out!");
			result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
		} catch (Throwable e) {
			Sandbox.tearDownEverything();
			logger.info("Exception at statement " + num + "! " + e);
			//logger.info(test.toCode());
			for (StackTraceElement elem : e.getStackTrace()) {
				logger.info(elem.toString());
			}
			if (e instanceof java.lang.reflect.InvocationTargetException) {
				logger.info("Cause: ");
				logger.info(e.getCause().toString(), e);
				e = e.getCause();
			}
			if (e instanceof AssertionError
			        && e.getStackTrace()[0].getClassName().contains("org.evosuite")) {
				//e1.printStackTrace();
				logger.error("Assertion Error in evosuitecode, for statement \n"
				        + test.getStatement(num).getCode() + " \n which is number: "
				        + num + " testcase \n" + test.toCode(), e);
				throw (AssertionError) e;
			}
			result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
			ExecutionTracer.getExecutionTracer().clear();
			// exceptionThrown = e;
			//logger.info("Error while executing statement " + test.toCode(), e);
			// System.exit(1);

		} // finally {
		finally {
			if (!Properties.PRINT_TO_SYSTEM) {
				//System.setOut(old_out);
				//System.setErr(old_err);
				LoggingUtils.restorePreviousOutAndErrStream();
			}
		}

		runFinished = true;
		Sandbox.tearDownMocks();
		Runtime.handleRuntimeAccesses();
		if (Properties.VIRTUAL_FS) {
			test.setAccessedFiles(new ArrayList<String>(IOWrapper.getAccessedFiles()));
			FileSystem.restoreOriginalFS();
		}

		result.setExecutionTime(System.currentTimeMillis() - startTime);

		// FIXXME: Why don't we write into the result directly?
		result.setThrownExceptions(getExceptionsThrown());
		if (Sandbox.canUseFileContentGeneration())
			try {
				logger.debug("Enabling file handling");
				Method m = Sandbox.class.getMethod("generateFileContent",
				                                   EvosuiteFile.class, String.class);
				// TODO: Re-insert!
				// if (!TestCluster.getInstance().test_methods.contains(m))
				//	TestCluster.getInstance().test_methods.add(m);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		return result;
		//}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.InterfaceTestRunnable#getExceptionsThrown()
	 */
	/** {@inheritDoc} */
	@Override
	public Map<Integer, Throwable> getExceptionsThrown() {
		HashMap<Integer, Throwable> copy = new HashMap<Integer, Throwable>();
		copy.putAll(exceptionsThrown);
		return copy;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.InterfaceTestRunnable#isRunFinished()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isRunFinished() {
		return runFinished;
	}

}
