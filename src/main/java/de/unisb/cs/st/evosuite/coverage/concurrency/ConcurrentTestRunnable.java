/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.sandbox.Sandbox;
import de.unisb.cs.st.evosuite.testcase.DefaultTestFactory;
import de.unisb.cs.st.evosuite.testcase.ExecutionObserver;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.InterfaceTestRunnable;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.PrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCluster;
import de.unisb.cs.st.evosuite.testcase.TestRunnable;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author Sebastian Steenbuck
 * Note: output for concurrent testcases is not provided at this point in time (read: it is not recorded)
 * #TODO steenbuck an abstract runnable should exist, most of this code is repeated in TestRunnable
 * 
 */
public class ConcurrentTestRunnable implements InterfaceTestRunnable {

	private static Logger logger = Logger.getLogger(ConcurrentTestRunnable.class);

	private final ConcurrentTestCase test;

	private Scope scope = null;

	private final boolean log = true;

	//#TODO steenbuck should be private
	public boolean runFinished;

	private static ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

	private static boolean print_to_system = false;//Properties.getPropertyOrDefault("print_to_system", true);

	private static PrintStream out = (print_to_system?System.out:new PrintStream(byteStream));
	private static final PrintStream sysoutOrg = System.out;
	private static final PrintStream syserrOrg = System.err;

	//#TODO steenbuck should be private
	public Map<Integer, Throwable> exceptionsThrown = new HashMap<Integer, Throwable>();

	//#TODO steenbuck should be private
	public List<ExecutionObserver> observers;


	public ConcurrentTestRunnable(TestCase tc, Scope scope, List<ExecutionObserver> observers) {
		if(tc instanceof ConcurrentTestCase){
			test = (ConcurrentTestCase)tc;
		}else{
			throw new AssertionError("Apparently the test case was constructed by the wrong factory");
		}
		//#TODO steenbuck we don't use this scope, maybe we need to wrap it
		this.scope = scope;
		this.observers = observers;
		runFinished = false;
	}



	//#TODO steenbuck here for some testing
	private VariableReference getInitialTest(BasicTestCase tc, Class<?> clazz){
		assert(clazz!=null);
		//TestCase tc = new TestCase();
		//TestCluster cluster = TestCluster.getInstance();
		//#TODO a random constructor should be used
		DefaultTestFactory tf = DefaultTestFactory.getInstance();
		Set<Constructor<?>> consts = TestCluster.getConstructors(clazz);
		assert(consts.size()>=1): "We need at least one constructor for the object under test ";
		for(Constructor<?> c : consts){
			try {
				VariableReference v = tf.addConstructor(tc, c, 0, 0); //not the nicest way to handle control flow	
				assert(tc.size()>=1): "Usually object creation needs at least one statement";
				return v;
			} catch (ConstructionFailedException e) {
				logger.fatal("Unhappy :(", e);
				//#TODO steenbuck in the long run this shouldn't be an exception but normal control flow
				System.exit(-1);
				return null;
			}
		}
		throw new AssertionError("We need at least one constructor for the object under test");
	}

	public final static Set<ConcurrentTestCase> t  = Collections.synchronizedSet(new HashSet<ConcurrentTestCase>());

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 * 
	 * we first call the setup stuff to get us an object of the type under test, and then start two threads giving them the generated object
	 * 
	 */
	@Override
	public ExecutionResult call() {
		CallLogger callLogger = new CallLogger();
		boolean log =true;
		if(t.contains(test)){
			log=false;
			((CallLogger)test.reporter).log=false;
			//AssertionError e = new AssertionError("A testcase is executed twice");
			//logger.fatal("going dark", e);
			//throw e;
			//System.exit(1);
		}else{
			t.add(test);
			test.setCallReporter(callLogger);
			test.setScheduleObserver(callLogger);
		}
		

		BasicTestCase initialTestCase = new BasicTestCase();
		assert(Properties.getTargetClass()!=null);
		VariableReference objectToTest = this.getInitialTest(initialTestCase, Properties.getTargetClass());
		assert(objectToTest!=null);
		Scope s = new Scope();
		Map<Integer, Throwable> m = new HashMap<Integer, Throwable>();

		execute(initialTestCase, s, m);
		assert(s.get(objectToTest)!=null);
		assert(m.keySet().size()==0);//TODO steenbuck for testing, in reality exceptions might be thrown.

		//#TODO all the thread start and stopping magic needs to happen here

		ControllerRuntime controller=new ControllerRuntime(test.getSchedule(), ConcurrencyCoverageFactory.THREAD_COUNT);
		FutureTask<Void> controllerFuture = new FutureTask<Void>(controller);
		LockRuntime.controller=controller;
		Thread controllerThread = new Thread(controllerFuture, "controllerThread");
		controllerThread.start();

		Set<FutureTask<ExecutionResult>> testFutures = new HashSet<FutureTask<ExecutionResult>>();


		System.setOut(out);
		System.setErr(out);
		for(int i=0 ; i<ConcurrencyCoverageFactory.THREAD_COUNT ; i++){
			ConcurrentTestCase testCopy = test.clone();
			if(log)testCopy.setCallReporter(callLogger);
			if(log)testCopy.setScheduleObserver(callLogger);
			ConcurrentTestCase testToExecute = addThreadEndCode(addThreadRegistrationStatements(testCopy));
			TestRunnable testRunner = new TestRunnable(testToExecute, new ConcurrentScope(s.get(objectToTest)), observers);
			FutureTask<ExecutionResult> testFuture = new FutureTask<ExecutionResult>(testRunner);
			Thread testThread = new Thread(testFuture, "TestThread" + i);
			testThread.start();
			testFutures.add(testFuture);
		}

		try{
			//#TODO do we need to combine the execution results? We will if the two threads run on different code
			ExecutionResult result=null;
			for(FutureTask<ExecutionResult> testFuture : testFutures){
				result = testFuture.get();
				assert(result!=null);
				assert(result.getTrace()!=null);
			}
			controllerFuture.get();
			return result;
		}catch(Throwable e){
			if(e.getCause()!=null)
				e.getCause().printStackTrace();
			logger.fatal("why....", e);
			e.printStackTrace();
			System.exit(1);
			return null;
		}finally{
			System.setOut(sysoutOrg);
			System.setErr(syserrOrg);
		}


		/*try {
			//System.out.println(r++);
			TestRunnable t2 = new TestRunnable(test.clone(), new ConcurrentScope(s.get(objectToTest)), observers);
			FutureTask<ExecutionResult> f2 = new FutureTask<ExecutionResult>(t2);
			Thread t= new Thread(f2);
			UncaughtExceptionHandler han = new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(Thread t, Throwable e) {
					System.out.println("uncaught exception" + t);
					e.printStackTrace();
				}
			};
			//t.setUncaughtExceptionHandler(han);
			//System.out.println("before");

			t.start();
			//dfs

			//f2.run();

			ExecutionResult result =  f2.get();
			//ConcurrentTestCaseExecutor exe=ConcurrentTestCaseExecutor.getInstance();
			//ExecutionResult result = exe.execute(test.clone(), new ConcurrentScope(s.get(objectToTest)));
			if(false)throw new InterruptedException();
			if(false)throw new ExecutionException(null);
			//System.exit(1);
			//ExecutionResult result=null;
			//System.exit(1);
			assert(result!=null);
			return result;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
			return null;
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
			return null;
		}catch (Throwable e){
			e.printStackTrace();
			System.exit(1);
			return null;
		}*/

		//return execute(test, testScope, exceptionsThrown);
		//}
	}

