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
package org.evosuite.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringEscapeUtils;
import org.evosuite.Properties;
import org.evosuite.Properties.NoSuchParameterException;
import org.evosuite.contracts.AssertionErrorContract;
import org.evosuite.contracts.EqualsContract;
import org.evosuite.contracts.EqualsHashcodeContract;
import org.evosuite.contracts.EqualsNullContract;
import org.evosuite.contracts.EqualsSymmetricContract;
import org.evosuite.contracts.FailingTestSet;
import org.evosuite.contracts.HashCodeReturnsNormallyContract;
import org.evosuite.contracts.JCrasherExceptionContract;
import org.evosuite.contracts.NullPointerExceptionContract;
import org.evosuite.contracts.ToStringReturnsNormallyContract;
import org.evosuite.contracts.UndeclaredExceptionContract;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.runtime.sandbox.PermissionStatistics;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.ExecutionTrace;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.JUnitTestCarvedChromosomeFactory;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.FileTerminal;
import com.panayotis.gnuplot.terminal.GNUPlotTerminal;

/**
 * <p>
 * Abstract ReportGenerator class.
 * </p>
 * 
 * @author Gordon Fraser
 */
@Deprecated
public abstract class ReportGenerator implements SearchListener, Serializable {

	private static final long serialVersionUID = -920540796220051609L;
	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

	/**
	 * Return the folder of where reports should be generated.
	 * If the folder does not exist, try to create it
	 * 
	 * @return
	 * @throws RuntimeException if folder does not exist, and we cannot create it
	 */
	public static File getReportDir() throws RuntimeException{
		File dir = new File(Properties.REPORT_DIR);
		
		if(!dir.exists()){
			boolean created = dir.mkdirs();
			if(!created){
				String msg = "Cannot create report dir: "+Properties.REPORT_DIR;
				logger.error(msg);
				throw new RuntimeException(msg);
			}
		}
		
		return dir;			
	}

	/**
	 * <p>
	 * This enumeration defines all the runtime variables we want to store in
	 * the CSV files. Note, it is perfectly fine to add new ones, in any
	 * position. Just be sure to define a proper mapper in {@code getCSVvalue}.
	 * </p>
	 * 
	 * <p>
	 * WARNING: do not change the name of any variable! If you do, current R
	 * scripts will break. If you really need to change a name, please first
	 * contact Andrea Arcuri.
	 * </p>
	 * 
	 * @author arcuri
	 * 
	 */
	@Deprecated
	public enum RuntimeVariable {
		/** The class under test */
		Class,
		/** Number of predicates */
		Predicates,
		Total_Branches,
		Covered_Branches,
		Total_Methods,
		Branchless_Methods,
		Covered_Methods,
		Covered_Branchless_Methods,
		Total_Goals,
		Covered_Goals,
		Statements_Executed,
		/** Obtained coverage of the chosen testing criterion */
		Coverage,
		/**
		 * Obtained coverage at different points in time
		 */
		CoverageTimeline,
		/**
		 * Not only the covered branches ratio, but also including the
		 * branchless methods
		 */
		BranchCoverage,
		NumberOfGeneratedTestCases,
		/**
		 * The number of serialized objects that EvoSuite is
		 * going to use for seeding strategies
		 */
		NumberOfInputPoolObjects,
		DefUseCoverage,
		WeakMutationScore,
		Creation_Time,
		Minimization_Time,
		Total_Time,
		Test_Execution_Time,
		Goal_Computation_Time,
		Result_Size,
		Result_Length,
		Minimized_Size,
		Minimized_Length,
		Chromosome_Length,
		Population_Size,
		Random_Seed,
		Budget,
		AllPermission,
		SecurityPermission,
		UnresolvedPermission,
		AWTPermission,
		FilePermission,
		SerializablePermission,
		ReflectPermission,
		RuntimePermission,
		NetPermission,
		SocketPermission,
		SQLPermission,
		PropertyPermission,
		LoggingPermission,
		SSLPermission,
		AuthPermission,
		AudioPermission,
		OtherPermission,
		Threads,
		CoveredBranchesBitString,
		MutationScore,
		Explicit_MethodExceptions,
		Explicit_TypeExceptions,
		Implicit_MethodExceptions,
		Implicit_TypeExceptions,
		Error_Predicates,
		Error_Branches_Covered,
		Error_Branchless_Methods,
		Error_Branchless_Methods_Covered,
		AssertionContract,
		EqualsContract,
		EqualsHashcodeContract,
		EqualsNullContract,
		EqualsSymmetricContract,
		HashCodeReturnsNormallyContract,
		JCrasherExceptionContract,
		NullPointerExceptionContract,
		ToStringReturnsNormallyContract,
		UndeclaredExceptionContract,
		Contract_Violations,
		Unique_Violations,
		Data_File,
		/**
		 * Dataflow stuff
		 */
		Definitions,
		Uses,
		DefUsePairs,
		IntraMethodPairs,
		InterMethodPairs,
		IntraClassPairs,
		ParameterPairs,
		AliasingIntraMethodPairs,
		AliasingInterMethodPairs,
		AliasingIntraClassPairs,
		AliasingParameterPairs,
		CoveredIntraMethodPairs,
		CoveredInterMethodPairs,
		CoveredIntraClassPairs,
		CoveredParameterPairs,
		CoveredAliasIntraMethodPairs,
		CoveredAliasInterMethodPairs,
		CoveredAliasIntraClassPairs,
		CoveredAliasParameterPairs,
		CarvedTests,
		CarvedCoverage,
		HadUnstableTests
	};

	/** Constant <code>DATE_FORMAT_NOW="yyyy-MM-dd HH:mm:ss"</code> */
	protected static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	/**
	 * Statistics about one test generation run
	 * 
	 * @author Gordon Fraser
	 * 
	 */
	public class StatisticEntry implements Serializable {

		private static final long serialVersionUID = 8690481387977534927L;

		/** Run id */
		public int id = 0;

		public String className;

		public int population_size;

		public int chromosome_length;

		/** Total number of branches */
		public int total_branches;

		public int error_branches = 0;

		public int error_branches_covered = 0;

		public int error_branchless_methods = 0;

		public int error_branchless_methods_covered = 0;

		/** Total number of branches */
		public int covered_branches;

		public int total_methods;

		public int branchless_methods;

		public int covered_branchless_methods;

		public int covered_methods;

		public int total_goals;

		public int covered_goals;

		public Set<Integer> coverage = new HashSet<Integer>();

		public double mutationScore = 0.0;

