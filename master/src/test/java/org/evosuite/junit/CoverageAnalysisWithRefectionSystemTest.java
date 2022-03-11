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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.opencsv.exceptions.CsvException;
import org.apache.commons.io.FileUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.StatisticsBackend;
import org.evosuite.SystemTestBase;
import org.evosuite.continuous.persistency.CsvJUnitData;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.SearchStatistics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.ClassHierarchyIncludingInterfaces;
import com.examples.with.different.packagename.ClassHierarchyIncludingInterfacesTest;
import com.examples.with.different.packagename.ClassPublicInterface;
import com.examples.with.different.packagename.ClassPublicInterfaceTest;
import com.examples.with.different.packagename.ClassWithPrivateInterfaces;
import com.examples.with.different.packagename.ClassWithPrivateInterfacesTest;

import com.opencsv.CSVReader;

public class CoverageAnalysisWithRefectionSystemTest extends SystemTestBase {

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

        CoverageAnalysis.reset();
    }

    @Test
    public void testGetAllInterfaces() throws IOException, CsvException {

        EvoSuite evosuite = new EvoSuite();

        String targetClass = ClassWithPrivateInterfaces.class.getCanonicalName();
        String testClass = ClassWithPrivateInterfacesTest.class.getCanonicalName();
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

        Object statistics = evosuite.parseCommandLine(command);
        Assert.assertNotNull(statistics);

        // Assert coverage

        String statistics_file = System.getProperty("user.dir") + File.separator +
                Properties.REPORT_DIR + File.separator +
                "statistics.csv";
        System.out.println("statistics_file: " + statistics_file);

        CSVReader reader = new CSVReader(new FileReader(statistics_file));
        List<String[]> rows = reader.readAll();
        assertEquals(2, rows.size());
        reader.close();

        assertEquals("14", CsvJUnitData.getValue(rows, RuntimeVariable.Total_Goals.name()));
        assertEquals(0.93, Double.valueOf(CsvJUnitData.getValue(rows, RuntimeVariable.LineCoverage.name())), 0.01);

        // Assert that all test cases have passed

        String matrix_file = System.getProperty("user.dir") + File.separator +
                Properties.REPORT_DIR + File.separator +
                "data" + File.separator + targetClass + File.separator +
                Properties.Criterion.LINE.name() + File.separator + Properties.COVERAGE_MATRIX_FILENAME;
        System.out.println("matrix_file: " + matrix_file);

        List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath(matrix_file));
        assertEquals(1, lines.size());

        assertEquals(13 + 1 + 1, lines.get(0).replace(" ", "").length()); // number of goals + test result ('+' pass, '-' fail)
        assertTrue(lines.get(0).replace(" ", "").endsWith("+"));
    }

    @Test
    public void testHierarchyIncludingInterfaces() throws IOException, CsvException {

        EvoSuite evosuite = new EvoSuite();

        String targetClass = ClassHierarchyIncludingInterfaces.class.getCanonicalName();
        String testClass = ClassHierarchyIncludingInterfacesTest.class.getCanonicalName();
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

        Object statistics = evosuite.parseCommandLine(command);
        Assert.assertNotNull(statistics);

        // Assert coverage

        String statistics_file = System.getProperty("user.dir") + File.separator +
                Properties.REPORT_DIR + File.separator +
                "statistics.csv";
        System.out.println("statistics_file: " + statistics_file);

        CSVReader reader = new CSVReader(new FileReader(statistics_file));
        List<String[]> rows = reader.readAll();
        assertEquals(2, rows.size());
        reader.close();

        // The number of lines seems to be different depending on the compiler
        assertTrue("Expected 32-34lines, but found: " + CsvJUnitData.getValue(rows, RuntimeVariable.Total_Goals.name()),
                CsvJUnitData.getValue(rows, RuntimeVariable.Total_Goals.name()).equals("32") ||
                        CsvJUnitData.getValue(rows, RuntimeVariable.Total_Goals.name()).equals("33") ||
                        CsvJUnitData.getValue(rows, RuntimeVariable.Total_Goals.name()).equals("34"));

        // Assert that all test cases have passed

        String matrix_file = System.getProperty("user.dir") + File.separator +
                Properties.REPORT_DIR + File.separator +
                "data" + File.separator + targetClass + File.separator +
                Properties.Criterion.LINE.name() + File.separator + Properties.COVERAGE_MATRIX_FILENAME;
        System.out.println("matrix_file: " + matrix_file);

        List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath(matrix_file));
        assertEquals(1, lines.size());

        // The number of lines seems to be different depending on the compiler
        assertTrue("Expected lines to be 32-34, but got: " + (lines.get(0).replace(" ", "").length()), 34 - lines.get(0).replace(" ", "").length() <= 2); // number of goals + test result ('+' pass, '-' fail)
        assertTrue("Expected line to end with +, but line is: " + lines.get(0).replace(" ", ""), lines.get(0).replace(" ", "").endsWith("+"));
    }

    @Test
    public void testBindFilteredEventsToMethod() throws IOException {

        EvoSuite evosuite = new EvoSuite();

        String targetClass = ClassPublicInterface.class.getCanonicalName();
        String testClass = ClassPublicInterfaceTest.class.getCanonicalName();
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

        // The number of lines seems to be different depending on the compiler
        assertTrue(27 - ((Integer) outputVariables.get(RuntimeVariable.Total_Goals.name()).getValue()) <= 1);
        assertTrue(11 - ((Integer) outputVariables.get(RuntimeVariable.Covered_Goals.name()).getValue()) <= 1);
        assertEquals(1, (Integer) outputVariables.get(RuntimeVariable.Tests_Executed.name()).getValue(), 0.0);

        // Assert that all test cases have passed

        String matrix_file = System.getProperty("user.dir") + File.separator +
                Properties.REPORT_DIR + File.separator +
                "data" + File.separator + targetClass + File.separator +
                Properties.Criterion.LINE.name() + File.separator + Properties.COVERAGE_MATRIX_FILENAME;
        System.out.println("matrix_file: " + matrix_file);

        List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath(matrix_file));
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).replace(" ", "").endsWith("+"));
    }
}
