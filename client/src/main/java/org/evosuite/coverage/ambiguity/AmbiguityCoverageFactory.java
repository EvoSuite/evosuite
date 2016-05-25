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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.coverage.line.LineCoverageTestFitness;
import org.evosuite.instrumentation.LinePool;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author José Campos
 */
public class AmbiguityCoverageFactory extends
	AbstractFitnessFactory<LineCoverageTestFitness> implements Serializable {

	private static final long serialVersionUID = 1424282176155102252L;

	private static final Logger logger = LoggerFactory.getLogger(AmbiguityCoverageFactory.class);

	/**
	 * 
	 */
	private static List<LineCoverageTestFitness> goals = new ArrayList<LineCoverageTestFitness>();

	/**
	 * 
	 */
	private static List<StringBuilder> transposedMatrix = new ArrayList<StringBuilder>();

	/**
	 * 
	 */
	private static double max_ambiguity_score = Double.MAX_VALUE;

	/**
	 * Read the coverage of a test suite from a file
	 */
	protected static void loadCoverage() {

		if (!new File(Properties.COVERAGE_MATRIX_FILENAME).exists()) {
			return ;
		}

		BufferedReader br = null;

		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(Properties.COVERAGE_MATRIX_FILENAME));

			List<StringBuilder> matrix = new ArrayList<StringBuilder>();
			while ((sCurrentLine = br.readLine()) != null) {
				sCurrentLine = sCurrentLine.replace(" ", "");
				// we do not want to consider test result
				sCurrentLine = sCurrentLine.substring(0, sCurrentLine.length() - 1);
				matrix.add(new StringBuilder(sCurrentLine));
			}

			transposedMatrix = tranposeMatrix(matrix);
			//double ag = AmbiguityCoverageFactory.getDefaultAmbiguity(transposedMatrix) * 1.0 / ((double) goals.size());
			double ag = TestFitnessFunction.normalize(AmbiguityCoverageFactory.getDefaultAmbiguity(transposedMatrix));
			logger.info("AmbiguityScore of an existing test suite: " + ag);

			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.AmbiguityScore_T0, ag);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Size_T0, matrix.size());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (br != null) {
					br.close();
				}
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 
	 */
	@Override
	public List<LineCoverageTestFitness> getCoverageGoals() {
		return getGoals();
	}

	/**
	 * 
	 * @return
	 */
	public static List<LineCoverageTestFitness> getGoals() {

		if (!goals.isEmpty()) {
			return goals;
		}

		for(String className : LinePool.getKnownClasses()) {
			Set<Integer> lines = LinePool.getLines(className);
			for (Integer line : lines) {
				logger.info("Adding goal for method " + className + ". Line " + line + ".");
				goals.add(new LineCoverageTestFitness(className, Properties.TARGET_METHOD, line));
			}
		}
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, goals.size());

		max_ambiguity_score = (1.0) // goals.size() / goals.size()
								* ( (((double) goals.size()) - 1.0) / 2.0 );

		if (Properties.USE_EXISTING_COVERAGE) {
			// extremely important: before loading any previous coverage (i.e., from a coverage
			// matrix) goals need to be sorted. otherwise any previous coverage won't match!
			Collections.sort(goals, new Comparator<LineCoverageTestFitness>() {
				@Override
				public int compare(LineCoverageTestFitness l1, LineCoverageTestFitness l2) {
					return Integer.compare(l1.getLine(), l2.getLine());
				}
			});
			loadCoverage();
		} else {
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.AmbiguityScore_T0, 1.0);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Size_T0, 0);
		}

		return goals;
	}

	/**
	 * 
	 * @return
	 */
	public static List<StringBuilder> getTransposedMatrix() {
		return transposedMatrix;
	}

	/**
	 * 
	 * @param matrix
	 * @return
	 */
	private static List<StringBuilder> tranposeMatrix(List<StringBuilder> matrix) {

		int number_of_components = matrix.get(0).length();
		List<StringBuilder> new_matrix = new ArrayList<StringBuilder>();

		for (int c_i = 0; c_i < number_of_components; c_i++) {
			StringBuilder str = new StringBuilder();
			for (StringBuilder t_i : matrix) {
				str.append(t_i.charAt(c_i));
			}

			new_matrix.add(str);
		}

		return new_matrix;
	}

	/**
	 * 
	 * @return
	 */
	public static double getMaxAmbiguityScore() {
		return max_ambiguity_score;
	}

	/**
	 * 
	 * @param matrix transposed matrix
	 * @return
	 */
	protected static double getDefaultAmbiguity(List<StringBuilder> matrix) {

		int number_of_components = matrix.size();
		Map<String, Integer> groups = new HashMap<String, Integer>();

		for (StringBuilder s : matrix) {
			if (!groups.containsKey(s.toString())) {
				groups.put(s.toString(), 1); // in the beginning they are ambiguity, so they belong to the same group '1'
			} else {
				groups.put(s.toString(), groups.get(s.toString()) + 1);
			}
		}

		return getAmbiguity(number_of_components, groups);
	}
	
	/**
	 * 
	 * @param matrix transposed matrix
	 * @return
	 */
	public static double getAmbiguity(int number_of_components, Map<String, Integer> groups) {

		double fit = 0.0;
		for (String s : groups.keySet()) {
			double cardinality = groups.get(s);
			if (cardinality == 1.0) {
				continue ;
			}

			fit += (cardinality / ((double) number_of_components)) * ((cardinality - 1.0) / 2.0);
		}

		return fit;
	}

	// only for testing
	protected static void reset() {
		goals.clear();
		transposedMatrix.clear();
	}
}
