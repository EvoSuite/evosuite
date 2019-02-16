package org.evosuite.coverage.epa;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.examples.with.different.packagename.epa.BoundedStackWithInvalidadState;

public class TestInvalidState extends TestEPATransitionCoverage {

	private static final String BOUNDED_STACK_EPA_XML = String.join(File.separator, System.getProperty("user.dir"),
			"src", "test", "resources", "epas", "MyBoundedStack.xml");

	private int DEFAULT_TIMEOUT = Properties.TIMEOUT;

	@Before
	public void prepareTest() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		final File epaXMLFile = new File(BOUNDED_STACK_EPA_XML);
		Assume.assumeTrue(epaXMLFile.exists());
		Properties.EPA_XML_PATH = BOUNDED_STACK_EPA_XML;
		Properties.TIMEOUT = Integer.MAX_VALUE;
		EPAMonitor.reset();
	}

	@After
	public void tearDown() {
		Properties.EPA_XML_PATH = null;
		Properties.TIMEOUT = DEFAULT_TIMEOUT;
	}

	@Test
	public void testInvalidState() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IOException,
			SAXException, ParserConfigurationException {
		Properties.TARGET_CLASS = BoundedStackWithInvalidadState.class.getName();
		Properties.EPA_XML_PATH = BOUNDED_STACK_EPA_XML;
		Properties.CRITERION = new Properties.Criterion[] { Criterion.EPATRANSITION };

		EPATransitionCoverageFactory factory = new EPATransitionCoverageFactory(Properties.TARGET_CLASS,
				EPAFactory.buildEPA(Properties.EPA_XML_PATH));
		List<EPATransitionCoverageTestFitness> goals = factory.getCoverageGoals();
		assertEquals(7, goals.size());

		DefaultTestCase test = createTestCase0();
		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(test);
		TestChromosome testChromosome = suite.getTestChromosome(0);

		EPATransitionCoverageSuiteFitness epaFitness = new EPATransitionCoverageSuiteFitness(BOUNDED_STACK_EPA_XML);
		ExecutionResult execResult = testChromosome.executeForFitnessFunction(epaFitness);
		List<EPATrace> epaTraces = new LinkedList<EPATrace>(execResult.getTrace().getEPATraces());

		assertEquals(1, epaTraces.size());
		EPATrace epa_trace = epaTraces.get(0);
		assertEquals(3, epa_trace.getEpaTransitions().size());
		EPATransition t1 = epa_trace.getEpaTransitions().get(0);
		assertEquals(new EPANormalTransition(new EPAState("S0"), "MyBoundedStack()", new EPAState("S1")), t1);
		EPATransition t2 = epa_trace.getEpaTransitions().get(1);
		assertEquals(new EPANormalTransition(new EPAState("S1"), "push()", new EPAState("INVALID_OBJECT_STATE")), t2);
		EPATransition t3 = epa_trace.getEpaTransitions().get(2);
		assertEquals(new EPAExceptionalTransition(new EPAState("INVALID_OBJECT_STATE"), "pop()",
				new EPAState("INVALID_OBJECT_STATE"), NullPointerException.class.getName()), t3);

	}

	/**
	 * Builds the test case:
	 * 
	 * <code>
	 * stack = new BoundedStack(); 
	 * stack.push(10);
	 * stack.break(); 
	 * stack.push(10);
	 * </code>
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 */
	private DefaultTestCase createTestCase0() throws ClassNotFoundException, NoSuchMethodException {
		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		Constructor<?> constructor = clazz.getConstructor();
		EPATestCaseBuilder builder = new EPATestCaseBuilder();
		VariableReference bounded_stack_var = builder.addConstructorStatement(constructor);
		Method push_method = clazz.getMethod("push", int.class);
		VariableReference int_value_var = builder.addIntegerStatement(10);
		builder.addMethodStatement(bounded_stack_var, push_method, int_value_var);
		Method pop_method = clazz.getMethod("pop");
		builder.addMethodStatement(bounded_stack_var, pop_method);
		DefaultTestCase test = builder.toTestCase();
		return test;
	}

}
