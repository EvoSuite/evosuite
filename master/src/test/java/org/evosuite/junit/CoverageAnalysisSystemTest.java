package org.evosuite.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.StatisticsBackend;
import org.evosuite.SystemTest;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.SearchStatistics;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.Calculator;
import com.examples.with.different.packagename.CalculatorTest;

import au.com.bytecode.opencsv.CSVReader;

public class CoverageAnalysisSystemTest extends SystemTest {

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
            "-junit", testClass,
            "-Djunit_prefix=" + testClass,
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
        assertEquals("01111", outputVariables.get("CoverageBitString").getValue().toString());
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

        Properties.OUTPUT_VARIABLES="TARGET_CLASS,criterion,Coverage,Covered_Goals,Total_Goals,CoverageBitString";
        Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;

        String[] command = new String[] {
            "-class", targetClass,
            "-junit", testClass,
            "-Djunit_prefix=" + testClass,
            "-measureCoverage"
        };

        SearchStatistics statistics = (SearchStatistics) evosuite.parseCommandLine(command);
        Assert.assertNotNull(statistics);

        String statistics_file = System.getProperty("user.dir") + File.separator + Properties.REPORT_DIR + File.separator + "statistics.csv";
        System.out.println(statistics_file);

        CSVReader reader = new CSVReader(new FileReader(statistics_file));
        List<String[]> rows = reader.readAll();
        assertTrue(rows.size() == 3);
        reader.close();

        List<String> values = this.getValues(rows, "TARGET_CLASS");
        assertTrue(values.get(0).equals("com.examples.with.different.packagename.Calculator"));
        assertTrue(values.get(1).equals("com.examples.with.different.packagename.Calculator"));

        values = this.getValues(rows, "criterion");
        assertTrue(values.get(0).equals(Properties.Criterion.BRANCH.toString()));
        assertTrue(values.get(1).equals(Properties.Criterion.LINE.toString()));

        values = this.getValues(rows, "Coverage");
        assertEquals(Double.valueOf(values.get(0)), 0.8, 0.0);
        assertEquals(Double.valueOf(values.get(1)), 1.0, 0.0);

        values = this.getValues(rows, "Covered_Goals");
        assertEquals(Integer.valueOf(values.get(0)), 4, 0);
        assertEquals(Integer.valueOf(values.get(1)), 4, 0);

        values = this.getValues(rows, "Total_Goals");
        assertEquals(Integer.valueOf(values.get(0)), 5, 0);
        assertEquals(Integer.valueOf(values.get(1)), 4, 0);

        values = this.getValues(rows, "CoverageBitString");
        assertTrue(values.get(0).equals("01111"));
        assertTrue(values.get(1).equals("1111"));
	}

	@Test
	public void testMoreThanOneClassOneCriterion() {
		fail("Implementation missing...");
	}

	@Test
	public void testMoreThanOneClassMoreThanOneCriterion() {
		fail("Implementation missing...");
	}

	/**
	 * 
	 */
	private List<String> getValues(List<String[]> rows, String columnName) {
		String[] header = rows.get(0);

		int column;
		for (column = 0; column < header.length; column++) {
			if (header[column].trim().equalsIgnoreCase(columnName.trim())) {
				break;
			}
		}

		List<String> values = new ArrayList<String>();
		for (int row_i = 1; row_i < rows.size(); row_i++) {
			String[] row = rows.get(row_i);
			values.add(row[column]);
		}

		return values;
	}
}
