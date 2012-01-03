package de.unisb.cs.st.evosuite.symbolic.search;
import gov.nasa.jpf.JPF;

import java.util.Collection;
import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
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
	public static int getDistance(StringComparison sc) {
		// TODO Auto-generated method stub
		// Estimation should be done on MaxValue of the vars 
		// 					on MinValue if MaxValue == null
		return -(int)(Math.random()*10);
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
		// TODO Auto-generated method stub
		// Estimation should be done on MaxValue of the vars 
		// 					on MinValue if MaxValue == null
		return true;
	}
	
	
}
