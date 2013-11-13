/**
 * 
 */
package org.evosuite;

import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.ExampleFieldClass;
import com.examples.with.different.packagename.ExampleInheritedClass;
import com.examples.with.different.packagename.test.AbsTest;
import com.examples.with.different.packagename.test.ArrayTest;
import com.examples.with.different.packagename.test.AssignmentTest;
import com.examples.with.different.packagename.test.CallTest;
import com.examples.with.different.packagename.test.DepTest;
import com.examples.with.different.packagename.test.EmptyTest;
import com.examples.with.different.packagename.test.EnumTest;
import com.examples.with.different.packagename.test.EnumTest2;
import com.examples.with.different.packagename.test.ExampleComplexReturnClass;
import com.examples.with.different.packagename.test.ExampleObserverClass;
import com.examples.with.different.packagename.test.ExampleStaticVoidSetterClass;
import com.examples.with.different.packagename.test.FieldTest;
import com.examples.with.different.packagename.test.MemberClass;
import com.examples.with.different.packagename.test.ObjectTest;
import com.examples.with.different.packagename.test.ObserverTest;
import com.examples.with.different.packagename.test.PolyExample;
import com.examples.with.different.packagename.test.StaticFieldTest;
import com.examples.with.different.packagename.test.SwitchTest;

/**
 * @author Gordon Fraser
 * 
 */
public class TestRegression extends SystemTest {

	private GeneticAlgorithm<?> runTest(String targetClass) {
		EvoSuite evosuite = new EvoSuite();

		//Properties.CLIENT_ON_THREAD = true;
		Properties.TARGET_CLASS = targetClass;
		//Properties.resetTargetClass();
		Properties.SEARCH_BUDGET = 100000;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		//, "-cp",
		//        "target/test-classes", "-Dshow_progress=false"};
		//		        "-Dclient_on_thread=true", "-Dsearch_budget=100000" };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		return ga;
	}

	private void testCovered(String targetClass, int numGoals) {
		GeneticAlgorithm<?> ga = runTest(targetClass);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println(best.toString());
		// TODO: Need to fix the check, some reset is not working
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		Assert.assertEquals("Wrong number of target goals", numGoals,
		                    best.getNumOfCoveredGoals());
		Assert.assertEquals("Wrong fitness: ", 0.0, best.getFitness(), 0.00);
		Assert.assertTrue("Wrong number of statements: ", best.size() > 0);
	}

	@Test
	public void testAbs() {
		testCovered(AbsTest.class.getCanonicalName(), 3);
	}

	@Test
	public void testArray() {
		testCovered(ArrayTest.class.getCanonicalName(), 11);
	}

	@Test
	public void testAssignment() {
		Properties.CLASS_PREFIX = AssignmentTest.class.getCanonicalName().substring(0,
		                                                                            AssignmentTest.class.getCanonicalName().lastIndexOf('.'));
		testCovered(AssignmentTest.class.getCanonicalName(), 12);
	}

	@Test
	public void testCall() {
		// TODO: Should Callee be included or not??
		testCovered(CallTest.class.getCanonicalName(), 3);
	}

	@Test
	public void testDependency() {
		testCovered(DepTest.class.getCanonicalName(), 2);
		// TODO: Re-run with deprecated active
	}

	@Test
	public void testEmpty() {
		testCovered(EmptyTest.class.getCanonicalName(), 2);
	}

	@Test
	public void testEnum() {
		testCovered(EnumTest.class.getCanonicalName(), 6);
	}

	@Test
	public void testEnum2() {
		testCovered(EnumTest2.class.getCanonicalName(), 4);
	}

	@Test
	public void testComplexReturn() {
		testCovered(ExampleComplexReturnClass.class.getCanonicalName(), 3);
	}

	@Test
	public void testFieldClass() {
		testCovered(ExampleFieldClass.class.getCanonicalName(), 2);
	}

	@Test
	public void testInheritedClass() {
		testCovered(ExampleInheritedClass.class.getCanonicalName(), 3);
	}

	@Test
	public void testObserverClass() {
		testCovered(ExampleObserverClass.class.getCanonicalName(), 3);
	}

	@Test
	public void testStaticVoidSetter() {
		testCovered(ExampleStaticVoidSetterClass.class.getCanonicalName(), 2);
	}

	@Test
	public void testField() {
		testCovered(FieldTest.class.getCanonicalName(), 8);
	}

	@Test
	public void testMember() {
		testCovered(MemberClass.class.getCanonicalName(), 10);
	}

	//@Test
	//public void testMulti3Array() {
	//	testCovered(Multi3Array.class.getCanonicalName());
	//}

	@Test
	public void testObject() {
		testCovered(ObjectTest.class.getCanonicalName(), 5);
	}

	@Test
	public void testObserver() {
		testCovered(ObserverTest.class.getCanonicalName(), 3);
	}

	@Test
	public void testPoly() {
		testCovered(PolyExample.class.getCanonicalName(), 5);
	}

	@Test
	public void testStaticField() {
		testCovered(StaticFieldTest.class.getCanonicalName(), 5);
	}

	@Test
	public void testSwitch() {
		testCovered(SwitchTest.class.getCanonicalName(), 11);
	}

}
