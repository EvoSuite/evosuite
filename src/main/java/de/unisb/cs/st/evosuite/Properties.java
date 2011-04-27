/*
 * Copyright (C) 2010 Saarland University
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
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Gordon Fraser
 * 
 */
public class Properties {

	public enum CRITERIA {
		CONCURRENCY, LCSAJ, DEFUSE, PATH, BRANCH, MUTATION
	}

	/** Cache target class */
	private static Class<?> TARGET_CLASS_INSTANCE = null;

	/** Package name of target package */
	public static String PROJECT_PREFIX = null;

	/** Package name of target class (might be a subpackage) */
	public static String CLASS_PREFIX = "";

	/** Sub-package name of target class */
	public static String SUB_PREFIX = "";

	/** Class under test */
	public static String TARGET_CLASS = "";

	/** Method under test */
	public static String TARGET_METHOD = null;

	/** Directory in which to put generated files */
	public static String OUTPUT_DIR = "";

	/** Singleton instance */
	private static Properties instance = new Properties();

	/** Internal properties hashmap */
	private final java.util.Properties properties;

	/** Values of integer parameters */
	private static Map<String, Integer> intValues = new HashMap<String, Integer>();

	/** Values of Boolean parameters */
	private static Map<String, Boolean> booleanValues = new HashMap<String, Boolean>();

	/** Values of double parameters */
	private static Map<String, Double> doubleValues = new HashMap<String, Double>();

	/** Values of string parameters */
	private static Map<String, String> stringValues = new HashMap<String, String>();

	/** Meta-information about parameters */
	private static Map<String, Type<?>> parameters = new HashMap<String, Type<?>>();

