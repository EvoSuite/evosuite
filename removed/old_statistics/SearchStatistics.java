/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.testsuite;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.evosuite.Properties;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.dataflow.DefUseCoverageFactory;
import org.evosuite.coverage.dataflow.DefUseCoverageTestFitness;
import org.evosuite.coverage.dataflow.DefUsePool;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.stoppingconditions.MaxFitnessEvaluationsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.ExecutionTrace;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.ReportGenerator;
import org.evosuite.utils.Utils;
import org.objectweb.asm.Type;

/**
 * <p>
 * SearchStatistics class.
 * </p>
 * 
 * @author Gordon Fraser
 */
@Deprecated
public class SearchStatistics extends ReportGenerator implements Serializable {

	private static final long serialVersionUID = 8780927435434567712L;

	private static SearchStatistics instance = null;

	private SearchStatistics() {

	}

	/**
	 * <p>
	 * Getter for the field <code>instance</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testsuite.SearchStatistics} object.
	 */
	public static SearchStatistics getInstance() {
		if (instance == null) {
			instance = new SearchStatistics();
		}
		return instance;
	}

	/**
	 * <p>
	 * Setter for the field <code>instance</code>.
	 * </p>
	 * 
	 * @param statistics
	 *            a {@link org.evosuite.testsuite.SearchStatistics} object.
	 */
	public static void setInstance(SearchStatistics statistics) {
		instance = statistics;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Write a file for a particular run
	 */
	@Override
	protected String writeRunPage(StatisticEntry run) {

		StringBuffer sb = new StringBuffer();
		writeHTMLHeader(sb, run.className);

		/*
		sb.append("<div id=\"header\"><div id=\"logo\">");
		sb.append("<h2>Target class: ");
		sb.append(run.className);
		sb.append(": ");
		sb.append(String.format("%.2f", 100.0 * run.covered_goals / run.total_goals));
		sb.append("%");
		sb.append("</h2></div></div>\n");
		*/
		sb.append("<br><br><h2 class=title>Summary</h2>\n");
		sb.append("<ul><li>Target class: ");
		sb.append(run.className);
		sb.append(": ");
		sb.append(String.format("%.2f", 100.0 * run.covered_goals / run.total_goals));
		sb.append("%</ul>");

		writeResultTable(sb, run);
		// writeMutationTable(sb);
		sb.append("<div id=\"page\"><div id=\"page-bgtop\"><div id=\"page-bgbtm\"><div id=\"content\">");
		sb.append("<div id=\"post\">");

		// Resulting test case
		sb.append("<h2 class=title id=tests>Test suite</h2>\n<div class=tests>\n");
		if (run.tests != null) {
			int num = 0;
			for (TestCase test : run.tests) {
				sb.append("<h3>Test case ");
				sb.append(++num);
				sb.append("</h3>\n");
				/*
				 * if(test.exceptionThrown != null) { sb.append("<p>Raises:");
				 * sb.append(test.exceptionThrown); sb.append("</p>"); }
				 */
				sb.append("<pre class=\"prettyprint\" style=\"border: 1px solid #888;padding: 2px\">\n");
				int linecount = 1;
				String code = null;
				if (run.results.containsKey(test))
					code = test.toCode(run.results.get(test));
				else
					code = test.toCode();

				for (String line : code.split("\n")) {
					sb.append(String.format("<span class=\"nocode\"><a name=\"%d\">%3d: </a></span>",
					                        linecount, linecount));
					/*
					 * if(test.exceptionsThrown != null &&
					 * test.exception_statement == test_line)
					 * sb.append("<span style=\"background: #FF0000\">");
					 */
					sb.append(StringEscapeUtils.escapeHtml4(line));
					/*
					 * if(test.exceptionThrown != null &&
					 * test.exception_statement == test_line)
					 * sb.append("</span>");
					 */
					linecount++;
					sb.append("\n");
				}
				sb.append("</pre>\n");
			}
		} else {
			sb.append("No test cases generated");
		}
		sb.append("</div></div>");
		sb.append("<div id=\"post\">");

		// Source code
		/*
		 * Iterable<String> source =
		 * html_analyzer.getClassContent(run.className);
		 * sb.append("<h2>Coverage</h2>\n"); sb.append("<p>"); sb.append(
		 * "<pre class=\"prettyprint\" style=\"border: 1px solid #888;padding: 2px\">"
		 * ); int line_num = run.mutation.getLineNumber() - 3; if(line_num < 0)
		 * line_num = 0; int linecount = 1; for (String line : source) {
		 * if(linecount >= line_num && linecount < (line_num + 6)) {
		 * sb.append(String.format( "<span class=\"nocode\">%3d: </span>",
		 * linecount)); sb.append(StringEscapeUtils.escapeHtml(line));
		 * sb.append("\n"); } linecount++; } sb.append("</pre>\n");
		 */

		// Chart of fitness
		if (Properties.PLOT) {
			sb.append("<h2 class=title id=tests>Statistics</h2>\n<div class=tests>\n");

			if (run.fitness_history.isEmpty()) {
				sb.append("<h3>No fitness history</h3>\n");
			} else {
				String filename = writeDoubleChart(run.fitness_history, run.className
				        + "-" + run.id, "Fitness");
				sb.append("<h3>Fitness</h3>\n");
				sb.append("<p>");
				sb.append("<img src=\"../img/");
				sb.append(filename);
				sb.append("\" height=\"250\">");
				sb.append("</p>\n");
			}

			// Chart of size
			if (run.size_history.isEmpty()) {
				sb.append("<h3>No size history</h3>\n");
			} else {
				String filename = writeIntegerChart(run.size_history, run.className + "-"
				        + run.id, "Size");
				sb.append("<h3>Size</h3>\n");
				sb.append("<p>");
				sb.append("<img src=\"../img/");
				sb.append(filename);
				sb.append("\" height=\"250\">");
				sb.append("</p>\n");
			}

			// Chart of length
			if (run.length_history.isEmpty()) {
				sb.append("<h3>No length history</h3>\n");
			} else {
				String filename = writeIntegerChart(run.length_history, run.className
				        + "-" + run.id, "Length");
				sb.append("<h3>Length</h3>\n");
				sb.append("<p>");
				sb.append("<img src=\"../img/");
				sb.append(filename);
				sb.append("\" height=\"250\">");
				sb.append("</p>\n");
			}

			// Chart of average number of tests
			/*
			if (run.size_history.isEmpty()) {
				sb.append("<h2>No average length history</h2>\n");
			} else {
				String filename = writeDoubleChart(run.average_length_history,
				                                   run.className + "-" + run.id, "Length");
				sb.append("<h2>Average Length</h2>\n");
				sb.append("<p>");
				sb.append("<img src=\"../img/");
				sb.append(filename);
				sb.append("\" height=\"200\">");
				sb.append("</p>\n");
			}
			*/
			sb.append("</div>");
		}
		sb.append("</div>");
		sb.append("<div id=\"post\">");

		// Source code
		try {
			Iterable<String> source = html_analyzer.getClassContent(run.className);
			sb.append("<h2 class=title id=source>Source Code</h2>\n");
			sb.append("<div class=source><p>");
			sb.append("<pre class=\"prettyprint\" style=\"border: 1px solid #888;padding: 2px\">");
			int linecount = 1;
			for (String line : source) {
				sb.append(String.format("<span class=\"nocode\"><a name=\"%d\">%3d: </a></span>",
				                        linecount, linecount));
				if (run.coverage.contains(linecount)) {
					sb.append("<span style=\"background-color: #ffffcc\">");
					sb.append(StringEscapeUtils.escapeHtml4(line));
					sb.append("</span>");
				}

				else
					sb.append(StringEscapeUtils.escapeHtml4(line));
				sb.append("\n");
				linecount++;
			}
			sb.append("</pre>\n");

			sb.append("</p></div>\n");
		} catch (Exception e) {
			// Don't display source if there is an error
		}
		sb.append("</div>");
		sb.append("<div id=\"post\">");

		writeParameterTable(sb, run);
		sb.append("</div>");
		sb.append("<p><br><a href=\"../report-generation.html\">Back to Overview</a></p>\n");

		writeHTMLFooter(sb);

		String filename = "report-" + run.className + "-" + run.id + ".html";
		File file = new File(getReportDir().getAbsolutePath() + "/html/" + filename);
		Utils.writeFile(sb.toString(), file);
		// return file.getAbsolutePath();
		return filename;
	}

	/**
	 * <p>
	 * mutationScore
	 * </p>
	 * 
	 * @param mutationScore
	 *            a double.
	 */
	public void mutationScore(double mutationScore) {
		StatisticEntry entry = statistics.get(statistics.size() - 1);
		entry.mutationScore = mutationScore;
	}

	public void addCoverage(String criterion, double coverage) {
		StatisticEntry entry = statistics.get(statistics.size() - 1);
		entry.coverageMap.put(criterion, coverage);
	}

	public boolean hasCoverage(String criterion) {
		StatisticEntry entry = statistics.get(statistics.size() - 1);
		return entry.coverageMap.containsKey(criterion);
	}

	public double getCoverage(String criterion) {
		StatisticEntry entry = statistics.get(statistics.size() - 1);
		return entry.coverageMap.get(criterion);
	}

	public void setCoveredGoals(int num) {
		StatisticEntry entry = statistics.get(statistics.size() - 1);
		entry.covered_goals = num;
	}

	public void setHadUnstableTests(boolean unstable) {
		
		if(statistics.size() == 0){ //FIXME better handling once refactoring statistics
			//this could happen in the test cases
			return;
		}
		
		StatisticEntry entry = statistics.get(statistics.size() - 1);
		entry.hadUnstableTests = unstable;
	}

	
	/** {@inheritDoc} */
	@Override
	public void minimized(Chromosome chromosome) {
		TestSuiteChromosome best = (TestSuiteChromosome) chromosome;
		StatisticEntry entry = statistics.get(statistics.size() - 1);
		entry.tests = best.getTests();
		// TODO: Remember which lines were covered
		// This information is in ExecutionTrace.coverage
		entry.size_minimized = best.size();
		entry.length_minimized = best.totalLengthOfTestCases();
		entry.minimized_time = System.currentTimeMillis();

		entry.coverage = new HashSet<Integer>();
		entry.coveredIntraMethodPairs = 0;
		entry.coveredInterMethodPairs = 0;
		entry.coveredIntraClassPairs = 0;
		entry.coveredParameterPairs = 0;

		entry.aliasingIntraMethodPairs = 0;
		entry.aliasingInterMethodPairs = 0;
		entry.aliasingIntraClassPairs = 0;
		entry.aliasingParameterPairs = 0;

		entry.coveredAliasIntraMethodPairs = 0;
		entry.coveredAliasInterMethodPairs = 0;
		entry.coveredAliasIntraClassPairs = 0;
		entry.coveredAliasParameterPairs = 0;

		// TODO isn't this more or less copy-paste of
		// BranchCoverageSuiteFitness.getFitness()?
		// DONE To make this work for other criteria too, it would be perfect if
		// one
		// could ask every suite fitness how many goals were covered

		logger.debug("Calculating coverage of best individual with fitness "
		        + chromosome.getFitness());

		Map<Integer, Double> true_distance = new HashMap<Integer, Double>();
		Map<Integer, Double> false_distance = new HashMap<Integer, Double>();
		Map<Integer, Integer> predicate_count = new HashMap<Integer, Integer>();
		Set<String> covered_methods = new HashSet<String>();
		Map<String, Set<Class<?>>> implicitTypesOfExceptions = new HashMap<String, Set<Class<?>>>();
		Map<String, Set<Class<?>>> explicitTypesOfExceptions = new HashMap<String, Set<Class<?>>>();

		Map<TestCase, Map<Integer, Boolean>> isExceptionExplicit = new HashMap<TestCase, Map<Integer, Boolean>>();

		Set<DefUseCoverageTestFitness> coveredDUGoals = new HashSet<DefUseCoverageTestFitness>();
		if (ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.DEFUSE)
		        || Properties.ANALYSIS_CRITERIA.toUpperCase().contains("DEFUSE")) {
			for (DefUseCoverageTestFitness goal : DefUseCoverageFactory.getDUGoals()) {
				if (goal.isInterMethodPair())
					entry.numInterMethodPairs++;
				else if (goal.isIntraClassPair())
					entry.numIntraClassPairs++;
				else if (goal.isParameterGoal())
					entry.numParameterPairs++;
				else
					entry.numIntraMethodPairs++;						
				if(goal.isAlias()) {
					if (goal.isInterMethodPair())
						entry.aliasingInterMethodPairs++;
					else if (goal.isIntraClassPair())
						entry.aliasingIntraClassPairs++;
					else if (goal.isParameterGoal())
						entry.aliasingParameterPairs++;
					else
						entry.aliasingIntraMethodPairs++;
				}
			}
			entry.numDefinitions = DefUsePool.getDefCounter();
			entry.numUses = DefUsePool.getUseCounter();
			entry.numDefUsePairs = DefUseCoverageFactory.getDUGoals().size();
		}
		logger.debug("Calculating line coverage");

		for (TestChromosome test : best.tests) {
			ExecutionResult result = executeTest(test, entry.className);
			ExecutionTrace trace = result.getTrace();
			entry.coverage.addAll(getCoveredLines(trace, entry.className));
			isExceptionExplicit.put(test.getTestCase(), result.explicitExceptions);

			if (ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.DEFUSE)
			        || Properties.ANALYSIS_CRITERIA.toUpperCase().contains("DEFUSE")) {
				for (DefUseCoverageTestFitness goal : DefUseCoverageFactory.getDUGoals()) {
					if (coveredDUGoals.contains(goal))
						continue;
					if (goal.isCovered(result)) {
						coveredDUGoals.add(goal);
						if (goal.isInterMethodPair()) {
							entry.coveredInterMethodPairs++;
							if(goal.isAlias()) {
								entry.coveredAliasInterMethodPairs++;
							}
						}
						else if (goal.isIntraClassPair()) {
							entry.coveredIntraClassPairs++;
							if(goal.isAlias()) {
								entry.coveredAliasIntraClassPairs++;
							}
						}
						else if (goal.isParameterGoal()) {
							entry.coveredParameterPairs++;
							if(goal.isAlias()) {
								entry.coveredAliasParameterPairs++;
							}
						}
						else {
							entry.coveredIntraMethodPairs++;
							if(goal.isAlias()) {
								entry.coveredAliasIntraMethodPairs++;
							}
						}
					}
				}
			}

			for (String method : trace.getCoveredMethods()) {
				if (method.startsWith(Properties.TARGET_CLASS)
				        || method.startsWith(Properties.TARGET_CLASS + '$'))
					covered_methods.add(method);
			}
			// covered_methods.addAll(trace.getCoveredMethods());

			for (Entry<Integer, Double> e : trace.getTrueDistances().entrySet()) {
				if (!predicate_count.containsKey(e.getKey()))
					predicate_count.put(e.getKey(), 1);
				else
					predicate_count.put(e.getKey(), predicate_count.get(e.getKey()) + 1);

				if (!true_distance.containsKey(e.getKey())
				        || true_distance.get(e.getKey()) > e.getValue()) {
					true_distance.put(e.getKey(), e.getValue());
				}
			}
			for (Entry<Integer, Double> e : trace.getFalseDistances().entrySet()) {
				if (!predicate_count.containsKey(e.getKey()))
					predicate_count.put(e.getKey(), 1);
				else
					predicate_count.put(e.getKey(), predicate_count.get(e.getKey()) + 1);

				if (!false_distance.containsKey(e.getKey())
				        || false_distance.get(e.getKey()) > e.getValue()) {
					false_distance.put(e.getKey(), e.getValue());
				}
			}

		}

