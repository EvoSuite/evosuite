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
package org.evosuite.localsearch;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTestBase;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.coverage.cbranch.CBranchSuiteFitness;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;

import com.examples.with.different.packagename.cbranch.CBranchExample;

public class CBranchFitnessArchiveSystemTest extends SystemTestBase {

    @Test
    public void test() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
        Properties.TARGET_CLASS = CBranchExample.class.getCanonicalName();
        Properties.CRITERION = new Criterion[]{Criterion.CBRANCH};
        Properties.TEST_ARCHIVE = true;

        InstrumentingClassLoader classLoader = TestGenerationContext.getInstance().getClassLoaderForSUT();
        Class<CBranchExample> sut = (Class<CBranchExample>) classLoader.loadClass(Properties.TARGET_CLASS);
        List<String> classpath = new ArrayList<>();
        String cp = System.getProperty("user.dir") + "/target/test-classes";
        classpath.add(cp);
        ClassPathHandler.getInstance().addElementToTargetProjectClassPath(cp);
        DependencyAnalysis.analyzeClass(Properties.TARGET_CLASS, classpath);

        Constructor<CBranchExample> ctor = sut.getConstructor();
        Method method = sut.getMethod("Subject", String.class, String.class);

        TestSuiteChromosome suiteChromosome = new TestSuiteChromosome();
        TestCaseBuilder builder = new TestCaseBuilder();
        VariableReference fileSuffix1 = builder.appendConstructor(ctor);
        VariableReference string0 = builder.appendStringPrimitive("MY}6.);:<<TtF");
        VariableReference string1 = builder.appendStringPrimitive("bin");
        builder.appendMethod(fileSuffix1, method, string0, string1);

        DefaultTestCase defaultTestCase = builder.getDefaultTestCase();
        suiteChromosome.addTest(defaultTestCase);

        CBranchSuiteFitness fitness = new CBranchSuiteFitness();
        double fitnessValue = fitness.getFitness(suiteChromosome);

        // removes covered goals due to ARCHIVE
        fitness.updateCoveredGoals();

        double fitnessValue2 = fitness.getFitness(suiteChromosome);

        assertTrue(fitnessValue2 <= fitnessValue);

    }
}
