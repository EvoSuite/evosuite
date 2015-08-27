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
package org.evosuite.coverage.rho;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.testsuite.AbstractFitnessFactory;

/**
 * <p>
 * RhoCoverageFactory class.
 * </p>
 * 
 * @author José Campos
 */
public class RhoCoverageFactory extends
		AbstractFitnessFactory<RhoCoverageTestFitness> {

	/**
	 * 
	 */
	private static boolean called = false;

	/**
	 * 
	 */
	private static List<RhoCoverageTestFitness> goals = new ArrayList<RhoCoverageTestFitness>();

	/**
	 * 
	 */
	private static int number_of_ones = 0;

	/**
	 * 
	 */
	private static int number_of_test_cases = 0;

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
						goals.add(new RhoCoverageTestFitness(ins));
			}
		}
		/*LoggingUtils.getEvoLogger().info("* Total number of coverage goals using Rho Fitness Function: "
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
		if (lineNumbers.add(ins.getLineNumber()) == false) // this line number already exists?
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

			String[] split;
			while ((sCurrentLine = br.readLine()) != null) {
				split = sCurrentLine.split(" ");
				for (int i = 0; i < split.length - 1; i++) { // - 1, because we do not want to consider test result
					if (split[i].compareTo("1") == 0)
						number_of_ones++;
				}

				number_of_test_cases++;
			}

			/*double rho = ((double) number_of_ones) / ((double) number_of_test_cases) / ((double) getNumberComponents());
			rho = Math.abs(0.5 - rho);
			LoggingUtils.getEvoLogger().info("* Original fitness (RHO): " + rho);*/
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
	public List<RhoCoverageTestFitness> getCoverageGoals() {
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
	public static List<RhoCoverageTestFitness> retrieveCoverageGoals() {
		if (!called)
			computeGoals();
		return goals;
	}

	/**
	 * 
	 * @return
	 */
	public static int getNumberOnes() {
		return number_of_ones;
	}

	/**
	 * 
	 * @return
	 */
	public static int getNumberTestCases() {
		return number_of_test_cases;
	}
}
