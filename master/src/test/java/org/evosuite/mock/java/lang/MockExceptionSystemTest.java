package org.evosuite.mock.java.lang;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.mock.java.lang.ExtendingRuntimeException;

public class MockExceptionSystemTest extends SystemTest {

	  @Test
	  public void testRuntimeException(){
		  String targetClass = ExtendingRuntimeException.class.getCanonicalName();

		  Properties.TARGET_CLASS = targetClass;
		  Properties.REPLACE_CALLS = true;
		  Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.LINE};

		  EvoSuite evosuite = new EvoSuite();
		  String[] command = new String[] { "-generateSuite", "-class", targetClass };
		  Object result = evosuite.parseCommandLine(command);

		  GeneticAlgorithm<?> ga = getGAFromResult(result);
		  TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		  Assert.assertNotNull(best);
		  Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	  }
}
