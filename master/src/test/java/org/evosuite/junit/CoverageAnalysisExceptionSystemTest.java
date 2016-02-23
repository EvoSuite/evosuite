/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
