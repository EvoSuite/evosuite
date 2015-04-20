package org.evosuite.runtime.jvm;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ShutdownHookHandlerTest {

	@Before
	public void init(){
		ShutdownHookHandler.getInstance().initHandler();
	}

	@After
	public void tearDown(){
		//be sure no hook is left
		ShutdownHookHandler.getInstance().processWasHalted();
	}

	@Test
	public void testAddHook(){

		int n = ShutdownHookHandler.getInstance().getNumberOfAllExistingHooks();

		Runtime.getRuntime().addShutdownHook(new Thread(){});

		Assert.assertEquals(n+1, ShutdownHookHandler.getInstance().getNumberOfAllExistingHooks());				
	}

	@Test
	public void testDoubleInit(){
		int n = ShutdownHookHandler.getInstance().getNumberOfAllExistingHooks();

		Runtime.getRuntime().addShutdownHook(new Thread(){});

		//this should remove the above hook thread
		ShutdownHookHandler.getInstance().initHandler(); 
		
		Assert.assertEquals(n, ShutdownHookHandler.getInstance().getNumberOfAllExistingHooks());	
	}

	@Test
	public void testNormalExecution(){
		
		final int[] array = new int[1];
		final int value = 42;
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){array[0]=value;}
		});
		
		//value not modified yet
		Assert.assertNotEquals(value, array[0]);
		
		ShutdownHookHandler.getInstance().executeAddedHooks();
		
		//hook should had modified the value by now
		Assert.assertEquals(value, array[0]);
	}
	
	@Test
	public void testExecutionWithException(){
		int n = ShutdownHookHandler.getInstance().getNumberOfAllExistingHooks();
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){throw new IllegalStateException();}
		});
	
		try{
			ShutdownHookHandler.getInstance().executeAddedHooks();
			Assert.fail();
		} catch(IllegalStateException e){
			//expected
		}
		
		//even if failed, hook should had been removed
		Assert.assertEquals(n, ShutdownHookHandler.getInstance().getNumberOfAllExistingHooks());	
	}
}
