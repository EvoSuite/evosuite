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
package org.evosuite;

import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.lm.MutationType;
import org.evosuite.runtime.LoopCounter;
import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.symbolic.dse.algorithm.DSEAlgorithms;
import org.evosuite.utils.FileIOUtils;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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

/**
 * Central property repository. All global parameters of EvoSuite should be
 * declared as fields here, using the appropriate annotation. Access is possible
 * directly via the fields, or with getter/setter methods.
 *
 * @author Gordon Fraser
 */
public class Properties {

    public static final String JAVA_VERSION_WARN_MSG = "EvoSuite does not support Java versions > 8 yet";

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
    @Parameter(key = "test_excludes", group = "Test Creation", description = "File containing methods that should not be used in testing")
    public static String TEST_EXCLUDES = "test.excludes";

    @Parameter(key = "test_includes", group = "Test Creation", description = "File containing methods that should be included in testing")
    public static String TEST_INCLUDES = "test.includes";

    @Parameter(key = "evosuite_use_uispec", group = "Test Creation", description = "If set to true EvoSuite test generation inits UISpec in order to avoid display of UI")
    public static boolean EVOSUITE_USE_UISPEC = false;

    @Deprecated
    @Parameter(key = "make_accessible", group = "TestCreation", description = "Change default package rights to public package rights")
    public static boolean MAKE_ACCESSIBLE = false;

    @Parameter(key = "string_replacement", group = "Test Creation", description = "Replace string.equals with levenshtein distance")
    public static boolean STRING_REPLACEMENT = true;

    @Parameter(key = "reset_static_fields", group = "Test Creation", description = "Call static constructors only after each static field was modified")
    public static boolean RESET_STATIC_FIELDS = true;

    @Parameter(key = "reset_static_final_fields", group = "Test Creation", description = "Remove the static modifier in target fields")
    public static boolean RESET_STATIC_FINAL_FIELDS = true;

    @Parameter(key = "reset_static_field_gets", group = "Test Creation", description = "Call static constructors also after each static field was read")
    public static boolean RESET_STATIC_FIELD_GETS = false;

    @Parameter(key = "reset_all_classes_during_test_generation", group = "Test Creation", description = "Test Generation does not apply the selective method of selection of class re-initalization")
    public static boolean RESET_ALL_CLASSES_DURING_TEST_GENERATION = false;

    @Parameter(key = "reset_all_classes_during_assertion_generation", group = "Test Creation", description = "Test Generation does not apply the selective method of selection of class re-initalization")
    public static boolean RESET_ALL_CLASSES_DURING_ASSERTION_GENERATION = true;


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

    @Parameter(key = "null_probability", group = "Test Creation", description = "Probability to use null instead of constructing an object")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double NULL_PROBABILITY = 0.1;

    @Parameter(key = "object_reuse_probability", group = "Test Creation", description = "Probability to reuse an existing reference, if available")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double OBJECT_REUSE_PROBABILITY = 0.9;

    @Parameter(key = "primitive_reuse_probability", group = "Test Creation", description = "Probability to reuse an existing primitive, if available")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double PRIMITIVE_REUSE_PROBABILITY = 0.5;

    @Parameter(key = "primitive_pool", group = "Test Creation", description = "Probability to use a primitive from the pool rather than a random value")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double PRIMITIVE_POOL = 0.5;

    @Parameter(key = "dynamic_pool", group = "Test Creation", description = "Probability to use a primitive from the dynamic pool rather than a random value")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double DYNAMIC_POOL = 0.5;

    @Parameter(key = "variable_pool", group = "Test Creation", description = "Set probability of a constant based on the number of occurrences")
    @DoubleValue(min = 0.0, max = 1.0)
    public static boolean VARIABLE_POOL = false;

    @Deprecated
    @Parameter(key = "dynamic_seeding", group = "Test Creation", description = "Use numeric dynamic seeding")
    public static boolean DYNAMIC_SEEDING = true;

    @Parameter(key = "dynamic_pool_size", group = "Test Creation", description = "Number of dynamic constants to keep")
    public static int DYNAMIC_POOL_SIZE = 50;

    @Parameter(key = "p_special_type_call", group = "Test Creation", description = "Probability of using a non-standard call on a special case (collection/numeric)")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double P_SPECIAL_TYPE_CALL = 0.05;

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

    @Parameter(key = "string_length", group = "Test Creation", description = "Maximum length of randomly generated strings")
    public static int STRING_LENGTH = 20;

    @Parameter(key = "max_string", group = "Test Creation", description = "Maximum length of strings in assertions")
    @IntValue(min = 1, max = 32767) // String literals may not be longer than 32767
    public static int MAX_STRING = 1000;


    @Parameter(key = "epsilon", group = "Test Creation", description = "Epsilon for floats in local search")
    @Deprecated
    // does not seem to be used anywhere
    public static double EPSILON = 0.001;

    @Parameter(key = "max_int", group = "Test Creation", description = "Maximum size of randomly generated integers (minimum range = -1 * max)")
    public static int MAX_INT = 2048;

    @Parameter(key = "restrict_pool", group = "Test Creation", description = "Prohibit integers in the pool greater than max_int")
    public static boolean RESTRICT_POOL = false;

    @Parameter(key = "max_delta", group = "Test Creation", description = "Maximum size of delta for numbers during mutation")
    public static int MAX_DELTA = 20;

    @Parameter(key = "random_perturbation", group = "Test Creation", description = "Probability to replace a primitive with a random new value rather than adding a delta")
    public static double RANDOM_PERTURBATION = 0.2;

    @Parameter(key = "max_array", group = "Test Creation", description = "Maximum length of randomly generated arrays")
    public static int MAX_ARRAY = 10;

    @Parameter(key = "max_attempts", group = "Test Creation", description = "Number of attempts when generating an object before giving up")
    public static int MAX_ATTEMPTS = 1000;

    @Parameter(key = "max_recursion", group = "Test Creation", description = "Recursion depth when trying to create objects")
    public static int MAX_RECURSION = 10;

    @Parameter(key = "max_length", group = "Test Creation", description = "Maximum length of test suites (0 = no check)")
    public static int MAX_LENGTH = 0;

    @Parameter(key = "max_size", group = "Test Creation", description = "Maximum number of test cases in a test suite")
    public static int MAX_SIZE = 100;

    @Parameter(key = "num_tests", group = "Test Creation", description = "Number of tests in initial test suites")
    public static int NUM_TESTS = 2;

    @Parameter(key = "num_random_tests", group = "Test Creation", description = "Number of random tests")
    public static int NUM_RANDOM_TESTS = 20;

    @Parameter(key = "min_initial_tests", group = "Test Creation", description = "Minimum number of tests in initial test suites")
    public static int MIN_INITIAL_TESTS = 1;

    @Parameter(key = "max_initial_tests", group = "Test Creation", description = "Maximum number of tests in initial test suites")
    public static int MAX_INITIAL_TESTS = 10;

    @Parameter(key = "use_deprecated", group = "Test Creation", description = "Include deprecated methods in tests")
    public static boolean USE_DEPRECATED = false;

    @Parameter(key = "insertion_score_uut", group = "Test Creation", description = "Score for selection of insertion of UUT calls")
    public static int INSERTION_SCORE_UUT = 1;

    @Parameter(key = "insertion_uut", group = "Test Creation", description = "Score for selection of insertion of UUT calls")
    public static double INSERTION_UUT = 0.5;

    @Parameter(key = "insertion_uut", group = "Test Creation", description = "Score for selection of insertion of call to a input parameter")
    public static double INSERTION_PARAMETER = 0.4;

    @Parameter(key = "insertion_uut", group = "Test Creation", description = "Score for selection of insertion of call on the environment")
    public static double INSERTION_ENVIRONMENT = 0.1;

    @Parameter(key = "new_object_selection", group = "Test Creation", description = "Score for selection of insertion of UUT calls")
    public static boolean NEW_OBJECT_SELECTION = true;

    @Parameter(key = "insertion_score_object", group = "Test Creation", description = "Score for selection of insertion of call on existing object")
    public static int INSERTION_SCORE_OBJECT = 1;

    @Parameter(key = "insertion_score_parameter", group = "Test Creation", description = "Score for selection of insertion call with existing object")
    public static int INSERTION_SCORE_PARAMETER = 1;

    @Parameter(key = "consider_main_methods", group = "Test Creation", description = "Generate unit tests for 'main(String[] args)' methods as well")
    public static boolean CONSIDER_MAIN_METHODS = true; //should be on by default, otherwise unnecessary lower coverage: up to user if wants to skip them

    @Parameter(key = "headless_mode", group = "Test Generation", description = "Run Java in AWT Headless mode")
    public static boolean HEADLESS_MODE = true;

    @Parameter(key = "p_reflection_on_private", group = "Test Creation", description = "Probability [0,1] of using reflection to set private fields or call private methods")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double P_REFLECTION_ON_PRIVATE = 0.0; // Optimal value: 0.5

    @Parameter(key = "reflection_start_percent", group = "Test Creation", description = "Percentage [0,1] of search budget after which reflection fields/methods handling is activated")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double REFLECTION_START_PERCENT = 0.8;

    @Parameter(key = "p_functional_mocking", group = "Test Creation", description = "Probability [0,1] of using functional mocking (eg Mockito) when creating object instances")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double P_FUNCTIONAL_MOCKING = 0.0; // Optimal value: 0.8

    @Parameter(key = "mock_if_no_generator", group = "Test Creation", description = "Allow mock objects if there are no generators")
    public static boolean MOCK_IF_NO_GENERATOR = true;

    @Parameter(key = "functional_mocking_percent", group = "Test Creation", description = "Percentage [0,1] of search budget after which functional mocking can be activated. Mocking of missing concrete classes will be activated immediately regardless of this parameter")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double FUNCTIONAL_MOCKING_PERCENT = 0.5;

    @Parameter(key = "functional_mocking_input_limit", group = "Test Creation", description = "When mocking a method, define max number of mocked return values for that method. Calls after the last will just re-use the last specified value")
    @DoubleValue(min = 1)
    public static int FUNCTIONAL_MOCKING_INPUT_LIMIT = 5;

