package org.evosuite.symbolic;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.RealConstraint;
import org.evosuite.symbolic.expr.StringConstraint;
import org.evosuite.symbolic.solver.ConstraintCache;
import org.evosuite.utils.LoggingUtils;

/**
 * This class is used to store statistics on DSE.
 * 
 * @author galeotti
 * 
 */
public abstract class DSEStats {

	private static long nrOfUNSATs = 0;
	private static long nrOfSATs = 0;
	private static long nrOfTimeouts = 0;
	private static long nrOfSolutionWithNoImprovement = 0;
	private static long nrOfNewTestFound = 0;

	/**
	 * This method initializes all counters to 0. It should be called only if
	 * the user wants to clean all statistics.
	 */
	public static void clear() {
		nrOfUNSATs = 0;
		nrOfSATs = 0;
		nrOfSolutionWithNoImprovement = 0;
		nrOfNewTestFound = 0;
		constraintTypeCounter.clear();
	}

	public static void reportNewUNSAT() {
		nrOfUNSATs++;
	}

	/**
	 * Invoke this method when a SAT instance was found by a Constraint Solver
	 */
	public static void reportNewSAT() {
		nrOfSATs++;
	}

	/**
	 * Call this method to report a new test found by DSE did not lead to a
	 * fitness improvement.
	 */
	public static void reportNewTestUnuseful() {
		nrOfSolutionWithNoImprovement++;
	}

	/**
	 * Invoke this method when no instance was found by a Constraint Solver
	 */
	public static long getUNSAT() {
		return nrOfUNSATs;
	}

	/**
	 * Returns the number of SAT instances found. This instance may lead to a
	 * new Test Case or not
	 * 
	 * @return
	 */
	public static long getSAT() {
		return nrOfSATs;
	}

	/**
	 * Returns the total number of SAT instances that did not lead to a fitness
	 * improvement.
	 * 
	 * @return
	 */
	public static long getUnusefulTests() {
		return nrOfSolutionWithNoImprovement;
	}

	/**
	 * Invoke this method when a new test found by DSE is added to the test
	 * suite.
	 */
	public static void reportNewTestUseful() {
		nrOfNewTestFound++;
	}

	/**
	 * Returns the total number of new tests found by DSE added to a test suite.
	 * 
	 * @return
	 */
	public static long getUsefulTests() {
		return nrOfNewTestFound;
	}

	public static void printStatistics() {

		LoggingUtils.getEvoLogger().info("* DSE Statistics");

		printSolvingStatistics();

		LoggingUtils.getEvoLogger().info("");
		printConstraintSizeStatistics();

		LoggingUtils.getEvoLogger().info("");
		printPathConditionLengthStatistics();

		LoggingUtils.getEvoLogger().info("");
		printTimeStatistics();

		LoggingUtils.getEvoLogger().info("");
		printCacheStatistics();
		LoggingUtils.getEvoLogger().info("");

		LoggingUtils.getEvoLogger().info("");
		printAdaptationStatistics();
		LoggingUtils.getEvoLogger().info("");

	}

	private static void printAdaptationStatistics() {
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

		LoggingUtils.getEvoLogger().info("* DSE) Adaptation statistics");
		LoggingUtils.getEvoLogger().info(
				"* DSE)   Adaptations: " + buff.toString());
	}

	private static void printCacheStatistics() {
		LoggingUtils.getEvoLogger().info("* DSE) Constraint Cache Statistics");
		final int numberOfSATs = ConstraintCache.getInstance()
				.getNumberOfSATs();
		final int numberOfUNSATs = ConstraintCache.getInstance()
				.getNumberOfUNSATs();

		if (numberOfSATs == 0 || numberOfUNSATs == 0) {
			LoggingUtils.getEvoLogger().info(
					"* DSE)   Constraint Cache was not used.");

		} else {

			LoggingUtils.getEvoLogger().info(
					String.format("* DSE)   Stored SAT constraints: %s",
							numberOfSATs));

			LoggingUtils.getEvoLogger().info(
					String.format("* DSE)   Stored UNSAT constraints: %s",
							numberOfUNSATs));

			NumberFormat percentFormat = NumberFormat.getPercentInstance();
			percentFormat.setMaximumFractionDigits(1);
			String hit_rate_str = percentFormat.format(ConstraintCache
					.getInstance().getHitRate());
			LoggingUtils.getEvoLogger().info(
					String.format("* DSE)   Cache hit rate: %s", hit_rate_str));
		}
	}

