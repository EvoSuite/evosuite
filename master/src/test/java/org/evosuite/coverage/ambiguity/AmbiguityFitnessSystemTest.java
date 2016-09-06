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
package org.evosuite.coverage.ambiguity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.TestFactory;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.Compositional;
import com.examples.with.different.packagename.coverage.IndirectlyCoverableBranches;

@SuppressWarnings("unchecked")
public class AmbiguityFitnessSystemTest extends SystemTestBase {

	private static String MATRIX_CONTENT =
			"1 0 0 1 +\n" +
			"0 1 1 0 -\n" +
			"0 0 1 0 +\n";

	private void writeMatrix(String MATRIX_CONTENT) {
		String path = Properties.REPORT_DIR + File.separator;
		final File tmp = new File(path);
		tmp.mkdirs();

		Properties.COVERAGE_MATRIX_FILENAME = path + File.separator + Properties.TARGET_CLASS + ".matrix";

		try {
			final File matrix = new File(Properties.COVERAGE_MATRIX_FILENAME);
			matrix.createNewFile();

			FileWriter fw = new FileWriter(matrix.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(MATRIX_CONTENT);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Before
	public void prepare() {
		AmbiguityCoverageFactory.reset();
		try {
			FileUtils.deleteDirectory(new File("evosuite-report"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Properties.CRITERION = new Properties.Criterion[] {
			Properties.Criterion.AMBIGUITY
		};

		Properties.TEST_ARCHIVE = false;
		Properties.TEST_FACTORY = TestFactory.RANDOM;
		Properties.MINIMIZE = false;
		Properties.MINIMIZE_VALUES = false;
		Properties.INLINE = false;
		Properties.ASSERTIONS = false;
		Properties.USE_EXISTING_COVERAGE = false;
	}

	@Test
	public void testTransposedMatrix() {
		Properties.TARGET_CLASS = "tmpClass";
		this.writeMatrix(AmbiguityFitnessSystemTest.MATRIX_CONTENT);

		AmbiguityCoverageFactory.loadCoverage();
		List<StringBuilder> transposedMatrix = AmbiguityCoverageFactory.getTransposedMatrix();
		assertEquals(4, transposedMatrix.size());
		assertEquals(transposedMatrix.get(0).toString(), "100");
		assertEquals(transposedMatrix.get(1).toString(), "010");
		assertEquals(transposedMatrix.get(2).toString(), "011");
		assertEquals(transposedMatrix.get(3).toString(), "100");
	}

	@Test
	public void testTransposedMatrixWithoutPreviousCoverage() {
		Properties.TARGET_CLASS = "no_class";

		AmbiguityCoverageFactory.loadCoverage();
		List<StringBuilder> transposedMatrix = AmbiguityCoverageFactory.getTransposedMatrix();
		assertEquals(0, transposedMatrix.size());
	}

	@Test
	public void testMatrixAmbiguityScore() {
		Properties.TARGET_CLASS = "tmpClass";
		this.writeMatrix(AmbiguityFitnessSystemTest.MATRIX_CONTENT);

		AmbiguityCoverageFactory.loadCoverage();
		List<StringBuilder> matrix = AmbiguityCoverageFactory.getTransposedMatrix();
		assertEquals(4, matrix.size());
		assertEquals(0.25, AmbiguityCoverageFactory.getDefaultAmbiguity(matrix), 0.00);
	}

	@Test
	public void testZeroAmbiguityScore() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Compositional.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		String[] command = new String[] {
			"-class", targetClass,
			"-generateSuite"
		};

		List<List<TestGenerationResult>> result = (List<List<TestGenerationResult>>) evosuite.parseCommandLine(command);
		Assert.assertNotNull(result);

		List<?> goals = AmbiguityCoverageFactory.getGoals();
		assertEquals(12, goals.size());

		GeneticAlgorithm<?> ga = result.get(0).get(0).getGeneticAlgorithm();
		Assert.assertNotNull(ga);
		assertEquals(0.0, ga.getBestIndividual().getFitnessInstanceOf(AmbiguityCoverageSuiteFitness.class), 0.0);
	}

	@Test
	public void testZeroAmbiguityScoreWithPreviousCoverage() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Compositional.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		String previous_tmp_coverage =
			"1 1 1 1 1 1 1 1 1 1 1 +\n" +
			"1 1 1 1 0 0 0 0 0 0 0 -\n";
		this.writeMatrix(previous_tmp_coverage);
		Properties.USE_EXISTING_COVERAGE = true;

		String[] command = new String[] {
			"-class", targetClass,
			"-generateSuite"
		};

		List<List<TestGenerationResult>> result = (List<List<TestGenerationResult>>) evosuite.parseCommandLine(command);
		Assert.assertNotNull(result);

		List<?> goals = AmbiguityCoverageFactory.getGoals();
		assertEquals(12, goals.size());

		GeneticAlgorithm<?> ga = result.get(0).get(0).getGeneticAlgorithm();
		Assert.assertNotNull(ga);
		assertEquals(0.0, ga.getBestIndividual().getFitnessInstanceOf(AmbiguityCoverageSuiteFitness.class), 0.0);
	}

	@Test
	public void testNonZeroAmbiguityScore() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = IndirectlyCoverableBranches.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 35_000;

		String[] command = new String[] {
			"-class", targetClass,
			"-generateSuite"
		};

		List<List<TestGenerationResult>> result = (List<List<TestGenerationResult>>) evosuite.parseCommandLine(command);
		assertNotNull(result);

		List<?> goals = AmbiguityCoverageFactory.getGoals();
		assertEquals(12, goals.size());

		GeneticAlgorithm<?> ga = result.get(0).get(0).getGeneticAlgorithm();
		assertNotNull(ga);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		// goals of 'IndirectlyCoverableBranches': 22, 24, 25, 28, 29, 30, 31, 34, 35, 38, 39, 41
		//
		// minimum ambiguity:
		// {22}, {24}, {25}, {28,29,30,31}, {34,35}, {38,39,41}
		double ambiguity = 0.0; // {22}
		ambiguity += 0.0; // {24}
		ambiguity += 0.0; // {25}
		ambiguity += (4.0 / ((double) goals.size())) * (3.0 / 2.0); // {28,29,30,31}
		ambiguity += (2.0 / ((double) goals.size())) * (1.0 / 2.0); // {34,35}
		ambiguity += (3.0 / ((double) goals.size())) * (2.0 / 2.0); // {38,39,41}
		assertEquals(0.8333, ambiguity, 0.0001);
		//assertEquals(ambiguity * 1.0 / ((double) goals.size()), best.getFitnessInstanceOf(AmbiguityCoverageSuiteFitness.class), 0.001);
		assertEquals(FitnessFunction.normalize(ambiguity), best.getFitnessInstanceOf(AmbiguityCoverageSuiteFitness.class), 0.001);
	}
}
