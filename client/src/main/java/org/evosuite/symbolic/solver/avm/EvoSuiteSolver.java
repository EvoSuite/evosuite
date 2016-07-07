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
package org.evosuite.symbolic.solver.avm;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.SolverEmptyQueryException;
import org.evosuite.symbolic.solver.DistanceEstimator;
import org.evosuite.symbolic.solver.Solver;
import org.evosuite.symbolic.solver.SolverResult;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Solves a collection of constraints using the Alternating Variable method.
 * 
 * @author galeotti
 * 
 */
public final class EvoSuiteSolver extends Solver {

	static Logger log = LoggerFactory.getLogger(EvoSuiteSolver.class);

	@Override
	public SolverResult solve(Collection<Constraint<?>> constraints)
			throws SolverTimeoutException, SolverEmptyQueryException {

		long timeout = Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS;

		long startTimeMillis = System.currentTimeMillis();

		Set<Variable<?>> variables = getVariables(constraints);
		Map<String, Object> initialValues = getConcreteValues(variables);

		double distance = DistanceEstimator.getDistance(constraints);
		if (distance == 0.0) {
			log.info("Initial distance already is 0.0, skipping search");
			SolverResult satResult = SolverResult.newSAT(initialValues);
			return satResult;
		}

		for (int attempt = 0; attempt <= Properties.DSE_VARIABLE_RESETS; attempt++) {
			for (Variable<?> v : variables) {
				long currentTimeMillis = System.currentTimeMillis();

				long elapsed_solving_time = currentTimeMillis - startTimeMillis;
				if (elapsed_solving_time > timeout) {
					throw new SolverTimeoutException();
				}

				log.debug("Variable: " + v + ", " + variables);

				if (v instanceof IntegerVariable) {
					IntegerVariable integerVariable = (IntegerVariable) v;
					IntegerAVM avm = new IntegerAVM(integerVariable, constraints, startTimeMillis, timeout);
					avm.applyAVM();
				} else if (v instanceof RealVariable) {
					RealVariable realVariable = (RealVariable) v;
					RealAVM avm = new RealAVM(realVariable, constraints, startTimeMillis, timeout);
					avm.applyAVM();
				} else if (v instanceof StringVariable) {
					StringVariable strVariable = (StringVariable) v;
					StringAVM avm = new StringAVM(strVariable, constraints, startTimeMillis, timeout);
					avm.applyAVM();
				} else {
					throw new RuntimeException("Unknown variable type " + v.getClass().getName());
				}
				distance = DistanceEstimator.getDistance(constraints);
				if (distance <= 0.0) {
					log.info("Distance is 0, ending search");
					break;
				}
			}
			if (distance <= 0.0) {
				log.info("Distance is 0, ending search");
				break;
			} else {
				log.info("Randomizing variables");
				randomizeValues(variables, getConstants(constraints));
			}
		}

		// distance = DistanceEstimator.getDistance(constraints);
		if (distance <= 0) {
			log.debug("Distance is " + distance + ", found solution");
			Map<String, Object> new_model = getConcreteValues(variables);
			setConcreteValues(variables, initialValues);
			SolverResult satResult = SolverResult.newSAT(new_model);
			return satResult;
		} else {
			setConcreteValues(variables, initialValues);
			log.debug("Returning null, search was not successful");
			SolverResult unsatResult = SolverResult.newUNSAT();
			return unsatResult;
		}

	}

	private static void randomizeValues(Set<Variable<?>> variables, Set<Object> constants) {
		Set<String> stringConstants = new HashSet<String>();
		Set<Long> longConstants = new HashSet<Long>();
		Set<Double> realConstants = new HashSet<Double>();
		for (Object o : constants) {
			if (o instanceof String)
				stringConstants.add((String) o);
			else if (o instanceof Double)
				realConstants.add((Double) o);
			else if (o instanceof Long)
				longConstants.add((Long) o);
			else
				assert(false) : "Unexpected constant type: " + o;
		}

		for (Variable<?> v : variables) {
			if (v instanceof StringVariable) {
				StringVariable sv = (StringVariable) v;
				if (!stringConstants.isEmpty() && Randomness.nextDouble() < Properties.DSE_CONSTANT_PROBABILITY) {
					sv.setConcreteValue(Randomness.choice(stringConstants));
				} else {
					sv.setConcreteValue(Randomness.nextString(Properties.STRING_LENGTH));
				}
			} else if (v instanceof IntegerVariable) {
				IntegerVariable iv = (IntegerVariable) v;
				if (!longConstants.isEmpty() && Randomness.nextDouble() < Properties.DSE_CONSTANT_PROBABILITY) {
					iv.setConcreteValue(Randomness.choice(longConstants));
				} else {
					iv.setConcreteValue((long) Randomness.nextInt(Properties.MAX_INT * 2) - Properties.MAX_INT);
				}
			} else if (v instanceof RealVariable) {
				RealVariable rv = (RealVariable) v;
				if (!realConstants.isEmpty() && Randomness.nextDouble() < Properties.DSE_CONSTANT_PROBABILITY) {
					rv.setConcreteValue(Randomness.choice(realConstants));
				} else {
					rv.setConcreteValue((long) Randomness.nextInt(Properties.MAX_INT * 2) - Properties.MAX_INT);
				}
			}
		}
	}

	private static Set<Object> getConstants(Collection<Constraint<?>> constraints) {
		Set<Object> constants = new HashSet<Object>();
		for (Constraint<?> c : constraints) {
			constants.addAll(c.getConstants());
		}
		return constants;
	}

}
