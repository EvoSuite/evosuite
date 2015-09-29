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
import org.evosuite.SystemTest;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.SearchStatistics;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.WordUtils;
import com.examples.with.different.packagename.WordUtilsTest;

public class TestCoverageAnalysisCharSequence extends SystemTest {

    @Test
    public void test() throws IOException {

        EvoSuite evosuite = new EvoSuite();

        String targetClass = WordUtils.class.getCanonicalName();
        String testClass = WordUtilsTest.class.getCanonicalName();
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
            "-Dsandbox=false",
            "-Dvirtual_fs=false",
            "-Dvirtual_net=false",
            "-Dreplace_calls=false",
            "-Dreplace_system_in=false",
            "-Dmax_loop_iterations=-1",
            "-measureCoverage"
        };

        SearchStatistics statistics = (SearchStatistics) evosuite.parseCommandLine(command);
        Assert.assertNotNull(statistics);

        Map<String, OutputVariable<?>> outputVariables = statistics.getOutputVariables();

        assertEquals(9, (Integer) outputVariables.get(RuntimeVariable.Total_Goals.name()).getValue(), 0.0);
        assertEquals(9, (Integer) outputVariables.get(RuntimeVariable.Covered_Goals.name()).getValue(), 0.0);
        assertEquals(1.0, (Double) outputVariables.get(RuntimeVariable.LineCoverage.name()).getValue(), 0.0);
        assertEquals(1, (Integer) outputVariables.get(RuntimeVariable.Tests_Executed.name()).getValue(), 0.0);
        assertEquals("111111111", (String) outputVariables.get(RuntimeVariable.LineCoverageBitString.name()).getValue());

        // check coverage matrix
        String coveragematrix_file = System.getProperty("user.dir") + File.separator +
                Properties.REPORT_DIR + File.separator +
                "data" + File.separator + Properties.TARGET_CLASS + "." + Properties.Criterion.LINE.name() + ".matrix";
        System.out.println("CoverageMatrix file " + coveragematrix_file);

        List<String> lines = Files.readAllLines(Paths.get(coveragematrix_file));
        // coverage of one test case
        assertEquals(1, lines.size());
        // all components have been covered ("1"), and the test case pass ("+")
        assertTrue(lines.get(0).equals("1 1 1 1 1 1 1 1 1 +"));
    }
}
