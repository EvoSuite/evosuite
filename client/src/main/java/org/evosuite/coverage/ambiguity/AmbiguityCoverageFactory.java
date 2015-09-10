/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.ambiguity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.evosuite.utils.MD5;

/**
 * <p>
 * AmbiguityFactory class.
 * </p>
 * 
 * @author José Campos
 */
public class AmbiguityCoverageFactory extends
		AbstractFitnessFactory<AmbiguityCoverageTestFitness> {

	/**
	* 
	*/
	private static boolean called = false;
	
	/**
	* 
	*/
	private static List<AmbiguityCoverageTestFitness> goals = new ArrayList<AmbiguityCoverageTestFitness>();

	/**
	 * 
	 */
	private static List<StringBuilder> transposedMatrix = new ArrayList<StringBuilder>();

	/**
	 * 
	 */
	private static LinkedHashSet<Integer> lineNumbers = new LinkedHashSet<Integer>();

	/**
	 * 
	 */
	private static void computeGoals() {
		if (called)
			return ;

		String targetClass = Properties.TARGET_CLASS;

		final MethodNameMatcher matcher = new MethodNameMatcher();
        //for (String className : BytecodeInstructionPool.getInstance(TestGenerationContext.getClassLoader()).knownClasses()) {
        for (String className : BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownClasses()) {
            if (!(targetClass.equals("") || className.endsWith(targetClass)))
                continue ;
            //for (String methodName : BytecodeInstructionPool.getInstance(TestGenerationContext.getClassLoader()).knownMethods(className)) {
            for (String methodName : BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownMethods(className)) {
                if (!matcher.methodMatches(methodName))
                    continue ;
                /*for (BytecodeInstruction ins : BytecodeInstructionPool.getInstance(TestGenerationContext.getClassLoader()).getInstructionsIn(className,
                                                                                                                                                methodName))*/
                for (BytecodeInstruction ins : BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getInstructionsIn(className,
                                                                                                                                                                 methodName))
                    if (isUsable(ins))
                        goals.add(new AmbiguityCoverageTestFitness(ins));
            }
        }
		/*LoggingUtils.getEvoLogger().info("* Total number of coverage goals using Ambiguity Fitness Function: "
											+ goals.size());*/

		called = true;
		loadCoverage();
	}

	/**
	 * 
	 * @param ins
	 * @return
	 */
	private static boolean isUsable(BytecodeInstruction ins) {
		if (lineNumbers.add(ins.getLineNumber()) == false) // the line number already exists?
			return false;
		return ins.isLineNumber();
	}

	/**
	 * Read the coverage of a test suite from a file
	 */
	protected static void loadCoverage() {

		BufferedReader br = null;

		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(Properties.REPORT_DIR + File.separator + "data" + File.separator + Properties.TARGET_CLASS + ".matrix"));

			List<StringBuilder> matrix = new ArrayList<StringBuilder>();
			while ((sCurrentLine = br.readLine()) != null) {
				sCurrentLine = sCurrentLine.replace(" ", "");
				sCurrentLine = sCurrentLine.substring(0, sCurrentLine.length() - 1); // we do not want to consider test result
				matrix.add(new StringBuilder(sCurrentLine));
			}

			transposedMatrix = tranposeMatrix(matrix);
			//double ambiguity = getAmbiguity(transposedMatrix);
			//LoggingUtils.getEvoLogger().info("* Original fitness (AMBIGUITY): " + FitnessFunction.normalize(ambiguity));
		}
		catch (IOException e) {
			// the coverage matrix file does not exist, ok no problem... we will generate new test cases from scratch
		}
		finally {
			try {
				if (br != null)
					br.close();
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
	public List<AmbiguityCoverageTestFitness> getCoverageGoals() {
		if (!called)
			computeGoals();
		return goals;
	}

	/**
	 * <p>
	 * retrieveCoverageGoals
	 * </p>
	 * 
	 * @return a {@link java.util.List} object.
	 */
	public static List<AmbiguityCoverageTestFitness> retrieveCoverageGoals() {
		if (!called)
			computeGoals();
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
	 * @param matrix transposed matrix
	 * @return
	 */
	public static double getAmbiguity(List<StringBuilder> matrix) {

	    int number_of_components = matrix.size();
	    LinkedHashMap<String, Integer> groups = new LinkedHashMap<String, Integer>();

		for (StringBuilder s : matrix) {
			String md5 = MD5.hash(s.toString());
			if (!groups.containsKey(md5))
				groups.put(md5, 1); // in the beginning they are ambiguity, so they belong to the same group '1'
			else
				groups.put(md5, groups.get(md5) + 1);
		}

		double fit = 0.0;
		for (String s : groups.keySet()) {
			double cardinality = groups.get(s);
			if (cardinality == 1.0)
				continue ;

			fit += (cardinality / ((double) number_of_components)) * ((cardinality - 1.0) / 2.0);
		}

		return fit;
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
			for (StringBuilder t_i : matrix)
				str.append(t_i.charAt(c_i));

			new_matrix.add(str);
		}

		return new_matrix;
	}
}
