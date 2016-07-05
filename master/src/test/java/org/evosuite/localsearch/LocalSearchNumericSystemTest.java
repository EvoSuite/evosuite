/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.localsearch;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.evosuite.Properties;
import org.evosuite.Properties.LocalSearchBudgetType;
import org.evosuite.SystemTestBase;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.localsearch.DefaultLocalSearchObjective;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.localsearch.BranchCoverageMap;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.numeric.DoublePrimitiveStatement;
import org.evosuite.testcase.statements.numeric.FloatPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.localsearch.TestSuiteLocalSearch;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.localsearch.BasicDoubleExample;
import com.examples.with.different.packagename.localsearch.BasicFloatExample;
import com.examples.with.different.packagename.localsearch.BasicIntegerExample;
import com.examples.with.different.packagename.localsearch.DoubleLocalSearchExample;
import com.examples.with.different.packagename.localsearch.FloatLocalSearchExample;
import com.examples.with.different.packagename.localsearch.IntegerLocalSearchExample;

public class LocalSearchNumericSystemTest extends SystemTestBase {

	@Before
    public void init(){
        Properties.DSE_PROBABILITY = 0.0;
        Properties.PRIMITIVE_POOL = 0.0;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.TESTS;
		Properties.LOCAL_SEARCH_BUDGET = 1000;
		Properties.LOCAL_SEARCH_REFERENCES = false;
		Properties.LOCAL_SEARCH_ARRAYS = false;
		Properties.RESET_STATIC_FIELD_GETS = true;

    }
	
	private TestCase getIntTest(int x, int y) throws NoSuchMethodException, SecurityException, ConstructionFailedException, ClassNotFoundException {
		Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		GenericClass clazz = new GenericClass(sut);
		
		DefaultTestCase test = new DefaultTestCase();
		GenericConstructor gc = new GenericConstructor(clazz.getRawClass().getConstructors()[0], clazz);

		TestFactory testFactory = TestFactory.getInstance();
		VariableReference callee = testFactory.addConstructor(test, gc, 0, 0);
		VariableReference intVar1 = test.addStatement(new IntPrimitiveStatement(test, x));
		VariableReference intVar0 = test.addStatement(new IntPrimitiveStatement(test, y));

		Method m = clazz.getRawClass().getMethod("testMe", new Class<?>[] { int.class, int.class });
		GenericMethod method = new GenericMethod(m, sut);
		MethodStatement ms = new MethodStatement(test, method, callee, Arrays.asList(new VariableReference[] {intVar0, intVar1}));
		test.addStatement(ms);

		return test;
	}
	
	private TestCase getFloatTest(float x, float y) throws NoSuchMethodException, SecurityException, ConstructionFailedException, ClassNotFoundException {
		Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		GenericClass clazz = new GenericClass(sut);
		
		DefaultTestCase test = new DefaultTestCase();
		GenericConstructor gc = new GenericConstructor(clazz.getRawClass().getConstructors()[0], clazz);

		TestFactory testFactory = TestFactory.getInstance();
		VariableReference callee = testFactory.addConstructor(test, gc, 0, 0);
		VariableReference intVar1 = test.addStatement(new FloatPrimitiveStatement(test, x));
		VariableReference intVar0 = test.addStatement(new FloatPrimitiveStatement(test, y));

		Method m = clazz.getRawClass().getMethod("testMe", new Class<?>[] { float.class, float.class });
		GenericMethod method = new GenericMethod(m, sut);
		MethodStatement ms = new MethodStatement(test, method, callee, Arrays.asList(new VariableReference[] {intVar0, intVar1}));
		test.addStatement(ms);

		return test;
	}
	
	private TestCase getDoubleTest(double x, double y) throws NoSuchMethodException, SecurityException, ConstructionFailedException, ClassNotFoundException {
		Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		GenericClass clazz = new GenericClass(sut);
		
		DefaultTestCase test = new DefaultTestCase();
		GenericConstructor gc = new GenericConstructor(clazz.getRawClass().getConstructors()[0], clazz);

		TestFactory testFactory = TestFactory.getInstance();
		VariableReference callee = testFactory.addConstructor(test, gc, 0, 0);
		VariableReference intVar1 = test.addStatement(new DoublePrimitiveStatement(test, x));
		VariableReference intVar0 = test.addStatement(new DoublePrimitiveStatement(test, y));

		Method m = clazz.getRawClass().getMethod("testMe", new Class<?>[] { double.class, double.class });
		GenericMethod method = new GenericMethod(m, sut);
		MethodStatement ms = new MethodStatement(test, method, callee, Arrays.asList(new VariableReference[] {intVar0, intVar1}));
		test.addStatement(ms);

		return test;
	}
	
	private void runIntExample(int x, int y) throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		TestCase test = getIntTest(x, y);
		
		TestSuiteChromosome suite = new TestSuiteChromosome();
		BranchCoverageSuiteFitness fitness = new BranchCoverageSuiteFitness();

		BranchCoverageMap.getInstance().searchStarted(null);
		assertEquals(4.0, fitness.getFitness(suite), 0.1F);
		suite.addTest(test);
		assertEquals(1.0, fitness.getFitness(suite), 0.1F);
		
