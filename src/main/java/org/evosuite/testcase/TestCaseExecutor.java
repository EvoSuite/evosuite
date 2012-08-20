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

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.evosuite.Properties;
import org.evosuite.contracts.ContractChecker;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition;
import org.evosuite.sandbox.PermissionStatistics;
import org.evosuite.sandbox.Sandbox;
import org.evosuite.setup.TestCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The test case executor manages thread creation/deletion to execute a test
 * case
 * 
 * @author Gordon Fraser
 */
public class TestCaseExecutor implements ThreadFactory {

	/**
	 * Used to identify the threads spawn by the SUT
	 */
	public static final String TEST_EXECUTION_THREAD_GROUP = "Test Execution";

	private static final Logger logger = LoggerFactory.getLogger(TestCaseExecutor.class);

	private static final PrintStream systemOut = System.out;
	private static final PrintStream systemErr = System.err;

	private static TestCaseExecutor instance = null;

	private ExecutorService executor;

	private Thread currentThread = null;

	private ThreadGroup threadGroup = null;

	//private static ExecutorService executor = Executors.newCachedThreadPool();

	private Set<ExecutionObserver> observers;

	private final Set<Thread> stalledThreads = new HashSet<Thread>();

	/** Constant <code>timeExecuted=0</code> */
	public static long timeExecuted = 0;

	/** Constant <code>testsExecuted=0</code> */
	public static int testsExecuted = 0;

	/**
	 * <p>
	 * Getter for the field <code>instance</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testcase.TestCaseExecutor} object.
	 */
	public static synchronized TestCaseExecutor getInstance() {
		if (instance == null)
			instance = new TestCaseExecutor();

		return instance;
	}

	/**
	 * Execute a test case
	 * 
	 * @param test
	 *            The test case to execute
	 * @return Result of the execution
	 */
	public static ExecutionResult runTest(TestCase test) {

		ExecutionResult result = new ExecutionResult(test, null);

		try {
			TestCaseExecutor executor = getInstance();
			logger.debug("Executing test");
			result = executor.execute(test);

			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);

			// for(TestObserver observer : observers) {
			// observer.testResult(result);
			// }
		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			logger.error("TG: Exception caught: ", e);
			System.exit(1);
		}

