/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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

	private static boolean hasException = false;

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
		return timeouts >= MAX_TIMEOUTS || hasException;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#reset()
	 */
	@Override
	public void reset() {
		timeouts = 0;
		hasException = false;
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

	// TODO: Still need a good way to call this
	public static void raisedException(Mutation mutation) {
		hasException = true;
		disabled.add(mutation);
	}

	@Override
	public void forceCurrentValue(long value) {
		timeouts = (int) value;
	}

}