		TestSuiteLocalSearch localSearch = TestSuiteLocalSearch.selectTestSuiteLocalSearch();
		LocalSearchObjective<TestSuiteChromosome> localObjective = new DefaultLocalSearchObjective<TestSuiteChromosome>();
		localObjective.addFitnessFunction(fitness);
		localSearch.doSearch(suite, localObjective);
		System.out.println("Fitness: "+fitness.getFitness(suite));
		System.out.println("Test suite: "+suite);
		assertEquals(0.0, fitness.getFitness(suite), 0.1F);
		BranchCoverageMap.getInstance().searchFinished(null);
	}
	
	@Test
	public void testBasicIntLocalSearch() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = BasicIntegerExample.class.getCanonicalName();
		runIntExample(1, 1);
	}
	
	@Test
	public void testBasicIntLocalSearch2() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = BasicIntegerExample.class.getCanonicalName();
		runIntExample(-2342352, +23847235);
	}
	
	@Test
	public void testBasicIntLocalSearch3() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = BasicIntegerExample.class.getCanonicalName();
		runIntExample(0, 0);
	}
	
	@Test
	public void testIntLocalSearch() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = IntegerLocalSearchExample.class.getCanonicalName();
		runIntExample(1, 1);
	}
	
	@Test
	public void testIntLocalSearch2() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = IntegerLocalSearchExample.class.getCanonicalName();
		runIntExample(2, 2); // Many other numbers end up in local optima...
	}
	
	@Test
	public void testIntLocalSearch3() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = IntegerLocalSearchExample.class.getCanonicalName();
		runIntExample(0, 1);
		// 0, 0 is a local optimum
	}
	
	private void runFloatExample(float x, float y) throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		TestCase test = getFloatTest(x, y);
		
		TestSuiteChromosome suite = new TestSuiteChromosome();
		BranchCoverageSuiteFitness fitness = new BranchCoverageSuiteFitness();

		BranchCoverageMap.getInstance().searchStarted(null);
		assertEquals(4.0, fitness.getFitness(suite), 0.1F);
		suite.addTest(test);
		assertEquals(1.0, fitness.getFitness(suite), 0.1F);
		
		TestSuiteLocalSearch localSearch = TestSuiteLocalSearch.selectTestSuiteLocalSearch();
		LocalSearchObjective<TestSuiteChromosome> localObjective = new DefaultLocalSearchObjective<TestSuiteChromosome>();
		localObjective.addFitnessFunction(fitness);
		localSearch.doSearch(suite, localObjective);
		System.out.println("Fitness: "+fitness.getFitness(suite));
		System.out.println("Test suite: "+suite);
		assertEquals(0.0, fitness.getFitness(suite), 0.1F);
		BranchCoverageMap.getInstance().searchFinished(null);
	}
	
	@Test
	public void testBasicFloatLocalSearch() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = BasicFloatExample.class.getCanonicalName();
		runFloatExample(1F, 1F);
	}
	
	@Test
	public void testBasicFloatLocalSearch2() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = BasicFloatExample.class.getCanonicalName();
		runFloatExample(-124.3423432F, +124.124F);
	}
	
	@Test
	public void testBasicFloatLocalSearch3() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = BasicFloatExample.class.getCanonicalName();
		runFloatExample(0F, 0F);		
	}
	
	@Test
	public void testFloatLocalSearch() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = FloatLocalSearchExample.class.getCanonicalName();
		runFloatExample(1F, 1F);
	}
	
	@Test
	public void testFloatLocalSearch2() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = FloatLocalSearchExample.class.getCanonicalName();
		runFloatExample(-124.3423432F, +124.124F);
	}
	
	@Test
	public void testFloatLocalSearch3() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = FloatLocalSearchExample.class.getCanonicalName();
		runFloatExample(0F, 1.0F); // 0, 0 would be a local optimum	
	}
	
	private void runDoubleExample(double x, double y) throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		TestCase test = getDoubleTest(x, y);
		
		TestSuiteChromosome suite = new TestSuiteChromosome();
		BranchCoverageSuiteFitness fitness = new BranchCoverageSuiteFitness();

		BranchCoverageMap.getInstance().searchStarted(null);
		assertEquals(4.0, fitness.getFitness(suite), 0.1F);
		suite.addTest(test);
		assertEquals(1.0, fitness.getFitness(suite), 0.1F);
		
		TestSuiteLocalSearch localSearch = TestSuiteLocalSearch.selectTestSuiteLocalSearch();
		LocalSearchObjective<TestSuiteChromosome> localObjective = new DefaultLocalSearchObjective<TestSuiteChromosome>();
		localObjective.addFitnessFunction(fitness);
		localSearch.doSearch(suite, localObjective);
		System.out.println("Fitness: "+fitness.getFitness(suite));
		System.out.println("Test suite: "+suite);
		assertEquals(0.0, fitness.getFitness(suite), 0.1F);
		BranchCoverageMap.getInstance().searchFinished(null);
	}
	
	@Test
	public void testBasicDoubleLocalSearch() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = BasicDoubleExample.class.getCanonicalName();
		runDoubleExample(1.0, 1.0);
	}
	
	@Test
	public void testBasicDoubleLocalSearch2() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = BasicDoubleExample.class.getCanonicalName();
		runDoubleExample(121.3423432, -125.124);		
	}
	
	@Test
	public void testBasicDoubleLocalSearch3() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = BasicDoubleExample.class.getCanonicalName();
		runDoubleExample(0.0, 0.0);
	}
	
	@Test
	public void testDoubleLocalSearch() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = DoubleLocalSearchExample.class.getCanonicalName();
		runDoubleExample(1.0, 1.0);
	}
	
	@Test
	public void testDoubleLocalSearch2() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = DoubleLocalSearchExample.class.getCanonicalName();
		runDoubleExample(121.3423432, -125.124);		
	}
	
	@Test
	public void testDoubleLocalSearch3() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = DoubleLocalSearchExample.class.getCanonicalName();
		runDoubleExample(0.0, 1.0);
	}
}
