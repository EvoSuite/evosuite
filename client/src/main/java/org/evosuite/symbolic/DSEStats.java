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
package org.evosuite.symbolic;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.RealConstraint;
import org.evosuite.symbolic.expr.StringConstraint;
import org.evosuite.symbolic.solver.SolverCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to store statistics on DSE.
 * 
 * @author galeotti
 * 
 */
public class DSEStats {

	static Logger logger = LoggerFactory.getLogger(DSEStats.class);

	private static DSEStats instance = null;

	public static DSEStats getInstance() {
		if (instance==null) {
			instance=new DSEStats();
		}
		return instance;
	}

	/**
	 * This method initializes all counters to 0. It should be called only if
	 * the user wants to clean all statistics.
	 */
	public static void clear() {
		instance = null;
	}

	/**
	 * This class cannot be built directly 
	 */
	private DSEStats() {
		
	}

	private long nrOfUNSATs = 0;
	private long nrOfSATs = 0;
	private long nrOfTimeouts = 0;
	private long nrOfSolutionWithNoImprovement = 0;
	private long nrOfNewTestFound = 0;
	private long totalSolvingTimeMillis = 0;
	private long totalConcolicExecutionTimeMillis = 0;
	private int constraintTooLongCounter = 0;
	private int max_path_condition_length;
	private int min_path_condition_length;
	private double avg_path_condition_length;
	private int max_constraint_size = 0;
	private int min_constraint_size = 0;
	private double avg_constraint_size = 0;
	private int constraint_count = 0;
	private int path_condition_count = 0;
	private final List<Boolean> changes = new LinkedList<Boolean>();
	private final ConstraintTypeCounter constraintTypeCounter = new ConstraintTypeCounter();

	public void reportNewUNSAT() {
		nrOfUNSATs++;
	}

	/**
	 * Invoke this method when a SAT instance was found by a Constraint Solver
	 */
	public void reportNewSAT() {
		nrOfSATs++;
	}

	/**
	 * Call this method to report a new test found by DSE did not lead to a
	 * fitness improvement.
	 */
	public void reportNewTestUnuseful() {
		nrOfSolutionWithNoImprovement++;
	}

	/**
	 * Invoke this method when no instance was found by a Constraint Solver
	 */
	private long getUNSAT() {
		return nrOfUNSATs;
	}

	/**
	 * Returns the number of SAT instances found. This instance may lead to a
	 * new Test Case or not
	 * 
	 * @return
	 */
	private long getSAT() {
		return nrOfSATs;
	}

	/**
	 * Returns the total number of SAT instances that did not lead to a fitness
	 * improvement.
	 * 
	 * @return
	 */
	private long getUnusefulTests() {
		return nrOfSolutionWithNoImprovement;
	}

	/**
	 * Invoke this method when a new test found by DSE is added to the test
	 * suite.
	 */
	public void reportNewTestUseful() {
		nrOfNewTestFound++;
	}

	/**
	 * Returns the total number of new tests found by DSE added to a test suite.
	 * 
	 * @return
	 */
	private long getUsefulTests() {
		return nrOfNewTestFound;
	}

	public void logStatistics() {

		logger.info("* DSE Statistics");

		logSolverStatistics();

		logger.info("");
		logConstraintSizeStatistics();

		logger.info("");
		logPathConditionLengthStatistics();

		logger.info("");
		logTimeStatistics();

		logger.info("");
		logCacheStatistics();
		logger.info("");

		logger.info("");
		logAdaptationStatistics();
		logger.info("");

		logger.info("");
		logConstraintTypeStatistics();
		logger.info("");

	}

	private void logAdaptationStatistics() {
		StringBuffer buff = new StringBuffer();
		buff.append("[");
		for (Boolean change : changes) {
			if (change) {
				buff.append("+");
			} else {
				buff.append("-");
			}
		}
		buff.append("]");

		logger.info("* LS) Local Search Adaptation statistics");
		logger.info("* LS)   Adaptations: " + buff.toString());
	}

