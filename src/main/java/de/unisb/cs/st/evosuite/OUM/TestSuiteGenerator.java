/**
 * 
 */
package de.unisb.cs.st.evosuite.OUM;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageGoal;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageTestFitness;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.ga.CrossOverFunction;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.MuPlusLambdaGA;
import de.unisb.cs.st.evosuite.ga.OnePlusOneEA;
import de.unisb.cs.st.evosuite.ga.Randomness;
import de.unisb.cs.st.evosuite.ga.RankSelection;
import de.unisb.cs.st.evosuite.ga.SelectionFunction;
import de.unisb.cs.st.evosuite.ga.SinglePointRelativeCrossOver;
import de.unisb.cs.st.evosuite.ga.StandardGA;
import de.unisb.cs.st.evosuite.ga.SteadyStateGA;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxFitnessEvaluationsStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxGenerationStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxTimeStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.ZeroFitnessStoppingCondition;
import de.unisb.cs.st.evosuite.junit.JUnitTestSuite;
import de.unisb.cs.st.evosuite.mutation.MutationStatistics;
import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
import de.unisb.cs.st.evosuite.testcase.DefaultTestCase;
import de.unisb.cs.st.evosuite.testcase.DefaultTestFactory;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.FieldStatement;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.RandomLengthTestFactory;
import de.unisb.cs.st.evosuite.testcase.RelativeLengthBloatControl;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testcase.TestCaseMinimizer;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testcase.VariableReference;
import de.unisb.cs.st.evosuite.testsuite.SearchStatistics;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosomeFactory;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteReplacementFunction;

/**
 * @author Gordon Fraser
 * 
 */
public class TestSuiteGenerator {

	private static Logger logger = Logger.getLogger(TestSuiteGenerator.class);

	boolean minimize = Properties.getPropertyOrDefault("minimize", true);

	private final StoppingCondition max_gen = new MaxGenerationStoppingCondition();
	private final StoppingCondition max_fitness = new MaxFitnessEvaluationsStoppingCondition();
	private final StoppingCondition max_time = new MaxTimeStoppingCondition();
	private final StoppingCondition max_tests = new MaxTestsStoppingCondition();
	private final MaxStatementsStoppingCondition max_statements = new MaxStatementsStoppingCondition();
	private final StoppingCondition zero_fitness = new ZeroFitnessStoppingCondition();
	private int num_experiments = 0;

	private final static boolean PARENTS_LENGTH = Properties.getPropertyOrDefault("check_parents_length",
	                                                                              true);

	private void compareAgainstJUnit(TestSuiteChromosome suite) {
		JUnitTestSuite junit = new JUnitTestSuite();
		String name = Properties.getProperty("junit_tests");
		if (name == null)
			return;

		junit.runSuite(name);

		Set<String> junit_covered_true = junit.getTrueCoveredBranches();
		Set<String> junit_covered_false = junit.getFalseCoveredBranches();
		Set<String> junit_covered_methods = junit.getCoveredMethods();

		junit.runSuite(suite);

		Set<String> generated_covered_true = junit.getTrueCoveredBranches();
		Set<String> generated_covered_false = junit.getFalseCoveredBranches();
		Set<String> generated_covered_methods = junit.getCoveredMethods();

		Set<String> common_covered_true = junit.getTrueCoveredBranches();
		Set<String> common_covered_false = junit.getFalseCoveredBranches();
		Set<String> common_covered_methods = junit.getCoveredMethods();

		int coverage_junit = 0;
		int coverage_generated = 0;
		int junit_not_generated = 0;
		int generated_not_junit = 0;
		int common = 0;
		for (String branch : junit_covered_true) {
			coverage_junit++;
			if (!generated_covered_true.contains(branch))
				junit_not_generated++;
			else
				common_covered_true.add(branch);

		}
		for (String branch : junit_covered_false) {
			coverage_junit++;
			if (!generated_covered_false.contains(branch))
				junit_not_generated++;
			else
				common_covered_false.add(branch);
		}
		for (String branch : junit_covered_methods) {
			coverage_junit++;
			if (!generated_covered_methods.contains(branch))
				junit_not_generated++;
			else
				common_covered_methods.add(branch);
		}
		for (String branch : generated_covered_true) {
			coverage_generated++;
			if (!junit_covered_true.contains(branch))
				generated_not_junit++;
			else
				common_covered_true.add(branch);

		}
		for (String branch : generated_covered_false) {
			coverage_generated++;
			if (!junit_covered_false.contains(branch))
				generated_not_junit++;
			else
				common_covered_false.add(branch);

		}
		for (String branch : generated_covered_methods) {
			coverage_generated++;
			if (!junit_covered_methods.contains(branch))
				generated_not_junit++;
			else
				common_covered_methods.add(branch);
		}
		common = common_covered_true.size();
		common = common_covered_false.size();
		common = common_covered_methods.size();
		System.out.println("Branches covered by JUnit but not by generated: "
		        + junit_not_generated);
		System.out.println("Branches covered by generated but not by JUnit: "
		        + generated_not_junit);

		String P = "1.0";
		String filename = Properties.getProperty("tests");
		if (filename.contains("_00.xml"))
			P = "0.0";
		else if (filename.contains("_025.xml"))
			P = "0.25";
		else if (filename.contains("_05.xml"))
			P = "0.5";
		else if (filename.contains("_075.xml"))
			P = "0.75";
		else if (filename.contains("_10.xml"))
			P = "1.0";
		System.out.println("CSV," + Properties.TARGET_CLASS + "," + P + ","
		        + coverage_junit + "," + coverage_generated + "," + common + ","
		        + junit_not_generated + "," + generated_not_junit);

		/*		
				try {
					FileWriter writer = new FileWriter(Properties.OUTPUT_DIR+"/junit_comparison.csv", true);
					BufferedWriter w = new BufferedWriter(writer);
					String factory = Properties.getPropertyOrDefault("test_factory", "Random");
					
					w.write(factory +","+ Randomness.getInstance().getSeed()+","+Properties.TARGET_CLASS+","+suite.length()+","+100.0*coverage_junit/total+","+100.0*coverage_generated/total+","+common+","+junit_not_generated+","+generated_not_junit+"\n");
					w.close();
				} catch(IOException e) {
					
				}
				*/
	}

