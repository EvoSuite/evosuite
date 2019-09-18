package org.evosuite.testcase.mutation.insertion;

import org.evosuite.Properties;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.metaheuristics.mosa.structural.MultiCriteriaManager;
import org.evosuite.graphs.ddg.FieldEntry;
import org.evosuite.graphs.ddg.MethodEntry;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestCluster;
import org.evosuite.symbolic.instrument.ClassLoaderUtils;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.statements.EntityWithParametersStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericExecutable;
import org.evosuite.utils.generic.GenericField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class implements a guided insertion strategy. That is, it uses static and dynamic
 * information about the SUT and the current state of the test generation process to make an
 * educated guess about which statements are most promising to cover new targets.
 *
 * @author Sebastian Schweikl
 */
public class GuidedInsertion extends AbstractInsertion {

    private static final Logger logger = LoggerFactory.getLogger(GuidedInsertion.class);
    private static final int parallelComputationThreshold = 50; // just arbitrarily picked
    private static final int binarySearchThreshold = 10;        // just arbitrarily picked

    private MultiCriteriaManager goalsManager = null;

    private GuidedInsertion() {
        // singleton design pattern, use getInstance() instead
    }

    /**
     * Returns the singleton instance of this class.
     *
     * @return the singleton
     */
    public static GuidedInsertion getInstance() {
        return SingletonContainer.instance;
    }

    /**
     * Sets the {@code MultiCriteriaManager} to be used for managing goals. {@code GuidedInsertion}
     * uses this to obtain information about the currently targeted goals, among other things.
     * Note that the {@code MultiCriteriaManager} can only be set once during the lifetime of this
     * object. Attempts to set it more than once will result in an {@code IllegalStateException}.
     *
     * @param goalsManager the goals manager to be used
     */
    public void setGoalsManager(MultiCriteriaManager goalsManager) {
        if (this.goalsManager == null) {
            debug("Initializing goals manager");
            this.goalsManager = Objects.requireNonNull(goalsManager);
        } else {
            throw new IllegalStateException("goals manager must not be initialized more than once");
        }
    }

    /**
     * Convenience method to retrieve the set of current goals from the goals manager.
     *
     * @return the current set of goals
     */
    private Set<TestFitnessFunction> goals() {
        return goalsManager.getCurrentGoals();
    }

    /**
     * Inserts a call to the UUT into the given test case as the specified position. The insertion
     * is guided, that is, it queries the goals manager for the set of current goals and tries to
     * insert a method that reaches one of those goals. The decision about which particular goal
     * to cover is made probabilistically.
     *
     * @param test     the test case in which to insert
     * @param position the position at which to insert
     * @return {@code true} on success, {@code false} otherwise
     */
    @Override
    protected boolean insertUUT(final TestCase test, final int position) {
        Objects.requireNonNull(test, "mutation: test case to modify must not be null");

        if (!test.isEmpty() && position < 0) {
            throw new IllegalArgumentException("illegal position for statement insertion");
        }

        if (goals().isEmpty()) { // all goals have been covered
            info("mutation: no more goals to cover");
            return insertRandomCall(test, position); // Just insert some random stuff then...
        }

        // The goal the given test case intended to cover.
        final TestFitnessFunction previousGoal = test.getTarget();

        // Whether the test case actually managed to cover the intended goal.
        final boolean previousGoalCovered = !goals().contains(previousGoal)
                || (previousGoal == null && test.isEmpty());

        debug("Previous goal has not been covered: {}", previousGoal);

        /*
         * Whether it should be retried to cover the previous goal. Only applies if the goal has
         * not been covered in the first place. The probability of a retry decreases linearly
         * with the number of failed attempts to cover the goal. However, if the non-covered goal
         * is the only goal left, we always retry to cover it.
         */
        final boolean retry = !previousGoalCovered
                && (nextRandomInt(previousGoal.getMaxFailures()) > previousGoal.getFailurePenalty()
                    || goals().size() == 1);

        // The goal intended to cover this time. Might be the same goal as before (if we failed
        // to cover it last time and we decided to try it again), or a new goal.
        final TestFitnessFunction chosenGoal = retry ? previousGoal : chooseNewGoalFor(test);

        return insertCallFor(test, chosenGoal, position);
    }

