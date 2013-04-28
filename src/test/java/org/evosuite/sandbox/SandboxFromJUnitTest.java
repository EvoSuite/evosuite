package org.evosuite.sandbox;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.evosuite.Properties;
import org.junit.*;

public class SandboxFromJUnitTest {

	private static ExecutorService executor;
	
	@BeforeClass
	public static void initEvoSuiteFramework(){
		Assert.assertNull(System.getSecurityManager());
		
		Properties.getInstance();
		Sandbox.initializeSecurityManagerForSUT();
		executor = Executors.newCachedThreadPool();

	}
	
	@AfterClass
	public static void clearEvoSuiteFramework(){
		Assert.assertNotNull(System.getSecurityManager());	
		
		executor.shutdownNow();
		Sandbox.resetDefaultSecurityManager();

		Assert.assertNull(System.getSecurityManager());		
	}
	
	@Before
	public void initTest(){		
		Sandbox.goingToExecuteSUTCode();
	}
	
	@After
	public void doneWithTestCase(){
		Sandbox.doneWithExecutingSUTCode();	
	}
	
	
	@Test
	public void testExit() throws Exception{
		
		Future<?> future = executor.submit(new Runnable(){
			@Override
			public void run() {
		//-------
		Foo foo = new Foo();
		try{
			foo.tryToExit();
			Assert.fail();
		} catch(SecurityException e){
			//expected
		}
		//-------		
			}
		});
		future.get(5000, TimeUnit.MILLISECONDS);
		
	}
	
}


class Foo{
	
	public void tryToExit(){
		System.exit(0);
	}
}


