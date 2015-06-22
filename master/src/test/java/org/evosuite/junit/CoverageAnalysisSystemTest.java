package org.evosuite.junit;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import com.examples.with.different.packagename.CalculatorTest;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTest;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.SearchStatistics;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.Calculator;

public class CoverageAnalysisSystemTest extends SystemTest {

	@Test
	public void testOneCriterion() {

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
        assertEquals(5, (Integer) outputVariables.get("Total_Goals").getValue(), 0);
	}

	@Test
	public void testMoreThanOneCriterion() {

		EvoSuite evosuite = new EvoSuite();

        String targetClass = Calculator.class.getCanonicalName();
        String testClass = CalculatorTest.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[] {
    		Criterion.LINE,
    		Criterion.BRANCH,
    		Criterion.EXCEPTION,
    		Criterion.WEAKMUTATION,
    		Criterion.OUTPUT,
    		Criterion.METHOD,
    		Criterion.METHODNOEXCEPTION,
    		Criterion.CBRANCH
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
        assertEquals(1.00, (Double) outputVariables.get("LineCoverage").getValue(), 0.0);
        assertEquals(1.00, (Double) outputVariables.get("ExceptionCoverage").getValue(), 0.0);
        assertEquals(1.00, (Double) outputVariables.get("WeakMutationScore").getValue(), 0.05);
        assertEquals(0.00, (Double) outputVariables.get("OutputCoverage").getValue(), 0.0);
        assertEquals(0.00, (Double) outputVariables.get("MethodCoverage").getValue(), 0.0);
        assertEquals(0.00, (Double) outputVariables.get("MethodNoExceptionCoverage").getValue(), 0.0);
        assertEquals(0.00, (Double) outputVariables.get("CBranchCoverage").getValue(), 0.0);

        assertEquals(0.65, (Double) outputVariables.get("Coverage").getValue(), 0.01);
        assertEquals(4, (Integer) outputVariables.get("Tests_Executed").getValue(), 0);
        assertEquals(84, (Integer) outputVariables.get("Total_Goals").getValue(), 0);
	}

}
