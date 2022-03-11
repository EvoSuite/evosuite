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
package org.evosuite.coverage;

import com.examples.with.different.packagename.coverage.BooleanOneLine;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by arcuri on 12/24/14.
 */
public class CompositeFitnessSystemTest extends SystemTestBase {

    @Test
    public void testBooleanOneLine_3() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = BooleanOneLine.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[]{
                Properties.Criterion.LINE,
                Properties.Criterion.OUTPUT,
                Properties.Criterion.EXCEPTION
        };

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();

        System.out.println(best);
        /*
            Should be 3 tests:
            1) return true
            2) return false
            3) NPE
         */
        Assert.assertEquals(3, best.getTests().size());
    }
}