	private double getCoverage(TestSuiteChromosome suite) {
		JUnitTestSuite junit = new JUnitTestSuite();

		junit.runSuite(suite);

		Set<String> generated_covered_true = junit.getTrueCoveredBranches();
		Set<String> generated_covered_false = junit.getFalseCoveredBranches();
		Set<String> generated_covered_methods = junit.getCoveredMethods();
		int total = CFGMethodAdapter.methods.size() + BranchPool.getBranchCounter() * 2;
		int coverage_generated = generated_covered_true.size()
		        + generated_covered_false.size() + generated_covered_methods.size();
		return 100.0 * coverage_generated / total;
	}

	public void experiment1() {
		ExecutionTrace.trace_calls = true;

		OUMTestFactory oum_factory = OUMTestFactory.getInstance();
		DefaultTestFactory test_factory = DefaultTestFactory.getInstance();
		logger.info("Experiment 1");
		num_experiments = Properties.getPropertyOrDefault("num_experiments", 1);

		Properties.CHROMOSOME_LENGTH = 10000;
		Properties.setProperty("test_factory", "OUM");
		for (int num = 0; num < num_experiments; num++) {
			logger.info("Experiment run: " + num);
			// Generate test
			TestCase test = new DefaultTestCase();
			while (test.size() < Properties.CHROMOSOME_LENGTH) {
				oum_factory.appendRandomCall(test);
			}
			//logger.info("OUM test");
			//logger.info(test.toCode());
			// Measure coverage
			TestSuiteChromosome suite = new TestSuiteChromosome();
			suite.addTest(test);
			compareAgainstJUnit(suite);
		}
		Properties.setProperty("test_factory", "Random");
		for (int num = 0; num < num_experiments; num++) {
			logger.info("Experiment run: " + num);
			// Generate test
			TestCase test = new DefaultTestCase();
			while (test.size() < Properties.CHROMOSOME_LENGTH) {
				test_factory.insertRandomCall(test, test.size());
			}
			//logger.info("Random test");
			//logger.info(test.toCode());

			// Measure coverage
			TestSuiteChromosome suite = new TestSuiteChromosome();
			suite.addTest(test);
			compareAgainstJUnit(suite);
		}

	}

