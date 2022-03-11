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

package org.evosuite.basic;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.generic.AbstractGenericClass;
import com.examples.with.different.packagename.generic.AbstractGuavaExample;
import com.examples.with.different.packagename.generic.DelayedQueueExample;
import com.examples.with.different.packagename.generic.GenericArray;
import com.examples.with.different.packagename.generic.GenericArrayWithGenericType;
import com.examples.with.different.packagename.generic.GenericArrayWithGenericTypeVariable;
import com.examples.with.different.packagename.generic.GenericClassWithGenericMethod;
import com.examples.with.different.packagename.generic.GenericClassWithGenericMethodAndSubclass;
import com.examples.with.different.packagename.generic.GenericCollectionUtil;
import com.examples.with.different.packagename.generic.GenericConstructorParameterOnRawList;
import com.examples.with.different.packagename.generic.GenericGenericParameter;
import com.examples.with.different.packagename.generic.GenericMemberclass;
import com.examples.with.different.packagename.generic.GenericMethod;
import com.examples.with.different.packagename.generic.GenericMethodAlternativeBounds;
import com.examples.with.different.packagename.generic.GenericMethodReturningTypeVariable;
import com.examples.with.different.packagename.generic.GenericMethodWithBounds;
import com.examples.with.different.packagename.generic.GenericOnlyInMemberclass;
import com.examples.with.different.packagename.generic.GenericParameterExtendingGenericBounds;
import com.examples.with.different.packagename.generic.GenericParameterWithBound;
import com.examples.with.different.packagename.generic.GenericParameterWithGenericBound;
import com.examples.with.different.packagename.generic.GenericParameters1;
import com.examples.with.different.packagename.generic.GenericParameters2;
import com.examples.with.different.packagename.generic.GenericParameters3;
import com.examples.with.different.packagename.generic.GenericParameters4;
import com.examples.with.different.packagename.generic.GenericParameters5;
import com.examples.with.different.packagename.generic.GenericParameters6;
import com.examples.with.different.packagename.generic.GenericParameters7;
import com.examples.with.different.packagename.generic.GenericParameters8;
import com.examples.with.different.packagename.generic.GenericSUT;
import com.examples.with.different.packagename.generic.GenericSUTString;
import com.examples.with.different.packagename.generic.GenericSUTTwoParameters;
import com.examples.with.different.packagename.generic.GenericStaticMemberclass;
import com.examples.with.different.packagename.generic.GenericStaticMethod1;
import com.examples.with.different.packagename.generic.GenericStaticMethod2;
import com.examples.with.different.packagename.generic.GenericStaticMethod3;
import com.examples.with.different.packagename.generic.GenericStaticMethod4;
import com.examples.with.different.packagename.generic.GenericSuperclassOmittingTypeParameters;
import com.examples.with.different.packagename.generic.GenericTripleParameter;
import com.examples.with.different.packagename.generic.GenericTwoDimensionalArray;
import com.examples.with.different.packagename.generic.GenericVarArgMethod;
import com.examples.with.different.packagename.generic.GenericWildcardParameter;
import com.examples.with.different.packagename.generic.GenericWithPartialParameters;
import com.examples.with.different.packagename.generic.GenericWithWildcardParameter;
import com.examples.with.different.packagename.generic.GuavaExample;
import com.examples.with.different.packagename.generic.GuavaExample2;
import com.examples.with.different.packagename.generic.GuavaExample3;
import com.examples.with.different.packagename.generic.GuavaExample5;
import com.examples.with.different.packagename.generic.PartiallyGenericReturnType;
import com.examples.with.different.packagename.generic.ReallyCaselessMap;

/**
 * @author Gordon Fraser
 */
public class GenericsSystemTest extends SystemTestBase {
    @Test
    public void testGenericList() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericParameters1.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 80000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        Assert.assertNotNull(result);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 7, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericStringListLength() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericParameters2.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        Assert.assertNotNull(result);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericStringMap() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericParameters3.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        Assert.assertNotNull(result);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 5, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericListsDifferentTypes() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericParameters4.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 80000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        Assert.assertNotNull(result);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 5, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericWildcardList() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericParameters5.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 80000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        Assert.assertNotNull(result);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 5, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericWildcardStringList() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericParameters6.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 80000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 5, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericSUT() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericSUT.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericSUTTwoParameters() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericSUTTwoParameters.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericSUTString() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericSUTString.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericRawTypes() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericParameters7.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 4, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericRawParameterTypes() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericParameters8.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericMemberclass() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericMemberclass.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericStaticMemberclass() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericStaticMemberclass.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericOnlyInMemberclass() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericOnlyInMemberclass.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericArray() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericArray.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericTwoDimensionalArray() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericTwoDimensionalArray.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericArrayWithGenericType() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericArrayWithGenericType.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        String testSuite = best.toString();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertFalse(testSuite.contains("? listArray"));
        // Assert.assertFalse(testSuite.contains("List<?>"));
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericArrayWithGenericTypeVariable() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericArrayWithGenericTypeVariable.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        String testSuite = best.toString();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertFalse(testSuite.contains("? listArray"));
        Assert.assertFalse(testSuite.contains("List<?>"));
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericGenericParameter() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericGenericParameter.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 80000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericParameterWithBounds() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericParameterWithBound.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericParameterWithGenericBound() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericParameterWithGenericBound.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 20000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericWildcardParameter() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericWildcardParameter.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);
        // String testSuite = best.toString();
        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        // Is this valid or not:
        // Assert.assertFalse(testSuite.contains("List<?>"));
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericSuperclassOmittingTypeParameter() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericSuperclassOmittingTypeParameters.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericMethod() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericMethod.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericMethodWithBounds() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericMethodWithBounds.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericConstructorParameterOnRawList() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericConstructorParameterOnRawList.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericTypeWithGenericParameter() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericParameterExtendingGenericBounds.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testDifferingNumberOfTypeParameters() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ReallyCaselessMap.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericMethodWithEnumBounds() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericMethodAlternativeBounds.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        // Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericClassWithGenericMethod() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericClassWithGenericMethod.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        // Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericClassWithGenericMethodAndSubclass() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericClassWithGenericMethodAndSubclass.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        // Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericClassWithThreeParameters() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericTripleParameter.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        // Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericClassWithWildcardParameter() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericWithWildcardParameter.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        // Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericGuavaExample() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GuavaExample.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        // Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericGuavaExample2() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GuavaExample2.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        // Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericGuavaExample3() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GuavaExample3.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        // Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testPartialGenericExample() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericWithPartialParameters.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        // Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericMethodReturningTypeVariable() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericMethodReturningTypeVariable.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        // Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericAbstractMethod() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = AbstractGenericClass.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        // Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericGuavaExample5() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GuavaExample5.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        // Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericGuavaExample5Abstract() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = AbstractGuavaExample.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 250000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericVarArgs() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericVarArgMethod.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testGenericQueue() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DelayedQueueExample.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testPartiallyGenericReturnType() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = PartiallyGenericReturnType.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testStaticGenericMethod1() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericStaticMethod1.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testStaticGenericMethod2() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericStaticMethod2.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testStaticGenericMethod3() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericStaticMethod3.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testStaticGenericMethod4() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericStaticMethod4.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testStaticGenericUtils1() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = GenericCollectionUtil.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        // int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
        // Assert.assertEquals("Wrong number of goals: ", 3, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

}
