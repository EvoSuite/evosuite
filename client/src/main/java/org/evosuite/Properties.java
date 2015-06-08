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
package org.evosuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central property repository. All global parameters of EvoSuite should be
 * declared as fields here, using the appropriate annotation. Access is possible
 * directly via the fields, or with getter/setter methods.
 *
 * @author Gordon Fraser
 */
public class Properties {

	private final static Logger logger = LoggerFactory.getLogger(Properties.class);

	/**
	 * Parameters are fields of the Properties class, annotated with this
	 * annotation. The key parameter is used to identify values in property
	 * files or on the command line, the group is used in the config file or
	 * input plugins to organize parameters, and the description is also
	 * displayed there.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Parameter {
		String key();

		String group() default "Experimental";

		String description();
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface IntValue {
		int min() default Integer.MIN_VALUE;

		int max() default Integer.MAX_VALUE;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface LongValue {
		long min() default Long.MIN_VALUE;

		long max() default Long.MAX_VALUE;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface DoubleValue {
		double min() default -(Double.MAX_VALUE - 1); // FIXXME: Check

		double max() default Double.MAX_VALUE;
	}

	// ---------------------------------------------------------------
	// Test sequence creation
	/** Constant <code>TEST_EXCLUDES="test.excludes"</code> */
	@Parameter(key = "test_excludes", group = "Test Creation", description = "File containing methods that should not be used in testing")
	public static String TEST_EXCLUDES = "test.excludes";

	/** Constant <code>TEST_INCLUDES="test.includes"</code> */
	@Parameter(key = "test_includes", group = "Test Creation", description = "File containing methods that should be included in testing")
	public static String TEST_INCLUDES = "test.includes";

	/** Constant <code>EVOSUITE_USE_UISPEC=false</code> */
	@Parameter(key = "evosuite_use_uispec", group = "Test Creation", description = "If set to true EvoSuite test generation inits UISpec in order to avoid display of UI")
	public static boolean EVOSUITE_USE_UISPEC = false;

	/** Constant <code>MAKE_ACCESSIBLE=true</code> */
    @Deprecated
	@Parameter(key = "make_accessible", group = "TestCreation", description = "Change default package rights to public package rights")
	public static boolean MAKE_ACCESSIBLE = false;

	/** Constant <code>STRING_REPLACEMENT=true</code> */
	@Parameter(key = "string_replacement", group = "Test Creation", description = "Replace string.equals with levenshtein distance")
	public static boolean STRING_REPLACEMENT = true;

	/** Constant <code>RESET_STATIC_FIELDS =false</code> */
	@Parameter(key = "reset_static_fields", group = "Test Creation", description = "Call static constructors only after each a static field was modified")
	public static boolean RESET_STATIC_FIELDS = true;

	/** Constant <code>RESET_STANDARD_STREAMS =false</code> */
	@Parameter(key = "reset_standard_streams", group = "Test Creation", description = "Restore System.out, System.in and DebugGraphics.logStream after test execution")
	public static boolean RESET_STANDARD_STREAMS = false;

	/**
	 * TODO: this option is off by default because still experimental and not
	 * fully tested
	 */
	@Parameter(key = "test_carving", group = "Test Creation", description = "Enable test carving")
	public static boolean TEST_CARVING = false;

	@Parameter(key = "chop_carved_exceptions", group = "Test Creation", description = "If a carved test throws an exception, either chop it off, or drop it")
	public static boolean CHOP_CARVED_EXCEPTIONS = true;

	/** Constant <code>NULL_PROBABILITY=0.1</code> */
	@Parameter(key = "null_probability", group = "Test Creation", description = "Probability to use null instead of constructing an object")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double NULL_PROBABILITY = 0.1;

	/** Constant <code>OBJECT_REUSE_PROBABILITY=0.9</code> */
	@Parameter(key = "object_reuse_probability", group = "Test Creation", description = "Probability to reuse an existing reference, if available")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double OBJECT_REUSE_PROBABILITY = 0.9;

	/** Constant <code>PRIMITIVE_REUSE_PROBABILITY=0.5</code> */
	@Parameter(key = "primitive_reuse_probability", group = "Test Creation", description = "Probability to reuse an existing primitive, if available")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double PRIMITIVE_REUSE_PROBABILITY = 0.5;

	/** Constant <code>PRIMITIVE_POOL=0.5</code> */
	@Parameter(key = "primitive_pool", group = "Test Creation", description = "Probability to use a primitive from the pool rather than a random value")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double PRIMITIVE_POOL = 0.5;

	/** Constant <code>DYNAMIC_POOL=0.5</code> */
	@Parameter(key = "dynamic_pool", group = "Test Creation", description = "Probability to use a primitive from the dynamic pool rather than a random value")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double DYNAMIC_POOL = 0.5;

	/** Constant <code>DYNAMIC_SEEDING=false</code> */
	@Parameter(key = "dynamic_seeding", group = "Test Creation", description = "Use numeric dynamic seeding")
	public static boolean DYNAMIC_SEEDING = true;

	/** Constant <code>DYNAMIC_POOL_SIZE=50</code> */
	@Parameter(key = "dynamic_pool_size", group = "Test Creation", description = "Number of dynamic constants to keep")
	public static int DYNAMIC_POOL_SIZE = 50;

	@Parameter(key = "p_special_type_call", group = "Test Creation", description = "Probability of using a non-standard call on a special case (collection/numeric)")
    @DoubleValue(min = 0.0, max = 1.0)
	public static double P_SPECIAL_TYPE_CALL = 0.05;

	/** Constant <code>OBJECT_POOL=0.0</code> */
	@Parameter(key = "p_object_pool", group = "Test Creation", description = "Probability to use a predefined sequence from the pool rather than a random generator")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double P_OBJECT_POOL = 0.3;

	@Parameter(key = "object_pools", group = "Test Creation", description = "List of object pools")
	public static String OBJECT_POOLS = "";

	@Parameter(key = "carve_object_pool", group = "Test Creation", description = "Carve junit tests for pool")
	public static boolean CARVE_OBJECT_POOL = false;

	@Parameter(key = "seed_types", group = "Test Creation", description = "Use type information gathered from casts to instantiate generics")
	public static boolean SEED_TYPES = true;

	@Parameter(key = "max_generic_depth", group = "Test Creation", description = "Maximum level of nesting for generic types")
	public static int MAX_GENERIC_DEPTH = 3;

	/** Constant <code>STRING_LENGTH=20</code> */
	@Parameter(key = "string_length", group = "Test Creation", description = "Maximum length of randomly generated strings")
	public static int STRING_LENGTH = 20;

	/** Constant <code>EPSILON=0.001</code> */
	@Parameter(key = "epsilon", group = "Test Creation", description = "Epsilon for floats in local search")
	@Deprecated
	// does not seem to be used anywhere
	public static double EPSILON = 0.001;

	/** Constant <code>MAX_INT=2048</code> */
	@Parameter(key = "max_int", group = "Test Creation", description = "Maximum size of randomly generated integers (minimum range = -1 * max)")
	public static int MAX_INT = 2048;

	/** Constant <code>RESTRICT_POOL=false</code> */
	@Parameter(key = "restrict_pool", group = "Test Creation", description = "Prohibit integers in the pool greater than max_int")
	public static boolean RESTRICT_POOL = false;

	/** Constant <code>MAX_DELTA=20</code> */
	@Parameter(key = "max_delta", group = "Test Creation", description = "Maximum size of delta for numbers during mutation")
	public static int MAX_DELTA = 20;

	/** Constant <code>RANDOM_PERTURBATION=0.2</code> */
	@Parameter(key = "random_perturbation", group = "Test Creation", description = "Probability to replace a primitive with a random new value rather than adding a delta")
	public static double RANDOM_PERTURBATION = 0.2;

	/** Constant <code>MAX_ARRAY=10</code> */
	@Parameter(key = "max_array", group = "Test Creation", description = "Maximum length of randomly generated arrays")
	public static int MAX_ARRAY = 10;

	/** Constant <code>MAX_ATTEMPTS=1000</code> */
	@Parameter(key = "max_attempts", group = "Test Creation", description = "Number of attempts when generating an object before giving up")
	public static int MAX_ATTEMPTS = 1000;

	/** Constant <code>MAX_RECURSION=10</code> */
	@Parameter(key = "max_recursion", group = "Test Creation", description = "Recursion depth when trying to create objects")
	public static int MAX_RECURSION = 10;

	/** Constant <code>MAX_LENGTH=0</code> */
	@Parameter(key = "max_length", group = "Test Creation", description = "Maximum length of test suites (0 = no check)")
	public static int MAX_LENGTH = 0;

	/** Constant <code>MAX_SIZE=100</code> */
	@Parameter(key = "max_size", group = "Test Creation", description = "Maximum number of test cases in a test suite")
	public static int MAX_SIZE = 100;

	/** Constant <code>NUM_TESTS=2</code> */
	@Parameter(key = "num_tests", group = "Test Creation", description = "Number of tests in initial test suites")
	public static int NUM_TESTS = 2;

	@Parameter(key = "num_random_tests", group = "Test Creation", description = "Number of random tests")
	public static int NUM_RANDOM_TESTS = 20;

	/** Constant <code>MIN_INITIAL_TESTS=1</code> */
	@Parameter(key = "min_initial_tests", group = "Test Creation", description = "Minimum number of tests in initial test suites")
	public static int MIN_INITIAL_TESTS = 1;

	/** Constant <code>MAX_INITIAL_TESTS=10</code> */
	@Parameter(key = "max_initial_tests", group = "Test Creation", description = "Maximum number of tests in initial test suites")
	public static int MAX_INITIAL_TESTS = 10;

	/** Constant <code>USE_DEPRECATED=false</code> */
	@Parameter(key = "use_deprecated", group = "Test Creation", description = "Include deprecated methods in tests")
	public static boolean USE_DEPRECATED = false;

	/** Constant <code>INSERTION_SCORE_UUT=1</code> */
	@Parameter(key = "insertion_score_uut", group = "Test Creation", description = "Score for selection of insertion of UUT calls")
	public static int INSERTION_SCORE_UUT = 1;

	@Parameter(key = "insertion_uut", group = "Test Creation", description = "Score for selection of insertion of UUT calls")
	public static double INSERTION_UUT = 0.5;

	@Parameter(key = "new_object_selection", group = "Test Creation", description = "Score for selection of insertion of UUT calls")
	public static boolean NEW_OBJECT_SELECTION = true;

	/** Constant <code>INSERTION_SCORE_OBJECT=1</code> */
	@Parameter(key = "insertion_score_object", group = "Test Creation", description = "Score for selection of insertion of call on existing object")
	public static int INSERTION_SCORE_OBJECT = 1;

	/** Constant <code>INSERTION_SCORE_PARAMETER=1</code> */
	@Parameter(key = "insertion_score_parameter", group = "Test Creation", description = "Score for selection of insertion call with existing object")
	public static int INSERTION_SCORE_PARAMETER = 1;

	@Parameter(key = "consider_main_methods", group = "Test Creation", description = "Generate unit tests for 'main(String[] args)' methods as well")
	public static boolean CONSIDER_MAIN_METHODS = true; //should be on by default, otherwise unnecessary lower coverage: up to user if wants to skip them

	@Parameter(key = "headless_mode", group = "Test Generation", description = "Run Java in AWT Headless mode")
	public static boolean HEADLESS_MODE = true;

    @Parameter(key = "p_reflection_on_private", group = "Test Creation", description = "Probability [0,1] of using reflection to set private fields or call private methods")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double P_REFLECTION_ON_PRIVATE = 0.0; // TODO off by default. likely need something like 0.5