		public Map<String, Double> coverageMap = new HashMap<String, Double>();

		/** Resulting test cases */
		public List<TestCase> tests = null;

		/** History of best fitness values */
		public List<Double> fitness_history = new ArrayList<Double>();

		/** History of best test suite size */
		public List<Integer> size_history = new ArrayList<Integer>();

		/** History of best test length */
		public List<Integer> length_history = new ArrayList<Integer>();

		/** History of average test length */
		public List<Double> average_length_history = new ArrayList<Double>();

		/** History of best test coverage */
		public List<Double> coverage_history = new ArrayList<Double>();

		/** History of best test length */
		public List<Long> tests_executed = new ArrayList<Long>();

		/** History of best test length */
		public List<Long> statements_executed = new ArrayList<Long>();

		/** History of the time stamps for generations */
		public List<Long> timeStamps = new ArrayList<Long>();

		/** History of best test length */
		public List<Long> fitness_evaluations = new ArrayList<Long>();

		/** Time at which this entry was created */
		public final long creationTime = System.currentTimeMillis();

		/** Number of tests after GA */
		public int size_final = 0;

		/** Total length of tests after GA */
		public int length_final = 0;

		/** Number of tests after minimization */
		public int size_minimized = 0;

		/** Total length of tests after minimization */
		public int length_minimized = 0;

		public Map<TestCase, Map<Integer, Throwable>> results = new HashMap<TestCase, Map<Integer, Throwable>>();

		public long start_time;

		public long end_time;

		public long minimized_time;

		public long testExecutionTime;

		public long goalComputationTime;

		public int result_fitness_evaluations = 0;

		public long result_tests_executed = 0;

		public long result_statements_executed = 0;

		public int age = 0;

		public double fitness = 0.0;

		public long seed = 0;

		public long stoppingCondition;

		public long globalTimeStoppingCondition;

		public boolean timedOut;

		public int numDefinitions;
		public int numUses;
		public int numDefUsePairs;

		public int numIntraMethodPairs;
		public int numInterMethodPairs;
		public int numIntraClassPairs;
		public int numParameterPairs;

		public int coveredIntraMethodPairs;
		public int coveredInterMethodPairs;
		public int coveredIntraClassPairs;
		public int coveredParameterPairs;

		public int aliasingIntraMethodPairs;
		public int aliasingInterMethodPairs;
		public int aliasingIntraClassPairs;
		public int aliasingParameterPairs;

		public int coveredAliasIntraMethodPairs;
		public int coveredAliasInterMethodPairs;
		public int coveredAliasIntraClassPairs;
		public int coveredAliasParameterPairs;

		public String goalCoverage;

		public int explicitMethodExceptions;

		public int explicitTypeExceptions;

		public int implicitMethodExceptions;

		public int implicitTypeExceptions;

		public Map<String, Set<Class<?>>> implicitExceptions;

		public Map<String, Set<Class<?>>> explicitExceptions;
		
		
		public boolean hadUnstableTests;
		
		//--------------------------------------------------

		private String[] getTimelineHeaderSuffixes() {
			int numberOfIntervals = calculateNumberOfIntervals();
			String[] suffixes = new String[numberOfIntervals];
			for (int i = 0; i < suffixes.length; i++) {
				/*
				 * NOTE: we start from T1 and not T0 because, by definition, coverage
				 * at T0 is equal to 0, and no point in showing it in a graph
				 */
				suffixes[i] = "_T" + (i + 1);
			}
			return suffixes;
		}

		private int calculateNumberOfIntervals() {
			long interval = Properties.TIMELINE_INTERVAL;
			/*
			 * TODO: this might need refactoring once we choose 
			 * a different way to handle search timeouts.
			 * 
			 * The point here is that we need to support both if we use time
			 * as search budget, and fitness/statement evaluations.
			 * We cannot just look at the obtained history, because the search might
			 * have finished earlier, eg if 100% coverage
			 */
			long totalTime = Properties.GLOBAL_TIMEOUT * 1000l;

			int numberOfIntervals = (int) (totalTime / interval);
			return numberOfIntervals;
		}

		/**
		 * Return array of variables to dump in CSV files, based on what defined
		 * in {@code Properties.OUTPUT_VARIABLES}
		 * 
		 * @return
		 */
		public String[] getUsedVariables() throws IllegalStateException {
			String property = Properties.OUTPUT_VARIABLES;

			List<String> runtimeList = new ArrayList<String>();
			for (RuntimeVariable var : RuntimeVariable.values()) {
				runtimeList.add(var.toString());
			}

			//no choice define, just dump all runtime variables
			if (property == null) {
				handleTimelineVariableHeaders(runtimeList);
				return runtimeList.toArray(new String[runtimeList.size()]);
			}

			//extract parameters
			String[] splitArray = property.split(",");
			List<String> usedList = new ArrayList<String>();
			for (int i = 0; i < splitArray.length; i++) {
				splitArray[i] = splitArray[i].trim();
				if (!splitArray[i].isEmpty()) {
					usedList.add(splitArray[i]);
				}
			}

			//check if parameters exist
			for (String param : usedList) {
				if (runtimeList.contains(param)) {
					continue;
				}
				if (Properties.hasParameter(param)) {
					continue;
				}

				throw new IllegalStateException("Parameter \"" + param
				        + "\" defined inside \"output_variables\" does not exist");
			}

			handleTimelineVariableHeaders(usedList);
			String[] usedArray = usedList.toArray(new String[usedList.size()]);
			return usedArray;
		}

		private void handleTimelineVariableHeaders(List<String> usedList) {
			/*
			 * now, handle timeline variables. For now, it is just coverage
			 */
			String covTimeline = RuntimeVariable.CoverageTimeline.toString();
			if (usedList.contains(covTimeline)) {
				usedList.remove(covTimeline);
				for (String suf : getTimelineHeaderSuffixes()) {
					usedList.add(covTimeline + suf);
				}
			}
		}

		/**
		 * Return value of a parameter, based on whether it is a EvoSuite
		 * property (e.g., population size), or something calculated at runtime
		 * (e.g. coverage)
		 * 
		 * @return
		 */
		private String getValueOfOutputVariable(String name) {
			//first check if it is an EvoSuite parameter (e.g. population size)
			if (Properties.hasParameter(name)) {
				try {
					return Properties.getStringValue(name);
				} catch (Exception e) {
					/*
					 * when we call this method, the parameters should had been already validated, and program aborted if necessary.
					 * An exception here is likely a bug
					 */
					logger.error("Error in getting value of parameter " + name, e);
					throw new Error(
					        "If this method inside EvoSuite is called, then it should never happen that the following exception is raised: "
					                + e.getMessage());
				}
			}

			if (isTimelineVariable(name)) {
				return timeLineValue(name);
			}

			//check if it is a runtime property of the search, e.g. coverage
			RuntimeVariable var = null;
			try {
				var = RuntimeVariable.valueOf(name);
			} catch (Exception e) {
				//note: we throw an Error as this protected method should never be called with wrong input / or on wrong internal state
				throw new Error("Parameter " + name + " does not exist");
			}

			//if it is not an EvoSuite property, it has to be a runtime one
			return getCSVvalue(var);
		}

