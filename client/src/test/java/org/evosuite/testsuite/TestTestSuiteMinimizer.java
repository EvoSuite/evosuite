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
package org.evosuite.testsuite;

import com.examples.with.different.packagename.FlagExample1;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.reset.ClassReInitializer;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassFactory;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unused")
public class TestTestSuiteMinimizer {
    private static java.util.Properties currentProperties;

    @Before
    public void setUp() {
        ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
        Properties.getInstance().resetToDefaults();
        Randomness.setSeed(42);
        TestGenerationContext.getInstance().resetContext();
        ClassReInitializer.resetSingleton();
        Randomness.setSeed(42);
        currentProperties = (java.util.Properties) System.getProperties().clone();
    }

    @After
    public void tearDown() {
        TestGenerationContext.getInstance().resetContext();
        System.setProperties(currentProperties);
        Properties.getInstance().resetToDefaults();
    }

    @Test
    public void minimizeEmptySuite() throws ClassNotFoundException {

        DefaultTestCase test = new DefaultTestCase();

        TestSuiteChromosome tsc = new TestSuiteChromosome();
        tsc.addTest(test);
        TestSuiteFitnessFunction ff = new BranchCoverageSuiteFitness();
        double previous_fitness = ff.getFitness(tsc);
        tsc.setFitness(ff, previous_fitness);
        assertEquals(0.0, previous_fitness, 0.0);

        TestSuiteMinimizer minimizer = new TestSuiteMinimizer(new BranchCoverageFactory());
        minimizer.minimize(tsc, false);
        assertEquals(0, tsc.getTests().size());

        double fitness = ff.getFitness(tsc);
        assertEquals(previous_fitness, fitness, 0.0);
    }

    @Test
    public void minimizeSuiteOnlyWithVariables() {
        DefaultTestCase test = new DefaultTestCase();
        for (int i = 0; i < 10; i++) {
            IntPrimitiveStatement ips = new IntPrimitiveStatement(test, i);
            test.addStatement(ips);
        }

        assertEquals(10, test.size());

        TestSuiteChromosome tsc = new TestSuiteChromosome();
        tsc.addTest(test);
        TestSuiteFitnessFunction ff = new BranchCoverageSuiteFitness();
        double previous_fitness = ff.getFitness(tsc);
        tsc.setFitness(ff, previous_fitness);
        assertEquals(0.0, previous_fitness, 0.0);

        TestSuiteMinimizer minimizer = new TestSuiteMinimizer(new BranchCoverageFactory());
        minimizer.minimize(tsc, false);
        assertEquals(0, tsc.getTests().size());

        double fitness = ff.getFitness(tsc);
        assertEquals(previous_fitness, fitness, 0.0);
    }

    @Test
    public void minimizeSuiteHalfCoverage() throws ClassNotFoundException, NoSuchFieldException, SecurityException, ConstructionFailedException, NoSuchMethodException {
        Properties.TARGET_CLASS = FlagExample1.class.getCanonicalName();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
        GenericClass<?> clazz = GenericClassFactory.get(sut);

        DefaultTestCase test = new DefaultTestCase();
        GenericConstructor gc = new GenericConstructor(clazz.getRawClass().getConstructors()[0], clazz);

        TestFactory testFactory = TestFactory.getInstance();
        testFactory.addConstructor(test, gc, 0, 0);

        List<VariableReference> parameters = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            IntPrimitiveStatement ips = new IntPrimitiveStatement(test, 28234 + i);
            VariableReference vr = test.addStatement(ips, i + 1);
        }

        ConstructorStatement ct = new ConstructorStatement(test, gc, parameters);

        Method m = clazz.getRawClass().getMethod("testMe", new Class<?>[]{int.class});
        GenericMethod method = new GenericMethod(m, sut);
        testFactory.addMethod(test, method, 11, 0);

        assertEquals(12, test.size());

        TestSuiteChromosome tsc = new TestSuiteChromosome();
        tsc.addTest(test);
        TestSuiteFitnessFunction ff = new BranchCoverageSuiteFitness();
        double previous_fitness = ff.getFitness(tsc);
        tsc.setFitness(ff, previous_fitness);
        assertEquals(2.0, previous_fitness, 0.0);

        TestSuiteMinimizer minimizer = new TestSuiteMinimizer(new BranchCoverageFactory());
        minimizer.minimize(tsc, false);
        assertEquals(1, tsc.getTests().size());
        assertEquals(3, tsc.getTests().get(0).size());
        //assertTrue(tsc.getTests().get(0).toCode().equals("FlagExample1 flagExample1_0 = new FlagExample1();\nint int0 = 28234;\nflagExample1_0.testMe(int0);\n"));

