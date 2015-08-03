package org.evosuite.junit;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.StatisticsBackend;
import org.evosuite.statistics.RuntimeVariable;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.ClassNumberUtils;
import com.examples.with.different.packagename.ClassNumberUtilsTest;

public class TestCoverageAnalysisStringInstrumentation {

	@Test
	public void testCreateBigInteger() throws IOException {

		EvoSuite evosuite = new EvoSuite();

        String targetClass = ClassNumberUtils.class.getCanonicalName();
        String testClass = ClassNumberUtilsTest.class.getCanonicalName();
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

        fail("* Failure: String index out of range: 3"
        		+ "   java.lang.String.substring(String.java:1963)"
        		+ "   org.evosuite.instrumentation.testability.StringHelper.StringStartsWith(StringHelper.java:324)");
	}
}
