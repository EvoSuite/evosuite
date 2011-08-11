/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.sandbox.Sandbox;

/**
 * @author Gordon Fraser
 * 
 */
public class TestRunnable implements InterfaceTestRunnable {

	private static Logger logger = LoggerFactory.getLogger(TestRunnable.class);

	private final TestCase test;

	private Scope scope = null;

	private final boolean log = true;

	public boolean runFinished;

	private static ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

	private static PrintStream out = (Properties.PRINT_TO_SYSTEM ? System.out
	        : new PrintStream(byteStream));

	public Map<Integer, Throwable> exceptionsThrown = new HashMap<Integer, Throwable>();

	public List<ExecutionObserver> observers;

	private final boolean breakOnUndeclaredException;

	public TestRunnable(TestCase tc, Scope scope, List<ExecutionObserver> observers) {
		this(tc, scope, observers, true);
	}

	/**
	 * 
	 * @param tc
	 * @param scope
	 * @param observers
	 * @param breakOnUndeclaredException
	 *            if this is true, the TestRunnable will exit if a statement
	 *            returns an UndeclaredException. (Note that undeclaredException
	 *            is defined by StatementInterface.isDeclaredException/1)
	 */
	public TestRunnable(TestCase tc, Scope scope, List<ExecutionObserver> observers,
	        boolean breakOnUndeclaredException) {
		test = tc;
		this.scope = scope;
		this.observers = observers;
		runFinished = false;
		this.breakOnUndeclaredException = breakOnUndeclaredException;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public ExecutionResult call() {

		runFinished = false;
		ExecutionResult result = new ExecutionResult(test, null);

		int num = 0;
		try {

			Sandbox.setUpMocks();
			// exceptionsThrown = test.execute(scope, observers, !log);
			for (StatementInterface s : test) {
				if (Thread.currentThread().isInterrupted() || Thread.interrupted()) {
					logger.info("Thread interrupted at statement " + num + ": "
					        + s.getCode());
					throw new TimeoutException();
				}
				if (logger.isDebugEnabled())
					logger.debug("Executing statement " + s.getCode());
				ExecutionTracer.statementExecuted();

				out.flush();
				byteStream.reset();

				Sandbox.setUpMockedSecurityManager();
				Throwable exceptionThrown = s.execute(scope, out);
				Sandbox.tearDownMockedSecurityManager();

				if (exceptionThrown != null) {
					exceptionsThrown.put(num, exceptionThrown);

					if (exceptionThrown instanceof SecurityException) {
						logger.debug("Security exception found: " + exceptionThrown);
						break;
					}

					/*
					 * #TODO this is a penalty for test cases which contain a statement that throws an undeclared exception.
					 * As those test cases are not going to be executed after the statement (i.e. no coverage for those parts is generated) 
					 * This comment should explain, why that behavior is desirable 
					 */
					if (breakOnUndeclaredException
					        && !s.isDeclaredException(exceptionThrown))
						break;

					// exception_statement = num; 
					if (log && logger.isDebugEnabled())
						logger.debug("Exception thrown in statement: " + s.getCode()
						        + " - " + exceptionThrown.getClass().getName() + " - "
						        + exceptionThrown.getMessage());
				}
				if (logger.isDebugEnabled())
					logger.debug("Done statement " + s.getCode());
				for (ExecutionObserver observer : observers) {
					observer.statement(s, scope, exceptionThrown);
				}
				num++;
			}
			result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());

		} catch (ThreadDeath e) {// can't stop these guys
			Sandbox.tearDownEverything();
			logger.info("Found error:");
			logger.info(test.toCode());
			logger.warn("Found error in " + test.toCode(), e);
			runFinished = true;
			throw e;
		} catch (TimeoutException e) {
			Sandbox.tearDownEverything();
			logger.info("Test timed out!");
		} catch (Throwable e) {
			Sandbox.tearDownEverything();
			logger.info("Exception at statement " + num + "! " + e);
			logger.info(test.toCode());
			if (e instanceof java.lang.reflect.InvocationTargetException) {
				logger.info("Cause: ");
				logger.info(e.getCause().toString(), e);
				e = e.getCause();
			}
			if (e instanceof AssertionError
			        && e.getStackTrace()[0].getClassName().contains("de.unisb.cs.st.evosuite")) {
				//e1.printStackTrace();
				logger.error("Assertion Error in evosuitecode, for statement \n"
				        + test.getStatement(num).getCode() + " \n which is number: "
				        + num + " testcase \n" + test.toCode(), e);
				throw (AssertionError) e;
			}
			result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
			ExecutionTracer.getExecutionTracer().clear();
			// exceptionThrown = e;
			logger.warn("Error while executing statement " + test.toCode(), e);
			// System.exit(1);

		} // finally {
		runFinished = true;
		Sandbox.tearDownMocks();

		result.exceptions = exceptionsThrown;

		return result;
		//}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.InterfaceTestRunnable#getExceptionsThrown()
	 */
	@Override
	public Map<Integer, Throwable> getExceptionsThrown() {
		return exceptionsThrown;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.InterfaceTestRunnable#isRunFinished()
	 */
	@Override
	public boolean isRunFinished() {
		return runFinished;
	}

}
