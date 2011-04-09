/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.omg.CORBA.TCKind;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.sandbox.Sandbox;
import de.unisb.cs.st.evosuite.testcase.*;

/**
 * @author Steenbuck
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

	private static boolean print_to_system = true;//Properties.getPropertyOrDefault("print_to_system", true);

	private static PrintStream out = (print_to_system?System.out:new PrintStream(byteStream));

	//#TODO steenbuck should be private
	public Map<Integer, Throwable> exceptionsThrown = new HashMap<Integer, Throwable>();

	//#TODO steenbuck should be private
	public List<ExecutionObserver> observers;


	public ConcurrentTestRunnable(de.unisb.cs.st.evosuite.testcase.TestCase tc, Scope scope, List<ExecutionObserver> observers) {
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
	private VariableReference getInitialTest(BasicTestCase tc, Class clazz){
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
				logger.fatal("Unhappy :(");
				e.printStackTrace();
				//#TODO steenbuck in the long run this shouldn't be an exception but normal control flow
				System.exit(-1);
				return null;
			}
		}
		throw new AssertionError("We need at least one constructor for the object under test");
	}

	public static volatile int r=0;

	private class ownThreadGroup extends ThreadGroup{
		public ownThreadGroup(String name){
			super(name);
		}


		@Override
		public void uncaughtException(Thread t, Throwable e) {
			System.out.println("bla uncaught exception handler");
			e.printStackTrace();
		}
	}


	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 * 
	 * we first call the setup stuff to get us an object of the type under test, and then start two threads giving them the generated object
	 * 
	 */
	@Override
	public ExecutionResult call() {
		BasicTestCase initialTestCase = new BasicTestCase(false);
		assert(Properties.getTargetClass()!=null);
		VariableReference objectToTest = this.getInitialTest(initialTestCase, Properties.getTargetClass());
		assert(objectToTest!=null);
		Scope s = new Scope();
		Map<Integer, Throwable> m = new HashMap<Integer, Throwable>();
		
		execute(initialTestCase, s, m);
		assert(s.get(objectToTest)!=null);
		assert(m.keySet().size()==0);//TODO steenbuck for testing, in reality exceptions might be thrown.


		//	System.out.println(objectToTest.getName());
		//	System.out.println(initialTestCase.size());


		//System.out.println("t" + this.toString());

		UncaughtExceptionHandler uh = new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				e.printStackTrace();
			}
		};

		//#TODO all the thread start and stopping magic needs to happen here
		ThreadGroup g1 = new ownThreadGroup("threadsdd");
		
		ControllerRuntime controller=new ControllerRuntime(test.getSchedule(), 2);
		FutureTask<Void> controllerFuture = new FutureTask<Void>(controller);
		LockRuntime.controller=controller;
		Thread controllerThread = new Thread(g1, controllerFuture);
		controllerThread.start();

		TestRunnable t1 = new TestRunnable(addThreadEndCode(addThreadRegistrationStatements(test.clone())), new ConcurrentScope(s.get(objectToTest)), observers);
		TestRunnable t2 = new TestRunnable(addThreadEndCode(addThreadRegistrationStatements(test.clone())), new ConcurrentScope(s.get(objectToTest)), observers);
		
		FutureTask<ExecutionResult> f1 = new FutureTask<ExecutionResult>(t1);
		FutureTask<ExecutionResult> f2 = new FutureTask<ExecutionResult>(t2);
		
		Thread.setDefaultUncaughtExceptionHandler(uh);
		Thread th1= new Thread(g1, f1);
		Thread th2= new Thread(g1, f2);

		th1.setUncaughtExceptionHandler(uh);
		th2.setUncaughtExceptionHandler(uh);
		th1.start();
		th2.start();


		try{
			
			//#TODO do we need to combine the execution results? We will if the two threads run on different code
			f1.get();
			ExecutionResult e = f2.get();
			controllerFuture.get();
			return e;
		}catch(Throwable e){
			e.printStackTrace();
			System.exit(1);
			return null;
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
	 * add thread registration code
	 * @param test
	 * @return
	 */
	private ConcurrentTestCase addThreadRegistrationStatements(ConcurrentTestCase test){
		Method[] methods = LockRuntime.class.getMethods();
		Method register=null;
		//#TODO should use getMethod()
		for(Method met : methods){
			if(met.getName().contains("registerThread") && met.getParameterTypes().length==1)
				register=met;
		}

		VariableReference idRef = new VariableReference(Integer.class, 0);
		test.addStatement(new PrimitiveStatement<Integer>(idRef, LockRuntime.getUniqueThreadID()),0);
		List<VariableReference> paramsThreadRegistration = new ArrayList<VariableReference>();
		paramsThreadRegistration.add(idRef);
		test.addStatement(new MethodStatement(register, null, new VariableReference(Void.class, 1), paramsThreadRegistration), 1);
		return test;
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

		test.addStatement(new MethodStatement(threadEnd, null, new VariableReference(Void.class, test.size()), new ArrayList<VariableReference>()));
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
			for (Statement s : localTest) {
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

				Sandbox.setUpMockedSecurityManager();
				Throwable exceptionThrown = s.execute(localScope, out);
				Sandbox.tearDownMockedSecurityManager();

				// During runtime the type of a variable might change
				// E.g. if declared Object, after the first run it will
				// be set to the actual class observed at runtime
				// If changed, we need to update all references
				if (!s.getReturnValue().equals(returnValue)) {
					for (int pos = num; pos < localTest.size(); pos++) {
						localTest.getStatement(pos).replace(returnValue,
								s.getReturnValue().clone());
					}
				}

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

			result.trace = ExecutionTracer.getExecutionTracer().getTrace();

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
