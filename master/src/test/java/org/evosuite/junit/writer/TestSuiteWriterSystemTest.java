package org.evosuite.junit.writer;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.junit.writer.Foo;

public class TestSuiteWriterSystemTest extends SystemTest {

	
	@Test
	public void testSingleFile(){		
		Properties.TEST_SCAFFOLDING = false;
		test();
	}
	
	@Test
	public void testScaffoldingFile(){		
		Properties.TEST_SCAFFOLDING = true;
		test();
	}
	
	
	public void test(){

		Assert.assertNull(System.getSecurityManager());
		
		String targetClass = Foo.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.JUNIT_TESTS = true;
		Properties.JUNIT_CHECK = true;
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		EvoSuite evosuite = new EvoSuite();
		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}
