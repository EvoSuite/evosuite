package org.evosuite.testcase.mutation.insertion;

import org.evosuite.Properties;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.metaheuristics.mosa.structural.MultiCriteriaManager;
import org.evosuite.graphs.ddg.FieldEntry;
import org.evosuite.graphs.ddg.MethodEntry;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.symbolic.instrument.ClassLoaderUtils;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.mutation.MutationUtils;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.EntityWithParametersStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericExecutable;
import org.evosuite.utils.generic.GenericField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.evosuite.testcase.mutation.MutationUtils.*;

/**
 * This class implements a guided insertion strategy. That is, it uses static and dynamic
 * information about the SUT and the current state of the test generation process to make an
 * educated guess about which statements are most promising to cover new targets.
 *
 * @author Sebastian Schweikl
 */
public class GuidedInsertion extends AbstractInsertion {

    private static final Logger logger = LoggerFactory.getLogger(GuidedInsertion.class);

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
     * uses this to obtain information about the currently targeted goals, among other things. Note
     * that the {@code MultiCriteriaManager} can only be set once during the lifetime of this
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
    public Set<TestFitnessFunction> goals() {
        return goalsManager.getCurrentGoals();
    }

    /**
     * Inserts a call to the UUT into the given test case as the specified position. The insertion
     * is guided, that is, it queries the goals manager for the set of current goals and tries to
     * insert a method that reaches one of those goals. The decision about which particular goal to
     * cover is made probabilistically.
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
                && (Randomness.nextDouble() < (double) previousGoal.getFailurePenalty() / previousGoal.getMaxFailures()
                || goals().size() == 1);

        // The goal intended to cover this time. Might be the same goal as before (if we failed
        // to cover it last time and we decided to try it again), or a new goal.
        final TestFitnessFunction chosenGoal = retry ? previousGoal : chooseNewGoalFor(test);

        return insertCallFor(test, chosenGoal, retry, position);
    }

    /**
     * Decides which goal should be tried to cover for the given test case. Takes into consideration
     * the current goals as given by the multi-criteria manager, as well as the goals that are
     * already reached in the test case. Prefers the goals that can be "reached directly".
     *
     * @param test the test case for which to decided which goal to cover next
     * @return the chosen goal
     */
    private TestFitnessFunction chooseNewGoalFor(final TestCase test) {
        debug("Choosing new goal for {}", test);

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
                final Optional<TestFitnessFunction> g = chooseGoal(fallbackGoals);
                final TestFitnessFunction luckyLoser = g.orElseThrow(IllegalStateException::new);
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
        final Optional<TestFitnessFunction> g =
                chooseGoal(readingGoals.isEmpty() ? candidates : readingGoals);
        final TestFitnessFunction chosenGoal = g.orElseThrow(IllegalStateException::new);

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
     * Tells whether the given goal is promising to be reached w.r.t. the given test case. A target
     * is considered promising if it is contained within a public executable that satisfies one of
     * the following criteria: the executable is...
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
                || (hasParametersResolved(goal, test) && (goal.isStatic() || test.hasObject(goal.getClazz(), test.size()))));
    }

    private boolean hasParametersResolved(final TestFitnessFunction goal, final TestCase test) {
        final GenericExecutable<?, ?> target = goal.getExecutable();
        final Type[] parameterTypes = target.getParameterTypes();
        return Arrays.stream(parameterTypes).allMatch(test::hasAssignableObject);
    }

    /**
     * Tells whether the given test case already covers a control dependency of the given goal. This
     * implies that the method containing the goal is already called by the test case.
     *
     * @param goal the goal to cover
     * @param test the test case to cover the goal
     * @return {@code true} if the test case contains a direct control dependency to the goal,
     * {@code false otherwise}
     */
    private boolean coversDirectControlDependency(final TestFitnessFunction goal,
                                                  final TestCase test) {
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

    /**
     * Tries to append a statement calling the target method or target constructor of the given
     * coverage goal to the end of specified test case. If everything succeeds, a {@code
     * VariableReference} to the return value of aforementioned statement is returned. Otherwise, if
     * there is an error, {@code null} is returned.
     *
     * @param test    the test case to which the call of the target method should be appended
     * @param goal    the coverage goal (target class and method) that should be attempted to reach
     * @param retry   whether the given goal was already tried to be covered before
     * @param lastPos the position of the last valid statement of {@code test}
     * @return a reference to the return value of the statement calling the target method, or {@code
     * null} if unsuccessful
     */
    private boolean insertCallFor(final TestCase test, final TestFitnessFunction goal,
                                  boolean retry, final int lastPos) {
        debug("Trying to insert call that covers {}", goal);

        /*
         * The executable to call might not be public. In this case, we cannot call it directly.
         * Instead, we must search for a public method that calls the non-public method for us.
         * When we find such a caller, there is no guarantee that the non-public method will
         * actually be invoked. This might depend on many other factors, such as the particular
         * assignment of input parameter values, the object's current state, etc.
         */
        if (!goal.isPublic()) {
            if (retry) {
                // When we already tried to call the non-public method before, but we didn't reach
                // the target, we could try to insert yet another call to a public "proxy" method.
                // Or, alternatively, we could try to fuzz the parameters to the proxy method, or even
                // fuzz the state of the callee object.
                try {
                    final VariableReference object = test.getLastObject(goal.getClazz(), lastPos);
                    return super.insertRandomCallOnObjectAt(test, object, lastPos);
                } catch (ConstructionFailedException e) {
                    // Last-ditch effort.
                    return super.insertRandomCall(test, lastPos);
                }
            } else {
                debug("Goal is not public, trying to find a public caller");
                final String calleeClassName = goal.getTargetClassName();
                final String calleeMethodName = goal.getTargetMethodName();
                return insertCallFor(test, calleeClassName, calleeMethodName, lastPos);
            }
        } else {
            final boolean containsCall = test.callsMethod(goal);
            if (retry && containsCall) {
                // The test case already calls the right method but still misses the target.
                // If the method has complex input parameters, we might want to fuzz the state of
                // some of them.
                final int index = test.lastIndexOfCallTo(goal);
                final Statement stmt = test.getStatement(index);

                // We are only interested in method or constructor statements, no functional mocks.
                if (stmt instanceof ConstructorStatement) {
                    ConstructorStatement call = (ConstructorStatement) stmt;
                    return fuzzComplexParameters(test, call, null, index);
                } else if (stmt instanceof MethodStatement) {
                    MethodStatement call = (MethodStatement) stmt;
                    return fuzzComplexParameters(test, call, call.getCallee(), index);
                } else {
                    error("can only handle method or constructor statements");
                    return false;
                }
            } else {
                // Try to call the target method directly.
                final GenericExecutable<?, ?> executable = goal.getExecutable();
                return super.insertCallFor(test, executable, lastPos);
            }
        }
    }

    private boolean fuzzComplexParameters(TestCase test, EntityWithParametersStatement call,
                                          VariableReference callee, int pos) {
        // TODO: Currently, we only handle objects and arrays. But FieldReferences and
        //  ArrayIndexes might point to complex objects and other arrays, too. It would be
        //  nice if we would try to resolve them recursively.
        final Set<VariableReference> complexParameters = call.getParameterReferences().stream()
                .filter(v -> !(v.isPrimitive()
                        || v.isString()
                        || v.isEnum()
                        || v.isVoid()
                        || v.isFieldReference()
                        || v.isArrayIndex()))
                .collect(Collectors.toSet());

        if (callee != null) {
            complexParameters.add(callee);
        }

        if (complexParameters.isEmpty()) {
            return false;
        }

        boolean success = false;
        final double fuzzingProbability = 1d / complexParameters.size();
        for (VariableReference parameter : complexParameters) {
            if (Randomness.nextDouble() < fuzzingProbability) {
                success |= fuzzObjectState(test, parameter, pos);
            }
        }
        return success;
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
            warn("no public caller found for {} in {}", calleeMethodName, calleeClassName);
            return false;
        } else {
            return super.insertCallFor(test, publicCaller, lastPos);
        }
    }

    private Set<FieldEntry> getWrittenFields(final TestCase test) {
        return test.getCalledMethods().stream()
                .map(MutationUtils::toMethodEntry)
                .flatMap(m -> DependencyAnalysis.getWrittenFieldsMerged(m).stream())
                .collect(Collectors.toSet());
    }

    private GenericExecutable<?, ?> findPublicCallerFor(final String calleeClassName,
                                                        final String calleeMethodName) {
        final MethodEntry[] publicCallers = DependencyAnalysis.getCallGraph()
                .getPublicCallersOf(calleeClassName, calleeMethodName)
                .toArray(new MethodEntry[0]);

        if (publicCallers.length == 0) {
            // If we don't find a public caller, we can't do anything :(
            warn("No public caller for {} in {}", calleeMethodName, calleeClassName);
            return null;
        }

        // We randomly choose the first caller that works. In the future, we could try to make a
        // random biased selection based on how difficult it is to generate the input parameters
        // for the caller or the cyclomatic complexity of the caller.
        Randomness.shuffle(publicCallers);
        for (final MethodEntry caller : publicCallers) {
            final String callerMethodNameDesc = caller.getMethodNameDesc();
            final String callerClassName = caller.getClassName();

            final Class<?> clazz = ClassLoaderUtils.getClazz(callerClassName);

            if (clazz == null) {
                warn("Unable to reflect {}", callerClassName);
                continue; // try the next caller
            }

            // Return the first one that works out.
            return ClassLoaderUtils.getExecutable(callerMethodNameDesc, clazz);
        }

        warn("No public caller for {} in {} could be reflected!", calleeMethodName, calleeClassName);
        return null;
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

        if (previousGoalCovered || !Properties.METHOD_DEPENDENCE_ANALYSIS) {
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

    private boolean fuzzObjectState(final TestCase test, final VariableReference var,
                                    final int pos) {
        final GenericClass clazz = var.getGenericClass();
        final Set<MethodEntry> stateModifiers =
                DependencyAnalysis.getDataDependenceGraph().getStateModifiers(clazz);
        if (stateModifiers.isEmpty()) {
            return false;
        }

        // TODO: find public fields that we can write to directly
        return insertCallFor(test, Randomness.choice(stateModifiers), var.getStPosition() + 1);
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
     * @param args   the arguments for the format string
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
     * @param args   the arguments for the format string
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
     * @param args   the arguments for the format string
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
     * @param args   the arguments for the format string
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
        private static final GuidedInsertion instance = new GuidedInsertion();

        static {
            if (!Properties.PURE_INSPECTORS) {
                warn("Purity analysis is disabled, use the switch -Dpure_inspectors=true to enable");
            }
        }
    }
}