	private void logCacheStatistics() {
		logger.info("* DSE) Constraint Cache Statistics");
		final int numberOfSATs = SolverCache.getInstance().getNumberOfSATs();
		final int numberOfUNSATs = SolverCache.getInstance().getNumberOfUNSATs();

		if (numberOfSATs == 0 || numberOfUNSATs == 0) {
			logger.info("* DSE)   Constraint Cache was not used.");

		} else {

			logger.info(String.format("* DSE)   Stored SAT constraints: %s", numberOfSATs));

			logger.info(String.format("* DSE)   Stored UNSAT constraints: %s", numberOfUNSATs));

			NumberFormat percentFormat = NumberFormat.getPercentInstance();
			percentFormat.setMaximumFractionDigits(1);
			String hit_rate_str = percentFormat.format(SolverCache.getInstance().getHitRate());
			logger.info(String.format("* DSE)   Cache hit rate: %s", hit_rate_str));
		}
	}

	private void logTimeStatistics() {
		logger.info("* DSE) Time Statistics");
		logger.info(String.format("* DSE)   Time spent solving constraints: %sms", totalSolvingTimeMillis));
		logger.info(String.format("* DSE)   Time spent executing test concolically: %sms",
				totalConcolicExecutionTimeMillis));
	}

	private  void logSolverStatistics() {
		long total_constraint_solvings = getSAT()
				+ getUNSAT() + getTimeouts();

		String SAT_ratio_str = "Nan";
		String UNSAT_ratio_str = "Nan";
		String useful_tests_ratio_str = "Nan";
		String unuseful_tests_ratio_str = "Nan";
		String timeout_ratio_str = "Nan";

		if (total_constraint_solvings > 0) {
			double SAT_ratio = (double) getSAT()
					/ (double) total_constraint_solvings;
			double UNSAT_ratio = (double) getUNSAT()
					/ (double) total_constraint_solvings;
			double useful_tests_ratio = (double) getUsefulTests()
					/ (double) total_constraint_solvings;
			double unuseful_tests_ratio = (double) getUnusefulTests()
					/ (double) total_constraint_solvings;
			double timeout_ratio = (double) getTimeouts()
					/ (double) total_constraint_solvings;

			NumberFormat percentFormat = NumberFormat.getPercentInstance();
			percentFormat.setMaximumFractionDigits(1);

			SAT_ratio_str = percentFormat.format(SAT_ratio);
			UNSAT_ratio_str = percentFormat.format(UNSAT_ratio);
			useful_tests_ratio_str = percentFormat.format(useful_tests_ratio);
			unuseful_tests_ratio_str = percentFormat
					.format(unuseful_tests_ratio);
			timeout_ratio_str = percentFormat.format(timeout_ratio);
		}

		logger.info("* DSE) Solving statistics");
		logger.info(String.format("* DSE)   SAT: %s (%s)", getSAT(),
				SAT_ratio_str));
		logger.info(String.format("* DSE) 	  Useful Tests: %s (%s)",
				getUsefulTests(), useful_tests_ratio_str));
		logger.info(String.format("* DSE) 	  Unuseful Tests:  %s (%s)",
				getUnusefulTests(), unuseful_tests_ratio_str));
		logger.info(String.format("* DSE)   UNSAT: %s (%s)",
				getUNSAT(), UNSAT_ratio_str));
		logger.info(String.format("* DSE)   Timeouts: %s (%s)",
				timeout_ratio_str, getTimeouts()));

		logger.info(String.format("* DSE)   # Constraint solvings: %s (%s+%s)",
				total_constraint_solvings, getSAT(),
				getUNSAT()));

	}