		for (TestCase test : entry.results.keySet()) {
			Map<Integer, Throwable> exceptions = entry.results.get(test);
			//iterate on the indexes of the statements that resulted in an exception
			for (Integer i : exceptions.keySet()) {
				Throwable t = exceptions.get(i);
				if (t instanceof SecurityException && Properties.SANDBOX)
					continue;
				if (i >= test.size()) {
					// Timeouts are put after the last statement if the process was forcefully killed
					continue;
				}

				String methodName = "";
				boolean sutException = false;

				if (test.getStatement(i) instanceof MethodStatement) {
					MethodStatement ms = (MethodStatement) test.getStatement(i);
					Method method = ms.getMethod().getMethod();
					methodName = method.getName() + Type.getMethodDescriptor(method);
					if (method.getDeclaringClass().equals(Properties.getTargetClass()))
						sutException = true;
				} else if (test.getStatement(i) instanceof ConstructorStatement) {
					ConstructorStatement cs = (ConstructorStatement) test.getStatement(i);
					Constructor<?> constructor = cs.getConstructor().getConstructor();
					methodName = "<init>" + Type.getConstructorDescriptor(constructor);
					if (constructor.getDeclaringClass().equals(Properties.getTargetClass()))
						sutException = true;
				}
				boolean notDeclared = !test.getStatement(i).getDeclaredExceptions().contains(t.getClass());
				if (notDeclared && sutException) {
					/*
					 * we need to distinguish whether it is explicit (ie "throw" in the code, eg for validating
					 * input for pre-condition) or implicit ("likely" a real fault).
					 */

					/*
					 * FIXME: need to find a way to calculate it
					 */
					boolean isExplicit = isExceptionExplicit.get(test).containsKey(i)
					        && isExceptionExplicit.get(test).get(i);
					if (isExplicit) {
						if (!explicitTypesOfExceptions.containsKey(methodName))
							explicitTypesOfExceptions.put(methodName,
							                              new HashSet<Class<?>>());
						explicitTypesOfExceptions.get(methodName).add(t.getClass());
					} else {
						if (!implicitTypesOfExceptions.containsKey(methodName))
							implicitTypesOfExceptions.put(methodName,
							                              new HashSet<Class<?>>());
						implicitTypesOfExceptions.get(methodName).add(t.getClass());
					}
				}
			}
		}

