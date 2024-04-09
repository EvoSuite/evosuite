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
package org.evosuite.testsuite.fuzzsearch;

import org.evosuite.Properties;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.line.LineCoverageTestFitness;
import org.evosuite.ga.archive.Archive;
import org.evosuite.ga.localsearch.LocalSearch;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.seeding.ConstantPool;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.testcase.*;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.*;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.*;

public class TestSuiteFuzzSearch implements LocalSearch<TestSuiteChromosome> {

    private static final Logger logger = LoggerFactory.getLogger(TestSuiteFuzzSearch.class);

    private static final TestSuiteFuzzSearch instance = new TestSuiteFuzzSearch();

    private Set<TestFitnessFunction> fuzzGoals = null;

    private TestSuiteChromosome solution = null;
    private List<Integer> fuzzIndices = null;

    private long lastTemplateSize = 0;
    private int templateFuzzingCalls = 0;
    private long totalMutationCounts = 0;

    public TestSuiteFuzzSearch() {
        super();
    }

    public static TestSuiteFuzzSearch getInstance() {
        return instance;
    }

    public boolean prepare(Set<TestFitnessFunction> uncoveredTargets) {
        solution = getSolutionSuite();
        if(solution == null) {
            LoggingUtils.getEvoLogger().info("* Cannot start fuzzing - no solutions");
            return false;
        }

        fuzzIndices = getIndicesWithMutableParameters(solution);
        if(fuzzIndices == null) {
            Archive.getArchiveInstance().setMinimizedSuite(solution); // Todo: consider all fitness solution
            LoggingUtils.getEvoLogger().info("* Cannot start fuzzing " +
                    "- no templates (# of tests in solution: " + solution.size() + " )");
            return false;
        }

        //set all fitness goals
        Set<Class<?>> fitnessClasses = new HashSet<>();
        for (Properties.Criterion c : Properties.CRITERION) {
            fitnessClasses.add(FitnessFunctions.getTestFitnessFunctionClass(c));
        }
        fuzzGoals = new HashSet<>(uncoveredTargets.stream().filter(g -> (fitnessClasses.contains(g.getClass()))).collect(Collectors.toSet()));
        return true;
    }

    public void search() {
        switch(Properties.FUZZ_STRATEGY) {
            case COVERAGE:
//                coverageGuidedFuzz(fuzzTemplates);
            case UNIFORM_COVERAGE:
            default:
                uniformCoverageGuidedFuzz(solution, fuzzIndices);
        }
    }

    public void finish() {
        Archive.getArchiveInstance().setMinimizedSuite(solution);
    }

    public TestSuiteChromosome getSolutionSuite() {
        TestSuiteChromosome solution = new TestSuiteChromosome();
        solution.addTestChromosomes(Archive.getArchiveInstance().getSolutions());

        //inline tests
        ConstantInliner inliner = new ConstantInliner();
        inliner.inline(solution);

        //minimize test suite regarding fuzz criterion
        if(Properties.FUZZ_MINIMIZATION) {
            TestSuiteMinimizer minimizer = new FuzzTestSuiteMinimizer(TestSuiteGenerator.getFitnessFactories(Properties.CRITERION));
            minimizer.minimize(solution, true);
            if (solution.size() == 0) {
                return null;
            }
        }
        return solution;
    }