		private String timeLineValue(String name) {

			long interval = Properties.TIMELINE_INTERVAL;

			int index = Integer.parseInt((name.split("_T"))[1]);
			long preferredTime = interval * index;

			assert this.timeStamps.size() == this.coverage_history.size();

			/*
			 * No data. Is it even possible? Maybe if population is too large,
			 * and budget was not enough to get even first generation
			 */
			if (timeStamps.size() == 0) {
				return "" + 0;
			}

			for (int i = 0; i < timeStamps.size(); i++) {
				/*
				 * find the first stamp that is after the time we would like to
				 * get coverage from
				 */
				long stamp = timeStamps.get(i);
				if (stamp < preferredTime) {
					continue;
				}

				if (i == 0) {
					/*
					 * it is the first element, so not much to do, we just use it as value
					 */
					return "" + coverage_history.get(i);
				}

				/*
				 * Now we interpolate the coverage, as usually we don't have the value for exact time we want
				 */
				long timeDelta = timeStamps.get(i) - timeStamps.get(i - 1);

				if (timeDelta > 0) {
					double covDelta = coverage_history.get(i)
					        - coverage_history.get(i - 1);
					double ratio = covDelta / timeDelta;

					long diff = preferredTime - timeStamps.get(i - 1);
					double cov = coverage_history.get(i - 1) + (diff * ratio);
					return "" + cov;
				}
			}

			/*
			 * No time stamp was higher. This might happen if coverage is 100% and we stop search.
			 * So just return last value seen
			 */

			return "" + coverage_history.get(coverage_history.size() - 1);
		}

		private boolean isTimelineVariable(String name) {
			if (name == null || name.isEmpty()) {
				return false;
			}
			if (name.startsWith(RuntimeVariable.CoverageTimeline.toString())) {
				return true;
			} else {
				return false;
			}
		}

		public String getCSVHeader() {
			StringBuilder r = new StringBuilder();

			String[] variables = getUsedVariables();
			if (variables.length > 0) {
				r.append(variables[0]);
			}

			for (int i = 1; i < variables.length; i++) {
				r.append(",");
				r.append(variables[i]);
			}

			return r.toString();
		}

		public String getCSVData() {
			StringBuilder r = new StringBuilder();

			String[] variables = getUsedVariables();
			if (variables.length > 0) {
				r.append(getValueOfOutputVariable(variables[0]));
			}

			for (int i = 1; i < variables.length; i++) {
				r.append(",");
				r.append(getValueOfOutputVariable(variables[i]));
			}

			return r.toString();
		}