		int num_covered = 0;
		entry.error_branches = BranchPool.getNumArtificialBranches();

		for (Integer key : predicate_count.keySet()) {
			// logger.info("Key: "+key);
			double df = true_distance.get(key);
			double dt = false_distance.get(key);
			Branch b = BranchPool.getBranch(key);
			if (!b.getClassName().startsWith(Properties.TARGET_CLASS)
			        && !b.getClassName().startsWith(Properties.TARGET_CLASS + '$'))
				continue;

			//if (!b.isInstrumented()) {
			if (df == 0.0)
				num_covered++;
			if (dt == 0.0)
				num_covered++;
			//}
			if (b.isInstrumented()) {
				// entry.error_branches++;
				if (df == 0.0)
					entry.error_branches_covered++;
				if (dt == 0.0)
					entry.error_branches_covered++;
			}
		}

		for (String methodName : CFGMethodAdapter.getMethodsPrefix(Properties.TARGET_CLASS)) {
			boolean allArtificial = true;
			int splitPoint = methodName.lastIndexOf(".");
			String cName = methodName.substring(0, splitPoint);
			String mName = methodName.substring(splitPoint + 1);
			boolean hasBranches = false;
			for (Branch b : BranchPool.retrieveBranchesInMethod(cName, mName)) {
				hasBranches = true;
				if (!b.isInstrumented()) {
					allArtificial = false;
					break;
				}
			}
			if (hasBranches && allArtificial) {
				entry.error_branchless_methods++;
				if (covered_methods.contains(methodName)) {
					entry.error_branchless_methods_covered++;
				}
			}
		}

