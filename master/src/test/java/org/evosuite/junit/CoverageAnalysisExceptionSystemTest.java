package org.evosuite.junit;

import com.examples.with.different.packagename.ImplicitExplicitException;
import com.examples.with.different.packagename.ImplicitExplicitExceptionTest;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.SearchStatistics;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by gordon on 03/01/2016.
 */
public class CoverageAnalysisExceptionSystemTest extends SystemTestBase {

    private SearchStatistics aux(Properties.Criterion[] criterion) {

        EvoSuite evosuite = new EvoSuite();

        String targetClass = ImplicitExplicitException.class.getCanonicalName();
        String testClass = ImplicitExplicitExceptionTest.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = criterion;

        String[] command = new String[] {
                "-class", targetClass,
                "-Djunit=" + testClass,
                "-measureCoverage"
        };

        SearchStatistics statistics = (SearchStatistics) evosuite.parseCommandLine(command);
        Assert.assertNotNull(statistics);
        return statistics;
    }

    @Test
    public void testMethodCoverage() {
        SearchStatistics statistics = this.aux(new Properties.Criterion[] {
                Properties.Criterion.METHOD
        });

        Map<String, OutputVariable<?>> variables = statistics.getOutputVariables();
        assertEquals(6, (Integer) variables.get("Total_Goals").getValue(), 0.0);
        assertEquals(6, (Integer) variables.get("Covered_Goals").getValue(), 0.0);
    }

    @Test
    public void testMethodNoExceptionCoverage() {
        SearchStatistics statistics = this.aux(new Properties.Criterion[] {
                Properties.Criterion.METHODNOEXCEPTION
        });

        Map<String, OutputVariable<?>> variables = statistics.getOutputVariables();
        assertEquals(6, (Integer) variables.get("Total_Goals").getValue(), 0.0);
        assertEquals(1, (Integer) variables.get("Covered_Goals").getValue(), 0.0);
    }

    @Test
    public void testExceptionCoverage() {
        SearchStatistics statistics = this.aux(new Properties.Criterion[] {
                Properties.Criterion.EXCEPTION
        });

        Map<String, OutputVariable<?>> variables = statistics.getOutputVariables();
        assertEquals(5, (Integer) variables.get("Total_Goals").getValue(), 0.0);
        assertEquals(5, (Integer) variables.get("Covered_Goals").getValue(), 0.0);
    }

}