    @Parameter(key = "num_parallel_clients", group = "Test Creation", description = "Number of EvoSuite clients to run in parallel")
    public static int NUM_PARALLEL_CLIENTS = 1;

    @Parameter(key = "migrants_iteration_frequency", group = "Test Creation", description = "Determines amount of iterations between sending migrants to other client (-1 to disable any iterations between clients)")
    public static int MIGRANTS_ITERATION_FREQUENCY = 2;

    @Parameter(key = "migrants_communication_rate", group = "Test Creation", description = "Determines amount of migrants per communication step")
    public static int MIGRANTS_COMMUNICATION_RATE = 3;

    // ---------------------------------------------------------------
    // Search algorithm
    public enum Algorithm {
        // random
        RANDOM_SEARCH,
        // GAs
        STANDARD_GA, MONOTONIC_GA, STEADY_STATE_GA, BREEDER_GA, CELLULAR_GA, STANDARD_CHEMICAL_REACTION, MAP_ELITES,
        // mu-lambda
        ONE_PLUS_LAMBDA_LAMBDA_GA, ONE_PLUS_ONE_EA, MU_PLUS_LAMBDA_EA, MU_LAMBDA_EA,
        // many-objective algorithms
        MOSA, DYNAMOSA, LIPS, MIO,
        // multiple-objective optimisation algorithms
        NSGAII, SPEA2
    }

    // MOSA PROPERTIES
    public enum RankingType {
        // Preference sorting is the ranking strategy proposed in
        PREFERENCE_SORTING,
        FAST_NON_DOMINATED_SORTING
    }

    @Parameter(key = "ranking_type", group = "Runtime", description = "type of ranking to use in MOSA")
    public static RankingType RANKING_TYPE = RankingType.PREFERENCE_SORTING;

    public enum MapElitesChoice {
        ALL,
        SINGLE,
        SINGLE_AVG
    }

    @Parameter(key = "map_elites_choice", group = "Search Algorithm", description = "Selection of chromosome branches to mutate")
    public static MapElitesChoice MAP_ELITES_CHOICE = MapElitesChoice.SINGLE_AVG;

    @Parameter(key = "map_elites_mosa_mutations", group = "Search Algorithm", description = "Enable mosa style mutations for map elites")
    public static boolean MAP_ELITES_MOSA_MUTATIONS = true;

    @Parameter(key = "map_elites_random", group = "Search Algorithm", description = "Probability used for adding new chromosomes")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double MAP_ELITES_RANDOM = 0.5;

    @Parameter(key = "map_elites_ignore_features", group = "Search Algorithm", description = "Enable this to disable feature based mapping")
    public static boolean MAP_ELITES_IGNORE_FEATURES = false;

    @Parameter(key = "algorithm", group = "Search Algorithm", description = "Search algorithm")
    public static Algorithm ALGORITHM = Algorithm.DYNAMOSA;

    /**
     * Different models of neighbourhoods in the Cellular GA
     **/
    public enum CGA_Models {
        ONE_DIMENSION,
        LINEAR_FIVE,
        COMPACT_NINE,
        COMPACT_THIRTEEN
    }

    @Parameter(key = "neighborhood_model", group = "Search Algorithm", description = "The model of neighborhood used in case of CGA. L5 is default")
    public static CGA_Models MODEL = CGA_Models.LINEAR_FIVE;

    @Parameter(key = "random_seed", group = "Search Algorithm", description = "Seed used for random generator. If left empty, use current time")
    public static Long RANDOM_SEED = null;

    @Parameter(key = "check_best_length", group = "Search Algorithm", description = "Check length against length of best individual")
    public static boolean CHECK_BEST_LENGTH = true;

    @Parameter(key = "check_parents_length", group = "Search Algorithm", description = "Check length against length of parents")
    public static boolean CHECK_PARENTS_LENGTH = false; // note, based on STVR experiments

    // @Parameter(key = "check_rank_length", group = "Search Algorithm", description = "Use length in rank selection")
    // public static boolean CHECK_RANK_LENGTH = false;

    @Parameter(key = "parent_check", group = "Search Algorithm", description = "Check against parents in Mu+Lambda algorithm")
    public static boolean PARENT_CHECK = true;

    @Parameter(key = "check_max_length", group = "Search Algorithm", description = "Check length against fixed maximum")
    public static boolean CHECK_MAX_LENGTH = true;

    @Parameter(key = "chop_max_length", group = "Search Algorithm", description = "Chop statements after exception if length has reached maximum")
    public static boolean CHOP_MAX_LENGTH = true;

    //----------- DSE, which is a special case of LS ---------------

    /**
     * ilebrero: Mostly for benchmarks for new module, I dont think the legacy strategy is gonna be used anymore
     **/
    public enum DSE_MODULE_VERSION {
        LEGACY,
        NEW
    }

    /**
     * ilebrero: Hope it doesn't make a lot of confusion that there are two versions of arrays supported.
     * - ARRAYS_THEORY: Supports Integers and Reals.
     * - LAZY_VARIABLES: Supports Integers and Reals.
     **/
    public enum DSE_ARRAYS_MEMORY_MODEL_VERSION {
        SELECT_STORE_EXPRESSIONS,
        LAZY_VARIABLES
    }

    @Parameter(key = "dse_module_version", group = "DSE", description = "Module version of DSE, mostly used for benchmarking between modules. For other things the new one is recomended.")
    public static DSE_MODULE_VERSION CURRENT_DSE_MODULE_VERSION = DSE_MODULE_VERSION.NEW;

    @Parameter(key = "dse_enable_arrays_support", group = "DSE", description = "If arrays should be supported by the concolic engine")
    public static boolean IS_DSE_ARRAYS_SUPPORT_ENABLED = true;

    @Parameter(key = "selected_dse_module_arrays_support_version", group = "DSE", description = "Which implementation of arrays is used on the concolic engine.")
    public static DSE_ARRAYS_MEMORY_MODEL_VERSION SELECTED_DSE_ARRAYS_MEMORY_MODEL_VERSION = DSE_ARRAYS_MEMORY_MODEL_VERSION.SELECT_STORE_EXPRESSIONS;

    @Parameter(key = "dse_probability", group = "DSE", description = "Probability used to specify when to use DSE instead of regular LS when LS is applied")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double DSE_PROBABILITY = 0.5;

    @Parameter(key = "dse_constraint_solver_timeout_millis", group = "DSE", description = "Maximum number of solving time for Constraint solver in milliseconds")
    public static long DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS = 1000;

    @Parameter(key = "dse_rank_branch_conditions", group = "DSE", description = "Rank branch conditions")
    public static boolean DSE_RANK_BRANCH_CONDITIONS = true;

    @Parameter(key = "dse_negate_all_conditions", group = "DSE", description = "Negate all branch conditions in the path condition (covered or not)")
    public static boolean DSE_NEGATE_ALL_CONDITIONS = true;

    @Parameter(key = "dse_constraint_length", group = "DSE", description = "Maximal length of the constraints in DSE")
    public static int DSE_CONSTRAINT_LENGTH = 100000;

    @Parameter(key = "dse_constant_probability", group = "DSE", description = "Probability with which to use constants from the constraints when resetting variables during search")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double DSE_CONSTANT_PROBABILITY = 0.5;

    @Parameter(key = "dse_variable_resets", group = "DSE", description = "Times DSE resets the int and real variables with random values")
    public static int DSE_VARIABLE_RESETS = 2;

    // By default the target is 100
    @Parameter(key = "dse_target_coverage", group = "DSE", description = "Percentage (out of 100) of target coverage to cover")
    public static int DSE_TARGET_COVERAGE = 100;

    public enum DSEType {
        /**
         * apply DSE per statement
         */
        STATEMENT,
        /**
         * apply DSE with all primitives in a test
         */
        TEST,
        /**
         * DSE on whole suites
         */
        SUITE
    }

    // NOTE (ilebrero): This is the current method name being explored. This is NOT a good practice, but it's
    //	     the only way I can imagine to get the current method name for saving the bytecodeLogging info in a file.
    //		 TODO: Is there a better way of doing this?
    public static String CURRENT_TARGET_METHOD = "";

    // NOTE: by default we use the sage implementation of the algorithm
    @Parameter(key = "dse_exploration_algorithm", group = "DSE", description = "Type of DSE algorithm to use.")
    public static DSEAlgorithms DSE_EXPLORATION_ALGORITHM_TYPE = DSEAlgorithms.GENERATIONAL_SEARCH;

    @Parameter(key = "local_search_dse", group = "DSE", description = "Granularity of DSE application")
    public static DSEType LOCAL_SEARCH_DSE = DSEType.TEST;

    @Deprecated
    @Parameter(key = "dse_keep_all_tests", group = "DSE", description = "Keep tests even if they do not increase fitness")
    public static boolean DSE_KEEP_ALL_TESTS = false;

    public enum SolverType {
        EVOSUITE_SOLVER, Z3_SOLVER, CVC4_SOLVER
    }

    @Parameter(key = "dse_solver", group = "DSE", description = "Specify which constraint solver to use. Note: external solver will need to be installed and cofigured separately")
    public static SolverType DSE_SOLVER = SolverType.EVOSUITE_SOLVER;

    @Parameter(key = "z3_path", group = "DSE", description = "Indicates the path to the Z3 solver")
    public static String Z3_PATH = null;

    @Parameter(key = "cvc4_path", group = "DSE", description = "Indicates the path to the CVC4 solver")
    public static String CVC4_PATH = null;

    public enum DSEStoppingConditionCriterion {
        TARGETCOVERAGE,
        MAXTIME,
        /**
         * In seconds
         */
        ZEROFITNESS,
        MAXTESTS,
        DEFAULTS /** The ones that are setted by default on the algorithm + Strategy */
    }

    @Parameter(key = "dse_stopping_condition", group = "DSE", description = "Indicate which stopping condition to use.")
    public static DSEStoppingConditionCriterion DSE_STOPPING_CONDITION = DSEStoppingConditionCriterion.DEFAULTS;

    @Parameter(key = "bytecode_logging_enabled", group = "DSE", description = "Indicates whether bytecode instructions that are being executed should be logged.")
    public static boolean BYTECODE_LOGGING_ENABLED = false;