		private String getCSVvalue(RuntimeVariable var) {

			PermissionStatistics pstats = PermissionStatistics.getInstance();

			switch (var) {
			case Class:
				return className;
			case Predicates:
				return "" + total_branches;
			case Total_Branches:
				return "" + (total_branches * 2);
			case Covered_Branches:
				return "" + covered_branches;
			case Total_Methods:
				return "" + total_methods;
			case Branchless_Methods:
				return "" + branchless_methods;
			case Covered_Methods:
				return "" + covered_methods;
			case Covered_Branchless_Methods:
				return "" + covered_branchless_methods;
			case Total_Goals:
				return "" + total_goals;
			case Covered_Goals:
				return "" + covered_goals;
			case Coverage:
				return "" + getCoverageDouble();
			case NumberOfGeneratedTestCases:
				return ""+ (tests!=null? tests.size() : 0);
			case NumberOfInputPoolObjects:
				String s = Properties.OBJECT_POOLS;
				if(s==null || s.isEmpty()){
					return ""+0;
				} else {
					return ""+s.split(File.pathSeparator).length;
				}
			case BranchCoverage:
				double cov = 0.0;

				if (total_branches + branchless_methods > 0)
					cov = (double) (covered_branches + covered_branchless_methods)
					        / (double) ((total_branches * 2) + branchless_methods);
				else
					cov = 1.0;

				if (!(cov >= 0 && cov <= 1)) {
					String message = "Invalid coverage: " + cov;
					message += " . covered_branches=" + covered_branches;
					message += " , covered_branchless_methods="
					        + covered_branchless_methods;
					message += " , total_branches*2=" + (total_branches * 2);
					message += " , branchless_methods=" + branchless_methods;
					logger.error(message);
					throw new AssertionError("Wrong coverage value: " + cov);
				}
				return "" + cov;
			case DefUseCoverage:
				if (coverageMap.containsKey("DEFUSE"))
					return "" + coverageMap.get("DEFUSE");
				else
					return "";
			case WeakMutationScore:
				if (coverageMap.containsKey("WEAKMUTATION"))
					return "" + coverageMap.get("WEAKMUTATION");
				else
					return "";
			case Creation_Time:
				return "" + (minimized_time - start_time);
			case Minimization_Time:
				return "" + (minimized_time - end_time);
			case Total_Time:
				return "" + (end_time - start_time);
			case Test_Execution_Time:
				return "" + testExecutionTime;
			case Goal_Computation_Time:
				return "" + goalComputationTime;
			case Result_Size:
				return "" + size_final;
			case Result_Length:
				return "" + length_final;
			case Minimized_Size:
				return "" + size_minimized;
			case Minimized_Length:
				return "" + length_minimized;
			case Chromosome_Length:
				return "" + chromosome_length;
			case Population_Size:
				return "" + population_size;
			case Random_Seed:
				return "" + seed;
			case Budget:
				return "" + Properties.SEARCH_BUDGET;
			case AllPermission:
				return "" + pstats.getNumAllPermission();
			case SecurityPermission:
				return "" + pstats.getNumSecurityPermission();
			case UnresolvedPermission:
				return "" + pstats.getNumUnresolvedPermission();
			case AWTPermission:
				return "" + pstats.getNumAWTPermission();
			case FilePermission:
				return "" + pstats.getNumFilePermission();
			case SerializablePermission:
				return "" + pstats.getNumSerializablePermission();
			case ReflectPermission:
				return "" + pstats.getNumReflectPermission();
			case RuntimePermission:
				return "" + pstats.getNumRuntimePermission();
			case NetPermission:
				return "" + pstats.getNumNetPermission();
			case SocketPermission:
				return "" + pstats.getNumSocketPermission();
			case SQLPermission:
				return "" + pstats.getNumSQLPermission();
			case PropertyPermission:
				return "" + pstats.getNumPropertyPermission();
			case LoggingPermission:
				return "" + pstats.getNumLoggingPermission();
			case SSLPermission:
				return "" + pstats.getNumSSLPermission();
			case AuthPermission:
				return "" + pstats.getNumAuthPermission();
			case AudioPermission:
				return "" + pstats.getNumAudioPermission();
			case OtherPermission:
				return "" + pstats.getNumOtherPermission();
			case Threads:
				return "" + pstats.getMaxThreads();
			case CoveredBranchesBitString:				
				return "" + goalCoverage;
			case MutationScore:
				return "" + mutationScore;
			case Explicit_MethodExceptions:
				return "" + explicitMethodExceptions;
			case Explicit_TypeExceptions:
				return "" + explicitTypeExceptions;
			case Implicit_MethodExceptions:
				return "" + implicitMethodExceptions;
			case Implicit_TypeExceptions:
				return "" + implicitTypeExceptions;
			case Error_Predicates:
				return "" + error_branches;
			case Error_Branches_Covered:
				return "" + error_branches_covered;
			case Error_Branchless_Methods:
				return "" + error_branchless_methods;
			case Error_Branchless_Methods_Covered:
				return "" + error_branchless_methods_covered;
			case AssertionContract:
				return ""
				        + FailingTestSet.getNumberOfViolations(AssertionErrorContract.class);
			case EqualsContract:
				return "" + FailingTestSet.getNumberOfViolations(EqualsContract.class);
			case EqualsHashcodeContract:
				return ""
				        + FailingTestSet.getNumberOfViolations(EqualsHashcodeContract.class);
			case EqualsNullContract:
				return ""
				        + FailingTestSet.getNumberOfViolations(EqualsNullContract.class);
			case EqualsSymmetricContract:
				return ""
				        + FailingTestSet.getNumberOfViolations(EqualsSymmetricContract.class);
			case HashCodeReturnsNormallyContract:
				return ""
				        + FailingTestSet.getNumberOfViolations(HashCodeReturnsNormallyContract.class);
			case JCrasherExceptionContract:
				return ""
				        + FailingTestSet.getNumberOfViolations(JCrasherExceptionContract.class);
			case NullPointerExceptionContract:
				return ""
				        + FailingTestSet.getNumberOfViolations(NullPointerExceptionContract.class);
			case ToStringReturnsNormallyContract:
				return ""
				        + FailingTestSet.getNumberOfViolations(ToStringReturnsNormallyContract.class);
			case UndeclaredExceptionContract:
				return ""
				        + FailingTestSet.getNumberOfViolations(UndeclaredExceptionContract.class);
			case Contract_Violations:
				return "" + FailingTestSet.getNumberOfViolations();
			case Unique_Violations:
				return "" + FailingTestSet.getNumberOfUniqueViolations();
			case Data_File:
				return getCSVFilepath();
			case Statements_Executed:
				return "" + result_statements_executed;
			case Definitions:
				//if (Properties.CRITERION == Properties.Criterion.DEFUSE
				//        || Properties.ANALYSIS_CRITERIA.toUpperCase().contains("DEFUSE"))
				//	return "" + DefUsePool.getDefCounter();
				//else
					return ""+numDefinitions;
			case Uses:
				//if (Properties.CRITERION == Properties.Criterion.DEFUSE
				//        || Properties.ANALYSIS_CRITERIA.toUpperCase().contains("DEFUSE"))
				//	return "" + DefUsePool.getUseCounter();
				//else
					return "" + numUses;
			case DefUsePairs:
				//if (Properties.CRITERION == Properties.Criterion.DEFUSE
				//        || Properties.ANALYSIS_CRITERIA.toUpperCase().contains("DEFUSE"))
				//	return "" + DefUseCoverageFactory.getDUGoals().size();
				//else
					return ""+numDefUsePairs;
			case IntraMethodPairs:
				//if (Properties.CRITERION == Properties.Criterion.DEFUSE
				//        || Properties.ANALYSIS_CRITERIA.toUpperCase().contains("DEFUSE"))
				//	return "" + DefUseCoverageFactory.getIntraMethodGoalsCount();
				//else
					return ""+numIntraMethodPairs;
			case InterMethodPairs:
				//if (Properties.CRITERION == Properties.Criterion.DEFUSE
				//        || Properties.ANALYSIS_CRITERIA.toUpperCase().contains("DEFUSE"))
				//	return "" + DefUseCoverageFactory.getInterMethodGoalsCount();
				//else
					return "" + numInterMethodPairs;
			case IntraClassPairs:
				//if (Properties.CRITERION == Properties.Criterion.DEFUSE
				//        || Properties.ANALYSIS_CRITERIA.toUpperCase().contains("DEFUSE"))
				//	return "" + DefUseCoverageFactory.getIntraClassGoalsCount();
				//else
					return ""+numIntraClassPairs;
			case ParameterPairs:
				//if (Properties.CRITERION == Properties.Criterion.DEFUSE
				//        || Properties.ANALYSIS_CRITERIA.toUpperCase().contains("DEFUSE"))
				//	return "" + DefUseCoverageFactory.getParamGoalsCount();
				//else
					return ""+numParameterPairs;
			case CoveredIntraMethodPairs:
				return "" + coveredIntraMethodPairs;
			case CoveredInterMethodPairs:
				return "" + coveredInterMethodPairs;
			case CoveredIntraClassPairs:
				return "" + coveredIntraClassPairs;
			case CoveredParameterPairs:
				return "" + coveredParameterPairs;
			case CoveredAliasIntraMethodPairs:
				return "" + coveredAliasIntraMethodPairs;
			case CoveredAliasInterMethodPairs:
				return "" + coveredAliasInterMethodPairs;
			case CoveredAliasIntraClassPairs:
				return "" + coveredAliasIntraClassPairs;
			case CoveredAliasParameterPairs:
				return "" + coveredAliasParameterPairs;
			case AliasingIntraMethodPairs:
				return "" + aliasingIntraMethodPairs;
			case AliasingInterMethodPairs:
				return "" + aliasingInterMethodPairs;
			case AliasingIntraClassPairs:
				return "" + aliasingIntraClassPairs;
			case AliasingParameterPairs:
				return "" + aliasingParameterPairs;
			case CarvedTests:
				return ""+JUnitTestCarvedChromosomeFactory.getTotalNumberOfTestsCarved();
			case CarvedCoverage:
				return ""+JUnitTestCarvedChromosomeFactory.getCoverageOfCarvedTests();
			case HadUnstableTests:
				return ""+hadUnstableTests;
			default:
				break;

			}

			/*
			 * note, this should never happen. If it does, then it is a bug in EvoSuite. But instead of throwing an exception,
			 * we just log it. In this way, we still get the CSV file for debugging 
			 */
			logger.error("No mapping defined for variable " + var.toString());

			return "-1";
		}