    /**
     * Decides which goal should be tried to cover for the given test case. Takes into
     * consideration the current goals as given by the multi-criteria manager, as well as the
     * goals that are already reached in the test case. Prefers the goals that can be "reached
     * directly" (see {@link GuidedInsertion#isPromising}).
     *
     * @param test the test case for which to decided which goal to cover next
     * @return the chosen goal
     */
    private TestFitnessFunction chooseNewGoalFor(final TestCase test) {
        debug("Choosing new goal for {}",  test);

        // We exclude the goal the given test case intended to target last time.
        final TestFitnessFunction previousGoal = test.getTarget(); // might be null

        // Determine the set of executables that are directly callable without having to instantiate
        // the owner class first (i.e., constructors, static methods, and non-static methods for
        // which the test already contains a proper object).
        final Map<Boolean, List<TestFitnessFunction>> partition = goals().stream()
                .filter(g -> !g.equals(previousGoal))
                .collect(Collectors.partitioningBy(goal -> isPromising(goal, test)));

        /*
         * We prefer goals whose target executables are directly callable (they should require the
         * least effort for the test generation process and are the most promising). With
         * logarithmically decreasing probability, we also include some of the fallback goals
         * into our list of candidate goals to increase the diversity of the generated tests. The
         * fallback goals are called lucky losers.
         */
        final List<TestFitnessFunction> preferredGoals = partition.get(true);
        final List<TestFitnessFunction> fallbackGoals = partition.get(false);
        final List<TestFitnessFunction> candidates;

        if (preferredGoals.isEmpty()) {
            debug("Have to use a fallback goal");
            candidates = fallbackGoals;
        } else {
            candidates = preferredGoals;

            while (Randomness.nextBoolean() && !fallbackGoals.isEmpty()) {
                final TestFitnessFunction luckyLoser = rouletteWheelSelect(fallbackGoals);
                fallbackGoals.remove(luckyLoser);
                candidates.add(luckyLoser);
            }
        }

        // The set of methods that are able to observe a side-effect caused by one of the methods
        // invoked in the test case.
        final Set<MethodEntry> readingMethods = test.getCalledMethods().stream()
                .flatMap(m -> DependencyAnalysis.getReadingMethodsMerged(toMethodEntry(m)).stream())
                .collect(Collectors.toSet());

        // The set of candidate goals with target methods that are able to observe such
        // side effects, offering a great potential for creating short and punchy test sequences
        // (in terms of coverage and reaching new program states).
        final List<TestFitnessFunction> readingGoals = new LinkedList<>();
        for (MethodEntry rm : readingMethods) {
            final String className1 = rm.getClassName();
            final String methodName1 = rm.getMethodNameDesc();

            for (TestFitnessFunction g : candidates) {
                final String className2 = g.getTargetClassName();
                final String methodName2 = g.getTargetMethodName();

                if (className1.equals(className2) && methodName1.equals(methodName2)) {
                    readingGoals.add(g);
                }
            }
        }

        /*
         * Biased random selection of a goal based on the inverse cyclomatic complexity of its
         * target executable. The rationale is that the cyclomatic complexity is directly
         * proportional to the number of decision nodes. And more decision nodes means more
         * potential structural dependencies, which makes it harder to reach full coverage of
         * that executable. Hence, we prefer "easier" (i.e., less complex) targets over "harder"
         * ones since easier methods are often also easier to cover. Hopefully, this increases
         * coverage fast by covering easy targets first, and reduces the number of cases where
         * we spend too much search budget on goals that are infeasible to cover.
         */
        final TestFitnessFunction chosenGoal =
                rouletteWheelSelect(readingGoals.isEmpty() ? candidates : readingGoals);

        /*
         * Encode the intended coverage goal in the test case. This is required since fitness
         * evaluation would otherwise not be able to increase the failure penalty for that goal in
         * case it was not covered or the test case was invalid (e.g., it dit not compile or threw
         * an exception when it was executed). Also, it provides the mutation operator additional
         * information to reason about the test case.
         */
        test.setTarget(chosenGoal);

        return chosenGoal;
    }

