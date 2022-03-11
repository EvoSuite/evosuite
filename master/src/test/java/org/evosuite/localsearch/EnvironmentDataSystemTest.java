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

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.localsearch.DefaultLocalSearchObjective;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.testdata.EvoSuiteFile;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.localsearch.BranchCoverageMap;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.statements.environment.FileNamePrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.localsearch.TestSuiteLocalSearch;
import org.evosuite.utils.generic.*;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.localsearch.DseBar;
import com.examples.with.different.packagename.localsearch.DseFoo;

public class EnvironmentDataSystemTest extends SystemTestBase {

    @Before
    public void init() {
        Properties.LOCAL_SEARCH_PROBABILITY = 1.0;
        Properties.LOCAL_SEARCH_RATE = 1;
        Properties.LOCAL_SEARCH_BUDGET_TYPE = Properties.LocalSearchBudgetType.TESTS;
        Properties.LOCAL_SEARCH_BUDGET = 100;
        Properties.SEARCH_BUDGET = 50000;
        RuntimeSettings.useVFS = true;
        Properties.RESET_STATIC_FIELD_GETS = true;

    }

    @Test
    public void testOnSpecificTest() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
        Properties.TARGET_CLASS = DseBar.class.getCanonicalName();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
        Class<?> fooClass = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(DseFoo.class.getCanonicalName());
        GenericClass<?> clazz = GenericClassFactory.get(sut);

        DefaultTestCase test = new DefaultTestCase();

        // String string0 = "baz5";
        VariableReference stringVar = test.addStatement(new StringPrimitiveStatement(test, "baz5"));

        // DseFoo dseFoo0 = new DseFoo();
        GenericConstructor fooConstructor = new GenericConstructor(fooClass.getConstructors()[0], fooClass);
        ConstructorStatement fooConstructorStatement = new ConstructorStatement(test, fooConstructor, Arrays.asList(new VariableReference[]{}));
        VariableReference fooVar = test.addStatement(fooConstructorStatement);

        // String fileName = new String("/home/galeotti/README.txt")
        String path = "/home/galeotti/README.txt";
        EvoSuiteFile evosuiteFile = new EvoSuiteFile(path);
        FileNamePrimitiveStatement fileNameStmt = new FileNamePrimitiveStatement(test, evosuiteFile);
        test.addStatement(fileNameStmt);

        Method fooIncMethod = fooClass.getMethod("inc", new Class<?>[]{});
        GenericMethod incMethod = new GenericMethod(fooIncMethod, fooClass);
        test.addStatement(new MethodStatement(test, incMethod, fooVar, Arrays.asList(new VariableReference[]{})));
        test.addStatement(new MethodStatement(test, incMethod, fooVar, Arrays.asList(new VariableReference[]{})));
        test.addStatement(new MethodStatement(test, incMethod, fooVar, Arrays.asList(new VariableReference[]{})));
        test.addStatement(new MethodStatement(test, incMethod, fooVar, Arrays.asList(new VariableReference[]{})));
        test.addStatement(new MethodStatement(test, incMethod, fooVar, Arrays.asList(new VariableReference[]{})));

        // DseBar dseBar0 = new DseBar(string0);
        GenericConstructor gc = new GenericConstructor(clazz.getRawClass().getConstructors()[0], clazz);
        ConstructorStatement constructorStatement = new ConstructorStatement(test, gc, Arrays.asList(new VariableReference[]{stringVar}));
        VariableReference callee = test.addStatement(constructorStatement);

        // dseBar0.coverMe(dseFoo0);
        Method m = clazz.getRawClass().getMethod("coverMe", new Class<?>[]{fooClass});
        GenericMethod method = new GenericMethod(m, sut);
        MethodStatement ms = new MethodStatement(test, method, callee, Arrays.asList(new VariableReference[]{fooVar}));
        test.addStatement(ms);
        System.out.println(test);

        TestSuiteChromosome suite = new TestSuiteChromosome();
        BranchCoverageSuiteFitness fitness = new BranchCoverageSuiteFitness();

        BranchCoverageMap.getInstance().searchStarted(null);
        assertEquals(4.0, fitness.getFitness(suite), 0.1F);
        suite.addTest(test);
        assertEquals(1.0, fitness.getFitness(suite), 0.1F);

        System.out.println("Test suite: " + suite);

        Properties.CONCOLIC_TIMEOUT = Integer.MAX_VALUE;

        TestSuiteLocalSearch localSearch = TestSuiteLocalSearch.selectTestSuiteLocalSearch();
        LocalSearchObjective<TestSuiteChromosome> localObjective = new DefaultLocalSearchObjective();
        localObjective.addFitnessFunction(fitness);
        localSearch.doSearch(suite, localObjective);
        System.out.println("Fitness: " + fitness.getFitness(suite));
        System.out.println("Test suite: " + suite);
        assertEquals("Local search failed to cover class", 0.0, fitness.getFitness(suite), 0.1F);
        BranchCoverageMap.getInstance().searchFinished(null);
    }


}