		int coveredBranchlessMethods = 0;
		for (String branchlessMethod : BranchPool.getBranchlessMethodsMemberClasses(Properties.TARGET_CLASS)) {
			if (covered_methods.contains(branchlessMethod))
				coveredBranchlessMethods++;
		}

		entry.covered_branches = num_covered; // + covered branchless methods?
		entry.covered_methods = covered_methods.size();
		entry.covered_branchless_methods = coveredBranchlessMethods;
		//BranchCoverageSuiteFitness f = new BranchCoverageSuiteFitness();

		/*
		if (Properties.CRITERION == Properties.Criterion.DEFUSE
		        || Properties.ANALYSIS_CRITERIA.contains("DefUse")) {
			entry.coveredIntraMethodPairs = DefUseCoverageSuiteFitness.mostCoveredGoals.get(DefUsePairType.INTRA_METHOD);
			entry.coveredInterMethodPairs = DefUseCoverageSuiteFitness.mostCoveredGoals.get(DefUsePairType.INTER_METHOD);
			entry.coveredIntraClassPairs = DefUseCoverageSuiteFitness.mostCoveredGoals.get(DefUsePairType.INTRA_CLASS);
			entry.coveredParameterPairs = DefUseCoverageSuiteFitness.mostCoveredGoals.get(DefUsePairType.PARAMETER);
		}
			*/

