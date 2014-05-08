package org.evosuite.testcase;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.seeding.ObjectPool;
import org.evosuite.seeding.ObjectPoolManager;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.pool.ClassDependingOnExceptionClass;
import com.examples.with.different.packagename.pool.DependencyClass;
import com.examples.with.different.packagename.pool.DependencyClassWithException;
import com.examples.with.different.packagename.pool.DependencySubClass;
import com.examples.with.different.packagename.pool.OtherClass;

public class TestPool extends SystemTest {

	private String pools = "";
	
	private double pPool = 0.0;
	
	private long budget = 0;
	
	@Before
	public void storeProperties() {
		pools  = Properties.OBJECT_POOLS;
		pPool  = Properties.P_OBJECT_POOL;
		budget = Properties.SEARCH_BUDGET;
	}
	
	@After
	public void restoreProperties() {
		Properties.OBJECT_POOLS = pools;
		Properties.P_OBJECT_POOL = pPool;
		Properties.SEARCH_BUDGET = budget;
	}
	
	@Test
	public void testPoolDependency() throws IOException {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DependencyClass.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 100000;
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testPool() throws IOException {
		File f = File.createTempFile("EvoSuiteTestPool",null, FileUtils.getTempDirectory());
		String filename = f.getAbsolutePath();
		f.delete();
		System.out.println(filename);
		
		
		
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DependencyClass.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 100000;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		ObjectPool pool = ObjectPool.getPoolFromTestSuite(best);
		pool.writePool(filename);
		System.out.println("EvolvedTestSuite:\n" + best);
		resetStaticVariables();
		setDefaultPropertiesForTestCases();

		targetClass = OtherClass.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.P_OBJECT_POOL = 1.0;
		Properties.OBJECT_POOLS = filename;
		Properties.SEARCH_BUDGET = 10000;
		ObjectPoolManager.getInstance().initialisePool();
		//Properties.SEARCH_BUDGET = 50000;

		command = new String[] { "-generateSuite", "-class", targetClass, "-Dobject_pools=" + filename };

		result = evosuite.parseCommandLine(command);
		ga = getGAFromResult(result);
		TestSuiteChromosome best2 = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best2);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best2.getCoverage(), 0.001);
		f = new File(filename);
		f.delete();

	}
	
	@Test
	public void testNoPool() throws IOException {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = OtherClass.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.P_OBJECT_POOL = 0.0;
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertTrue("Non-optimal coverage: ", best.getCoverage() < 1.0);

	}
	
	@Test
	public void testPoolWithSubClass() throws IOException {
		File f = File.createTempFile("EvoSuiteTestPool",null, FileUtils.getTempDirectory());
		String filename = f.getAbsolutePath();
		f.delete();
		System.out.println(filename);
		
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DependencySubClass.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		// It takes a bit longer to cover the branch here
		Properties.SEARCH_BUDGET = 50000;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		ObjectPool pool = ObjectPool.getPoolFromTestSuite(best);
		pool.writePool(filename);
		System.out.println("EvolvedTestSuite:\n" + best);
		resetStaticVariables();
		setDefaultPropertiesForTestCases();

		targetClass = OtherClass.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.P_OBJECT_POOL = 1.0;
		Properties.OBJECT_POOLS = filename;
		ObjectPoolManager.getInstance().initialisePool();

		command = new String[] { "-generateSuite", "-class", targetClass, "-Dobject_pools=" + filename };

		result = evosuite.parseCommandLine(command);

		ga = getGAFromResult(result);
		best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		f = new File(filename);
		f.delete();

	}
	
	@Test
	public void testPoolWithException() throws IOException {
		File f = File.createTempFile("EvoSuiteTestPool",null, FileUtils.getTempDirectory());
		String filename = f.getAbsolutePath();
		f.delete();
		System.out.println(filename);
		
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DependencyClassWithException.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		ObjectPool pool = ObjectPool.getPoolFromTestSuite(best);
		pool.writePool(filename);
		System.out.println("EvolvedTestSuite:\n" + best);
		
		resetStaticVariables();
		setDefaultPropertiesForTestCases();
		
		targetClass = ClassDependingOnExceptionClass.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.P_OBJECT_POOL = 1.0;
		Properties.OBJECT_POOLS = filename;
		ObjectPoolManager.getInstance().initialisePool();
		//Properties.SEARCH_BUDGET = 50000;

		command = new String[] { "-generateSuite", "-class", targetClass, "-Dobject_pools=" + filename };

		result = evosuite.parseCommandLine(command);

		ga = getGAFromResult(result);
		best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		f = new File(filename);
		f.delete();

	}
	
	@Test
	public void testNoPoolWithException() throws IOException {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ClassDependingOnExceptionClass.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.P_OBJECT_POOL = 0.0;
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertTrue("Non-optimal coverage: ", best.getCoverage() < 1.0);

	}
}
