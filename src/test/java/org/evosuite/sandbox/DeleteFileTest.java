/**
 * 
 */
package org.evosuite.sandbox;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.examples.with.different.packagename.DeleteFileCommonsIO;
import com.examples.with.different.packagename.DeleteFileExample;
import com.examples.with.different.packagename.DeleteFileProcess;
import com.examples.with.different.packagename.DeleteFileThread;
import com.examples.with.different.packagename.sandbox.DeleteInStaticInitializer;

/**
 * @author Gordon Fraser
 * 
 */
public class DeleteFileTest extends SystemTest {
	
	private static final boolean DEFAULT_RESET_STATIC = Properties.RESET_STATIC_FIELDS; 
	
	@After
	public void tearDown(){
		Properties.RESET_STATIC_FIELDS = DEFAULT_RESET_STATIC;
	}
	
	@Test
	public void testDeleteStaticNoReset() throws IOException {

		Properties.RESET_STATIC_FIELDS = false;
		
		String tmpdir = System.getProperty("java.io.tmpdir");
		File toDelete = new File(tmpdir + File.separator
		        + "this_file_should_not_be_deleted_by_evosuite");
		FileUtils.write(toDelete, "BlahBlah");

		assertTrue(toDelete.exists());
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DeleteInStaticInitializer.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		assertTrue("File has been deleted: " + toDelete.getAbsolutePath(),
		           toDelete.exists());
		toDelete.delete();
	}
	
	@Test
	public void testDeleteStaticWithReset() throws IOException {

		Properties.RESET_STATIC_FIELDS = true;
		
		String tmpdir = System.getProperty("java.io.tmpdir");
		File toDelete = new File(tmpdir + File.separator
		        + "this_file_should_not_be_deleted_by_evosuite");
		FileUtils.write(toDelete, "BlahBlah");

		assertTrue(toDelete.exists());
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DeleteInStaticInitializer.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		assertTrue("File has been deleted: " + toDelete.getAbsolutePath(),
		           toDelete.exists());
		toDelete.delete();
	}
	
	
	@Test
	public void testDeleteOnExit() throws IOException {

		String tmpdir = System.getProperty("java.io.tmpdir");
		File toDelete = new File(tmpdir + File.separator
		        + "this_file_should_not_be_deleted_by_evosuite");
		FileUtils.write(toDelete, "BlahBlah");

		assertTrue(toDelete.exists());
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DeleteFileExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		assertTrue("File has been deleted: " + toDelete.getAbsolutePath(),
		           toDelete.exists());
		toDelete.delete();
	}

	@Test
	public void testDeleteOnThread() throws IOException {

		String tmpdir = System.getProperty("java.io.tmpdir");
		File toDelete = new File(tmpdir + File.separator
		        + "this_file_should_not_be_deleted_by_evosuite");
		FileUtils.write(toDelete, "BlahBlah");

		assertTrue(toDelete.exists());
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DeleteFileThread.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		assertTrue("File has been deleted: " + toDelete.getAbsolutePath(),
		           toDelete.exists());
		toDelete.delete();
	}

	@Test
	public void testDeleteOnProcess() throws IOException {
		Assume.assumeTrue(new File("/bin/rm").exists());

		String tmpdir = System.getProperty("java.io.tmpdir");
		File toDelete = new File(tmpdir + File.separator
		        + "this_file_should_not_be_deleted_by_evosuite");
		FileUtils.write(toDelete, "BlahBlah");

		assertTrue(toDelete.exists());
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DeleteFileProcess.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		assertTrue("File has been deleted: " + toDelete.getAbsolutePath(),
		           toDelete.exists());
		toDelete.delete();
	}

	@Test
	public void testDeleteCommonsIO() throws IOException {
		Assume.assumeTrue(new File("/bin/rm").exists());

		String tmpdir = System.getProperty("java.io.tmpdir");
		File toDelete = new File(tmpdir + File.separator
		        + "this_file_should_not_be_deleted_by_evosuite");
		FileUtils.write(toDelete, "BlahBlah");

		assertTrue(toDelete.exists());
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DeleteFileCommonsIO.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		assertTrue("File has been deleted: " + toDelete.getAbsolutePath(),
		           toDelete.exists());
		toDelete.delete();
	}

	/*
	 * The class DeleteFileNIO requires Java 7
	 *  
	@Test
	public void testDeleteNIO() throws IOException {
		Assume.assumeTrue(new File("/bin/rm").exists());

		String tmpdir = System.getProperty("java.io.tmpdir");
		File toDelete = new File(tmpdir + File.separator
		        + "this_file_should_not_be_deleted_by_evosuite");
		FileUtils.write(toDelete, "BlahBlah");

		assertTrue(toDelete.exists());
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DeleteFileNIO.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		assertTrue("File has been deleted: " + toDelete.getAbsolutePath(),
		           toDelete.exists());
		toDelete.delete();
	}
	*/

}