		public String getCSVFilepath() {
			return getReportDir().getAbsolutePath() + File.separator + getCSVFileName();
		}

		public String getCSVFileName() {
			return "data" + File.separator + "statistics_" + className + "-" + id
			        + ".csv.gz";
		}

		public String getExceptionFilepath() {
			return getReportDir().getAbsolutePath() + "/data/exceptions_" + className
			        + "-" + id + ".csv";
		}

		public String getCoverage() {
			if (total_goals == 0)
				return "100.00%";
			else
				return String.format("%.2f",
				                     (100.0 * covered_goals / (1.0 * total_goals))).replaceAll(",",
				                                                                               ".")
				        + "%";
		}

		public double getCoverageDouble() {
			if (total_goals == 0)
				return 1.0;
			else
				return covered_goals / (1.0 * total_goals);
		}
	};

	protected List<StatisticEntry> statistics = new ArrayList<StatisticEntry>();

	/** Constant <code>html_analyzer</code> */
	protected static final HtmlAnalyzer html_analyzer = new HtmlAnalyzer();

	/**
	 * <p>
	 * writeIntegerChart
	 * </p>
	 * 
	 * @param values
	 *            a {@link java.util.List} object.
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param title
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String writeIntegerChart(List<Integer> values, String className,
	        String title) {
		File file = new File(getReportDir().getAbsolutePath() + "/img/statistics_"
		        + title + "_" + className + ".png");
		JavaPlot plot = new JavaPlot();
		GNUPlotTerminal terminal = new FileTerminal("png", getReportDir()
		        + "/img/statistics_" + title + "_" + className + ".png");
		plot.setTerminal(terminal);

		// plot.set("size", "1, 0.5");
		plot.set("xlabel", "\"Generation\"");
		plot.set("ylabel", "\"" + title + "\"");
		// plot.set("xrange", "[0:]");
		// plot.set("yrange", "[0:]");
		plot.set("autoscale", "ymax");

		int[][] data = new int[values.size()][2];
		for (int i = 0; i < values.size(); i++) {
			data[i][0] = i;
			data[i][1] = values.get(i);
		}

		plot.addPlot(data);
		PlotStyle stl = ((AbstractPlot) plot.getPlots().get(0)).getPlotStyle();
		stl.setStyle(Style.LINESPOINTS);
		plot.setKey(JavaPlot.Key.OFF);
		plot.plot();

		return file.getName();
	}

	/**
	 * <p>
	 * writeDoubleChart
	 * </p>
	 * 
	 * @param values
	 *            a {@link java.util.List} object.
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param title
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String writeDoubleChart(List<Double> values, String className, String title) {

		File file = new File(getReportDir().getAbsolutePath() + "/img/statistics_"
		        + title + "_" + className + ".png");
		JavaPlot plot = new JavaPlot();
		GNUPlotTerminal terminal = new FileTerminal("png", getReportDir()
		        + "/img/statistics_" + title + "_" + className + ".png");
		plot.setTerminal(terminal);
		//plot.set("size", "1, 0.8");

		plot.set("xlabel", "\"Generation\"");
		plot.set("ylabel", "\"" + title + "\"");
		// plot.set("xrange", "[0:]");
		// plot.set("yrange", "[0:]");
		plot.set("autoscale", "ymax");

		double[][] data = new double[values.size()][2];
		for (int i = 0; i < values.size(); i++) {
			data[i][0] = i;
			data[i][1] = values.get(i);
		}
		plot.addPlot(data);

		PlotStyle stl = ((AbstractPlot) plot.getPlots().get(0)).getPlotStyle();
		stl.setStyle(Style.LINESPOINTS);
		plot.setKey(JavaPlot.Key.OFF);

		plot.plot();

		return file.getName();
	}

	/**
	 * HTML header
	 * 
	 * @param buffer
	 *            a {@link java.lang.StringBuffer} object.
	 * @param title
	 *            a {@link java.lang.String} object.
	 */
	public static void writeHTMLHeader(StringBuffer buffer, String title) {
		buffer.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">\n");
		buffer.append("<html>\n");
		buffer.append("<head>\n");
		buffer.append("<title>\n");
		buffer.append(title);
		buffer.append("\n</title>\n");

