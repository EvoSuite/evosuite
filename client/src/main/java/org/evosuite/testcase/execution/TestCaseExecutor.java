/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcase.execution;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition;
import org.evosuite.runtime.LoopCounter;
import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.sandbox.PermissionStatistics;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.runtime.util.JOptionPaneInputs;
import org.evosuite.runtime.util.SystemInUtil;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.reset.ClassReInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * <p>
 * The test case executor manages thread creation/deletion to execute a test
 * case
 * </p>
 *
 * <p>
 * WARNING: never give "privileged" rights in MSecurityManager to any of the
 * threads generated here
 * </p>
 *
 * @author Gordon Fraser
 */
public class TestCaseExecutor implements ThreadFactory {

    /**
     * Used to identify the threads spawn by the SUT
     */
    public static final String TEST_EXECUTION_THREAD_GROUP = "Test_Execution_Group";

    /**
     * Name used to define the threads spawn by this factory
     */
    public static final String TEST_EXECUTION_THREAD = "TEST_EXECUTION_THREAD";

    private static final Logger logger = LoggerFactory.getLogger(TestCaseExecutor.class);

    private static final PrintStream systemOut = System.out;
    private static final PrintStream systemErr = System.err;

    private static TestCaseExecutor instance = null;

    private ExecutorService executor;

    private Thread currentThread = null;

    private ThreadGroup threadGroup = null;

    // private static ExecutorService executor =
    // Executors.newCachedThreadPool();

    private Set<ExecutionObserver> observers;

    private final Set<Thread> stalledThreads = new HashSet<>();

    /**
     * Constant <code>timeExecuted=0</code>
     */
    public static long timeExecuted = 0;

    /**
     * Constant <code>testsExecuted=0</code>
     */
    public static int testsExecuted = 0;

    /**
     * Used when we spawn a new thread to give a unique name
     */
    public volatile int threadCounter;

    static {
        PermissionStatistics.getInstance().setThreadGroupToMonitor(TEST_EXECUTION_THREAD_GROUP);
    }

    /**
     * <p>
     * Getter for the field <code>instance</code>.
     * </p>
     *
     * @return a {@link org.evosuite.testcase.execution.TestCaseExecutor}
     * object.
     */
    public static synchronized TestCaseExecutor getInstance() {
        if (instance == null)
            instance = new TestCaseExecutor();

        return instance;
    }

    /**
     * Execute a test case
     *
     * @param test The test case to execute
     * @return Result of the execution
     */
    public static ExecutionResult runTest(TestCase test) {

        ExecutionResult result = new ExecutionResult(test, null);

        try {
            TestCaseExecutor executor = getInstance();
            logger.debug("Executing test");
            result = executor.execute(test);

            MaxStatementsStoppingCondition.statementsExecuted(result.getExecutedStatements());

        } catch (Exception e) {
            logger.error("TG: Exception caught: ", e);
            throw new Error(e);
        }

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
        if (instance != null) {
            if (instance.executor != null) {
                instance.executor.shutdownNow();
                instance.executor = null;
            }
        }
    }

    /**
     * <p>
     * initExecutor
     * </p>
     */
    public static void initExecutor() {
        if (instance != null) {
            if (instance.executor == null) {
                logger.info("TestCaseExecutor instance is non-null, but its actual executor is null");
                instance.executor = Executors.newSingleThreadExecutor(instance);
            } else {
                instance.executor = Executors.newSingleThreadExecutor(instance);
            }
        }
    }

    /**
     * <p>
     * addObserver
     * </p>
     *
     * @param observer a {@link org.evosuite.testcase.execution.ExecutionObserver}
     *                 object.
     */
    public void addObserver(ExecutionObserver observer) {
        if (!observers.contains(observer)) {
            logger.debug("Adding observer");
            observers.add(observer);
        }
        // FIXXME: Find proper solution for this
        // for (ExecutionObserver o : observers)
        // if (o.getClass().equals(observer.getClass()))
        // return;

    }

    /**
     * <p>
     * removeObserver
     * </p>
     *
     * @param observer a {@link org.evosuite.testcase.execution.ExecutionObserver}
     *                 object.
     */
    public void removeObserver(ExecutionObserver observer) {
        if (observers.contains(observer)) {
            logger.debug("Removing observer");
            observers.remove(observer);
        }
    }

    /**
     * <p>
     * newObservers
     * </p>
     */
    public void newObservers() {
        observers = new LinkedHashSet<>();
    }

    public Set<ExecutionObserver> getExecutionObservers() {
        return new LinkedHashSet<>(observers);
    }