    @Parameter(key = "reflection_start_percent", group = "Test Creation", description = "Percentage [0,1] of search budget after which reflection fields/methods handling is activated")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double REFLECTION_START_PERCENT = 0.5;



    // ---------------------------------------------------------------
	// Search algorithm
	public enum Algorithm {
		STANDARDGA, MONOTONICGA, ONEPLUSONEEA, STEADYSTATEGA, RANDOM, NSGAII
	}

	/** Constant <code>ALGORITHM</code> */
	@Parameter(key = "algorithm", group = "Search Algorithm", description = "Search algorithm")
	public static Algorithm ALGORITHM = Algorithm.MONOTONICGA;

	/** Constant <code>RANDOM_SEED</code> */
	@Parameter(key = "random_seed", group = "Search Algorithm", description = "Seed used for random generator. If left empty, use current time")
	public static Long RANDOM_SEED = null;

	/** Constant <code>CHECK_BEST_LENGTH=true</code> */
	@Parameter(key = "check_best_length", group = "Search Algorithm", description = "Check length against length of best individual")
	public static boolean CHECK_BEST_LENGTH = true;

	/** Constant <code>CHECK_PARENTS_LENGTH=false</code> */
	@Parameter(key = "check_parents_length", group = "Search Algorithm", description = "Check length against length of parents")
	public static boolean CHECK_PARENTS_LENGTH = false; // note, based on STVR experiments

	// @Parameter(key = "check_rank_length", group = "Search Algorithm", description = "Use length in rank selection")
	// public static boolean CHECK_RANK_LENGTH = false;

	/** Constant <code>PARENT_CHECK=true</code> */
	@Parameter(key = "parent_check", group = "Search Algorithm", description = "Check against parents in Mu+Lambda algorithm")
	public static boolean PARENT_CHECK = true;

	/** Constant <code>CHECK_MAX_LENGTH=true</code> */
	@Parameter(key = "check_max_length", group = "Search Algorithm", description = "Check length against fixed maximum")
	public static boolean CHECK_MAX_LENGTH = true;

	@Parameter(key = "chop_max_length", group = "Search Algorithm", description = "Chop statements after exception if length has reached maximum")
	public static boolean CHOP_MAX_LENGTH = true;

	//----------- DSE, which is a special case of LS ---------------

	@Parameter(key = "dse_probability", group = "DSE", description = "Probability used to specify when to use DSE instead of regular LS when LS is applied")
    @DoubleValue(min = 0.0, max = 1.0)
	public static double DSE_PROBABILITY = 0.5;

	/** Constant <code>DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS=0</code> */
	@Parameter(key = "dse_constraint_solver_timeout_millis", group = "DSE", description = "Maximum number of solving time for Constraint solver in milliseconds")
	public static long DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS = 1000;

	/** Constant <code>DSE_RANK_BRANCH_CONDITIONS=false</code> */
	@Parameter(key = "dse_rank_branch_conditions", group = "DSE", description = "Rank branch conditions")
	public static boolean DSE_RANK_BRANCH_CONDITIONS = true;

	/** Constant <code>DSE_NEGATE_ALL_CONDITIONS=false</code> */
	@Parameter(key = "dse_negate_all_conditions", group = "DSE", description = "Negate all branch conditions in the path condition (covered or not)")
	public static boolean DSE_NEGATE_ALL_CONDITIONS = true;

	/** Constant <code>DSE_CONSTRAINT_LENGTH=100000</code> */
	@Parameter(key = "dse_constraint_length", group = "DSE", description = "Maximal length of the constraints in DSE")
	public static int DSE_CONSTRAINT_LENGTH = 100000;

	@Parameter(key = "dse_constant_probability", group = "DSE", description = "Probability with which to use constants from the constraints when resetting variables during search")
    @DoubleValue(min = 0.0, max = 1.0)
	public static double DSE_CONSTANT_PROBABILITY = 0.5;

	/** Constant <code>DSE_VARIABLE_RESETS=1</code> */
	@Parameter(key = "dse_variable_resets", group = "DSE", description = "Times DSE resets the int and real variables with random values")
	public static int DSE_VARIABLE_RESETS = 2;

	public enum DSEType {
		/** apply DSE per primitive */
		STATEMENT,
		/** apply DSE with all primitives in a test */
		TEST,
		/** DSE on whole suites */
		SUITE;
	}

	@Parameter(key = "local_search_dse", group = "DSE", description = "Granularity of DSE application")
	public static DSEType LOCAL_SEARCH_DSE = DSEType.TEST;

	@Parameter(key = "dse_keep_all_tests", group = "DSE", description = "Keep tests even if they do not increase fitness")
	public static boolean DSE_KEEP_ALL_TESTS = false;

	public enum SolverType {
		EVOSUITE_SOLVER, Z3_SOLVER, Z3_STR2_SOLVER, CVC4_SOLVER;
	}

	@Parameter(key = "dse_solver", group = "DSE", description = "Specify which constraint solver to use. Note: external solver will need to be installed and cofigured separately")
	public static SolverType DSE_SOLVER = SolverType.EVOSUITE_SOLVER;

	@Parameter(key = "z3_path", group = "DSE", description = "Indicates the path to the Z3 solver")
	public static String Z3_PATH = null;

	@Parameter(key = "z3_str2_path", group = "DSE", description = "Indicates the path to the Z3-Str2 solver")
	public static String Z3_STR2_PATH = null;

	@Parameter(key = "cvc4_path", group = "DSE", description = "Indicates the path to the CVC4 solver")
	public static String CVC4_PATH = null;


	// --------- LS ---------

	/** Constant <code>LOCAL_SEARCH_RATE=-1</code> */
	@Parameter(key = "local_search_rate", group = "Local Search", description = "Apply local search at every X generation")
	public static int LOCAL_SEARCH_RATE = -1;

	@Parameter(key = "local_search_probability", group = "Local Search", description = "Probability of applying local search at every X generation")
    @DoubleValue(min = 0.0, max = 1.0)
	public static double LOCAL_SEARCH_PROBABILITY = 1.0;

	@Parameter(key = "local_search_selective", group = "Local Search", description = "Apply local search only to individuals that changed fitness")
	public static boolean LOCAL_SEARCH_SELECTIVE = false;

	@Parameter(key = "local_search_selective_primitives", group = "Local Search", description = "Only check primitives for selective LS")
	public static boolean LOCAL_SEARCH_SELECTIVE_PRIMITIVES = false; //TODO what is this? unclear

	@Parameter(key = "local_search_expand_tests", group = "Local Search", description = "Expand test cases before applying local search such that each primitive is used only once")
	public static boolean LOCAL_SEARCH_EXPAND_TESTS = true;

	@Parameter(key = "local_search_ensure_double_execution", group = "Local Search", description = "If a branch is only executed once by a test suite, duplicate that test")
	public static boolean LOCAL_SEARCH_ENSURE_DOUBLE_EXECUTION = true;

	@Parameter(key = "local_search_restore_coverage", group = "Local Search", description = "Add tests that cover branches already covered in the past")
	public static boolean LOCAL_SEARCH_RESTORE_COVERAGE = false; // Not needed with archive

	@Parameter(key = "local_search_adaptation_rate", group = "Local Search", description = "Parameter used to adapt at runtime the probability of applying local search")
	public static double LOCAL_SEARCH_ADAPTATION_RATE = 0.33;

	@Parameter(key = "local_search_budget", group = "Local Search", description = "Maximum budget usable for improving individuals per local search")
	public static long LOCAL_SEARCH_BUDGET = 5;

	public enum LocalSearchBudgetType {
		STATEMENTS, TESTS,
		/** Time expressed in seconds */
		TIME,
		SUITES, FITNESS_EVALUATIONS
	}

	/** Constant <code>LOCAL_SEARCH_BUDGET_TYPE</code> */
	@Parameter(key = "local_search_budget_type", group = "Local Search", description = "Interpretation of local_search_budget")
	public static LocalSearchBudgetType LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.TIME;

	/** Constant <code>LOCAL_SEARCH_PROBES=10</code> */
	@Parameter(key = "local_search_probes", group = "Local Search", description = "How many mutations to apply to a string to check whether it improves coverage")
	public static int LOCAL_SEARCH_PROBES = 10;

	@Parameter(key = "local_search_primitives", group = "Local Search", description = "Perform local search on primitive values")
	public static boolean LOCAL_SEARCH_PRIMITIVES = true;

	@Parameter(key = "local_search_strings", group = "Local Search", description = "Perform local search on primitive values")
	public static boolean LOCAL_SEARCH_STRINGS = true;

	@Parameter(key = "local_search_arrays", group = "Local Search", description = "Perform local search on array statements")
	public static boolean LOCAL_SEARCH_ARRAYS = true;

	@Parameter(key = "local_search_references", group = "Local Search", description = "Perform local search on reference types")
	public static boolean LOCAL_SEARCH_REFERENCES = true;

	//--------------------------

	/** Constant <code>CROSSOVER_RATE=0.75</code> */
	@Parameter(key = "crossover_rate", group = "Search Algorithm", description = "Probability of crossover")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double CROSSOVER_RATE = 0.75;

	/** Constant <code>HEADLESS_CHICKEN_TEST=false</code> */
	@Parameter(key = "headless_chicken_test", group = "Search Algorithm", description = "Activate headless chicken test")
	public static boolean HEADLESS_CHICKEN_TEST = false;

	/** Constant <code>MUTATION_RATE=0.75</code> */
	@Parameter(key = "mutation_rate", group = "Search Algorithm", description = "Probability of mutation")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double MUTATION_RATE = 0.75;

	/** Constant <code>NUMBER_OF_MUTATIONS=1</code> */
	@Parameter(key = "number_of_mutations", group = "Search Algorithm", description = "Number of single mutations applied on an individual when a mutation event occurs")
	public static int NUMBER_OF_MUTATIONS = 1;

	/** Constant <code>P_TEST_INSERTION=0.1</code> */
	@Parameter(key = "p_test_insertion", group = "Search Algorithm", description = "Initial probability of inserting a new test in a test suite")
    @DoubleValue(min = 0.0, max = 1.0)
	public static double P_TEST_INSERTION = 0.1;

	/** Constant <code>P_STATEMENT_INSERTION=0.5</code> */
	@Parameter(key = "p_statement_insertion", group = "Search Algorithm", description = "Initial probability of inserting a new statement in a test case")
    @DoubleValue(min = 0.0, max = 1.0)
	public static double P_STATEMENT_INSERTION = 0.5;

	/** Constant <code>P_CHANGE_PARAMETER=0.1</code> */
	@Parameter(key = "p_change_parameter", group = "Search Algorithm", description = "Initial probability of inserting a new statement in a test case")
    @DoubleValue(min = 0.0, max = 1.0)
	public static double P_CHANGE_PARAMETER = 0.1;

	/** Constant <code>P_TEST_DELETE=1d / 3d</code> */
	@Parameter(key = "p_test_delete", group = "Search Algorithm", description = "Probability of deleting statements during mutation")
    @DoubleValue(min = 0.0, max = 1.0)
	public static double P_TEST_DELETE = 1d / 3d;

	/** Constant <code>P_TEST_CHANGE=1d / 3d</code> */
	@Parameter(key = "p_test_change", group = "Search Algorithm", description = "Probability of changing statements during mutation")
    @DoubleValue(min = 0.0, max = 1.0)
	public static double P_TEST_CHANGE = 1d / 3d;

	/** Constant <code>P_TEST_INSERT=1d / 3d</code> */
	@Parameter(key = "p_test_insert", group = "Search Algorithm", description = "Probability of inserting new statements during mutation")
    @DoubleValue(min = 0.0, max = 1.0)
	public static double P_TEST_INSERT = 1d / 3d;