		// System.out.println("TG: Killed "+result.getNumKilled()+" out of "+mutants.size());
		return result;
	}

	private TestCaseExecutor() {
		executor = Executors.newSingleThreadExecutor(this);
		newObservers();
	}

	public static class TimeoutExceeded extends RuntimeException {
		private static final long serialVersionUID = -5314228165430676893L;
	}

	/**
	 * <p>
	 * setup
	 * </p>
	 */
	public void setup() {
		// start own thread
	}

	/**
	 * <p>
	 * pullDown
	 * </p>
	 */
	public static void pullDown() {
		if (instance != null)
			instance.executor.shutdownNow();
	}

	/**
	 * <p>
	 * initExecutor
	 * </p>
	 */
	public static void initExecutor() {
		if (instance != null) {
			if (instance.executor == null) {
				logger.warn("TestCaseExecutor instance is non-null, but its actual executor is null");
				instance.executor = Executors.newSingleThreadExecutor(instance);
			} else {
				if (instance.executor.isShutdown()) {
					instance.executor = Executors.newSingleThreadExecutor(instance);
				}
			}
		}
	}

	/**
	 * <p>
	 * addObserver
	 * </p>
	 * 
	 * @param observer
	 *            a {@link org.evosuite.testcase.ExecutionObserver} object.
	 */
	public void addObserver(ExecutionObserver observer) {
		if (!observers.contains(observer)) {
			logger.debug("Adding observer " + observer);
			observers.add(observer);
		}
		// FIXXME: Find proper solution for this
		//for (ExecutionObserver o : observers)
		//	if (o.getClass().equals(observer.getClass()))
		//		return;

	}

	/**
	 * <p>
	 * removeObserver
	 * </p>
	 * 
	 * @param observer
	 *            a {@link org.evosuite.testcase.ExecutionObserver} object.
	 */
	public void removeObserver(ExecutionObserver observer) {
		if (observers.contains(observer)) {
			logger.debug("Removing observer " + observer);
			observers.remove(observer);
		}
	}

	/**
	 * <p>
	 * newObservers
	 * </p>
	 */
	public void newObservers() {
		observers = new LinkedHashSet<ExecutionObserver>();
		if (Properties.CHECK_CONTRACTS) {
			observers.add(new ContractChecker());
		}

	}

	private void resetObservers() {
		for (ExecutionObserver observer : observers) {
			observer.clear();
		}
	}

	/**
	 * Execute a test case on a new scope
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @return a {@link org.evosuite.testcase.ExecutionResult} object.
	 */
	public ExecutionResult execute(TestCase tc) {
		Scope scope = new Scope();
		return execute(tc, scope);
	}

	/**
	 * Execute a test case on an existing scope
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param scope
	 *            a {@link org.evosuite.testcase.Scope} object.
	 * @return a {@link org.evosuite.testcase.ExecutionResult} object.
	 */
	@SuppressWarnings("deprecation")
	public ExecutionResult execute(TestCase tc, Scope scope) {
		ExecutionTracer.getExecutionTracer().clear();
		// TODO: Re-insert!
		if (Properties.STATIC_HACK)
			TestCluster.getInstance().resetStaticClasses();
		resetObservers();
		ExecutionObserver.setCurrentTest(tc);
		MaxTestsStoppingCondition.testExecuted();

		long startTime = System.currentTimeMillis();

		TimeoutHandler<ExecutionResult> handler = new TimeoutHandler<ExecutionResult>();

		//#TODO steenbuck could be nicer (TestRunnable should be an interface
		InterfaceTestRunnable callable = new TestRunnable(tc, scope, observers);

		//FutureTask<ExecutionResult> task = new FutureTask<ExecutionResult>(callable);
		//executor.execute(task);

		try {
			//ExecutionResult result = task.get(timeout, TimeUnit.MILLISECONDS);
			ExecutionResult result = handler.execute(callable, executor,
			                                         Properties.TIMEOUT,
			                                         Properties.CPU_TIMEOUT);

			long endTime = System.currentTimeMillis();
			timeExecuted += endTime - startTime;
			testsExecuted++;
			return result;
		} catch (ThreadDeath t) {
			logger.warn("Caught ThreadDeath during test execution");
			Sandbox.tearDownEverything();
			ExecutionResult result = new ExecutionResult(tc, null);
			result.setThrownExceptions(callable.getExceptionsThrown());
			result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
			ExecutionTracer.getExecutionTracer().clear();
			return result;

		} catch (InterruptedException e1) {
			Sandbox.tearDownEverything();
			logger.info("InterruptedException");
			ExecutionResult result = new ExecutionResult(tc, null);
			result.setThrownExceptions(callable.getExceptionsThrown());
			result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
			ExecutionTracer.getExecutionTracer().clear();
			return result;
		} catch (ExecutionException e1) {
			/*
			 * An ExecutionException at this point, is most likely an error in evosuite. As exceptions from the tested code are caught before this.
			 */
			Sandbox.tearDownEverything();
			System.setOut(systemOut);
			System.setErr(systemErr);

			logger.error("ExecutionException (this is likely a serious error in the framework)",
			             e1);
			ExecutionResult result = new ExecutionResult(tc, null);
			result.setThrownExceptions(callable.getExceptionsThrown());
			result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
			ExecutionTracer.getExecutionTracer().clear();
			if (e1.getCause() instanceof Error) { //an error was thrown somewhere in evosuite code
				throw (Error) e1.getCause();
			} else if (e1.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e1.getCause();
			}
			return result; //FIXME: is this reachable?
		} catch (TimeoutException e1) {
			Sandbox.tearDownEverything();
			//System.setOut(systemOut);
			//System.setErr(systemErr);

			if (Properties.LOG_TIMEOUT) {
				System.err.println("Timeout occurred for " + Properties.TARGET_CLASS);
			}
			logger.info("TimeoutException, need to stop runner", e1);
			ExecutionTracer.setKillSwitch(true);
			try {
				handler.getLastTask().get(Properties.SHUTDOWN_TIMEOUT,
				                          TimeUnit.MILLISECONDS);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				//e2.printStackTrace();
			} catch (ExecutionException e2) {
				// TODO Auto-generated catch block
				//e2.printStackTrace();
			} catch (TimeoutException e2) {
				// TODO Auto-generated catch block
				//e2.printStackTrace();
			}
			//task.cancel(true);

			if (!callable.isRunFinished()) {
				logger.info("Cancelling thread:");
				for (StackTraceElement elem : currentThread.getStackTrace()) {
					logger.info(elem.toString());
				}
				handler.getLastTask().cancel(true);
				logger.info("Run not finished, waiting...");
				try {
					executor.awaitTermination(Properties.SHUTDOWN_TIMEOUT,
					                          TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					logger.info("Interrupted");
					e.printStackTrace();
				}
				if (!callable.isRunFinished()) {
					logger.info("Run still not finished, replacing executor.");
					try {
						executor.shutdownNow();
						if (currentThread.isAlive()) {
							logger.info("Thread survived - unsafe operation.");
							for (StackTraceElement element : currentThread.getStackTrace()) {
								logger.info(element.toString());
							}
							logger.info("Killing thread:");
							for (StackTraceElement elem : currentThread.getStackTrace()) {
								logger.info(elem.toString());
							}
							currentThread.stop();
						}
					} catch (ThreadDeath t) {
						logger.info("ThreadDeath.");
					} catch (Throwable t) {
						logger.info("Throwable: " + t);
					}
					ExecutionTracer.disable();
					executor = Executors.newSingleThreadExecutor(this);
				}
			} else {
				logger.info("Run is finished - " + currentThread.isAlive() + ": "
				        + getNumStalledThreads());

			}
			ExecutionTracer.disable();

			ExecutionResult result = new ExecutionResult(tc, null);
			result.setThrownExceptions(callable.getExceptionsThrown());
			result.reportNewThrownException(tc.size(),
			                                new TestCaseExecutor.TimeoutExceeded());
			result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
			ExecutionTracer.getExecutionTracer().clear();
			ExecutionTracer.setKillSwitch(false);
			ExecutionTracer.enable();
			System.setOut(systemOut);
			System.setErr(systemErr);

			return result;
		} finally {
			PermissionStatistics.getInstance().countThreads(threadGroup.activeCount());
		}
	}

	/**
	 * <p>
	 * getNumStalledThreads
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getNumStalledThreads() {
		Iterator<Thread> iterator = stalledThreads.iterator();
		while (iterator.hasNext()) {
			Thread t = iterator.next();
			if (!t.isAlive()) {
				iterator.remove();
			}
		}
		return stalledThreads.size();
	}

	/** {@inheritDoc} */
	@Override
	public Thread newThread(Runnable r) {
		if (currentThread != null && currentThread.isAlive()) {
			currentThread.setPriority(Thread.MIN_PRIORITY);
			stalledThreads.add(currentThread);
			logger.info("Current number of stalled threads: " + getNumStalledThreads());
		} else {
			logger.info("No stalled threads");
		}

		if (threadGroup != null) {
			PermissionStatistics.getInstance().countThreads(threadGroup.activeCount());
		}
		threadGroup = new ThreadGroup(TEST_EXECUTION_THREAD_GROUP);
		currentThread = new Thread(threadGroup, r);
		currentThread.setContextClassLoader(TestCluster.classLoader);
		ExecutionTracer.setThread(currentThread);
		return currentThread;
	}
}