	/**
	 * All parameter have to be registered properly
	 */
	static {
		// Test sequence creation
		registerStringParameter("test_excludes",
		                        "File containing methods that should not be used in testing",
		                        "Test Creation", "test.excludes");
		registerStringParameter("test_includes",
		                        "File containing methods that should be included in testing",
		                        "Test Creation", "test.includes");
		registerBooleanParameter("make_accessible",
		                         "Change default package rights to public package rights (?)",
		                         "Test Creation", false);
		registerBooleanParameter("string_replacement",
		                         "Replace string.equals with levenshtein distance",
		                         "Test Creation", true);
		registerBooleanParameter("static_hack",
		                         "Call static constructors after each test execution",
		                         "Test Creation", false);
		registerDoubleParameter("null_probability",
		                        "Probability to use null instead of constructing an object",
		                        "Test Creation", 0.1, 0.0, 1.0);
		registerDoubleParameter("object_reuse_probability",
		                        "Probability to reuse an existing reference, if available",
		                        "Test Creation", 0.9, 0.0, 1.0);
		registerDoubleParameter("primitive_reuse_probability",
		                        "Probability to reuse an existing primitive, if available",
		                        "Test Creation", 0.5, 0.0, 1.0);
		registerDoubleParameter("primitive_pool",
		                        "Probability to use a primitive from the pool rather than a random value",
		                        "Test Creation", 0.5, 0.0, 1.0);
		registerIntParameter("string_length",
		                     "Maximum length of randomly generated strings",
		                     "Test Creation", 20);
		registerIntParameter("max_int",
		                     "Maximum size of randomly generated integers (minimum range = -1 * max)",
		                     "Test Creation", 256);
		registerIntParameter("max_array", "Maximum lengthof randomly generated arrays",
		                     "Test Creation", 20);
		registerIntParameter("max_attempts",
		                     "Number of attempts when generating an object before giving up",
		                     "Test Creation", 1000);
		registerIntParameter("max_recursion",
		                     "Recursion depth when trying to create objects",
		                     "Test Creation", 10);
		registerIntParameter("max_length",
		                     "Maximum length of test suites (0 = no check)",
		                     "Test Creation", 0);
		registerIntParameter("max_size", "Maximum number of test cases in a test suite",
		                     "Test Creation", 50);
		registerIntParameter("num_tests", "Number of tests in initial test suites",
		                     "Test Creation", 2);
		registerBooleanParameter("use_deprecated", "Include deprecated methods in tests",
		                         "Test Creation", false);
		// deprecated
		registerIntParameter("generator_tournament",
		                     "Size of tournament when choosing a generator",
		                     "Test Creation", 1);
		registerBooleanParameter("generate_objects",
		                         "Generate .object files that allow adapting method signatures",
		                         "Test Creation", false);

		// Search algorithm
		registerStringParameter("algorithm", "Search algorithm", "Search algorithm",
		                        "SteadyStateGA", "StandardGA", "SteadyStateGA",
		                        "(1+1)EA", "MuPlusLambdaGA");
		registerBooleanParameter("check_best_length",
		                         "Check length against length of best individual",
		                         "Search algorithm", true);
		registerBooleanParameter("check_parents_length",
		                         "Check length against length of parents",
		                         "Search algorithm", true);
		registerBooleanParameter("check_rank_length", "Use length in rank selection",
		                         "Search algorithm", true);
		registerBooleanParameter("parent_check",
		                         "Check against parents in Mu+Lambda algorithm",
		                         "Search algorithm", true);
		registerBooleanParameter("check_max_length",
		                         "Check length against fixed maximum",
		                         "Search algorithm", true);
		registerDoubleParameter("crossover_rate", "Probability of crossover",
		                        "Search algorithm", 0.75, 0.0, 1.0);
		registerDoubleParameter("kincompensation", "Penalty for duplicate individuals",
		                        "Search algorithm", 1.0, 0.0, 1.0);
		registerIntParameter("elite", "Elite size for search algorithm",
		                     "Search Algorithm", 1);
		registerIntParameter("tournament_size",
		                     "Number of individuals for tournament selection",
		                     "Search Algorithm", 5);
		registerDoubleParameter("rank_bias",
		                        "Bias for better individuals in rank selection",
		                        "Search Algorithm", 1.7);
		registerIntParameter("chromosome_length",
		                     "Maximum length of chromosomes during search",
		                     "Search Algorithm", 100, 1, 100000);
		registerIntParameter("population", "Population size of genetic algorithm",
		                     "Search Algorithm", 100, 1, 100000);
		registerIntParameter("generations", "Maximum search duration",
		                     "Search Algorithm", 1000, 1, Integer.MAX_VALUE);
		registerStringParameter("stopping_condition",
		                        "What condition should be checked to end the search",
		                        "Search Algorithm", "MaxGenerations", "MaxStatements",
		                        "MaxTests", "MaxTime", "MaxGenerations",
		                        "MaxFitnessEvaluations");
		registerStringParameter("crossover_function", "Crossover function during search",
		                        "Search Algorithm", "SinglePoint", "SinglePointRelative",
		                        "SinglePointFixed", "SinglePoint");
		registerStringParameter("selection_function", "Selection function during search",
		                        "Search Algorithm", "Rank", "Rank", "RouletteWheel",
		                        "Tournament");
		// TODO: Fix values
		registerStringParameter("secondary_objectives",
		                        "Secondary objective during search", "Search Algorithm",
		                        "maxlength", "maxlength", "maxsize", "avglength");
		registerIntParameter("bloat_factor", "Maximum relative increase in length",
		                     "Search Algorithm", 2);
		registerBooleanParameter("stop_zero", "Stop optimization once goal is covered",
		                         "Search Algorithm", true);
		registerBooleanParameter("dynamic_limit",
		                         "Multiply search budget by number of test goals",
		                         "Search Algorithm", false);
		registerIntParameter("global_timeout", "Seconds allowed for entire search",
		                     "Search Algorithm", 600);

		// Single branch mode
		registerIntParameter("random_tests",
		                     "Number of random tests to run before test generation (Single branch mode)",
		                     "Single Branch Mode", 0);
		registerBooleanParameter("skip_covered",
		                         "Skip coverage goals that have already been (coincidentally) covered",
		                         "Single Branch Mode", true);
		registerBooleanParameter("reuse_budget",
		                         "Use leftover budget on unsatisfied test goals (Single branch mode)",
		                         "Single Branch Mode", true);
		registerBooleanParameter("shuffle_goals",
		                         "Shuffle test goals before test generation (Single branch mode)",
		                         "Single Branch Mode", true);
		registerBooleanParameter("recycle_chromosomes",
		                         "Seed initial population with related individuals (Single branch mode)",
		                         "Single Branch Mode", true);

		// Output
		registerBooleanParameter("print_to_system", "Allow test output on console",
		                         "Output", false);
		registerBooleanParameter("plot", "Create plots of size and fitness", "Output",
		                         false);
		registerBooleanParameter("html", "Create html reports", "Output", true);
		registerBooleanParameter("junit_tests", "Create JUnit test suites", "Output",
		                         true);
		registerBooleanParameter("log_goals",
		                         "Create a CSV file for each individual evolution",
		                         "Output", false);
		registerBooleanParameter("minimize", "Minimize test suite after generation",
		                         "Output", true);
		registerStringParameter("report_dir",
		                        "Directory in which to put HTML and CSV reports",
		                        "Output", "report");
		registerBooleanParameter("print_current_goals",
		                         "Print out current goal during test generation",
		                         "Output", false);
		registerBooleanParameter("print_covered_goals",
		                         "Print out covered goals during test generation",
		                         "Output", false);
		registerBooleanParameter("assertions", "Create assertions", "Output", false);
		registerStringParameter("test_dir", "Directory in which to place JUnit tests",
		                        "Output", "evosuite-tests");

		// Sandbox
		registerBooleanParameter("sandbox", "Execute tests in a sandbox environment",
		                         "Sandbox", false);
		registerBooleanParameter("mocks", "Usage of the mocks for the IO, Network etc",
		                         "Sandbox", false);
		registerStringParameter("sandbox_folder",
		                        "Folder used for IO, when mocks are enabled ", "Sandbox",
		                        "sandbox/");
		registerBooleanParameter("stubs", "Stub generation for abstract classes",
		                         "Sandbox", false);

		// Experimental
		registerBooleanParameter("remote_testing", "Include remote calls",
		                         "Experimental", false);
		registerBooleanParameter("cpu_timeout",
		                         "Measure timeouts on CPU time, not global time",
		                         "Experimental", false);
		registerBooleanParameter("log_timeout",
		                         "Produce output each time a test times out",
		                         "Experimental", false);
		registerDoubleParameter("call_probability",
		                        "Probability to reuse an existing test case, if it produces a required object",
		                        "Experimental", 0.0, 0.0, 1.0);
		registerStringParameter("usage_models", "Names of usage model files",
		                        "Experimental", "");
		registerDoubleParameter("usage_rate",
		                        "Probability with which to use transitions out of the OUM",
		                        "Experimental", 0.5, 0.0, 1.0);
		registerBooleanParameter("instrument_parent",
		                         "Also count coverage goals in superclasses",
		                         "Experimental", false);
		registerBooleanParameter("check_contracts",
		                         "Check contracts during test execution", "Experimental",
		                         false);
		registerStringParameter("test_factory", "Which factory creates tests",
		                        "Experimental", "Random", "Random", "OUM");
		registerDoubleParameter("concolic_mutation",
		                        "Probability of using concolic mutation operator",
		                        "Experimental", 0.0, 0.0, 1.0);
		registerBooleanParameter("testability_transformation",
		                         "Apply testability transformation (Yanchuan)",
		                         "Experimental", false);
		registerIntParameter("TT.stack",
		                     "Maximum stack depth for testability transformation",
		                     "Experimental", 10);
		registerBooleanParameter("TT", "Testability transformation", "Experimental",
		                         false);

		// Test Execution
		registerIntParameter("timeout", "Milliseconds allowed per test",
		                     "Test Execution", 5000);
		registerIntParameter("mutation_timeouts",
		                     "Number of timeouts before we consider a mutant killed",
		                     "Test Execution", 3);

		// TODO: Fix description
		registerStringParameter("alternative_fitness_calculation_mode", "",
		                        "Experimental", "sum", "sum", "min", "max", "avg",
		                        "single");
		registerDoubleParameter("initially_enforced_randomness", "", "Experimental", 0.4,
		                        0.0, 1.0);
		registerDoubleParameter("alternative_fitness_range", "", "Experimental", 100.0);
		registerBooleanParameter("preorder_goals_by_difficulty", "", "Experimental",
		                         false);
		registerBooleanParameter("starve_by_fitness", "", "Experimental", true);
		registerBooleanParameter("penalize_overwriting_definitions_flat", "",
		                         "Experimental", false);
		registerBooleanParameter("penalize_overwriting_definitions_linearly", "",
		                         "Experimental", false);
		registerBooleanParameter("enable_alternative_fitness_calculation", "",
		                         "Experimental", true);
		registerBooleanParameter("defuse_debug_mode", "", "Experimental", false);
		registerBooleanParameter("randomize_difficulty", "", "Experimental", true);

		// Project
		registerStringParameter("PROJECT_PREFIX", "Name of the package that is tested",
		                        "Project", "");

		// These should not be normal parameters but something else...runtime config?
		// Options
		registerStringParameter("strategy",
		                        "Whole test suite optimization vs. single branch strategy",
		                        "Option", "EvoSuite", "EvoSuite", "OneBranch");
		registerStringParameter("criterion", "Coverage criterion", "Option", "Branch",
		                        "Branch", "LCSAJ", "DefUse", "Path", "Mutation");
		registerStringParameter("target_method", "Method for which to create tests",
		                        "Option", "");
	}

