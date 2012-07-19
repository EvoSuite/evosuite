/**
 * 
 */
package org.evosuite;

import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import test.AbsTest;
import test.ArrayTest;
import test.AssignmentTest;
import test.CallTest;
import test.DepTest;
import test.EmptyTest;
import test.EnumTest;
import test.EnumTest2;
import test.ExampleComplexReturnClass;
import test.ExampleObserverClass;
import test.ExampleStaticVoidSetterClass;
import test.FieldTest;
import test.MemberClass;
import test.ObjectTest;
import test.ObserverTest;
import test.PolyExample;
import test.StaticFieldTest;
import test.SwitchTest;

import com.examples.with.different.packagename.ExampleFieldClass;
import com.examples.with.different.packagename.ExampleInheritedClass;

/**
 * @author Gordon Fraser
 * 
 */
public class TestRegression {

	private GeneticAlgorithm runTest(String targetClass) {
		EvoSuite evosuite = new EvoSuite();

		Properties.CLIENT_ON_THREAD = true;
		Properties.TARGET_CLASS = targetClass;

		String[] command = new String[] { "-generateSuite", "-class", targetClass, "-cp",
		        "target/test-classes", "-Dshow_progress=false",
		        "-Dclient_on_thread=true", "-Dsearch_budget=100000" };

		Object result = evosuite.parseCommandLine(command);
		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :" + result.getClass(),
		                  result instanceof GeneticAlgorithm);

		return (GeneticAlgorithm) result;
	}

	private void testCovered(String targetClass) {
		GeneticAlgorithm ga = runTest(targetClass);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		Assert.assertEquals("Wrong fitness: ", 0.0, best.getFitness(), 0.00);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		Assert.assertTrue("Wrong number of statements: ", best.size() > 0);
	}

	@Test
	public void testAbs() {
		testCovered(AbsTest.class.getCanonicalName());
	}

	@Test
	public void testArray() {
		testCovered(ArrayTest.class.getCanonicalName());
	}

	@Test
	public void testAssignment() {
		testCovered(AssignmentTest.class.getCanonicalName());
	}

	@Test
	public void testCall() {
		testCovered(CallTest.class.getCanonicalName());
	}

	@Test
	public void testDependency() {
		testCovered(DepTest.class.getCanonicalName());
	}

	@Test
	public void testEmpty() {
		testCovered(EmptyTest.class.getCanonicalName());
	}

	@Test
	public void testEnum() {
		testCovered(EnumTest.class.getCanonicalName());
	}

	@Test
	public void testEnum2() {
		testCovered(EnumTest2.class.getCanonicalName());
	}

	@Test
	public void testComplexReturn() {
		testCovered(ExampleComplexReturnClass.class.getCanonicalName());
	}

	@Test
	public void testFieldClass() {
		testCovered(ExampleFieldClass.class.getCanonicalName());
	}

	@Test
	public void testInheritedClass() {
		testCovered(ExampleInheritedClass.class.getCanonicalName());
	}

	@Test
	public void testObserverClass() {
		testCovered(ExampleObserverClass.class.getCanonicalName());
	}

	@Test
	public void testStaticVoidSetter() {
		testCovered(ExampleStaticVoidSetterClass.class.getCanonicalName());
	}

	@Test
	public void testField() {
		testCovered(FieldTest.class.getCanonicalName());
	}

	@Test
	public void testMember() {
		testCovered(MemberClass.class.getCanonicalName());
	}

	//@Test
	//public void testMulti3Array() {
	//	testCovered(Multi3Array.class.getCanonicalName());
	//}

	@Test
	public void testObject() {
		testCovered(ObjectTest.class.getCanonicalName());
	}

	@Test
	public void testObserver() {
		testCovered(ObserverTest.class.getCanonicalName());
	}

	@Test
	public void testPoly() {
		testCovered(PolyExample.class.getCanonicalName());
	}

	@Test
	public void testStaticField() {
		testCovered(StaticFieldTest.class.getCanonicalName());
	}

	@Test
	public void testSwitch() {
		testCovered(SwitchTest.class.getCanonicalName());
	}

}
