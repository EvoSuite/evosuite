/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.sandbox.MSecurityManager;
import de.unisb.cs.st.evosuite.sandbox.Mocks;

/**
 * @author Gordon Fraser
 * 
 */
public class TestRunnable implements Callable<ExecutionResult> {

	private static Logger logger = Logger.getLogger(TestRunner.class);

	private final TestCase test;

	private Scope scope = null;

	private final boolean log = true;

	public boolean runFinished;

	// if SecurityManager should be changed
	private static boolean changeSM = Properties.SANDBOX;

	private static Mocks mocks = new Mocks();

	private static SecurityManager newManager = new MSecurityManager();

	private static ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

	private static PrintStream out = new PrintStream(byteStream);

	public Map<Integer, Throwable> exceptionsThrown = new HashMap<Integer, Throwable>();

	public List<ExecutionObserver> observers;

	public TestRunnable(TestCase tc, Scope scope, List<ExecutionObserver> observers) {
		test = tc;
		this.scope = scope;
		this.observers = observers;
		runFinished = false;
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

			SecurityManager oldManager = null;
			//SecurityManager newManager = null;

			if (changeSM) {
				// Current SecurityManager used by default
				oldManager = System.getSecurityManager();
				// Mocked SecurityManager that forbids all access to I/O, Network
				// etc
			}

			mocks.setUpMocks();
			// exceptionsThrown = test.execute(scope, observers, !log);
			for (Statement s : test.statements) {
				if (Thread.currentThread().isInterrupted() || Thread.interrupted()) {
					logger.info("Thread interrupted at statement " + num + ": "
					        + s.getCode());
					throw new TimeoutException();
				}
				if (logger.isDebugEnabled())
					logger.debug("Executing statement " + s.getCode());
				ExecutionTracer.statementExecuted();
				VariableReference returnValue = s.getReturnValue().clone();

				out.flush();
				byteStream.reset();

				// check if SecurityManager should be changed to mocked SecurityManager
				if (changeSM) {
					System.setSecurityManager(newManager);
				}
				Throwable exceptionThrown = s.execute(scope, out);

				// check if default SecurityManager should be set
				if (changeSM) {
					System.setSecurityManager(oldManager);
				}

				// During runtime the type of a variable might change
				// E.g. if declared Object, after the first run it will
				// be set to the actual class observed at runtime
				// If changed, we need to update all references
				if (!s.getReturnValue().equals(returnValue)) {
					for (int pos = num; pos < test.statements.size(); pos++) {
						test.statements.get(pos).replace(returnValue,
						                                 s.getReturnValue().clone());
					}
				}

				if (exceptionThrown != null) {
					exceptionsThrown.put(num, exceptionThrown);

					// exception_statement = num;
					if (log && logger.isDebugEnabled())
						logger.debug("Exception thrown in statement: " + s.getCode()
						        + " - " + exceptionThrown.getClass().getName() + " - "
						        + exceptionThrown.getMessage());
				}
				if (logger.isDebugEnabled())
					logger.debug("Done statement " + s.getCode());
				for (ExecutionObserver observer : observers) {
					observer.statement(num, scope, s.getReturnValue());
				}
				num++;
			}
			result.trace = ExecutionTracer.getExecutionTracer().getTrace();

		} catch (ThreadDeath e) {// can't stop these guys
			mocks.tearDownMocks();
			logger.info("Found error:");
			logger.info(test.toCode());
			e.printStackTrace();
			runFinished = true;
			throw e;
		} catch (TimeoutException e) {
			logger.info("Test timed out!");
		} catch (Throwable e) {
			logger.info("Exception at statement " + num + "! " + e);
			logger.info(test.toCode());
			if (e instanceof java.lang.reflect.InvocationTargetException) {
				logger.info("Cause: ");
				logger.info(e.getCause());
				e = e.getCause();
			}
			// exceptionThrown = e;
			e.printStackTrace();
			// System.exit(1);

		} // finally {
		runFinished = true;
		mocks.tearDownMocks();

		result.exceptions = exceptionsThrown;

		return result;
		//}
	}

}