    public List<Integer> getIndicesWithMutableParameters(TestSuiteChromosome solution){
        List<Integer> fuzzIndices = new ArrayList<>();
        for(int idx = 0; idx<solution.size(); ++idx) {
            TestCase tc = solution.getTestChromosome(idx).getTestCase();
            int numParameters = countMutableParameters(tc);
            if(numParameters > 0) {
                tc.setRandomizableParamters(numParameters);
                fuzzIndices.add(idx);
            }
        }
        if(fuzzIndices.size() == 0) {
            return null;
        }
        return fuzzIndices;
    }

//    private long computeChildrenCount(double totalFitness, double thisFitness) {
//        return (long) (Properties.FUZZ_MUTATAIONS * thisFitness / totalFitness);
//    }

//    private void coverageGuidedFuzz(TestSuiteChromosome suite) {
//        //fuzz each of templates
//        for(int i = 0; i<suite.size(); ++i) {
//            TestChromosome test = suite.getTestChromosome(i).clone();
//            if (!mutateParameters(test.getTestCase())) {
//                continue;
//            }
//
//            //execute and add the test if it achieves new coverage
//            ExecutionResult result = TestCaseExecutor.runTest(test.getTestCase());
//            if (result == null || result.hasTimeout() || result.hasTestException() ||
//                    result.getTrace().getCoveredLines().size() == 0) {
//                continue;
//            }
//            test.setLastExecutionResult(result);
//            test.setChanged(false);
//
//            boolean newCoverage = false;
//            for (Iterator<TestFitnessFunction> it = fuzzGoals.iterator(); it.hasNext(); ) {
//                TestFitnessFunction uncoveredGoal = it.next();
//                if (uncoveredGoal.getFitness(test) == 0) { //remove covered goals, update archive in addition to fitness calc.
//                    newCoverage = true;
//                    LoggingUtils.getEvoLogger().info("* Fuzzing achieves new coverage: " + uncoveredGoal);
//                    it.remove();
//                }
//            }
//            if (newCoverage) {
//                LoggingUtils.getEvoLogger().info("* Test added: " + test);
//                suite.addTestChromosome(test);
//            }
//        }
//        numLastTemplates = suite.size();
//        ++numTemplateFuzzingCalls;
//        numTestExecutions += suite.size();
//    }

    private int getNumMutationsFromUniformDistribution(boolean isNewTest) {
        if(!isNewTest) {
            return Properties.FUZZ_NUM_MUTATIONS;
        }else{
            return Properties.FUZZ_NUM_MUTATIONS * (templateFuzzingCalls + 1);
        }
    }

    private void uniformCoverageGuidedFuzz(TestSuiteChromosome suite, List<Integer> templateIndices) {
        //fuzz each of templates
        int lastSuiteSize = suite.size();
        for(int i=0; i<templateIndices.size(); ++i) {
            int templateIdx = templateIndices.get(i);
            TestChromosome test = suite.getTestChromosome(templateIdx).clone();
            int numMutations = getNumMutationsFromUniformDistribution(templateIdx >= lastSuiteSize);
            this.totalMutationCounts += numMutations;
            while(--numMutations >= 0) {
                if (!mutateParameters(test.getTestCase())) {
                    continue;
                }

                //execute and add the test if it achieves new coverage
                ExecutionResult result = TestCaseExecutor.runTest(test.getTestCase());
                if (result == null || result.hasTimeout() || result.hasTestException() ||
                        result.getTrace().getCoveredLines().size() == 0) {
                    continue;
                }
                test.setLastExecutionResult(result);
                test.setChanged(false);

                boolean newCoverage = false;
                boolean newCodeOrMutation = false;
                for (Iterator<TestFitnessFunction> it = fuzzGoals.iterator(); it.hasNext(); ) {
                    TestFitnessFunction uncoveredGoal = it.next();
                    if (uncoveredGoal.getFitness(test) == 0) { //remove covered goals, update archive in addition to fitness calc.
                        newCoverage = true;
                        newCodeOrMutation = (uncoveredGoal instanceof BranchCoverageTestFitness)
                                || (uncoveredGoal instanceof LineCoverageTestFitness);
                        it.remove();
                    }
                }
                if (newCoverage) {
                    LoggingUtils.getEvoLogger().info("* new test added " + (newCodeOrMutation ? "(code coverage)" : "(etc coverage)"));
                    suite.addTestChromosome(test);
                    templateIndices.add(suite.size() - 1);
                }
            }
        }
        lastTemplateSize = templateIndices.size();
        ++templateFuzzingCalls;
    }

    public long getLastTemplateSize() {
        return lastTemplateSize;
    }

    public long getTemplateFuzzingCalls() {
        return templateFuzzingCalls;
    }

    public long getTotalMutationCounts() {
        return totalMutationCounts;
    }

    private static int sampleGeometric(double mean) {
        double p = 1 / mean;
        double uniform = Randomness.nextDouble();
        return (int) ceil(log(1-uniform) / log(1-p));
    }