		//System.out.println(covered_methods);

		// DONE make this work for other criteria too. this will only work for
		// branch coverage - see searchStarted()/Finished()

		// entry.total_goals = 2 * entry.total_branches +
		// entry.branchless_methods; - moved to searchStarted()
		// entry.covered_goals = num_covered; - moved to searchFinished()

		// for(String e : CFGMethodAdapter.branchless_methods) {
		// for (String e : CFGMethodAdapter.methods) {
		// for (String e : BranchPool.getBranchlessMethods()) {
		// if (covered_methods.contains(e)) {
		// logger.info("Covered method: " + e);
		// entry.covered_goals++;
		// } else {
		// logger.info("Method is not covered: " + e);
		// }
		/*
		 * logger.debug("Covered methods: " + covered_methods.size() + "/" +
		 * entry.total_methods); for (String method : covered_methods) {
		 * logger.debug("Covered method: " + method); }
		 */
		// }

		String s = calculateCoveredBranchesBitString(best);
		entry.goalCoverage = s;
		entry.explicitMethodExceptions = getNumExceptions(explicitTypesOfExceptions);
		entry.explicitTypeExceptions = getNumClassExceptions(explicitTypesOfExceptions);

		entry.implicitMethodExceptions = getNumExceptions(implicitTypesOfExceptions);
		entry.implicitTypeExceptions = getNumClassExceptions(implicitTypesOfExceptions);