	public static CRITERIA CRITERION = getCriterion();

	private static CRITERIA getCriterion() {
		String crit = getStringValue("criterion");
		if (crit.equalsIgnoreCase("concurrency")) {
			return CRITERIA.CONCURRENCY;
		} else if (crit.equalsIgnoreCase("lcsaj")) {
			return CRITERIA.LCSAJ;
		} else if (crit.equalsIgnoreCase("defuse")) {
			return CRITERIA.DEFUSE;
		} else if (crit.equalsIgnoreCase("path")) {
			return CRITERIA.PATH;
		} else if (crit.equalsIgnoreCase("mutation")) {
			return CRITERIA.MUTATION;
		} else if (crit.equals("BranchCoverage") || crit.equalsIgnoreCase("branch")) {
			return CRITERIA.BRANCH;
		} else {
			throw new AssertionError("Unknown coverage criterion: " + crit);
		}
	}

	public static class NoSuchPropertyException extends Exception {

		private static final long serialVersionUID = 9074828392047742535L;

		public NoSuchPropertyException(String key) {
			super("No such property defined: " + key);
		}
	}

	private Properties() {
		properties = new java.util.Properties();
		try {
			InputStream in = this.getClass().getClassLoader().getResourceAsStream("evosuite.properties");
			properties.load(in);
			PROJECT_PREFIX = properties.getProperty("PREFIX");
			OUTPUT_DIR = properties.getProperty("OUTPUT_DIR");
			TARGET_CLASS = properties.getProperty("TARGET_CLASS");
			TARGET_METHOD = properties.getProperty("TARGET_METHOD");
			System.out.println("* Properties loaded from configuration file evosuite.properties");
			System.out.println("* Project prefix: " + PROJECT_PREFIX);
		} catch (FileNotFoundException e) {
			System.err.println("Error: Could not find configuration file evosuite.properties");
		} catch (IOException e) {
			System.err.println("Error: Could not find configuration file evosuite.properties");
		} catch (Exception e) {
			System.err.println("Error: Could not find configuration file evosuite.properties");
		}
		if (System.getProperty("OUTPUT_DIR") != null) {
			OUTPUT_DIR = System.getProperty("OUTPUT_DIR");
		}
		if (System.getProperty("TARGET_CLASS") != null) {
			TARGET_CLASS = System.getProperty("TARGET_CLASS");
		}
		if (PROJECT_PREFIX == null) {
			PROJECT_PREFIX = System.getProperty("PROJECT_PREFIX");
		}
		if (TARGET_CLASS != null) {
			properties.setProperty("TARGET_CLASS", TARGET_CLASS);
			CLASS_PREFIX = TARGET_CLASS.substring(0, TARGET_CLASS.lastIndexOf('.'));
			SUB_PREFIX = CLASS_PREFIX.replace(PROJECT_PREFIX + ".", "");
		}

		properties.setProperty("PROJECT_PREFIX", PROJECT_PREFIX);
		properties.setProperty("output_dir", OUTPUT_DIR);
	}

