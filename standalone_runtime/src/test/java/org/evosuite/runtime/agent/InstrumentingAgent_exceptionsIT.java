package org.evosuite.runtime.agent;

import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.instrumentation.MethodCallReplacementCache;
import org.evosuite.runtime.mock.EvoSuiteMock;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.java.lang.MockThrowable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.examples.with.different.packagename.agent.ExceptionHolder;

public class InstrumentingAgent_exceptionsIT {

	private final boolean replaceCalls = RuntimeSettings.mockJVMNonDeterminism;

	public static NullPointerException getNPE(){
		return new NullPointerException("This shouldn't be mocked");
	}


	@BeforeClass
	public static void initClass(){
		InstrumentingAgent.initialize();
	}

	@Before
	public void storeValues() {
		RuntimeSettings.mockJVMNonDeterminism = true;
        MethodCallReplacementCache.resetSingleton();
		Runtime.getInstance().resetRuntime();
	}

	@After
	public void resetValues() {
		RuntimeSettings.mockJVMNonDeterminism = replaceCalls;
	}


	@Test
	public void testExceptions(){

		Object obj = null;

		try{
			InstrumentingAgent.activate();
			obj = new ExceptionHolder();
		} finally {
			InstrumentingAgent.deactivate();
		}

		try{
			MockFramework.enable();
			ExceptionHolder eh = (ExceptionHolder) obj;

			Assert.assertFalse(eh.getNonMockedNPE() instanceof EvoSuiteMock);
			Assert.assertTrue(eh.getMockedThrowable() instanceof EvoSuiteMock);

			StackTraceElement[] traces = new MockThrowable().getStackTrace();

			StackTraceElement[] a = eh.getTracesWhenCast();
			Assert.assertEquals(traces[0], a[0]);
			
		} finally{
			MockFramework.disable();
		}
	}


	@Test
	public void testStaticClassExceptions(){

		Object obj = null;

		try{
			InstrumentingAgent.activate();			
			obj = new ExceptionHolder.StaticPublicException();
		} finally {
			InstrumentingAgent.deactivate();
		}

		try{
			MockFramework.enable();

			Exception foo = (ExceptionHolder.StaticPublicException) obj;
			Assert.assertTrue(foo instanceof EvoSuiteMock);		
		} finally{
			MockFramework.disable();
		}
	}
	
	@Test
	public void testReplacementCallInSubClassOfException(){
		Object obj = null;

		try{
			InstrumentingAgent.activate();
			obj = new ExceptionHolder();
		} finally {
			InstrumentingAgent.deactivate();
		}

		try{
			MockFramework.enable();
			
			StackTraceElement[] traces = new MockThrowable().getStackTrace();
			
			ExceptionHolder eh = (ExceptionHolder) obj;
			StackTraceElement[] b = eh.getTraces();
			Assert.assertEquals(traces[0], b[0]);

		} finally{
			MockFramework.disable();
		}

	}
	
}
