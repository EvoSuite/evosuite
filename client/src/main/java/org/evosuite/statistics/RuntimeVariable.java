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
package org.evosuite.statistics;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * <p>
 * This enumeration defines all the runtime variables we want to store in
 * the CSV files.
 * A runtime variable is either an output of the search (e.g., obtained branch coverage)
 * or something that can only be determined once the CUT is analyzed (e.g., the number of branches)
 *
 * <p>
 * Note, it is perfectly fine to add new runtime variables in this enum, in any position.
 * But it is essential to provide JavaDoc <b>descriptions</b> for each new variable
 *
 * <p>
 * WARNING: do not change the name of any variable! If you do, current R
 * scripts will break. If you really need to change a name, please first
 * contact Andrea Arcuri.
 *
 * @author arcuri
 */
public enum RuntimeVariable {

    /**
     * Number of predicates in CUT
     */
    Predicates,
    /**
     * Number of added jump conditions through instrumentation
     */
    Instrumented_Predicates,
    /**
     * Number of classes in classpath
     */
    Classpath_Classes,
    /**
     * Number of classes analyzed for test cluster
     */
    Analyzed_Classes,
    /**
     * Total number of generators
     */
    Generators,
    /**
     * Total number of modifiers
     */
    Modifiers,
    /**
     * Total number of branches in CUT
     */
    Total_Branches,
    /**
     * Number of covered branches in CUT
     */
    Covered_Branches,
    /**
     * Total number of gradient branches
     */
    Gradient_Branches,
    /**
     * Total number of covered gradient branches
     */
    Gradient_Branches_Covered,
    /**
     * The number of lines in the CUT
     */
    Lines,
    /**
     * The actual covered line numbers
     */
    Covered_Lines,
    /**
     * Total number of methods in CUT
     */
    Total_Methods,
    /**
     * Number of methods covered
     */
    Covered_Methods,
    /**
     * Number of methods without any predicates
     */
    Branchless_Methods,
    /**
     * Number of methods without predicates covered
     */
    Covered_Branchless_Methods,
    /**
     * Total number of coverage goals for current criterion
     */
    Total_Goals,
    /**
     * Total number of covered goals
     */
    Covered_Goals,
    /**
     * Number of mutants
     */
    Mutants,
    /**
     * Total number of statements executed
     */
    Statements_Executed,
    /**
     * The total number of tests executed during the search
     */
    Tests_Executed,
    /**
     * The total number of fitness evaluations during the search
     */
    Fitness_Evaluations,
    /**
     * Number of generations the search algorithm has been evolving
     */
    Generations,
    /**
     * Obtained coverage of the chosen testing criterion
     */
    Coverage,
    /**
     * A bit string (0/1) representing whether goals (in order) are covered
     */
    CoverageBitString,
    /**
     * Fitness value of the best individual
     */
    Fitness,
    /**
     * Obtained coverage (of the chosen testing criterion) at different points in time
     */
    CoverageTimeline,
    /**
     * Obtained fitness values at different points in time
     */
    FitnessTimeline,
    /**
     * Population similarity values at different points in time
     */
    DiversityTimeline,
    /**
     * Obtained size values at different points in time
     */
    Size_T0,
    SizeTimeline,
    /**
     * Obtained length values at different points in time
     */
    LengthTimeline,
    /**
     * The obtained statement coverage
     */
    StatementCoverage,
    /**
     * A bit string (0/1) representing whether statements (in order) are covered
     */
    StatementCoverageBitString,
    /**
     * The obtained rho coverage
     */
    RhoScore,
    RhoScore_T0,
    RhoScoreTimeline,
    /**
     * The obtained ambiguity coverage
     */
    AmbiguityScore,
    AmbiguityScore_T0,
    AmbiguityScoreTimeline,
    /**
     * Not only the covered branches ratio, but also including the branchless methods. FIXME: this will need to be changed
     */
    BranchCoverage,
    /**
     * Coverage of instrumented branches
     */
    Total_Branches_Real,
    Total_Branches_Instrumented,
    Covered_Branches_Real,
    Covered_Branches_Instrumented,
    TryCatchCoverage,
    BranchCoverageTimeline,
    /**
     * A bit string (0/1) representing whether branches (in order) are covered
     */
    BranchCoverageBitString,
    /**
     * Only the covered branches ratio.
     */
    OnlyBranchCoverage,
    OnlyBranchFitnessTimeline,
    OnlyBranchCoverageTimeline,
    OnlyBranchCoverageBitString,
    CBranchCoverage,
    CBranchFitnessTimeline,
    CBranchCoverageTimeline,
    CBranchCoverageBitString,
    IBranchCoverage,
    IBranchInitialGoals,
    IBranchInitialGoalsInTargetClass,
    IBranchGoalsTimeline,
    IBranchCoverageBitString,
    /**
     * The obtained method coverage (method calls anywhere in trace)
     */
    MethodTraceCoverage,
    MethodTraceFitnessTimeline,
    MethodTraceCoverageTimeline,
    MethodTraceCoverageBitString,
    /**
     * The obtained method coverage
     */
    MethodCoverage,
    MethodFitnessTimeline,
    MethodCoverageTimeline,
    MethodCoverageBitString,
    /**
     * The obtained method coverage (only normal behaviour)
     */
    MethodNoExceptionCoverage,
    MethodNoExceptionFitnessTimeline,
    MethodNoExceptionCoverageTimeline,
    MethodNoExceptionCoverageBitString,
    /**
     * The obtained line coverage
     */
    LineCoverage,
    LineFitnessTimeline,
    LineCoverageTimeline,
    LineCoverageBitString,
    /**
     * The obtained output value coverage
     */
    OutputCoverage,
    OutputFitnessTimeline,
    OutputCoverageTimeline,
    OutputCoverageBitString,
    /**
     * The input value coverage
     */
    InputCoverage,
    InputFitnessTimeline,
    InputCoverageTimeline,
    InputCoverageBitString,
    /**
     * The obtained exception coverage
     */
    ExceptionCoverage,
    ExceptionFitnessTimeline,
    ExceptionCoverageTimeline,
    ExceptionCoverageBitString,
    /**
     * The obtained score for weak mutation testing
     */
    WeakMutationScore,
    WeakMutationCoverageTimeline,
    WeakMutationCoverageBitString,
    /**
     * Only mutation = only infection distance
     */
    OnlyMutationScore,
    OnlyMutationFitnessTimeline,
    OnlyMutationCoverageTimeline,
    OnlyMutationCoverageBitString,
    /**
     * The obtained score for (strong) mutation testing
     */
    MutationScore,
    MutationCoverageBitString,
    /**
     * The total time EvoSuite spent generating the test cases
     */
    Total_Time,
    /**
     * Number of tests in resulting test suite
     */
    Size,
    /**
     * Total number of statements in final test suite
     */
    Length,
    /**
     * Number of tests in resulting test suite before minimization
     */
    Result_Size,
    /**
     * Total number of statements in final test suite before minimization
     */
    Result_Length,
    /**
     * Either use {@link RuntimeVariable#Size}
     */
    @Deprecated
    Minimized_Size,
    /**
     * Either use  {@link RuntimeVariable#Length}
     */
    @Deprecated
    Minimized_Length,
    /**
     * The random seed used during the search. A random one was used if none was specified at the beginning
     */
    Random_Seed,
    /**
     * How many tests were carved, ie used as input seeds for the search
     */
    CarvedTests,
    /**
     * The branch coverage of the carved tests
     */
    CarvedCoverage,
    /**
     * Was any test unstable in the generated JUnit files?
     */
    HadUnstableTests,
    /**
     * Number of unstable tests in the generated JUnit files
     */
    NumUnstableTests,
    /**
     * An estimate (ie not precise) of the maximum number of threads running at the same time in the CUT
     */
    Threads,
    /**
     * Number of top-level methods throwing an undeclared exception explicitly with a 'throw new'
     */
    Explicit_MethodExceptions,
    /**
     * Number of undeclared exception types that were explicitly thrown with a 'throw new' at least once
     */
    Explicit_TypeExceptions,
    /**
     * Number of top-level methods throwing an undeclared exception implicitly (ie, no 'new throw')
     */
    Implicit_MethodExceptions,
    /**
     * Number of undeclared exception types that were implicitly thrown (ie, no 'new throw') at least once
     */
    Implicit_TypeExceptions,
    /**
     * Total number of exceptions covered
     */
    TotalExceptionsTimeline,