	/** Constant <code>KINCOMPENSATION=1.0</code> */
	@Parameter(key = "kincompensation", group = "Search Algorithm", description = "Penalty for duplicate individuals")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double KINCOMPENSATION = 1.0;

	/** Constant <code>ELITE=1</code> */
	@Parameter(key = "elite", group = "Search Algorithm", description = "Elite size for search algorithm")
	public static int ELITE = 1;

	/** Constant <code>TOURNAMENT_SIZE=10</code> */
	@Parameter(key = "tournament_size", group = "Search Algorithm", description = "Number of individuals for tournament selection")
	public static int TOURNAMENT_SIZE = 10;

	/** Constant <code>RANK_BIAS=1.7</code> */
	@Parameter(key = "rank_bias", group = "Search Algorithm", description = "Bias for better individuals in rank selection")
	public static double RANK_BIAS = 1.7;

	/** Constant <code>CHROMOSOME_LENGTH=40</code> */
	@Parameter(key = "chromosome_length", group = "Search Algorithm", description = "Maximum length of chromosomes during search")
	@IntValue(min = 1, max = 100000)
	public static int CHROMOSOME_LENGTH = 40;

	/** Constant <code>POPULATION=50</code> */
	@Parameter(key = "population", group = "Search Algorithm", description = "Population size of genetic algorithm")
	@IntValue(min = 1)
	public static int POPULATION = 50;

	public enum PopulationLimit {
		INDIVIDUALS, TESTS, STATEMENTS;
	}

	/** Constant <code>POPULATION_LIMIT</code> */
	@Parameter(key = "population_limit", group = "Search Algorithm", description = "What to use as limit for the population size")
	public static PopulationLimit POPULATION_LIMIT = PopulationLimit.INDIVIDUALS;

	/** Constant <code>SEARCH_BUDGET=60</code> */
	@Parameter(key = "search_budget", group = "Search Algorithm", description = "Maximum search duration")
	@LongValue(min = 1)
	public static long SEARCH_BUDGET = 60;

	/** Constant <code>OUTPUT_DIR="evosuite-files"</code> */
	@Parameter(key = "OUTPUT_DIR", group = "Runtime", description = "Directory in which to put generated files")
	public static String OUTPUT_DIR = "evosuite-files";

	/**
	 * Constant
	 * <code>PROPERTIES_FILE="OUTPUT_DIR + File.separatorevosuite.pro"{trunked}</code>
	 */
	public static String PROPERTIES_FILE = OUTPUT_DIR + File.separator + "evosuite.properties";

	public enum StoppingCondition {
		MAXSTATEMENTS, MAXTESTS,
        /** Max time in seconds */ MAXTIME,
        MAXGENERATIONS, MAXFITNESSEVALUATIONS, TIMEDELTA
	}

	/** Constant <code>STOPPING_CONDITION</code> */
	@Parameter(key = "stopping_condition", group = "Search Algorithm", description = "What condition should be checked to end the search")
	public static StoppingCondition STOPPING_CONDITION = StoppingCondition.MAXTIME;

	public enum CrossoverFunction {
		SINGLEPOINTRELATIVE, SINGLEPOINTFIXED, SINGLEPOINT, COVERAGE
	}

	/** Constant <code>CROSSOVER_FUNCTION</code> */
	@Parameter(key = "crossover_function", group = "Search Algorithm", description = "Crossover function during search")
	public static CrossoverFunction CROSSOVER_FUNCTION = CrossoverFunction.SINGLEPOINTRELATIVE;

	public enum TheReplacementFunction {
		/**
		 * Indicates a replacement function which works for all chromosomes
		 * because it solely relies on fitness values.
		 */
		FITNESSREPLACEMENT,
		/**
		 * EvoSuite's default replacement function which only works on subtypes
		 * of the default chromosome types. Relies on fitness plus secondary
		 * goals such as length.
		 */
		DEFAULT
	}

	/**
	 * During search the genetic algorithm has to decide whether the parent
	 * chromosomes or the freshly created offspring chromosomes should be
	 * preferred. If you use EvoSuite with its default chromosomes the
	 * TheReplacementFunction.DEFAULT is what you want. If your chromosomes are
	 * not a subclass of the default chromosomes your have to write your own
	 * replacement function or use TheReplacementFunction.FITNESSREPLACEMENT.
	 */
	@Parameter(key = "replacement_function", group = "Search Algorithm", description = "Replacement function for comparing offspring to parents during search")
	public static TheReplacementFunction REPLACEMENT_FUNCTION = TheReplacementFunction.DEFAULT;

	public enum SelectionFunction {
		RANK, ROULETTEWHEEL, TOURNAMENT, BINARY_TOURNAMENT
	}

	/** Constant <code>SELECTION_FUNCTION</code> */
	@Parameter(key = "selection_function", group = "Search Algorithm", description = "Selection function during search")
	public static SelectionFunction SELECTION_FUNCTION = SelectionFunction.RANK;

	// TODO: Fix values
	/** Constant <code>SECONDARY_OBJECTIVE="totallength"</code> */
	@Parameter(key = "secondary_objectives", group = "Search Algorithm", description = "Secondary objective during search")
	// @SetValue(values = { "maxlength", "maxsize", "avglength", "none" })
	public static String SECONDARY_OBJECTIVE = "totallength";

	@Parameter(key = "enable_secondary_objective_after", group = "Search Algorithm", description = "Activate the second secondary objective after a certain amount of search budget")
	public static int ENABLE_SECONDARY_OBJECTIVE_AFTER = 0;

	@Parameter(key = "enable_secondary_starvation", group = "Search Algorithm", description = "Activate the second secondary objective after a certain amount of search budget")
	public static boolean ENABLE_SECONDARY_OBJECTIVE_STARVATION = false;

	@Parameter(key = "starvation_after_generation", group = "Search Algorithm", description = "Activate the second secondary objective after a certain amount of search budget")
	public static int STARVATION_AFTER_GENERATION = 500;

	/** Constant <code>BLOAT_FACTOR=2</code> */
	@Parameter(key = "bloat_factor", group = "Search Algorithm", description = "Maximum relative increase in length")
	public static int BLOAT_FACTOR = 2;

	/** Constant <code>STOP_ZERO=true</code> */
	@Parameter(key = "stop_zero", group = "Search Algorithm", description = "Stop optimization once goal is covered")
	public static boolean STOP_ZERO = true;

	/** Constant <code>DYNAMIC_LIMIT=false</code> */
	@Parameter(key = "dynamic_limit", group = "Search Algorithm", description = "Multiply search budget by number of test goals")
	public static boolean DYNAMIC_LIMIT = false;

	/** Constant <code>GLOBAL_TIMEOUT=600</code> */
	@Parameter(key = "global_timeout", group = "Search Algorithm", description = "Maximum seconds allowed for entire search when not using time as stopping criterion")
	@IntValue(min = 0)
	public static int GLOBAL_TIMEOUT = 120;

	/** Constant <code>MINIMIZATION_TIMEOUT=600</code> */
	@Parameter(key = "minimization_timeout", group = "Search Algorithm", description = "Seconds allowed for minimization at the end")
	@IntValue(min = 0)
	public static int MINIMIZATION_TIMEOUT = 60;

    @Parameter(key = "assertion_timeout", group = "Search Algorithm", description = "Seconds allowed for assertion generation at the end")
    @IntValue(min = 0)
    public static int ASSERTION_TIMEOUT = 60;

	@Parameter(key = "junit_check_timeout", group = "Search Algorithm", description = "Seconds allowed for checking the generated JUnit files (e.g., compilation and stability)")
	@IntValue(min = 0)
	public static int JUNIT_CHECK_TIMEOUT = 60;

	@Parameter(key = "write_junit_timeout", group = "Search Algorithm", description = "Seconds allowed to write on disk the generated JUnit files")
	@IntValue(min = 0)
	public static int WRITE_JUNIT_TIMEOUT = 60; //Note: we need it, as we currently first run the tests before we write them

	@Parameter(key = "carving_timeout", group = "Search Algorithm", description = "Seconds allowed for carving JUnit tests")
	@IntValue(min = 0)
	public static int CARVING_TIMEOUT = 120;

	@Parameter(key = "initialization_timeout", group = "Search Algorithm", description = "Seconds allowed for initializing the search")
	@IntValue(min = 0)
	public static int INITIALIZATION_TIMEOUT = 120;

	@Parameter(key = "extra_timeout", group = "Search Algorithm", description = "Extra seconds allowed for the search")
	@IntValue(min = 0)
	public static int EXTRA_TIMEOUT = 60;

	@Parameter(key = "track_boolean_branches", group = "Search Algorithm", description = "Track branches that have a distance of either 0 or 1")
	public static boolean TRACK_BOOLEAN_BRANCHES = false;

	@Parameter(key = "track_covered_gradient_branches", group = "Search Algorithm", description = "Track gradient branches that were covered")
	public static boolean TRACK_COVERED_GRADIENT_BRANCHES = false;

	@Parameter(key = "branch_comparison_types", group = "Search Algorithm", description = "Track branch comparison types based on the bytecode")
	public static boolean BRANCH_COMPARISON_TYPES = false;


	@Parameter(key = "analysis_criteria", group = "Output", description = "List of criteria which should be measured on the completed test suite")
	public static String ANALYSIS_CRITERIA = "";

	//----------------------------------------------------------------
	// Continuous Test Generation

	@Parameter(key = "ctg_memory", group = "Continuous Test Generation", description = "Total Memory (in MB) that CTG will use")
	public static int CTG_MEMORY = 1000;

	@Parameter(key = "ctg_cores", group = "Continuous Test Generation", description = "Number of cores CTG will use")
	public static int CTG_CORES = 1;

	@Parameter(key = "ctg_time", group = "Continuous Test Generation", description = "How many minutes in total CTG will run")
	public static int CTG_TIME = 1;

	@Parameter(key = "ctg_time_per_class", group = "Continuous Test Generation", description = "How many minutes to allocate for each class. If this parameter is set, then ctg_time is going to be ignored. This parameter is mainly meant for debugging purposes.")
	public static Integer CTG_TIME_PER_CLASS = null;

	@Parameter(key = "ctg_min_time_per_job", group = "Continuous Test Generation", description = "How many minutes each class under test should have at least")
	public static int CTG_MIN_TIME_PER_JOB = 1;

	@Parameter(key = "ctg_folder", group = "Continuous Test Generation", description = "Where generated files will be stored")
	public static String CTG_DIR = ".evosuite";

	@Parameter(key = "ctg_bests_folder", group = "Continuous Test Generation", description = "Folder where all the best test suites generated so far in all CTG runs are stored")
	public static String CTG_BESTS_DIR = CTG_DIR + File.separator + "evosuite-tests";

	@Parameter(key = "ctg_generation_dir_prefix", group = "Continuous Test Generation", description = "")
	public static String CTG_GENERATION_DIR_PREFIX = null;

	@Parameter(key = "ctg_logs_dir", group = "Continuous Test Generation", description = "")
	public static String CTG_LOGS_DIR = "logs";

	@Parameter(key = "ctg_pools_dir", group = "Continuous Test Generation", description = "")
	public static String CTG_POOLS_DIR = "pools";

	@Parameter(key = "ctg_reports_dir", group = "Continuous Test Generation", description = "")
	public static String CTG_REPORTS_DIR = "reports";

	@Parameter(key = "ctg_seeds_dir", group = "Continuous Test Generation", description = "")
	public static String CTG_SEEDS_DIR = CTG_DIR + File.separator + "evosuite-seeds";

	@Parameter(key = "ctg_tests_dir", group = "Continuous Test Generation", description = "")
	public static String CTG_TESTS_DIR = "tests";

	@Parameter(key = "ctg_project_info", group = "Continuous Test Generation", description = "XML file which stores stats about all CTG executions")
	public static String CTG_PROJECT_INFO = CTG_DIR + File.separator + "project_info.xml";

