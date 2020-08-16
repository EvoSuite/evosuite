/*
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

package org.evosuite.coverage.path;

import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.MethodCall;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;

/**
 * <p>
 * PrimePathTestFitness class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class PrimePathTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = 704930253578667965L;

	private final PrimePath path;

	private final String className;

	private final String methodName;

	private final int length;

	/**
	 * <p>
	 * Constructor for PrimePathTestFitness.
	 * </p>
	 * 
	 * @param path
	 *            a {@link org.evosuite.coverage.path.PrimePath} object.
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 */
	public PrimePathTestFitness(PrimePath path, String className, String methodName) {
		this.path = path;
		this.className = className;
		this.methodName = methodName;
		length = path.branches.size();
	}

	@SuppressWarnings("unused")
	private static int getNextBranch(PrimePath path, int position) {
		for (int i = position + 1; i < path.getSize(); i++) {
			if (path.get(i).isBranch())
				return i;
		}
		return path.getSize();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getFitness(org.evosuite.testcase.TestChromosome, org.evosuite.testcase.ExecutionResult)
	 */
	/** {@inheritDoc} */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		double minMatch = length;
		for (MethodCall call : result.getTrace().getMethodCalls()) {
			if (call.className.equals(className) && call.methodName.equals(methodName)) {
				double matches = 0.0;
				for (Integer i : call.branchTrace) {
					logger.debug(" |-> " + i);
				}
				logger.debug("-------");
				logger.debug(path.toString());
				for (int i = 0; i < path.branches.size(); i++) {
					logger.debug(" -> " + path.branches.get(i).branch + " "
					        + path.branches.get(i).value);
				}
				logger.debug("Length: " + length);
				int pos_path = 0;
				int pos_trace = 0;
				while (pos_path < path.branches.size()) {
					logger.debug("Current matches: " + matches);
					if (pos_trace >= call.branchTrace.size()) {
						logger.debug("End of trace?"
						        + ": "
						        + (normalize(call.trueDistanceTrace.get(pos_trace - 1)) + normalize(call.falseDistanceTrace.get(pos_trace - 1))));

						matches += 1 - (normalize(call.trueDistanceTrace.get(pos_trace - 1)) + normalize(call.falseDistanceTrace.get(pos_trace - 1)));
						break;
					} else if (path.branches.get(pos_path).branch.getActualBranchId() == call.branchTrace.get(pos_trace)) {
						logger.debug("Found branch match: "
						        + path.branches.get(pos_path).branch);
						matches++;
						if (path.branches.get(pos_path).value == true) {
							if (call.trueDistanceTrace.get(pos_trace) == 0.0) {
								logger.debug("[True] Truth value match");
								pos_path++;
								pos_trace++;
							} else {
								logger.debug("[True] Truth value mismatch: "
								        + (normalize(call.trueDistanceTrace.get(pos_trace)))
								        + " / "
								        + (normalize(call.falseDistanceTrace.get(pos_trace))));
								matches -= (normalize(call.trueDistanceTrace.get(pos_trace)));
								break;
							}
						} else {
							if (call.falseDistanceTrace.get(pos_trace) == 0.0) {
								logger.debug("[False] Truth value match");
								pos_path++;
								pos_trace++;
							} else {
								logger.debug("[False] Truth value mismatch: "
								        + (normalize(call.falseDistanceTrace.get(pos_trace)))
								        + " / "
								        + (normalize(call.trueDistanceTrace.get(pos_trace))));
								matches -= (normalize(call.falseDistanceTrace.get(pos_trace)));
								break;
							}
						}
					} else {
						logger.warn("Size of trace: " + call.trueDistanceTrace.size());
						logger.warn("Position: " + pos_trace);
						logger.debug("Found mismatch at "
						        + pos_path
						        + " / "
						        + path.getSize()
						        + ": "
						        + (normalize(call.trueDistanceTrace.get(pos_trace - 1)) + normalize(call.falseDistanceTrace.get(pos_trace - 1))));
						matches += 1 - (normalize(call.trueDistanceTrace.get(pos_trace - 1)) + normalize(call.falseDistanceTrace.get(pos_trace - 1)));
						// -1?
						break;
					}
				}
				matches = length - matches;
				logger.debug("Current fitness: " + matches);
				minMatch = Math.min(minMatch, matches);
				logger.debug("Current best fitness: " + minMatch);
				assert (minMatch >= 0) : "Fitness is " + minMatch;
			}
		}
		logger.debug("Final Fitness: " + minMatch);
		return minMatch;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return path.toString();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
	 */
	@Override
	public int compareTo(TestFitnessFunction other) {
		if (other instanceof PrimePathTestFitness) {
			// TODO
			return length - ((PrimePathTestFitness) other).length;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
	 */
	@Override
	public String getTargetClass() {
		return path.className;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
	 */
	@Override
	public String getTargetMethod() {
		return path.methodName;
	}
}
