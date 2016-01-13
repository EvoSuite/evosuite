package org.evosuite.coverage.epa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.examples.with.different.packagename.epa.ListItr;
import com.examples.with.different.packagename.epa.MyArrayList;
import com.examples.with.different.packagename.epa.MyBoundedStack;

public class TestEPAGoalsListItr extends TestEPATransitionCoverage {

	private static DefaultTestCase buildTestCase0() throws ClassNotFoundException, NoSuchMethodException {
		Class<?> listItrClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(ListItr.class.getName());
		Class<?> arrayListClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(MyArrayList.class.getName());

		EPATestCaseBuilder builder = new EPATestCaseBuilder();

		// adds "MyArrayList list = new MyArrayList();"
		Constructor<?> arrayListCtor = arrayListClass.getConstructor();
		VariableReference arrayListVar = builder.addConstructorStatement(arrayListCtor);

		// adds "ListItr itr = list.listIterator();"
		Method m = arrayListClass.getMethod("listIterator");
		VariableReference itrVar = builder.addMethodStatement(arrayListVar, m);

		// adds "Object object0 = new Object();"
		Constructor<Object> objectConstructor = Object.class.getConstructor();
		VariableReference object0 = builder.addConstructorStatement(objectConstructor);

		// adds "itr.add(object0);"
		Method addMethod = listItrClass.getMethod("add", Object.class);
		builder.addMethodStatement(itrVar, addMethod, object0);

		DefaultTestCase tc = builder.toTestCase();
		return tc;
	}

	private static final String LIST_ITR_EPA_XML = String.join(File.separator, System.getProperty("user.dir"), "src",
			"test", "resources", "epas", "ListItr.xml");

	@Before
	public void checkXMLFilename() {
		final File epaXMLFile = new File(LIST_ITR_EPA_XML);
		Assume.assumeTrue(epaXMLFile.exists());
	}

	@Test
	public void testGoalsCovered() throws ClassNotFoundException, NoSuchMethodException, SecurityException,
			FileNotFoundException, ParserConfigurationException, SAXException, IOException, MalformedEPATraceException {
		Properties.TARGET_CLASS = ListItr.class.getName();
		Properties.EPA_XML_PATH = LIST_ITR_EPA_XML;

		EPA epa = EPAFactory.buildEPA(LIST_ITR_EPA_XML);

		EPATransitionCoverageFactory factory = new EPATransitionCoverageFactory(Properties.TARGET_CLASS,
				Properties.EPA_XML_PATH);
		List<EPATransitionCoverageTestFitness> goals = factory.getCoverageGoals();
		assertEquals(69, goals.size());

		DefaultTestCase test = buildTestCase0();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(test);
		TestChromosome testChromosome = suite.getTestChromosome(0);


		EPATransitionCoverageSuiteFitness epaFitness = new EPATransitionCoverageSuiteFitness(LIST_ITR_EPA_XML);
		suite.addFitness(epaFitness);
		double suiteFitness = epaFitness.getFitness(suite);
		assertEquals(67.0, suiteFitness , 0.000000001);
		assertTrue(suiteFitness == suite.getFitness());

		ExecutionResult execResult = testChromosome.executeForFitnessFunction(epaFitness);
		List<EPATrace> epaTraces = EPATraceFactory.buildEPATraces(Properties.TARGET_CLASS, execResult.getTrace(),
				epa);
		
		assertEquals(1, epaTraces.size());

		int covered = 0;
		int uncovered = 0;
		double totalFitness = 0.0;
		for (EPATransitionCoverageTestFitness g : goals) {
			System.out.println(g.toString());
			double fitness = g.getFitness(testChromosome);
			if (fitness == 0.0) {
				covered++;
			} else {
				uncovered++;
			}
			totalFitness += fitness;
		}

		assertEquals(69, covered + uncovered);
		assertEquals(2, covered);
		assertEquals(67, uncovered);
		assertEquals(67.0, totalFitness, 0.00000000001);

	}

}
