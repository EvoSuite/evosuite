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
package org.evosuite.mock.java.lang;

import com.examples.with.different.packagename.mock.java.lang.SourceExceptions;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 19/08/15.
 */
public class SourceExceptionsSystemTest extends SystemTestBase {

    @Test
    public void testRuntimeException() {
        String targetClass = SourceExceptions.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.REPLACE_CALLS = true;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.LINE, Properties.Criterion.EXCEPTION};

        EvoSuite evosuite = new EvoSuite();
        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Assert.assertNotNull(best);
        TestCaseExecutor.initExecutor();
        for (TestChromosome test : best.getTestChromosomes()) {
            ExecutionResult executionResult = TestCaseExecutor.runTest(test.getTestCase());
            test.setLastExecutionResult(executionResult);
        }

        String code = best.toString();
        Assert.assertTrue("Code:\n" + code, code.contains("verifyException(\"com.examples.with.different.packagename.mock.java.lang.SourceExceptions\","));
        Assert.assertTrue("Code:\n" + code, code.contains("verifyException(\"com.examples.with.different.packagename.mock.java.lang.SourceExceptions$Foo\","));
    }
}
