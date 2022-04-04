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
package org.evosuite.coverage.ibranch;


import java.util.Arrays;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.SecondaryObjective;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.context.complex.EntryPointsClass;

/**
 * Ibranch Goals of provacomplex.EntryPointsClass.
 * size: 27
 * infeasible: 2
 * expected coverage using branch as main criterion and ibranch as secondary criterion, 100%
 * expected coverage using ibranch, 25/27, 92%.
 * -----------------
 * infeasible goals; 2 goals
 * provacomplex.ISubClass.checkFiftneen(I)Z: root-Branch in context: provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.ISubClass:checkFiftneen(I)Z
 * provacomplex.SubClass.bla(I)Z: I28 Branch 4 IFNE L24 - true in context: provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.SubClass:checkFiftneen(I)Z provacomplex.SubClass:bla(I)Z
 * -----------------
 * ibranch goals; 27 goals
 * provacomplex.EntryPointsClass:<init>()V provacomplex.SubClass:<init>()V provacomplex.ISubClass:<init>()V:provacomplex.ISubClass.<init>()V: root-Branch
 * provacomplex.EntryPointsClass:<init>()V provacomplex.SubClass:<init>()V provacomplex.SubSubClass:<init>()V:provacomplex.SubSubClass.<init>()V: root-Branch
 * provacomplex.EntryPointsClass:<init>()V provacomplex.SubClass:<init>()V:provacomplex.SubClass.<init>()V: root-Branch
 * provacomplex.EntryPointsClass:<init>()V:provacomplex.EntryPointsClass.<init>()V: root-Branch
 * provacomplex.EntryPointsClass:doObj(Lprovacomplex/AParameterObject;)V provacomplex.ParameterObject:isEnabled()Z:provacomplex.ParameterObject.isEnabled()Z: I5 Branch 8 IF_ICMPLE L17 - false
 * provacomplex.EntryPointsClass:doObj(Lprovacomplex/AParameterObject;)V provacomplex.ParameterObject:isEnabled()Z:provacomplex.ParameterObject.isEnabled()Z: I5 Branch 8 IF_ICMPLE L17 - true
 * provacomplex.EntryPointsClass:doObj(Lprovacomplex/AParameterObject;)V provacomplex.ParameterObject:isEnabled()Z:provacomplex.ParameterObject.isEnabled()Z: I9 Branch 9 IF_ICMPGE L17 - false
 * provacomplex.EntryPointsClass:doObj(Lprovacomplex/AParameterObject;)V provacomplex.ParameterObject:isEnabled()Z:provacomplex.ParameterObject.isEnabled()Z: I9 Branch 9 IF_ICMPGE L17 - true
 * provacomplex.EntryPointsClass:doObj(Lprovacomplex/AParameterObject;)V:provacomplex.EntryPointsClass.doObj(Lprovacomplex/AParameterObject;)V: I4 Branch 7 IFEQ L18 - false
 * provacomplex.EntryPointsClass:doObj(Lprovacomplex/AParameterObject;)V:provacomplex.EntryPointsClass.doObj(Lprovacomplex/AParameterObject;)V: I4 Branch 7 IFEQ L18 - true
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.ISubClass:checkFiftneen(I)Z:provacomplex.ISubClass.checkFiftneen(I)Z: root-Branch
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.SubClass2:checkFiftneen(I)Z provacomplex.SubClass2:bla(I)Z:provacomplex.SubClass2.bla(I)Z: I10 Branch 11 IF_ICMPNE L17 - false
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.SubClass2:checkFiftneen(I)Z provacomplex.SubClass2:bla(I)Z:provacomplex.SubClass2.bla(I)Z: I10 Branch 11 IF_ICMPNE L17 - true
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.SubClass2:checkFiftneen(I)Z:provacomplex.SubClass2.checkFiftneen(I)Z: I9 Branch 10 IFEQ L8 - false
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.SubClass2:checkFiftneen(I)Z:provacomplex.SubClass2.checkFiftneen(I)Z: I9 Branch 10 IFEQ L8 - true
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.SubClass:checkFiftneen(I)Z provacomplex.SubClass:bla(I)Z provacomplex.SubSubClass:innermethod(I)Z:provacomplex.SubSubClass.innermethod(I)Z: I3 Branch 5 IFLE L6 - false
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.SubClass:checkFiftneen(I)Z provacomplex.SubClass:bla(I)Z provacomplex.SubSubClass:innermethod(I)Z:provacomplex.SubSubClass.innermethod(I)Z: I3 Branch 5 IFLE L6 - true
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.SubClass:checkFiftneen(I)Z provacomplex.SubClass:bla(I)Z:provacomplex.SubClass.bla(I)Z: I10 Branch 2 IF_ICMPNE L20 - false
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.SubClass:checkFiftneen(I)Z provacomplex.SubClass:bla(I)Z:provacomplex.SubClass.bla(I)Z: I10 Branch 2 IF_ICMPNE L20 - true
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.SubClass:checkFiftneen(I)Z provacomplex.SubClass:bla(I)Z:provacomplex.SubClass.bla(I)Z: I26 Branch 3 IFNE L24 - false
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.SubClass:checkFiftneen(I)Z provacomplex.SubClass:bla(I)Z:provacomplex.SubClass.bla(I)Z: I26 Branch 3 IFNE L24 - true
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.SubClass:checkFiftneen(I)Z provacomplex.SubClass:bla(I)Z:provacomplex.SubClass.bla(I)Z: I28 Branch 4 IFNE L24 - false
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.SubClass:checkFiftneen(I)Z provacomplex.SubClass:bla(I)Z:provacomplex.SubClass.bla(I)Z: I28 Branch 4 IFNE L24 - true
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.SubClass:checkFiftneen(I)Z:provacomplex.SubClass.checkFiftneen(I)Z: I9 Branch 1 IFEQ L13 - false
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V provacomplex.SubClass:checkFiftneen(I)Z:provacomplex.SubClass.checkFiftneen(I)Z: I9 Branch 1 IFEQ L13 - true
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V:provacomplex.EntryPointsClass.dosmt(ILjava/lang/String;D)V: I10 Branch 6 IFEQ L12 - false
 * provacomplex.EntryPointsClass:dosmt(ILjava/lang/String;D)V:provacomplex.EntryPointsClass.dosmt(ILjava/lang/String;D)V: I10 Branch 6 IFEQ L12 - true
 *
 * @author mattia
 */