        double fitness = ff.getFitness(tsc);
        assertEquals(previous_fitness, fitness, 0.0);
    }

    @Test
    public void minimizeSuiteHalfCoverageWithTwoFitnessFunctions() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
        Properties.TARGET_CLASS = FlagExample1.class.getCanonicalName();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
        GenericClass<?> clazz = GenericClassFactory.get(sut);

        DefaultTestCase test = new DefaultTestCase();
        GenericConstructor gc = new GenericConstructor(clazz.getRawClass().getConstructors()[0], clazz);

        TestFactory testFactory = TestFactory.getInstance();
        testFactory.addConstructor(test, gc, 0, 0);

        List<VariableReference> parameters = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            IntPrimitiveStatement ips = new IntPrimitiveStatement(test, 28234 + i);
            VariableReference vr = test.addStatement(ips, i + 1);
        }

        ConstructorStatement ct = new ConstructorStatement(test, gc, parameters);

        Method m = clazz.getRawClass().getMethod("testMe", new Class<?>[]{int.class});
        GenericMethod method = new GenericMethod(m, sut);
        testFactory.addMethod(test, method, 11, 0);

        assertEquals(12, test.size());

        TestSuiteChromosome tsc = new TestSuiteChromosome();
        tsc.addTest(test);

        TestSuiteFitnessFunction branch = new BranchCoverageSuiteFitness();
        double previous_branch_fitness = branch.getFitness(tsc);
        tsc.setFitness(branch, previous_branch_fitness);
        assertEquals(2.0, previous_branch_fitness, 0.0);


        List<TestFitnessFactory<? extends TestFitnessFunction>> factories = new ArrayList<>();
        factories.add(new BranchCoverageFactory());

        TestSuiteMinimizer minimizer = new TestSuiteMinimizer(factories);
        minimizer.minimize(tsc, false);
        assertEquals(1, tsc.getTests().size());
        assertEquals(3, tsc.getTests().get(0).size());
        //assertTrue(tsc.getTests().get(0).toCode().equals("FlagExample1 flagExample1_0 = new FlagExample1();\nint int0 = 28234;\nflagExample1_0.testMe(int0);\n"));

        double branch_fitness = branch.getFitness(tsc);
        assertEquals(previous_branch_fitness, branch_fitness, 0.0);

    }

    @Test
    public void minimizeSuiteFullCoverage() throws ClassNotFoundException, NoSuchFieldException, SecurityException, ConstructionFailedException, NoSuchMethodException {
        Properties.TARGET_CLASS = FlagExample1.class.getCanonicalName();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
        GenericClass<?> clazz = GenericClassFactory.get(sut);

        DefaultTestCase test = new DefaultTestCase();
        GenericConstructor gc = new GenericConstructor(clazz.getRawClass().getConstructors()[0], clazz);

        TestFactory testFactory = TestFactory.getInstance();
        testFactory.addConstructor(test, gc, 0, 0);

        List<VariableReference> parameters = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            IntPrimitiveStatement ips = new IntPrimitiveStatement(test, 28234 + i);
            VariableReference vr = test.addStatement(ips, i + 1);
        }

        ConstructorStatement ct = new ConstructorStatement(test, gc, parameters);

        Method m = clazz.getRawClass().getMethod("testMe", new Class<?>[]{int.class});
        GenericMethod method = new GenericMethod(m, sut);
        testFactory.addMethod(test, method, 11, 0);

        parameters = new ArrayList<>();
        for (int i = 12; i < 15; i++) {
            IntPrimitiveStatement ips = new IntPrimitiveStatement(test, i);
            VariableReference vr = test.addStatement(ips, i);
        }
        ct = new ConstructorStatement(test, gc, parameters);
        testFactory.addMethod(test, method, 15, 0);

        assertEquals(16, test.size());

        TestSuiteChromosome tsc = new TestSuiteChromosome();
        tsc.addTest(test);
        TestSuiteFitnessFunction ff = new BranchCoverageSuiteFitness();
        double previous_fitness = ff.getFitness(tsc);
        tsc.setFitness(ff, previous_fitness);
        assertEquals(0.0, previous_fitness, 0.0);

        TestSuiteMinimizer minimizer = new TestSuiteMinimizer(new BranchCoverageFactory());
        minimizer.minimize(tsc, false);
        assertEquals(1, tsc.getTests().size());
        assertEquals(5, tsc.getTests().get(0).size());
        //assertTrue(tsc.getTests().get(0).toCode().equals("FlagExample1 flagExample1_0 = new FlagExample1();\nint int0 = 28234;\nint int1 = 28241;\nflagExample1_0.testMe(int1);\nflagExample1_0.testMe(int0);\n"));

        double fitness = ff.getFitness(tsc);
        assertEquals(previous_fitness, fitness, 0.0);
    }

    @Test
    public void minimizeSuiteFullCoverageWithTwoFitnessFunctions() throws ClassNotFoundException, NoSuchFieldException, SecurityException, ConstructionFailedException, NoSuchMethodException {
        Properties.TARGET_CLASS = FlagExample1.class.getCanonicalName();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
        GenericClass<?> clazz = GenericClassFactory.get(sut);

        DefaultTestCase test = new DefaultTestCase();
        GenericConstructor gc = new GenericConstructor(clazz.getRawClass().getConstructors()[0], clazz);

        TestFactory testFactory = TestFactory.getInstance();
        testFactory.addConstructor(test, gc, 0, 0);

        List<VariableReference> parameters = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            IntPrimitiveStatement ips = new IntPrimitiveStatement(test, 28234 + i);
            VariableReference vr = test.addStatement(ips, i + 1);
        }

        ConstructorStatement ct = new ConstructorStatement(test, gc, parameters);

        Method m = clazz.getRawClass().getMethod("testMe", new Class<?>[]{int.class});
        GenericMethod method = new GenericMethod(m, sut);
        testFactory.addMethod(test, method, 11, 0);

        parameters = new ArrayList<>();
        for (int i = 12; i < 15; i++) {
            IntPrimitiveStatement ips = new IntPrimitiveStatement(test, i);
            VariableReference vr = test.addStatement(ips, i);
        }
        ct = new ConstructorStatement(test, gc, parameters);
        testFactory.addMethod(test, method, 15, 0);

        assertEquals(16, test.size());

        TestSuiteChromosome tsc = new TestSuiteChromosome();
        tsc.addTest(test);

        TestSuiteFitnessFunction branch = new BranchCoverageSuiteFitness();
        double previous_branch_fitness = branch.getFitness(tsc);
        tsc.setFitness(branch, previous_branch_fitness);
        assertEquals(0.0, previous_branch_fitness, 0.0);

        List<TestFitnessFactory<? extends TestFitnessFunction>> factories = new ArrayList<>();
        factories.add(new BranchCoverageFactory());

        TestSuiteMinimizer minimizer = new TestSuiteMinimizer(factories);
        minimizer.minimize(tsc, false);
        assertEquals(1, tsc.getTests().size());
        assertEquals(5, tsc.getTests().get(0).size());
        //assertTrue(tsc.getTests().get(0).toCode().equals("FlagExample1 flagExample1_0 = new FlagExample1();\nint int0 = 28234;\nint int1 = 28241;\nflagExample1_0.testMe(int1);\nflagExample1_0.testMe(int0);\n"));

        double branch_fitness = branch.getFitness(tsc);
        assertEquals(previous_branch_fitness, branch_fitness, 0.0);

    }

    @Test
    public void minimizeSuiteFullCoverageWithTwoFitnessFunctionsMinimizeTestsEnabled() throws ClassNotFoundException, NoSuchFieldException, SecurityException, ConstructionFailedException, NoSuchMethodException {
        Properties.TARGET_CLASS = FlagExample1.class.getCanonicalName();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
        GenericClass<?> clazz = GenericClassFactory.get(sut);

        DefaultTestCase test = new DefaultTestCase();
        GenericConstructor gc = new GenericConstructor(clazz.getRawClass().getConstructors()[0], clazz);

        TestFactory testFactory = TestFactory.getInstance();
        testFactory.addConstructor(test, gc, 0, 0);

        List<VariableReference> parameters = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            IntPrimitiveStatement ips = new IntPrimitiveStatement(test, 28234 + i);
            VariableReference vr = test.addStatement(ips, i + 1);
        }

        ConstructorStatement ct = new ConstructorStatement(test, gc, parameters);

        Method m = clazz.getRawClass().getMethod("testMe", new Class<?>[]{int.class});
        GenericMethod method = new GenericMethod(m, sut);
        testFactory.addMethod(test, method, 11, 0);

        parameters = new ArrayList<>();
        for (int i = 12; i < 15; i++) {
            IntPrimitiveStatement ips = new IntPrimitiveStatement(test, i);
            VariableReference vr = test.addStatement(ips, i);
        }
        ct = new ConstructorStatement(test, gc, parameters);
        testFactory.addMethod(test, method, 15, 0);

        assertEquals(16, test.size());

        TestSuiteChromosome tsc = new TestSuiteChromosome();
        tsc.addTest(test);

        TestSuiteFitnessFunction branch = new BranchCoverageSuiteFitness();
        double previous_branch_fitness = branch.getFitness(tsc);
        tsc.setFitness(branch, previous_branch_fitness);
        assertEquals(0.0, previous_branch_fitness, 0.0);

        List<TestFitnessFactory<? extends TestFitnessFunction>> factories = new ArrayList<>();
        factories.add(new BranchCoverageFactory());

        TestSuiteMinimizer minimizer = new TestSuiteMinimizer(factories);
        minimizer.minimize(tsc, true);
        assertEquals(2, tsc.getTests().size());
        assertEquals(3, tsc.getTests().get(0).size());
        assertEquals(3, tsc.getTests().get(1).size());
//        assertTrue(tsc.getTests().get(0).toCode().equals("FlagExample1 flagExample1_0 = new FlagExample1();\nint int0 = 28234;\nflagExample1_0.testMe(int0);\n"));
//        assertTrue(tsc.getTests().get(1).toCode().equals("FlagExample1 flagExample1_0 = new FlagExample1();\nint int0 = 28241;\nflagExample1_0.testMe(int0);\n"));

        double branch_fitness = branch.getFitness(tsc);
        assertEquals(previous_branch_fitness, branch_fitness, 0.0);
    }
}
