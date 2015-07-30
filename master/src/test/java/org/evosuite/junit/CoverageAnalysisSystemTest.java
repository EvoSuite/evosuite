package org.evosuite.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.StatisticsBackend;
import org.evosuite.SystemTest;
import org.evosuite.continuous.persistency.CsvJUnitData;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.SearchStatistics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.Calculator;
import com.examples.with.different.packagename.CalculatorTest;
import com.examples.with.different.packagename.ClassHierarchyIncludingInterfaces;
import com.examples.with.different.packagename.ClassHierarchyIncludingInterfacesTest;
import com.examples.with.different.packagename.ClassWithPrivateInterfaces;
import com.examples.with.different.packagename.ClassWithPrivateInterfacesTest;

import au.com.bytecode.opencsv.CSVReader;

public class CoverageAnalysisSystemTest extends SystemTest {

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
	public void testOneClassOneCriterion() {

		EvoSuite evosuite = new EvoSuite();

        String targetClass = Calculator.class.getCanonicalName();
        String testClass = CalculatorTest.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[] {
        	Properties.Criterion.BRANCH
        };

        String[] command = new String[] {
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
	public void testOneClassMoreThanOneCriterion() throws IOException {

		EvoSuite evosuite = new EvoSuite();

        String targetClass = Calculator.class.getCanonicalName();
        String testClass = CalculatorTest.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[] {
        	Properties.Criterion.BRANCH,
        	Properties.Criterion.LINE
        };

        Properties.OUTPUT_VARIABLES = "TARGET_CLASS,criterion," +
        		RuntimeVariable.Coverage.name() + "," + RuntimeVariable.Covered_Goals + "," + RuntimeVariable.Total_Goals + "," +
        		RuntimeVariable.BranchCoverage + "," + RuntimeVariable.BranchCoverageBitString + "," +
        		RuntimeVariable.LineCoverage + "," + RuntimeVariable.LineCoverageBitString;
        Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;

        String[] command = new String[] {
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
        assertTrue(rows.size() == 2);
        reader.close();

        assertTrue(CsvJUnitData.getValue(rows, "TARGET_CLASS").equals(Calculator.class.getCanonicalName()));
        assertTrue(CsvJUnitData.getValue(rows, "criterion").equals(Properties.Criterion.BRANCH.toString() + ";" + Properties.Criterion.LINE.toString()));

        assertEquals(Double.valueOf(CsvJUnitData.getValue(rows, "Coverage")), 0.88, 0.01);
        assertEquals(Integer.valueOf(CsvJUnitData.getValue(rows, "Covered_Goals")), 8, 0);
        assertEquals(Integer.valueOf(CsvJUnitData.getValue(rows, "Total_Goals")), 9, 0);

        assertEquals(Double.valueOf(CsvJUnitData.getValue(rows, "BranchCoverage")), 0.8, 0.0);
        assertEquals(Double.valueOf(CsvJUnitData.getValue(rows, "LineCoverage")), 1.0, 0.0);

        assertTrue(CsvJUnitData.getValue(rows, "BranchCoverageBitString").equals("10111"));
        assertTrue(CsvJUnitData.getValue(rows, "LineCoverageBitString").equals("1111"));
	}

	@Test
	public void testMoreThanOneClassOneCriterion() {
		fail("Implementation missing...");
	}

	@Test
	public void testMoreThanOneClassMoreThanOneCriterion() {
		fail("Implementation missing...");
	}

	@Test
	public void testGetAllInterfaces() throws IOException {

		EvoSuite evosuite = new EvoSuite();

        String targetClass = ClassWithPrivateInterfaces.class.getCanonicalName();
        String testClass = ClassWithPrivateInterfacesTest.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[] {
        	Properties.Criterion.LINE
        };

        Properties.OUTPUT_VARIABLES = RuntimeVariable.Total_Goals + "," + RuntimeVariable.LineCoverage;
        Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;
        Properties.COVERAGE_MATRIX = true;

        String[] command = new String[] {
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
        assertTrue(rows.size() == 2);
        reader.close();

        assertEquals("13", CsvJUnitData.getValue(rows, RuntimeVariable.Total_Goals.name()));
        assertEquals("1.0", CsvJUnitData.getValue(rows, RuntimeVariable.LineCoverage.name()));

        // Assert that all test cases have passed

        String matrix_file = System.getProperty("user.dir") + File.separator + 
        		Properties.REPORT_DIR + File.separator + 
        		"data" + File.separator +
        		targetClass + "." + Properties.Criterion.LINE.name() + ".matrix";
        System.out.println("matrix_file: " + matrix_file);

        List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath(matrix_file));
        assertTrue(lines.size() == 1);

        assertEquals(13 + 1, lines.get(0).replace(" ", "").length()); // number of goals + test result ('+' pass, '-' fail)
        assertTrue(lines.get(0).replace(" ", "").endsWith("+"));
	}

	@Test
	public void testHierarchyIncludingInterfaces() throws IOException {

		EvoSuite evosuite = new EvoSuite();

        String targetClass = ClassHierarchyIncludingInterfaces.class.getCanonicalName();
        String testClass = ClassHierarchyIncludingInterfacesTest.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[] {
        	Properties.Criterion.LINE
        };

        Properties.OUTPUT_VARIABLES = RuntimeVariable.Total_Goals + "," + RuntimeVariable.LineCoverage;
        Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;
        Properties.COVERAGE_MATRIX = true;

        String[] command = new String[] {
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
        assertTrue(rows.size() == 2);
        reader.close();

        assertEquals("32", CsvJUnitData.getValue(rows, RuntimeVariable.Total_Goals.name()));

        // Assert that all test cases have passed

        String matrix_file = System.getProperty("user.dir") + File.separator + 
        		Properties.REPORT_DIR + File.separator + 
        		"data" + File.separator +
        		targetClass + "." + Properties.Criterion.LINE.name() + ".matrix";
        System.out.println("matrix_file: " + matrix_file);

        List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath(matrix_file));
        assertTrue(lines.size() == 1);

        assertEquals(32 + 1, lines.get(0).replace(" ", "").length()); // number of goals + test result ('+' pass, '-' fail)
        assertTrue(lines.get(0).replace(" ", "").endsWith("+"));
	}
}
