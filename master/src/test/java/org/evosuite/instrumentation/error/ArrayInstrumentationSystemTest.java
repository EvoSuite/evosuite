/**
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
package org.evosuite.instrumentation.error;

import com.examples.with.different.packagename.errorbranch.ArrayAccess;
import com.examples.with.different.packagename.errorbranch.ArrayCreation;
import com.opencsv.CSVReader;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ArrayInstrumentationSystemTest extends AbstractErrorBranchTest {

	@Test
	public void testArrayAccessWithoutErrorBranches() {
		Properties.ERROR_BRANCHES = false;
		Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.ARRAY};
		checkErrorBranches(ArrayAccess.class, 2, 0, 2, 0);
	}

	@Test
	public void testArrayAccessWithErrorBranches() {
		Properties.ERROR_BRANCHES = true;
		Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.ARRAY};
		checkErrorBranches(ArrayAccess.class, 2, 6, 2, 5);
	}

	@Test
	public void testArrayCreationWithoutErrorBranches() {
		Properties.ERROR_BRANCHES = false;
		Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.ARRAY};
		checkErrorBranches(ArrayCreation.class, 2, 0, 2, 0);
	}

	@Test
	public void testArrayCreationWithErrorBranches() {
		Properties.ERROR_BRANCHES = true;
		Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.ARRAY};
		checkErrorBranches(ArrayCreation.class, 2, 2, 2, 2);
	}
}
