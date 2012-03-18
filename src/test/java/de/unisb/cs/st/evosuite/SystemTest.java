package de.unisb.cs.st.evosuite;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.*;


/**
 * @author Andrea Arcuri
 * 
 */
public class SystemTest {

	@Before
	public void setDefaultPropertiesForTestCases(){
		Properties.HTML = false;
		Properties.SHOW_PROGRESS = false;
		Properties.SERIALIZE_RESULT = false;
		Properties.JUNIT_TESTS = false;
		Properties.PLOT = false;
		
		Properties.SEARCH_BUDGET = 1000;

		Properties.CLIENT_ON_THREAD = true;
	}
	
	/*
	 * stupid Maven plug-ins do not properly handle custom output directories
	 * when JUnit is run, ie problems with classpath :(
	 * 
	 *  even the solution below in the end didn't work, due to problems in how
	 *  class loaders are used inside JUnit
	 *
	@BeforeClass
	public static void hackClassPath(){
		try {
			ClassPathHacker.addFile(System.getProperty("user.dir") + File.separator+"target"+File.separator+"suts-for-system-testing");
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}
	*/
	
	/*
	 * this static variable is a safety net to be sure it is called only once. 
	 * static variables are shared and not re-initialized
	 * during a sequence of test cases.
	 */
	private static boolean hasBeenAlreadyRun = false;
	
	@BeforeClass
	public static void runSetup(){
		if(hasBeenAlreadyRun){
			return;
		}
		
		deleteEvoDirs();
		
		System.out.println("*** SystemTest: runSetup() ***");
		
		//String target = System.getProperty("user.dir") + File.separator+"target"+File.separator+"suts-for-system-testing";
		String target = System.getProperty("user.dir") + File.separator+"target"+File.separator+"test-classes";
		
		File targetDir = new File(target);
		try {
			Assert.assertTrue("Target directory does not exist: "+targetDir.getCanonicalPath(), targetDir.exists());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		Assert.assertTrue(targetDir.isDirectory());
		
		/*
		 * "stupid" Java does not allow to change the current directory:
		 * http://bugs.sun.com/bugdatabase/view%5Fbug.do?bug%5Fid=4045688
		 * 
		 *  so the setProperty("user.dir" has no effect on the java.io framework
		 */
		/*
		File newDir = null;
		try {
			newDir = File.createTempFile("foo", "");
			Assert.assertTrue(newDir.delete());
			Assert.assertTrue(newDir.mkdir());
			newDir.deleteOnExit();
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		
		System.setProperty("user.dir", newDir.getAbsolutePath());
		*/
		EvoSuite evosuite = new EvoSuite();
		String[] command = new String[]{				
				//EvoSuite.JAVA_CMD,
				"-setup",
				target
		};

		Object result = evosuite.parseCommandLine(command);
		Assert.assertNull(result);
		//File evoDir = new File(newDir.getAbsoluteFile()+File.separator+Properties.OUTPUT_DIR + File.separator+ "evosuite.properties");
		File evoProp = new File(Properties.OUTPUT_DIR + File.separator+ "evosuite.properties");
		Assert.assertTrue("It was not created: "+evoProp.getAbsolutePath(), evoProp.exists());
		
		hasBeenAlreadyRun = true;
	}
	
	/*
	 * it's giving some problems
	 */
	//@AfterClass
	public static void deleteEvoDirs(){
		//if(!hasBeenAlreadyRun){
			//return;
		//}
		
		System.out.println("*** SystemTest: deleteEvoDirs() ***");

		try {
			org.apache.commons.io.FileUtils.deleteDirectory(new File("evosuite-files"));
			org.apache.commons.io.FileUtils.deleteDirectory(new File("evosuite-report"));
			org.apache.commons.io.FileUtils.deleteDirectory(new File("evosuite-tests"));
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		hasBeenAlreadyRun = false;
	}
}


/*
class ClassPathHacker 
{

	private static final Class<?>[] parameters = new Class[]{URL.class};

	public static void addFile(String s) throws IOException 
	{
		File f = new File(s);
		addFile(f);
	}//end method

	public static void addFile(File f) throws IOException 
	{
		//addURL(f.toURL());
		addURL(f.toURI().toURL());
	}//end method


	public static void addURL(URL u) throws IOException {

		//URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		URLClassLoader sysloader = (URLClassLoader)ClassPathHacker.class.getClassLoader();
		Class<?> sysclass = URLClassLoader.class;

		try {
			Method method = sysclass.getDeclaredMethod("addURL",parameters);
			method.setAccessible(true);
			method.invoke(sysloader,new Object[]{ u });
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IOException("Error, could not add URL to system classloader");
		}//end try catch

	}//end method

}//end class
*/