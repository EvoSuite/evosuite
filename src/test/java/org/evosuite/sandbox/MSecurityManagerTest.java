package org.evosuite.sandbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import  org.junit.*;

public class MSecurityManagerTest {

	private static ExecutorService executor;
	private static  MSecurityManager securityManager; 
	
	@BeforeClass
	public static void initClass(){
		executor = Executors.newCachedThreadPool();
		securityManager = new MSecurityManager();
	}
	
	@AfterClass
	public static void doneWithClass(){
		executor.shutdownNow();
	}
	
	@Before
	public void initTest(){
		securityManager.apply();
		securityManager.goingToExecuteTestCase();
	}
	
	@After
	public void doneWithTestCase(){
		securityManager.goingToEndTestCase();
		securityManager.restoreDefaultManager();		
	}
	
	
	@Test
	public void testReadButNotWriteOfFiles() throws IOException, InterruptedException, ExecutionException, TimeoutException{
		
		File tmp = null;
		
		final String text = "EvoSuite rock!";
		
		try{
			//even if securityManager is on, the thread that set it should be able to write files
			tmp = File.createTempFile("foo", "tmp");
			tmp.deleteOnExit(); //just in case...
			
			BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
			out.write(text);
			out.flush();
			out.close();
			
			final String fileName = tmp.getAbsolutePath();
			

			
			//check that reading is fine
			Future<?> future = executor.submit(new Runnable(){
				@Override
				public void run() {
					try {
						File reading = new File(fileName);
						BufferedReader in = new BufferedReader(new FileReader(reading));
						String input = in.readLine();
						Assert.assertEquals(text, input);
						in.close();
					} catch (Exception e) {
						throw new Error(e);
					}
				}
				
			});			
			future.get(1000, TimeUnit.MILLISECONDS);
			
			//check that writing is forbidden
			future = executor.submit(new Runnable(){
				@Override
				public void run() {
					try {
						BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
						out.write(text);
						out.flush();
						out.close();
					} catch(SecurityException se){
						throw se;
					} catch (Exception e) {
						throw new Error(e);
					}
				}
				
			});			
			try{
				future.get(1000, TimeUnit.MILLISECONDS);
				Assert.fail();
			} catch(ExecutionException e){
				if(! (e.getCause() instanceof SecurityException) ){
					Assert.fail();
				}
			}
			
		} finally {
			if(tmp!=null){
				tmp.delete();
			}
		}
	}

	@Test
	public void testReadAndWriteOfProperties() throws InterruptedException, ExecutionException, TimeoutException{
		final String userDir = System.getProperty("user.dir");
		Assert.assertNotNull(userDir);
		
		final String rocks = "EvoSuite Rocks!";
		Assert.assertNotSame(rocks, userDir);

		
		//check that reading is fine
		Future<?> future = executor.submit(new Runnable(){
			@Override
			public void run() {
				String readUserDir = System.getProperty("user.dir");
				Assert.assertEquals(userDir, readUserDir);
				System.setProperty("user.dir", rocks);
			}			
		});	
		future.get(1000, TimeUnit.MILLISECONDS);
		
		String modified = System.getProperty("user.dir");
		Assert.assertEquals(rocks, modified);
		
		//now, "stopping" the test case should re-store value
		try{
			securityManager.goingToEndTestCase();		
			modified = System.getProperty("user.dir");
			Assert.assertEquals(userDir, modified);
		} finally {
			securityManager.goingToExecuteTestCase(); //needed
		}
	}
	
	@Test
	public void testCanLoadSwingStuff() throws InterruptedException, ExecutionException, TimeoutException{
		/*
		 * Note: this test is not particularly robust. Eg, one thing it tests is whether SUT can load
		 * the gui native code but that "could" be already loaded (shouldn't be though)
		 */
		Future<?> future = executor.submit(new Runnable(){
			@Override
			public void run() {
				try {
					/*
					 * Note, this JUnit class shouldn't have a static link to AWt (eg "import"), otherwise this
					 * test would be pointless
					 */
					Class.forName("javax.swing.JFrame");
				} catch (ClassNotFoundException e) {
					throw new Error(e);
				}
			}			
		});	
		future.get(1000, TimeUnit.MILLISECONDS);
	}
}
