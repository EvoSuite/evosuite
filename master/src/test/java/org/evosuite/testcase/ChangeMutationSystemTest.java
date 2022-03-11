/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.testcase;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.coverage.line.LineCoverageSuiteFitness;
import org.evosuite.coverage.mutation.WeakMutationSuiteFitness;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.generic.*;
import org.junit.After;
import org.junit.Test;

import com.examples.with.different.packagename.TrivialInt;
import com.examples.with.different.packagename.coverage.IntExampleWithNoElse;

public class ChangeMutationSystemTest extends SystemTestBase {

    private double oldPInsert = Properties.P_TEST_INSERT;
    private double oldPDelete = Properties.P_TEST_DELETE;
    private double oldPChange = Properties.P_TEST_CHANGE;
    private double oldPPool = Properties.PRIMITIVE_POOL;

    @After
    public void restoreProperties() {
        Properties.P_TEST_INSERT = oldPInsert;
        Properties.P_TEST_DELETE = oldPDelete;
        Properties.P_TEST_CHANGE = oldPChange;
        Properties.PRIMITIVE_POOL = oldPPool;
    }

    private TestCase getIntTest(int x) throws NoSuchMethodException, SecurityException, ConstructionFailedException, ClassNotFoundException {
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
        GenericClass<?> clazz = GenericClassFactory.get(sut);

        DefaultTestCase test = new DefaultTestCase();
        GenericConstructor gc = new GenericConstructor(clazz.getRawClass().getConstructors()[0], clazz);

        TestFactory testFactory = TestFactory.getInstance();
        VariableReference callee = testFactory.addConstructor(test, gc, 0, 0);
        VariableReference intVar = test.addStatement(new IntPrimitiveStatement(test, x));

        Method m = clazz.getRawClass().getMethod("testMe", new Class<?>[]{int.class});
        GenericMethod method = new GenericMethod(m, sut);
        MethodStatement ms = new MethodStatement(test, method, callee, Arrays.asList(new VariableReference[]{intVar}));
        test.addStatement(ms);

        return test;
    }

    private TestCase getTwoIntTest(int x, int y) throws NoSuchMethodException, SecurityException, ConstructionFailedException, ClassNotFoundException {
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
        GenericClass<?> clazz = GenericClassFactory.get(sut);

        DefaultTestCase test = new DefaultTestCase();
        GenericConstructor gc = new GenericConstructor(clazz.getRawClass().getConstructors()[0], clazz);

        TestFactory testFactory = TestFactory.getInstance();
        VariableReference callee = testFactory.addConstructor(test, gc, 0, 0);
        VariableReference intVar1 = test.addStatement(new IntPrimitiveStatement(test, x));
        VariableReference intVar2 = test.addStatement(new IntPrimitiveStatement(test, y));

        Method m = clazz.getRawClass().getMethod("testMe", new Class<?>[]{int.class, int.class});
        GenericMethod method = new GenericMethod(m, sut);
        MethodStatement ms = new MethodStatement(test, method, callee, Arrays.asList(new VariableReference[]{intVar1, intVar2}));
        test.addStatement(ms);

        return test;
    }

