package org.evosuite.ga.archive;

import org.evosuite.performance.comparator.EpsilonDominanceComparator;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

import java.util.Comparator;

/**
 * Implements the strategy that updates the archived according to the dominance of the performance indicators
 * @author Giovanni Grano
 */
@SuppressWarnings("Duplicates")
public class DominancePerformanceArchive<F extends TestFitnessFunction, T extends TestChromosome>
        extends CoverageArchive<F, T> {

    private static Comparator comparator = new EpsilonDominanceComparator();

    @Override
    public boolean isBetterThanCurrent(T currentSolution, T candidateSolution) {
        ExecutionResult currentSolutionExecution = currentSolution.getLastExecutionResult();
        ExecutionResult candidateSolutionExecution = candidateSolution.getLastExecutionResult();
        if (currentSolutionExecution != null
                && (currentSolutionExecution.hasTimeout() || currentSolutionExecution.hasTestException())) {
            // If the latest execution of the current solution in the archive has ran out of time or has
            // thrown any exception, a candidate could be considered better if its latest execution has
            // not ran out of time and has not thrown any exception, independent of whether it uses more
            // functional mocks or whether it is longer than the current solution.
            if (candidateSolutionExecution != null && !candidateSolutionExecution.hasTimeout()
                    && !candidateSolutionExecution.hasTestException()) {
                return true;
            }
        }

        // Check if solutions are using any functional mock or private access. A solution is considered
        // better than any other solution if does not use functional mock / private access at all, or if
        // it uses less of those functionalities.
        int penaltyCurrentSolution = this.calculatePenalty(currentSolution.getTestCase());
        int penaltyCandidateSolution = this.calculatePenalty(candidateSolution.getTestCase());

        if (penaltyCandidateSolution < penaltyCurrentSolution) {
            return true;
        } else if (penaltyCandidateSolution > penaltyCurrentSolution) {
            return false;
        }

        // only look at other properties (e.g., length) if penalty scores are the same
        assert penaltyCandidateSolution == penaltyCurrentSolution;

        /* --------------------------- usage of the comparator for performances ------------------------------- */

        if (comparator.compare(currentSolution, candidateSolution) == -1)
            return true;
        else
            return false;
    }
}
