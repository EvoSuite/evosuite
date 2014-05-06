/**
 * 
 */
package org.evosuite.testcase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.setup.TestCluster;
import org.evosuite.utils.GenericAccessibleObject;
import org.evosuite.utils.GenericMethod;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.FactoryExample;

/**
 * @author Gordon Fraser
 * 
 */
public class TestFactoryTest extends SystemTest {

	@Before
	public void setupCluster() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = FactoryExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ASSERTIONS = false;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		// Object result = 
		evosuite.parseCommandLine(command);
		// GeneticAlgorithm<?> ga = getGAFromResult(result);
		// TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		// System.out.println("EvolvedTestSuite:\n" + best);
	}

	@Test
	public void testTestCalls() throws ConstructionFailedException,
	        NoSuchMethodException, SecurityException {
		List<GenericAccessibleObject<?>> testCalls = TestCluster.getInstance().getTestCalls();
		System.out.println(testCalls.toString());
		assertEquals("Expected 3 test calls, but got: " + testCalls.size() + ": "
		        + testCalls, 3, testCalls.size());
	}

	@Test
	public void testIntegerDependency() throws ConstructionFailedException,
	        NoSuchMethodException, SecurityException {
		TestFactory testFactory = TestFactory.getInstance();

		GenericMethod method = new GenericMethod(
		        FactoryExample.class.getMethod("testByte", byte.class, byte.class),
		        FactoryExample.class);
		DefaultTestCase test = new DefaultTestCase();
		testFactory.addMethod(test, method, 0, 0);
		String code = test.toCode();
		assertEquals(4, test.size());
		assertTrue(code.contains("FactoryExample0.testByte(byte0, byte1)"));
	}

	@Test
	public void testObjectDependencyReuse() throws ConstructionFailedException,
	        NoSuchMethodException, SecurityException {
		TestFactory testFactory = TestFactory.getInstance();

		GenericMethod method = new GenericMethod(
		        FactoryExample.class.getMethod("testByte", byte.class, byte.class),
		        FactoryExample.class);
		DefaultTestCase test = new DefaultTestCase();
		Properties.PRIMITIVE_REUSE_PROBABILITY = 1.0;
		Properties.OBJECT_REUSE_PROBABILITY = 1.0;
		testFactory.addMethod(test, method, 0, 0);
		testFactory.addMethod(test, method, 4, 0);
		String code = test.toCode();
		System.out.println(code);

		// With object reuse being 0, there should be no new instance of this object
		assertEquals(5, test.size());
		assertTrue(code.contains("FactoryExample0.testByte(byte0, byte1)"));
		assertFalse(code.contains("FactoryExample1"));
	}

	@Test
	public void testObjectDependencyNoReuse() throws ConstructionFailedException,
	        NoSuchMethodException, SecurityException {
		TestFactory testFactory = TestFactory.getInstance();

		GenericMethod method = new GenericMethod(
		        FactoryExample.class.getMethod("testByte", byte.class, byte.class),
		        FactoryExample.class);
		DefaultTestCase test = new DefaultTestCase();
		Properties.PRIMITIVE_REUSE_PROBABILITY = 0.0;
		Properties.OBJECT_REUSE_PROBABILITY = 0.0;
		testFactory.addMethod(test, method, 0, 0);
		testFactory.addMethod(test, method, 4, 0);
		String code = test.toCode();
		System.out.println(code);

		// With object reuse being 0, there should be no new instance of this object
		assertEquals(8, test.size());
		assertTrue(code.contains("FactoryExample0.testByte(byte0, byte1)"));
		// byte2 is the first return value
		assertTrue(code.contains("FactoryExample1.testByte(byte3, byte4"));
	}

	@Test
	public void testStaticMethod() throws ConstructionFailedException,
	        NoSuchMethodException, SecurityException {
		TestFactory testFactory = TestFactory.getInstance();

		GenericMethod method = new GenericMethod(
		        FactoryExample.class.getMethod("testStatic"), FactoryExample.class);
		DefaultTestCase test = new DefaultTestCase();
		testFactory.addMethod(test, method, 0, 0);
		assertEquals(1, test.size());
		testFactory.addMethod(test, method, 1, 0);
		assertEquals(2, test.size());
		String code = test.toCode();
		System.out.println(code);

		// With object reuse being 0, there should be no new instance of this object
		assertTrue(code.contains("FactoryExample.testStatic()"));
		// No instance
		assertFalse(code.contains("FactoryExample0"));
	}
}