	private static void printTimeStatistics() {
		LoggingUtils.getEvoLogger().info("* DSE) Time Statistics");
		LoggingUtils.getEvoLogger().info(
				String.format("* DSE)   Time spent solving constraints: %sms",
						totalSolvingTimeMillis));
		LoggingUtils
				.getEvoLogger()
				.info(String
						.format("* DSE)   Time spent executing test concolically: %sms",
								totalConcolicExecutionTimeMillis));
	}

	private static void printSolvingStatistics() {
		long total_constraint_solvings = DSEStats.getSAT()
				+ DSEStats.getUNSAT() + DSEStats.getTimeouts();

		String SAT_ratio_str = "Nan";
		String UNSAT_ratio_str = "Nan";
		String useful_tests_ratio_str = "Nan";
		String unuseful_tests_ratio_str = "Nan";
		String timeout_ratio_str = "Nan";

		if (total_constraint_solvings > 0) {
			double SAT_ratio = (double) DSEStats.getSAT()
					/ (double) total_constraint_solvings;
			double UNSAT_ratio = (double) DSEStats.getUNSAT()
					/ (double) total_constraint_solvings;
			double useful_tests_ratio = (double) DSEStats.getUsefulTests()
					/ (double) total_constraint_solvings;
			double unuseful_tests_ratio = (double) DSEStats.getUnusefulTests()
					/ (double) total_constraint_solvings;
			double timeout_ratio = (double) DSEStats.getTimeouts()
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

		LoggingUtils.getEvoLogger().info("* DSE) Solving statistics");
		LoggingUtils.getEvoLogger().info(
				String.format("* DSE)   SAT: %s (%s)", DSEStats.getSAT(),
						SAT_ratio_str));
		LoggingUtils.getEvoLogger().info(
				String.format("* DSE) 	  Useful Tests: %s (%s)",
						DSEStats.getUsefulTests(), useful_tests_ratio_str));
		LoggingUtils.getEvoLogger().info(
				String.format("* DSE) 	  Unuseful Tests:  %s (%s)",
						DSEStats.getUnusefulTests(), unuseful_tests_ratio_str));
		LoggingUtils.getEvoLogger().info(
				String.format("* DSE)   UNSAT: %s (%s)", DSEStats.getUNSAT(),
						UNSAT_ratio_str));
		LoggingUtils.getEvoLogger().info(
				String.format("* DSE)   Timeouts: %s (%s)", timeout_ratio_str,
						DSEStats.getTimeouts()));

		LoggingUtils.getEvoLogger().info(
				String.format("* DSE)   # Constraint solvings: %s (%s+%s)",
						total_constraint_solvings, DSEStats.getSAT(),
						DSEStats.getUNSAT()));

	}

	private static void printConstraintTypeStatistics() {
		int total = constraintTypeCounter.getTotalNumberOfConstraints();

		int integerOnly = constraintTypeCounter.getIntegerOnlyConstraints();
		int realOnly = constraintTypeCounter.getRealOnlyConstraints();
		int stringOnly = constraintTypeCounter.getStringOnlyConstraints();

		int integerRealOnly = constraintTypeCounter
				.getIntegerAndRealConstraints();
		int integerStringOnly = constraintTypeCounter
				.getIntegerAndStringConstraints();
		int realStringOnly = constraintTypeCounter
				.getRealAndStringConstraints();

		int integerRealStringConstraints = constraintTypeCounter
				.getIntegerRealAndStringConstraints();

		if (total == 0) {
			LoggingUtils.getEvoLogger().info(
					String.format("* DSE)   no constraints",
							avg_constraint_size));
		} else {
			String line1 = String.format(
					"* DSE)   Number of integer only constraints : %s / %s ",
					integerOnly, total);
			String line2 = String.format(
					"* DSE)   Number of real only constraints : %s", realOnly,
					total);
			String line3 = String.format(
					"* DSE)   Number of string only constraints : %s",
					stringOnly, total);
			String line4 = String.format(
					"* DSE)   Number of integer+real constraints : %s / %s ",
					integerRealOnly, total);
			String line5 = String.format(
					"* DSE)   Number of integer+string constraints : %s / %s ",
					integerStringOnly, total);
			String line6 = String.format(
					"* DSE)   Number of real+string constraints : %s / %s ",
					realStringOnly, total);
			String line7 = String
					.format("* DSE)   Number of integer+real+string constraints : %s / %s ",
							integerRealStringConstraints, total);

			LoggingUtils.getEvoLogger().info(line1);
			LoggingUtils.getEvoLogger().info(line2);
			LoggingUtils.getEvoLogger().info(line3);
			LoggingUtils.getEvoLogger().info(line4);
			LoggingUtils.getEvoLogger().info(line5);
			LoggingUtils.getEvoLogger().info(line6);
			LoggingUtils.getEvoLogger().info(line7);

		}
	}

	private static void printConstraintSizeStatistics() {
		LoggingUtils.getEvoLogger().info("* DSE) Constraint size:");
		LoggingUtils.getEvoLogger().info(
				String.format("* DSE)   max constraint size: %s",
						max_constraint_size));
		LoggingUtils.getEvoLogger().info(
				String.format("* DSE)   min constraint size: %s",
						min_constraint_size));
		LoggingUtils.getEvoLogger().info(
				String.format("* DSE)   avg constraint size: %s",
						avg_constraint_size));
		LoggingUtils.getEvoLogger().info(
				String.format("* DSE)   Too big constraints: %s (max size %s)",
						DSEStats.getConstraintTooLongCounter(),
						Properties.DSE_CONSTRAINT_LENGTH));
	}

	private static void printPathConditionLengthStatistics() {
		LoggingUtils.getEvoLogger().info("* DSE) Path condition length:");
		LoggingUtils.getEvoLogger().info(
				String.format("* DSE)   max path condition length: %s",
						max_path_condition_length));
		LoggingUtils.getEvoLogger().info(
				String.format("* DSE)   min path condition length: %s",
						min_path_condition_length));
		LoggingUtils.getEvoLogger().info(
				String.format("* DSE)   avg path condition length: %s",
						avg_path_condition_length));
	}

	private static int getConstraintTooLongCounter() {
		return constraintTooLongCounter;
	}

	private static int max_path_condition_length;
	private static int min_path_condition_length;
	private static double avg_path_condition_length;

	private static int max_constraint_size = 0;
	private static int min_constraint_size = 0;
	private static double avg_constraint_size = 0;
	private static int constraint_count = 0;
	private static int path_condition_count = 0;
	private static final ConstraintTypeCounter constraintTypeCounter = new ConstraintTypeCounter();

	public static void reportNewConstraints(
			Collection<Constraint<?>> constraints) {

		if (path_condition_count == 0) {
			min_path_condition_length = constraints.size();
			max_path_condition_length = constraints.size();
			avg_path_condition_length = constraints.size();
		} else {
			// update average size
			double new_avg_size = avg_path_condition_length
					+ ((((double) constraints.size() - avg_path_condition_length)) / ((double) path_condition_count + 1));
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

	private static void countTypesOfConstraints(
			Collection<Constraint<?>> constraints) {
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
				throw new IllegalArgumentException("The constraint type "
						+ constraint.getClass().getCanonicalName()
						+ " is not considered!");
			}
		}
		constraintTypeCounter.addNewConstraint(hasIntegerConstraint,
				hasRealConstraint, hasStringConstraint);
	}

	private static long totalSolvingTimeMillis = 0;

	/**
	 * Reports a new solving time (use of a constraint solver)
	 * 
	 * @param solvingTimeMillis
	 */
	public static void reportNewSolvingTime(long solvingTimeMillis) {
		totalSolvingTimeMillis += solvingTimeMillis;
	}

	private static long totalConcolicExecutionTimeMillis = 0;
	private static int constraintTooLongCounter = 0;

	/**
	 * Reports a new concolic execution time (use of instrumentation and path
	 * constraint collection)
	 * 
	 * @param concolicExecutionTimeMillis
	 */
	public static void reportNewConcolicExecutionTime(
			long concolicExecutionTimeMillis) {
		totalConcolicExecutionTimeMillis += concolicExecutionTimeMillis;
	}

	public static void reportConstraintTooLong(int size) {
		constraintTooLongCounter++;
	}

	public static void reportNewTimeout() {
		nrOfTimeouts++;
	}

	public static long getTimeouts() {
		return nrOfTimeouts;
	}

	private static List<Boolean> changes = new LinkedList<Boolean>();

	public static void reportNewIncrease() {
		changes.add(true);
	}

	public static void reportNewDecrease() {
		changes.add(false);
	}

}
