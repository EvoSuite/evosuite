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

import org.evosuite.PackageInfo;
import org.evosuite.Properties;
import org.evosuite.dse.VMError;
import org.evosuite.runtime.System.SystemExitException;
import org.evosuite.runtime.jvm.ShutdownHookHandler;
import org.evosuite.runtime.thread.KillSwitch;
import org.evosuite.runtime.thread.ThreadStopper;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * TestRunnable class.
 * </p>
 *
 * @author Gordon Fraser
 */
public class TestRunnable implements InterfaceTestRunnable {

    private static final Logger logger = LoggerFactory.getLogger(TestRunnable.class);

    private static final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

    private final TestCase test;

    private final Scope scope;

    protected boolean runFinished;

    /**
     * Map a thrown exception ('value') with the the position ('key') in the
     * test sequence in which it was thrown from.
     */
    protected Map<Integer, Throwable> exceptionsThrown = new HashMap<>();

    protected Set<ExecutionObserver> observers;

    protected final ThreadStopper threadStopper;

    /**
     * <p>
     * Constructor for TestRunnable.
     * </p>
     *
     * @param tc        a {@link org.evosuite.testcase.TestCase} object.
     * @param scope     a {@link org.evosuite.testcase.execution.Scope} object.
     * @param observers a {@link java.util.Set} object.
     */
    public TestRunnable(TestCase tc, Scope scope, Set<ExecutionObserver> observers) {
        test = tc;
        this.scope = scope;
        this.observers = observers;
        runFinished = false;

        KillSwitch killSwitch = ExecutionTracer::setKillSwitch;
        Set<String> threadsToIgnore = new LinkedHashSet<>();
        threadsToIgnore.add(TestCaseExecutor.TEST_EXECUTION_THREAD);
        threadsToIgnore.addAll(Arrays.asList(Properties.IGNORE_THREADS));

        threadStopper = new ThreadStopper(killSwitch, threadsToIgnore, Properties.TIMEOUT);
    }

    /**
     * <p>
     * After the test case is executed, if any SUT thread is still running, we
     * will wait for their termination. To identify which thread belong to SUT,
     * before test case execution we should check which are the threads that are
     * running.
     * </p>
     * <p>
     * WARNING: The sandbox might prevent accessing thread informations, so best
     * to call this method from outside this class
     * </p>
     */
    public void storeCurrentThreads() {
        threadStopper.storeCurrentThreads();
    }

    /**
     * Try to kill (and then join) the SUT threads. Killing the SUT threads is
     * important, because some spawn threads could just wait on objects/locks,
     * and so make the test case executions always last TIMEOUT ms.
     */
    public void killAndJoinClientThreads() throws IllegalStateException {
        threadStopper.killAndJoinClientThreads();
    }

    /**
     * Inform all observers that we are going to execute the input statement
     *
     * @param s the statement to execute
     */
    protected void informObservers_before(Statement s) {
        ExecutionTracer.disable();
        try {
            observers.forEach(o -> o.beforeStatement(s, scope));
        } finally {
            ExecutionTracer.enable();
        }
    }

    /**
     * Inform all observers that input statement has been executed
     *
     * @param s               the executed statement
     * @param exceptionThrown the exception thrown when executing the statement, if any (can
     *                        be null)
     */
    protected void informObservers_after(Statement s, Throwable exceptionThrown) {
        ExecutionTracer.disable();
        try {
            observers.forEach(o -> o.afterStatement(s, scope, exceptionThrown));
        } finally {
            ExecutionTracer.enable();
        }
    }

