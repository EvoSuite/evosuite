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
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Gordon Fraser
 */
package org.evosuite.ga.stoppingconditions;

import org.evosuite.ga.metaheuristics.SearchListener;
public interface StoppingCondition extends SearchListener {

	/**
	 * Force a specific amount of used up budget. Handle with care!
	 *
	 * @param value
	 *            The new amount of used up budget for this StoppingCondition
	 */
	public abstract void forceCurrentValue(long value);

	/**
	 * How much of the budget have we used up
	 *
	 * @return a long.
	 */
	public abstract long getCurrentValue();

	/**
	 * Get upper limit of resources
	 *
	 * Mainly used for toString()
	 *
	 * @return limit
	 */
	public abstract long getLimit();

	/**
	 * <p>isFinished</p>
	 *
	 * @return a boolean.
	 */
	boolean isFinished();

	/**
	 * Reset everything
	 */
	void reset();

	/**
	 * Set new upper limit of resources
	 *
	 * @param limit a long.
	 */
	void setLimit(long limit);
}
