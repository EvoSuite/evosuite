/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.path;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace.MethodCall;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * @author Gordon Fraser
 * 
 */
public class PrimePathTestFitness extends TestFitnessFunction {

	private final PrimePath path;

	private final String className;

	private final String methodName;

	private final int length;

	public PrimePathTestFitness(PrimePath path, String className, String methodName) {
		this.path = path;
		this.className = className;
		this.methodName = methodName;
		length = path.branches.size();
	}

	private static int getNextBranch(PrimePath path, int position) {
		for (int i = position + 1; i < path.getSize(); i++) {
			if (path.get(i).isBranch())
				return i;
		}
		return path.getSize();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestFitnessFunction#getFitness(de.unisb.cs.st.evosuite.testcase.TestChromosome, de.unisb.cs.st.evosuite.testcase.ExecutionResult)
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		double minMatch = length;
		for (MethodCall call : result.getTrace().finished_calls) {
			if (call.className.equals(className) && call.methodName.equals(methodName)) {
				double matches = 0.0;
				for (Integer i : call.branchTrace) {
					logger.debug(" -> " + i);
				}
				logger.debug("-------");
				logger.debug(path.toString());
				for (int i = 0; i < path.getSize(); i++) {
					if (path.get(i).isBranch())
						logger.debug(" -> " + path.get(i).getInstructionId());
				}
				logger.debug("Length: " + length);
				int pos_path = 0;
				int pos_trace = 0;
				while (pos_path < path.branches.size()) {
					if (pos_trace >= call.branchTrace.size()) {
						logger.debug("End of trace?"
						        + ": "
						        + (normalize(call.trueDistanceTrace.get(pos_trace - 1)) + normalize(call.falseDistanceTrace.get(pos_trace - 1))));

						matches += 1 - (normalize(call.trueDistanceTrace.get(pos_trace - 1)) + normalize(call.falseDistanceTrace.get(pos_trace - 1)));
						break;
					} else if (path.branches.get(pos_path).vertex.getInstructionId() == call.branchTrace.get(pos_trace)) {
						logger.debug("Found branch match: "
						        + path.branches.get(pos_path).vertex.getInstructionId());
						matches++;
						if (path.branches.get(pos_path).value == true) {
							if (call.trueDistanceTrace.get(pos_trace) == 0.0) {
								logger.debug("Truth value match");
								pos_path++;
								pos_trace++;
							} else {
								logger.debug("Truth value mismatch: "
								        + (normalize(call.trueDistanceTrace.get(pos_trace))));
								matches += 1 - (normalize(call.trueDistanceTrace.get(pos_trace)));
								break;
							}
						} else {
							if (call.falseDistanceTrace.get(pos_trace) == 0.0) {
								logger.debug("Truth value match");
								pos_path++;
								pos_trace++;
							} else {
								logger.debug("Truth value mismatch: "
								        + (normalize(call.falseDistanceTrace.get(pos_trace))));
								matches += 1 - (normalize(call.falseDistanceTrace.get(pos_trace)));
								break;
							}
						}
					} else {
						logger.debug("Found mismatch at "
						        + pos_path
						        + " / "
						        + path.getSize()
						        + ": "
						        + (normalize(call.trueDistanceTrace.get(pos_trace - 1)) + normalize(call.falseDistanceTrace.get(pos_trace - 1))));
						matches += 1 - (normalize(call.trueDistanceTrace.get(pos_trace - 1)) + normalize(call.falseDistanceTrace.get(pos_trace - 1)));
						break;
					}
				}
				matches = length - matches;
				logger.debug("Current fitness: " + matches);
				minMatch = Math.min(minMatch, matches);
				logger.debug("Current best fitness: " + minMatch);
			}
		}
		logger.debug("Final Fitness: " + minMatch);
		return minMatch;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#updateIndividual(de.unisb.cs.st.evosuite.ga.Chromosome, double)
	 */
	@Override
	protected void updateIndividual(Chromosome individual, double fitness) {
		individual.setFitness(fitness);
	}

	@Override
	public String toString() {
		return path.toString();
	}
}