    /**
     * Performs a roulette wheel selection on the given collection of gaols. The probability of a
     * goal being selected is inversely proportional to the cyclomatic complexity of the target
     * executable (i.e., method or constructor) of that goal. This means that two executables with
     * the same cyclomatic complexity have the same probability of being selected, while an
     * executable that is twice as complex as another executable only has half the probability of
     * being selected.
     *
     * @param goals the goals on which to perform the selection
     * @return a goal chosen via biased-random selection
     */
    private TestFitnessFunction rouletteWheelSelect(final Collection<TestFitnessFunction> goals) {
        /*
         * The collection of goals could be unordered (e.g., when it's a set). Still, the inner
         * workings of the roulette wheel selection require some arbitrary but fixed order. We
         * impose this order by converting the collection of goals to an array (arrays also offer
         * good performance and random access, which the algorithm also benefits from). The imposed
         * order, even though being arbitrary, has no impact on the outcome of the selection,
         * since it's fixed during the course of the selection.
         */
        final TestFitnessFunction[] gs = goals.toArray(new TestFitnessFunction[0]);

        // by construction, gs cannot be empty here, so no need to check for gs.length == 0

        if (gs.length == 1) {
            return gs[0];
        }

        if (gs.length == 2) {
            final int cc0 = gs[0].getCyclomaticComplexity();
            final int cc1 = gs[1].getCyclomaticComplexity();
            final int pivot = nextRandomInt(cc0 + cc1);
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
    private double[] reciprocalPrefixSum(final TestFitnessFunction[] goals) {
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
     * Tells whether the given goal is promising to be reached w.r.t. the given test case. A
     * target is considered promising if it is contained within a public executable that
     * satisfies one of the following criteria: the executable is...
     * <ul>
     *     <li>static, or</li>
     *     <li>a constructor, or</li>
     *     <li>a non-static method for which the supplied test case already contains an
     *     instance of the method's owner class, or</li>
     *     <li>a non-static method for which the supplied test case already has the input
     *     parameters resolved.</li>
     * </ul>
     * In addition, a target is also promising if it is contained inside a non-public
     * executable and the test case already covers a control dependency of the target.
     *
     * @param goal the goal for which to determine whether the target executable is directly
     *             callable
     * @param test the test case in which the target executable should be called
     * @return {@code true} if the target executable is directly callable, {@code false} otherwise
     */
    private boolean isPromising(final TestFitnessFunction goal, final TestCase test) {
        return goal.isPublic()
                && (goal.isStatic()
                    || goal.isConstructor()
                    || test.hasObject(goal.getClazz(), test.size())
                    || coversDirectControlDependency(goal, test)
                    || test.callsMethod(goal)
                    || hasParametersResolved(goal, test));
    }

    /**
     * Tells whether the given test case already covers a control dependency of the given goal.
     * This implies that the method containing the goal is already called by the test case.
     *
     * @param goal the goal to cover
     * @param test the test case to cover the goal
     * @return {@code true} if the test case contains a direct control dependency to the goal,
     * {@code false otherwise}
     */
    private boolean coversDirectControlDependency(final TestFitnessFunction goal, final TestCase test) {
        if (goal instanceof BranchCoverageTestFitness) {
            final Set<TestFitnessFunction> parents = goalsManager.getStructuralParents(goal);
            for (TestFitnessFunction coveredGoal : test.getCoveredGoals()) {
                if (parents.contains(coveredGoal)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasParametersResolved(final TestFitnessFunction goal, final TestCase test) {
        final GenericExecutable<?, ?> target = goal.getExecutable();
        final Type[] parameterTypes = target.getParameterTypes();
        return Arrays.stream(parameterTypes).allMatch(test::hasAssignableObject);
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
    private double nextRandomDouble(final double bound) {
        return ThreadLocalRandom.current().nextDouble(bound);
    }

    /**
     * Returns a pseudorandom integer value between 0 (inclusive) and the specified bound
     * (exclusive), using a random number generator that is isolated to the current thread. Using
     * such isolated generators in concurrent applications (such as this one) as opposed to
     * accessing the same shared instance of {@code java.util.Random} usually entails much less
     * overhead and contention.
     *
     * @param bound the upper bound (exclusive)
     * @return a pseudorandom integer value between zero (inclusive) and the bound (exclusive)
     */
    private int nextRandomInt(final int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
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
    private int findIndex(final double[] sortedArray, final double key) {
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
        debug("Trying to insert call that covers {}", goal);

        /*
         * The executable to call might not be public. In this case, we cannot call it directly.
         * Instead, we must search for a public method that calls the non-public method for us.
         * When we find such a caller, there is no guarantee that the non-public method will
         * actually be invoked. This might depend on many other factors, such as the particular
         * assignment of input parameter values, the object's current state, etc.
         */
        if (!goal.isPublic()) {
            debug("Goal is not public, trying to find a public caller");
            final String calleeClassName = goal.getTargetClassName();
            final String calleeMethodName = goal.getTargetMethodName();
            return insertCallFor(test, calleeClassName, calleeMethodName, lastPos);
        } else {
            final GenericExecutable<?, ?> executable = goal.getExecutable();
            return super.insertCallFor(test, executable, lastPos);
        }
    }

    private boolean insertCallFor(final TestCase test, final MethodEntry entry,
                                  final int lastPos) {
        final String calleeClassName = entry.getClassName();
        final String calleeMethodNameDesc = entry.getMethodNameDesc();
        return insertCallFor(test, calleeClassName, calleeMethodNameDesc, lastPos);
    }

    private boolean insertCallFor(final TestCase test,
                                  final String calleeClassName,
                                  final String calleeMethodName,
                                  final int lastPos) {
        final GenericExecutable<?, ?> publicCaller =
                findPublicCallerFor(calleeClassName, calleeMethodName);
        if (publicCaller == null) {
            return false;
        } else {
            return super.insertCallFor(test, publicCaller, lastPos);
        }
    }

    private Set<FieldEntry> getWrittenFields(final TestCase test) {
        return test.getCalledMethods().stream()
                .map(this::toMethodEntry)
                .flatMap(m -> DependencyAnalysis.getWrittenFieldsMerged(m).stream())
                .collect(Collectors.toSet());
    }

    private GenericExecutable<?,?> findPublicCallerFor(final String calleeClassName,
                                                       final String calleeMethodName) {
        final Set<MethodEntry> publicCallers = DependencyAnalysis.getCallGraph()
                .getPublicCallersOf(calleeClassName, calleeMethodName);

        if (publicCallers.isEmpty()) {
            debug("Could not find public caller for {} in class {}", calleeMethodName, calleeClassName);
            // If we don't find a public caller, we have no choice but to give up... :(
            return null;
        }

        final MethodEntry chosenCaller;
        if (publicCallers.size() == 1) {
            chosenCaller = publicCallers.iterator().next();
        } else {
            // Just choose a caller randomly. In the future, we could try to make a random
            // biased selection based on how difficult it is to generate the input parameters
            // for the caller or the cyclomatic complexity of the caller.
            chosenCaller = Randomness.choice(publicCallers);
        }

        final String callerMethodNameDesc = chosenCaller.getMethodNameDesc();
        final String callerClassName = chosenCaller.getClassName();

        final Class<?> clazz = ClassLoaderUtils.getClazz(callerClassName);
        return ClassLoaderUtils.getExecutable(callerMethodNameDesc, clazz);
    }

    @Override
    protected int insertParam(TestCase test, int position) {
        Objects.requireNonNull(test, "mutation: test case to modify must not be null");

        if (!test.isEmpty() && position < 0) {
            throw new IllegalArgumentException("illegal position for statement insertion");
        }

        if (goals().isEmpty()) {
            info("mutation: no more goals to cover");
            return insertRandomParam(test, position);
        }

        final TestFitnessFunction previousGoal = test.getTarget();
        final boolean previousGoalCovered = !goals().contains(previousGoal)
                || (previousGoal == null && test.isEmpty());

        if (previousGoalCovered) {
            debug("intended goal already covered, inserting random statements");
            return insertRandomParam(test, position);
        } else {
            debug("Intended goal not yet covered: {}", previousGoal);

            /*
             * Preliminary consideration:
             * When a test already calls the method containing our coverage goal but the goal
             * is still missed, it must be because at some decision node control-flow took the
             * "wrong" branch. Observation: branch conditions often involve class attributes.
             * If we manage to assign a different value to an attribute (e.g. via a setter)
             * before the method is called, control flow might take a different path. Idea:
             * similar to data dependencies between statements, one can identify data dependencies
             * between methods. We deliberately try to create such dependencies in our test
             * by inserting methods that write and subsequently read the same class attribute.
             * Sometimes, the class attribute in question might even be public, so that a new
             * value can be assigned directly.
             */

            // We try to modify public fields first. This is the easiest thing to do and incurs
            // the least amount of bloat in the generated test case.
            final Set<FieldEntry> readFields =
                    DependencyAnalysis.getReadFieldsMerged(toMethodEntry(previousGoal));

            // Set of candidate fields for modification. Fields must be public and either static
            // or there must already exist an object of the required type.
            final Set<Field> candidateFields = new HashSet<>(readFields.size());
            for (FieldEntry field : readFields) {
                final String className = field.getClassName();

                if (!DependencyAnalysis.isTargetProject(className)) {
                    continue;
                }

                final Class<?> clazz = ClassLoaderUtils.getClazz(className);

                if (clazz == null) {
                    continue;
                }

                final Field f = ClassLoaderUtils.getField(field.getFieldName(), clazz);

                if (f == null) {
                    continue;
                }

                final boolean isPublic = Modifier.isPublic(f.getModifiers());
                final boolean isStatic = Modifier.isStatic(f.getModifiers());

                if (isPublic && (isStatic || test.hasObject(clazz, test.size()))) {
                    candidateFields.add(f);
                }
            }

            // In case there are fields meeting our criteria, we try to mutate one of those fields.
            if (!candidateFields.isEmpty()) {
                final Field choice = Randomness.choice(candidateFields);

                debug("trying to write to public field directly: {}", choice);

                final Class<?> clazz = choice.getDeclaringClass();
                final GenericField field = new GenericField(choice, clazz);
                final int index = test.lastIndexOfCallTo(previousGoal.getTargetClassName(),
                        previousGoal.getTargetMethodName());

                final VariableReference callee;
                try {
                    callee = test.getRandomObject(clazz, index);
                } catch (ConstructionFailedException e) {
                    debug("Could not find callee object for field {}", choice);
                    return -1;
                }

                //final boolean success = insertCallFor(test, field, index);
                final boolean success = addCallForMethodOrField(test, callee, field, index);
                if (success) {
                    return index;
                } else {
                    debug("Failed to modify field directly: {}", choice);
                    return -1;
                }
            }

            // If we cannot mutate a field directly, try to achieve indirect mutations by inserting
            // method calls that perform the mutations as side effect by writing to the field.
            final MethodEntry writingMethod;

            if (previousGoal.isPublic()) {
                // The goal is located inside a public method. We insert a call to another method
                // that holds a data dependency on the current method.
                final List<EntityWithParametersStatement> calledMethods = test.getCalledMethods();
                final Set<MethodEntry> writingMethods = calledMethods.stream()
                        .flatMap(m -> getWritingMethodsMerged(m).stream())
                        .collect(Collectors.toSet());
                writingMethod = Randomness.choice(writingMethods);
            } else {
                /*
                 * The goal is located inside a non-public method. We must have inserted a call to
                 * a public method that in turn tried to call the non-public one. Two
                 * possibilities:
                 * (1) The public method didn't actually manage to call the non-public one. In
                 *     this case, insert a data dependency on the public method.
                 * (2) The non-public method was entered but control flow took the wrong path.
                 *     We have to insert a data dependency on the non-public method.
                 */

                final String className = previousGoal.getTargetClassName();
                final String methodName = previousGoal.getTargetMethodName();

                // All public callers of the non-public method that are already contained in the
                // test case.
                final Stream<MethodEntry> publicCallers = DependencyAnalysis.getCallGraph()
                        .getPublicCallersOf(className, methodName)
                        .stream()
                        .filter(m -> test.callsMethod(m.getClassName(), m.getMethodNameDesc()));

                // The methods holding a data dependency on the public callers.
                final Stream<MethodEntry> writingMethods1 = publicCallers
                        .flatMap(m -> getWritingMethodsMerged(m).stream());

                // The methods holding a data dependency on the non-public target method.
                final Stream<MethodEntry> writingMethods2 = DependencyAnalysis.getCallGraph()
                        .getCallsFrom(className, methodName)
                        .stream()
                        .flatMap(m -> getWritingMethodsMerged(m).stream());

                // The union of all methods holding data dependencies on either the public caller
                // or the non-public target method.
                final Set<MethodEntry> writingMethods =
                        Stream.concat(writingMethods1, writingMethods2)
                                .collect(Collectors.toSet());

                // We randomly choose the method we want to insert and hope for the best.
                writingMethod = Randomness.choice(writingMethods);
            }

            debug("Trying to call public mutator that holds a data dependency on the target");

            if (writingMethod == null) {
                debug("Couldn't find such a method");
                return -1;
            }

            final int insertionIndex = test.lastIndexOfCallTo(previousGoal.getTargetClassName(),
                    previousGoal.getTargetMethodName());
            if (insertionIndex < 0) {
                debug("Invalid insertion index");
                return -1;
            } else {
                final boolean success = insertCallFor(test, writingMethod, insertionIndex);

                if (success) {
                    debug("Successfully inserted call to {} at index {}", writingMethod, insertionIndex);
                    return insertionIndex;
                } else {
                    debug("Could not insert call to {} at index {}", writingMethod, insertionIndex);
                    return -1;
                }
            }
        }
    }

    private boolean insertDataDependency(final TestFitnessFunction goal, final TestCase test,
                                         final int lastPos) {
        final Set<MethodEntry> writingMethods = getWritingMethods(goal);
        return !writingMethods.isEmpty()
                && insertCallFor(test, Randomness.choice(writingMethods), lastPos);
    }

    public boolean fuzzObjectState(final TestCase test, final VariableReference var,
                                   final int lastPos) {
        final GenericClass clazz = var.getGenericClass();
        final Set<GenericAccessibleObject<?>> modifiers;
        try {
            modifiers = TestCluster.getInstance().getCallsFor(clazz);
        } catch (ConstructionFailedException e) {
            return false;
        }
        if (modifiers.isEmpty()) {
            return false;
        } else {
            return super.insertCallFor(test, Randomness.choice(modifiers), lastPos);
        }
    }

    private boolean callsMethod(final TestCase test, final MethodEntry methodEntry) {
        final String className = methodEntry.getClassName();
        final String methodNameDesc = methodEntry.getMethodName() + methodEntry.getDescriptor();
        return test.callsMethod(className, methodNameDesc);
    }

    private Set<MethodEntry> getWritingMethods(final TestFitnessFunction goal) {
        final MethodEntry entry = toMethodEntry(goal);
        return DependencyAnalysis.getWritingMethods(entry);
    }

    private Set<MethodEntry> getWritingMethodsMerged(final TestFitnessFunction goal) {
        final MethodEntry entry = toMethodEntry(goal);
        return DependencyAnalysis.getWritingMethodsMerged(entry);
    }

    private Set<MethodEntry> getWritingMethodsMerged(final EntityWithParametersStatement stmt) {
        return DependencyAnalysis.getWritingMethodsMerged(toMethodEntry(stmt));
    }

    private Set<MethodEntry> getWritingMethodsMerged(final MethodEntry entry) {
        return DependencyAnalysis.getWritingMethodsMerged(entry);
    }

    private MethodEntry toMethodEntry(final TestFitnessFunction f) {
        final String clazz = f.getTargetClassName();
        final String nameDesc = f.getTargetMethodName();
        return new MethodEntry(clazz, nameDesc);
    }

    private MethodEntry toMethodEntry(final EntityWithParametersStatement stmt) {
        final String className = stmt.getDeclaringClassName();
        final String methodName = stmt.getMethodName();
        final String descriptor = stmt. getDescriptor();
        return new MethodEntry(className, methodName, descriptor);
    }

    /**
     * Logs a message at the debug level if the logger is enabled at the debug level.
     *
     * @param msg the message to log
     */
    private static void debug(final String msg) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg);
        }
    }

    /**
     * Logs a message at the debug level if the logger is enabled at the debug level.
     *
     * @param format the format string message to log
     * @param args the arguments for the format string
     */
    private static void debug(final String format, final Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(format, args);
        }
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

    /**
     * This private static inner class implements the singleton design pattern using the
     * initialization-on-demand holder idiom.
     */
    private static final class SingletonContainer {
        static {
            if (!Properties.PURE_INSPECTORS) {
                warn("Purity analysis is disabled, use the switch -Dpure_inspectors=true to enable");
            }
        }
        private static final GuidedInsertion instance = new GuidedInsertion();
    }
}