    @Test
    public void testSimpleInt() throws NoSuchMethodException, SecurityException, ClassNotFoundException, ConstructionFailedException {
        Properties.TARGET_CLASS = TrivialInt.class.getCanonicalName();
        TestChromosome test1 = new TestChromosome();
        test1.setTestCase(getIntTest(-1000000));
        TestChromosome test2 = test1.clone();

        TestSuiteChromosome suite = new TestSuiteChromosome();
        BranchCoverageSuiteFitness fitness = new BranchCoverageSuiteFitness();

        assertEquals(4.0, fitness.getFitness(suite), 0.0F);
        suite.addTest(test1);
        assertEquals(1.0, fitness.getFitness(suite), 0.0F);
        suite.addTest(test2);
        assertEquals(1.0, fitness.getFitness(suite), 0.01F); // 0.99...

        Properties.P_TEST_CHANGE = 1.0;
        Properties.P_TEST_DELETE = 0.0;
        Properties.P_TEST_INSERT = 0.0;
        Properties.PRIMITIVE_POOL = 0.0;
        double oldFitness = suite.getFitness();
        int notChanged = 0;
        for (int i = 0; i < 10000; i++) {
            TestChromosome testNew = test1.clone();
            testNew.mutate();
            if (testNew.isChanged()) {
                suite.deleteTest(test1);
                suite.addTest(testNew);
                double newFitness = fitness.getFitness(suite);
                if (newFitness < oldFitness) {
                    test1 = testNew;
                    oldFitness = newFitness;
                    System.out.println("" + i + ":" + ((IntPrimitiveStatement) test1.getTestCase().getStatement(1)).getValue());
                    if (newFitness == 0.0) {
                        System.out.println("Iterations: " + i);
                        System.out.println("Not changed: " + notChanged);
                        break;
                    }
                } else {
                    suite.deleteTest(testNew);
                    suite.addTest(test1);
                    fitness.getFitness(suite);
                }
            } else {
                notChanged++;
            }
        }

        System.out.println("Fitness: " + fitness.getFitness(suite));
        System.out.println("Test suite: " + suite);
        assertEquals(0.0, fitness.getFitness(suite), 0.1F);
    }

    @Test
    public void testTwoInts() throws NoSuchMethodException, SecurityException, ClassNotFoundException, ConstructionFailedException {
        Properties.TARGET_CLASS = IntExampleWithNoElse.class.getCanonicalName();
        TestChromosome test1 = new TestChromosome();
        test1.setTestCase(getTwoIntTest(1000, 100));
        TestChromosome test2 = new TestChromosome();
        test2.setTestCase(getTwoIntTest(-23423423, 234234234));
        TestChromosome test3 = new TestChromosome();
        test3.setTestCase(getTwoIntTest(0, 0));

        TestSuiteChromosome suite = new TestSuiteChromosome();
        BranchCoverageSuiteFitness fitness = new BranchCoverageSuiteFitness();

        assertEquals(6.0, fitness.getFitness(suite), 0.0F);
        suite.addTest(test1);
        assertEquals(2.0, fitness.getFitness(suite), 0.0F);
        suite.addTest(test2);
        assertEquals(1.99, fitness.getFitness(suite), 0.01F); // 0.99...
        suite.addTest(test3);
        assertEquals(0.99, fitness.getFitness(suite), 0.01F); // 0.99...

        Properties.P_TEST_CHANGE = 1.0;
        Properties.P_TEST_DELETE = 0.0;
        Properties.P_TEST_INSERT = 0.0;
        Properties.PRIMITIVE_POOL = 0.0;
        double oldFitness = suite.getFitness();
        int notChanged = 0;
        for (int i = 0; i < 10000; i++) {
            TestChromosome testNew = test1.clone();
            testNew.mutate();
            if (testNew.isChanged()) {
                suite.deleteTest(test1);
                suite.addTest(testNew);
                double newFitness = fitness.getFitness(suite);
                if (newFitness < oldFitness) {
                    test1 = testNew;
                    oldFitness = newFitness;
                    System.out.println("" + i + ":" + ((IntPrimitiveStatement) test1.getTestCase().getStatement(1)).getValue());
                    System.out.println("    " + ((IntPrimitiveStatement) test1.getTestCase().getStatement(2)).getValue());
                    if (newFitness == 0.0) {
                        System.out.println("Iterations: " + i);
                        System.out.println("Not changed: " + notChanged);
                        break;
                    }
                } else {
                    suite.deleteTest(testNew);
                    suite.addTest(test1);
                    fitness.getFitness(suite);
                }
            } else {
                notChanged++;
            }
        }

        System.out.println("Fitness: " + fitness.getFitness(suite));
        System.out.println("Test suite: " + suite);
        assertEquals(0.0, fitness.getFitness(suite), 0.1F);
    }

