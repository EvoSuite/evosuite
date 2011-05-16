/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.Properties.Criterion;
import de.unisb.cs.st.evosuite.contracts.ContractChecker;
import de.unisb.cs.st.evosuite.coverage.concurrency.ConcurrentTestRunnable;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition;
import de.unisb.cs.st.evosuite.sandbox.Sandbox;

/**
 * The test case executor manages thread creation/deletion to execute a test
 * case
 * 
 * @author Gordon Fraser
 * 
 */
public class TestCaseExecutor implements ThreadFactory {

	private static final Logger logger = Logger.getLogger(TestCaseExecutor.class);

	private boolean log = true;

	private static final PrintStream systemOut = System.out;
	private static final PrintStream systemErr = System.err;

	private static TestCaseExecutor instance = null;

	private ExecutorService executor;

	private Thread currentThread = null;

	//private static ExecutorService executor = Executors.newCachedThreadPool();

	private List<ExecutionObserver> observers;

	public static long timeExecuted = 0;

	public static int testsExecuted = 0;

	public static TestCaseExecutor getInstance() {
		if (instance == null)
			instance = new TestCaseExecutor();

		return instance;
	}

	/**
	 * Execute a test case
	 * 
	 * @param test
	 *            The test case to execute
	 * @param mutant
	 *            The mutation to active (null = no mutation)
	 * 
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
			logger.fatal("TG: Exception caught: ", e);
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

	public void setup() {
		// start own thread
	}

	public static void pullDown() {
		if (instance != null)
			instance.executor.shutdownNow();
	}

	public void setLogging(boolean value) {
		log = value;
	}

	public void addObserver(ExecutionObserver observer) {
		observers.add(observer);
	}

	public void removeObserver(ExecutionObserver observer) {
		observers.remove(observer);
	}

	public void newObservers() {
		observers = new ArrayList<ExecutionObserver>();
		if (Properties.CHECK_CONTRACTS) {
			observers.add(new ContractChecker());
		}

	}

	private void resetObservers() {
		for (ExecutionObserver observer : observers) {
			observer.clear();
		}
	}

	public ExecutionResult execute(TestCase tc) {
		Scope scope = new Scope();
		return execute(tc, scope);
	}

	public ExecutionResult execute(TestCase tc, Scope scope) {
		ExecutionTracer.getExecutionTracer().clear();
		if (Properties.STATIC_HACK)
			TestCluster.getInstance().resetStaticClasses();
		resetObservers();
		ExecutionObserver.currentTest(tc);
		MaxTestsStoppingCondition.testExecuted();

		long startTime = System.currentTimeMillis();

		TimeoutHandler<ExecutionResult> handler = new TimeoutHandler<ExecutionResult>();

		//#TODO steenbuck could be nicer (TestRunnable should be an interface
		InterfaceTestRunnable callable;
		if (Properties.CRITERION == Criterion.CONCURRENCY) {
			callable = new ConcurrentTestRunnable(tc, scope, observers);
		} else {
			callable = new TestRunnable(tc, scope, observers);
		}
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
			result.exceptions = callable.getExceptionsThrown();
			result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
			ExecutionTracer.getExecutionTracer().clear();
			return result;

		} catch (InterruptedException e1) {
			Sandbox.tearDownEverything();
			logger.info("InterruptedException");
			ExecutionResult result = new ExecutionResult(tc, null);
			result.exceptions = callable.getExceptionsThrown();
			result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
			ExecutionTracer.getExecutionTracer().clear();
			return result;
		} catch (ExecutionException e1) {
			/*
			 * An ExecutionException at this point, is most likely an error in evosuite. As exceptions from the tested code are catched before this.
			 */
			Sandbox.tearDownEverything();
			logger.error("ExecutionException (this is likely a serious error in the framework)", e1);
			ExecutionResult result = new ExecutionResult(tc, null);
			result.exceptions = callable.getExceptionsThrown();
			result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
			ExecutionTracer.getExecutionTracer().clear();
			return result;
		} catch (TimeoutException e1) {
			Sandbox.tearDownEverything();
			System.setOut(systemOut);
			System.setErr(systemErr);

			if (Properties.LOG_TIMEOUT) {
				System.err.println("Timeout occurred for " + Properties.TARGET_CLASS);
			}
			logger.info("TimeoutException, need to stop runner");
			ExecutionTracer.setKillSwitch(true);
			ExecutionTracer.disable();
			//task.cancel(true);
			handler.getLastTask().cancel(true);

			if (!callable.isRunFinished()) {
				logger.info("Run not finished, waiting...");
				try {
					executor.awaitTermination(Properties.TIMEOUT, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					logger.info("Interrupted");
					e.printStackTrace();
				}
				if (!callable.isRunFinished()) {
					logger.info("Run still not finished, replacing executor.");
					try {
						executor.shutdownNow();
						if (currentThread.isAlive()) {
							logger.warn("Thread survived - unsafe operation.");
							for (StackTraceElement element : currentThread.getStackTrace()) {
								logger.warn(element);
							}
							currentThread.stop();
						}
					} catch (ThreadDeath t) {
						logger.info("ThreadDeath.");
					} catch (Throwable t) {
						logger.info("Throwable: " + t);
					}
					executor = Executors.newSingleThreadExecutor(this);
				}
			}
			ExecutionResult result = new ExecutionResult(tc, null);
			result.exceptions = callable.getExceptionsThrown();
			result.exceptions.put(tc.size(), new TestCaseExecutor.TimeoutExceeded());
			result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
			ExecutionTracer.getExecutionTracer().clear();
			ExecutionTracer.setKillSwitch(false);
			ExecutionTracer.enable();

			return result;
		}
	}

	@Override
	public Thread newThread(Runnable r) {
		currentThread = new Thread(r);
		ExecutionTracer.setThread(currentThread);
		return currentThread;
	}
}
