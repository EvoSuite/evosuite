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
package org.evosuite.junit.writer;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.junit.writer.Foo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestSuiteWriterSystemTest extends SystemTestBase {


    @Test
    public void testSingleFile() {
        Properties.TEST_SCAFFOLDING = false;
        test();
    }

    @Test
    public void testScaffoldingFile() {
        Properties.TEST_SCAFFOLDING = true;
        test();
    }

    @Test
    public void testWriteCoveredGoals() throws IOException {
        Properties.WRITE_COVERED_GOALS_FILE = true;
        test();
        Path path = Paths.get(Properties.COVERED_GOALS_FILE);
        Assert.assertTrue("Covered goals file does not exist", Files.exists(path));
        Assert.assertEquals("Covered goals file with 2 lines was expected", 2, Files.readAllLines(path).size());
    }


    public void test() {

        Assert.assertNull(System.getSecurityManager());

        String targetClass = Foo.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.JUNIT_TESTS = true;
        Properties.JUNIT_CHECK = Properties.JUnitCheckValues.TRUE;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        EvoSuite evosuite = new EvoSuite();
        Object result = evosuite.parseCommandLine(command);

        Assert.assertNotNull(result);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }
}
