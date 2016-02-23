/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.coverage.mutation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.Strategy;
import org.evosuite.ga.stoppingconditions.StoppingConditionImpl;


/**
 * <p>MutationTimeoutStoppingCondition class.</p>
 *
 * @author Gordon Fraser
 */
public class MutationTimeoutStoppingCondition extends StoppingConditionImpl {

	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MutationTimeoutStoppingCondition.class);

	public static Map<Mutation, Integer> timeouts = new HashMap<Mutation, Integer>();

	private static final long serialVersionUID = -7347443938884126325L;

	private static int timeout = 0;

	private static boolean hasException = false;

	private static Set<Mutation> exceptions = new HashSet<Mutation>();
	
	private static int MAX_TIMEOUTS = Properties.MUTATION_TIMEOUTS;

	private static Set<Mutation> disabled = new HashSet<Mutation>();

	/**
	 * <p>isDisabled</p>
	 *
	 * @param mutation a {@link org.evosuite.coverage.mutation.Mutation} object.
	 * @return a boolean.
	 */
	public static boolean isDisabled(Mutation mutation) {
		return disabled.contains(mutation);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#getCurrentValue()
	 */
	/** {@inheritDoc} */
	@Override
	public long getCurrentValue() {
		if (Properties.STRATEGY != Strategy.ONEBRANCH){
			return 0;
		}else{
			return timeout;
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#isFinished()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isFinished() {
		logger.debug("Number of timeouts registered for this mutant: " + timeouts + "/"
		        + MAX_TIMEOUTS);
		
		if (Properties.STRATEGY != Strategy.ONEBRANCH){
			return false;
		}
		
		if (timeout >= MAX_TIMEOUTS) {
			logger.debug("Mutation timed out, stopping search");
		}
		return timeout >= MAX_TIMEOUTS || hasException;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#reset()
	 */
	/** {@inheritDoc} */
	@Override
	public void reset() {
		if (Properties.STRATEGY != Strategy.ONEBRANCH){
			timeouts = new HashMap<Mutation, Integer>();
			exceptions = new HashSet<Mutation>();
		}else{
			timeout = 0;
			hasException = false;
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#setLimit(int)
	 */
	/** {@inheritDoc} */
	@Override
	public void setLimit(long limit) {
		MAX_TIMEOUTS = (int) limit;
	}

	/** {@inheritDoc} */
	@Override
	public long getLimit() {
		return MAX_TIMEOUTS;
	}

	/**
	 * <p>timeOut</p>
	 *
	 * @param mutation a {@link org.evosuite.coverage.mutation.Mutation} object.
	 */
	public static void timeOut(Mutation mutation) {
		
		if (Properties.STRATEGY != Strategy.ONEBRANCH){
			int t = 1;
			if (timeouts.containsKey(mutation)){
				t = timeouts.get(mutation) + 1;
			}
			timeouts.put(mutation, t);
			
	//		timeouts++;
			if (t >= MAX_TIMEOUTS)
				disabled.add(mutation);
		}else{
			timeout++;
			if (timeout >= MAX_TIMEOUTS){
				disabled.add(mutation);
			}
		}
	}

	// TODO: Still need a good way to call this
	/**
	 * <p>raisedException</p>
	 *
	 * @param mutation a {@link org.evosuite.coverage.mutation.Mutation} object.
	 */
	public static void raisedException(Mutation mutation) {
		if (Properties.STRATEGY != Strategy.ONEBRANCH){
			exceptions.add(mutation);
		}else{
			hasException = true;
		}
		disabled.add(mutation);
	}

	/** {@inheritDoc} */
	@Override
	public void forceCurrentValue(long value) {
		if (Properties.STRATEGY == Strategy.ONEBRANCH){
			timeout = (int) value;
		}
	}
	
	public static void resetStatic() {
		timeouts.clear();
		exceptions.clear();
		disabled.clear();
		timeout = 0;
		hasException = false;
	}

}