    @Parameter(key = "bytecode_logging_mode", group = "DSE", description = "How to log executed bytecode")
    public static DSEBytecodeLoggingMode BYTECODE_LOGGING_MODE = DSEBytecodeLoggingMode.STD_OUT;

    // TODO (ilebrero): add other modes
    public enum DSEBytecodeLoggingMode {
        STD_OUT,
        FILE_DUMP
    }

    // --------- LS ---------

    @Parameter(key = "local_search_rate", group = "Local Search", description = "Apply local search at every X generation")
    public static int LOCAL_SEARCH_RATE = -1;

    @Parameter(key = "local_search_probability", group = "Local Search", description = "Probability of applying local search at every X generation")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double LOCAL_SEARCH_PROBABILITY = 1.0;

    @Deprecated
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
    public static double LOCAL_SEARCH_ADAPTATION_RATE = 2.0;

    @Parameter(key = "local_search_budget", group = "Local Search", description = "Maximum budget usable for improving individuals per local search")
    public static long LOCAL_SEARCH_BUDGET = 5;

    public enum LocalSearchBudgetType {
        STATEMENTS, TESTS,
        /**
         * Time expressed in seconds
         */
        TIME,
        SUITES, FITNESS_EVALUATIONS
    }

    @Parameter(key = "local_search_budget_type", group = "Local Search", description = "Interpretation of local_search_budget")
    public static LocalSearchBudgetType LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.TIME;

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

    @Parameter(key = "crossover_rate", group = "Search Algorithm", description = "Probability of crossover")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double CROSSOVER_RATE = 0.75;

    @Parameter(key = "headless_chicken_test", group = "Search Algorithm", description = "Activate headless chicken test")
    public static boolean HEADLESS_CHICKEN_TEST = false;

    @Parameter(key = "mutation_rate", group = "Search Algorithm", description = "Probability of mutation")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double MUTATION_RATE = 0.75;

    @Parameter(key = "breeder_truncation", group = "Search Algorithm", description = "Percentage of population to use for breeding in breeder GA")
    @DoubleValue(min = 0.01, max = 1.0)
    public static double TRUNCATION_RATE = 0.5;

    @Parameter(key = "number_of_mutations", group = "Search Algorithm", description = "Number of single mutations applied on an individual when a mutation event occurs")
    public static int NUMBER_OF_MUTATIONS = 1;

    @Parameter(key = "p_test_insertion", group = "Search Algorithm", description = "Initial probability of inserting a new test in a test suite")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double P_TEST_INSERTION = 0.1;

    @Parameter(key = "p_statement_insertion", group = "Search Algorithm", description = "Initial probability of inserting a new statement in a test case")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double P_STATEMENT_INSERTION = 0.5;

    @Parameter(key = "p_change_parameter", group = "Search Algorithm", description = "Probability of replacing parameters when mutating a method or constructor statementa in a test case")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double P_CHANGE_PARAMETER = 0.1;

    @Parameter(key = "p_test_delete", group = "Search Algorithm", description = "Probability of deleting statements during mutation")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double P_TEST_DELETE = 1d / 3d;

    @Parameter(key = "p_test_change", group = "Search Algorithm", description = "Probability of changing statements during mutation")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double P_TEST_CHANGE = 1d / 3d;

    @Parameter(key = "p_test_insert", group = "Search Algorithm", description = "Probability of inserting new statements during mutation")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double P_TEST_INSERT = 1d / 3d;

    @Parameter(key = "kincompensation", group = "Search Algorithm", description = "Penalty for duplicate individuals")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double KINCOMPENSATION = 1.0;

    @Parameter(key = "elite", group = "Search Algorithm", description = "Elite size for search algorithm")
    public static int ELITE = 1;

    @Parameter(key = "mu", group = "Search Algorithm", description = "Number of individuals selected by Mu + Lambda EA for the next generation")
    public static int MU = 1;

    @Parameter(key = "lambda", group = "Search Algorithm", description = "Number of individuals produced by Mu + Lambda EA at each generation")
    public static int LAMBDA = 1;

    @Parameter(key = "tournament_size", group = "Search Algorithm", description = "Number of individuals for tournament selection")
    public static int TOURNAMENT_SIZE = 10;

    @Parameter(key = "rank_bias", group = "Search Algorithm", description = "Bias for better individuals in rank selection")
    public static double RANK_BIAS = 1.7;

    @Parameter(key = "chromosome_length", group = "Search Algorithm", description = "Maximum length of chromosomes during search")
    @IntValue(min = 1, max = 100000)
    public static int CHROMOSOME_LENGTH = 40;

    @Parameter(key = "number_of_tests_per_target", group = "Search Algorithm", description = "Number of test cases for each target goal to keep in an archive")
    public static int NUMBER_OF_TESTS_PER_TARGET = 10;

    @Parameter(key = "p_random_test_or_from_archive", group = "Search Algorithm", description = "Probability [0,1] of sampling a new test at random or choose an existing one in an archive")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double P_RANDOM_TEST_OR_FROM_ARCHIVE = 0.5;

    @Parameter(key = "exploitation_starts_at_percent", group = "Search Algorithm", description = "Percentage [0,1] of search budget after which exploitation is activated")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double EXPLOITATION_STARTS_AT_PERCENT = 0.5;

    @Parameter(key = "max_num_mutations_before_giving_up", group = "Search Algorithm", description = "Maximum number of mutations allowed to be done on the same individual before sampling a new one")
    public static int MAX_NUM_MUTATIONS_BEFORE_GIVING_UP = 10;

    @Parameter(key = "max_num_fitness_evaluations_before_giving_up", group = "Search Algorithm", description = "Maximum number of fitness evaluations allowed to be done on the same individual before sampling a new one")
    public static int MAX_NUM_FITNESS_EVALUATIONS_BEFORE_GIVING_UP = 10;

    @Parameter(key = "population", group = "Search Algorithm", description = "Population size of genetic algorithm")
    @IntValue(min = 1)
    public static int POPULATION = 50;

    public enum PopulationLimit {
        INDIVIDUALS, TESTS, STATEMENTS
    }

    @Parameter(key = "population_limit", group = "Search Algorithm", description = "What to use as limit for the population size")
    public static PopulationLimit POPULATION_LIMIT = PopulationLimit.INDIVIDUALS;

    @Parameter(key = "write_individuals", group = "Search Algorithm",
            description = "Write to a file all fitness values of each individual on each iteration of a GA")
    public static boolean WRITE_INDIVIDUALS = false;

    @Parameter(key = "search_budget", group = "Search Algorithm", description = "Maximum search duration")
    @LongValue(min = 1)
    public static long SEARCH_BUDGET = 60;

    @Parameter(key = "OUTPUT_DIR", group = "Runtime", description = "Directory in which to put generated files")
    public static String OUTPUT_DIR = "evosuite-files";

    public static String PROPERTIES_FILE = OUTPUT_DIR + File.separator + "evosuite.properties";

    public enum StoppingCondition {
        MAXSTATEMENTS, MAXTESTS,
        /**
         * Max time in seconds
         */
        MAXTIME,
        MAXGENERATIONS, MAXFITNESSEVALUATIONS, TIMEDELTA
    }

    @Parameter(key = "stopping_condition", group = "Search Algorithm", description = "What condition should be checked to end the search")
    public static StoppingCondition STOPPING_CONDITION = StoppingCondition.MAXTIME;

