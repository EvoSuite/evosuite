/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
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
package de.unisb.cs.st.evosuite.coverage.branch;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJPool;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.graphs.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.javaagent.LinePool;
import de.unisb.cs.st.evosuite.testcase.ExecutableChromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.AbstractTestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * Fitness function for a whole test suite for all branches
 * 
 * @author Gordon Fraser
 * 
 */
public class BranchCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 2991632394620406243L;

	private static Logger logger = LoggerFactory.getLogger(TestSuiteFitnessFunction.class);

	public final int totalMethods;
	public final int totalBranches;
	public final int numBranchlessMethods;
	public final Set<Integer> lines;
	private final Set<String> branchlessMethods;

	public int covered_branches = 0;

	public int covered_methods = 0;

	public double best_fitness = Double.MAX_VALUE;

	public int totalGoals;

	public static int mostCoveredGoals = 0;

	protected boolean check = false;

	private final Set<String> publicTargetMethods = new HashSet<String>();

	public BranchCoverageSuiteFitness() {

		String prefix = Properties.TARGET_CLASS_PREFIX;

		if (prefix.isEmpty()) {
			prefix = Properties.TARGET_CLASS;
			totalMethods = CFGMethodAdapter.getNumMethodsMemberClasses(prefix);
			totalBranches = BranchPool.getBranchCountForMemberClasses(prefix);
			numBranchlessMethods = BranchPool.getNumBranchlessMethodsMemberClasses(prefix);
			branchlessMethods = BranchPool.getBranchlessMethodsMemberClasses(prefix);
		} else {
			totalMethods = CFGMethodAdapter.getNumMethodsPrefix(prefix);
			totalBranches = BranchPool.getBranchCountForPrefix(prefix);
			numBranchlessMethods = BranchPool.getNumBranchlessMethodsPrefix(prefix);
			branchlessMethods = BranchPool.getBranchlessMethodsPrefix(prefix);
		}

		/* TODO: Would be nice to use a prefix here */
		lines = LinePool.getLines(Properties.TARGET_CLASS);

		totalGoals = 2 * totalBranches + numBranchlessMethods;

		logger.info("Total branch coverage goals: " + totalGoals);
		logger.info("Total branches: " + totalBranches);
		logger.info("Total branchless methods: " + numBranchlessMethods);
		logger.info("Total methods: " + totalMethods + ": "
		        + CFGMethodAdapter.methods.get(Properties.TARGET_CLASS));

		getPublicMethods();
		determineCoverageGoals();
	}

	public int maxCoveredBranches = 0;

	public int maxCoveredMethods = 0;

	public double bestFitness = Double.MAX_VALUE;

	private final Map<Integer, TestFitnessFunction> branchCoverageTrueMap = new HashMap<Integer, TestFitnessFunction>();
	private final Map<Integer, TestFitnessFunction> branchCoverageFalseMap = new HashMap<Integer, TestFitnessFunction>();

	private final Map<String, TestFitnessFunction> branchlessMethodCoverageMap = new HashMap<String, TestFitnessFunction>();

	private void determineCoverageGoals() {
		List<TestFitnessFunction> goals = new BranchCoverageFactory().getCoverageGoals();
		for (TestFitnessFunction goal : goals) {
			BranchCoverageTestFitness goalFitness = (BranchCoverageTestFitness) goal;
			if (goalFitness.getBranch() == null) {
				branchlessMethodCoverageMap.put(goalFitness.getClassName() + "."
				        + goalFitness.getMethod(), goal);
			} else {
				if (goalFitness.getBranchExpressionValue())
					branchCoverageTrueMap.put(goalFitness.getBranch().getActualBranchId(),
					                          goal);
				else
					branchCoverageFalseMap.put(goalFitness.getBranch().getActualBranchId(),
					                           goal);
			}
		}
	}

	private void getPublicMethods() {
		for (Method method : Properties.getTargetClass().getDeclaredMethods()) {
			if (Modifier.isPublic(method.getModifiers())) {
				String name = method.getName() + Type.getMethodDescriptor(method);
				publicTargetMethods.add(name);
			}
		}
	}

	private Set<String> getDirectlyCoveredMethods(
	        AbstractTestSuiteChromosome<ExecutableChromosome> suite) {
		Set<String> covered = new HashSet<String>();
		for (ExecutableChromosome test : suite.getTestChromosomes()) {
			ExecutionResult result = test.getLastExecutionResult();
			int limit = test.size();
			if (!result.noThrownExceptions()) {
				limit = result.getFirstPositionOfThrownException() + 1;
			}
			for (int i = 0; i < limit; i++) {
				StatementInterface statement = result.test.getStatement(i);
				if (statement instanceof MethodStatement) {
					MethodStatement methodStatement = (MethodStatement) statement;
					Method method = methodStatement.getMethod();
					if (method.getDeclaringClass().equals(Properties.getTargetClass())
					        && Modifier.isPublic(method.getModifiers())) {
						String name = method.getName() + Type.getMethodDescriptor(method);
						covered.add(name);
					}
				}
			}
		}
		return covered;
	}

	/**
	 * Execute all tests and count covered branches
	 */
	@SuppressWarnings("unchecked")
	@Override
	public double getFitness(Chromosome individual) {
		logger.trace("Calculating branch fitness");

		long start = System.currentTimeMillis();

		AbstractTestSuiteChromosome<ExecutableChromosome> suite = (AbstractTestSuiteChromosome<ExecutableChromosome>) individual;
		long estart = System.currentTimeMillis();
		List<ExecutionResult> results = runTestSuite(suite);
		long eend = System.currentTimeMillis();
		double fitness = 0.0;
		Map<Integer, Double> trueDistance = new HashMap<Integer, Double>();
		Map<Integer, Double> falseDistance = new HashMap<Integer, Double>();
		Map<Integer, Integer> predicateCount = new HashMap<Integer, Integer>();
		Map<String, Integer> callCount = new HashMap<String, Integer>();
		Set<Integer> covered_lines = new HashSet<Integer>();
		boolean hasTimeout = false;

		for (ExecutionResult result : results) {
			if (hasTimeout(result)) {
				hasTimeout = true;
			}

			for (Entry<String, Integer> entry : result.getTrace().coveredMethods.entrySet()) {
				if (!callCount.containsKey(entry.getKey()))
					callCount.put(entry.getKey(), entry.getValue());
				else {
					callCount.put(entry.getKey(),
					              callCount.get(entry.getKey()) + entry.getValue());
				}
				if (branchlessMethodCoverageMap.containsKey(entry.getKey())) {
					result.test.addCoveredGoal(branchlessMethodCoverageMap.get(entry.getKey()));
				}

			}
			for (Entry<Integer, Integer> entry : result.getTrace().coveredPredicates.entrySet()) {
				if (!LCSAJPool.isLCSAJBranch(BranchPool.getBranch(entry.getKey()))) {
					if (!predicateCount.containsKey(entry.getKey()))
						predicateCount.put(entry.getKey(), entry.getValue());
					else {
						predicateCount.put(entry.getKey(),
						                   predicateCount.get(entry.getKey())
						                           + entry.getValue());
					}
				}
			}
			for (Entry<Integer, Double> entry : result.getTrace().trueDistances.entrySet()) {
				if (!LCSAJPool.isLCSAJBranch(BranchPool.getBranch(entry.getKey()))) {
					if (!trueDistance.containsKey(entry.getKey()))
						trueDistance.put(entry.getKey(), entry.getValue());
					else {
						trueDistance.put(entry.getKey(),
						                 Math.min(trueDistance.get(entry.getKey()),
						                          entry.getValue()));
					}
					if (entry.getValue() == 0.0) {
						result.test.addCoveredGoal(branchCoverageTrueMap.get(entry.getKey()));
					}
				}
			}
			for (Entry<Integer, Double> entry : result.getTrace().falseDistances.entrySet()) {
				if (!LCSAJPool.isLCSAJBranch(BranchPool.getBranch(entry.getKey()))) {
					if (!falseDistance.containsKey(entry.getKey()))
						falseDistance.put(entry.getKey(), entry.getValue());
					else {
						falseDistance.put(entry.getKey(),
						                  Math.min(falseDistance.get(entry.getKey()),
						                           entry.getValue()));
					}
				}
				if (entry.getValue() == 0.0) {
					result.test.addCoveredGoal(branchCoverageFalseMap.get(entry.getKey()));
				}
			}

			if (Properties.BRANCH_STATEMENT) {
				// Add requirement on statements
				for (Map<String, Map<Integer, Integer>> coverage : result.getTrace().coverage.values()) {
					for (Map<Integer, Integer> coveredLines : coverage.values())
						covered_lines.addAll(coveredLines.keySet());
				}
			}
			//if (result.hasUndeclaredException())
			//	fitness += 1.0;
		}

		int numCoveredBranches = 0;

		for (Integer key : predicateCount.keySet()) {
			if (!trueDistance.containsKey(key) || !falseDistance.containsKey(key))
				continue;
			int numExecuted = predicateCount.get(key);
			double df = trueDistance.get(key);
			double dt = falseDistance.get(key);

			// If the branch predicate was only executed once, then add 1 
			if (numExecuted == 1) {
				fitness += 1.0;
			} else {
				fitness += normalize(df) + normalize(dt);
			}
			if (df == 0.0)
				numCoveredBranches++;

			if (dt == 0.0)
				numCoveredBranches++;
		}

		int missingMethods = 0;

		Set<String> methods = Properties.TARGET_CLASS_PREFIX.isEmpty() ? CFGMethodAdapter.getMethods(Properties.TARGET_CLASS)
		        : CFGMethodAdapter.getMethodsPrefix(Properties.TARGET_CLASS_PREFIX);

		for (String e : methods) {
			if (!callCount.containsKey(e)) {
				fitness += 1.0;
				missingMethods += 1;
			}
		}

		fitness += 2 * (totalBranches - predicateCount.size());

		//		covered_methods  = Math.max(covered_methods,  call_count.size());
		if (Properties.BRANCH_STATEMENT) {
			int totalLines = lines.size();
			logger.info("Covered " + covered_lines.size() + " out of " + totalLines
			        + " lines");
			fitness += normalize(totalLines - covered_lines.size());
		}
		printStatusMessages(suite, numCoveredBranches, totalMethods - missingMethods,
		                    fitness);

		//logger.info("Fitness: "+fitness+", size: "+suite.size()+", length: "+suite.length());

		/*
		Set<String> coveredPublicMethods = getDirectlyCoveredMethods(suite);
		Set<String> uncoveredPublicMethods = new HashSet<String>(publicTargetMethods);
		uncoveredPublicMethods.removeAll(coveredPublicMethods);
		fitness += uncoveredPublicMethods.size();
		logger.debug("Adding penalty for public methods: "
		        + uncoveredPublicMethods.size());
		for (String method : uncoveredPublicMethods) {
			logger.debug(method);
		}
		*/

		long end = System.currentTimeMillis();
		if (end - start > 1000) {
			logger.info("Executing tests took    : " + (eend - estart) + "ms");
			logger.info("Calculating fitness took: " + (end - start) + "ms");
		}
		int coverage = numCoveredBranches;
		for (String e : branchlessMethods) {
			if (callCount.keySet().contains(e)) {
				coverage++;
			}

		}
		if (mostCoveredGoals < coverage)
			mostCoveredGoals = coverage;

		assert (coverage <= totalGoals) : "Covered " + coverage + " vs total goals "
		        + totalGoals;
		suite.setCoverage((double) coverage / (double) totalGoals);
		assert (fitness != 0.0 || coverage == totalGoals) : "Fitness: " + fitness + ", "
		        + "coverage: " + coverage + "/" + totalGoals;
		if (hasTimeout) {
			logger.info("Test suite has timed out, setting fitness to max value "
			        + (totalBranches * 2 + totalMethods));
			fitness = totalBranches * 2 + totalMethods;
			//suite.setCoverage(0.0);
		}

		updateIndividual(individual, fitness);

		assert (suite.getCoverage() <= 1.0) && (suite.getCoverage() >= 0.0) : "Wrong coverage value "
		        + suite.getCoverage();
		//if (!check)
		//	checkFitness(suite, fitness);
		return fitness;
	}

	/**
	 * Some useful debug information
	 * 
	 * @param coveredBranches
	 * @param coveredMethods
	 * @param fitness
	 */
	private void printStatusMessages(
	        AbstractTestSuiteChromosome<ExecutableChromosome> suite, int coveredBranches,
	        int coveredMethods, double fitness) {
		if (coveredBranches > maxCoveredBranches) {
			maxCoveredBranches = coveredBranches;
			logger.info("(Branches) Best individual covers " + coveredBranches + "/"
			        + (totalBranches * 2) + " branches and " + coveredMethods + "/"
			        + totalMethods + " methods");
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());
		}
		if (coveredMethods > maxCoveredMethods) {
			logger.info("(Methods) Best individual covers " + coveredBranches + "/"
			        + (totalBranches * 2) + " branches and " + coveredMethods + "/"
			        + totalMethods + " methods");
			maxCoveredMethods = coveredMethods;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());

		}
		if (fitness < bestFitness) {
			logger.info("(Fitness) Best individual covers " + coveredBranches + "/"
			        + (totalBranches * 2) + " branches and " + coveredMethods + "/"
			        + totalMethods + " methods");
			bestFitness = fitness;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());

		}
	}

	/**
	 * This method can be used for debugging purposes to ensure that the fitness
	 * calculation is deterministic
	 * 
	 * @param suite
	 * @param fitness
	 */
	protected void checkFitness(AbstractTestSuiteChromosome<ExecutableChromosome> suite,
	        double fitness) {
		for (ExecutableChromosome test : suite.getTestChromosomes()) {
			test.setChanged(true);
		}
		logger.info("Running double check");
		check = true;
		double fitness2 = getFitness(suite);
		check = false;
		//		assert (fitness == fitness2) : "Fitness is " + fitness + " but should be "
		//		        + fitness2;
		if (fitness != fitness2)
			logger.error("Fitness is " + fitness + " but should be " + fitness2);
	}
}
