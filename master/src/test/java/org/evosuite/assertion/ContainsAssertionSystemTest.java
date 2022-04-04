/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.assertion;

import com.examples.with.different.packagename.assertion.ContainerExample;
import org.evosuite.EvoSuite;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestCase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class ContainsAssertionSystemTest extends SystemTestBase {

    @Test
    public void testAssertionsIncludeContains() {

        //Properties.INLINE = false;
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ContainerExample.class.getCanonicalName();

        String[] command = new String[]{
                "-generateSuite", "-class", targetClass, "-Dassertion_strategy=all"};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome suite = ga.getBestIndividual();
        System.out.println(suite.toString());

        Assert.assertTrue(suite.size() > 0);
        for (TestCase test : suite.getTests()) {
            boolean hasContainsAssertion = false;
            for (Assertion ass : test.getAssertions()) {
                if (ass instanceof ContainsAssertion) {
                    hasContainsAssertion = true;
                }
            }
            Assert.assertTrue("Test has no contains assertions: " + test.toCode(),
                    hasContainsAssertion);
        }
    }
}