	@Parameter(key = "ctg_history_file", group = "Continuous Test Generation", description = "File with the list of new(A)/modified(M)/deleted(D) files")
	public static String CTG_HISTORY_FILE = null;

	@Parameter(key = "ctg_selected_cuts", group = "Continuous Test Generation", description = "Comma ',' separated list of CUTs to use in CTG. If none specified, then test all classes")
	public static String CTG_SELECTED_CUTS = null;

	@Parameter(key = "ctg_export_folder", group = "Continuous Test Generation", description = "If specified, make a copy of all tests into the target export folder")
	public static String CTG_EXPORT_FOLDER = null;


	/**
	 * The types of CTG schedules that can be used
	 */
	public enum AvailableSchedule {
		SIMPLE, BUDGET, SEEDING, BUDGET_AND_SEEDING, HISTORY
	};

	/*
	 * FIXME choose best schedule for default
	 * Note: most likely we ll use this parameter only for testing/experiments.
	 * Maven plugin will use the default, best one
	 */
	@Parameter(key = "ctg_schedule", group = "Continuous Test Generation", description = "Schedule used to run jobs")
	public static AvailableSchedule CTG_SCHEDULE = AvailableSchedule.BUDGET;

	// ---------------------------------------------------------------
	// Single branch mode
	/** Constant <code>RANDOM_TESTS=0</code> */
	@Parameter(key = "random_tests", group = "Single Branch Mode", description = "Number of random tests to run before test generation (Single branch mode)")
	public static int RANDOM_TESTS = 0;

	/** Constant <code>SKIP_COVERED=true</code> */
	@Parameter(key = "skip_covered", group = "Single Branch Mode", description = "Skip coverage goals that have already been (coincidentally) covered")
	public static boolean SKIP_COVERED = true;

	/** Constant <code>REUSE_BUDGET=true</code> */
	@Parameter(key = "reuse_budget", group = "Single Branch Mode", description = "Use leftover budget on unsatisfied test goals (Single branch mode)")
	public static boolean REUSE_BUDGET = true;

	/** Constant <code>SHUFFLE_GOALS=true</code> */
	@Parameter(key = "shuffle_goals", group = "Single Branch Mode", description = "Shuffle test goals before test generation (Single branch mode)")
	public static boolean SHUFFLE_GOALS = true;

	/** Constant <code>RECYCLE_CHROMOSOMES=true</code> */
	@Parameter(key = "recycle_chromosomes", group = "Single Branch Mode", description = "Seed initial population with related individuals (Single branch mode)")
	public static boolean RECYCLE_CHROMOSOMES = true;

	// ---------------------------------------------------------------
	// Output
	public enum OutputFormat {
		JUNIT3, JUNIT4, TESTNG
	}

	/** Constant <code>TEST_FORMAT</code> */
	@Parameter(key = "test_format", group = "Output", description = "Format of the resulting test cases")
	public static OutputFormat TEST_FORMAT = OutputFormat.JUNIT4;

	@Parameter(key = "test_comments", group = "Output", description = "Include a header with coverage information for each test")
	public static boolean TEST_COMMENTS = true;

	@Parameter(key = "test_scaffolding", group = "Output", description = "Generate all the scaffolding needed to run EvoSuite JUnit tests in a separate file")
	public static boolean TEST_SCAFFOLDING = true;

	@Parameter(key = "no_runtime_dependency", group = "Output", description = "Avoid runtime dependencies in JUnit test")
	public static boolean NO_RUNTIME_DEPENDENCY = false;

	/** Constant <code>PRINT_TO_SYSTEM=false</code> */
	@Parameter(key = "print_to_system", group = "Output", description = "Allow test output on console")
	public static boolean PRINT_TO_SYSTEM = false;

	/** Constant <code>PLOT=false</code> */
	@Parameter(key = "plot", group = "Output", description = "Create plots of size and fitness")
	public static boolean PLOT = false;

	/** Constant <code>HTML=true</code> */
	@Parameter(key = "html", group = "Output", description = "Create html reports")
	public static boolean HTML = true;

	/** Constant <code>COVERAGE_MATRIX=true</code> */
	@Parameter(key = "coverage_matrix", group = "Output", description = "Create coverage matrix")
	public static boolean COVERAGE_MATRIX = false;

	/** Constant <code>JUNIT_TESTS=true</code> */
	@Parameter(key = "junit_tests", group = "Output", description = "Create JUnit test suites")
	public static boolean JUNIT_TESTS = true;

	@Parameter(key = "junit_check", group = "Output", description = "Compile and run resulting JUnit test suite (if any was created)")
	public static boolean JUNIT_CHECK = true;

	@Parameter(key = "junit_check_on_separate_process", group = "Output", description = "Compile and run resulting JUnit test suite on a separate process")
	@Deprecated
	//this gives quite a few issues. and hopefully the problems it was aimed to fix are no longer
	public static boolean JUNIT_CHECK_ON_SEPARATE_PROCESS = false;

	@Parameter(key = "junit_suffix", group = "Output", description = "Suffix that is appended at each generated JUnit file name")
	public static String JUNIT_SUFFIX = "_ESTest";

	@Parameter(key = "scaffolding_suffix", group = "Output", description = "Suffix used to specify scaffolding files")
	public static String SCAFFOLDING_SUFFIX = "scaffolding";

	@Parameter(key = "tools_jar_location", group = "Output", description = "Location of where to locate tools.jar")
	public static String TOOLS_JAR_LOCATION = null;
//	public static String TOOLS_JAR_LOCATION = "/Library/Java/JavaVirtualMachines/jdk1.7.0_51.jdk/Contents/Home/lib";

	@Parameter(key = "pure_inspectors", group = "Output", description = "Selects only an underapproximation of all inspectors that are also pure (no side-effects)")
	public static boolean PURE_INSPECTORS = true;

	@Parameter(key = "pure_equals", group = "Output", description = "Selects only an underapproximation of equals(Object) that are also known to be pure (no side-effects)")
	public static boolean PURE_EQUALS = false;

	/**
	 * TODO: this functionality is not implemented yet
	 */
	@Parameter(key = "junit_extend", group = "Output", description = "Extend existing JUnit test suite")
	public static String JUNIT_EXTEND = "";

	/** Constant <code>JUNIT_PREFIX=""</code> */
	@Parameter(key = "junit_prefix", group = "Experimental", description = "Prefix of JUnit tests to execute")
	public static String JUNIT_PREFIX = "";

	/** Constant <code>LOG_GOALS=false</code> */
	@Parameter(key = "log_goals", group = "Output", description = "Create a CSV file for each individual evolution")
	public static boolean LOG_GOALS = false;

	@Parameter(key = "log.level", group = "Output", description = "Verbosity level of logger")
	public static String LOG_LEVEL = null;

	@Parameter(key = "log.target", group = "Output", description = "Target logger - all logging if not set")
	public static String LOG_TARGET = null;

	/** Constant <code>MINIMIZE=true</code> */
	@Parameter(key = "minimize", group = "Output", description = "Minimize test suite after generation")
	public static boolean MINIMIZE = true;

	/** Constant <code>MINIMIZE_SECOND_PASS=true</code> */
	@Parameter(key = "minimize_second_pass", group = "Output", description = "Minimize test suite after generation")
	public static boolean MINIMIZE_SECOND_PASS = true;

    /** Constant <code>MINIMIZE_SORT=true</code> */
    @Parameter(key = "minimize_sort", group = "Output", description = "Sort goals before Minimization")
    public static boolean MINIMIZE_SORT = true;


    /** Constant <code>MINIMIZE_SKIP_COINCIDENTAL=true</code> */
	@Parameter(key = "minimize_skip_coincidental", group = "Output", description = "Minimize test suite after generation")
	public static boolean MINIMIZE_SKIP_COINCIDENTAL = true;

	/** Constant <code>MINIMIZE_OLD=false</code> */
	@Parameter(key = "minimize_old", group = "Output", description = "Minimize test suite using old algorithm")
	@Deprecated
	public static boolean MINIMIZE_OLD = false;

	/** Constant <code>MINIMIZE_VALUES=false</code> */
	@Parameter(key = "minimize_values", group = "Output", description = "Minimize constants and method calls")
	public static boolean MINIMIZE_VALUES = false;

	/** Constant <code>COVERAGE=true</code> */
	@Parameter(key = "coverage", group = "Output", description = "Minimize test suite after generation")
	public static boolean COVERAGE = true;

	/** Constant <code>INLINE=false</code> */
	@Parameter(key = "inline", group = "Output", description = "Inline all constants")
	public static boolean INLINE = false;

	/** Constant <code>WRITE_POOL=false</code> */
	@Parameter(key = "write_pool", group = "Output", description = "Keep sequences for object pool")
	public static String WRITE_POOL = "";

	/** Constant <code>REPORT_DIR="evosuite-report"</code> */
	@Parameter(key = "report_dir", group = "Output", description = "Directory in which to put HTML and CSV reports")
	public static String REPORT_DIR = "evosuite-report";

	/** Constant <code>OUTPUT_VARIABLES=null</code> */
	@Parameter(key = "output_variables", group = "Output", description = "List of variables to output to CSV file. Variables are separated by commas. Null represents default values")
	public static String OUTPUT_VARIABLES = null;

	/** Constant <code>CONFIGURATION_ID=null</code> */
	@Parameter(key = "configuration_id", group = "Output", description = "Label that identifies the used configuration of EvoSuite. This is only done when running experiments.")
	public static String CONFIGURATION_ID = null;

	/** Constant <code>GROUP_ID="none"</code> */
	@Parameter(key = "group_id", group = "Output", description = "Label that specifies a group the SUT belongs to. This is only needed for running experiments.")
	public static String GROUP_ID = "none";

	/** Constant <code>SAVE_ALL_DATA=true</code> */
	@Parameter(key = "save_all_data", group = "Output", description = "Generate and store all data reports")
	public static boolean SAVE_ALL_DATA = true;

	/** Constant <code>PRINT_GOALS=false</code> */
	@Parameter(key = "print_goals", group = "Output", description = "Print out goals of class under test")
	public static boolean PRINT_GOALS = false;

	/** Constant <code>PRINT_CURRENT_GOALS=false</code> */
	@Parameter(key = "print_current_goals", group = "Output", description = "Print out current goal during test generation")
	public static boolean PRINT_CURRENT_GOALS = false;

	/** Constant <code>PRINT_COVERED_GOALS=false</code> */
	@Parameter(key = "print_covered_goals", group = "Output", description = "Print out covered goals during test generation")
	public static boolean PRINT_COVERED_GOALS = false;

	/** Constant <code>ASSERTIONS=false</code> */
	@Parameter(key = "assertions", group = "Output", description = "Create assertions")
	public static boolean ASSERTIONS = true;

	public enum AssertionStrategy {
		ALL, MUTATION, UNIT, STRUCTURED
	}

	/** Constant <code>ASSERTION_STRATEGY</code> */
	@Parameter(key = "assertion_strategy", group = "Output", description = "Which assertions to generate")
	public static AssertionStrategy ASSERTION_STRATEGY = AssertionStrategy.MUTATION;

	@Parameter(key = "filter_assertions", group = "Output", description = "Filter flaky assertions")
	public static boolean FILTER_ASSERTIONS = false;

	@Parameter(key = "max_mutants_per_test", group = "Output", description = "How many mutants to use when trying to find assertions for a test")
	public static int MAX_MUTANTS_PER_TEST = 100;

	@Parameter(key = "max_mutants_per_method", group = "Output", description = "How many mutants can be inserted into a single method")
	public static int MAX_MUTANTS_PER_METHOD = 700;