    /**
     * Map Elites
     */
    DensityTimeline,
    FeaturePartitionCount,
    FeatureCount,
    FeaturesFound,

    /* ----- number of unique permissions that were denied for each kind --- */
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
    /* -------------------------------------------------------------------- */
    /**
     * Timings
     */
    Time_Assertion,
    Time_Coverage,
    Time_StateDistance,
    Time_Diversity,
    /* -------------------------------------------------------------------- */
    /**
     * Count of branch comparison types in bytecode (static)
     */
    Cmp_IntZero,
    Cmp_IntInt,
    Cmp_RefNull,
    Cmp_RefRef,
    /**
     * Count of branch comparisons reached (dynamic)
     */
    Reached_IntZero,
    Reached_IntInt,
    Reached_RefNull,
    Reached_RefRef,
    /**
     * Count of branch comparisons covered (dynamic)
     */
    Covered_IntZero,
    Covered_IntInt,
    Covered_RefNull,
    Covered_RefRef,
    /**
     * Count of bytecode instructions (static)
     */
    BC_lcmp,
    BC_fcmpl,
    BC_fcmpg,
    BC_dcmpl,
    BC_dcmpg,
    /**
     * Count of bytecode instructions reached (dynamic)
     */
    Reached_lcmp,
    Reached_fcmpl,
    Reached_fcmpg,
    Reached_dcmpl,
    Reached_dcmpg,
    /**
     * Count of bytecode instructions reached (dynamic)
     */
    Covered_lcmp,
    Covered_fcmpl,
    Covered_fcmpg,
    Covered_dcmpl,
    Covered_dcmpg,
    /**
     * For sanity-checking purposes
     */
    RSM_OverMinimized,
    /* -------------------------------------------------------------------- */
    /* TODO following needs to be implemented/updated. Currently they are not (necessarily) supported */
    /**
     * (FIXME: need to be implemented) The number of serialized objects that EvoSuite is going to use for seeding strategies
     */
    NumberOfInputPoolObjects,
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
    /* --- Dataflow stuff. FIXME: Is this stuff still valid? --- */
    AllDefCoverage,
    AllDefCoverageBitString,
    DefUseCoverage,
    DefUseCoverageBitString,
    Definitions,
    Uses,
    DefUsePairs,
    IntraMethodPairs,
    InterMethodPairs,
    IntraClassPairs,
    ParameterPairs,
    LCSAJs,
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
    /* -------------------------------------------------------------------- */
    /**
     * The number of constraint made of integer constraints and no other type
     */
    IntegerOnlyConstraints,
    /**
     * The number of constraint made of real constraints and no other type
     */
    RealOnlyConstraints,
    /**
     * The number of constraint made of real constraints and no other type
     */
    StringOnlyConstraints,
    /**
     * The number of constraint made of integer and real constraints
     */
    IntegerAndRealConstraints,
    /**
     * The number of constraint made of integer and string constraints
     */
    IntegerAndStringConstraints,
    /**
     * The number of constraint made of real and string constraints
     */
    RealAndStringConstraints,
    /**
     * Number of constraints containing all three types altogether
     */
    IntegerRealAndStringConstraints,
    /** The total number of constraints during the execution of the Genetic Algorithm*/
    /**
     * This total should be the sum of all the other types of constraints
     */
    TotalNumberOfConstraints,

