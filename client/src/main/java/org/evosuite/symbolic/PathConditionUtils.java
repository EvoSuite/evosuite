package org.evosuite.symbolic;

import org.evosuite.symbolic.expr.Constraint;

import java.util.Collection;
import java.util.Set;

public class PathConditionUtils {
    /**
	   * Returns true if the constraints in the query are a subset of any of the constraints in the set
	   * of queries
	   *
	   * @param query
	   * @param queries
	   * @return
	   */
    public static boolean isConstraintSetSubSetOf(Set<Constraint<?>> query,
                                                  Collection<Set<Constraint<?>>> queries) {

        for (Set<Constraint<?>> pathCondition : queries) {
		  if (pathCondition.containsAll(query)) {
			return true;
		  }
		}

        return false;
    }

}
