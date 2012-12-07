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
 */
package org.evosuite.symbolic.search;

import java.util.Collection;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.RealConstraint;
import org.evosuite.symbolic.expr.StringConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Abstract DistanceEstimator class.
 * </p>
 * 
 * @author krusev
 */
public abstract class DistanceEstimator {

	static Logger log = LoggerFactory.getLogger(DistanceEstimator.class);

	// static Logger log =
	// JPF.getLogger("org.evosuite.symbolic.search.DistanceEstimator");

	private static double normalize(double x) {
		return x / (x + 1.0);
	}

	/**
	 * <p>
	 * getDistance
	 * </p>
	 * 
	 * @param constraints
	 *            a {@link java.util.Collection} object.
	 * @return normalized distance in [0,1]
	 */
	public static double getDistance(Collection<Constraint<?>> constraints) {
		double result = 0;

		try {
			for (Constraint<?> c : constraints) {

				if (c instanceof StringConstraint) {
					StringConstraint string_constraint = (StringConstraint) c;

					try {
						double strD = string_constraint.getStringDist();
						result += normalize(strD);
						log.debug("S: " + string_constraint + " strDist "
								+ strD);
					} catch (Throwable t) {
						log.debug("S: " + string_constraint + " strDist " + t);
						result += 1.0;
					}

				} else if (c instanceof IntegerConstraint) {

					IntegerConstraint integer_constraint = (IntegerConstraint) c;
					long intD = integer_constraint.getIntegerDist();
					result += normalize(intD);
					log.debug("C: " + integer_constraint + " intDist " + intD);

				} else if (c instanceof RealConstraint) {
					RealConstraint real_constraint = (RealConstraint) c;
					double realD = real_constraint.getRealDist();
					result += normalize(realD);
					log.debug("C: " + real_constraint + " realDist " + realD);

				} else {
					throw new IllegalArgumentException(
							"DistanceEstimator.getDistance(): "
									+ "got an unknown constraint: " + c);
				}
			}
			log.debug("Resulting distance: " + result);
			return Math.abs(result);
			
		} catch (Exception e) {
			// log.warn(e.toString());
			// System.err.println(e.getClass().getName());
			// e.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

}
