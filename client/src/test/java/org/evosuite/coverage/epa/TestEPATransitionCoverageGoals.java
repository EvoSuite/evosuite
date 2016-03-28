package org.evosuite.coverage.epa;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.examples.with.different.packagename.epa.MyBoundedStack;

public class TestEPATransitionCoverageGoals extends TestEPATransitionCoverage {

	private static final String BOUNDED_STACK_EPA_XML = String.join(File.separator, System.getProperty("user.dir"),
			"src", "test", "resources", "epas", "MyBoundedStack.xml");


	private int DEFAULT_TIMEOUT = Properties.TIMEOUT;

	@Before
	public void prepareTest() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		final File epaXMLFile = new File(BOUNDED_STACK_EPA_XML);
		Assume.assumeTrue(epaXMLFile.exists());
		Properties.EPA_XML_PATH = BOUNDED_STACK_EPA_XML;
		Properties.TIMEOUT = Integer.MAX_VALUE;
	}

	@After
	public void tearDown() {
		Properties.EPA_XML_PATH = null;
		Properties.TIMEOUT = DEFAULT_TIMEOUT;
	}

	@Test
	public void testGoalsCovered() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = MyBoundedStack.class.getName();
		Properties.EPA_XML_PATH = BOUNDED_STACK_EPA_XML;
		Properties.CRITERION = new Properties.Criterion[] { Criterion.EPATRANSITION };

		EPATransitionCoverageFactory factory = new EPATransitionCoverageFactory(Properties.TARGET_CLASS,
				Properties.EPA_XML_PATH);
		List<EPATransitionCoverageTestFitness> goals = factory.getCoverageGoals();
		assertEquals(7, goals.size());

		DefaultTestCase test = createTestCase0();
		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(test);
		TestChromosome testChromosome = suite.getTestChromosome(0);

		int covered = 0;
		int uncovered = 0;
		double totalFitness = 0.0;
		for (EPATransitionCoverageTestFitness g : goals) {
			double fitness = g.getFitness(testChromosome);
			if (fitness == 0.0) {
				covered++;
			} else {
				uncovered++;
			}
			totalFitness += fitness;
		}

		assertEquals(7, covered + uncovered);
		assertEquals(1, covered);
		assertEquals(6, uncovered);
		assertEquals(6.0, totalFitness, 0.00000000001);

	}

	private DefaultTestCase createTestCase0() throws ClassNotFoundException, NoSuchMethodException {
		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		Constructor<?> constructor = clazz.getConstructor();
		EPATestCaseBuilder builder = new EPATestCaseBuilder();
		builder.addConstructorStatement(constructor);
		DefaultTestCase test = builder.toTestCase();
		return test;
	}

}