	@Parameter(key = "max_replace_mutants", group = "Output", description = "How many replacement mutants can be inserted for any one variable")
	public static int MAX_REPLACE_MUTANTS = 100;

	/** Constant <code>TEST_DIR="evosuite-tests"</code> */
	@Parameter(key = "test_dir", group = "Output", description = "Directory in which to place JUnit tests")
	public static String TEST_DIR = "evosuite-tests";

	/** Constant <code>WRITE_CFG=false</code> */
	@Parameter(key = "write_cfg", group = "Output", description = "Create CFG graphs")
	public static boolean WRITE_CFG = false;

	/** Constant <code>SHUTDOWN_HOOK=true</code> */
	@Parameter(key = "shutdown_hook", group = "Output", description = "Store test suite on Ctrl+C")
	public static boolean SHUTDOWN_HOOK = true;

	/** Constant <code>SHOW_PROGRESS=true</code> */
	@Parameter(key = "show_progress", group = "Output", description = "Show progress bar on console")
	public static boolean SHOW_PROGRESS = true;

	/** Constant <code>SERIALIZE_RESULT=false</code> */
	@Parameter(key = "serialize_result", group = "Output", description = "Serialize result of search to main process")
	public static boolean SERIALIZE_RESULT = false;

	@Parameter(key = "new_statistics", group = "Output", description = "Use the new statistics backend on the master")
	public static boolean NEW_STATISTICS = true;

	//@Parameter(key = "old_statistics", group = "Output", description = "Use the old statistics backend on the master")
	//public static boolean OLD_STATISTICS = false;

    @Parameter(key = "validate_runtime_variables", group = "Output", description = "Validate runtime values before writing statistics")
    public static boolean VALIDATE_RUNTIME_VARIABLES = true;

	@Parameter(key = "serialize_ga", group = "Output", description = "Include the GA instance in the test generation result")
	public static boolean SERIALIZE_GA = false;

	public enum StatisticsBackend {
		NONE, CONSOLE, CSV, HTML, DEBUG;
	}

	@Parameter(key = "statistics_backend", group = "Output", description = "Which backend to use to collect data")
	public static StatisticsBackend STATISTICS_BACKEND = StatisticsBackend.CSV;

	/** Constant <code>TIMELINE_INTERVAL=60000</code> */
	@Parameter(key = "timeline_interval", group = "Output", description = "Time interval in milliseconds for timeline statistics")
	public static long TIMELINE_INTERVAL = 60 * 1000;

    /** Constant <code>TIMELINE_INTERPOLATION=true</code> */
    @Parameter(key = "timeline_interpolation", group = "Output", description = "Interpolate timeline values")
    public static boolean TIMELINE_INTERPOLATION = true;

    public enum OutputGranularity {
		MERGED, TESTCASE
	}

	/** Constant <code>OUTPUT_GRANULARITY</code> */
	@Parameter(key = "output_granularity", group = "Output", description = "Write all test cases for a class into a single file or to separate files.")
	public static OutputGranularity OUTPUT_GRANULARITY = OutputGranularity.MERGED;

	/** Constant <code>MAX_COVERAGE_DEPTH=-1</code> */
	@Parameter(key = "max_coverage_depth", group = "Output", description = "Maximum depth in the calltree to count a branch as covered")
	public static int MAX_COVERAGE_DEPTH = -1;

	// ---------------------------------------------------------------
	// Sandbox
	/** Constant <code>SANDBOX=false</code> */
	@Parameter(key = "sandbox", group = "Sandbox", description = "Execute tests in a sandbox environment")
	public static boolean SANDBOX = true;

	/** Constant <code>SANDBOX_MODE=Sandbox.SandboxMode.RECOMMENDED</code> */
	@Parameter(key = "sandbox_mode", group = "Sandbox", description = "Mode in which the sandbox is applied")
	public static Sandbox.SandboxMode SANDBOX_MODE = Sandbox.SandboxMode.RECOMMENDED;

	@Parameter(key = "filter_sandbox_tests", group = "Sandbox", description = "Drop tests that require the sandbox")
	public static boolean FILTER_SANDBOX_TESTS = false;

	/** Constant <code>VIRTUAL_FS=false</code> */
    @Parameter(key = "virtual_fs", group = "Sandbox", description = "Usa a virtual file system for all File I/O operations")
    public static boolean VIRTUAL_FS = true;


    @Parameter(key = "virtual_net", group = "Sandbox", description = "Usa a virtual network for all TCP/UDP communications")
    public static boolean VIRTUAL_NET = true;

    @Parameter(key = "use_separate_classloader", group = "Sandbox", description = "Usa a separate classloader in the final test cases")
    public static boolean USE_SEPARATE_CLASSLOADER = false;


    // ---------------------------------------------------------------
	// Experimental

	@Parameter(key = "cluster_recursion", description = "The maximum level of recursion when calculating the dependencies in the test cluster")
	public static int CLUSTER_RECURSION = 10;

	/** Constant <code>INHERITANCE_FILE=""</code> */
	@Parameter(key = "inheritance_file", description = "Cached version of inheritance tree")
	public static String INHERITANCE_FILE = "";

	/** Constant <code>BRANCH_EVAL=false</code> */
	@Parameter(key = "branch_eval", description = "Jeremy's branch evaluation")
	public static boolean BRANCH_EVAL = false;

	/** Constant <code>BRANCH_STATEMENT=false</code> */
	@Parameter(key = "branch_statement", description = "Require statement coverage for branch coverage")
	public static boolean BRANCH_STATEMENT = false;

	/** Constant <code>REMOTE_TESTING=false</code> */
	@Parameter(key = "remote_testing", description = "Include remote calls")
	public static boolean REMOTE_TESTING = false;

	/** Constant <code>CPU_TIMEOUT=false</code> */
	@Parameter(key = "cpu_timeout", description = "Measure timeouts on CPU time, not global time")
	public static boolean CPU_TIMEOUT = false;

	/** Constant <code>LOG_TIMEOUT=false</code> */
	@Parameter(key = "log_timeout", description = "Produce output each time a test times out")
	public static boolean LOG_TIMEOUT = false;

	/** Constant <code>CALL_PROBABILITY=0.0</code> */
	@Parameter(key = "call_probability", description = "Probability to reuse an existing test case, if it produces a required object")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double CALL_PROBABILITY = 0.0;

	/** Constant <code>USAGE_MODELS=""</code> */
	@Parameter(key = "usage_models", description = "Names of usage model files")
	public static String USAGE_MODELS = "";

	/** Constant <code>USAGE_RATE=0.5</code> */
	@Parameter(key = "usage_rate", description = "Probability with which to use transitions out of the OUM")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double USAGE_RATE = 0.5;

	/** Constant <code>INSTRUMENTATION_SKIP_DEBUG=false</code> */
	@Parameter(key = "instrumentation_skip_debug", description = "Skip debug information in bytecode instrumentation (needed for compatibility with classes transformed by Emma code instrumentation due to an ASM bug)")
	public static boolean INSTRUMENTATION_SKIP_DEBUG = false;

	/** Constant <code>INSTRUMENT_PARENT=false</code> */
	@Parameter(key = "instrument_parent", description = "Also count coverage goals in superclasses")
	public static boolean INSTRUMENT_PARENT = false;

	@Parameter(key = "instrument_context", description = "Also instrument methods called from the SUT")
	public static boolean INSTRUMENT_CONTEXT = false;

	@Parameter(key = "instrument_method_calls", description = "Instrument methods calls")
	public static boolean INSTRUMENT_METHOD_CALLS = false;
	
	@Parameter(key = "instrument_libraries", description = "Instrument the libraries used by the project under test")
	public static boolean INSTRUMENT_LIBRARIES = false;

	/** Constant <code>BREAK_ON_EXCEPTION=true</code> */
	@Parameter(key = "break_on_exception", description = "Stop test execution if exception occurrs")
	public static boolean BREAK_ON_EXCEPTION = true;

	/** Constant <code>HANDLE_STATIC_FIELDS=false</code> */
	@Parameter(key = "handle_static_fields", description = "Include methods that update required static fields")
	public static boolean HANDLE_STATIC_FIELDS = true;

	public enum TestFactory {
		RANDOM, ALLMETHODS, TOURNAMENT, JUNIT, ARCHIVE, SERIALIZATION,
		SEED_BEST_INDIVIDUAL, SEED_RANDOM_INDIVIDUAL,
		SEED_BEST_AND_RANDOM_INDIVIDUAL, SEED_BEST_INDIVIDUAL_METHOD,
		SEED_RANDOM_INDIVIDUAL_METHOD, SEED_MUTATED_BEST_INDIVIDUAL
	}

	@Parameter(key = "test_archive", description = "Use an archive of covered goals during test generation")
	public static boolean TEST_ARCHIVE = true;

	/** Constant <code>TEST_FACTORY</code> */
	@Parameter(key = "test_factory", description = "Which factory creates tests")
	public static TestFactory TEST_FACTORY = TestFactory.ARCHIVE;

	@Parameter(key = "seed_file", description = "File storing TestGenerationResult or GeneticAlgorithm")
	public static String SEED_FILE = "";

	@Parameter(key = "seed_probability", description = "Probability to seed on methods with randomness involved")
	public static double SEED_PROBABILITY = 0.1;

	@Parameter(key = "selected_junit", description = "List of fully qualified class names (separated by ':') indicating which JUnit test suites the user has selected (e.g., for seeding)")
	public static String SELECTED_JUNIT = null;

	/** Constant <code>JUNIT_STRICT=false</code> */
	@Parameter(key = "junit_strict", description = "Only include test files containing the target classname")
	public static boolean JUNIT_STRICT = false;

	/** Constant <code>SEED_CLONE=0.2</code> */
	@Parameter(key = "seed_clone", description = "Probability with which existing individuals are cloned")
    @DoubleValue(min = 0.0, max = 1.0)
	public static double SEED_CLONE = 0.2;

	/** Constant <code>SEED_MUTATIONS=2</code> */
	@Parameter(key = "seed_mutations", description = "Number of mutations applied to a cloned individual")
	public static int SEED_MUTATIONS = 2;

	/** Constant <code>SEED_DIR=""</code> */
	@Parameter(key = "seed_dir", group = "Output", description = "Directory name to save best chromosomes")
	public static String SEED_DIR = "evosuite-seeds";

	/** Constant <code>CONCOLIC_MUTATION=0.0</code> */
	@Parameter(key = "concolic_mutation", description = "Probability of using concolic mutation operator")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double CONCOLIC_MUTATION = 0.0;

	@Parameter(key = "constraint_solution_attempts", description = "Number of attempts to solve constraints related to one code branch")
	public static int CONSTRAINT_SOLUTION_ATTEMPTS = 3;

	/** Constant <code>TESTABILITY_TRANSFORMATION=false</code> */
	@Parameter(key = "testability_transformation", description = "Apply testability transformation (Yanchuan)")
	public static boolean TESTABILITY_TRANSFORMATION = false;

	/** Constant <code>TT_stack=10</code> */
	@Parameter(key = "TT_stack", description = "Maximum stack depth for testability transformation")
	public static int TT_stack = 10;

	/** Constant <code>TT=false</code> */
	@Parameter(key = "TT", description = "Testability transformation")
	public static boolean TT = false;

	public enum TransformationScope {
		TARGET, PREFIX, ALL
	}

	/** Constant <code>TT_SCOPE</code> */
	@Parameter(key = "tt_scope", description = "Testability transformation")
	public static TransformationScope TT_SCOPE = TransformationScope.ALL;

	// ---------------------------------------------------------------
	// Contracts / Asserts:
	/** Constant <code>CHECK_CONTRACTS=false</code> */
	@Parameter(key = "check_contracts", description = "Check contracts during test execution")
	public static boolean CHECK_CONTRACTS = false;

