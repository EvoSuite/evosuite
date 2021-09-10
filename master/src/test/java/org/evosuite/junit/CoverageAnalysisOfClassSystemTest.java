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
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.opencsv.exceptions.CsvException;
import org.apache.commons.io.FileUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.Properties.StatisticsBackend;
import org.evosuite.continuous.persistency.CsvJUnitData;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.SearchStatistics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.Calculator;
import com.examples.with.different.packagename.CalculatorTest;
import com.examples.with.different.packagename.coverage.MethodWithSeveralInputArguments;
import com.examples.with.different.packagename.coverage.TestMethodWithSeveralInputArguments;

import com.opencsv.CSVReader;

public class CoverageAnalysisOfClassSystemTest extends SystemTestBase {

    @Before
    public void prepare() {
        try {
            FileUtils.deleteDirectory(new File("evosuite-report"));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        Properties.TARGET_CLASS = "";
        Properties.OUTPUT_VARIABLES = null;
        Properties.STATISTICS_BACKEND = StatisticsBackend.DEBUG;
        Properties.COVERAGE_MATRIX = false;

        SearchStatistics.clearInstance();
        CoverageAnalysis.reset();
    }

    @Test
    public void testOneClassOneCriterion() {

        EvoSuite evosuite = new EvoSuite();

        String targetClass = Calculator.class.getCanonicalName();
        String testClass = CalculatorTest.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[]{
                Properties.Criterion.BRANCH
        };

        String[] command = new String[]{
                "-class", targetClass,
                "-Djunit=" + testClass,
                "-measureCoverage"
        };

        SearchStatistics statistics = (SearchStatistics) evosuite.parseCommandLine(command);
        Assert.assertNotNull(statistics);

        Map<String, OutputVariable<?>> outputVariables = statistics.getOutputVariables();

        assertEquals(0.80, (Double) outputVariables.get("BranchCoverage").getValue(), 0.0);
        assertEquals(0.80, (Double) outputVariables.get("Coverage").getValue(), 0.0);
        assertEquals(4, (Integer) outputVariables.get("Tests_Executed").getValue(), 0);
        assertEquals(4, (Integer) outputVariables.get("Covered_Goals").getValue(), 0);
        assertEquals(5, (Integer) outputVariables.get("Total_Goals").getValue(), 0);
        assertEquals("10111", outputVariables.get("BranchCoverageBitString").getValue());
    }

    @Test
    public void testOneClassMoreThanOneCriterion() throws IOException, CsvException {

        EvoSuite evosuite = new EvoSuite();

        String targetClass = Calculator.class.getCanonicalName();
        String testClass = CalculatorTest.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[]{
                Properties.Criterion.BRANCH,
                Properties.Criterion.LINE
        };

        Properties.OUTPUT_VARIABLES = "TARGET_CLASS,criterion," +
                RuntimeVariable.Coverage.name() + "," + RuntimeVariable.Covered_Goals + "," + RuntimeVariable.Total_Goals + "," +
                RuntimeVariable.BranchCoverage + "," + RuntimeVariable.BranchCoverageBitString + "," +
                RuntimeVariable.LineCoverage + "," + RuntimeVariable.LineCoverageBitString;
        Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;

        String[] command = new String[]{
                "-class", targetClass,
                "-Djunit=" + testClass,
                "-measureCoverage"
        };

        SearchStatistics statistics = (SearchStatistics) evosuite.parseCommandLine(command);
        Assert.assertNotNull(statistics);

        String statistics_file = System.getProperty("user.dir") + File.separator + Properties.REPORT_DIR + File.separator + "statistics.csv";
        System.out.println(statistics_file);

        CSVReader reader = new CSVReader(new FileReader(statistics_file));
        List<String[]> rows = reader.readAll();
        assertEquals(2, rows.size());
        reader.close();

        assertEquals(CsvJUnitData.getValue(rows, "TARGET_CLASS"), Calculator.class.getCanonicalName());
        assertEquals(CsvJUnitData.getValue(rows, "criterion"), Properties.Criterion.BRANCH.toString() + ";" + Properties.Criterion.LINE.toString());

        assertEquals(0.8, Double.valueOf(CsvJUnitData.getValue(rows, RuntimeVariable.Coverage.name())), 0.01);
        assertEquals(8, (int) Integer.valueOf(CsvJUnitData.getValue(rows, RuntimeVariable.Covered_Goals.name())));
        assertEquals(10, (int) Integer.valueOf(CsvJUnitData.getValue(rows, RuntimeVariable.Total_Goals.name())));

        assertEquals(0.8, Double.valueOf(CsvJUnitData.getValue(rows, RuntimeVariable.BranchCoverage.name())), 0.0);
        assertEquals(0.8, Double.valueOf(CsvJUnitData.getValue(rows, RuntimeVariable.LineCoverage.name())), 0.0);

        assertEquals("10111", CsvJUnitData.getValue(rows, RuntimeVariable.BranchCoverageBitString.name()));
        assertEquals("01111", CsvJUnitData.getValue(rows, RuntimeVariable.LineCoverageBitString.name()));
    }

    @Test
    public void testMeasureCoverageOfCarvedTests() {

        EvoSuite evosuite = new EvoSuite();

        String targetClass = MethodWithSeveralInputArguments.class.getCanonicalName();
        String testClass = TestMethodWithSeveralInputArguments.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[]{
                Properties.Criterion.INPUT,
                Properties.Criterion.METHOD,
                Properties.Criterion.OUTPUT
        };

        String[] command = new String[]{
                "-class", targetClass,
                "-Djunit=" + testClass,
                "-Dselected_junit=" + testClass,
                "-measureCoverage"
        };

        SearchStatistics result = (SearchStatistics) evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);
        OutputVariable<?> methodCoverage = result.getOutputVariables().get(RuntimeVariable.MethodCoverage.name());
        OutputVariable<?> inputCoverage = result.getOutputVariables().get(RuntimeVariable.InputCoverage.name());
        OutputVariable<?> outputCoverage = result.getOutputVariables().get(RuntimeVariable.OutputCoverage.name());

        Assert.assertEquals("Unexpected method coverage value", 1d, (Double) methodCoverage.getValue(), 0.01);
        Assert.assertEquals("Unexpected input coverage value", 0.67d, (Double) inputCoverage.getValue(), 0.01);
        Assert.assertEquals("Unexpected output coverage value", 0.33d, (Double) outputCoverage.getValue(), 0.01);
    }
}