	/**
	 * Experiment 2: Generate test suites
	 */
	public void experiment2() {
		// Set up search algorithm
		logger.info("Setting up search algorithm for experiment 2");
		SearchStatistics statistics = SearchStatistics.getInstance();

		int num_experiments = Properties.getPropertyOrDefault("num_experiments", 1);
		for (int current_experiment = 0; current_experiment < num_experiments; current_experiment++) {
			// Reset everything
			Randomness.getInstance().setSeed(Randomness.getInstance().getSeed()
			                                         + current_experiment);
			System.out.println("New run with seed " + Randomness.getInstance().getSeed());

			Properties.setProperty("test_factory", "OUM");
			GeneticAlgorithm ga = getGeneticAlgorithm_TestSuite();

			if (!UsageModel.getInstance().hasClass(Properties.TARGET_CLASS)) {
				System.out.println("Have no usage information for "
				        + Properties.TARGET_CLASS);
				System.out.println("UsageCSV," + Properties.getProperty("usage_models")
				        + "," + Properties.getProperty("usage_rate") + ","
				        + Properties.TARGET_CLASS + ",0.0,0,0");
				return;
			}

			// Generate test suite
			logger.info("Starting evolution #" + (current_experiment + 1) + "/"
			        + num_experiments);
			ga.generateSolution();

			TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

			logger.info("Best OUM individual has fitness: " + best.getFitness());
			if (minimize) {
				logger.info("Starting minimization (" + best.size() + "/" + best.length()
				        + ") " + best.getFitness());
				//TestSuiteMinimizer minimizer = new TestSuiteMinimizer();
				//minimizer.minimize((TestSuiteChromosome) ga.getBestIndividual(), ga.getFitnessFunction());
				logger.info("Finished minimization (" + best.size() + "/" + best.length()
				        + ")");
			}

			statistics.iteration(ga.getPopulation());
			statistics.minimized(ga.getBestIndividual());
			System.out.println();
			//			System.out.println("Bloat rejections: "+SteadyStateGA.rejected_bloat);
			compareAgainstJUnit(best);
			double coverage = getCoverage(best);
			int size = best.size();
			int length = best.length();

			System.out.println("UsageCSV," + Properties.getProperty("usage_models") + ","
			        + Properties.getProperty("usage_rate") + ","
			        + Randomness.getInstance().getSeed() + "," + Properties.TARGET_CLASS
			        + "," + coverage + "," + size + "," + length);
			/*
			 * 			String model = "CLIENT";
			if(Properties.getProperty("usage_models").contains("model_tests")) {
				if(Properties.getProperty("usage_models").contains("model_source")) {
					model = "ALL";
				} else {
					model = "TEST";
				}
				
			}
			XStream xstream = new XStream();
			FileWriter fstream;
			try {
				String rate = Properties.getProperty("usage_rate").replace(".","");
				fstream = new FileWriter(Properties.TARGET_CLASS+"_tests_"+model+"_"+Randomness.getInstance().getSeed()+"_"+rate+".xml");
				BufferedWriter out = new BufferedWriter(fstream);

				xstream.toXML(best.getTests(), out);
				String xml = xstream.toXML(best.getTests());
				//TestCase copy = (TestCase) xstream.fromXML(xml);
				//logger.info("After xstream:");
				//logger.info(copy.toCode());
			} catch (IOException e) {
			}
			*/
			resetStoppingConditions();
			/*

			Properties.setProperty("test_factory", "Random");
			ga = getGeneticAlgorithm_TestSuite();

			// Generate test suite
			logger.info("Starting evolution #"+current_experiment+"/"+num_experiments);
			ga.generateSolution();
			
			best = (TestSuiteChromosome) ga.getBestIndividual();

			
			logger.info("Best random individual has fitness: "+best.getFitness());
			if(minimize) {
				logger.info("Starting minimization ("+best.size()+"/"+best.length()+") "+best.getFitness());
				TestSuiteMinimizer minimizer = new TestSuiteMinimizer();
				minimizer.minimize((TestSuiteChromosome) ga.getBestIndividual(), ga.getFitnessFunction());
				logger.info("Finished minimization ("+best.size()+"/"+best.length()+")");
			}

			statistics.iteration(ga.getPopulation());
			statistics.minimized(ga.getBestIndividual());
			System.out.println();
			System.out.println("Bloat rejections: "+SteadyStateGA.rejected_bloat);
			if(Properties.getProperty("junit_tests") != null)
				compareAgainstJUnit(best);

			resetStoppingConditions();
			*/

		}
		statistics.writeReport();

	}