public class IBranchSystemTest extends SystemTestBase {

    private Properties.Criterion[] oldCriteria = Arrays.copyOf(Properties.CRITERION, Properties.CRITERION.length);
    private Properties.StoppingCondition oldStoppingCondition = Properties.STOPPING_CONDITION;
    private double oldPrimitivePool = Properties.PRIMITIVE_POOL;
    private long budget = Properties.SEARCH_BUDGET;
    private SecondaryObjective[] secondary = Properties.SECONDARY_OBJECTIVE;
    private final boolean oldArchive = Properties.TEST_ARCHIVE;

    @Before
    public void beforeTest() {
        oldCriteria = Arrays.copyOf(Properties.CRITERION, Properties.CRITERION.length);
        oldStoppingCondition = Properties.STOPPING_CONDITION;
        oldPrimitivePool = Properties.PRIMITIVE_POOL;
        TestSuiteChromosome.removeAllSecondaryObjectives();
        secondary = Properties.SECONDARY_OBJECTIVE;
        budget = Properties.SEARCH_BUDGET;
        Properties.INSTRUMENT_CONTEXT = true;
        Properties.SEARCH_BUDGET = 50000;

    }

    @After
    public void restoreProperties() {
        Properties.CRITERION = oldCriteria;
        Properties.STOPPING_CONDITION = oldStoppingCondition;
        Properties.PRIMITIVE_POOL = oldPrimitivePool;
        Properties.SECONDARY_OBJECTIVE = secondary;
        Properties.INSTRUMENT_CONTEXT = false;
        Properties.SEARCH_BUDGET = budget;
        Properties.TEST_ARCHIVE = oldArchive;
    }


    @Test
    public void testIBranchAsSecondaryObjective() {
        Properties.CRITERION = new Properties.Criterion[]{Criterion.BRANCH};
        Properties.SECONDARY_OBJECTIVE = new SecondaryObjective[]{Properties.SecondaryObjective.IBRANCH, Properties.SecondaryObjective.TOTAL_LENGTH};

        Properties.TEST_ARCHIVE = false;
        Properties.SEARCH_BUDGET = 60000;
        EvoSuite evosuite = new EvoSuite();
        String targetClass = EntryPointsClass.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(5, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testArchiveIBranchAsSecondaryObjective() {
        Properties.CRITERION = new Properties.Criterion[]{Criterion.BRANCH};
        Properties.SECONDARY_OBJECTIVE = new SecondaryObjective[]{Properties.SecondaryObjective.IBRANCH, Properties.SecondaryObjective.TOTAL_LENGTH};

        EvoSuite evosuite = new EvoSuite();
        String targetClass = EntryPointsClass.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 80000; // Sometimes seems a bit more difficult?
        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(5, goals);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testIBranch() {
        Properties.SEARCH_BUDGET = 140000;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.IBRANCH};
        Properties.SECONDARY_OBJECTIVE = new SecondaryObjective[]{Properties.SecondaryObjective.TOTAL_LENGTH};
        Properties.TEST_ARCHIVE = false;

        EvoSuite evosuite = new EvoSuite();
        String targetClass = EntryPointsClass.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(27, goals);
        Assert.assertTrue(best.getCoverage() >= 16.0 / 27.0);
    }

    @Test
    public void testArchiveIBranch() {
        Properties.SEARCH_BUDGET = 100000;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.IBRANCH};
        Properties.SECONDARY_OBJECTIVE = new SecondaryObjective[]{Properties.SecondaryObjective.TOTAL_LENGTH};

        EvoSuite evosuite = new EvoSuite();
        String targetClass = EntryPointsClass.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
        System.out.println("EvolvedTestSuite:\n" + best);
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(27, goals);
//		Assert.assertEquals("Non-optimal coverage: ", 0.92d, best.getCoverage(), 0.001);
    }


//	@Test
//	public void testListOfGoalsWith_RESET_STATIC_FIELDS_enable()
//	{
//		String targetClass = StaticFoo.class.getCanonicalName();
//		Properties.TARGET_CLASS = targetClass;
//
//		Properties.RESET_STATIC_FIELDS = true;
//
//		EvoSuite evosuite = new EvoSuite();
//
//		String[] command = new String[] {
//				"-printStats",
//				"-class", targetClass
//		};
//
//		evosuite.parseCommandLine(command);
//
//		LineCoverageFactory rc = new LineCoverageFactory();
//
//		List<LineCoverageTestFitness> goals = rc.getCoverageGoals();
//		for (LineCoverageTestFitness goal : goals)
//			System.out.println(goal);
//
//		assertEquals(goals.size(), 8);
//	}

}
