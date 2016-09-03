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

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTestBase;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.SearchStatistics;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.BMICalculator;
import com.examples.with.different.packagename.TestBMICalculator;

public class CoverageAnalysisSystemTest extends SystemTestBase {

	private SearchStatistics aux(Criterion[] criterion) {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = BMICalculator.class.getCanonicalName();
		String testClass = TestBMICalculator.class.getCanonicalName();

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
	public void testLineCoverage() {
		SearchStatistics statistics = this.aux(new Properties.Criterion[] {
			Properties.Criterion.LINE
		});

		Map<String, OutputVariable<?>> variables = statistics.getOutputVariables();
		assertEquals(11, (Integer) variables.get("Total_Goals").getValue(), 0.0);
		assertEquals(11, (Integer) variables.get("Covered_Goals").getValue(), 0.0);
	}

	@Test
	public void testOnlyLineCoverage() {
		SearchStatistics statistics = this.aux(new Properties.Criterion[] {
			Properties.Criterion.ONLYLINE
		});

		Map<String, OutputVariable<?>> variables = statistics.getOutputVariables();
		assertEquals(11, (Integer) variables.get("Total_Goals").getValue(), 0.0);
		assertEquals(11, (Integer) variables.get("Covered_Goals").getValue(), 0.0);
	}

	@Test
	public void testBranchCoverage() {
		SearchStatistics statistics = this.aux(new Properties.Criterion[] {
			Properties.Criterion.BRANCH
		});

		Map<String, OutputVariable<?>> variables = statistics.getOutputVariables();
		assertEquals(9, (Integer) variables.get("Total_Goals").getValue(), 0.0);
		assertEquals(9, (Integer) variables.get("Covered_Goals").getValue(), 0.0);
	}

	@Test
	public void testCBranchCoverage() {
		SearchStatistics statistics = this.aux(new Properties.Criterion[] {
			Properties.Criterion.CBRANCH
		});

		Map<String, OutputVariable<?>> variables = statistics.getOutputVariables();
		assertEquals(9, (Integer) variables.get("Total_Goals").getValue(), 0.0);
		assertEquals(9, (Integer) variables.get("Covered_Goals").getValue(), 0.0);
	}

	@Test
	public void testOnlyBranchCoverage() {
		SearchStatistics statistics = this.aux(new Properties.Criterion[] {
			Properties.Criterion.ONLYBRANCH
		});

		Map<String, OutputVariable<?>> variables = statistics.getOutputVariables();
		assertEquals(8, (Integer) variables.get("Total_Goals").getValue(), 0.0);
		assertEquals(8, (Integer) variables.get("Covered_Goals").getValue(), 0.0);
	}

	@Test
	public void testRhoCoverage() {
		SearchStatistics statistics = this.aux(new Properties.Criterion[] {
			Properties.Criterion.RHO
		});

		Map<String, OutputVariable<?>> variables = statistics.getOutputVariables();
		assertEquals(11, (Integer) variables.get("Total_Goals").getValue(), 0.0);
		assertEquals(11, (Integer) variables.get("Covered_Goals").getValue(), 0.0);
	}

	@Test
	public void testAmbiguityCoverage() {
		SearchStatistics statistics = this.aux(new Properties.Criterion[] {
			Properties.Criterion.AMBIGUITY
		});

		Map<String, OutputVariable<?>> variables = statistics.getOutputVariables();
		assertEquals(11, (Integer) variables.get("Total_Goals").getValue(), 0.0);
		assertEquals(11, (Integer) variables.get("Covered_Goals").getValue(), 0.0);
	}
}