    /* -------------------------------------------------------------------- */
    /**
     * The number of SAT answers to Solver queries
     */
    NumberOfSATQueries,
    /**
     * The number of UNSAT answers to Solver queries
     */
    NumberOfUNSATQueries,
    /**
     * The number of TIMEOUTs when solving queries
     */
    NumberOfTimeoutQueries,
    /**
     * How many SAT queries led to Useful (i.e. better fitness) new tests
     */
    NumberOfUsefulNewTests,
    /**
     * How many SAT queries led to Unuseful (i.e. no better fitness) new tests
     */
    NumberOfUnusefulNewTests,
    /**
     * How much time was spent solving constraints on the SMT solver
     */
    TotalTimeSpentSolvingConstraints,

    /* -------------------------------------------------------------------- */
    /** Search budget needed to reach the maximum coverage */
    /**
     * Used in the comparison between LISP and MOSA
     */
    Time2MaxCoverage,

    /* -------------------------------------------------------------------- */
    /******* DSE related section *******/

    /**
     * Path condition related
     */
    MaxPathConditionLength,
    MinPathConditionLength,
    AvgPathConditionLength,

    /**
     * Path explotarion related
     */
    NumberOfPathsExplored,
    NumberOfPathsDiverged,

    /**
     * How much time was spent executing tests
     */
    TotalTimeSpentExecutingConcolicaly,
    TotalTimeSpentExecutingTestCases,
    TotalTimeSpentExecutingNonConcolicTestCases,