    private static boolean mutateBoolean() {
        return Randomness.nextBoolean();
    }

    private static byte mutateByte() {
        if (Randomness.nextDouble() >= Properties.PRIMITIVE_POOL)
            return (byte) (Randomness.nextInt(256) - 128);
        else {
            return (byte) ConstantPoolManager.getInstance().getConstantPool().getRandomInt();
        }
    }

    private static char mutateChar() {
        return Randomness.nextChar();
    }

    private static double mutateDouble(Double value) {
        if (value != null && Randomness.nextDouble() >= Properties.PRIMITIVE_POOL) {
            Long bits = Double.doubleToLongBits(value);
            int lastOffset = Randomness.nextInt(Double.BYTES);
            int startOffset = min(lastOffset, Randomness.nextInt(Double.BYTES));
            for (int i = startOffset; i <= lastOffset; ++i) {
                int shift = i * 8, mask = 0xff << shift;
                bits = (bits & ~mask) | (Randomness.nextByte() << shift);
            }
            return Double.longBitsToDouble(bits);
        } else {
            return ConstantPoolManager.getInstance().getConstantPool().getRandomDouble();
        }
    }

    private static float mutateFloat(Float value) {
        if (value != null && Randomness.nextDouble() >= Properties.PRIMITIVE_POOL) {
            int bits = Float.floatToIntBits(value);
            int lastOffset = Randomness.nextInt(Float.BYTES);
            int startOffset = min(lastOffset, Randomness.nextInt(Float.BYTES));
            for (int i = startOffset; i <= lastOffset; ++i) {
                int shift = i * 8, mask = 0xff << shift;
                bits = (bits & ~mask) | (Randomness.nextByte() << shift);
            }
            return Float.intBitsToFloat(bits);
        } else {
            return ConstantPoolManager.getInstance().getConstantPool().getRandomFloat();
        }
    }

    private static int mutateInt(Integer value) {
        if (value != null && Randomness.nextDouble() >= Properties.PRIMITIVE_POOL) {
            int lastOffset = Randomness.nextInt(Integer.BYTES);
            int startOffset = min(lastOffset, Randomness.nextInt(Integer.BYTES));
            for (int i = startOffset; i <= lastOffset; ++i) {
                int shift = i * 8, mask = 0xff << shift;
                value = (value & ~mask) | (Randomness.nextByte() << shift);
            }
            return value;
        }else {
            return ConstantPoolManager.getInstance().getConstantPool().getRandomInt();
        }
    }

    private static long mutateLong(Long value) {
        if (value != null && Randomness.nextDouble() >= Properties.PRIMITIVE_POOL) {
            int lastOffset = Randomness.nextInt(Long.BYTES);
            int startOffset = min(lastOffset, Randomness.nextInt(Long.BYTES));
            for (int i = startOffset; i <= lastOffset; ++i) {
                int shift = i * 8, mask = 0xff << shift;
                value = (value & ~mask) | (Randomness.nextByte() << shift);
            }
            return value;
        }else {
            return ConstantPoolManager.getInstance().getConstantPool().getRandomLong();
        }
    }

    private static short mutateShort(Short value) {
        if (value != null && Randomness.nextDouble() >= Properties.PRIMITIVE_POOL) {
            int lastOffset = Randomness.nextInt(Short.BYTES);
            int startOffset = min(lastOffset, Randomness.nextInt(Short.BYTES));
            for (int i = startOffset; i <= lastOffset; ++i) {
                int shift = i * 8, mask = 0xff << shift;
                value = (short) ((value & ~mask) | (Randomness.nextByte() << shift));
            }
            return value;
        }else {
            return (short)ConstantPoolManager.getInstance().getConstantPool().getRandomInt();
        }
    }

