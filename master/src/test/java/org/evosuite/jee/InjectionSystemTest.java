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
package org.evosuite.jee;

import com.examples.with.different.packagename.jee.injection.*;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 29/06/15.
 */
public class InjectionSystemTest extends SystemTestBase {

    private void doTest(Class<?> target){
        EvoSuite evosuite = new EvoSuite();

        String targetClass = target.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.LINE};
        Properties.JEE = true;
        Properties.P_REFLECTION_ON_PRIVATE = 0.0;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testInjectionSourceForItself(){
        doTest(InjectionSourceForItself.class);
    }

    @Test
    public void testInjectionInInputParameter(){
        doTest(InjectionInInputParameter.class);
    }

    @Test
    public void testInjectionSimpleInheritance(){
        doTest(InjectionSimpleInheritance.class);
    }

    @Test
    public void testInjectionTrivialInheritance(){
        doTest(InjectionTrivialInheritance.class);
    }


    @Test
    public void testInjectionWithInheritance(){
        doTest(InjectionWithInheritance.class);
    }


    @Test
    public void testInjectionWithSimpleMethods(){
        doTest(InjectionWithSimpleMethods.class);
    }

    @Test
    public void testCombination(){
        doTest(InjectionWithInheritance.class);
        super.resetStaticVariables(); //After
        super.setDefaultPropertiesForTestCases(); //Before
        doTest(InjectionSimpleInheritance.class);
    }

    @Test
    public void testCombination3(){
        doTest(InjectionWithInheritance.class);
        super.resetStaticVariables(); //After
        super.setDefaultPropertiesForTestCases(); //Before
        doTest(GeneralInjectionExample.class);
        super.resetStaticVariables(); //After
        super.setDefaultPropertiesForTestCases(); //Before
        doTest(InjectionSimpleInheritance.class);
    }

    @Test
    public void testInjectionAndPostConstruct(){
        doTest(InjectionAndPostConstruct.class);
    }

    @Test
    public void testGeneralInjection(){
        doTest(GeneralInjectionExample.class);
    }

    @Test
    public void testPostConstructor(){
        doTest(PostConstructorInjection.class);
    }

    @Test
    public void testEntityManager(){
        doTest(EntityManagerInjection.class);
    }


    @Test
    public void testMultipleDefaultInjections(){
        doTest(MultipleDefaultInjections.class);
    }
}