	/**
	 * #TODO only executed for the initial test case. Should 
	 * add thread registration code
	 * @param test
	 * @return
	 */
	private ConcurrentTestCase addThreadRegistrationStatements(ConcurrentTestCase test){
		try {

			Class<?> params[] = {int.class}; 
			Method register = LockRuntime.class.getMethod(LockRuntime.RUNTIME_REGISTER_THREAD_METHOD, params);

			StatementInterface idst = new PrimitiveStatement<Integer>(test, Integer.class, LockRuntime.getUniqueThreadID());
			test.addStatement(idst,0, false);
			VariableReference idRef = idst.getReturnValue();
			
			List<VariableReference> paramsThreadRegistration = new ArrayList<VariableReference>();
			paramsThreadRegistration.add(idRef);
			test.addStatement(new MethodStatement(test, register, null, Void.class, paramsThreadRegistration), 1, false);

			return test;

		} catch (Exception e) {
			logger.warn("Tried to get method " + LockRuntime.RUNTIME_REGISTER_THREAD_METHOD + " but couldn't find such a method");
			throw new AssertionError(e);
		}


	}

	/**
	 * Clone test and add thread end code
	 * @param test
	 * @return 
	 */
	private ConcurrentTestCase addThreadEndCode(ConcurrentTestCase test){
		Method[] methods = LockRuntime.class.getMethods();
		Method threadEnd=null;
		//#TODO should use getMethod()
		for(Method met : methods){
			if(met.getName().contains("threadEnd") && met.getParameterTypes().length==0)
				threadEnd=met;
		}

		test.addStatement(new MethodStatement(test, threadEnd, null, Void.class, new ArrayList<VariableReference>()), false);
		return test;
	}

	/**
	 * mods runFinished
	 * @return
	 */
	private ExecutionResult execute(BasicTestCase localTest, Scope localScope, Map<Integer, Throwable> exceptionsThrownLocal){
		runFinished = false;
		ExecutionResult result = new ExecutionResult(localTest, null);

		//System.out.println("test : " + TestCluster.getConstructors(Triangle.class).size());
		//;
		//System.exit(-1);

		int num = 0;
		try {
			Sandbox.setUpMocks();
			// exceptionsThrown = test.execute(scope, observers, !log);
			for (StatementInterface s : localTest) {
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
				Throwable exceptionThrown = s.execute(localScope, out);
				Sandbox.tearDownMockedSecurityManager();

				if (exceptionThrown != null) {
					exceptionsThrownLocal.put(num, exceptionThrown);

					// exception_statement = num;
					if (log && logger.isDebugEnabled())
						logger.debug("Exception thrown in statement: " + s.getCode()
								+ " - " + exceptionThrown.getClass().getName() + " - "
								+ exceptionThrown.getMessage());
				}
				if (logger.isDebugEnabled())
					logger.debug("Done statement " + s.getCode());
				for (ExecutionObserver observer : observers) {
					observer.statement(s, localScope, exceptionThrown);
				}
				num++;
			}

			result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());

		} catch (ThreadDeath e) {// can't stop these guys
			Sandbox.tearDownEverything();
			logger.info("Found error:");
			logger.info(localTest.toCode());
			e.printStackTrace();
			runFinished = true;
			throw e;
		} catch (TimeoutException e) {
			Sandbox.tearDownEverything();
			logger.info("Test timed out!");
		} catch (Throwable e) {
			Sandbox.tearDownEverything();
			logger.info("Exception at statement " + num + "! " + e);
			logger.info(localTest.toCode());
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
		Sandbox.tearDownMocks();

		result.exceptions = exceptionsThrownLocal;

		return result;
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