    private static String mutateString(String value) {
        if (value != null && Randomness.nextDouble() >= Properties.PRIMITIVE_POOL) {
            if (value.length() != 0) {
                StringBuilder chars = new StringBuilder(value);
                int lastOffset = Randomness.nextInt(chars.length());
                int startOffset = min(lastOffset, Randomness.nextInt(chars.length()));
                for (int i = startOffset; i <= lastOffset; ++i) {
                    chars.setCharAt(i, Randomness.nextChar());
                }
                return chars.toString();
            }else {
                return Randomness.nextString(Randomness.nextInt(Properties.STRING_LENGTH));
            }
        }else {
            ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();
            String candidateString = constantPool.getRandomString();
            if (Properties.MAX_STRING > 0 && candidateString.length() < Properties.MAX_STRING)
                value = candidateString;
            else
                value = Randomness.nextString(Randomness.nextInt(Properties.STRING_LENGTH));
            return value;
        }
    }

    private static Object getRandomValue(Object type) {
        if (type.equals(boolean.class)) {
            return mutateBoolean();
        }else if(type.equals(byte.class)) {
            return mutateByte();
        }else if(type.equals(char.class)) {
            return mutateChar();
        }else if(type.equals(double.class)) {
            return mutateDouble(null);
        }else if(type.equals(float.class)) {
            return mutateFloat(null);
        }else if(type.equals(int.class)) {
            return mutateInt(null);
        }else if(type.equals(long.class)) {
            return mutateInt(null);
        }else if(type.equals(short.class)) {
            return mutateShort(null);
        }else if(type.equals(String.class)) {
            return mutateString(null);
        }
        LoggingUtils.getEvoLogger().warn(type + "cannot be identified, we return null");
        return null;
    }

    private static Object getMutatedValue(Object value) {
        Object newValue = value;
        boolean changed = false;
        if (value instanceof Boolean) {
            newValue = mutateBoolean();
            changed = (boolean) newValue != (boolean) value;
        } else if (value instanceof Byte) {
            newValue = mutateByte();
            changed = (byte) newValue != (byte) value;
        } else if (value instanceof Character) {
            newValue = mutateChar();
            changed = (char) newValue != (char) value;
        } else if (value instanceof Double) {
            newValue = mutateDouble((Double) value);
            changed = (double) newValue != (double) value;
        } else if (value instanceof Float) {
            newValue = mutateFloat((Float) value);
            changed = (float) newValue != (float) value;
        } else if (value instanceof Integer) {
            newValue = mutateInt((Integer) value);
            changed = (int) newValue != (int) value;
        } else if (value instanceof Long) {
            newValue = mutateLong((Long) value);
            changed = (long) newValue != (long) value;
        } else if (value instanceof Short) {
            newValue = mutateShort((Short) value);
            changed = (short) newValue != (short) value;
        } else if (value instanceof String) {
            newValue = mutateString((String) value);
            if(newValue != null) {
                changed = !newValue.equals(value);
            }
        }
        return changed ? newValue : null;
    }
    private static boolean mutateConstantValue(ConstantValue constValue){
        Object value = constValue.getValue();
        Object newValue = null;
        if(value == null) {
            newValue = getRandomValue(constValue.getType());
        }else {
            newValue = getMutatedValue(value);
        }

        if(newValue != null) {
            constValue.setValue(newValue);
            return true;
        }
        return false;
    }

    private boolean mutateParameters(TestCase test) {
        int paramCounts = test.getRandomizableParameters();
        assert paramCounts > 0 : "param counts should be greater than 0";
        int paramMutationCounts = min(paramCounts, sampleGeometric(Properties.MEAN_MUTATION_COUNT));
        Set<Integer> paramPositions = new HashSet<>();
        while(paramPositions.size() < paramMutationCounts) {
            paramPositions.add(Randomness.nextInt(paramCounts));
        }

        boolean changed = false;
        int pos = 0;
        for (Statement s : test) {
            for(VariableReference var : s.getVariableReferences()) {
                if (var.equals(s.getReturnValue())
                        || var.equals(s.getReturnValue().getAdditionalVariableReference())) {
                    continue;
                }
                if(!(var instanceof ConstantValue)) {
                    continue;
                }
                if(var.isPrimitive() || var.isString()) {
                    if (paramPositions.contains(pos)) {
                        changed |= mutateConstantValue((ConstantValue) var);
                        paramPositions.remove(pos);
                        if (paramPositions.size() == 0) {
                            return changed;
                        }
                    }
                    pos++;
                }
            }
        }
        return changed;
    }

