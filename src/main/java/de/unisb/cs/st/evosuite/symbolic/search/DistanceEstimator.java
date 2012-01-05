package de.unisb.cs.st.evosuite.symbolic.search;
import gov.nasa.jpf.JPF;

import java.util.Collection;
import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.expr.Comparator;
import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringComparison;

/**
 * @author krusev
 *
 */
public abstract class DistanceEstimator {
	
	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.search.DistanceEstimator");

	/**
	 * This is still a dummy.
	 * 
	 * 
	 * 
	 * @param sc
	 * @return
	 */
	public static int getFitness(StringComparison sc) {
		
		long result = sc.execute();
		//log.warning("comparison: " + sc + " distance: " + result);
		//System.exit(0); 
		
		// Estimation should be done on MinValue
		
		return (int)result;
	}

	/**
	 * This is still a dummy.
	 * 
	 *  
	 * 
	 * @param constraints
	 * @return true if all but the last constraint (which is the target) are reachable
	 */
	public static boolean areReachable(Collection<Constraint<?>> constraints) {
		boolean result = true;

		//TODO forgot to scip the last one

		for (Constraint<?> c : constraints) {
			Expression<?> expr = c.getLeftOperand();
			if(expr instanceof StringComparison) {
				StringComparison sc = (StringComparison) expr;
				Comparator op = c.getComparator();
//				log.warning("condition: " + sc);
				long dis = sc.execute();
				if (op.equals(Comparator.NE)) {
					//we want to satisfy
					result = (dis >= 0);
				}
				if (op.equals(Comparator.EQ)) {
					//we DON'T want to satisfy
					result = (dis < 0);
				}
			}
		}
//		log.warning("are reachable says: " + result);
//		System.exit(0);
		return result;
	}
	
	
}
