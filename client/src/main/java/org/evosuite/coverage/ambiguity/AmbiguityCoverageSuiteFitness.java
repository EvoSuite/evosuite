package org.evosuite.coverage.ambiguity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.coverage.line.LineCoverageTestFitness;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * 
 * @author José Campos
 */
public class AmbiguityCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -2721073655092419390L;

	/**
	 * 
	 */
	private final Set<Integer> goals;

	/**
	 * 
	 */
	public AmbiguityCoverageSuiteFitness() {

		this.goals = new HashSet<Integer>();
		for (LineCoverageTestFitness goal : AmbiguityCoverageFactory.getGoals()) {
			this.goals.add(goal.getLine());
		}
	}

	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {

		List<StringBuilder> transposedMatrix = new ArrayList<StringBuilder>(AmbiguityCoverageFactory.getTransposedMatrix());
		List<Set<Integer>> coveredLines = new ArrayList<Set<Integer>>();

		// Execute test cases and collect the covered lines
		List<ExecutionResult> results = runTestSuite(suite);
		for (ExecutionResult result : results) {
			coveredLines.add(result.getTrace().getCoveredLines());
		}

		Map<String, Integer> groups = new HashMap<String, Integer>();
		int g_i = 0;

		for (Integer goal : this.goals) {
			StringBuffer str = null;

			if (transposedMatrix.size() > g_i) {
				str = new StringBuffer(transposedMatrix.get(g_i).length() + coveredLines.size());
				str.append(transposedMatrix.get(g_i));
			} else {
				str = new StringBuffer(coveredLines.size());
			}

			for (Set<Integer> covered : coveredLines) {
				str.append( covered.contains(goal) ? "1" : "0" );
			}

			if (!groups.containsKey(str.toString())) {
				groups.put(str.toString(), 1); // in the beginning they are ambiguity, so they belong to the same group '1'
			} else {
				groups.put(str.toString(), groups.get(str.toString()) + 1);
			}

			g_i++;
		}

		//double fitness = AmbiguityCoverageFactory.getAmbiguity(this.goals.size(), groups) * 1.0 / AmbiguityCoverageFactory.getMaxAmbiguityScore();
		double fitness = TestFitnessFunction.normalize(AmbiguityCoverageFactory.getAmbiguity(this.goals.size(), groups));
		updateIndividual(this, suite, fitness);

		return fitness;
	}
}