    private int countMutableParameters(TestCase test) {
        int counts = 0;
        for (Statement s : test) {
            for(VariableReference var : s.getVariableReferences()) {
                if (var.equals(s.getReturnValue())
                        || var.equals(s.getReturnValue().getAdditionalVariableReference())) {
                    continue;
                }
                if(!(var instanceof ConstantValue)) {
                    continue;
                }
                if(var.isPrimitive() || var.isString()) {
                    counts++;
                }
            }
        }
        return counts;
    }

    @Override
    public boolean doSearch(TestSuiteChromosome individual, LocalSearchObjective<TestSuiteChromosome> objective) {
        return false;
    }

    private long getSearchBudgetWithExtraTimeout() {
        /*
           Note that enabling fuzzing reduces the number and size of unit tests generated.
           In addition, it makes test suite minimization as a step in search procedure.
           We therefore approximately calculate reduced budgets of such procedures
           and add them to the search budget as follows.
        */
        double searchBudget = Properties.SEARCH_BUDGET;

        // add marginal budgets to search budget
        searchBudget += (Properties.MINIMIZATION_TIMEOUT - Properties.MINIMIZATION_TIMEOUT * Properties.FUZZ_START_PERCENT);
        if(Properties.ASSERTIONS) {
            searchBudget += (Properties.ASSERTION_TIMEOUT - Properties.ASSERTION_TIMEOUT * Properties.FUZZ_START_PERCENT);
        }
        if (Properties.JUNIT_TESTS && (Properties.JUNIT_CHECK == Properties.JUnitCheckValues.TRUE || Properties.JUNIT_CHECK == Properties.JUnitCheckValues.OPTIONAL)) {
            searchBudget += (Properties.JUNIT_CHECK_TIMEOUT - Properties.JUNIT_CHECK_TIMEOUT * Properties.FUZZ_START_PERCENT);
        }
        if (Properties.JUNIT_TESTS) {
            searchBudget += (Properties.WRITE_JUNIT_TIMEOUT - Properties.WRITE_JUNIT_TIMEOUT * Properties.FUZZ_START_PERCENT);
        }
        return (long)Math.ceil(searchBudget);
    }