    private void resetObservers() {
        for (ExecutionObserver observer : observers) {
            observer.clear();
        }
    }

    /**
     * Execute a test case on a new scope
     *
     * @param tc a {@link org.evosuite.testcase.TestCase} object.
     * @return a {@link org.evosuite.testcase.execution.ExecutionResult} object.
     */
    public ExecutionResult execute(TestCase tc) {
        ExecutionResult result = execute(tc, Properties.TIMEOUT);
        return result;
    }

    /**
     * Execute a test case on a new scope
     *
     * @param tc a {@link org.evosuite.testcase.TestCase} object.
     * @return a {@link org.evosuite.testcase.execution.ExecutionResult} object.
     */
    public ExecutionResult execute(TestCase tc, int timeout) {
        Scope scope = new Scope();
        ExecutionResult result = execute(tc, scope, timeout);

        if (Properties.RESET_STATIC_FIELDS) {
            logger.debug("Resetting classes after execution");
            ClassReInitializer.getInstance().reInitializeClassesAfterTestExecution(tc, result);
        }
        return result;
    }

    /**
     * Execute a test case on an existing scope
     *
     * @param tc    a {@link org.evosuite.testcase.TestCase} object.
     * @param scope a {@link org.evosuite.testcase.execution.Scope} object.
     * @return a {@link org.evosuite.testcase.execution.ExecutionResult} object.
     */
    @SuppressWarnings("deprecation")
    private ExecutionResult execute(TestCase tc, Scope scope, int timeout) {
        ExecutionTracer.getExecutionTracer().clear();

        // TODO: Re-insert!
        resetObservers();
        ExecutionObserver.setCurrentTest(tc);
        MaxTestsStoppingCondition.testExecuted();
        Runtime.getInstance().resetRuntime();

        long startTime = System.currentTimeMillis();

        TimeoutHandler<ExecutionResult> handler = new TimeoutHandler<>();

        // #TODO steenbuck could be nicer (TestRunnable should be an interface
        TestRunnable callable = new TestRunnable(tc, scope, observers);
        callable.storeCurrentThreads();

        /*
         * FIXME: the sequence of "catch" with calls to "result.set" should be
         * re-factored, as these things should be (already) handled in
         * TestRunnable.call. If not, it should be explained, as it is not
         * necessarily obvious why some checks are done here, and others in
         * TestRunnable
         */

        try {
            // ExecutionResult result = task.get(timeout,
            // TimeUnit.MILLISECONDS);

            ExecutionResult result = null;

            // important to call it before setting up the sandbox
            SystemInUtil.getInstance().initForTestCase();
            JOptionPaneInputs.getInstance().initForTestCase();

            Sandbox.goingToExecuteSUTCode();
            TestGenerationContext.getInstance().goingToExecuteSUTCode();
            try {
                result = handler.execute(callable, executor, timeout, Properties.CPU_TIMEOUT);
            } finally {
                Sandbox.doneWithExecutingSUTCode();
                TestGenerationContext.getInstance().doneWithExecutingSUTCode();
            }

            PermissionStatistics.getInstance().countThreads(threadGroup.activeCount());
            result.setSecurityException(PermissionStatistics.getInstance().getAndResetExceptionInfo());
            /*
             * TODO: this will need proper care when we ll start to handle
             * threads in the search.
             */
            callable.killAndJoinClientThreads();

            /*
             * TODO: we might want to initialize the ExecutionResult here, once
             * we waited for all SUT threads to finish
             */

            long endTime = System.currentTimeMillis();
            timeExecuted += endTime - startTime;
            testsExecuted++;
            return result;
        } catch (ThreadDeath t) {
            logger.warn("Caught ThreadDeath during test execution");
            ExecutionResult result = new ExecutionResult(tc, null);
            result.setThrownExceptions(callable.getExceptionsThrown());
            result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
            ExecutionTracer.getExecutionTracer().clear();
            return result;

        } catch (InterruptedException e1) {
            logger.info("InterruptedException");
            ExecutionResult result = new ExecutionResult(tc, null);
            result.setThrownExceptions(callable.getExceptionsThrown());
            result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
            ExecutionTracer.getExecutionTracer().clear();
            return result;
        } catch (ExecutionException e1) {
            /*
             * An ExecutionException at this point, is most likely an error in
             * evosuite. As exceptions from the tested code are caught before
             * this.
             */
            System.setOut(systemOut);
            System.setErr(systemErr);

            logger.error("ExecutionException (this is likely a serious error in the framework)", e1);
            ExecutionResult result = new ExecutionResult(tc, null);
            result.setThrownExceptions(callable.getExceptionsThrown());
            result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
            ExecutionTracer.getExecutionTracer().clear();
            if (e1.getCause() instanceof Error) { // an error was thrown
                // somewhere in evosuite
                // code
                throw (Error) e1.getCause();
            } else if (e1.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e1.getCause();
            }
            return result; // FIXME: is this reachable?
        } catch (TimeoutException e1) {
            // System.setOut(systemOut);
            // System.setErr(systemErr);

            if (Properties.LOG_TIMEOUT) {
                logger.warn("Timeout occurred for " + Properties.TARGET_CLASS);
            }
            logger.info("TimeoutException, need to stop runner", e1);
            ExecutionTracer.setKillSwitch(true);
            try {
                handler.getLastTask().get(Properties.SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e2) {
            } catch (ExecutionException e2) {
            } catch (TimeoutException e2) {
            }
            // task.cancel(true);

            if (!callable.isRunFinished()) {
                logger.info("Cancelling thread:");
                for (StackTraceElement elem : currentThread.getStackTrace()) {
                    logger.info(elem.toString());
                }
                logger.info(tc.toCode());
                boolean loopCounter = LoopCounter.getInstance().isActivated();
                while (isInStaticInit()) {
                    // LoopCounter and killswitch check the stacktrace often
                    // and that is costly - to speed things up we deactivate it
                    // until we're outside the static constructor
                    LoopCounter.getInstance().setActive(false);
                    ExecutionTracer.setKillSwitch(false);
                    logger.info("Run still not finished, but awaiting for static initializer to finish.");

                    try {
                        executor.awaitTermination(Properties.SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        logger.info("Interrupted");
                        e.printStackTrace();
                    }
                }
                LoopCounter.getInstance().setActive(loopCounter);
                ExecutionTracer.setKillSwitch(true);

                if (!callable.isRunFinished()) {
                    handler.getLastTask().cancel(true);
                    logger.info("Run not finished, waiting...");
                    try {
                        executor.awaitTermination(Properties.SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        logger.info("Interrupted");
                        e.printStackTrace();
                    }
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
                logger.info("Run is finished - " + currentThread.isAlive() + ": " + getNumStalledThreads());

            }
            ExecutionTracer.disable();

            // TODO: If this is true, is this problematic?
            if (Sandbox.isOnAndExecutingSUTCode()) {
                Sandbox.doneWithExecutingSUTCode();
                TestGenerationContext.getInstance().doneWithExecutingSUTCode();
            }

            ExecutionResult result = new ExecutionResult(tc, null);
            result.setThrownExceptions(callable.getExceptionsThrown());
            result.reportNewThrownException(tc.size(), new TestCaseExecutor.TimeoutExceeded());
            result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
            ExecutionTracer.getExecutionTracer().clear();
            ExecutionTracer.setKillSwitch(false);
            ExecutionTracer.enable();
            System.setOut(systemOut);
            System.setErr(systemErr);

            return result;
        } finally {
            if (threadGroup != null)
                PermissionStatistics.getInstance().countThreads(threadGroup.activeCount());
            TestCluster.getInstance().handleRuntimeAccesses(tc);
        }
    }

    private boolean isInStaticInit() {
        for (StackTraceElement elem : currentThread.getStackTrace()) {
            if (elem.getMethodName().equals("<clinit>"))
                return true;
            if (elem.getMethodName().equals("loadClass") && elem.getClassName()
                    .equals(org.evosuite.instrumentation.InstrumentingClassLoader.class.getCanonicalName()))
                return true;
            // CFontManager is responsible for loading fonts
            // which can take seconds
            if (elem.getClassName().equals("sun.font.CFontManager"))
                return true;
        }
        return false;
    }

    /**
     * <p>
     * getNumStalledThreads
     * </p>
     *
     * @return a int.
     */
    public int getNumStalledThreads() {
        stalledThreads.removeIf(t -> !t.isAlive());
        return stalledThreads.size();
    }

    /**
     * {@inheritDoc}
     */
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
        currentThread.setName(TEST_EXECUTION_THREAD + "_" + threadCounter);
        threadCounter++;
        currentThread.setContextClassLoader(TestGenerationContext.getInstance().getClassLoaderForSUT());
        ExecutionTracer.setThread(currentThread);
        return currentThread;
    }

    public void setExecutionObservers(Set<ExecutionObserver> observers) {
        this.observers = observers;
    }

}
