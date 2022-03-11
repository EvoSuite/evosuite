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
package org.evosuite.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.StatisticsBackend;
import org.evosuite.SystemTestBase;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.SearchStatistics;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.FinalClass;
import com.examples.with.different.packagename.FinalClassTest;

public class CoverageAnalysisFinalClassSystemTest extends SystemTestBase {

    @Test
    public void test() throws IOException {

        EvoSuite evosuite = new EvoSuite();

        String targetClass = FinalClass.class.getCanonicalName();
        String testClass = FinalClassTest.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[]{
                Properties.Criterion.LINE
        };

        Properties.OUTPUT_VARIABLES = RuntimeVariable.Total_Goals + "," + RuntimeVariable.LineCoverage;
        Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;
        Properties.COVERAGE_MATRIX = true;

        String[] command = new String[]{
                "-class", targetClass,
                "-Djunit=" + testClass,
                "-measureCoverage"
        };

        SearchStatistics statistics = (SearchStatistics) evosuite.parseCommandLine(command);
        Assert.assertNotNull(statistics);

        Map<String, OutputVariable<?>> outputVariables = statistics.getOutputVariables();

        assertEquals(2, (Integer) outputVariables.get(RuntimeVariable.Total_Goals.name()).getValue(), 0.0);
        assertEquals(2, (Integer) outputVariables.get(RuntimeVariable.Covered_Goals.name()).getValue(), 0.0);
        assertEquals(1, (Double) outputVariables.get(RuntimeVariable.LineCoverage.name()).getValue(), 0.0);
        assertEquals(1, (Integer) outputVariables.get(RuntimeVariable.Tests_Executed.name()).getValue(), 0.0);

        // check coverage matrix
        String coveragematrix_file = System.getProperty("user.dir") + File.separator +
                Properties.REPORT_DIR + File.separator +
                "data" + File.separator + Properties.TARGET_CLASS + File.separator +
                Properties.Criterion.LINE.name() + File.separator + Properties.COVERAGE_MATRIX_FILENAME;
        System.out.println("CoverageMatrix file " + coveragematrix_file);

        List<String> lines = Files.readAllLines(Paths.get(coveragematrix_file));
        // coverage of one test case
        assertEquals(1, lines.size());
        // the test case pass ("+")
        assertTrue(lines.get(0).contains("+"));
    }
}
