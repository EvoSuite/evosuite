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
package org.evosuite.coverage.input;

import com.examples.with.different.packagename.coverage.ClassWithField;
import com.examples.with.different.packagename.coverage.MethodWithPrimitiveInputArguments;
import com.examples.with.different.packagename.coverage.MethodWithSeveralInputArguments;
import com.examples.with.different.packagename.coverage.MethodWithWrapperInputArguments;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTestBase;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.numeric.BooleanPrimitiveStatement;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericField;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;


/**
 * @author Jose Miguel Rojas
 */
public class InputCoverageFitnessFunctionSystemTest extends SystemTestBase {

    private static final Criterion[] defaultCriterion = Properties.CRITERION;

    private static final boolean defaultArchive = Properties.TEST_ARCHIVE;

    @After
    public void resetProperties() {
        Properties.CRITERION = defaultCriterion;
        Properties.TEST_ARCHIVE = defaultArchive;
    }

    @Before
    public void beforeTest() {
        Properties.CRITERION = new Properties.Criterion[]{Criterion.INPUT};
    }

    @Test
    public void testInputCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = MethodWithSeveralInputArguments.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.MAX_ARRAY = 2;
        Properties.NULL_PROBABILITY = 0.2;
        Properties.SEARCH_BUDGET = 20000;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        List<?> goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals();
        Assert.assertEquals(12, goals.size());
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testInputCoverageWithPrimitiveTypes() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = MethodWithPrimitiveInputArguments.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        List<?> goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals();
        Assert.assertEquals(23, goals.size());
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }


    @Test
    public void testInputCoverageWithWrapperTypes() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = MethodWithWrapperInputArguments.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        List<?> goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals();
        Assert.assertEquals(31, goals.size());
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testInputCoverageClassWithField() throws NoSuchFieldException, NoSuchMethodException {
        Class<?> sut = ClassWithField.class;

        DefaultTestCase tc = new DefaultTestCase();
        // ClassWithField classWithField0 = new ClassWithField();
        GenericConstructor constructor = new GenericConstructor(sut.getConstructors()[0], sut);
        ConstructorStatement constructorStatement = new ConstructorStatement(tc, constructor, Arrays.asList(new VariableReference[]{}));
        VariableReference obj = tc.addStatement(constructorStatement);

        // classWithField0.testFoo(classWithField0.BOOLEAN_FIELD);
        FieldReference field = new FieldReference(tc, new GenericField(sut.getDeclaredField("BOOLEAN_FIELD"), sut), obj);
        Method m = sut.getMethod("testFoo", Boolean.TYPE);
        GenericMethod gm = new GenericMethod(m, sut);
        tc.addStatement(new MethodStatement(tc, gm, obj, Arrays.asList(new VariableReference[]{field})));

        // classWithField0.BOOLEAN_FIELD = false;
        VariableReference boolRef = tc.addStatement(new BooleanPrimitiveStatement(tc, false));
        tc.addStatement(new AssignmentStatement(tc, field, boolRef));
        tc.addStatement(new MethodStatement(tc, gm, obj, Arrays.asList(new VariableReference[]{field})));

        Properties.TARGET_CLASS = sut.getCanonicalName();
        Properties.JUNIT_TESTS = true;

        TestSuiteChromosome testSuite = new TestSuiteChromosome();
        testSuite.addTest(tc);

        FitnessFunction ffunction = FitnessFunctions.getFitnessFunction(Properties.Criterion.INPUT);
        assertEquals("Should be 0.0", 0.0, ffunction.getFitness(testSuite), 0.0);
        assertEquals("Should be 1.0", 1.0, testSuite.getCoverage(ffunction), 0.0);

    }

}