	/**
	 * Experiment 2: Generate test suites
	 */
	public void experiment3() {

		JUnitTestSuite junit = new JUnitTestSuite();
		String name = Properties.getProperty("junit_tests");
		if (name == null)
			return;

		junit.runSuite(name);

		Set<String> junit_covered_true = junit.getTrueCoveredBranches();
		Set<String> junit_covered_false = junit.getFalseCoveredBranches();
		Set<String> junit_covered_methods = junit.getCoveredMethods();

		int total = CFGMethodAdapter.methods.size() + BranchPool.getBranchCounter() * 2
		        + 2;
		int coverage_junit = junit_covered_true.size() + junit_covered_false.size()
		        + junit_covered_methods.size();

		System.out.println("COV," + Properties.TARGET_CLASS + ","
		        + (100.0 * coverage_junit / total));
	}

	@SuppressWarnings("unchecked")
	public void experiment4() {
		XStream xstream = new XStream();
		FileReader fstream;

		try {
			fstream = new FileReader(Properties.getProperty("tests"));
			BufferedReader in = new BufferedReader(fstream);

			ArrayList<TestCase> tests = (ArrayList<TestCase>) xstream.fromXML(in);

			int valid = 0;
			int invalid = 0;

			Set<Class<?>> classes = new HashSet<Class<?>>();
			Set<AccessibleObject> calls = new HashSet<AccessibleObject>();
			for (TestCase test : tests) {
				int num = 0;
				for (StatementInterface s : test.getStatements()) {
					for (VariableReference var : s.getVariableReferences()) {
						classes.add(var.getVariableClass());
					}
					if (s instanceof MethodStatement) {
						calls.add(((MethodStatement) s).getMethod());
					} else if (s instanceof FieldStatement) {
						calls.add(((FieldStatement) s).getField());
					} else if (s instanceof ConstructorStatement) {
						calls.add(((ConstructorStatement) s).getConstructor());
					}
					/*
					ConcreteCall last = factory.getLastUse(test, num, s.getReturnValue());
					ConcreteCall next = factory.getNextUse(test, num, s.getReturnValue());
					if(usage.hasUsageTransition(last, next))
						valid++;
					else
						invalid++;
						*/
					num++;
				}
			}
			int total = valid + invalid;
			String P = "1.0";
			String filename = Properties.getProperty("tests");
			if (filename.contains("_00.xml"))
				P = "0.0";
			else if (filename.contains("_025.xml"))
				P = "0.25";
			else if (filename.contains("_05.xml"))
				P = "0.5";
			else if (filename.contains("_075.xml"))
				P = "0.75";
			else if (filename.contains("_10.xml"))
				P = "1.0";
			System.out.println("Valid: " + valid + "/" + total + " - "
			        + (100.0 * valid / total) + "%");
			System.out.println("Classes accessed: " + classes.size());
			System.out.println("Calls accessed: " + calls.size());
			System.out.println("CSV," + Properties.TARGET_CLASS + "," + P + ","
			        + classes.size() + "," + calls.size());

		} catch (IOException e) {

		}
	}