    /**
     * Solver Cache Statistics
     */
    QueryCacheSize,
    QueryCacheCalls,
    QueryCacheHitRate;

    /* -------------------------------------------------- */

    private static final Logger logger = LoggerFactory.getLogger(RuntimeVariable.class);

    /**
     * check if the variables do satisfy a set of predefined constraints: eg, the
     * number of covered targets cannot be higher than their total number
     *
     * @param map from (key->variable name) to (value -> output variable)
     * @return
     */
    public static boolean validateRuntimeVariables(Map<String, OutputVariable<?>> map) {
        if (!Properties.VALIDATE_RUNTIME_VARIABLES) {
            logger.warn("Not validating runtime variables");
            return true;
        }
        boolean valid = true;

        try {
            Integer totalBranches = getIntegerValue(map, Total_Branches);
            Integer coveredBranches = getIntegerValue(map, Covered_Branches);

            if (coveredBranches != null && totalBranches != null && coveredBranches > totalBranches) {
                logger.error("Obtained invalid branch count: covered " + coveredBranches + " out of " + totalBranches);
                valid = false;
            }

            Integer totalGoals = getIntegerValue(map, Total_Goals);
            Integer coveredGoals = getIntegerValue(map, Covered_Goals);

            if (coveredGoals != null && totalGoals != null && coveredGoals > totalGoals) {
                logger.error("Obtained invalid goal count: covered " + coveredGoals + " out of " + totalGoals);
                valid = false;
            }

            Integer totalMethods = getIntegerValue(map, Total_Methods);
            Integer coveredMethods = getIntegerValue(map, Covered_Methods);

            if (coveredMethods != null && totalMethods != null && coveredMethods > totalMethods) {
                logger.error("Obtained invalid method count: covered " + coveredMethods + " out of " + totalMethods);
                valid = false;
            }

            String[] criteria = null;
            if (map.containsKey("criterion")) {
                criteria = map.get("criterion").toString().split(":");
            }

            Double coverage = getDoubleValue(map, Coverage);
            Double branchCoverage = getDoubleValue(map, BranchCoverage);

            if (criteria != null && criteria.length == 1 && criteria[0].equalsIgnoreCase(Criterion.BRANCH.toString())
                    && coverage != null && branchCoverage != null) {

                double diff = Math.abs(coverage - branchCoverage);
                if (diff > 0.001) {
                    logger.error("Targeting branch coverage, but Coverage is different " +
                            "from BranchCoverage: " + coverage + " != " + branchCoverage);
                    valid = false;
                }
            }


            /*
             * TODO there are more things we could check here
             */

        } catch (Exception e) {
            logger.error("Exception while validating runtime variables: " + e.getMessage(), e);
            valid = false;
        }

        return valid;
    }

    private static Integer getIntegerValue(Map<String, OutputVariable<?>> map, RuntimeVariable variable) {
        OutputVariable<?> out = map.get(variable.toString());
        if (out != null) {
            return (Integer) out.getValue();
        } else {
            return null;
        }
    }

    private static Double getDoubleValue(Map<String, OutputVariable<?>> map, RuntimeVariable variable) {
        OutputVariable<?> out = map.get(variable.toString());
        if (out != null) {
            return (Double) out.getValue();
        } else {
            return null;
        }
    }
}