		entry.implicitExceptions = implicitTypesOfExceptions;
		entry.explicitExceptions = explicitTypesOfExceptions;

	}

	private String calculateCoveredBranchesBitString(TestSuiteChromosome best) {
		StringBuffer buffer = new StringBuffer(1024);
		BranchCoverageFactory factory = new BranchCoverageFactory();
		List<BranchCoverageTestFitness> goals = factory.getCoverageGoals();
		Collections.sort(goals); 
		for (BranchCoverageTestFitness fitness : goals) {
			boolean covered = false;
			for (TestChromosome test1 : best.tests) {
				if (fitness.isCovered(test1)) {
					covered = true;
					buffer.append("1");
					break;
				}
			}
			if (!covered){
				buffer.append("0");
			}
		}
		return buffer.toString();
	}

	private static int getNumExceptions(Map<String, Set<Class<?>>> exceptions) {
		int total = 0;
		for (Set<Class<?>> exceptionSet : exceptions.values()) {
			total += exceptionSet.size();
		}
		return total;
	}

	private static int getNumClassExceptions(Map<String, Set<Class<?>>> exceptions) {
		Set<Class<?>> classExceptions = new HashSet<Class<?>>();
		for (Set<Class<?>> exceptionSet : exceptions.values()) {
			classExceptions.addAll(exceptionSet);
		}
		return classExceptions.size();
	}

	/**
	 * <p>
	 * writeStatistics
	 * </p>
	 */
	public void writeStatistics() {
		makeDirs();
		writeCSV();
	}

	/** {@inheritDoc} */
	@Override
	public void searchFinished(GeneticAlgorithm<?> algorithm) {
		Chromosome result = algorithm.getBestIndividual();
		StatisticEntry entry = statistics.get(statistics.size() - 1);

		entry.end_time = System.currentTimeMillis();
		entry.result_tests_executed = MaxTestsStoppingCondition.getNumExecutedTests();
		entry.result_statements_executed = MaxStatementsStoppingCondition.getNumExecutedStatements();
		entry.testExecutionTime = TestCaseExecutor.timeExecuted;
		entry.goalComputationTime = AbstractFitnessFactory.goalComputationTime;
		entry.covered_goals = result.getNumOfCoveredGoals();
		entry.timedOut = TestSuiteGenerator.global_time.isFinished();
		entry.stoppingCondition = TestSuiteGenerator.stopping_condition.getCurrentValue();
		entry.globalTimeStoppingCondition = TestSuiteGenerator.global_time.getCurrentValue();

		if (result instanceof TestSuiteChromosome) {
			TestSuiteChromosome best = (TestSuiteChromosome) result;
			entry.size_final = best.size();
			entry.length_final = best.totalLengthOfTestCases();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void searchStarted(GeneticAlgorithm<?> algorithm) {
		super.searchStarted(algorithm);
		StatisticEntry entry = statistics.get(statistics.size() - 1);

		entry.total_branches = Properties.TARGET_CLASS_PREFIX.isEmpty() ? BranchPool.getBranchCountForMemberClasses(Properties.TARGET_CLASS)
		        : BranchPool.getBranchCountForPrefix(Properties.TARGET_CLASS_PREFIX);

		entry.branchless_methods = Properties.TARGET_CLASS_PREFIX.isEmpty() ? BranchPool.getBranchlessMethodsMemberClasses(Properties.TARGET_CLASS).size()
		        : BranchPool.getBranchlessMethodsPrefix(Properties.TARGET_CLASS_PREFIX).size();

		entry.total_methods = Properties.TARGET_CLASS_PREFIX.isEmpty() ? CFGMethodAdapter.getNumMethodsMemberClasses(Properties.TARGET_CLASS)
		        : CFGMethodAdapter.getNumMethodsPrefix(Properties.TARGET_CLASS_PREFIX);

		List<? extends TestFitnessFunction> goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals(); // FIXME: since this class is classified as 'deprecate' we are just assuming one fitness function
		entry.total_goals = goals.size();

		//for (TestFitnessFunction f : TestSuiteGenerator.getFitnessFactory().getCoverageGoals()) {
		for (TestFitnessFunction f : goals) {
			if (f instanceof BranchCoverageTestFitness) {
				BranchCoverageTestFitness b = (BranchCoverageTestFitness) f;
				if (b.getBranch() != null && b.getBranch().isInstrumented()) {
				}
			}
		}

		// removed the code below with the one above, in order to have these
		// values for other criteria as well

		// if (algorithm.getFitnessFunction() instanceof
		// BranchCoverageSuiteFitness) {
		// BranchCoverageSuiteFitness fitness = (BranchCoverageSuiteFitness)
		// algorithm.getFitnessFunction();
		// entry.total_branches = fitness.total_branches;
		// entry.branchless_methods = fitness.branchless_methods;
		// entry.total_methods = fitness.total_methods;
		// }
	}

	/** {@inheritDoc} */
	@Override
	public void iteration(GeneticAlgorithm<?> algorithm) {
		super.iteration(algorithm);

		StatisticEntry entry = statistics.get(statistics.size() - 1);
		Chromosome best = algorithm.getBestIndividual();
		if (best instanceof TestSuiteChromosome) {
			entry.length_history.add(((TestSuiteChromosome) best).totalLengthOfTestCases());
			entry.coverage_history.add(((TestSuiteChromosome) best).getCoverage());
			entry.tests_executed.add(MaxTestsStoppingCondition.getNumExecutedTests());
			entry.statements_executed.add(MaxStatementsStoppingCondition.getNumExecutedStatements());
			entry.fitness_evaluations.add(MaxFitnessEvaluationsStoppingCondition.getNumFitnessEvaluations());
			entry.timeStamps.add(System.currentTimeMillis() - entry.creationTime);
		}
	}

	/**
	 * <p>
	 * getLastStatisticEntry
	 * </p>
	 * 
	 * @return a StatisticEntry object.
	 */
	public StatisticEntry getLastStatisticEntry() {
		return statistics.get(statistics.size() - 1);
	}
}