	@SuppressWarnings("unchecked")
	public void experiment5() {
		XStream xstream = new XStream();
		FileReader fstream;

		try {
			fstream = new FileReader(Properties.getProperty("tests"));
			BufferedReader in = new BufferedReader(fstream);

			ArrayList<TestCase> tests = (ArrayList<TestCase>) xstream.fromXML(in);

			//TestSuiteChromosome suite = new TestSuiteChromosome();
			TestCaseExecutor executor = TestCaseExecutor.getInstance();
			int num_exceptions = 0;
			int num_declared = 0;
			int num_checked = 0;
			int num_unchecked = 0;
			int num_relevant = 0;
			int length = 0;
			Map<String, Integer> exception_map = new HashMap<String, Integer>();
			for (TestCase test : tests) {
				length += test.size();
				Map<Integer, Throwable> exceptionsThrown = executor.run(test);
				num_exceptions += exceptionsThrown.size();
				// TODO: Don't count declared exceptions
				for (Integer pos : exceptionsThrown.keySet()) {
					if (exceptionsThrown.get(pos) instanceof RuntimeException) {
						num_unchecked++;
						String name = exceptionsThrown.get(pos).getClass().getName();
						if (!exception_map.containsKey(name))
							exception_map.put(name, 1);
						else
							exception_map.put(name, exception_map.get(name) + 1);
					} else if (exceptionsThrown.get(pos) instanceof RuntimeException)
						num_checked++;

					if (exceptionsThrown.get(pos) instanceof NullPointerException
					        || exceptionsThrown.get(pos) instanceof NumberFormatException)
						num_relevant++;

					if (test.getStatement(pos) instanceof MethodStatement) {
						MethodStatement ms = (MethodStatement) test.getStatement(pos);
						logger.info("Raised exception: " + exceptionsThrown.get(pos));
						List<Class<?>> declared = Arrays.asList(ms.getMethod().getExceptionTypes());
						for (Class<?> ex : declared) {
							logger.info("Declared exception: " + ex);
						}
						if (declared.contains(exceptionsThrown.get(pos)))
							num_declared++;
					}
				}
			}
			double avg_exceptions = 1.0 * num_exceptions / tests.size();
			double avg_undeclared = 1.0 * (num_exceptions - num_declared) / tests.size();
			double avg_exceptions_len = 1.0 * num_exceptions / length;
			double avg_undeclared_len = 1.0 * (num_exceptions - num_declared) / length;
			double avg_checked_exceptions = 1.0 * num_checked / length;
			double avg_unchecked_exceptions = 1.0 * (num_unchecked - num_declared)
			        / length;
			double avg_relevant = 1.0 * num_relevant / tests.size();
			double avg_relevant_len = 1.0 * num_relevant / length;

			//compareAgainstJUnit(suite);
			// Count undeclared exceptions
			String P = "1.0";
			String filename = Properties.getProperty("tests");
			if (filename.contains("_00.xml"))
				P = "0.0";
			else if (filename.contains("_025.xml"))
				P = "0.25";
			else if (filename.contains("_05.xml"))
				P = "0.5";
			else if (filename.contains("_075.xml"))
				P = "0.75";
			else if (filename.contains("_10.xml"))
				P = "1.0";

			System.out.println("CSV," + Properties.TARGET_CLASS + "," + P + ","
			        + num_exceptions + "," + (num_exceptions - num_declared) + ","
			        + avg_exceptions + "," + avg_undeclared + "," + avg_exceptions_len
			        + "," + avg_undeclared_len + "," + num_unchecked + "," + num_checked
			        + "," + avg_checked_exceptions + "," + avg_unchecked_exceptions + ","
			        + num_relevant + "," + avg_relevant + "," + avg_relevant_len);

			for (String name : exception_map.keySet()) {
				System.out.println("EXC," + Properties.TARGET_CLASS + "," + P + ","
				        + name + "," + exception_map.get(name));
			}

		} catch (IOException e) {

		}
	}

	@SuppressWarnings("unchecked")
	public void experiment6() {
		XStream xstream = new XStream();
		FileReader fstream;

		try {
			fstream = new FileReader(Properties.getProperty("tests"));
			BufferedReader in = new BufferedReader(fstream);

			ArrayList<TestCase> tests = (ArrayList<TestCase>) xstream.fromXML(in);

			TestSuiteChromosome suite = new TestSuiteChromosome();
			for (TestCase test : tests) {
				suite.addTest(test);
			}

		} catch (IOException e) {

		}
	}

