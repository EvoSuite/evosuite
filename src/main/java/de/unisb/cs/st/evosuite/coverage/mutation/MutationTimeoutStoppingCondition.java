/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.mutation;

import java.util.HashSet;
import java.util.Set;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingConditionImpl;

/**
 * @author Gordon Fraser
 * 
 */
public class MutationTimeoutStoppingCondition extends StoppingConditionImpl {
	
	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MutationTimeoutStoppingCondition.class);

	//public static Map<Mutation, Integer> timeouts = new HashMap<Mutation, Integer>();

	private static final long serialVersionUID = -7347443938884126325L;

	private static int timeouts = 0;

	private static int MAX_TIMEOUTS = Properties.MUTATION_TIMEOUTS;

	private static Set<Mutation> disabled = new HashSet<Mutation>();

	public static boolean isDisabled(Mutation mutation) {
		return disabled.contains(mutation);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#getCurrentValue()
	 */
	@Override
	public long getCurrentValue() {
		return timeouts;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#isFinished()
	 */
	@Override
	public boolean isFinished() {
		logger.debug("Number of timeouts registered for this mutant: " + timeouts + "/"
		        + MAX_TIMEOUTS);

		if (timeouts >= MAX_TIMEOUTS) {
			System.out.println("Mutation timed out, stopping search");
		}
		return timeouts >= MAX_TIMEOUTS;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#reset()
	 */
	@Override
	public void reset() {
		timeouts = 0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#setLimit(int)
	 */
	@Override
	public void setLimit(long limit) {
		MAX_TIMEOUTS = (int) limit;
	}

	@Override
	public long getLimit() {
		return MAX_TIMEOUTS;
	}

	public static void timeOut(Mutation mutation) {
		timeouts++;
		if (timeouts >= MAX_TIMEOUTS)
			disabled.add(mutation);
	}

	@Override
	public void forceCurrentValue(long value) {
		timeouts = (int) value;
	}

}
