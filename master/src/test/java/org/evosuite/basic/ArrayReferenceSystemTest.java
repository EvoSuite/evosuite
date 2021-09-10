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
package org.evosuite.basic;

import com.examples.with.different.packagename.ArrayReference;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class ArrayReferenceSystemTest extends SystemTestBase {

    public TestSuiteChromosome generateTest(boolean minimize) {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ArrayReference.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.ASSERTIONS = false;
        Properties.JUNIT_CHECK = Properties.JUnitCheckValues.FALSE;
        Properties.MINIMIZE = minimize;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        return ga.getBestIndividual();
    }

    @Test
    public void testArrayReferenceWithoutMinimization() {
        TestSuiteChromosome best = generateTest(false);
        Assert.assertFalse("Array reference should not be assigned to its first element", best.toString().contains("constructorArray0[0] = (Constructor<Insets>) constructorArray0;"));
    }

    @Test
    public void testArrayReferenceWithMinimization() {
        TestSuiteChromosome best = generateTest(true);
        Assert.assertFalse("Array reference should not be assigned to its first element", best.toString().contains("constructorArray0[0] = (Constructor<Insets>) constructorArray0;"));
    }

}