	/**
	 * Do the magic
	 */
	public void generateTestSuite() {
		System.out.println("Setting up genetic algorithm");
		SearchStatistics statistics = SearchStatistics.getInstance();
		ExecutionTrace.trace_calls = true;

		int max_s = Properties.GENERATIONS;

		GeneticAlgorithm ga = getGeneticAlgorithm_TestCases();
		TestSuiteChromosome suite = new TestSuiteChromosome();
		FitnessFunction suite_fitness = new de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageSuiteFitness();
		List<BranchCoverageGoal> goals = getBranches();
		Randomness.getInstance().shuffle(goals);
		int total_goals = goals.size();
		int covered_goals = 0;
		int current_statements = 0;

		statistics.searchStarted(suite_fitness);

		Set<Integer> covered = new HashSet<Integer>();

		while (current_statements < max_s && covered_goals < total_goals) {
			int budget = (max_s - current_statements) / (total_goals - covered_goals);
			logger.info("Budget: " + budget + "/" + (max_s - current_statements));
			logger.info("Statements: " + current_statements + "/" + max_s);
			logger.info("Goals covered: " + covered_goals + "/" + total_goals);
			max_statements.setMaxExecutedStatements(budget);
			int num = 0;
			//int num_statements = 0; //MaxStatementsStoppingCondition.getNumExecutedStatements();
			for (BranchCoverageGoal goal : goals) {

				if (covered.contains(num)) {
					num++;
					continue;
				}

				ga.resetStoppingConditions();
				ga.clearPopulation();

				System.out.println("Searching for goal " + num);
				logger.info("Goal " + num + "/" + (total_goals - covered_goals) + ": "
				        + goal);
				//					logger.info("Number of statements: "+max_statements.getNumExecutedStatements());
				//num_statements += MaxStatementsStoppingCondition.getNumExecutedStatements();

				zero_fitness.reset();
				//logger.info("Current number of statements executed: "+num_statements);
				if (goal.isCovered(suite.getTests())) {
					logger.info("Skipping goal because it is already covered");
					covered.add(num);
					covered_goals++;
					num++;
					continue;
				}
				/*
					if(num_statements >= budget) {
						logger.info("Stopping search as max number of statements has been reached");
						suite_fitness.getFitness(suite);
						statistics.iteration(suite);
						current_statements += MaxStatementsStoppingCondition.getNumExecutedStatements();
						max_statements.reset();
						break;
					}
				 */
				FitnessFunction fitness_function = new BranchCoverageTestFitness(goal);
				ga.setFitnessFunction(fitness_function);

				// Perform search
				logger.info("Starting evolution for goal " + goal);
				ga.generateSolution();

				if (ga.getBestIndividual().getFitness() == 0.0) {
					logger.info("Found solution, adding to test suite");
					TestCaseMinimizer minimizer = new TestCaseMinimizer(
					        (TestFitnessFunction) fitness_function);
					TestChromosome best = (TestChromosome) ga.getBestIndividual();
					minimizer.minimize(best);
					suite.addTest(best);
					logger.info("Test case for goal: " + goal);
					logger.info(best.test.toCode());

					covered_goals++;
					covered.add(num);
				} else {
					logger.info("Found no solution");
				}
				suite_fitness.getFitness(suite);
				List<Chromosome> population = new ArrayList<Chromosome>();
				population.add(suite);

				statistics.iteration(population);
				current_statements += MaxStatementsStoppingCondition.getNumExecutedStatements();
				if (current_statements > max_s)
					break;
				logger.info("Adding statements: "
				        + MaxStatementsStoppingCondition.getNumExecutedStatements()
				        + " -> " + current_statements + "/" + max_s);
				max_statements.reset();
				num++;

				//break;
			}
			max_statements.reset();

		}
		List<Chromosome> population = new ArrayList<Chromosome>();
		population.add(suite);

		statistics.searchFinished(population);
		logger.info("Resulting test suite: " + suite.size() + " tests, length "
		        + suite.length());
		// Generate a test suite chromosome once all test cases are done?
		/*
		if(minimize) {
			logger.info("Starting minimization");
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer();
			minimizer.minimize(suite, suite_fitness);
			logger.info("Finished minimization");
		}
		*/
		System.out.println("Resulting test suite has fitness " + suite.getFitness());
		System.out.println("Resulting test suite: " + suite.size() + " tests, length "
		        + suite.length());

		/*
		AssertionGenerator asserter = new AssertionGenerator();
		for(TestCase test : suite.getTests()) {
			Set<Long> killed = new HashSet<Long>();
			asserter.addAssertions(test, killed);
		}
		*/
		//logger.info("Mutants killed with assertions: "+killed.size());

		// Log some stats

		//statistics.iteration(population);
		statistics.minimized(suite);

		statistics.writeReport();

		compareAgainstJUnit(suite);

		XStream xstream = new XStream();
		FileWriter fstream;
		try {
			String rate = Properties.getProperty("usage_rate").replace(".", "");
			fstream = new FileWriter(Properties.TARGET_CLASS + "_tests_"
			        + Randomness.getInstance().getSeed() + "_" + rate + ".xml");
			BufferedWriter out = new BufferedWriter(fstream);

			xstream.toXML(suite.getTests(), out);
			//String xml = xstream.toXML(suite.getTests());
			//TestCase copy = (TestCase) xstream.fromXML(xml);
			//logger.info("After xstream:");
			//logger.info(copy.toCode());
		} catch (IOException e) {
		}

	}

	protected List<BranchCoverageGoal> getBranches() {
		List<BranchCoverageGoal> goals = new ArrayList<BranchCoverageGoal>();

		// Branchless methods
		String class_name = Properties.TARGET_CLASS;
		logger.info("Getting branches for " + class_name);
		for (String method : BranchPool.getBranchlessMethods()) {
			goals.add(new BranchCoverageGoal(class_name, method));
			logger.info("Adding new method goal for method " + method);
		}

		// Branches
		for (String className : BranchPool.branchMap.keySet()) {
			for (String methodName : BranchPool.branchMap.get(className).keySet()) {
				// Get CFG of method
				//				ControlFlowGraph cfg = ExecutionTracer.getExecutionTracer().getCFG(className, methodName);
				ControlFlowGraph cfg = CFGMethodAdapter.getMinimizedCFG(className, methodName);

				for (Branch b : BranchPool.branchMap.get(className).get(methodName)) {
					// Identify vertex in CFG
					goals.add(new BranchCoverageGoal(b, true, cfg, className, methodName));
					goals.add(new BranchCoverageGoal(b, false, cfg, className, methodName));
					logger.info("Adding new branch goals for method " + methodName);
				}

				// Approach level is measured in terms of line coverage? Or possible in terms of branches...
			}
		}

		return goals;
	}

