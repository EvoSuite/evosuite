package org.evosuite.runtime.agent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;

import org.junit.*;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.agent.InstrumentingAgent;

import com.examples.with.different.packagename.agent.ConcreteTime;
import com.examples.with.different.packagename.agent.AbstractTime;
import com.examples.with.different.packagename.agent.ExtendingTimeC;
import com.examples.with.different.packagename.agent.SecondAbstractTime;
import com.examples.with.different.packagename.agent.SecondConcreteTime;
import com.examples.with.different.packagename.agent.TimeA;
import com.examples.with.different.packagename.agent.TimeB;
import com.examples.with.different.packagename.agent.TimeC;

/**
 * Note: this needs be run as an integration test (IT), as it requires
 * the creation of the jar file first.
 * This is automatically set up in the pom file, but the test might fail
 * if run directly from an IDE
 * 
 * @author arcuri
 *
 */
public class InstrumentingAgent_IT {

	private final boolean replaceCalls = RuntimeSettings.mockJVMNonDeterminism;

	@BeforeClass
	public static void initClass(){
		InstrumentingAgent.initialize();
	}
	
	@Before
	public void storeValues() {
		RuntimeSettings.mockJVMNonDeterminism = true;
	}

	@After
	public void resetValues() {
		RuntimeSettings.mockJVMNonDeterminism = replaceCalls;
	}


	@Test
	public void testTransformationInClassExtendingAbstract(){
		long expected = 42;
		org.evosuite.runtime.System.setCurrentTimeMillis(expected);
		try{
			InstrumentingAgent.activate();
			ConcreteTime time = new ConcreteTime();
			/*
			 * Using abstract class here would fail, as it would be loaded 
			 * by JUnit before any method (static, BeforeClass) of this test
			 * suite is executed, and so it would not get instrumented
			 */
			//AbstractTime time = new ConcreteTime();
			Assert.assertEquals(expected, time.getTime());
		} finally {
			InstrumentingAgent.deactivate();
		}
	}

	@Test
	public void testFailingTransformation(){
		long expected = 42;
		org.evosuite.runtime.System.setCurrentTimeMillis(expected);
		try{
			InstrumentingAgent.activate();
			SecondAbstractTime time = new SecondConcreteTime();
			/*
			 * Using abstract class here fails, as it would be loaded 
			 * by JUnit before any method (static, BeforeClass) of this test
			 * suite is executed, and so it is not instrumented
			 */			
			Assert.assertNotEquals(expected, time.getTime());
		} finally {
			InstrumentingAgent.deactivate();
		}
	}


	@Test
	public void testTime(){

		long now = System.currentTimeMillis();
		Assert.assertTrue("",TimeB.getTime() >= now);
		
		long expected = 42;
		org.evosuite.runtime.System.setCurrentTimeMillis(expected);

		try{
			InstrumentingAgent.activate();
			Assert.assertEquals(expected, TimeA.getTime());
		} finally {
			InstrumentingAgent.deactivate();
		}
	}
	
	
	@Test
	public void testTransformationInAbstractClass(){
		long expected = 42;
		org.evosuite.runtime.System.setCurrentTimeMillis(expected);
		try{
			InstrumentingAgent.activate();
			//com.examples.with.different.packagename.agent.AbstractTime time = new com.examples.with.different.packagename.agent.ConcreteTime();
			//Assert.assertEquals(expected, time.getTime());
		} finally {
			InstrumentingAgent.deactivate();
		}
	}

	
	
	@Test
	public void testTransformation(){
		long expected = 42;
		org.evosuite.runtime.System.setCurrentTimeMillis(expected);
		try{
			InstrumentingAgent.activate();
			TimeC time = new TimeC();
			Assert.assertEquals(expected, time.getTime());
		} finally {
			InstrumentingAgent.deactivate();
		}
	}

	@Test
	public void testTransformationInExtendingClass(){
		long expected = 42;
		org.evosuite.runtime.System.setCurrentTimeMillis(expected);
		try{
			InstrumentingAgent.activate();
			ExtendingTimeC time = new ExtendingTimeC();
			Assert.assertEquals(expected, time.getTime());
		} finally {
			InstrumentingAgent.deactivate();
		}
	}


	
	@Test
	public void testInstrumetation() throws Exception{
	
		try{
			InstrumentingAgent.activate();
			
			Instrumentation inst = InstrumentingAgent.getInstumentation();
			Assert.assertNotNull(inst);
			ClassLoader loader = this.getClass().getClassLoader();
			Assert.assertTrue(inst.isModifiableClass(loader.loadClass(TimeA.class.getName())));
			Assert.assertTrue(inst.isModifiableClass(loader.loadClass(TimeB.class.getName())));
			Assert.assertTrue(inst.isModifiableClass(loader.loadClass(TimeC.class.getName())));
			Assert.assertTrue(inst.isModifiableClass(loader.loadClass(ExtendingTimeC.class.getName())));
			Assert.assertTrue(inst.isModifiableClass(loader.loadClass(ConcreteTime.class.getName())));
			Assert.assertTrue(inst.isModifiableClass(loader.loadClass(AbstractTime.class.getName())));
			
		} finally{
			InstrumentingAgent.deactivate();
		}
	}
}

