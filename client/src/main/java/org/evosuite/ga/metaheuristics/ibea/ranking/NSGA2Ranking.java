package org.evosuite.ga.metaheuristics.ibea.ranking;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.operators.ranking.FastNonDominatedSorting;

import java.util.List;

/**
 * This class implements some facilities for ranking solutions.
 * Given a <code>SolutionSet</code> object, their solutions are ranked
 * according to scheme proposed in NSGA-II; as a result, a set of subsets
 * are obtained. The subsets are numbered starting from 0 (in NSGA-II, the
 * numbering starts from 1); thus, subset 0 contains the non-dominated
 * solutions, subset 1 contains the non-dominated solutions after removing those
 * belonging to subset 0, and so on.
 */
public class NSGA2Ranking<T extends Chromosome> extends FastNonDominatedSorting<T> {

    public void computeRankingAssignment(List<T> solutions) {
        super.computeRankingAssignment(solutions, null);
    }
}