	/**
	 * Singleton accessor
	 * 
	 * @return Singleton reference
	 */
	private static Properties getInstance() {
		if (instance == null) {
			instance = new Properties();
		}
		return instance;
	}

	/**
	 * Get class object of class under test
	 * 
	 * @return
	 */
	public static Class<?> getTargetClass() {
		if (TARGET_CLASS_INSTANCE != null)
			return TARGET_CLASS_INSTANCE;

		try {
			TARGET_CLASS_INSTANCE = Class.forName(TARGET_CLASS);
			return TARGET_CLASS_INSTANCE;
		} catch (ClassNotFoundException e) {
			System.err.println("Could not find class under test " + TARGET_CLASS);
		}
		return null;
	}

	/**
	 * Abstract superclass of parameter meta-information
	 */
	public static abstract class Type<T> {
		String description;
		String groupName;
		T defaultValue;

		Type(T defaultValue, String description, String groupName) {
			this.defaultValue = defaultValue;
			this.description = description;
			this.groupName = groupName;
		}
	}

	/**
	 * Meta-information about an integer parameter
	 */
	public static class IntType extends Type<Integer> {
		int min;
		int max;

		IntType(int min, int max, int defaultValue, String description, String groupName) {
			super(defaultValue, description, groupName);
			this.min = min;
			this.max = max;
		}
	}

