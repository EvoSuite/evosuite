package org.evosuite.testcase.mutation;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.setup.TestCluster;
import org.evosuite.symbolic.instrument.ClassLoaderUtils;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericExecutable;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class GuidedInsertion extends AbstractInsertionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(GuidedInsertion.class);
    private static final int parallelComputationThreshold = 50;
    private static final int binarySearchThreshold = 5;
    private Set<TestFitnessFunction> goals = Collections.emptySet();

    // singleton design pattern, use getInstance() instead
    private GuidedInsertion() { }

    public static GuidedInsertion getInstance() {
        return SingletonContainer.instance;
    }

    /**
     * Sets the goals to be targeted during the insertion process.
     *
     * @param goals the goals intended for covering
     */
    public void setGoals(final Set<TestFitnessFunction> goals) {
        this.goals = Objects.requireNonNull(goals, "goals to cover must not be null");
    }

    @Override
    protected boolean insertUUT(final TestCase test, final int lastPosition) {
        Objects.requireNonNull(test, "mutation: test case to modify must not be null");

        if (!test.isEmpty() && lastPosition < 0) {
            throw new IllegalArgumentException("illegal position for statement insertion");
        }

        if (goals.isEmpty()) { // all goals have been covered
            info("mutation: no more goals to cover, doing nothing");
            return true;
            // return insertRandomCall(test, lastPosition);
        }

        /*
         * Biased random selection of a goal based on the inverse cyclomatic complexity of its
         * target method. The rationale is to prefer "easier" (i.e., less complex) targets over
         * "harder" ones since easier targets are often easier to cover. Hopefully, this increases
         * coverage fast by covering easy targets first, and reduces the number of cases where we
         * spend too much search budget on infeasible goals.
         */
        final TestFitnessFunction currentGoal = rouletteWheelSelect(goals);

        return insertCallFor(test, currentGoal, lastPosition);
    }

    @Override
    protected int insertParam(TestCase test, int lastPosition) {
        // TODO: insert own implementation here!
        return super.insertParam(test, lastPosition);
    }

    /**
     * Performs a roulette wheel selection on the given set of gaols. The probability of a goal
     * being selected is inversely proportional to the cyclomatic complexity of the target method
     * of that goal. This means that two methods with the same cyclomatic complexity have the same
     * probability of being selected, while a method that is twice as complex as another method
     * only has half the probability of being selected.
     *
     * @param goals the goals on which to perform the selection
     * @return a goal chosen via biased-random selection
     */
    private static TestFitnessFunction rouletteWheelSelect(final Set<TestFitnessFunction> goals) {
        // We convert the set to an array to impose some arbitrary but fixed order on the goals.
        final TestFitnessFunction[] gs = goals.toArray(new TestFitnessFunction[0]);

        if (gs.length == 1) {
            return gs[0];
        }

        if (gs.length == 2) {
            final int cc0 = gs[0].getCyclomaticComplexity();
            final int cc1 = gs[1].getCyclomaticComplexity();
            final int pivot = Randomness.nextInt(cc0 + cc1);
            return pivot < cc0 ? gs[1] : gs[0];
        }

        /*
         * The reciprocal of the cyclomatic complexity of a target method is directly proportional
         * to the probability of the corresponding goal being selected. The prefix sum of these
         * reciprocal values is used to determine the index of the selected goal later on.
         */
        final double[] prefixSums = reciprocalPrefixSum(gs);

        // We spin the roulette wheel and obtain a pivot point. This is the point on the wheel
        // where the roulette ball falls onto after having lost all of its momentum.
        final double sum = prefixSums[gs.length - 1];
        final double pivot = nextRandomDouble(sum);

        // Finds the pocket on the wheel where the pivot point is located in and converts it to an
        // array  index. This index corresponds to the selected goal.
        final int index = findIndex(prefixSums, pivot);

        return gs[index];
    }

    /**
     * Returns the prefix sums of the reciprocal cyclomatic complexities for the target methods
     * of the given goals. By construction, the resulting array is sorted.
     *
     * @param goals the goals whose target methods to consider
     * @return the prefix sums of the reciprocal cyclomatic complexities
     */
    private static double[] reciprocalPrefixSum(final TestFitnessFunction[] goals) {
        final double[] prefixSum;

        /*
         * For our applications a naive implementation such as the following one using floating-
         * point operations is sufficiently accurate and numerical stability is not an issue at all.
         * It turns out that the computation offsets don't accumulate enough to produce a
         * fundamentally flawed result, even when millions of coverage goals are considered.
         */

        final boolean parallelComputation = goals.length > parallelComputationThreshold;
        if (parallelComputation) {
            prefixSum = Arrays.stream(goals).parallel()
                    .mapToDouble(g -> 1d / g.getCyclomaticComplexity())
                    .toArray();
            Arrays.parallelPrefix(prefixSum, Double::sum);
        } else {
            prefixSum = new double[goals.length];
            prefixSum[0] = 1d / goals[0].getCyclomaticComplexity();
            for (int i = 1; i < goals.length; i++) {
                prefixSum[i] = prefixSum[i - 1] + 1d / goals[i].getCyclomaticComplexity();
            }
        }

        return prefixSum;
    }

    /**
     * Returns a pseudorandom double value between 0.0 (inclusive) and the specified bound
     * (exclusive), using a random number generator that is isolated to the current thread. Using
     * such isolated generators in concurrent applications (such as this one) as opposed to
     * accessing the same shared instance of {@code java.util.Random} usually entails much less
     * overhead and contention.
     *
     * @param bound the upper bound (exclusive)
     * @return a pseudorandom double value between zero (inclusive) and the bound (exclusive)
     */
    private static double nextRandomDouble(final double bound) {
        return ThreadLocalRandom.current().nextDouble(bound);
    }

    /**
     * Searches the given strictly sorted array for the specified key and returns the appropriate
     * index where the key is found. If the array does not contain the key, the insertion point
     * of the key (i.e. the index where it would be inserted) is returned instead.
     *
     * @param sortedArray the array to be searched (must be sorted and not contain duplicates)
     * @param key         the value to search for in the array
     * @return the index of the key or its insertion point if the key is not found
     */
    private static int findIndex(final double[] sortedArray, final double key) {
        final boolean binarySearch = sortedArray.length > binarySearchThreshold;
        if (binarySearch) {
            final int index = Arrays.binarySearch(sortedArray, key);
            return index < 0 ? ~index : index;
        } else { // linear search
            final int lastIndex = sortedArray.length - 1;
            for (int i = 0; i < lastIndex; i++) {
                if (key < sortedArray[i]) { // the array is sorted and free of duplicates
                    return i;
                }
            }
            return lastIndex;
        }
    }

    /**
     * Tries to append a statement calling the target method or target constructor of the given
     * coverage goal to the end of specified test case. If everything succeeds, a {@code
     * VariableReference} to the return value of aforementioned statement is returned. Otherwise,
     * if there is an error, {@code null} is returned.
     *
     * @param test the test case to which the call of the target method should be appended
     * @param goal the coverage goal (target class and method) that should be attempted to reach
     * @param lastPos the position of the last valid statement of {@code test}
     * @return a reference to the return value of the statement calling the target method, or
     * {@code null} if unsuccessful
     */
    private boolean insertCallFor(final TestCase test, final TestFitnessFunction goal,
                                  final int lastPos) {
        // Tries to reflect the target class.
        final Class<?> clazz = getTargetClass(goal);
        if (clazz == null) { // unable to reflect target class
            return false;
        }

        // Tries to reflect the target executable (either method or constructor).
        final GenericExecutable<?, ?> executable = getTargetExecutable(goal, clazz);
        if (executable == null) { // unable to reflect target executable
            return false;
        }

        return super.insertCallFor(test, executable, lastPos);

        /*
         * In the given test case, tries to find an object which acts as callee for the goal's
         * target executable. If the latter is static or a constructor, we can omit this step. However,
         * if the target executable is a non-static method we have to find or create an appropriate
         * instance of the target class before we can call the method.
         */
        /*
        final VariableReference callee;
        if (executable.isStatic() || executable.isConstructor()) {
            callee = null; // There is no callee, and we encode this fact by setting it to null.
        } else { // non-static (virtual) method
            callee = findOrCreateObject(clazz, test, lastPos);
            if (callee == null) { // unable to find or create a matching object
                return null;
            }
        }

        // Tries to generate the required input parameters for the method or constructor to call.
        final List<VariableReference> parameters = satisfyParameters(test, callee, executable, lastPos);
        if (parameters == null) { // unable to satisfy all input parameters
            return null;
        }

        // Creates an appropriate method statement or constructor statement.
        final Statement statement;
        if (executable.isConstructor()) {
            final GenericConstructor constructor = (GenericConstructor) executable;
            statement = new ConstructorStatement(test, constructor, parameters);
        } else {
            final GenericMethod method = (GenericMethod) executable;
            statement = new MethodStatement(test, method, callee, parameters);
        }

        // Adds the statement calling the target executable at the end of the test case.
        // Also returns the variable reference to the return value of that statement.
        return test.addStatement(statement);
         */
    }

    /**
     * Returns the {@code Class} instance for the target class of the specified goal, using the
     * fully qualified class name as given by {@link TestFitnessFunction#getTargetClass()}. If
     * no {@code Class} definition can be found for that name {@code null} is returned.
     *
     * @param goal the goal for which to return the target {@code Class}
     * @return the corresponding {@code Class} instance or {@code null} if no definition is found
     */
    private static Class<?> getTargetClass(final TestFitnessFunction goal) {
        final String name = goal.getTargetClass();
        try {
            return TestCluster.getInstance().getClass(name);
        } catch (ClassNotFoundException e) {
            error("Unable to reflect unknown class {}", name);
            return null;
        }
    }

    /**
     * <p>
     * Tries to reflect the method or constructor targeted by the given coverage goal in the
     * specified class {@code clazz}, and creates a corresponding {@code GenericMethod} or
     * {@code GenericConstructor} as appropriate. Callers may safely downcast the returned
     * {@code GenericExecutableMember} to a {@code GenericMethod} or {@code GenericConstructor}
     * by checking the concrete subtype via the methods {@link GenericExecutable#isMethod()
     * isMethod()} and {@link GenericExecutable#isConstructor() isConstructor()}.
     * </p>
     * <p>
     * The correct {@code Class} instance for {@code clazz} can be obtained by calling
     * {@link GuidedInsertion#getTargetClass(TestFitnessFunction) getTargetClass()}
     * beforehand.
     * </p>
     * <p>
     * If no public member matching the name and descriptor given by
     * {@link TestFitnessFunction#getTargetMethod()} can be found as part of the class
     * represented by {@code clazz}, {@code null} is returned.
     * </p>
     *
     * @param goal  the goal for which to return the target method or constructor
     * @param clazz the {@code Class} instance representing the goal's target class
     * @return the {@code GenericExecutableMember} object that represents the goal's target method
     * or constructor, or {@code null} if no such method or constructor can be found
     */
    private static GenericExecutable<?, ?> getTargetExecutable(final TestFitnessFunction goal,
                                                               final Class<?> clazz) {
        final String nameDesc = goal.getTargetMethod();

        // nameDesc = name + descriptor, we have to split it into two parts to work with it
        final int descriptorStartIndex = nameDesc.indexOf('(');
        assert descriptorStartIndex > 0 : "malformed method name or descriptor";
        final String name = nameDesc.substring(0, descriptorStartIndex);
        final String descriptor = nameDesc.substring(descriptorStartIndex);

        // Tries to reflect the argument types.
        final Class<?>[] argumentTypes;
        final ClassLoader classLoader = TestGenerationContext.getInstance().getClassLoaderForSUT();
        try {
            argumentTypes = ClassLoaderUtils.getArgumentClasses(classLoader, descriptor);
        } catch (Throwable t) {
            error("Unable to reflect argument types of method {}", nameDesc);
            error("\tCause: {}", t.getMessage());
            return null;
        }

        final boolean isConstructor = name.equals("<init>");
        if (isConstructor) {
            return new GenericConstructor(getConstructor(clazz, argumentTypes), clazz);
        } else {
            return new GenericMethod(getMethod(clazz, name, argumentTypes), clazz);
        }
    }

    /**
     * Tries to reflect the public constructor of the class represented by {@code clazz} with the
     * parameter list specified by {@code argumentTypes}. If no such constructor can be found
     * {@code null} is returned.
     *
     * @param clazz         the class whose constructor to reflect
     * @param argumentTypes the argument types of the constructor to reflect
     * @return a {@code Constructor} object that represents the desired constructor, or {@code null}
     * if no such constructor can be found
     */
    private static Constructor<?> getConstructor(final Class<?> clazz,
                                                 final Class<?>[] argumentTypes) {
        final Constructor<?> constructor;
        try {
            constructor = clazz.getConstructor(argumentTypes);
        } catch (NoSuchMethodException e) {
            error("No constructor of {} with argument types {}", clazz.getName(), argumentTypes);
            return null;
        }
        return constructor;
    }

    /**
     * Tries to reflect the public method with the given {@code name} and argument types in the
     * class represented by {@code clazz}. If no such method can be found {@code null} is returned.
     *
     * @param clazz         representation of the class containing the method to reflect
     * @param name          name of the method to reflect
     * @param argumentTypes representation of the argument types of the method to reflect
     * @return a {@code Method} object representing the specified method, or {@code null} if no
     * appropriate method can be found
     */
    private static Method getMethod(final Class<?> clazz,
                                    final String name,
                                    final Class<?>[] argumentTypes) {
        final Method method;
        try {
            method = clazz.getMethod(name, argumentTypes);
        } catch (NoSuchMethodException e) {
            error("No method with name {} and arguments {} in {}", name, argumentTypes,
                    clazz.getName());
            return null;
        }
        return method;
    }

    /**
     * Logs a message at the info level if the logger is enabled at the info level.
     *
     * @param msg the message to log
     */
    private static void info(final String msg) {
        if (logger.isInfoEnabled()) {
            logger.info(msg);
        }
    }

    /**
     * Logs a message at the info level if the logger is enabled at the info level.
     *
     * @param format the format string message to log
     * @param args the arguments for the format string
     */
    private static void info(final String format, final Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(format, args);
        }
    }

    /**
     * Logs a message at the warn level if the logger is enabled at the warn level.
     *
     * @param msg the message to log
     */
    private static void warn(final String msg) {
        if (logger.isWarnEnabled()) {
            logger.warn(msg);
        }
    }

    /**
     * Logs a message at the warn level if the logger is enabled at the warn level.
     *
     * @param format the format string message to log
     * @param args the arguments for the format string
     */
    private static void warn(final String format, final Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(format, args);
        }
    }

    /**
     * Logs a message at the error level if the logger is enabled at the error level.
     *
     * @param msg the message to log
     */
    private static void error(final String msg) {
        if (logger.isErrorEnabled()) {
            logger.error(msg);
        }
    }

    /**
     * Logs a message at the error level if the logger is enabled at the error level.
     *
     * @param format the format string message to log
     * @param args the arguments for the format string
     */
    private static void error(final String format, final Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(format, args);
        }
    }

    private static final class SingletonContainer {
        static {
            if (!Properties.PURE_INSPECTORS) {
                warn("Purity analysis is disabled, use the switch -Dpure_inspectors=true to enable");
            }
        }
        private static final GuidedInsertion instance = new GuidedInsertion();
    }
}