		buffer.append("<link href=\"files/prettify.css\" type=\"text/css\" rel=\"stylesheet\" />\n");
		buffer.append("<link href=\"files/style.css\" rel=\"stylesheet\" type=\"text/css\" media=\"screen\"/>\n");
		buffer.append("<script type=\"text/javascript\" src=\"files/prettify.js\"></script>\n");
		buffer.append("<script type=\"text/javascript\" src=\"files/jquery.js\"></script>\n");
		buffer.append("<script type=\"text/javascript\" src=\"files/foldButton.js\"></script>\n");
		buffer.append("<script type=\"text/javascript\">\n");
		buffer.append("  $(document).ready(function() {\n");
		//buffer.append("    $('div.tests').foldButton({'closedText':'open TITLE' });\n");
		//buffer.append("    $('div.source').foldButton({'closedText':'open TITLE' });\n");
		//buffer.append("    $('div.statistics').foldButton({'closedText':'open TITLE' });\n");
		buffer.append("    $('H2#tests').foldButton();\n");
		buffer.append("    $('H2#source').foldButton();\n");
		buffer.append("    $('H2#parameters').foldButton();\n");
		buffer.append("  });");
		buffer.append("</script>\n");
		buffer.append("<link href=\"files/foldButton.css\" rel=\"stylesheet\" type=\"text/css\">\n");
		buffer.append("</head>\n");
		buffer.append("<body onload=\"prettyPrint()\">\n");
		buffer.append("<div id=\"wrapper\">\n");
		buffer.append("<img src=\"files/evosuite.png\" height=\"40\"/>\n");
	}

	/**
	 * HTML footer
	 * 
	 * @param buffer
	 *            a {@link java.lang.StringBuffer} object.
	 */
	public static void writeHTMLFooter(StringBuffer buffer) {
		buffer.append("</div>\n");
		buffer.append("</body>\n");
		buffer.append("</html>\n");
	}

	/**
	 * <p>
	 * writeCSVData
	 * </p>
	 * 
	 * @param filename
	 *            a {@link java.lang.String} object.
	 * @param data
	 *            a {@link java.util.List} object.
	 */
	protected void writeCSVData(String filename, List<?>... data) {
		try {
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(filename,
			        false));
			out.putNextEntry(new ZipEntry(filename.replace(".gz", "")));

			//BufferedWriter out = new BufferedWriter(new FileWriter(filename, true));
			int length = Integer.MAX_VALUE;

			out.write("Generation,Fitness,Coverage,Size,Length,AverageLength,Evaluations,Tests,Statements,Time\n".getBytes());
			for (List<?> d : data) {
				length = Math.min(length, d.size());
			}
			for (int i = 0; i < length; i++) {
				out.write(("" + i).getBytes());
				for (List<?> d : data) {
					out.write(("," + d.get(i)).getBytes());
				}
				out.write("\n".getBytes());
			}
			out.close();
		} catch (IOException e) {
			logger.info("Exception while writing CSV data: " + e);
		}
	}

	/**
	 * <p>
	 * writeExceptionData
	 * </p>
	 * 
	 * @param filename
	 *            a {@link java.lang.String} object.
	 * @param exceptions
	 *            a {@link java.util.Map} object.
	 */
	protected void writeExceptionData(String filename,
	        Map<String, Set<Class<?>>> implicitExceptions,
	        Map<String, Set<Class<?>>> explicitExceptions) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename, true));
			out.write("Method,Exception,Explicit\n");
			for (String key : explicitExceptions.keySet()) {
				for (Class<?> exception : explicitExceptions.get(key)) {
					out.write(key + "," + exception.getCanonicalName() + ",1\n");
				}
			}
			for (String key : implicitExceptions.keySet()) {
				for (Class<?> exception : implicitExceptions.get(key)) {
					out.write(key + "," + exception.getCanonicalName() + ",0\n");
				}
			}
			out.close();
		} catch (IOException e) {
			logger.info("Exception while writing exception data: " + e);
		}
	}

	/**
	 * <p>
	 * getNumber
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @return a int.
	 */
	protected int getNumber(final String className) {
		int num = 0;
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("statistics_" + className)
				        && (name.endsWith(".csv.gz") || name.endsWith(".csv")); // && !dir.isDirectory();
			}
		};
		List<String> filenames = new ArrayList<String>();

		File[] files = (new File(getReportDir().getAbsolutePath() + "/data")).listFiles(filter);
		if (files != null) {
			for (File f : files)
				filenames.add(f.getName());
			while (filenames.contains("statistics_" + className + "-" + num + ".csv")
			        || filenames.contains("statistics_" + className + "-" + num
			                + ".csv.gz"))
				num++;
		}

		return num;
	}

	/**
	 * Write a file for a particular run
	 * 
	 * @param run
	 *            a {@link org.evosuite.utils.ReportGenerator.StatisticEntry}
	 *            object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String writeRunPage(StatisticEntry run) {

		StringBuffer sb = new StringBuffer();
		writeHTMLHeader(sb, run.className);

		sb.append("<div id=\"header\"><div id=\"logo\">");
		sb.append("<h2>");
		sb.append(run.className);
		sb.append(": ");
		sb.append(String.format("%.2f", 100.0 * run.covered_goals / run.total_goals));
		sb.append("%");
		sb.append("</h2></div></div>\n");
		sb.append("<p><a href=\"../report-generation.html\">Overview</a></p>\n");

		writeResultTable(sb, run);
		// writeMutationTable(sb);
		sb.append("<div id=\"page\"><div id=\"page-bgtop\"><div id=\"page-bgbtm\"><div id=\"content\">");
		sb.append("<div id=\"post\">");

		// Resulting test case
		sb.append("<h2 class=title>Test suite</h2>\n");
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
		sb.append("</div>");
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
			if (run.fitness_history.isEmpty()) {
				sb.append("<h2>No fitness history</h2>\n");
			} else {
				String filename = writeDoubleChart(run.fitness_history, run.className
				        + "-" + run.id, "Fitness");
				sb.append("<h2>Fitness</h2>\n");
				sb.append("<p>");
				sb.append("<img src=\"../img/");
				sb.append(filename);
				sb.append("\">");
				sb.append("</p>\n");
			}

			// Chart of size
			if (run.size_history.isEmpty()) {
				sb.append("<h2>No size history</h2>\n");
			} else {
				String filename = writeIntegerChart(run.size_history, run.className + "-"
				        + run.id, "Size");
				sb.append("<h2>Size</h2>\n");
				sb.append("<p>");
				sb.append("<img src=\"../img/");
				sb.append(filename);
				sb.append("\">");
				sb.append("</p>\n");
			}

			// Chart of length
			if (run.length_history.isEmpty()) {
				sb.append("<h2>No length history</h2>\n");
			} else {
				String filename = writeIntegerChart(run.length_history, run.className
				        + "-" + run.id, "Length");
				sb.append("<h2>Length</h2>\n");
				sb.append("<p>");
				sb.append("<img src=\"../img/");
				sb.append(filename);
				sb.append("\">");
				sb.append("</p>\n");
			}

			// Chart of average length
			if (run.average_length_history.isEmpty()) {
				sb.append("<h2>No average length history</h2>\n");
			} else {
				String filename = writeDoubleChart(run.average_length_history,
				                                   run.className + "-" + run.id, "Length");
				sb.append("<h2>Average Length</h2>\n");
				sb.append("<p>");
				sb.append("<img src=\"../img/");
				sb.append(filename);
				sb.append("\">");
				sb.append("</p>\n");
			}
		}
		sb.append("</div>");
		sb.append("<div id=\"post\">");

		// Source code
		try {
			Iterable<String> source = html_analyzer.getClassContent(run.className);
			sb.append("<h2 class=title>Source Code</h2>\n");
			sb.append("<p>");
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

			sb.append("</p>\n");
		} catch (Exception e) {
			// Don't display source if there is an error
		}
		sb.append("</div>");
		sb.append("<div id=\"post\">");

		writeParameterTable(sb, run);
		sb.append("</div>");

		writeHTMLFooter(sb);

		String filename = "report-" + run.className + "-" + run.id + ".html";
		File file = new File(getReportDir().getAbsolutePath() + "/html/" + filename);
		Utils.writeFile(sb.toString(), file);
		// return file.getAbsolutePath();
		return filename;
	}

	/**
	 * Write some overall stats
	 * 
	 * @param buffer
	 *            a {@link java.lang.StringBuffer} object.
	 * @param entry
	 *            a {@link org.evosuite.utils.ReportGenerator.StatisticEntry}
	 *            object.
	 */
	protected void writeParameterTable(StringBuffer buffer, StatisticEntry entry) {
		buffer.append("<h2 id=parameters>EvoSuite Parameters</h2>\n");
		buffer.append("<div class=statistics><ul>\n");
		for (String key : Properties.getParameters()) {
			try {
				buffer.append("<li>" + key + ": " + Properties.getStringValue(key) + "\n"); // TODO
			} catch (NoSuchParameterException e) {
				logger.error(e.getMessage(),e);
			} catch (IllegalArgumentException e) {
				logger.error(e.getMessage(),e);
			} catch (IllegalAccessException e) {
				logger.error(e.getMessage(),e);
			}
		}
		buffer.append("</ul></div>\n");

	}

	/**
	 * Write some overall stats
	 * 
	 * @param buffer
	 *            a {@link java.lang.StringBuffer} object.
	 * @param entry
	 *            a {@link org.evosuite.utils.ReportGenerator.StatisticEntry}
	 *            object.
	 */
	protected void writeResultTable(StringBuffer buffer, StatisticEntry entry) {

		//buffer.append("<h2>Statistics</h2>\n");
		buffer.append("<ul>\n");

		buffer.append("<li>");
		buffer.append(entry.result_fitness_evaluations);
		buffer.append(" fitness evaluations, ");
		buffer.append(entry.age);
		buffer.append(" generations, ");
		buffer.append(entry.result_statements_executed);
		buffer.append(" statements, ");
		buffer.append(entry.result_tests_executed);
		buffer.append(" tests.\n");

		long duration_GA = (entry.end_time - entry.start_time) / 1000;
		long duration_MI = (entry.minimized_time - entry.end_time) / 1000;
		long duration_TO = (entry.minimized_time - entry.start_time) / 1000;

		buffer.append("<li>Time: "
		        + String.format("%d:%02d:%02d", duration_TO / 3600,
		                        (duration_TO % 3600) / 60, (duration_TO % 60)));

		buffer.append("(Search: "
		        + String.format("%d:%02d:%02d", duration_GA / 3600,
		                        (duration_GA % 3600) / 60, (duration_GA % 60)) + ", ");
		buffer.append("minimization: "
		        + String.format("%d:%02d:%02d", duration_MI / 3600,
		                        (duration_MI % 3600) / 60, (duration_MI % 60)) + ")\n");

		buffer.append("<li>Coverage: " + entry.covered_branches + "/"
		        + (2 * entry.total_branches) + " branches, ");
		buffer.append(entry.covered_methods + "/" + entry.total_methods + " methods, ");
		buffer.append(entry.covered_goals + "/" + entry.total_goals + " total goals\n");
		buffer.append("<li>Mutation score: "
		        + NumberFormat.getPercentInstance().format(entry.mutationScore) + "\n");

		buffer.append("</ul>\n");
	}

	/**
	 * The big table of results
	 * 
	 * @param buffer
	 *            a {@link java.lang.StringBuffer} object.
	 */
	protected void writeRunTable(StringBuffer buffer) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);

		for (StatisticEntry entry : statistics) {
			buffer.append("<tr>");
			// buffer.append("<td>" + entry.id + "</td>");
			buffer.append("<td>");
			buffer.append(sdf.format(new Date(entry.start_time)));
			buffer.append("</td>");
			long duration_TO = (entry.minimized_time - entry.start_time) / 1000;
			buffer.append("<td>");
			buffer.append(String.format("%d:%02d:%02d", duration_TO / 3600,
			                            (duration_TO % 3600) / 60, (duration_TO % 60)));
			buffer.append("</td>");
			buffer.append("<td>");
			buffer.append(entry.getCoverage());
			buffer.append("</td>");
			buffer.append("<td><a href=\"html/");
			String filename = writeRunPage(entry);
			buffer.append(filename);
			buffer.append("\">");
			buffer.append(entry.className);
			// buffer.append("</a></td>");
			// buffer.append("<td><a href=\"");
			// buffer.append(entry.getCSVFileName());
			// buffer.append("\">CSV</a></td>");
			buffer.append("</tr>\n");
		}
		//  
		buffer.append("<!-- EVOSUITE INSERTION POINT -->\n");
		buffer.append("<tr class=\"top\"><td colspan=\"3\">&nbsp;<td></tr>\n");
		buffer.append("</table>");
	}

	/**
	 * <p>
	 * writeCSV
	 * </p>
	 */
	public void writeCSV() {
		if (statistics.isEmpty())
			return;

		StatisticEntry entry = statistics.get(statistics.size() - 1);
		try {
			File f = new File(getReportDir().getAbsolutePath() + "/statistics.csv");
			logger.info("Writing CSV to "+f.getAbsolutePath());
			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
			if (f.length() == 0L) {
				out.write(entry.getCSVHeader() + "\n");
			}
			out.write(entry.getCSVData() + "\n");
			out.close();

		} catch (IOException e) {
			logger.warn("Error while writing statistics: " + e.getMessage());
		}

		if (Properties.SAVE_ALL_DATA) {
		    if (ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.EXCEPTION)) {
				writeExceptionData(entry.getExceptionFilepath(),
				                   entry.implicitExceptions, entry.explicitExceptions);
			}
			writeCSVData(entry.getCSVFilepath(), entry.fitness_history,
			             entry.coverage_history, entry.size_history,
			             entry.length_history, entry.average_length_history,
			             entry.fitness_evaluations, entry.tests_executed,
			             entry.statements_executed, entry.timeStamps);
		}
	}

	/**
	 * <p>
	 * copyFile
	 * </p>
	 * 
	 * @param src
	 *            a {@link java.net.URL} object.
	 * @param dest
	 *            a {@link java.io.File} object.
	 */
	public static void copyFile(URL src, File dest) {
		try {
			InputStream in;
			in = src.openStream();
			OutputStream out = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
	}

	/**
	 * <p>
	 * copyFile
	 * </p>
	 * 
	 * @param name
	 *            a {@link java.lang.String} object.
	 */
	public static void copyFile(String name) {
		URL systemResource = ClassLoader.getSystemResource("report/" + name);
		logger.debug("Copying from resource: " + systemResource);
		copyFile(systemResource, new File(getReportDir(), "files/" + name));
		copyFile(systemResource, new File(getReportDir().getAbsolutePath()
		        + "/html/files/" + name));
	}

	/**
	 * Write an HTML report
	 */
	public void writeReport() {
		if (!Properties.HTML)
			return;

		if (statistics.isEmpty())
			return;

		new File(getReportDir().getAbsolutePath() + "/img").mkdirs();
		new File(getReportDir().getAbsolutePath() + "/html/files/").mkdirs();
		new File(getReportDir().getAbsolutePath() + "/data/").mkdirs();
		new File(getReportDir().getAbsolutePath() + "/files/").mkdirs();

		copyFile("prettify.js");
		copyFile("prettify.css");
		copyFile("style.css");
		copyFile("foldButton.js");
		copyFile("foldButton.css");
		copyFile("jquery.js");
		copyFile("detected.png");
		copyFile("not_detected.png");
		copyFile("img01.jpg");
		copyFile("img02.jpg");
		copyFile("img03.jpg");
		copyFile("img04.png");
		copyFile("evosuite.png");
		File file = new File(getReportDir(), "report-generation.html");
		StringBuffer report = new StringBuffer();

		if (file.exists()) {
			List<String> lines = Utils.readFile(file);
			for (String line : lines) {
				if (line.contains("<!-- EVOSUITE INSERTION POINT -->")) {
					break;
				}
				report.append(line);
			}
		} else {

			writeHTMLHeader(report, Properties.PROJECT_PREFIX);
			report.append("<div id=\"header\"><div id=\"logo\">");
			/*
			if (!Properties.PROJECT_PREFIX.isEmpty()) {
				report.append("<h1 class=title>EvoSuite: " + Properties.PROJECT_PREFIX
				        + "</h1>\n");
			}
			*/
			report.append("</div><br></div>");
			try {
				report.append("Run on "
				        + java.net.InetAddress.getLocalHost().getHostName() + "\n");
			} catch (Exception e) {
			}

			report.append("<div id=\"page\"><div id=\"page-bgtop\"><div id=\"page-bgbtm\"><div id=\"content\">");
			report.append("<div id=\"post\">");
			report.append("<h2 class=\"title\">Test generation runs:</h2>\n");
			report.append("<div style=\"clear: both;\">&nbsp;</div><div class=\"entry\">");
			report.append("<table cellspacing=0>"); // border=0 cellspacing=0 cellpadding=3>");
			report.append("<tr class=\"top bottom\">");
			// report.append("<td>Run</td>");
			report.append("<td>Date</td>");
			report.append("<td>Time</td>");
			report.append("<td>Coverage</td>");
			report.append("<td>Class</td>");
			// report.append("<td></td>");
			report.append("</tr>\n");
		}
		writeRunTable(report);
		report.append("</div></div></div></div></div></div>");

		writeHTMLFooter(report);

		Utils.writeFile(report.toString(), file);
	}

	/**
	 * <p>
	 * getCoveredLines
	 * </p>
	 * 
	 * @param trace
	 *            a {@link org.evosuite.testcase.ExecutionTrace} object.
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Integer> getCoveredLines(ExecutionTrace trace, String className) {
		return trace.getCoveredLines(className);
	}

	/**
	 * <p>
	 * executeTest
	 * </p>
	 * 
	 * @param testChromosome
	 *            a {@link org.evosuite.testcase.TestChromosome} object.
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.testcase.ExecutionResult} object.
	 */
	public ExecutionResult executeTest(TestChromosome testChromosome, String className) {
		ExecutionResult result = testChromosome.getLastExecutionResult();

		if (result == null || testChromosome.isChanged()) {
			try {
				if (logger.isTraceEnabled()) {
					logger.trace(testChromosome.getTestCase().toCode());
				}
				TestCaseExecutor executor = TestCaseExecutor.getInstance();
				result = executor.execute(testChromosome.getTestCase());

			} catch (Exception e) {
				logger.error("TG: Exception caught: " + e.getMessage(), e);
				try {
					Thread.sleep(1000);
					result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
				} catch (Exception e1) {
					logger.error("Cannot set trace in test case with exception. Going to kill client",
					             e1);
					throw new Error(e1);
				}
			}
		}

		StatisticEntry entry = statistics.get(statistics.size() - 1);
		entry.results.put(testChromosome.getTestCase(),
		                  result.getCopyOfExceptionMapping());

		return result;
	}

	/**
	 * <p>
	 * minimized
	 * </p>
	 * 
	 * @param result
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 */
	public abstract void minimized(Chromosome result);

	/**
	 * <p>
	 * makeDirs
	 * </p>
	 */
	protected void makeDirs() {
		getReportDir().mkdirs();
		if (Properties.SAVE_ALL_DATA)
			(new File(getReportDir().getAbsolutePath() + "/data")).mkdir();
		if (Properties.PLOT)
			(new File(getReportDir().getAbsolutePath() + "/img")).mkdir();
		if (Properties.HTML)
			(new File(getReportDir().getAbsolutePath() + "/html")).mkdir();
	}

	/** {@inheritDoc} */
	@Override
	public void searchStarted(GeneticAlgorithm<?> algorithm) {
		StatisticEntry entry = new StatisticEntry();
		entry.className = Properties.TARGET_CLASS;
		entry.id = getNumber(entry.className);

		entry.start_time = System.currentTimeMillis();
		entry.population_size = Properties.POPULATION;
		entry.chromosome_length = Properties.CHROMOSOME_LENGTH;
		entry.seed = Randomness.getSeed();
		statistics.add(entry);
	}

	/** {@inheritDoc} */
	@Override
	public void iteration(GeneticAlgorithm<?> algorithm) {
		StatisticEntry entry = statistics.get(statistics.size() - 1);
		Chromosome best = algorithm.getBestIndividual();
		entry.fitness_history.add(best.getFitness());
		entry.size_history.add(best.size());

		double average = 0.0;
		for (Chromosome individual : algorithm.getPopulation()) {
			average += individual.size();
		}

		entry.average_length_history.add(average / algorithm.getPopulation().size());

		// TODO: Need to get data of average size in here - how? Pass population
		// as parameter?
		entry.age++;
	}

	/** {@inheritDoc} */
	@Override
	public void fitnessEvaluation(Chromosome result) {
		StatisticEntry entry = statistics.get(statistics.size() - 1);
		entry.result_fitness_evaluations++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.ga.SearchListener#mutation(org.evosuite
	 * .ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub

	}
}