    // called by master jvm
    public void setEvoFuzzParameters(List<String> cmdLine) {
        //adjust evolution algorithms
        cmdLine.add("-Dstrategy=MOSuite");
        cmdLine.add("-Dalgorithm=EVOFUZZ");
        cmdLine.add("-Dselection_function=RANK_CROWD_DISTANCE_TOURNAMENT");
//        cmdLine.add("-Dassertion_strategy=" + (Properties.ASSERTION_STRATEGY == Properties.AssertionStrategy.MUTATION ?
//                Properties.AssertionStrategy.ALL : Properties.ASSERTION_STRATEGY));

        //adjust pre and post processes
        cmdLine.add("-Dinline=false");
        cmdLine.add("-Dcoverage=false");
        cmdLine.add("-Dvariable_pool=true");

//        if(Properties.ASSERTIONS) {
//            if(!Properties.FUZZ_MINIMIZATION && Properties.ASSERTION_STRATEGY == Properties.AssertionStrategy.MUTATION) {
//                Properties.ASSERTION_TIMEOUT += Properties.MINIMIZATION_TIMEOUT;
//                Properties.MINIMIZATION_TIMEOUT = 0;
//            }
//        }else{
//            Properties.SEARCH_BUDGET += Properties.ASSERTION_TIMEOUT;
//            Properties.ASSERTION_TIMEOUT = 0;
//        }

        //recompute search budget and fuzzing start percent
        long minTimeout, assertionTimeout, junitCheckTimeout, writeJunitTimeout, searchBudget, searchPlusMinTimeout, fuzzBudget;
        double fuzzStartPercent;
        if(!Properties.FUZZ_USE_EXTRA_TIMEOUTS) {
            minTimeout = Properties.MINIMIZATION_TIMEOUT;
            assertionTimeout = Properties.ASSERTION_TIMEOUT;
            junitCheckTimeout = Properties.JUNIT_CHECK_TIMEOUT;
            writeJunitTimeout = Properties.WRITE_JUNIT_TIMEOUT;
            fuzzBudget = Properties.SEARCH_BUDGET - (long) (Properties.SEARCH_BUDGET * Properties.FUZZ_START_PERCENT);
            searchBudget = Properties.SEARCH_BUDGET + fuzzBudget;
            if(Properties.FUZZ_MINIMIZATION) {
                searchBudget += minTimeout;
                cmdLine.add("-Dminimize=false");
                cmdLine.add("-Dfuzz_minimization_timeout=" + minTimeout);
            }
            fuzzStartPercent = (double)(Properties.SEARCH_BUDGET) / searchBudget;
        }else {
            throw new Error("not supported option");
//            minTimeout = (long) Math.ceil(Properties.MINIMIZATION_TIMEOUT * Properties.FUZZ_START_PERCENT);
//            assertionTimeout = (long) Math.ceil(Properties.ASSERTION_TIMEOUT * Properties.FUZZ_START_PERCENT);
//            junitCheckTimeout = (long) Math.ceil(Properties.JUNIT_CHECK_TIMEOUT * Properties.FUZZ_START_PERCENT);
//            writeJunitTimeout = (long) Math.ceil(Properties.WRITE_JUNIT_TIMEOUT * Properties.FUZZ_START_PERCENT);
//            searchBudget = getSearchBudgetWithExtraTimeout();
//            searchBudget += minTimeout;
//            fuzzBudget = searchBudget - (long) (searchBudget * Properties.FUZZ_START_PERCENT);
//            fuzzStartPercent = (double)(searchBudget) * Properties.FUZZ_START_PERCENT / searchBudget;
        }

        //set time budgets
        cmdLine.add("-Dfuzz_start_percent=" + fuzzStartPercent);
        cmdLine.add("-Dfuzz_budget=" + fuzzBudget);

        cmdLine.add("-Dsearch_budget=" + searchBudget);
        cmdLine.add("-Dassertion_timeout=" + assertionTimeout);
        cmdLine.add("-Djunit_check_timeout=" + junitCheckTimeout);
        cmdLine.add("-Dwrite_junit_timeout=" + writeJunitTimeout);

        // set others
        cmdLine.add("-Dfunctional_mocking_percent=" + (Properties.FUNCTIONAL_MOCKING_PERCENT * fuzzStartPercent));
        cmdLine.add("-Dreflection_start_percent=" + (Properties.REFLECTION_START_PERCENT * fuzzStartPercent));
//        cmdLine.add("-Dstopping_condition=" + Properties.StoppingCondition.MAXTIME);
        cmdLine.add("-Dp_functional_mocking=0.8");
        cmdLine.add("-Dp_reflection_on_private=0.5");
        cmdLine.add("-Dreuse_leftover_time=true");
        cmdLine.add("-Dtest_comments=false");
//        cmdLine.add("-Dmax_generic_depth=15");

        // disable statistics
        Properties.IGNORE_MISSING_STATISTICS = true;
        cmdLine.add("-Dignore_missing_statistics=true");
        Properties.NEW_STATISTICS = false;
        cmdLine.add("-Dnew_statistics=false");
        Properties.STATISTICS_BACKEND = Properties.StatisticsBackend.NONE;
        cmdLine.add("-Dstatistics_backend=NONE");

        LoggingUtils.getEvoLogger().info("* search budget(/w min) for evo+fuzzing and fuzz_start_percent: "
                + searchBudget + ", " + fuzzStartPercent);

        // recompute fuzzing criterion
//        ArrayList<Properties.Criterion> fuzzCriterion = new ArrayList<>();
//        for(Properties.Criterion c : Properties.CRITERION) {
//            if(c != Properties.Criterion.WEAKMUTATION && c != Properties.Criterion.STRONGMUTATION && c != Properties.Criterion.MUTATION) {
//                fuzzCriterion.add(c);
//            }
//        }
//        cmdLine.add("-Dcriterion=" + fuzzCriterion.toString().
//                replace("[", "").
//                replace("]", "").
//                replace(", ", ":"));
    }
}