	/**
	 * Meta-information about a double parameter
	 */
	public static class DoubleType extends Type<Double> {
		double min;
		double max;

		DoubleType(double min, double max, double defaultValue, String description,
		        String groupName) {
			super(defaultValue, description, groupName);
			this.min = min;
			this.max = max;
		}
	}

	/**
	 * Meta-information about a boolean parameter
	 */
	public static class BooleanType extends Type<Boolean> {
		BooleanType(boolean defaultValue, String description, String groupName) {
			super(defaultValue, description, groupName);
		}
	}

	/**
	 * Meta-information about a string parameter
	 */
	public static class StringType extends Type<String> {
		String[] values;

		StringType(String defaultValue, String[] values, String description,
		        String groupName) {
			super(defaultValue, description, groupName);
			this.values = values;
		}
	}

	/**
	 * Register a new integer parameter with default boundaries
	 * 
	 * @param key
	 * @param description
	 * @param defaultValue
	 */
	private static void registerIntParameter(String key, String description,
	        String group, int defaultValue) {
		registerIntParameter(key, description, group, defaultValue, Integer.MIN_VALUE,
		                     Integer.MAX_VALUE);
	}

	/**
	 * Register a new integer parameter
	 * 
	 * @param key
	 * @param description
	 * @param defaultValue
	 * @param min
	 * @param max
	 */
	private static void registerIntParameter(String key, String description,
	        String group, int defaultValue, int min, int max) {
		String value = System.getProperty(key);
		if (value != null) {
			// Commandline overrides everything
			Integer val = Integer.parseInt(value);
			intValues.put(key, val);
		} else if (Properties.getInstance().properties.containsKey(key)) {
			// Properties file overrides default
			Integer val = Integer.parseInt(Properties.getInstance().properties.getProperty(key));
			intValues.put(key, val);
		} else {
			// If parameter is not set, use default
			intValues.put(key, defaultValue);
		}

		parameters.put(key, new IntType(min, max, defaultValue, description, group));
	}

	/**
	 * Register a new double parameter with default boundaries
	 * 
	 * @param key
	 * @param description
	 * @param defaultValue
	 */
	private static void registerDoubleParameter(String key, String description,
	        String group, double defaultValue) {
		registerDoubleParameter(key, description, group, defaultValue, Double.MIN_VALUE,
		                        Double.MAX_VALUE);
	}

	/**
	 * Register a new double parameter
	 * 
	 * @param key
	 * @param description
	 * @param defaultValue
	 * @param min
	 * @param max
	 */
	private static void registerDoubleParameter(String key, String description,
	        String group, double defaultValue, double min, double max) {
		String value = System.getProperty(key);
		if (value != null) {
			// Commandline overrides everything
			Double val = Double.parseDouble(value);
			doubleValues.put(key, val);
		} else if (Properties.getInstance().properties.containsKey(key)) {
			// Properties file overrides default
			Double val = Double.parseDouble(Properties.getInstance().properties.getProperty(key));
			doubleValues.put(key, val);
		} else {
			// If parameter is not set, use default
			doubleValues.put(key, defaultValue);
		}

		parameters.put(key, new DoubleType(min, max, defaultValue, description, group));
	}

	/**
	 * Register a new parameter of Boolean type
	 * 
	 * @param key
	 * @param description
	 * @param defaultValue
	 */
	private static void registerBooleanParameter(String key, String description,
	        String group, boolean defaultValue) {
		String value = System.getProperty(key);
		if (value != null) {
			// Commandline overrides everything
			Boolean val = Boolean.parseBoolean(value);
			booleanValues.put(key, val);
		} else if (Properties.getInstance().properties.containsKey(key)) {
			// Properties file overrides default
			Boolean val = Boolean.parseBoolean(Properties.getInstance().properties.getProperty(key));
			booleanValues.put(key, val);
		} else {
			// If parameter is not set, use default
			booleanValues.put(key, defaultValue);
		}

		parameters.put(key, new BooleanType(defaultValue, description, group));
	}