    protected void informObservers_finished(ExecutionResult result) {
        ExecutionTracer.disable();
        try {
            observers.forEach(o -> o.testExecutionFinished(result, scope));
        } finally {
            ExecutionTracer.enable();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionResult call() {

        exceptionsThrown.clear();

        runFinished = false;
        ExecutionResult result = new ExecutionResult(test, null);
        // TODO: Moved this to TestCaseExecutor so it is not part of the test execution timeout
        //		Runtime.getInstance().resetRuntime();
        ExecutionTracer.enable();

        PrintStream out = (Properties.PRINT_TO_SYSTEM ? System.out : new PrintStream(byteStream));
        byteStream.reset();

        if (!Properties.PRINT_TO_SYSTEM) {
            LoggingUtils.muteCurrentOutAndErrStream();
        }

        threadStopper.startRecordingTime();

        /*
         *  need AtomicInteger as we want to get latest updated value even if exception is thrown in the 'try' block.
         *  we practically use it as wrapper for int, which we can then pass by reference
         */
        AtomicInteger num = new AtomicInteger(0);

        try {
            if (Properties.REPLACE_CALLS) {
                ShutdownHookHandler.getInstance().initHandler();
            }

            executeStatements(result, out, num);
        } catch (ThreadDeath e) {// can't stop these guys
            logger.info("Found error in " + test.toCode(), e);
            throw e; // this needs to be propagated
        } catch (TimeoutException | TestCaseExecutor.TimeoutExceeded e) {
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
            if (e instanceof AssertionError
                    && e.getStackTrace()[0].getClassName().contains(PackageInfo.getEvoSuitePackage())) {
                logger.error("Assertion Error in evosuitecode, for statement \n"
                        + test.getStatement(num.get()).getCode() + " \n which is number: "
                        + num + " testcase \n" + test.toCode(), e);
                throw (AssertionError) e;
            }

            logger.error("Suppressed/ignored exception during test case execution on class "
                    + Properties.TARGET_CLASS + ": " + e.getMessage(), e);
        } finally {
            if (!Properties.PRINT_TO_SYSTEM) {
                LoggingUtils.restorePreviousOutAndErrStream();
            }
            if (Properties.REPLACE_CALLS) {
                /*
                 * For simplicity, we call it here. Ideally, we could call it among the
                 * statements, with "non-safe" version, to check if any exception is thrown.
                 * But that would be quite a bit of work, which maybe is not really warranted
                 */
                ShutdownHookHandler.getInstance().safeExecuteAddedHooks();
            }

            runFinished = true;
        }

        result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
        result.setExecutionTime(System.currentTimeMillis() - threadStopper.getStartTime());
        result.setExecutedStatements(num.get());
        result.setThrownExceptions(exceptionsThrown);
        result.setReadProperties(org.evosuite.runtime.System.getAllPropertiesReadSoFar());
        result.setWasAnyPropertyWritten(org.evosuite.runtime.System.wasAnyPropertyWritten());

        return result;
    }

    /**
     * Iterate over all statements in the test case, and execute them one at a time
     *
     * @param result
     * @param out
     * @param num
     * @throws TimeoutException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws VMError
     * @throws EvosuiteError
     */
    private void executeStatements(ExecutionResult result, PrintStream out,
                                   AtomicInteger num) throws TimeoutException,
            InvocationTargetException, IllegalAccessException,
            InstantiationException, VMError, EvosuiteError {

        for (Statement s : test) {

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
                // if internal error, then throw exception
                // -------------------------------------------------------
                if (exceptionThrown instanceof VMError) {
                    throw (VMError) exceptionThrown;
                }
                if (exceptionThrown instanceof EvosuiteError) {
                    throw (EvosuiteError) exceptionThrown;
                }
                // -------------------------------------------------------

                /*
                 * This is implemented in this way due to ExecutionResult.hasTimeout()
                 */
                if (exceptionThrown instanceof TestCaseExecutor.TimeoutExceeded) {
                    logger.debug("Test timed out!");
                    exceptionsThrown.put(test.size(), exceptionThrown);
                    result.setThrownExceptions(exceptionsThrown);
                    result.reportNewThrownException(test.size(), exceptionThrown);
                    result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
                    break;
                }

                // keep track if the exception and where it was thrown
                exceptionsThrown.put(num.get(), exceptionThrown);

                // check if it was an explicit exception
                // --------------------------------------------------------
                if (ExecutionTracer.getExecutionTracer().getLastException() == exceptionThrown) {
                    result.explicitExceptions.put(num.get(), true);
                } else {
                    result.explicitExceptions.put(num.get(), false);
                }
                // --------------------------------------------------------

                printDebugInfo(s, exceptionThrown);
                // --------------------------------------------------------

                /*
                 * If an exception is thrown, we stop the execution of the test case, because the internal state could be corrupted, and not
                 * possible to verify the behavior of any following function call. Predicate should be true by default
                 */
                if (Properties.BREAK_ON_EXCEPTION
                        || exceptionThrown instanceof SystemExitException) {
                    informObservers_after(s, exceptionThrown);
                    break;
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Done statement " + s.getCode());
            }

            informObservers_after(s, exceptionThrown);

            num.incrementAndGet();
        } // end of loop
        informObservers_finished(result);
        //TODO
    }

    private void printDebugInfo(Statement s, Throwable exceptionThrown) {
        // some debugging info
        // --------------------------------------------------------

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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, Throwable> getExceptionsThrown() {
        return new HashMap<>(exceptionsThrown);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunFinished() {
        return runFinished;
    }

}
