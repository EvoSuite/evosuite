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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;

/**
 * The test case executor manages thread creation/deletion to execute a test
 * case
 * 
 * @author Gordon Fraser
 * 
 */
public class TestCaseExecutor implements ThreadFactory {

	private final Logger logger = Logger.getLogger(TestCaseExecutor.class);

	private boolean log = true;

	public static long timeout = Properties.getPropertyOrDefault("timeout", 5000);

	private static TestCaseExecutor instance = null;

	private ExecutorService executor;

	private Thread currentThread = null;

	//private static ExecutorService executor = Executors.newCachedThreadPool();

	private List<ExecutionObserver> observers;

	protected boolean static_hack = Properties.getPropertyOrDefault("static_hack", false);

	public static TestCaseExecutor getInstance() {
		if (instance == null)
			instance = new TestCaseExecutor();

		return instance;
	}

	private TestCaseExecutor() {
		observers = new ArrayList<ExecutionObserver>();
		executor = Executors.newSingleThreadExecutor(this);
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
		if (static_hack)
			TestCluster.getInstance().resetStaticClasses();
		resetObservers();
		MaxTestsStoppingCondition.testExecuted();

		TestRunnable callable = new TestRunnable(tc, scope, observers);
		FutureTask<ExecutionResult> task = new FutureTask<ExecutionResult>(callable);
		executor.execute(task);
		try {
			ExecutionResult result = task.get(timeout, TimeUnit.MILLISECONDS);
			return result;

		} catch (InterruptedException e1) {
			logger.info("InterruptedException");
			ExecutionResult result = new ExecutionResult(tc, null);
			result.exceptions = callable.exceptionsThrown;
			result.trace = ExecutionTracer.getExecutionTracer().getTrace();
			ExecutionTracer.getExecutionTracer().clear();
			return result;
		} catch (ExecutionException e1) {
			logger.info("ExecutionException");
			ExecutionResult result = new ExecutionResult(tc, null);
			result.exceptions = callable.exceptionsThrown;
			result.trace = ExecutionTracer.getExecutionTracer().getTrace();
			ExecutionTracer.getExecutionTracer().clear();
			return result;
		} catch (TimeoutException e1) {

			logger.info("TimeoutException, need to stop runner");
			ExecutionTracer.setKillSwitch(true);
			ExecutionTracer.disable();
			task.cancel(true);

			if (!callable.runFinished) {
				logger.info("Run not finished, waiting...");
				try {
					executor.awaitTermination(timeout, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					logger.info("Interrupted");
					e.printStackTrace();
				}
				if (!callable.runFinished) {
					logger.info("Run still not finished, replacing executor.");
					executor.shutdownNow();
					currentThread.stop();
					if (currentThread.isAlive()) {
						logger.warn("Thread survived - unsafe operation.");
					}
					executor = Executors.newSingleThreadExecutor(this);
				}
			}
			ExecutionResult result = new ExecutionResult(tc, null);
			result.exceptions = callable.exceptionsThrown;
			result.exceptions.put(tc.size(), new TestCaseExecutor.TimeoutExceeded());
			result.trace = ExecutionTracer.getExecutionTracer().getTrace();
			ExecutionTracer.getExecutionTracer().clear();
			ExecutionTracer.setKillSwitch(false);
			ExecutionTracer.enable();

			return result;
		}
	}

	@SuppressWarnings("deprecation")
	public Map<Integer, Throwable> run(TestCase tc, Scope scope) {
		// StringTraceExecutionObserver obs = new
		// StringTraceExecutionObserver();
		// observers.add(obs);
		ExecutionTracer.getExecutionTracer().clear();
		if (static_hack)
			TestCluster.getInstance().resetStaticClasses();
		resetObservers();

		TestRunner runner = new TestRunner(null);
		runner.setLogging(log);
		runner.setup(tc, scope, observers);
		MaxTestsStoppingCondition.testExecuted();
		// MaxStatementsStoppingCondition.statementsExecuted(tc.size());

		try {
			// Start the test.
			runner.start();

			// If test doesn't finish in time, suspend it.
			runner.join(timeout);

			if (!runner.runFinished) {
				logger.warn("Exceeded max wait (" + timeout + "ms): aborting test input:");
				logger.warn(tc.toCode());
				runner.interrupt();

				if (runner.isAlive()) {
					// If test doesn't finish in time, suspend it.
					logger.info("Thread ignored interrupt, using killswitch");
					ExecutionTracer.setKillSwitch(true);
					runner.join(timeout / 2);
					if (!runner.runFinished) {
						logger.info("Trying thread.stop()");
						for (StackTraceElement element : runner.getStackTrace()) {
							logger.info(element.toString());
						}
						runner.stop();// We use this deprecated method because
						              // it's the only way to
						// stop a thread no matter what it's doing.
						// return runner.exceptionsThrown;
						runner.join(timeout / 2);

						if (runner.isAlive()) {
							logger.warn("Thread ignored stop()! All is lost!");
							for (StackTraceElement element : runner.getStackTrace()) {
								logger.warn(element.toString());
							}
						}
					}

					ExecutionTracer.enable();
				}
				ExecutionTracer.getExecutionTracer().clear();
				ExecutionTracer.setKillSwitch(false);
				runner.exceptionsThrown.put(tc.size(),
				                            new TestCaseExecutor.TimeoutExceeded());
			}
			return runner.exceptionsThrown;

		} catch (java.lang.InterruptedException e) {
			throw new IllegalStateException(
			        "A RunnerThread thread shouldn't be interrupted by anyone! "
			                + "(this may be a bug in the program; please report it.)");
		}

	}

	public Map<Integer, Throwable> run(TestCase tc) {
		Scope scope = new Scope();
		return run(tc, scope);
	}

	@Override
	public Thread newThread(Runnable r) {
		currentThread = new Thread(r);
		ExecutionTracer.setThread(currentThread);
		return currentThread;
	}
}