	/**
	 * Register a new string parameter
	 * 
	 * @param key
	 * @param description
	 * @param defaultValue
	 * @param values
	 */
	private static void registerStringParameter(String key, String description,
	        String group, String defaultValue, String... values) {
		String value = System.getProperty(key);
		if (value != null) {
			// Commandline overrides everything
			stringValues.put(key, value);
		} else if (Properties.getInstance().properties.containsKey(key)) {
			// Properties file overrides default
			String val = Properties.getInstance().properties.getProperty(key);
			stringValues.put(key, val);
		} else {
			// If parameter is not set, use default
			stringValues.put(key, defaultValue);
		}

		parameters.put(key, new StringType(defaultValue, values, description, group));
	}

	/**
	 * Get set of parameters
	 * 
	 * @return
	 */
	public static Set<String> getParameters() {
		return parameters.keySet();
	}

	/**
	 * Get current value of a Boolean parameter
	 * 
	 * @param key
	 * @return
	 */
	public static boolean getBooleanValue(String key) {
		if (!booleanValues.containsKey(key))
			System.out.println("No such key: " + key);
		return booleanValues.get(key).booleanValue();
	}

	/**
	 * Get current value of an integer parameter
	 * 
	 * @param key
	 * @return
	 */
	public static int getIntegerValue(String key) {
		if (!intValues.containsKey(key))
			System.out.println("No such key: " + key);
		return intValues.get(key).intValue();
	}

	/**
	 * Get current value of a double parameter
	 * 
	 * @param key
	 * @return
	 */
	public static double getDoubleValue(String key) {
		if (!doubleValues.containsKey(key))
			System.out.println("No such key: " + key);
		return doubleValues.get(key).doubleValue();
	}

	/**
	 * Get current value of a string parameter
	 * 
	 * @param key
	 * @return
	 */
	public static String getStringValue(String key) {
		if (!stringValues.containsKey(key))
			System.out.println("No such key: " + key);
		return stringValues.get(key);
	}

	/**
	 * Get value as string
	 * 
	 * @param key
	 * @return
	 * @throws NoSuchPropertyException
	 */
	public static String getValue(String key) throws NoSuchPropertyException {
		if (intValues.containsKey(key))
			return intValues.get(key).toString();
		else if (doubleValues.containsKey(key))
			return doubleValues.get(key).toString();
		else if (booleanValues.containsKey(key))
			return booleanValues.get(key).toString();
		else if (stringValues.containsKey(key))
			return stringValues.get(key);
		else
			throw new NoSuchPropertyException(key);
	}

	/**
	 * Set a new value for int parameter
	 * 
	 * @param key
	 * @param val
	 */
	public static void setValue(String key, int val) {
		IntType type = (IntType) parameters.get(key);
		if (val >= type.min && val <= type.max)
			intValues.put(key, val);
	}

	/**
	 * Set a new value for double parameter
	 * 
	 * @param key
	 * @param val
	 */
	public static void setValue(String key, double val) {
		DoubleType type = (DoubleType) parameters.get(key);
		if (val >= type.min && val <= type.max)
			doubleValues.put(key, val);
	}

	/**
	 * Set a new value for boolean parameter
	 * 
	 * @param key
	 * @param val
	 */
	public static void setValue(String key, boolean val) {
		booleanValues.put(key, val);
	}

	/**
	 * Set a new value for string parameter
	 * 
	 * @param key
	 * @param val
	 */
	public static void setValue(String key, String val) {
		StringType type = (StringType) parameters.get(key);
		if (type.values.length == 0)
			stringValues.put(key, val);
		else {
			if (Arrays.asList(type.values).contains(val))
				stringValues.put(key, val);
		}
	}

	/**
	 * Retrieve meta information about the parameter
	 * 
	 * @param key
	 *            - name of parameter
	 * @return
	 */
	public static Type<?> getParameterType(String key) {
		return parameters.get(key);
	}

	/**
	 * Update the evosuite.properties file with the current setting
	 */
	public void writeConfiguration() {
		try {
			URL fileURL = this.getClass().getClassLoader().getResource("evosuite.properties");
			String name = fileURL.getFile();
			OutputStream out = new FileOutputStream(new File(name));
			// TODO: Update the properties!
			properties.store(out, "This file was automatically produced by EvoSuite");
		} catch (FileNotFoundException e) {
			System.err.println("Error: Could not find configuration file evosuite.properties");
		} catch (IOException e) {
			System.err.println("Error: Could not find configuration file evosuite.properties");
		} catch (Exception e) {
			System.err.println("Error: Could not find configuration file evosuite.properties");
		}

	}
}
