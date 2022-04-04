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
import org.junit.Ignore;
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
 */
public class RegressionSystemTest extends SystemTestBase {

    private GeneticAlgorithm<?> runTest(String targetClass) {
        EvoSuite evosuite = new EvoSuite();

        //Properties.CLIENT_ON_THREAD = true;
        Properties.TARGET_CLASS = targetClass;
        //Properties.resetTargetClass();
        Properties.SEARCH_BUDGET = 100000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        //, "-cp",
        //        "target/test-classes", "-Dshow_progress=false"};
        //		        "-Dclient_on_thread=true", "-Dsearch_budget=100000" };

        Object result = evosuite.parseCommandLine(command);
        return getGAFromResult(result);
    }

    private void testCovered(String targetClass, int numGoals) {
        GeneticAlgorithm<?> ga = runTest(targetClass);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println(best.toString());
        // TODO: Need to fix the check, some reset is not working
        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
        Assert.assertEquals(numGoals, goals);
        Assert.assertEquals("Wrong number of target goals", numGoals,
                best.getNumOfCoveredGoals());
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
        Assert.assertEquals("Wrong fitness: ", 0.0, best.getFitness(), 0.00);
        Assert.assertTrue("Wrong number of statements: ", best.size() > 0);
    }

    @Test
    public void testAbs() {
        testCovered(AbsTest.class.getCanonicalName(), 3);
    }

    @Test
    public void testArray() {
        testCovered(ArrayTest.class.getCanonicalName(), 9);
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

    // Deprecated is now only applied to dependencies
    @Test
    @Ignore
    public void testDependency_noDeprecated() {
        Properties.USE_DEPRECATED = false;
        testCovered(DepTest.class.getCanonicalName(), 2);
    }

    @Test
    public void testDependency_withDeprecated() {
        Properties.USE_DEPRECATED = true;
        testCovered(DepTest.class.getCanonicalName(), 3);
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