	/** Constant <code>CHECK_CONTRACTS_END=false</code> */
	@Parameter(key = "check_contracts_end", description = "Check contracts only once per test")
	public static boolean CHECK_CONTRACTS_END = false;

	@Parameter(key = "junit_theories", description = "Check JUnit theories as contracts")
	public static String JUNIT_THEORIES = "";

	/** Constant <code>ERROR_BRANCHES=false</code> */
	@Parameter(key = "error_branches", description = "Instrument code with error checking branches")
	public static boolean ERROR_BRANCHES = false;

	/** Constant <code>ENABLE_ASSERTS_FOR_EVOSUITE=false</code> */
	@Parameter(key = "enable_asserts_for_evosuite", description = "When running EvoSuite clients, for debugging purposes check its assserts")
	public static boolean ENABLE_ASSERTS_FOR_EVOSUITE = false;

	/** Constant <code>ENABLE_ASSERTS_FOR_SUT=true</code> */
	@Parameter(key = "enable_asserts_for_sut", description = "Check asserts in the SUT")
	public static boolean ENABLE_ASSERTS_FOR_SUT = true;

	// ---------------------------------------------------------------
	// Test Execution
	@Parameter(key = "timeout", group = "Test Execution", description = "Milliseconds allowed to execute the body of a test")
	public static int TIMEOUT = 3000;

	@Parameter(key = "timeout_reset", group = "Test Execution", description = "Milliseconds allowed to execute the static reset of a test")
	public static int TIMEOUT_RESET = 2000;


	@Parameter(key = "concolic_timeout", group = "Test Execution", description = "Milliseconds allowed per test during concolic execution")
	public static int CONCOLIC_TIMEOUT = 15000;

	/** Constant <code>SHUTDOWN_TIMEOUT=1000</code> */
	@Parameter(key = "shutdown_timeout", group = "Test Execution", description = "Milliseconds grace time to shut down test cleanly")
	public static int SHUTDOWN_TIMEOUT = 1000;

	/** Constant <code>MUTATION_TIMEOUTS=3</code> */
	@Parameter(key = "mutation_timeouts", group = "Test Execution", description = "Number of timeouts before we consider a mutant killed")
	public static int MUTATION_TIMEOUTS = 3;

	/** Constant <code>ARRAY_LIMIT=1000000</code> */
	@Parameter(key = "array_limit", group = "Test Execution", description = "Hard limit on array allocation in the code")
	public static int ARRAY_LIMIT = 1000000;

	/** Constant <code>MAX_MUTANTS=100</code> */
	@Parameter(key = "max_mutants", group = "Test Execution", description = "Maximum number of mutants to target at the same time")
	public static int MAX_MUTANTS = 100;

	/** Constant <code>MUTATION_GENERATIONS=10</code> */
	@Parameter(key = "mutation_generations", group = "Test Execution", description = "Number of generations before changing the currently targeted mutants")
	public static int MUTATION_GENERATIONS = 10;

	/** Constant <code>REPLACE_CALLS=false</code> */
	@Parameter(key = "replace_calls", group = "Test Execution", description = "Replace nondeterministic calls and System.exit")
	public static boolean REPLACE_CALLS = true;

	@Parameter(key = "replace_system_in", group = "Test Execution", description = "Replace System.in with a smart stub/mock")
	public static boolean REPLACE_SYSTEM_IN = true;

    @Parameter(key = "max_started_threads", group = "Test Execution", description = "Max number of threads allowed to be started in each test")
    public static int MAX_STARTED_THREADS = RuntimeSettings.maxNumberOfThreads;

    @Parameter(key = "max_loop_iterations", group = "Test Execution", description = "Max number of iterations allowed per loop. A negative value means no check is done.")
    public static long MAX_LOOP_ITERATIONS = RuntimeSettings.maxNumberOfIterationsPerLoop;

    // ---------------------------------------------------------------
	// Debugging

	/** Constant <code>DEBUG=false</code> */
	@Parameter(key = "debug", group = "Debugging", description = "Enables debugging support in the client VM")
	public static boolean DEBUG = false;

	/** Constant <code>PORT=1044</code> */
	@Parameter(key = "port", group = "Debugging", description = "Port on localhost, to which the client VM will listen for a remote debugger; defaults to 1044")
	@IntValue(min = 1024, max = 65535)
	public static int PORT = 1044;

	@Parameter(key = "jmc", group = "Debugging", description = "Experimental: activate Flight Recorder in spawn client process for Java Mission Controll")
	public static boolean JMC = false;


	// ---------------------------------------------------------------
	// TODO: Fix description
	public enum AlternativeFitnessCalculationMode {
		SUM, MIN, MAX, AVG, SINGLE
	}

	/** Constant <code>ALTERNATIVE_FITNESS_CALCULATION_MODE</code> */
	@Parameter(key = "alternative_fitness_calculation_mode", description = "")
	public static AlternativeFitnessCalculationMode ALTERNATIVE_FITNESS_CALCULATION_MODE = AlternativeFitnessCalculationMode.SUM;

	/** Constant <code>STARVE_BY_FITNESS=true</code> */
	@Parameter(key = "starve_by_fitness", description = "")
	public static boolean STARVE_BY_FITNESS = true;

	/** Constant <code>ENABLE_ALTERNATIVE_FITNESS_CALCULATION=false</code> */
	@Parameter(key = "enable_alternative_fitness_calculation", description = "")
	public static boolean ENABLE_ALTERNATIVE_FITNESS_CALCULATION = false;

	/** Constant <code>ENABLE_ALTERNATIVE_FITNESS_CALCULATION=false</code> */
	@Parameter(key = "enable_alternative_suite_fitness", description = "")
	public static boolean ENABLE_ALTERNATIVE_SUITE_FITNESS = false;

	/** Constant <code>DEFUSE_DEBUG_MODE=false</code> */
	@Parameter(key = "defuse_debug_mode", description = "")
	public static boolean DEFUSE_DEBUG_MODE = false;

	@Parameter(key = "defuse_aliases", description = "")
	public static boolean DEFUSE_ALIASES = true;

	/** Constant <code>RANDOMIZE_DIFFICULTY=true</code> */
	@Parameter(key = "randomize_difficulty", description = "")
	public static boolean RANDOMIZE_DIFFICULTY = true;

	// ---------------------------------------------------------------
	// UI Test generation parameters
	/** Constant <code>UI_BACKGROUND_COVERAGE_DELAY=-1</code> */
	@Parameter(key = "UI_BACKGROUND_COVERAGE_DELAY", group = "EXSYST", description = "How often to write out coverage information in the background (in ms). -1 to disable.")
	public static int UI_BACKGROUND_COVERAGE_DELAY = -1;

	// ---------------------------------------------------------------
	// Runtime parameters

	public enum Criterion {
		EXCEPTION, DEFUSE, ALLDEFS, BRANCH, CBRANCH, STRONGMUTATION, WEAKMUTATION,
        MUTATION, STATEMENT, RHO, AMBIGUITY, IBRANCH, READABILITY,
        ONLYBRANCH, ONLYMUTATION, METHODTRACE, METHOD, METHODNOEXCEPTION, LINE, ONLYLINE, OUTPUT,
        REGRESSION,	REGRESSIONTESTS
	}

    /** Constant <code>CRITERION</code> */
    @Parameter(key = "criterion", group = "Runtime", description = "Coverage criterion. Can define more than one criterion by using a ':' separated list")
    public static Criterion[] CRITERION = new Criterion[] {
            //these are basic criteria that should be always on by default
            Criterion.LINE, Criterion.BRANCH, Criterion.EXCEPTION, Criterion.WEAKMUTATION, Criterion.OUTPUT, Criterion.METHOD, Criterion.METHODNOEXCEPTION, Criterion.CBRANCH  };


    /** Cache target class */
	private static Class<?> TARGET_CLASS_INSTANCE = null;
	
	/** Cache target regression class */
	private static Class<?> TARGET_REGERSSION_CLASS_INSTANCE = null;

	/** Constant <code>CP=""</code> */
	@Parameter(key = "CP", group = "Runtime", description = "The classpath of the target classes")
	public static String CP = "";

	/** Constant <code>PROJECT_PREFIX="null"</code> */
	@Parameter(key = "PROJECT_PREFIX", group = "Runtime", description = "Package name of target package")
	public static String PROJECT_PREFIX = "";

	/** Constant <code>PROJECT_DIR="null"</code> */
	@Parameter(key = "PROJECT_DIR", group = "Runtime", description = "Directory name of target package")
	public static String PROJECT_DIR = null;

	/** Package name of target class (might be a subpackage) */
	public static String CLASS_PREFIX = "";

	/** Sub-package name of target class */
	public static String SUB_PREFIX = "";

	/** Constant <code>TARGET_CLASS_PREFIX=""</code> */
	@Parameter(key = "TARGET_CLASS_PREFIX", group = "Runtime", description = "Prefix of classes we are trying to cover")
	public static String TARGET_CLASS_PREFIX = "";

	/** Class under test */
	@Parameter(key = "TARGET_CLASS", group = "Runtime", description = "Class under test")
	public static String TARGET_CLASS = "";

	/** Method under test */
	@Parameter(key = "target_method", group = "Runtime", description = "Method for which to generate tests")
	public static String TARGET_METHOD = "";

	/** Method under test */
	@Parameter(key = "target_method_prefix", group = "Runtime", description = "All methods matching prefix will be used for generating tests")
	public static String TARGET_METHOD_PREFIX = "";

	/** Method under test */
	@Parameter(key = "target_method_list", group = "Runtime", description = "A colon(:) separated list of methods for which to generate tests")
	public static String TARGET_METHOD_LIST = "";

	/** Constant <code>HIERARCHY_DATA="hierarchy.xml"</code> */
	@Parameter(key = "hierarchy_data", group = "Runtime", description = "File in which hierarchy data is stored")
	public static String HIERARCHY_DATA = "hierarchy.xml";

	/** Constant <code>CONNECTION_DATA="connection.xml"</code> */
	@Parameter(key = "connection_data", group = "Runtime", description = "File in which connection data is stored")
	public static String CONNECTION_DATA = "connection.xml";

	/** Constant <code>CONNECTION_DATA="connection.xml"</code> */
	@Parameter(key = "exclude_ibranches_cut", group = "Runtime", description = "Exclude ibranches in the cut, to speed up ibranch as secondary criterion")
	public static boolean EXCLUDE_IBRANCHES_CUT = false;


	public enum Strategy {
		ONEBRANCH, EVOSUITE, RANDOM, RANDOM_FIXED, REGRESSION, REGRESSIONTESTS
	}
	
	/** Constant <code>REGRESSIONCP</code> */
	@Parameter(key = "regressioncp", group = "Runtime", description = "Regression testing classpath")
	public static String REGRESSIONCP = ".";
	
	/** Constant <code>REGRESSION_ANALYSIS_COMBINATIONS</code> */
	@Parameter(key = "regression_analysis_combinations", group = "Runtime", description = "What regression fitness combination stragetegy is used")
	public static int REGRESSION_ANALYSIS_COMBINATIONS = 0;
	
	/** Constant <code>REGRESSION_ANALYSIS_BRANCHDISTANCE</code> */
	@Parameter(key = "regression_analysis_branchdistance", group = "Runtime", description = "What regression branch distance fitness strategy is used")
	public static int REGRESSION_ANALYSIS_BRANCHDISTANCE = 0;
	
	/** Constant <code>REGRESSION_ANALYSIS_OBJECTDISTANCE</code> */
	@Parameter(key = "regression_analysis_objectdistance", group = "Runtime", description = "What regression object distance fitness strategy will be used")
	public static int REGRESSION_ANALYSIS_OBJECTDISTANCE = 0;
	