    public enum CrossoverFunction {
        SINGLEPOINTRELATIVE, SINGLEPOINTFIXED, SINGLEPOINT, COVERAGE, UNIFORM
    }

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
        RANK, ROULETTEWHEEL, TOURNAMENT, BINARY_TOURNAMENT, RANK_CROWD_DISTANCE_TOURNAMENT, BESTK, RANDOMK
    }

    @Parameter(key = "selection_function", group = "Search Algorithm", description = "Selection function during search")
    public static SelectionFunction SELECTION_FUNCTION = SelectionFunction.RANK;

    @Parameter(key = "emigrant_selection_function", group = "Search Algorithm", description = "Selection function for emigrant selection during search")
    public static SelectionFunction EMIGRANT_SELECTION_FUNCTION = SelectionFunction.RANDOMK;

    public enum MutationProbabilityDistribution {
        UNIFORM, BINOMIAL
    }

    /**
     * Constant <code>MUTATION_PROBABILITY_DISTRIBUTION</code>
     */
    @Parameter(key = "mutation_probability_distribution", group = "Search Algorithm", description = "Mutation probability distribution")
    public static MutationProbabilityDistribution MUTATION_PROBABILITY_DISTRIBUTION = MutationProbabilityDistribution.UNIFORM;

    public enum SecondaryObjective {
        AVG_LENGTH, MAX_LENGTH, TOTAL_LENGTH, SIZE, EXCEPTIONS, IBRANCH, RHO
    }

    @Parameter(key = "secondary_objectives", group = "Search Algorithm", description = "Secondary objective during search")
    public static SecondaryObjective[] SECONDARY_OBJECTIVE = new SecondaryObjective[]{SecondaryObjective.TOTAL_LENGTH};

    @Parameter(key = "enable_secondary_objective_after", group = "Search Algorithm", description = "Activate the second secondary objective after a certain amount of search budget")
    public static int ENABLE_SECONDARY_OBJECTIVE_AFTER = 0;

    @Parameter(key = "enable_secondary_starvation", group = "Search Algorithm", description = "Activate the second secondary objective after a certain amount of search budget")
    public static boolean ENABLE_SECONDARY_OBJECTIVE_STARVATION = false;

    @Parameter(key = "starvation_after_generation", group = "Search Algorithm", description = "Activate the second secondary objective after a certain amount of search budget")
    public static int STARVATION_AFTER_GENERATION = 500;

    @Parameter(key = "bloat_factor", group = "Search Algorithm", description = "Maximum relative increase in length")
    public static int BLOAT_FACTOR = 2;

    @Parameter(key = "stop_zero", group = "Search Algorithm", description = "Stop optimization once goal is covered")
    public static boolean STOP_ZERO = true;

    @Parameter(key = "dynamic_limit", group = "Search Algorithm", description = "Multiply search budget by number of test goals")
    public static boolean DYNAMIC_LIMIT = false;

    @Parameter(key = "global_timeout", group = "Search Algorithm", description = "Maximum seconds allowed for entire search when not using time as stopping criterion")
    @IntValue(min = 0)
    public static int GLOBAL_TIMEOUT = 120;

    @Parameter(key = "minimization_timeout", group = "Search Algorithm", description = "Seconds allowed for minimization at the end")
    @IntValue(min = 0)
    public static int MINIMIZATION_TIMEOUT = 60;

    @Parameter(key = "assertion_timeout", group = "Search Algorithm", description = "Seconds allowed for assertion generation at the end")
    @IntValue(min = 0)
    public static int ASSERTION_TIMEOUT = 60;

    @Parameter(key = "assertion_minimization_fallback", group = "Search Algorithm", description = "Percentage of tests expected to have assertions at fallback check time")
    public static double ASSERTION_MINIMIZATION_FALLBACK = 1 / 2d;

    @Parameter(key = "assertion_minimization_fallback_time", group = "Search Algorithm", description = "Percentage of tests applied to minimisation before checking fallback. 1.0 for no fallback.")
    public static double ASSERTION_MINIMIZATION_FALLBACK_TIME = 2 / 3d;

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

    @Parameter(key = "reuse_leftover_time", group = "Search Algorithm", description = "If a phase is ended before its timeout, allow the next phase to run over its timeout")
    public static boolean REUSE_LEFTOVER_TIME = false;

    @Parameter(key = "track_boolean_branches", group = "Search Algorithm", description = "Track branches that have a distance of either 0 or 1")
    public static boolean TRACK_BOOLEAN_BRANCHES = false;

    @Parameter(key = "track_covered_gradient_branches", group = "Search Algorithm", description = "Track gradient branches that were covered")
    public static boolean TRACK_COVERED_GRADIENT_BRANCHES = false;

    @Parameter(key = "branch_comparison_types", group = "Search Algorithm", description = "Track branch comparison types based on the bytecode")
    public static boolean BRANCH_COMPARISON_TYPES = false;

    @Parameter(key = "track_diversity", group = "Search Algorithm", description = "Track population diversity")
    public static boolean TRACK_DIVERSITY = false;

    @Parameter(key = "analysis_criteria", group = "Output", description = "List of criteria which should be measured on the completed test suite")
    public static String ANALYSIS_CRITERIA = "";

    @Parameter(key = "use_existing_coverage", group = "Experimental", description = "Use the coverage of existing test cases")
    public static boolean USE_EXISTING_COVERAGE = false;

    @Parameter(key = "epson", group = "Experimental", description = "Epson")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double EPSON = 0.01;

    // ---------------------------------------------------------------
    // Chemical Reaction Optimization Parameters

    @Parameter(key = "kinetic_energy_loss_rate", group = "Chemical Reaction Optimization", description = "Rate at which molecules lose kinetic energy")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double KINETIC_ENERGY_LOSS_RATE = 0.2;

    @Parameter(key = "molecular_collision_rate", group = "Chemical Reaction Optimization", description = "Rate of inter molecular collisions")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double MOLECULAR_COLLISION_RATE = 0.2;

    @Parameter(key = "initial_kinetic_energy", group = "Chemical Reaction Optimization", description = "Initial kinetic energy of each molecule")
    public static double INITIAL_KINETIC_ENERGY = 1000.0;

    @Parameter(key = "decomposition_threshold", group = "Chemical Reaction Optimization", description = "Threshold to be checked to decide when to trigger decomposition")
    public static int DECOMPOSITION_THRESHOLD = 500;

    @Parameter(key = "synthesis_threshold", group = "Chemical Reaction Optimization", description = "Threshold to be checked to decide when to trigger synthesis")
    public static int SYNTHESIS_THRESHOLD = 10;

    //----------------------------------------------------------------
    // Continuous Test Generation

    @Parameter(key = "ctg_memory", group = "Continuous Test Generation", description = "Total Memory (in MB) that CTG will use")
    public static int CTG_MEMORY = 1000;

    @Parameter(key = "ctg_cores", group = "Continuous Test Generation", description = "Number of cores CTG will use")
    public static int CTG_CORES = 1;

    @Parameter(key = "ctg_time", group = "Continuous Test Generation", description = "How many minutes in total CTG will run")
    public static int CTG_TIME = 3;

    @Parameter(key = "ctg_time_per_class", group = "Continuous Test Generation", description = "How many minutes to allocate for each class. If this parameter is set, then ctg_time is going to be ignored. This parameter is mainly meant for debugging purposes.")
    public static Integer CTG_TIME_PER_CLASS = null;

    @Parameter(key = "ctg_min_time_per_job", group = "Continuous Test Generation", description = "How many minutes each class under test should have at least")
    public static int CTG_MIN_TIME_PER_JOB = 1;

    @Parameter(key = "ctg_dir", group = "Continuous Test Generation", description = "Where generated files will be stored")
    public static String CTG_DIR = ".evosuite";

    @Parameter(key = "ctg_bests_folder", group = "Continuous Test Generation", description = "Folder where all the best test suites generated so far in all CTG runs are stored")
    public static String CTG_BESTS_DIR_NAME = "best-tests";

    @Parameter(key = "ctg_generation_dir_prefix", group = "Continuous Test Generation", description = "")
    public static String CTG_GENERATION_DIR_PREFIX = null;

    @Parameter(key = "ctg_delete_old_tmp_folders", group = "Continuous Test Generation", description = "If true, delete all the tmp folders before starting a new CTG run")
    public static boolean CTG_DELETE_OLD_TMP_FOLDERS = true;

    @Parameter(key = "ctg_tmp_logs_dir_name", group = "Continuous Test Generation", description = "")
    public static String CTG_TMP_LOGS_DIR_NAME = "logs";

    @Parameter(key = "ctg_tmp_pools_dir_name", group = "Continuous Test Generation", description = "")
    public static String CTG_TMP_POOLS_DIR_NAME = "pools";

    @Parameter(key = "ctg_tmp_reports_dir_name", group = "Continuous Test Generation", description = "")
    public static String CTG_TMP_REPORTS_DIR_NAME = "reports";

    @Parameter(key = "ctg_tmp_tests_dir_name", group = "Continuous Test Generation", description = "")
    public static String CTG_TMP_TESTS_DIR_NAME = "tests";

    @Parameter(key = "ctg_seeds_file_in", group = "Continuous Test Generation", description = "If specified, load serialized tests from that file")
    public static String CTG_SEEDS_FILE_IN = null;

    @Parameter(key = "ctg_seeds_file_out", group = "Continuous Test Generation", description = "If specified, save serialized tests to that file")
    public static String CTG_SEEDS_FILE_OUT = null;

    @Parameter(key = "ctg_seeds_dir_name", group = "Continuous Test Generation", description = "Name of seed folder where the serialized tests are stored")
    public static String CTG_SEEDS_DIR_NAME = "seeds";

    @Parameter(key = "ctg_seeds_ext", group = "Continuous Test Generation", description = "File extension for serialized test files")
    public static String CTG_SEEDS_EXT = "seed";

    @Parameter(key = "ctg_project_info", group = "Continuous Test Generation", description = "XML file which stores stats about all CTG executions")
    public static String CTG_PROJECT_INFO = "project_info.xml";

    @Parameter(key = "ctg_history_file", group = "Continuous Test Generation", description = "File with the list of new(A)/modified(M)/deleted(D) files")
    public static String CTG_HISTORY_FILE = null;

    @Parameter(key = "ctg_selected_cuts", group = "Continuous Test Generation", description = "Comma ',' separated list of CUTs to use in CTG. If none specified, then test all classes")
    public static String CTG_SELECTED_CUTS = null;

    @Parameter(key = "ctg_selected_cuts_file_location", group = "Continuous Test Generation", description = "Absolute path of text file where classes to test are specified. This is needed for operating systems like Windows where there are hard limits on parameters' size")
    public static String CTG_SELECTED_CUTS_FILE_LOCATION = null;

    @Parameter(key = "ctg_export_folder", group = "Continuous Test Generation", description = "If specified, make a copy of all tests into the target export folder")
    public static String CTG_EXPORT_FOLDER = null;

    @Parameter(key = "ctg_debug_port", group = "Continuous Test Generation", description = "Port for remote debugging of 'Master' spawn processes. 'Clinet' process will have port+1. This only applies when for a single CUT.")
    public static Integer CTG_DEBUG_PORT = null;

    /**
     * The types of CTG schedules that can be used
     */
    public enum AvailableSchedule {
        SIMPLE, BUDGET, SEEDING, BUDGET_AND_SEEDING, HISTORY
    }

    /*
     * FIXME choose best schedule for default
     * Note: most likely we ll use this parameter only for testing/experiments.
     * Maven plugin will use the default, best one
     */
    @Parameter(key = "ctg_schedule", group = "Continuous Test Generation", description = "Schedule used to run jobs")
    public static AvailableSchedule CTG_SCHEDULE = AvailableSchedule.BUDGET;


    @Parameter(key = "ctg_extra_args", group = "Continuous Test Generation", description = "Extra '-D' arguments to pass to EvoSuite test generation processes")
    public static String CTG_EXTRA_ARGS = null;


    // ---------------------------------------------------------------
    // Single branch mode
    @Parameter(key = "random_tests", group = "Single Branch Mode", description = "Number of random tests to run before test generation (Single branch mode)")
    public static int RANDOM_TESTS = 0;

    @Parameter(key = "skip_covered", group = "Single Branch Mode", description = "Skip coverage goals that have already been (coincidentally) covered")
    public static boolean SKIP_COVERED = true;

    @Parameter(key = "reuse_budget", group = "Single Branch Mode", description = "Use leftover budget on unsatisfied test goals (Single branch mode)")
    public static boolean REUSE_BUDGET = true;

    @Parameter(key = "shuffle_goals", group = "Single Branch Mode", description = "Shuffle test goals before test generation (Single branch mode)")
    public static boolean SHUFFLE_GOALS = true;

    @Parameter(key = "recycle_chromosomes", group = "Single Branch Mode", description = "Seed initial population with related individuals (Single branch mode)")
    public static boolean RECYCLE_CHROMOSOMES = true;

    // ---------------------------------------------------------------
    // Output
    public enum OutputFormat {
        JUNIT3, JUNIT4, TESTNG, JUNIT5
    }

    @Parameter(key = "test_format", group = "Output", description = "Format of the resulting test cases")
    public static OutputFormat TEST_FORMAT = OutputFormat.JUNIT4;

    @Parameter(key = "test_comments", group = "Output", description = "Include a header with coverage information for each test")
    public static boolean TEST_COMMENTS = false;

    @Parameter(key = "test_scaffolding", group = "Output", description = "Generate all the scaffolding needed to run EvoSuite JUnit tests in a separate file")
    public static boolean TEST_SCAFFOLDING = true;

    @Parameter(key = "max_length_test_case", group = "Output", description = "Maximum number of statements (normal statements and assertions)")
    public static int MAX_LENGTH_TEST_CASE = 2500;

    @Parameter(key = "no_runtime_dependency", group = "Output", description = "Avoid runtime dependencies in JUnit test")
    public static boolean NO_RUNTIME_DEPENDENCY = false;

    @Parameter(key = "print_to_system", group = "Output", description = "Allow test output on console")
    public static boolean PRINT_TO_SYSTEM = false;

    @Parameter(key = "plot", group = "Output", description = "Create plots of size and fitness")
    public static boolean PLOT = false;

    @Parameter(key = "coverage_matrix", group = "Output", description = "Create a coverage matrix (each row represents the coverage a test case, and each column represents one goal")
    public static boolean COVERAGE_MATRIX = false;

    @Parameter(key = "coverage_matrix_filename", group = "Output", description = "File to which the coverage matrix is written")
    public static String COVERAGE_MATRIX_FILENAME = "matrix";

    @Parameter(key = "junit_tests", group = "Output", description = "Create JUnit test suites")
    public static boolean JUNIT_TESTS = true;

    public enum JUnitCheckValues {
        TRUE, OPTIONAL, FALSE
    }

    @Parameter(key = "junit_check", group = "Output", description = "Compile and run resulting JUnit test suite (if any was created)")
    public static JUnitCheckValues JUNIT_CHECK = JUnitCheckValues.TRUE;

    @Parameter(key = "junit_check_on_separate_process", group = "Output", description = "Compile and run resulting JUnit test suite on a separate process")
    @Deprecated
    //this gives quite a few issues. and hopefully the problems it was aimed to fix are no longer
    public static boolean JUNIT_CHECK_ON_SEPARATE_PROCESS = false;

    @Parameter(key = "junit_suffix", group = "Output", description = "Suffix that is appended at each generated JUnit file name")
    public static String JUNIT_SUFFIX = "_ESTest";

    @Parameter(key = "junit_failed_suffix", group = "Output", description = "Suffix that is appended at each generated JUnit file name for failing tests")
    public static String JUNIT_FAILED_SUFFIX = "_Failed_ESTest";

    //WARN: do not change this value, as had to be hardcoded in quite a few places :( if really need to change it,
    // all that code has to be changed as well
    @Parameter(key = "scaffolding_suffix", group = "Output", description = "Suffix used to specify scaffolding files")
    public static String SCAFFOLDING_SUFFIX = "scaffolding";

    @Parameter(key = "tools_jar_location", group = "Output", description = "Location of where to locate tools.jar")
    public static String TOOLS_JAR_LOCATION = null;

    @Parameter(key = "pure_inspectors", group = "Output", description = "Selects only an underapproximation of all inspectors that are also pure (no side-effects)")
    public static boolean PURE_INSPECTORS = true;

    @Parameter(key = "pure_equals", group = "Output", description = "Selects only an underapproximation of equals(Object) that are also known to be pure (no side-effects)")
    public static boolean PURE_EQUALS = false;

    /**
     * TODO: this functionality is not implemented yet
     */
    @Parameter(key = "junit_extend", group = "Output", description = "Extend existing JUnit test suite")
    public static String JUNIT_EXTEND = "";

    @Parameter(key = "junit", group = "Experimental", description = "A colon(:) separated list of JUnit suites to execute. Can be a prefix (i.e., package name), a directory, a jar file, or the full name of a JUnit suite.")
    public static String JUNIT = "";

    @Parameter(key = "log_goals", group = "Output", description = "Create a CSV file for each individual evolution")
    public static boolean LOG_GOALS = false;

    @Parameter(key = "log.level", group = "Output", description = "Verbosity level of logger")
    public static String LOG_LEVEL = null;

    @Parameter(key = "log.target", group = "Output", description = "Target logger - all logging if not set")
    public static String LOG_TARGET = null;

    @Parameter(key = "minimize", group = "Output", description = "Minimize test suite after generation")
    public static boolean MINIMIZE = true;

    @Parameter(key = "minimize_second_pass", group = "Output", description = "Perform a second minimization pass as the first one may retain subsumed tests")
    public static boolean MINIMIZE_SECOND_PASS = true;

    @Parameter(key = "minimize_sort", group = "Output", description = "Sort goals before Minimization")
    public static boolean MINIMIZE_SORT = true;


    @Parameter(key = "minimize_skip_coincidental", group = "Output", description = "Minimize test suite after generation")
    public static boolean MINIMIZE_SKIP_COINCIDENTAL = true;

    @Parameter(key = "minimize_old", group = "Output", description = "Minimize test suite using old algorithm")
    @Deprecated
    public static boolean MINIMIZE_OLD = false;

    @Parameter(key = "minimize_values", group = "Output", description = "Minimize constants and method calls")
    public static boolean MINIMIZE_VALUES = false;

    @Parameter(key = "lm_strings", group = "Output", description = "Use language model on strings.  The parameter minimize_values must also be true.")
    public static boolean LM_STRINGS = false;

    @Parameter(key = "minimize_strings", group = "Output", description = "Try to minimise strings by deleting non-printables. The parameter minimize_values must also be true,")
    public static boolean MINIMIZE_STRINGS = true;

    @Parameter(key = "lm_src", description = "Text file for the language model.")
    public static String LM_SRC = "ukwac_char_lm";

    @Parameter(key = "lm_iterations", description = "Number of 1+1EA generations PER STRING PRIMITIVE for language model optimiser.")
    public static int LM_ITERATIONS = 1000;

    @Parameter(key = "lm_mutation_type", description = "Type of mutation to use in language model string optimiser.")
    public static MutationType LM_MUTATION_TYPE = MutationType.EVOSUITE;

    @Parameter(key = "coverage", group = "Output", description = "Calculate coverage after test suite generation")
    public static boolean COVERAGE = true;

    @Parameter(key = "inline", group = "Output", description = "Inline all constants")
    public static boolean INLINE = true;

    @Parameter(key = "write_pool", group = "Output", description = "Keep sequences for object pool")
    public static String WRITE_POOL = "";

    @Parameter(key = "report_dir", group = "Output", description = "Directory in which to put HTML and CSV reports")
    public static String REPORT_DIR = "evosuite-report";

    @Parameter(key = "bytecode_logging_report_dir", group = "Output", description = "Directory in which to put TXT executed bytecode logs.")
    public static String BYTECODE_LOGGING_REPORT_DIR = "executed-bytecode-logs";

    @Parameter(key = "output_variables", group = "Output", description = "List of variables to output to CSV file. Variables are separated by commas. Null represents default values")
    public static String OUTPUT_VARIABLES = null;

    @Parameter(key = "configuration_id", group = "Output", description = "Label that identifies the used configuration of EvoSuite. This is only done when running experiments.")
    public static String CONFIGURATION_ID = null;

    @Parameter(key = "group_id", group = "Output", description = "Label that specifies a group the SUT belongs to. This is only needed for running experiments.")
    public static String GROUP_ID = "none";

    @Parameter(key = "save_all_data", group = "Output", description = "Generate and store all data reports")
    public static boolean SAVE_ALL_DATA = true;

    @Parameter(key = "print_goals", group = "Output", description = "Print out goals of class under test")
    public static boolean PRINT_GOALS = false;

    @Parameter(key = "all_goals_file", group = "Output", description = "File to which the list of all goals is written")
    public static String ALL_GOALS_FILE = REPORT_DIR + File.separator + "all.goals";

    @Parameter(key = "write_all_goals_file", group = "Output", description = "If enabled, the list of all goals is written to a file")
    public static boolean WRITE_ALL_GOALS_FILE = false;

    @Parameter(key = "print_current_goals", group = "Output", description = "Print out current goal during test generation")
    public static boolean PRINT_CURRENT_GOALS = true;

    @Parameter(key = "print_covered_goals", group = "Output", description = "Print out covered goals during test generation")
    public static boolean PRINT_COVERED_GOALS = false;

    @Parameter(key = "print_missed_goals", group = "Output", description = "Print out missed goals at the end")
    public static boolean PRINT_MISSED_GOALS = false;

    @Parameter(key = "write_covered_goals_file", group = "Output", description = "Write covered goals file")
    public static boolean WRITE_COVERED_GOALS_FILE = false;

    @Parameter(key = "covered_goals_file", group = "Output", description = "File with relation of tests and covered goals")
    public static String COVERED_GOALS_FILE = REPORT_DIR + File.separator + "covered.goals";

    @Parameter(key = "assertions", group = "Output", description = "Create assertions")
    public static boolean ASSERTIONS = true;

    public enum AssertionStrategy {
        ALL, MUTATION, UNIT
    }

    @Parameter(key = "assertion_strategy", group = "Output", description = "Which assertions to generate")
    public static AssertionStrategy ASSERTION_STRATEGY = AssertionStrategy.MUTATION;

    @Parameter(key = "filter_assertions", group = "Output", description = "Filter flaky assertions")
    public static boolean FILTER_ASSERTIONS = false;

    @Parameter(key = "max_mutants_per_test", group = "Output", description = "How many mutants to use when trying to find assertions for a test")
    public static int MAX_MUTANTS_PER_TEST = 100;

    @Parameter(key = "max_mutants_per_method", group = "Output", description = "How many mutants can be inserted into a single method")
    public static int MAX_MUTANTS_PER_METHOD = 700;

    @Parameter(key = "max_mutants_per_class", group = "Output", description = "How many mutants can be used as target for a single class")
    public static int MAX_MUTANTS_PER_CLASS = 1000;

    @Parameter(key = "max_replace_mutants", group = "Output", description = "How many replacement mutants can be inserted for any one variable")
    public static int MAX_REPLACE_MUTANTS = 100;

    @Parameter(key = "test_dir", group = "Output", description = "Directory in which to place JUnit tests")
    public static String TEST_DIR = "evosuite-tests";

    @Parameter(key = "write_cfg", group = "Output", description = "Create CFG graphs")
    public static boolean WRITE_CFG = false;

    @Parameter(key = "shutdown_hook", group = "Output", description = "Store test suite on Ctrl+C")
    public static boolean SHUTDOWN_HOOK = true;

    @Parameter(key = "show_progress", group = "Output", description = "Show progress bar on console")
    public static boolean SHOW_PROGRESS = true;

    @Parameter(key = "serialize_result", group = "Output", description = "Serialize result of search to main process")
    public static boolean SERIALIZE_RESULT = false;

    @Parameter(key = "new_statistics", group = "Output", description = "Use the new statistics backend on the master")
    public static boolean NEW_STATISTICS = true;

    @Parameter(key = "ignore_missing_statistics", group = "Output", description = "Return an empty string for missing output variables")
    public static boolean IGNORE_MISSING_STATISTICS = false;

    @Parameter(key = "float_precision", group = "Output", description = "Precision to use in float comparisons and assertions")
    public static float FLOAT_PRECISION = 0.01F;

    @Parameter(key = "double_precision", group = "Output", description = "Precision to use in double comparisons and assertions")
    public static double DOUBLE_PRECISION = 0.01;

    //@Parameter(key = "old_statistics", group = "Output", description = "Use the old statistics backend on the master")
    //public static boolean OLD_STATISTICS = false;

    @Parameter(key = "validate_runtime_variables", group = "Output", description = "Validate runtime values before writing statistics")
    public static boolean VALIDATE_RUNTIME_VARIABLES = true;

    @Parameter(key = "serialize_ga", group = "Output", description = "Include the GA instance in the test generation result")
    public static boolean SERIALIZE_GA = false;

    @Parameter(key = "serialize_dse", group = "Output", description = "Include the DSE instance in the test generation result")
    public static boolean SERIALIZE_DSE = false;

    public enum StatisticsBackend {
        NONE, CONSOLE, CSV, HTML, DEBUG
    }

    @Parameter(key = "statistics_backend", group = "Output", description = "Which backend to use to collect data")
    public static StatisticsBackend STATISTICS_BACKEND = StatisticsBackend.CSV;

    @Parameter(key = "timeline_interval", group = "Output", description = "Time interval in milliseconds for timeline statistics")
    public static long TIMELINE_INTERVAL = 60 * 1000;

    @Parameter(key = "timeline_interpolation", group = "Output", description = "Interpolate timeline values")
    public static boolean TIMELINE_INTERPOLATION = true;

    public enum OutputGranularity {
        MERGED, TESTCASE
    }

    @Parameter(key = "output_granularity", group = "Output", description = "Write all test cases for a class into a single file or to separate files.")
    public static OutputGranularity OUTPUT_GRANULARITY = OutputGranularity.MERGED;

    @Parameter(key = "max_coverage_depth", group = "Output", description = "Maximum depth in the calltree to count a branch as covered")
    public static int MAX_COVERAGE_DEPTH = -1;

    public enum TestNamingStrategy {
        NUMBERED, COVERAGE
    }

    @Parameter(key = "test_naming_strategy", group = "Output", description = "What strategy to use to derive names for tests")
    public static TestNamingStrategy TEST_NAMING_STRATEGY = TestNamingStrategy.NUMBERED;

    // ---------------------------------------------------------------
    // Sandbox
    @Parameter(key = "sandbox", group = "Sandbox", description = "Execute tests in a sandbox environment")
    public static boolean SANDBOX = true;

    @Parameter(key = "sandbox_mode", group = "Sandbox", description = "Mode in which the sandbox is applied")
    public static Sandbox.SandboxMode SANDBOX_MODE = Sandbox.SandboxMode.RECOMMENDED;

    @Parameter(key = "filter_sandbox_tests", group = "Sandbox", description = "Drop tests that require the sandbox")
    public static boolean FILTER_SANDBOX_TESTS = false;

    @Parameter(key = "virtual_fs", group = "Sandbox", description = "Usa a virtual file system for all File I/O operations")
    public static boolean VIRTUAL_FS = true;


    @Parameter(key = "virtual_net", group = "Sandbox", description = "Usa a virtual network for all TCP/UDP communications")
    public static boolean VIRTUAL_NET = true;

    @Parameter(key = "use_separate_classloader", group = "Sandbox", description = "Usa a separate classloader in the final test cases")
    public static boolean USE_SEPARATE_CLASSLOADER = true;


    // ---------------------------------------------------------------
    // Experimental


    @Deprecated
    @Parameter(key = "jee", description = "Support for JEE")
    public static boolean JEE = false;

    @Deprecated
    @Parameter(key = "handle_servlets", description = "Special treatment of JEE Servlets")
    public static boolean HANDLE_SERVLETS = false;

    @Parameter(key = "cluster_recursion", description = "The maximum level of recursion when calculating the dependencies in the test cluster")
    public static int CLUSTER_RECURSION = 10;

    @Parameter(key = "sort_calls", description = "Sort SUT methods by remaining coverage to bias search towards uncovered parts")
    public static boolean SORT_CALLS = false;

    @Parameter(key = "sort_objects", description = "Sort objects in a test to make calls on objects closer to SUT more likely")
    public static boolean SORT_OBJECTS = false;

    @Parameter(key = "inheritance_file", description = "Cached version of inheritance tree")
    public static String INHERITANCE_FILE = "";

    @Parameter(key = "branch_eval", description = "Jeremy's branch evaluation")
    public static boolean BRANCH_EVAL = false;

    @Parameter(key = "branch_statement", description = "Require statement coverage for branch coverage")
    public static boolean BRANCH_STATEMENT = false;

    @Parameter(key = "remote_testing", description = "Include remote calls")
    public static boolean REMOTE_TESTING = false;

    @Parameter(key = "cpu_timeout", description = "Measure timeouts on CPU time, not global time")
    public static boolean CPU_TIMEOUT = false;

    @Parameter(key = "log_timeout", description = "Produce output each time a test times out")
    public static boolean LOG_TIMEOUT = false;

    @Parameter(key = "call_probability", description = "Probability to reuse an existing test case, if it produces a required object")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double CALL_PROBABILITY = 0.0;

    @Parameter(key = "usage_models", description = "Names of usage model files")
    public static String USAGE_MODELS = "";

    @Parameter(key = "usage_rate", description = "Probability with which to use transitions out of the OUM")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double USAGE_RATE = 0.5;

    @Parameter(key = "instrumentation_skip_debug", description = "Skip debug information in bytecode instrumentation (needed for compatibility with classes transformed by Emma code instrumentation due to an ASM bug)")
    public static boolean INSTRUMENTATION_SKIP_DEBUG = false;

    @Parameter(key = "instrument_parent", description = "Also count coverage goals in superclasses")
    public static boolean INSTRUMENT_PARENT = false;

    @Parameter(key = "instrument_context", description = "Also instrument methods called from the SUT")
    public static boolean INSTRUMENT_CONTEXT = false;

    @Parameter(key = "instrument_method_calls", description = "Instrument methods calls")
    public static boolean INSTRUMENT_METHOD_CALLS = false;

    @Parameter(key = "instrument_libraries", description = "Instrument the libraries used by the project under test")
    public static boolean INSTRUMENT_LIBRARIES = false;

    @Parameter(key = "break_on_exception", description = "Stop test execution if exception occurrs")
    public static boolean BREAK_ON_EXCEPTION = true;

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

    @Parameter(key = "test_factory", description = "Which factory creates tests")
    public static TestFactory TEST_FACTORY = TestFactory.ARCHIVE;

    public enum ArchiveType {
        COVERAGE, MIO
    }

    /**
     * Constant <code>ARCHIVE_TYPE=COVERAGE</code>
     */
    @Parameter(key = "archive_type", description = "Which type of archive to keep track of covered goals during search")
    public static ArchiveType ARCHIVE_TYPE = ArchiveType.COVERAGE;

    @Parameter(key = "seed_file", description = "File storing TestGenerationResult or GeneticAlgorithm")
    public static String SEED_FILE = "";

    @Parameter(key = "seed_probability", description = "Probability to seed on methods with randomness involved")
    public static double SEED_PROBABILITY = 0.1;

    @Parameter(key = "selected_junit", description = "List of fully qualified class names (separated by ':') indicating which JUnit test suites the user has selected (e.g., for seeding)")
    public static String SELECTED_JUNIT = null;

    @Parameter(key = "junit_strict", description = "Only include test files containing the target classname")
    public static boolean JUNIT_STRICT = false;

    @Parameter(key = "seed_clone", description = "Probability with which existing individuals are cloned")
    @DoubleValue(min = 0.0, max = 1.0)
    public static double SEED_CLONE = 0.2;

    @Parameter(key = "seed_mutations", description = "Number of mutations applied to a cloned individual")
    public static int SEED_MUTATIONS = 3;

    @Parameter(key = "seed_dir", group = "Output", description = "Directory name where the best chromosomes are saved")
    public static String SEED_DIR = "evosuite-seeds";

    @Parameter(key = "concolic_mutation", description = "Deprecated. Probability of using concolic mutation operator")
    @DoubleValue(min = 0.0, max = 1.0)
    @Deprecated
    public static double CONCOLIC_MUTATION = 0.0;

    @Parameter(key = "constraint_solution_attempts", description = "Number of attempts to solve constraints related to one code branch")
    public static int CONSTRAINT_SOLUTION_ATTEMPTS = 3;

    @Parameter(key = "testability_transformation", description = "Apply testability transformation (Yanchuan)")
    public static boolean TESTABILITY_TRANSFORMATION = false;

    @Parameter(key = "TT_stack", description = "Maximum stack depth for testability transformation")
    public static int TT_stack = 10;

    @Parameter(key = "TT", description = "Testability transformation")
    public static boolean TT = false;

    public enum TransformationScope {
        TARGET, PREFIX, ALL
    }

    @Parameter(key = "tt_scope", description = "Testability transformation")
    public static TransformationScope TT_SCOPE = TransformationScope.ALL;

    // ---------------------------------------------------------------
    // Contracts / Asserts:
    @Parameter(key = "check_contracts", description = "Check contracts during test execution")
    public static boolean CHECK_CONTRACTS = false;

    @Parameter(key = "check_contracts_end", description = "Check contracts only once per test")
    public static boolean CHECK_CONTRACTS_END = false;

    @Parameter(key = "catch_undeclared_exceptions", description = "Use try/catch block for undeclared exceptions")
    public static boolean CATCH_UNDECLARED_EXCEPTIONS = true;

    @Parameter(key = "junit_theories", description = "Check JUnit theories as contracts")
    public static String JUNIT_THEORIES = "";


    @Parameter(key = "exception_branches", description = "Instrument code with explicit branches for exceptional control flow")
    public static boolean EXCEPTION_BRANCHES = false;

    @Parameter(key = "error_branches", description = "Instrument code with error checking branches")
    public static boolean ERROR_BRANCHES = false;

    public enum ErrorInstrumentation {
        ARRAY, CAST, DEQUE, DIVISIONBYZERO, LINKEDHASHSET, NPE, OVERFLOW, QUEUE, STACK, VECTOR, LIST
    }

    @Parameter(key = "error_instrumentation", description = "Which instrumentation to use for error checks")
    public static ErrorInstrumentation[] ERROR_INSTRUMENTATION = new ErrorInstrumentation[]{ErrorInstrumentation.ARRAY, ErrorInstrumentation.CAST, ErrorInstrumentation.DEQUE, ErrorInstrumentation.DIVISIONBYZERO, ErrorInstrumentation.LINKEDHASHSET, ErrorInstrumentation.NPE, ErrorInstrumentation.OVERFLOW, ErrorInstrumentation.QUEUE, ErrorInstrumentation.STACK, ErrorInstrumentation.VECTOR};

    @Parameter(key = "enable_asserts_for_evosuite", description = "When running EvoSuite clients, for debugging purposes check its assserts")
    public static boolean ENABLE_ASSERTS_FOR_EVOSUITE = false;

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

    @Parameter(key = "shutdown_timeout", group = "Test Execution", description = "Milliseconds grace time to shut down test cleanly")
    public static int SHUTDOWN_TIMEOUT = 1000;

    @Parameter(key = "mutation_timeouts", group = "Test Execution", description = "Number of timeouts before we consider a mutant killed")
    public static int MUTATION_TIMEOUTS = 3;

    @Parameter(key = "array_limit", group = "Test Execution", description = "Hard limit on array allocation in the code")
    public static int ARRAY_LIMIT = 1000000;

    @Parameter(key = "max_mutants", group = "Test Execution", description = "Maximum number of mutants to target at the same time")
    public static int MAX_MUTANTS = 100;

    @Parameter(key = "mutation_generations", group = "Test Execution", description = "Number of generations before changing the currently targeted mutants")
    public static int MUTATION_GENERATIONS = 10;

    @Parameter(key = "replace_calls", group = "Test Execution", description = "Replace nondeterministic calls and System.exit")
    public static boolean REPLACE_CALLS = true;

    @Parameter(key = "replace_system_in", group = "Test Execution", description = "Replace System.in with a smart stub/mock")
    public static boolean REPLACE_SYSTEM_IN = true;

    @Parameter(key = "replace_gui", group = "Test Execution", description = "Replace javax.swing with a smart stub/mock")
    public static boolean REPLACE_GUI = false;


    @Parameter(key = "max_started_threads", group = "Test Execution", description = "Max number of threads allowed to be started in each test")
    public static int MAX_STARTED_THREADS = RuntimeSettings.maxNumberOfThreads;

    @Parameter(key = "max_loop_iterations", group = "Test Execution", description = "Max number of iterations allowed per loop. A negative value means no check is done.")
    public static long MAX_LOOP_ITERATIONS = RuntimeSettings.maxNumberOfIterationsPerLoop;

    // ---------------------------------------------------------------
    // Debugging

    @Parameter(key = "debug", group = "Debugging", description = "Enables debugging support in the client VM")
    public static boolean DEBUG = false;

    @Parameter(key = "profile", group = "Debugging", description = "Enables profiler support in the client VM")
    public static String PROFILE = "";

    @Parameter(key = "port", group = "Debugging", description = "Port on localhost, to which the client VM will listen for a remote debugger; defaults to 1044")
    @IntValue(min = 1024, max = 65535)
    public static int PORT = 1044;

    @Parameter(key = "jmc", group = "Debugging", description = "Experimental: activate Flight Recorder in spawn client process for Java Mission Control")
    public static boolean JMC = false;


    // ---------------------------------------------------------------
    // TODO: Fix description
    public enum AlternativeFitnessCalculationMode {
        SUM, MIN, MAX, AVG, SINGLE
    }

    @Parameter(key = "alternative_fitness_calculation_mode", description = "")
    public static AlternativeFitnessCalculationMode ALTERNATIVE_FITNESS_CALCULATION_MODE = AlternativeFitnessCalculationMode.SUM;

    @Parameter(key = "starve_by_fitness", description = "")
    public static boolean STARVE_BY_FITNESS = true;

    @Parameter(key = "enable_alternative_fitness_calculation", description = "")
    public static boolean ENABLE_ALTERNATIVE_FITNESS_CALCULATION = false;

    @Parameter(key = "enable_alternative_suite_fitness", description = "")
    public static boolean ENABLE_ALTERNATIVE_SUITE_FITNESS = false;

    @Parameter(key = "defuse_debug_mode", description = "")
    public static boolean DEFUSE_DEBUG_MODE = false;

    @Parameter(key = "defuse_aliases", description = "")
    public static boolean DEFUSE_ALIASES = true;

    @Parameter(key = "randomize_difficulty", description = "")
    public static boolean RANDOMIZE_DIFFICULTY = true;

    // ---------------------------------------------------------------
    // UI Test generation parameters
    @Parameter(key = "UI_BACKGROUND_COVERAGE_DELAY", group = "EXSYST", description = "How often to write out coverage information in the background (in ms). -1 to disable.")
    public static int UI_BACKGROUND_COVERAGE_DELAY = -1;

    // ---------------------------------------------------------------
    // Runtime parameters

    public enum Criterion {
        EXCEPTION, DEFUSE, ALLDEFS, BRANCH, CBRANCH, STRONGMUTATION, WEAKMUTATION,
        MUTATION, STATEMENT, RHO, AMBIGUITY, IBRANCH, READABILITY,
        ONLYBRANCH, ONLYMUTATION, METHODTRACE, METHOD, METHODNOEXCEPTION, LINE, ONLYLINE, OUTPUT, INPUT,
        TRYCATCH
    }

    @Parameter(key = "criterion", group = "Runtime", description = "Coverage criterion. Can define more than one criterion by using a ':' separated list")
    public static Criterion[] CRITERION = new Criterion[]{
            //these are basic criteria that should be always on by default
            Criterion.LINE, Criterion.BRANCH, Criterion.EXCEPTION, Criterion.WEAKMUTATION, Criterion.OUTPUT, Criterion.METHOD, Criterion.METHODNOEXCEPTION, Criterion.CBRANCH};


    /**
     * Cache target class
     */
    private static Class<?> TARGET_CLASS_INSTANCE = null;

    @Parameter(key = "CP", group = "Runtime", description = "The classpath of the target classes")
    public static String CP = "";

    @Parameter(key = "CP_file_path", group = "Runtime", description = "Location of file where classpath is specified (in its first line). This is needed for operating systems like Windows where cannot have too long input parameters")
    public static String CP_FILE_PATH = null;


    @Parameter(key = "PROJECT_PREFIX", group = "Runtime", description = "Package name of target package")
    public static String PROJECT_PREFIX = "";

    @Parameter(key = "PROJECT_DIR", group = "Runtime", description = "Directory name of target package")
    public static String PROJECT_DIR = null;

    /**
     * Package name of target class (might be a subpackage)
     */
    public static String CLASS_PREFIX = "";

    /**
     * Sub-package name of target class
     */
    public static String SUB_PREFIX = "";

    @Parameter(key = "TARGET_CLASS_PREFIX", group = "Runtime", description = "Prefix of classes we are trying to cover")
    public static String TARGET_CLASS_PREFIX = "";

    /**
     * Class under test
     */
    @Parameter(key = "TARGET_CLASS", group = "Runtime", description = "Class under test")
    public static String TARGET_CLASS = "";

    /**
     * Method under test
     */
    @Parameter(key = "target_method", group = "Runtime", description = "Method for which to generate tests")
    public static String TARGET_METHOD = "";

    /**
     * Method under test
     */
    @Parameter(key = "target_method_prefix", group = "Runtime", description = "All methods matching prefix will be used for generating tests")
    public static String TARGET_METHOD_PREFIX = "";

    /**
     * Method under test
     */
    @Parameter(key = "target_method_list", group = "Runtime", description = "A colon(:) separated list of methods for which to generate tests")
    public static String TARGET_METHOD_LIST = "";

    @Parameter(key = "hierarchy_data", group = "Runtime", description = "File in which hierarchy data is stored")
    public static String HIERARCHY_DATA = "hierarchy.xml";

    @Parameter(key = "connection_data", group = "Runtime", description = "File in which connection data is stored")
    public static String CONNECTION_DATA = "connection.xml";

    @Parameter(key = "exclude_ibranches_cut", group = "Runtime", description = "Exclude ibranches in the cut, to speed up ibranch as secondary criterion")
    public static boolean EXCLUDE_IBRANCHES_CUT = false;

    public enum Strategy {
        ONEBRANCH, EVOSUITE, RANDOM, RANDOM_FIXED, ENTBUG, MOSUITE, DSE, NOVELTY, MAP_ELITES
    }

    @Parameter(key = "strategy", group = "Runtime", description = "Which mode to use")
    public static Strategy STRATEGY = Strategy.MOSUITE;

    @Parameter(key = "process_communication_port", group = "Runtime", description = "Port at which the communication with the external process is done")
    public static int PROCESS_COMMUNICATION_PORT = -1;

    @Parameter(key = "spawn_process_manager_port", group = "Runtime", description = "Port at which the spawn process manager (if any) is listening")
    public static Integer SPAWN_PROCESS_MANAGER_PORT = null;

    @Parameter(key = "stopping_port", group = "Runtime", description = "Port at which a stopping condition waits for interruption")
    public static int STOPPING_PORT = -1;

    @Parameter(key = "max_stalled_threads", group = "Runtime", description = "Number of stalled threads")
    public static int MAX_STALLED_THREADS = 10;

    @Parameter(key = "ignore_threads", group = "Runtime", description = "Do not attempt to kill threads matching this prefix")
    public static String[] IGNORE_THREADS = new String[]{};

    @Parameter(key = "min_free_mem", group = "Runtime", description = "Minimum amount of available memory")
    public static int MIN_FREE_MEM = 50 * 1000 * 1000;


    @Parameter(key = "client_on_thread", group = "Runtime", description = "Run client process on same JVM of master in separate thread. To be used only for debugging purposes")
    public static volatile boolean CLIENT_ON_THREAD = false;


    @Parameter(key = "is_running_a_system_test", group = "Runtime", description = "Specify that a system test is running. To be used only for debugging purposes")
    public static volatile boolean IS_RUNNING_A_SYSTEM_TEST = false;


    // ---------------------------------------------------------------
    // Seeding test cases

    @Parameter(key = "classpath", group = "Test Seeding", description = "The classpath needed to compile the seeding test case.")
    public static String[] CLASSPATH = new String[]{""};

    @Parameter(key = "sourcepath", group = "Test Seeding", description = "The path to the test case source.")
    public static String[] SOURCEPATH = new String[]{""};

    // ---------------------------------------------------------------
    // Eclipse Plug-in flag

    @Parameter(key = "eclipse_plugin", group = "Plugin", description = "Running plugin for experiments. Use EvoSuiteTest annotation and decorate generated tests with (checked = false).")
    public static boolean ECLIPSE_PLUGIN = false;

    // Added - fix for @NotNull annotations issue on evo mailing list

    @Parameter(key = "honour_data_annotations", group = "Runtime", description = "Allows EvoSuite to generate tests with or without honouring the parameter data annotations")
    public static boolean HONOUR_DATA_ANNOTATIONS = true;

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
    private void initializeProperties() throws IllegalStateException {
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
     * @param propertiesPath a {@link java.lang.String} object.
     */
    public void loadProperties(String propertiesPath, boolean silent) {
        loadPropertiesFile(propertiesPath, silent);
        initializeProperties();
    }

    /**
     * Load a properties file
     *
     * @param propertiesPath a {@link java.lang.String} object.
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

    /**
     * All fields representing values, inserted via reflection
     */
    private static final Map<String, Field> parameterMap = new HashMap<>();

    /**
     * All fields representing values, inserted via reflection
     */
    private static final Map<Field, Object> defaultMap = new HashMap<>();

    static {
        // need to do it once, to capture all the default values
        reflectMap();
    }

    /**
     * Keep track of which fields have been changed from their defaults during
     * loading
     */
    private static final Set<String> changedFields = new HashSet<>();

    /**
     * Get class of parameter
     *
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.Class} object.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
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
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
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
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
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
     * @param key a {@link java.lang.String} object.
     * @return a {@link org.evosuite.Properties.IntValue} object.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
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
     * @param key a {@link java.lang.String} object.
     * @return a {@link org.evosuite.Properties.LongValue} object.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
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
     * @param key a {@link java.lang.String} object.
     * @return a {@link org.evosuite.Properties.DoubleValue} object.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
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
     * @param key a {@link java.lang.String} object.
     * @return a int.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
     * @throws java.lang.IllegalArgumentException               if any.
     * @throws java.lang.IllegalAccessException                 if any.
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
     * @param key a {@link java.lang.String} object.
     * @return a long.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
     * @throws java.lang.IllegalArgumentException               if any.
     * @throws java.lang.IllegalAccessException                 if any.
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
     * @param key a {@link java.lang.String} object.
     * @return a boolean.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
     * @throws java.lang.IllegalArgumentException               if any.
     * @throws java.lang.IllegalAccessException                 if any.
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
     * @param key a {@link java.lang.String} object.
     * @return a double.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
     * @throws java.lang.IllegalArgumentException               if any.
     * @throws java.lang.IllegalAccessException                 if any.
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
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
     * @throws java.lang.IllegalArgumentException               if any.
     * @throws java.lang.IllegalAccessException                 if any.
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
     * @param key   a {@link java.lang.String} object.
     * @param value a int.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
     * @throws java.lang.IllegalAccessException                 if any.
     * @throws java.lang.IllegalArgumentException               if any.
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
     * @param key   a {@link java.lang.String} object.
     * @param value a long.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
     * @throws java.lang.IllegalAccessException                 if any.
     * @throws java.lang.IllegalArgumentException               if any.
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
     * @param key   a {@link java.lang.String} object.
     * @param value a boolean.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
     * @throws java.lang.IllegalAccessException                 if any.
     * @throws java.lang.IllegalArgumentException               if any.
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
     * @param key   a {@link java.lang.String} object.
     * @param value a double.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
     * @throws java.lang.IllegalArgumentException               if any.
     * @throws java.lang.IllegalAccessException                 if any.
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
     * @param key   a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
     * @throws java.lang.IllegalArgumentException               if any.
     * @throws java.lang.IllegalAccessException                 if any.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
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
     * @param key   a {@link java.lang.String} object.
     * @param value an array of {@link java.lang.String} objects.
     * @throws org.evosuite.Properties.NoSuchParameterException if any.
     * @throws java.lang.IllegalArgumentException               if any.
     * @throws java.lang.IllegalAccessException                 if any.
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

    /**
     * Singleton instance
     */
    private static Properties instance = null; // new Properties(true, true);

    /**
     * Internal properties hashmap
     */
    private java.util.Properties properties;

    /**
     * Constructor
     */
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

    /**
     * Returns the target class. It required, it also executes the
     * <clinit> class initialiser of the target class
     *
     * @return the initialised target class
     */
    public static Class<?> getInitializedTargetClass() {
        return getTargetClass(true);
    }

    /**
     * Returns the target class. If the class is not yet initialised,
     * this method *does not* execute the <clinit> class initialiser of the target class.
     * This method explicitly states that the <clinit> method is not executed
     * because of this method.
     *
     * @return the target class. The target class could be uninitialised
     */
    public static Class<?> getTargetClassAndDontInitialise() {
        return getTargetClass(false);
    }


    /**
     * Returns true if there is a loaded target class object.
     * Warning: resetTargetClass() does not load the class, only
     * discards the previous target class object.
     *
     * @return
     */
    public static boolean hasTargetClassBeenLoaded() {
        return TARGET_CLASS_INSTANCE != null;
    }

    /**
     * Get class object of class under test
     *
     * @return a {@link java.lang.Class} object.
     */
    private static Class<?> getTargetClass(boolean initialise) {

        if (TARGET_CLASS_INSTANCE != null
                && TARGET_CLASS_INSTANCE.getCanonicalName()
                .equals(TARGET_CLASS))
            return TARGET_CLASS_INSTANCE;

        if (TARGET_CLASS_INSTANCE != null) {
            TARGET_CLASS_INSTANCE = null;
        }

        boolean wasLoopCheckOn = LoopCounter.getInstance().isActivated();

        try {
            /*
             * TODO: loading the SUT will execute its static initializer.
             * This might interact with the environment (eg, read a file, access static
             * variables of other classes), and even fails if an exception is thrown.
             * Those cases should be handled here before starting the search.
             */

            Runtime.getInstance().resetRuntime(); //it is important to initialize the VFS


            LoopCounter.getInstance().setActive(false);
            TARGET_CLASS_INSTANCE = Class.forName(TARGET_CLASS, initialise,
                    TestGenerationContext.getInstance().getClassLoaderForSUT());

            setClassPrefix();

        } catch (ClassNotFoundException e) {
            LoggingUtils.getEvoLogger().warn(
                    "* Could not find class under test " + Properties.TARGET_CLASS + ": " + e);
        } finally {
            LoopCounter.getInstance().setActive(wasLoopCheckOn);
        }

        return TARGET_CLASS_INSTANCE;
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
     * @param fileName a {@link java.lang.String} object.
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

        Map<String, Set<Parameter>> fieldMap = new HashMap<>();
        for (Field f : Properties.class.getFields()) {
            if (f.isAnnotationPresent(Parameter.class)) {
                Parameter p = f.getAnnotation(Parameter.class);
                if (!fieldMap.containsKey(p.group()))
                    fieldMap.put(p.group(), new HashSet<>());

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
        FileIOUtils.writeFile(buffer.toString(), fileName);
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

    /**
     * Checks whether the current generation strategy is DSE.
     *
     * @return a boolean value.
     */
    public static boolean isDSEStrategySelected() {
        return STRATEGY == Strategy.DSE;
    }

    /**
     * Checks whether DSE is enabled in Local Search.
     *
     * @return a boolean value.
     */
    public static boolean isDSEEnabledInLocalSearch() {
        return DSE_PROBABILITY > 0.0
                && LOCAL_SEARCH_RATE > 0
                && LOCAL_SEARCH_PROBABILITY > 0.0;
    }

    /**
     * Checks wheter the selected arrays implementation for DSE is arrays theory.
     *
     * @return a boolean value
     */
    public static boolean isArraysTheoryImplementationSelected() {
        return SELECTED_DSE_ARRAYS_MEMORY_MODEL_VERSION == DSE_ARRAYS_MEMORY_MODEL_VERSION.SELECT_STORE_EXPRESSIONS;
    }

    /**
     * Checks wheter the selected arrays implementation for DSE is lazy arrays.
     *
     * @return a boolean value.
     */
    public static boolean isLazyArraysImplementationSelected() {
        return SELECTED_DSE_ARRAYS_MEMORY_MODEL_VERSION == DSE_ARRAYS_MEMORY_MODEL_VERSION.LAZY_VARIABLES;
    }
}
