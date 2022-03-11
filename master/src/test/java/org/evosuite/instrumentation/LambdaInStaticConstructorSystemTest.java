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
package org.evosuite.instrumentation;

import com.examples.with.different.packagename.LambdaInStaticConstructor;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;

public class LambdaInStaticConstructorSystemTest extends SystemTestBase {

    @Test
    public void testNoCrashInCLINIT() throws Throwable {
        String targetClass = LambdaInStaticConstructor.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.TIMEOUT = 50000000;
        Properties.RESET_STATIC_FINAL_FIELDS = true;

        EvoSuite evosuite = new EvoSuite();
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);

        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println(best.toString());

    }
}