	/** Constant <code>REGRESSION_DIFFERENT_BRANCHES</code> */
	@Parameter(key = "regression_different_branches", group = "Runtime", description = "Classes under test have different branch orders")
	public static boolean REGRESSION_DIFFERENT_BRANCHES = false;
	
	/** Constant <code>REGRESSION_USE_FITNESS</code> */
	@Parameter(key = "regression_use_fitness", group = "Runtime", description = "Which fitness values will be used")
	public static int REGRESSION_USE_FITNESS = 4;
	
	/** Constant <code>REGRESSION_ANALYZE</code> */
	@Parameter(key = "regression_analyze", group = "Runtime", description = "Analyze the classes under test, to ensure the effectiveness of evosuite")
	public static boolean REGRESSION_ANALYZE = false;
	
	/** Constant <code>REGRESSION_RANDOM_STRATEGY</code> */
	@Parameter(key = "regression_random_strategy", group = "Runtime", description = "What strategy to take after the first fault is found")
	public static int REGRESSION_RANDOM_STRATEGY = 3;

	/** Constant <code>REGRESSION_DISABLE_SPECIAL_ASSERTIONS</code> */
	@Parameter(key = "regression_disable_special_assertions", group = "Runtime", description = "disable undesirable assertions")
	public static boolean REGRESSION_DISABLE_SPECIAL_ASSERTIONS = false;

	/** Constant <code>STRATEGY</code> */
	@Parameter(key = "strategy", group = "Runtime", description = "Which mode to use")
	public static Strategy STRATEGY = Strategy.EVOSUITE;

	/** Constant <code>PROCESS_COMMUNICATION_PORT=-1</code> */
	@Parameter(key = "process_communication_port", group = "Runtime", description = "Port at which the communication with the external process is done")
	public static int PROCESS_COMMUNICATION_PORT = -1;

	/** Constant <code>STOPPING_PORT=-1</code> */
	@Parameter(key = "stopping_port", group = "Runtime", description = "Port at which a stopping condition waits for interruption")
	public static int STOPPING_PORT = -1;

	/** Constant <code>MAX_STALLED_THREADS=10</code> */
	@Parameter(key = "max_stalled_threads", group = "Runtime", description = "Number of stalled threads")
	public static int MAX_STALLED_THREADS = 10;

	@Parameter(key = "ignore_threads", group = "Runtime", description = "Do not attempt to kill threads matching this prefix")
	public static String[] IGNORE_THREADS = new String[] {};

	/** Constant <code>MIN_FREE_MEM=50 * 1000 * 1000</code> */
	@Parameter(key = "min_free_mem", group = "Runtime", description = "Minimum amount of available memory")
	public static int MIN_FREE_MEM = 50 * 1000 * 1000;


	@Parameter(key = "max_perm_size", group = "Runtime", description = "MaxPermSize (in MB) for the client process")
	public static int MAX_PERM_SIZE = 256;

	/** Constant <code>CLIENT_ON_THREAD=false</code> */
	@Parameter(key = "client_on_thread", group = "Runtime", description = "Run client process on same JVM of master in separate thread. To be used only for debugging purposes")
	public static volatile boolean CLIENT_ON_THREAD = false;

	// ---------------------------------------------------------------
	// Seeding test cases

	/** Constant <code>CLASSPATH="new String[] {  }"</code> */
	@Parameter(key = "classpath", group = "Test Seeding", description = "The classpath needed to compile the seeding test case.")
	public static String[] CLASSPATH = new String[] { "" };

	/** Constant <code>SOURCEPATH="new String[] {  }"</code> */
	@Parameter(key = "sourcepath", group = "Test Seeding", description = "The path to the test case source.")
	public static String[] SOURCEPATH = new String[] { "" };

	// ---------------------------------------------------------------
	// Eclipse Plug-in flag

	/** Constant <code>ECLIPSE_PLUGIN=false</code> */
	@Parameter(key = "eclipse_plugin", group = "Plugin", description = "Running plugin")
	public static boolean ECLIPSE_PLUGIN = false;