	/**
	 * Factory method for search algorithm
	 * 
	 * @return
	 */
	protected GeneticAlgorithm getGeneticAlgorithm_TestCases() {

		GeneticAlgorithm ga = null;
		ChromosomeFactory factory = null;
		String factory_name = Properties.getPropertyOrDefault("test_factory", "Random");
		if (factory_name.equals("OUM"))
			factory = new OUMTestChromosomeFactory();
		else
			factory = new RandomLengthTestFactory();

		SelectionFunction selection_function = new RankSelection();
		selection_function.setMaximize(false);

		String search_algorithm = Properties.getProperty("algorithm");
		if (search_algorithm.equals("(1+1)EA")) {
			logger.info("Chosen search algorithm: (1+1)EA");
			ga = new OnePlusOneEA(factory);
			//((OnePlusOneEA)ga).setReplacementFunction(new TestCaseReplacementFunction(selection_function));

		} else if (search_algorithm.equals("SteadyStateGA")) {
			logger.info("Chosen search algorithm: SteadyStateGA");
			ga = new SteadyStateGA(factory);
			((SteadyStateGA) ga).setReplacementFunction(new de.unisb.cs.st.evosuite.testcase.TestCaseReplacementFunction(
			        selection_function));

		} else if (search_algorithm.equals("MuPlusLambdaGA")) {
			logger.info("Chosen search algorithm: MuPlusLambdaGA");
			ga = new MuPlusLambdaGA(factory);

		} else {
			logger.info("Chosen search algorithm: StandardGA");
			ga = new StandardGA(factory);
		}

		ga.setSelectionFunction(selection_function);

		String stopping_condition = Properties.getPropertyOrDefault("stopping_condition",
		                                                            "MaxGenerations");
		logger.info("Setting stopping condition: " + stopping_condition);
		if (stopping_condition.equals("MaxGenerations"))
			ga.setStoppingCondition(max_gen);
		else if (stopping_condition.equals("MaxEvaluations"))
			ga.setStoppingCondition(max_fitness);
		else if (stopping_condition.equals("MaxTime"))
			ga.setStoppingCondition(max_time);
		else if (stopping_condition.equals("MaxTests"))
			ga.setStoppingCondition(max_tests);
		else if (stopping_condition.equals("MaxStatements")) {
			ga.setStoppingCondition(max_statements);
			((MaxGenerationStoppingCondition) max_gen).setMaxIterations(1000);
			ga.addStoppingCondition(max_gen);
		} else {
			logger.warn("Unknown stopping condition: " + stopping_condition);
		}
		ga.addStoppingCondition(zero_fitness);

		// Relative position crossover to avoid that size increases
		CrossOverFunction crossover_function = new SinglePointRelativeCrossOver();
		ga.setCrossOverFunction(crossover_function);

		//MaxLengthBloatControl bloat_control = new MaxLengthBloatControl();
		//ga.setBloatControl(bloat_control);

		RelativeLengthBloatControl bloat_control = new RelativeLengthBloatControl();

		if (PARENTS_LENGTH) {
			logger.debug("Using parent bloat control");
			ga.addBloatControl(bloat_control);
			ga.addListener(bloat_control);
		} else {
			logger.debug("Not using parent bloat control");
		}
		//ga.addBloatControl(new MaxLengthBloatControl());

		//ga.addListener(SearchStatistics.getInstance());
		ga.addListener(MutationStatistics.getInstance());
		//ga.addListener(BestChromosomeTracker.getInstance());

		// Possibly change stopping condition
		//ga.addStoppingCondition(MaxStatementsStoppingCondition.getInstance());
		//ga.addListener(MaxStatementsStoppingCondition.getInstance());
		//ga.addListener(new MaxStatementsStoppingCondition());
		ga.addListener(new MaxTestsStoppingCondition());
		ga.addListener(new MaxFitnessEvaluationsStoppingCondition());
		return ga;
	}