    @Test
    public void testTwoIntsLineCoverage() throws NoSuchMethodException, SecurityException, ClassNotFoundException, ConstructionFailedException {
        Properties.TARGET_CLASS = IntExampleWithNoElse.class.getCanonicalName();
        Properties.TEST_ARCHIVE = false;
        TestChromosome test1 = new TestChromosome();
        test1.setTestCase(getTwoIntTest(1000, 100));
        TestChromosome test2 = new TestChromosome();
        test2.setTestCase(getTwoIntTest(0, 0));

        TestSuiteChromosome suite = new TestSuiteChromosome();
        LineCoverageSuiteFitness fitness = new LineCoverageSuiteFitness();
        suite.addTest(test1);
        suite.addTest(test2);

        Properties.P_TEST_CHANGE = 1.0;
        Properties.P_TEST_DELETE = 0.0;
        Properties.P_TEST_INSERT = 0.0;
        Properties.PRIMITIVE_POOL = 0.0;
        double oldFitness = fitness.getFitness(suite);
        int notChanged = 0;
        for (int i = 0; i < 10000; i++) {
            TestChromosome testNew = test1.clone();
            testNew.mutate();
            if (testNew.isChanged()) {
                suite.deleteTest(test1);
                suite.addTest(testNew);
                double newFitness = fitness.getFitness(suite);
                if (newFitness < oldFitness) {
                    test1 = testNew;
                    oldFitness = newFitness;
                    System.out.println("" + i + ":" + ((IntPrimitiveStatement) test1.getTestCase().getStatement(1)).getValue());
                    System.out.println("    " + ((IntPrimitiveStatement) test1.getTestCase().getStatement(2)).getValue());
                    if (newFitness == 0.0) {
                        System.out.println("Iterations: " + i);
                        System.out.println("Not changed: " + notChanged);
                        break;
                    }
                } else {
                    suite.deleteTest(testNew);
                    suite.addTest(test1);
                    fitness.getFitness(suite);
                }
            } else {
                notChanged++;
            }
        }

        System.out.println("Fitness: " + fitness.getFitness(suite));
        System.out.println("Test suite: " + suite);
        assertEquals(0.0, fitness.getFitness(suite), 0.1F);
    }

    @Test
    public void testTwoIntsWeakMutation() throws NoSuchMethodException, SecurityException, ClassNotFoundException, ConstructionFailedException {
        Properties.TARGET_CLASS = IntExampleWithNoElse.class.getCanonicalName();
        TestChromosome test1 = new TestChromosome();
        test1.setTestCase(getTwoIntTest(1, 1000));
        TestChromosome test2 = new TestChromosome();
        test2.setTestCase(getTwoIntTest(0, 0));

        TestSuiteChromosome suite = new TestSuiteChromosome();
        WeakMutationSuiteFitness fitness = new WeakMutationSuiteFitness();
        suite.addTest(test1);
        suite.addTest(test2);

        Properties.P_TEST_CHANGE = 1.0;
        Properties.P_TEST_DELETE = 0.0;
        Properties.P_TEST_INSERT = 0.0;
        Properties.PRIMITIVE_POOL = 0.0;
        double oldFitness = fitness.getFitness(suite);
        int notChanged = 0;
        for (int i = 0; i < 10000; i++) {
            TestChromosome testNew = test1.clone();
            testNew.mutate();
            if (testNew.isChanged()) {
                suite.deleteTest(test1);
                suite.addTest(testNew);
                double newFitness = fitness.getFitness(suite);
                if (newFitness < oldFitness) {
                    test1 = testNew;
                    oldFitness = newFitness;
                    System.out.println("" + i + ":" + ((IntPrimitiveStatement) test1.getTestCase().getStatement(1)).getValue());
                    System.out.println("    " + ((IntPrimitiveStatement) test1.getTestCase().getStatement(2)).getValue());
                    if (newFitness == 0.0) {
                        System.out.println("Iterations: " + i);
                        System.out.println("Not changed: " + notChanged);
                        break;
                    }
                } else {
                    suite.deleteTest(testNew);
                    suite.addTest(test1);
                    fitness.getFitness(suite);
                }
            } else {
                notChanged++;
            }
        }

        System.out.println("Fitness: " + fitness.getFitness(suite));
        System.out.println("Test suite: " + suite);
        assertEquals(0.0, fitness.getFitness(suite), 0.1F);
    }
}
