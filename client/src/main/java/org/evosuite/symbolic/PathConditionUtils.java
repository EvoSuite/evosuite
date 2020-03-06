package org.evosuite.symbolic;

import org.evosuite.symbolic.expr.Constraint;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class PathConditionUtils {
    /**
	   * Returns true if the constraints in the query are a subset of any of the constraints in the set
	   * of queries.
	   *
	   * @param query
	   * @param queries
	   * @return
	   */
    public static boolean isConstraintSetSubSetOf(
    	Set<Constraint<?>> query,
		Collection<Set<Constraint<?>>> queries)
	{
        for (Set<Constraint<?>> pathCondition : queries) {
		  if (pathCondition.containsAll(query)) {
			return true;
		  }
		}

        return false;
    }

    /**
	 * Checks whether the current path condition explored has diverged from it's supposed path.
	 * We simply check that the original path condition from which the new test was created
	 * is a prefix of the actual path being explored.
	 *
	 * TODO: see how to cite this paper properly.
	 * Idea taken from: An empirical investigation into path divergences for
	 * 		concolic execution using CREST,
	 *  	Ting Chen, Xiaodong Lin, Jin Huang, Abel Bacchus and Xiaosong Zhang
	 *
	 *  TODO: In the future we can support explaining which condition diverged.
	 *  		And Maybe talk about what happened, it's doesn't seem to be trivial to
	 *  	    check where the divergence generated but there may be an aprox. technique that
	 *  	    may give us some information about it.
	 *
	 * @param expectedPrefixPathCondition
	 * @param newPathCondition
	 * @return
	 */
	public static boolean hasPathConditionDiverged(PathCondition expectedPrefixPathCondition, PathCondition newPathCondition) {
		List<BranchCondition> expectedPrefixBranchConditions = expectedPrefixPathCondition.getBranchConditions();
		List<BranchCondition> newBranchConditions = newPathCondition.getBranchConditions();

		for (int currentBranchConditionIndex = 0; currentBranchConditionIndex < expectedPrefixBranchConditions.size(); ++currentBranchConditionIndex) {
			BranchCondition expectedPrefixBranchCondition = expectedPrefixBranchConditions.get(currentBranchConditionIndex);
			BranchCondition newBranchCondition = newBranchConditions.get(currentBranchConditionIndex);

			// if the expectedPrefix path is not a prefix of the current one, there's a divergence
			if (!expectedPrefixBranchCondition.equals(newBranchCondition)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Calculates the path divergece vs total paths ratio.
	 *
	 * @param pathDivergeAmount
	 * @param totalPathsAmount
	 * @return
	 */
	public static double calculatePathDivergenceRatio(int pathDivergeAmount, int totalPathsAmount) {
		double divergenceAmount = pathDivergeAmount;
		double pathsAmount = totalPathsAmount;

		return pathDivergeAmount / totalPathsAmount;
	}

}