	/**
	 * Get all parameters that are available
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<String> getParameters() {
		return parameterMap.keySet();
	}

	/**
	 * Determine fields that are declared as parameters
	 */
	private static void reflectMap() {
		for (Field f : Properties.class.getFields()) {
			if (f.isAnnotationPresent(Parameter.class)) {
				Parameter p = f.getAnnotation(Parameter.class);
				parameterMap.put(p.key(), f);
				try {
					defaultMap.put(f, f.get(null));
				} catch (Exception e) {
					logger.error("Exception: " + e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Initialize properties from property file or command line parameters
	 */
	private void initializeProperties() throws IllegalStateException{
		for (String parameter : parameterMap.keySet()) {
			try {
				String property = System.getProperty(parameter);
				if (property == null) {
					property = properties.getProperty(parameter);
				}
				if (property != null) {
					setValue(parameter, property);
				}
			} catch (Exception e) {
                throw new IllegalStateException("Wrong parameter settings for '" + parameter + "': " + e.getMessage());
            }
		}
		if (POPULATION_LIMIT == PopulationLimit.STATEMENTS) {
			if (MAX_LENGTH < POPULATION) {
				MAX_LENGTH = POPULATION;
			}
		}
	}

	/**
	 * Load and initialize a properties file from the default path
	 */
	public void loadProperties(boolean silent) {
		loadPropertiesFile(System.getProperty(PROPERTIES_FILE,
				"evosuite-files/evosuite.properties"), silent);
		initializeProperties();
	}

	/**
	 * Load and initialize a properties file from a given path
	 *
	 * @param propertiesPath
	 *            a {@link java.lang.String} object.
	 */
	public void loadProperties(String propertiesPath, boolean silent) {
		loadPropertiesFile(propertiesPath, silent);
		initializeProperties();
	}

	/**
	 * Load a properties file
	 *
	 * @param propertiesPath
	 *            a {@link java.lang.String} object.
	 */
	public void loadPropertiesFile(String propertiesPath, boolean silent) {
		properties = new java.util.Properties();
		try {
			InputStream in = null;
			File propertiesFile = new File(propertiesPath);
			if (propertiesFile.exists()) {
				in = new FileInputStream(propertiesPath);
				properties.load(in);

				if (!silent)
					LoggingUtils.getEvoLogger().info(
							"* Properties loaded from "
									+ propertiesFile.getAbsolutePath());
			} else {
				propertiesPath = "evosuite.properties";
				in = this.getClass().getClassLoader()
						.getResourceAsStream(propertiesPath);
				if (in != null) {
					properties.load(in);
					if (!silent)
						LoggingUtils.getEvoLogger().info(
								"* Properties loaded from "
										+ this.getClass().getClassLoader()
												.getResource(propertiesPath)
												.getPath());
				}
				// logger.info("* Properties loaded from default configuration file.");
			}
		} catch (FileNotFoundException e) {
			logger.warn("- Error: Could not find configuration file "
					+ propertiesPath);
		} catch (IOException e) {
			logger.warn("- Error: Could not find configuration file "
					+ propertiesPath);
		} catch (Exception e) {
			logger.warn("- Error: Could not find configuration file "
					+ propertiesPath);
		}
	}

	/** All fields representing values, inserted via reflection */
	private static Map<String, Field> parameterMap = new HashMap<String, Field>();

	/** All fields representing values, inserted via reflection */
	private static Map<Field, Object> defaultMap = new HashMap<Field, Object>();

	static {
		// need to do it once, to capture all the default values
		reflectMap();
	}

	/**
	 * Keep track of which fields have been changed from their defaults during
	 * loading
	 */
	private static Set<String> changedFields = new HashSet<String>();

	/**
	 * Get class of parameter
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @return a {@link java.lang.Class} object.
	 */
	public static Class<?> getType(String key) throws NoSuchParameterException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);
		return f.getType();
	}

	/**
	 * Get description string of parameter
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getDescription(String key)
			throws NoSuchParameterException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);
		Parameter p = f.getAnnotation(Parameter.class);
		return p.description();
	}

	/**
	 * Get group name of parameter
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getGroup(String key) throws NoSuchParameterException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);
		Parameter p = f.getAnnotation(Parameter.class);
		return p.group();
	}

	/**
	 * Get integer boundaries
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @return a {@link org.evosuite.Properties.IntValue} object.
	 */
	public static IntValue getIntLimits(String key)
			throws NoSuchParameterException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);
		return f.getAnnotation(IntValue.class);
	}

	/**
	 * Get long boundaries
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @return a {@link org.evosuite.Properties.LongValue} object.
	 */
	public static LongValue getLongLimits(String key)
			throws NoSuchParameterException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);
		return f.getAnnotation(LongValue.class);
	}

	/**
	 * Get double boundaries
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @return a {@link org.evosuite.Properties.DoubleValue} object.
	 */
	public static DoubleValue getDoubleLimits(String key)
			throws NoSuchParameterException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);
		return f.getAnnotation(DoubleValue.class);
	}

	/**
	 * Get an integer parameter value
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @return a int.
	 */
	public static int getIntegerValue(String key)
			throws NoSuchParameterException, IllegalArgumentException,
			IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		return parameterMap.get(key).getInt(null);
	}

	/**
	 * Get an integer parameter value
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @return a long.
	 */
	public static long getLongValue(String key)
			throws NoSuchParameterException, IllegalArgumentException,
			IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		return parameterMap.get(key).getLong(null);
	}

	/**
	 * Get a boolean parameter value
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @return a boolean.
	 */
	public static boolean getBooleanValue(String key)
			throws NoSuchParameterException, IllegalArgumentException,
			IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		return parameterMap.get(key).getBoolean(null);
	}

	/**
	 * Get a double parameter value
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @return a double.
	 */
	public static double getDoubleValue(String key)
			throws NoSuchParameterException, IllegalArgumentException,
			IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		return parameterMap.get(key).getDouble(null);
	}

	/**
	 * Get parameter value as string (works for all types)
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getStringValue(String key)
			throws NoSuchParameterException, IllegalArgumentException,
			IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		StringBuffer sb = new StringBuffer();
		Object val = parameterMap.get(key).get(null);
		if (val != null && val.getClass().isArray()) {
			int len = Array.getLength(val);
			for (int i = 0; i < len; i++) {
				if (i > 0)
					sb.append(";");

				sb.append(Array.get(val, i));
			}
		} else {
			sb.append(val);
		}
		return sb.toString();
	}

	/**
	 * Check if there exist any parameter with given name
	 *
	 * @param parameterName
	 * @return
	 */
	public static boolean hasParameter(String parameterName) {
		return parameterMap.containsKey(parameterName);
	}

	/**
	 * Set parameter to new integer value
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @param value
	 *            a int.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 */
	public void setValue(String key, int value)
			throws NoSuchParameterException, IllegalArgumentException,
			IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);

		if (f.isAnnotationPresent(IntValue.class)) {
			IntValue i = f.getAnnotation(IntValue.class);
			if (value < i.min() || value > i.max())
				throw new IllegalArgumentException();
		}

		f.setInt(this, value);
	}

	/**
	 * Set parameter to new long value
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @param value
	 *            a long.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 */
	public void setValue(String key, long value)
			throws NoSuchParameterException, IllegalArgumentException,
			IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);

		if (f.isAnnotationPresent(LongValue.class)) {
			LongValue i = f.getAnnotation(LongValue.class);
			if (value < i.min() || value > i.max())
				throw new IllegalArgumentException();
		}

		f.setLong(this, value);
	}

	/**
	 * Set parameter to new boolean value
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @param value
	 *            a boolean.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 */
	public void setValue(String key, boolean value)
			throws NoSuchParameterException, IllegalArgumentException,
			IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);
		f.setBoolean(this, value);
	}

	/**
	 * Set parameter to new double value
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @param value
	 *            a double.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 */
	public void setValue(String key, double value)
			throws NoSuchParameterException, IllegalArgumentException,
			IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);
		if (f.isAnnotationPresent(DoubleValue.class)) {
			DoubleValue i = f.getAnnotation(DoubleValue.class);
			if (value < i.min() || value > i.max())
				throw new IllegalArgumentException();
		}
		f.setDouble(this, value);
	}

	/**
	 * Set parameter to new value from String
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @param value
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setValue(String key, String value)
			throws NoSuchParameterException, IllegalArgumentException,
			IllegalAccessException {
		if (!parameterMap.containsKey(key)) {
			throw new NoSuchParameterException(key);
		}

		Field f = parameterMap.get(key);
		changedFields.add(key);

		//Enum
		if (f.getType().isEnum()) {
			f.set(null, Enum.valueOf((Class<Enum>) f.getType(),
					value.toUpperCase()));
		}
		//Integers
		else if (f.getType().equals(int.class)) {
			setValue(key, Integer.parseInt(value));
		} else if (f.getType().equals(Integer.class)) {
			setValue(key, (Integer) Integer.parseInt(value));
		}
		//Long
		else if (f.getType().equals(long.class)) {
			setValue(key, Long.parseLong(value));
		} else if (f.getType().equals(Long.class)) {
			setValue(key, (Long) Long.parseLong(value));
		}
		//Boolean
		else if (f.getType().equals(boolean.class)) {
			setValue(key, strictParseBoolean(value));
		} else if (f.getType().equals(Boolean.class)) {
			setValue(key, (Boolean) strictParseBoolean(value));
		}
		//Double
		else if (f.getType().equals(double.class)) {
			setValue(key, Double.parseDouble(value));
		} else if (f.getType().equals(Double.class)) {
			setValue(key, (Double) Double.parseDouble(value));
		}
		//Array
		else if (f.getType().isArray()) {
			if (f.getType().isAssignableFrom(String[].class)) {
				setValue(key, value.split(":"));
			} else if (f.getType().getComponentType().equals(Criterion.class)) {
				String[] values = value.split(":");
				Criterion[] criteria = new Criterion[values.length];

				int pos = 0;
				for (String stringValue : values) {
					criteria[pos++] = Enum.valueOf(Criterion.class,
							stringValue.toUpperCase());
				}

				f.set(this, criteria);
			}
		} else {
			f.set(null, value);
		}
	}

	/**
	 * we need this strict function because Boolean.parseBoolean silently
	 * ignores malformed strings
	 *
	 * @param s
	 * @return
	 */
	protected boolean strictParseBoolean(String s) {
		if (s == null || s.isEmpty()) {
			throw new IllegalArgumentException(
					"empty string does not represent a valid boolean");
		}

		if (s.equalsIgnoreCase("true")) {
			return true;
		}

		if (s.equalsIgnoreCase("false")) {
			return false;
		}

		throw new IllegalArgumentException(
				"Invalid string representing a boolean: " + s);
	}

	/**
	 * <p>
	 * setValue
	 * </p>
	 *
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @param value
	 *            an array of {@link java.lang.String} objects.
	 * @throws org.evosuite.Properties#NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 */
	public void setValue(String key, String[] value)
			throws NoSuchParameterException, IllegalArgumentException,
			IllegalAccessException {
		if (!parameterMap.containsKey(key)) {
			throw new NoSuchParameterException(key);
		}

		Field f = parameterMap.get(key);

		f.set(this, value);
	}

	/**
	 * Set the given <code>key</code> variable to the given input Object
	 * <code>value</code>
	 *
	 * @param key
	 * @param value
	 * @throws NoSuchParameterException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void setValue(String key, Object value)
			throws NoSuchParameterException, IllegalArgumentException,
			IllegalAccessException {
		if (!parameterMap.containsKey(key)) {
			throw new NoSuchParameterException(key);
		}

		Field f = parameterMap.get(key);

		f.set(this, value);
	}

	/** Singleton instance */
	private static Properties instance = null; // new Properties(true, true);

	/** Internal properties hashmap */
	private java.util.Properties properties;

	/** Constructor */
	private Properties(boolean loadProperties, boolean silent) {
		if (loadProperties)
			loadProperties(silent);
		setClassPrefix();
	}

	/**
	 * Singleton accessor
	 *
	 * @return a {@link org.evosuite.Properties} object.
	 */
	public static Properties getInstance() {
		if (instance == null)
			instance = new Properties(true, false);
		return instance;
	}

	/**
	 * Singleton accessor
	 *
	 * @return a {@link org.evosuite.Properties} object.
	 */
	public static Properties getInstanceSilent() {
		if (instance == null)
			instance = new Properties(true, true);
		return instance;
	}

	/**
	 * This exception is used when a non-existent parameter is accessed
	 *
	 *
	 */
	public static class NoSuchParameterException extends Exception {

		private static final long serialVersionUID = 9074828392047742535L;

		public NoSuchParameterException(String key) {
			super("No such property defined: " + key);
		}
	}

	private static void setClassPrefix() {
		if (TARGET_CLASS != null && !TARGET_CLASS.equals("")) {
			if (TARGET_CLASS.contains(".")) {
				CLASS_PREFIX = TARGET_CLASS.substring(0,
						TARGET_CLASS.lastIndexOf('.'));
				SUB_PREFIX = CLASS_PREFIX.replace(PROJECT_PREFIX + ".", "");
			}
			if (PROJECT_PREFIX == null || PROJECT_PREFIX.equals("")) {
				if (CLASS_PREFIX.contains("."))
					PROJECT_PREFIX = CLASS_PREFIX.substring(0,
							CLASS_PREFIX.indexOf("."));
				else
					PROJECT_PREFIX = CLASS_PREFIX;
				// LoggingUtils.getEvoLogger().info("* Using project prefix: "
				// + PROJECT_PREFIX);
			}
		}
	}
	
	private static boolean toReturnRegression = false;
	
	/*
	 * Get target class
	 * 
	 * @param isOriginal whether or not you want the original or the regression class.
	 */
	public static Class<?> getTargetClassRegression(boolean isOriginal){
		if (isOriginal && TARGET_CLASS_INSTANCE != null
		        && TARGET_CLASS_INSTANCE.getCanonicalName().equals(TARGET_CLASS))
			return TARGET_CLASS_INSTANCE;
		else if(!isOriginal && TARGET_REGERSSION_CLASS_INSTANCE != null
		        && TARGET_REGERSSION_CLASS_INSTANCE.getCanonicalName().equals(TARGET_CLASS))
			return TARGET_REGERSSION_CLASS_INSTANCE;
		
		if(isOriginal)
		 toReturnRegression = true;
		
		 Class<?> targetClass = getTargetClass();
		 
		 toReturnRegression = false;
		 return targetClass;
	}

	public static Class<?> getTargetClass() {
		return getTargetClass(true);
	}
	
	public static boolean hasTargetClassBeenLoaded() {
		return TARGET_CLASS_INSTANCE != null;
	}

	/**
	 * Get class object of class under test
	 *
	 * @return a {@link java.lang.Class} object.
	 */
	public static Class<?> getTargetClass(boolean initialise) {

		if (TARGET_CLASS_INSTANCE != null
				&& TARGET_CLASS_INSTANCE.getCanonicalName()
						.equals(TARGET_CLASS))
			return TARGET_CLASS_INSTANCE;

		TARGET_CLASS_INSTANCE = null;

		try {
			/*
			 * TODO: loading the SUT will execute its static initializer.
			 * This might interact with the environment (eg, read a file, access static
			 * variables of other classes), and even fails if an exception is thrown.
			 * Those cases should be handled here before starting the search.
			 */

			Runtime.getInstance().resetRuntime(); //it is important to initialize the VFS

			TARGET_CLASS_INSTANCE = Class.forName(TARGET_CLASS, initialise,
					TestGenerationContext.getInstance().getClassLoaderForSUT());
			

			if (STRATEGY == Strategy.REGRESSION || STRATEGY == Strategy.REGRESSIONTESTS) {
				TARGET_REGERSSION_CLASS_INSTANCE = Class.forName(TARGET_CLASS, initialise,
                        TestGenerationContext.getInstance().getRegressionClassLoaderForSUT());
			}

			setClassPrefix();

		} catch (ClassNotFoundException e) {
			LoggingUtils.getEvoLogger().warn(
					"* Could not find class under test " + Properties.TARGET_CLASS + ": " + e);
		}

		return (Properties.toReturnRegression)?TARGET_REGERSSION_CLASS_INSTANCE: TARGET_CLASS_INSTANCE;
	}

	/**
	 * Get class object of class under test
	 *
	 * @return a {@link java.lang.Class} object.
	 */
	public static void resetTargetClass() {
		TARGET_CLASS_INSTANCE = null;
	}

	/**
	 * Update the evosuite.properties file with the current setting
	 */
	public void writeConfiguration() {
		URL fileURL = this.getClass().getClassLoader()
				.getResource("evosuite.properties");
		String name = fileURL.getFile();
		writeConfiguration(name);
	}

	/**
	 * Update the evosuite.properties file with the current setting
	 *
	 * @param fileName
	 *            a {@link java.lang.String} object.
	 */
	public void writeConfiguration(String fileName) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("CP=");
		// Replace backslashes with forwardslashes, as backslashes are dropped during reading
		// TODO: What if there are weird characters in the code? Need regex
		buffer.append(ClassPathHandler.getInstance()
				.getTargetProjectClasspath().replace("\\", "/"));
		buffer.append("\nPROJECT_PREFIX=");
		if (Properties.PROJECT_PREFIX != null)
			buffer.append(Properties.PROJECT_PREFIX);
		buffer.append("\n");

		Map<String, Set<Parameter>> fieldMap = new HashMap<String, Set<Parameter>>();
		for (Field f : Properties.class.getFields()) {
			if (f.isAnnotationPresent(Parameter.class)) {
				Parameter p = f.getAnnotation(Parameter.class);
				if (!fieldMap.containsKey(p.group()))
					fieldMap.put(p.group(), new HashSet<Parameter>());

				fieldMap.get(p.group()).add(p);
			}
		}

		for (String group : fieldMap.keySet()) {
			if (group.equals("Runtime"))
				continue;

			buffer.append("#--------------------------------------\n");
			buffer.append("# ");
			buffer.append(group);
			buffer.append("\n#--------------------------------------\n\n");
			for (Parameter p : fieldMap.get(group)) {
				buffer.append("# ");
				buffer.append(p.description());
				buffer.append("\n");
				if (!changedFields.contains(p.key()))
					buffer.append("#");
				buffer.append(p.key());
				buffer.append("=");
				try {
					buffer.append(getStringValue(p.key()));
				} catch (Exception e) {
					logger.error("Exception " + e.getMessage(), e);
				}
				buffer.append("\n\n");
			}
		}
		Utils.writeFile(buffer.toString(), fileName);
	}

	/**
	 * <p>
	 * resetToDefaults
	 * </p>
	 */
	public void resetToDefaults() {
		Properties.instance = new Properties(false, true);
		for (Field f : Properties.class.getFields()) {
			if (f.isAnnotationPresent(Parameter.class)) {
				if (defaultMap.containsKey(f)) {
					try {
						f.set(null, defaultMap.get(f));
					} catch (Exception e) {
						logger.error("Failed to init property field " + f
								+ " , " + e.getMessage(), e);
					}
				}
			}
		}
	}
	
	
	/*
	 * whether or not the regression mode is running
	 */
	public static boolean isRegression(){
		boolean isRegression = (STRATEGY == Strategy.REGRESSION || STRATEGY == Strategy.REGRESSIONTESTS);
		return isRegression;
	}

}