	private void logConstraintTypeStatistics() {
		int total = constraintTypeCounter.getTotalNumberOfConstraints();

		int integerOnly = constraintTypeCounter.getIntegerOnlyConstraints();
		int realOnly = constraintTypeCounter.getRealOnlyConstraints();
		int stringOnly = constraintTypeCounter.getStringOnlyConstraints();

		int integerRealOnly = constraintTypeCounter.getIntegerAndRealConstraints();
		int integerStringOnly = constraintTypeCounter.getIntegerAndStringConstraints();
		int realStringOnly = constraintTypeCounter.getRealAndStringConstraints();

		int integerRealStringConstraints = constraintTypeCounter.getIntegerRealAndStringConstraints();

		if (total == 0) {
			logger.info(String.format("* DSE)   no constraints", avg_constraint_size));
		} else {
			String line1 = String.format("* DSE)   Number of integer only constraints : %s / %s ", integerOnly, total);
			String line2 = String.format("* DSE)   Number of real only constraints : %s", realOnly, total);
			String line3 = String.format("* DSE)   Number of string only constraints : %s", stringOnly, total);
			String line4 = String.format("* DSE)   Number of integer+real constraints : %s / %s ", integerRealOnly,
					total);
			String line5 = String.format("* DSE)   Number of integer+string constraints : %s / %s ", integerStringOnly,
					total);
			String line6 = String.format("* DSE)   Number of real+string constraints : %s / %s ", realStringOnly,
					total);
			String line7 = String.format("* DSE)   Number of integer+real+string constraints : %s / %s ",
					integerRealStringConstraints, total);

			logger.info(line1);
			logger.info(line2);
			logger.info(line3);
			logger.info(line4);
			logger.info(line5);
			logger.info(line6);
			logger.info(line7);

		}
	}

	private void logConstraintSizeStatistics() {
		logger.info("* DSE) Constraint size:");
		logger.info(String.format("* DSE)   max constraint size: %s", max_constraint_size));
		logger.info(String.format("* DSE)   min constraint size: %s", min_constraint_size));
		logger.info(String.format("* DSE)   avg constraint size: %s", avg_constraint_size));
		logger.info(String.format("* DSE)   Too big constraints: %s (max size %s)",
				getConstraintTooLongCounter(), Properties.DSE_CONSTRAINT_LENGTH));
	}

	private void logPathConditionLengthStatistics() {
		logger.info("* DSE) Path condition length:");
		logger.info(String.format("* DSE)   max path condition length: %s", max_path_condition_length));
		logger.info(String.format("* DSE)   min path condition length: %s", min_path_condition_length));
		logger.info(String.format("* DSE)   avg path condition length: %s", avg_path_condition_length));
	}

	private int getConstraintTooLongCounter() {
		return constraintTooLongCounter;
	}

	public void reportNewConstraints(Collection<Constraint<?>> constraints) {

		if (path_condition_count == 0) {
			min_path_condition_length = constraints.size();
			max_path_condition_length = constraints.size();
			avg_path_condition_length = constraints.size();
		} else {
			// update average size
			double new_avg_size = avg_path_condition_length
					+ ((((double) constraints.size() - avg_path_condition_length))
							/ ((double) path_condition_count + 1));
			avg_path_condition_length = new_avg_size;

			// update max length
			if (constraints.size() > max_path_condition_length) {
				max_path_condition_length = constraints.size();
			}

			// update min length
			if (constraints.size() < min_path_condition_length) {
				min_path_condition_length = constraints.size();
			}
		}

		path_condition_count++;

		for (Constraint<?> c : constraints) {
			if (constraint_count == 0) {
				min_constraint_size = c.getSize();
				max_constraint_size = c.getSize();
				avg_constraint_size = c.getSize();
			} else {
				// update average size
				double new_avg_size = avg_constraint_size
						+ ((((double) c.getSize() - avg_constraint_size)) / ((double) constraint_count + 1));
				avg_constraint_size = new_avg_size;

				// update max size
				if (c.getSize() > max_constraint_size) {
					max_constraint_size = c.getSize();
				}
				// update min size
				if (c.getSize() < min_constraint_size) {
					min_constraint_size = c.getSize();
				}

			}

			constraint_count++;
		}

		countTypesOfConstraints(constraints);

	}

