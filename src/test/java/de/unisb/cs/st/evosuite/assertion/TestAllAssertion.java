/**
 * 
 */
package de.unisb.cs.st.evosuite.assertion;

import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.ExampleFieldClass;
import com.examples.with.different.packagename.ExampleInheritedClass;
import com.examples.with.different.packagename.ExampleObserverClass;
import com.examples.with.different.packagename.ExampleStaticVoidSetterClass;

import de.unisb.cs.st.evosuite.EvoSuite;
import de.unisb.cs.st.evosuite.SystemTest;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

/**
 * @author fraser
 * 
 */
public class TestAllAssertion extends SystemTest {

	private TestSuiteChromosome generateSuite(Class<?> clazz) {
		EvoSuite evosuite = new EvoSuite();
		int generations = 1;

		String targetClass = clazz.getCanonicalName();

		String[] command = new String[] {
		        //EvoSuite.JAVA_CMD,
		        "-generateTests", "-class", targetClass, "-Dhtml=false", "-Dplot=false",
		        "-Djunit_tests=false", "-Dshow_progress=false",
		        "-Dgenerations=" + generations, "-assertions",
		        "-Dassertion_strategy=all", "-Dserialize_result=true" };

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :" + result.getClass() + ", " + result,
		                  result instanceof GeneticAlgorithm);

		GeneticAlgorithm ga = (GeneticAlgorithm) result;
		return (TestSuiteChromosome) ga.getBestIndividual();
	}

	@Test
	public void test1() {
		TestSuiteChromosome suite = generateSuite(ExampleObserverClass.class);

		Assert.assertTrue(suite.size() > 0);
		for (TestCase test : suite.getTests()) {
			Assert.assertTrue("Test has no assertions: " + test.toCode(),
			                  test.hasAssertions());
		}
	}

	@Test
	public void test2() {
		TestSuiteChromosome suite = generateSuite(ExampleFieldClass.class);

		Assert.assertTrue(suite.size() > 0);
		for (TestCase test : suite.getTests()) {
			Assert.assertTrue("Test has no assertions: " + test.toCode(),
			                  test.hasAssertions());
		}
	}

	@Test
	public void test3() {
		TestSuiteChromosome suite = generateSuite(ExampleInheritedClass.class);

		Assert.assertTrue(suite.size() > 0);
		for (TestCase test : suite.getTests()) {
			Assert.assertTrue("Test has no assertions: " + test.toCode(),
			                  test.hasAssertions());
		}
	}

	@Test
	public void test4() {
		TestSuiteChromosome suite = generateSuite(ExampleStaticVoidSetterClass.class);

		Assert.assertTrue(suite.size() > 0);
		for (TestCase test : suite.getTests()) {
			Assert.assertTrue("Test has no assertions: " + test.toCode(),
			                  test.hasAssertions());
		}
	}

}