	/**
	 * Factory method for search algorithm
	 * 
	 * @return
	 */
	protected GeneticAlgorithm getGeneticAlgorithm_TestSuite() {

		GeneticAlgorithm ga = null;
		ChromosomeFactory factory = new TestSuiteChromosomeFactory();

		SelectionFunction selection_function = new RankSelection();
		selection_function.setMaximize(false);

		String search_algorithm = Properties.getProperty("algorithm");
		if (search_algorithm.equals("(1+1)EA")) {
			logger.info("Chosen search algorithm: (1+1)EA");
			ga = new OnePlusOneEA(factory);

		} else if (search_algorithm.equals("SteadyStateGA")) {
			logger.info("Chosen search algorithm: SteadyStateGA");
			ga = new SteadyStateGA(factory);
			((SteadyStateGA) ga).setReplacementFunction(new TestSuiteReplacementFunction(
			        selection_function));

		} else if (search_algorithm.equals("MuPlusLambdaGA")) {
			logger.info("Chosen search algorithm: MuPlusLambdaGA");
			ga = new MuPlusLambdaGA(factory);
			((MuPlusLambdaGA) ga).setReplacementFunction(new TestSuiteReplacementFunction(
			        selection_function));

		} else {
			logger.info("Chosen search algorithm: StandardGA");
			ga = new StandardGA(factory);
		}

		ga.setSelectionFunction(selection_function);

		String stopping_condition = Properties.getPropertyOrDefault("stopping_condition",
		                                                            "MaxGenerations");
		logger.info("Setting stopping condition: " + stopping_condition);
		if (stopping_condition.equals("MaxGenerations"))
			ga.setStoppingCondition(max_gen);
		else if (stopping_condition.equals("MaxEvaluations"))
			ga.setStoppingCondition(max_fitness);
		else if (stopping_condition.equals("MaxTime"))
			ga.setStoppingCondition(max_time);
		else if (stopping_condition.equals("MaxTests"))
			ga.setStoppingCondition(max_tests);
		else if (stopping_condition.equals("MaxStatements")) {
			ga.setStoppingCondition(max_statements);
			((MaxGenerationStoppingCondition) max_gen).setMaxIterations(1000);
			ga.addStoppingCondition(max_gen);
		} else {
			logger.warn("Unknown stopping condition: " + stopping_condition);
		}
		ga.addStoppingCondition(zero_fitness);

		// Relative position crossover to avoid that size increases
		CrossOverFunction crossover_function = new SinglePointRelativeCrossOver();
		ga.setCrossOverFunction(crossover_function);

		FitnessFunction fitness_function = new de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageSuiteFitness();
		ga.setFitnessFunction(fitness_function);

		//MaxLengthBloatControl bloat_control = new MaxLengthBloatControl();
		//ga.setBloatControl(bloat_control);

		de.unisb.cs.st.evosuite.testsuite.RelativeLengthBloatControl bloat_control = new de.unisb.cs.st.evosuite.testsuite.RelativeLengthBloatControl();
		ga.addBloatControl(bloat_control);
		ga.addListener(bloat_control);
		//ga.addBloatControl(new MaxLengthBloatControl());

		ga.addListener(SearchStatistics.getInstance());
		//ga.addListener(MutationStatistics.getInstance());
		//ga.addListener(BestChromosomeTracker.getInstance());

		// Possibly change stopping condition
		//ga.addStoppingCondition(MaxStatementsStoppingCondition.getInstance());
		ga.addListener(new MaxStatementsStoppingCondition());
		ga.addListener(new MaxTestsStoppingCondition());
		ga.addListener(new MaxFitnessEvaluationsStoppingCondition());
		return ga;
	}

	private void resetStoppingConditions() {
		max_gen.reset();
		max_fitness.reset();
		max_time.reset();
		max_tests.reset();
		max_statements.reset();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Creating coverage test suite");
		TestSuiteGenerator generator = new TestSuiteGenerator();
		String experiment = System.getProperty("experiment");
		if (experiment != null && !experiment.equals("none") && !experiment.equals("")) {
			System.out.println("Starting experiment: " + experiment);
			int num = Integer.parseInt(experiment);
			switch (num) {
			case 1:
				generator.experiment1();
				break;
			case 2:
				generator.experiment2();
				break;
			case 3:
				generator.experiment3();
				break;
			case 4:
				generator.experiment4();
				break;
			case 5:
				generator.experiment5();
				break;
			default:
				generator.generateTestSuite();
			}
		} else
			generator.generateTestSuite();
	}
}