	private void countTypesOfConstraints(Collection<Constraint<?>> constraints) {
		boolean hasIntegerConstraint = false;
		boolean hasRealConstraint = false;
		boolean hasStringConstraint = false;
		for (Constraint<?> constraint : constraints) {
			if (constraint instanceof StringConstraint) {
				hasStringConstraint = true;
			} else if (constraint instanceof IntegerConstraint) {
				hasIntegerConstraint = true;
			} else if (constraint instanceof RealConstraint) {
				hasRealConstraint = true;
			} else {
				throw new IllegalArgumentException(
						"The constraint type " + constraint.getClass().getCanonicalName() + " is not considered!");
			}
		}
		constraintTypeCounter.addNewConstraint(hasIntegerConstraint, hasRealConstraint, hasStringConstraint);
	}

	/**
	 * Reports a new solving time (use of a constraint solver)
	 * 
	 * @param solvingTimeMillis
	 */
	public void reportNewSolvingTime(long solvingTimeMillis) {
		totalSolvingTimeMillis += solvingTimeMillis;
	}

	/**
	 * Reports a new concolic execution time (use of instrumentation and path
	 * constraint collection)
	 * 
	 * @param concolicExecutionTimeMillis
	 */
	public void reportNewConcolicExecutionTime(long concolicExecutionTimeMillis) {
		totalConcolicExecutionTimeMillis += concolicExecutionTimeMillis;
	}

	public void reportConstraintTooLong(int size) {
		constraintTooLongCounter++;
	}

	public void reportNewTimeout() {
		nrOfTimeouts++;
	}

	private long getTimeouts() {
		return nrOfTimeouts;
	}

	public void reportNewIncrease() {
		changes.add(true);
	}

	public void reportNewDecrease() {
		changes.add(false);
	}

	public void trackConstraintTypes() {
		int total = constraintTypeCounter.getTotalNumberOfConstraints();

		int integerOnly = constraintTypeCounter.getIntegerOnlyConstraints();
		int realOnly = constraintTypeCounter.getRealOnlyConstraints();
		int stringOnly = constraintTypeCounter.getStringOnlyConstraints();

		int integerRealOnly = constraintTypeCounter.getIntegerAndRealConstraints();
		int integerStringOnly = constraintTypeCounter.getIntegerAndStringConstraints();
		int realStringOnly = constraintTypeCounter.getRealAndStringConstraints();

		int integerRealStringConstraints = constraintTypeCounter.getIntegerRealAndStringConstraints();

		trackOutputVariable(RuntimeVariable.IntegerOnlyConstraints, integerOnly);

		trackOutputVariable(RuntimeVariable.RealOnlyConstraints, realOnly);

		trackOutputVariable(RuntimeVariable.StringOnlyConstraints, stringOnly);

		trackOutputVariable(RuntimeVariable.IntegerAndRealConstraints, integerRealOnly);

		trackOutputVariable(RuntimeVariable.IntegerAndStringConstraints, integerStringOnly);

		trackOutputVariable(RuntimeVariable.RealAndStringConstraints, realStringOnly);

		trackOutputVariable(RuntimeVariable.IntegerRealAndStringConstraints, integerRealStringConstraints);

		trackOutputVariable(RuntimeVariable.TotalNumberOfConstraints, total);

	}

	public void trackSolverStatistics() {
		trackOutputVariable(RuntimeVariable.NumberOfSATQueries, getSAT());

		trackOutputVariable(RuntimeVariable.NumberOfUNSATQueries, getUNSAT());

		trackOutputVariable(RuntimeVariable.NumberOfTimeoutQueries, getTimeouts());

		trackOutputVariable(RuntimeVariable.NumberOfUsefulNewTests, getUsefulTests());

		trackOutputVariable(RuntimeVariable.NumberOfUnusefulNewTests, getUnusefulTests());

	}

	private void trackOutputVariable(RuntimeVariable var, Object value) {
		ClientServices.getInstance().getClientNode().trackOutputVariable(var, value);

	}
}
