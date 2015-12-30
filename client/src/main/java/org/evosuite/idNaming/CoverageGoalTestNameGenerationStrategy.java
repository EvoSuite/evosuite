package org.evosuite.idNaming;

import org.evosuite.Properties;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.input.InputCoverageTestFitness;
import org.evosuite.coverage.input.InputObserver;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.method.MethodNoExceptionCoverageTestFitness;
import org.evosuite.coverage.output.OutputCoverageTestFitness;
import org.evosuite.coverage.output.OutputObserver;
import org.evosuite.runtime.mock.EvoSuiteMock;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.utils.Randomness;
import org.objectweb.asm.Type;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by gordon on 22/12/2015.
 */
public class CoverageGoalTestNameGenerationStrategy implements TestNameGenerationStrategy {

    private Map<TestCase, String> testToName = new HashMap<>();

    private Map<String, Set<String>> methodCount = new HashMap<>();

    public static final String PREFIX = "test";

    public static final String STR_CREATE = "Creates";

    public static final String STR_CREATE_EXCEPTION = "FailsToCreate";

    public static final String STR_THROWS = "Throws";

    public static final String STR_WITH = "With";

    public static final String STR_WITHOUT = "Without";

    public static final String STR_RETURNS = "Returning";

    public static final String STR_ARGUMENTS = "Arguments";

    public CoverageGoalTestNameGenerationStrategy(List<TestCase> testCases, List<ExecutionResult> results) {
        addGoalsNotIncludedInTargetCriteria(results);
        Map<TestCase, Set<TestFitnessFunction>> testToGoals = initializeCoverageMapFromResults(results);
        generateNames(testToGoals);
    }

    /**
     * This assumes all goals are already saved in the tests
     * @param testCases
     */
    public CoverageGoalTestNameGenerationStrategy(List<TestCase> testCases) {
        Map<TestCase, Set<TestFitnessFunction>> testToGoals = initializeCoverageMapFromTests(testCases);
        generateNames(testToGoals);
    }


    /**
     * Helper method that does the bulk of the work
     * @param testToGoals
     */
    private void generateNames(Map<TestCase, Set<TestFitnessFunction>> testToGoals ) {
        initializeMethodCoverageCount(testToGoals);
        findUniqueGoals(testToGoals);
        selectGoalName(testToGoals);
        fixAmbiguousTestNames();
    }

    /**
     * Builds the name map based on coverage goal stored as covered in each of the tests
     * @param tests
     * @return
     */
    private Map<TestCase, Set<TestFitnessFunction>> initializeCoverageMapFromTests(List<TestCase> tests) {
        Map<TestCase, Set<TestFitnessFunction>> testToGoals = new HashMap<>();
        for(TestCase test : tests) {
            testToGoals.put(test, filterSupportedGoals(new HashSet<>(test.getCoveredGoals())));
        }
        return testToGoals;
    }

    /**
     * Builds the name map based on coverage goals stored as covered in the tests pointed to by results
     * @param results
     * @return
     */
    private Map<TestCase, Set<TestFitnessFunction>> initializeCoverageMapFromResults(List<ExecutionResult> results) {
        Map<TestCase, Set<TestFitnessFunction>> testToGoals = new HashMap<>();
        for(ExecutionResult result : results) {
            testToGoals.put(result.test, filterSupportedGoals(new HashSet<>(result.test.getCoveredGoals())));
        }
        return testToGoals;
    }

    /**
     * Name generation assumes that certain coverage criteria are included. If we haven't targeted them yet,
     * we need to determine the covered goals. This may require re-executing the tests with observers.
     *
     * @param results
     */
    private void addGoalsNotIncludedInTargetCriteria(List<ExecutionResult> results) {
        List<Properties.Criterion> requiredCriteria = new ArrayList<>(Arrays.asList(new Properties.Criterion[] { Properties.Criterion.OUTPUT, Properties.Criterion.INPUT, Properties.Criterion.METHOD, Properties.Criterion.METHODNOEXCEPTION, Properties.Criterion.EXCEPTION}));
        requiredCriteria.removeAll(Arrays.asList(Properties.CRITERION));
        results = getUpdatedResults(requiredCriteria, results);
        for(Properties.Criterion c : requiredCriteria) {
            TestFitnessFactory<? extends TestFitnessFunction> goalFactory = FitnessFunctions.getFitnessFactory(c);
            List<? extends TestFitnessFunction> goals = goalFactory.getCoverageGoals();
            for(ExecutionResult result : results) {
                for(TestFitnessFunction goal : goals) {
                    if(goal.isCovered(result))
                        result.test.addCoveredGoal(goal);
                }
            }
        }
    }

    /**
     * Some criteria require re-execution with observers. Make sure the results are up-to-date
     *
     * @param requiredCriteria
     * @param origResults
     * @return
     */
    private List<ExecutionResult> getUpdatedResults(List<Properties.Criterion> requiredCriteria, List<ExecutionResult> origResults) {
        List<ExecutionObserver> newObservers = new ArrayList<ExecutionObserver>();
        if(requiredCriteria.contains(Properties.Criterion.INPUT)) {
            newObservers.add(new InputObserver());
        }
        if(requiredCriteria.contains(Properties.Criterion.OUTPUT)) {
            newObservers.add(new OutputObserver());
        }
        if(newObservers.isEmpty()) {
            return origResults;
        }
        for(ExecutionObserver observer : newObservers)
            TestCaseExecutor.getInstance().addObserver(observer);

        List<ExecutionResult> newResults = new ArrayList<ExecutionResult>();
        for(ExecutionResult result : origResults) {
            ExecutionResult newResult = TestCaseExecutor.getInstance().runTest(result.test);
            newResults.add(newResult);
        }

        for(ExecutionObserver observer : newObservers)
            TestCaseExecutor.getInstance().removeObserver(observer);

        return newResults;
    }

    /** We use only a subset of the possible criteria to determine names */
    private List<Class<?>> supportedClasses = Arrays.asList(new Class<?> [] { MethodCoverageTestFitness.class, MethodNoExceptionCoverageTestFitness.class,
            ExceptionCoverageTestFitness.class, OutputCoverageTestFitness.class, InputCoverageTestFitness.class});

    /**
     * Remove any goals that are irrelevant for name generation
     *
     * @param goals
     * @return
     */
    private Set<TestFitnessFunction> filterSupportedGoals(Set<TestFitnessFunction> goals) {
        return goals.stream().filter(c -> supportedClasses.contains(c.getClass())).collect(Collectors.toSet());
    }

    /**
     * Determine if we have overloaded methods, which requires the use of signatures
     *
     * @param testToGoals
     */
    private void initializeMethodCoverageCount(Map<TestCase, Set<TestFitnessFunction>> testToGoals) {
        for(Set<TestFitnessFunction> goals : testToGoals.values()) {
            for(TestFitnessFunction goal : goals) {
                String methodName = getMethodNameWithoutDescriptor(goal.getTargetMethod());
                if(!methodCount.containsKey(methodName)) {
                    methodCount.put(methodName, new HashSet<>());
                }
                methodCount.get(methodName).add(goal.getTargetMethod());
            }
        }
    }

    /**
     * Determine for each test the set of coverage goals uniquely covered by this test
     *
     * @param testToGoals
     */
    private void findUniqueGoals(Map<TestCase, Set<TestFitnessFunction>> testToGoals) {
        // Could be optimised
        Map<TestCase, Set<TestFitnessFunction>> goalMapCopy = new HashMap<>();

        for(Map.Entry<TestCase, Set<TestFitnessFunction>> entry : testToGoals.entrySet()) {
            Set<TestFitnessFunction> goalSet = new HashSet<>(entry.getValue());
            for(Map.Entry<TestCase, Set<TestFitnessFunction>> otherEntry : testToGoals.entrySet()) {
                if(entry == otherEntry)
                    continue;
                goalSet.removeAll(otherEntry.getValue());
            }
            goalMapCopy.put(entry.getKey(), goalSet);
        }
        testToGoals.putAll(goalMapCopy);
    }

    /**
     * Store the name for that test based on the unique goals determined for that test.
     *
     * @param testToGoals
     */
    private void selectGoalName(Map<TestCase, Set<TestFitnessFunction>> testToGoals) {
        for(Map.Entry<TestCase, Set<TestFitnessFunction>> entry : testToGoals.entrySet()) {
            if(entry.getValue().isEmpty()) {
                // If there is nothing unique about the test
                // use the original goals
                testToName.put(entry.getKey(), getTestName(entry.getKey(), entry.getKey().getCoveredGoals()));

            } else {
                testToName.put(entry.getKey(), getTestName(entry.getKey(), entry.getValue()));
            }
        }
    }

    /**
     * There may be tests with the same calculated name, in which case we add a number suffix
     */
    private void fixAmbiguousTestNames() {
        Map<String, Integer> nameCount = new HashMap<>();
        Map<String, Integer> testCount = new HashMap<>();
        for(String methodName : testToName.values()) {
            if(nameCount.containsKey(methodName))
                nameCount.put(methodName, nameCount.get(methodName) + 1);
            else {
                nameCount.put(methodName, 1);
                testCount.put(methodName, 0);
            }
        }
        for(Map.Entry<TestCase, String> entry : testToName.entrySet()) {
            if(nameCount.get(entry.getValue()) > 1) {
                int num = testCount.get(entry.getValue());
                testCount.put(entry.getValue(), num + 1);
                testToName.put(entry.getKey(), entry.getValue() + num);
            }
        }
    }

    /**
     * Make first letter upper case
     *
     * @param input
     * @return
     */
    private static String capitalize(String input) {
        final char[] buffer = input.toCharArray();
        buffer[0] = Character.toTitleCase(buffer[0]);
        return new String(buffer);
    }

    /**
     * Determine name for the given test
     *
     * @param test
     * @param uniqueGoals
     * @return
     */
    private String getTestName(TestCase test, Set<TestFitnessFunction> uniqueGoals) {
        List<TestFitnessFunction> goalList = getTopGoals(uniqueGoals);
        String name = PREFIX;
        if(goalList.isEmpty()) {
            // If there is nothing unique, we have to make do with what the test has
            if(!test.getCoveredGoals().isEmpty()) {
                return getTestName(test, test.getCoveredGoals());
            } else {
                // TODO - can this happen?
            }
        } else if(goalList.size() == 1) {
            name += capitalize(getGoalName(goalList.get(0)));
        } else if(goalList.size() == 2) {
            name += getGoalPairName(goalList.get(0), goalList.get(1));
        } else {
            name += capitalize(getGoalName(chooseRepresentativeGoal(test, goalList)));
        }
        return name;
    }

    /**
     * Retrieve all goals at the highest level of priority
     *
     * @param coveredGoals
     * @return
     */
    private List<TestFitnessFunction> getTopGoals(Set<TestFitnessFunction> coveredGoals) {
        List<TestFitnessFunction> goalList = new ArrayList<>(coveredGoals);
        Collections.sort(goalList, new GoalComparator());

        List<TestFitnessFunction> topGoals = new ArrayList<>();
        if(coveredGoals.isEmpty())
            return topGoals;

        Iterator<TestFitnessFunction> iterator = goalList.iterator();
        TestFitnessFunction lastGoal = iterator.next();
        topGoals.add(lastGoal);
        while(iterator.hasNext()) {
            TestFitnessFunction nextGoal = iterator.next();
            if(!nextGoal.getClass().equals(lastGoal.getClass()))
                break;
            topGoals.add(nextGoal);
            lastGoal = nextGoal;
        }
        return topGoals;
    }

    /**
     * Out of a set of multiple goals, select one that is representative.
     * Assumes that goals is not empty, and all items in goals have the same type
     * @param test
     * @param goals
     * @return
     */
    private TestFitnessFunction chooseRepresentativeGoal(TestCase test, Collection<TestFitnessFunction> goals) {
        Map<String, Integer> methodToPosition = new HashMap<>();
        for(Statement st : test) {
            if(st instanceof MethodStatement) {
                MethodStatement ms = (MethodStatement)st;
                String name = ms.getMethod().getName() + Type.getMethodDescriptor(ms.getMethod().getMethod());
                methodToPosition.put(name, st.getPosition());
            } else if (st instanceof ConstructorStatement) {
                ConstructorStatement cs = (ConstructorStatement)st;
                String name = "<init>" + Type.getConstructorDescriptor(cs.getConstructor().getConstructor());
                methodToPosition.put(name, st.getPosition());
            }
        }
        TestFitnessFunction chosenGoal = Randomness.choice(goals);
        int chosenPosition = -1;
        for(TestFitnessFunction goal : goals) {
            if(methodToPosition.containsKey(goal.getTargetMethod())) {
                int position = methodToPosition.get(goal.getTargetMethod());
                if(position >= chosenPosition) {
                    chosenPosition = position;
                    chosenGoal = goal;
                }
            }
        }
        return chosenGoal;
    }

    /**
     * Helper function to redirect name retrieval to the right method
     *
     * @param goal
     * @return
     */
    private String getGoalName(TestFitnessFunction goal) {
        if(goal instanceof MethodCoverageTestFitness) {
            return getGoalName((MethodCoverageTestFitness)goal);
        } else if(goal instanceof MethodCoverageTestFitness) {
            return getGoalName((MethodCoverageTestFitness)goal);
        } else if(goal instanceof MethodNoExceptionCoverageTestFitness) {
            return getGoalName((MethodNoExceptionCoverageTestFitness)goal);
        } else if(goal instanceof ExceptionCoverageTestFitness) {
            return getGoalName((ExceptionCoverageTestFitness)goal);
        } else if(goal instanceof InputCoverageTestFitness) {
            return getGoalName((InputCoverageTestFitness)goal);
        } else if(goal instanceof OutputCoverageTestFitness) {
            return getGoalName((OutputCoverageTestFitness)goal);
        } else {
            throw new RuntimeException("Not implemented yet: "+goal.getClass());
        }
    }

    /**
     * Get name for a single method goal
     * @param goal
     * @return
     */
    private String getGoalName(MethodCoverageTestFitness goal) {
        return formatMethodName(goal.getClassName(), goal.getMethod());
    }

    /**
     * Get name for a single method without exception goal
     * @param goal
     * @return
     */
    private String getGoalName(MethodNoExceptionCoverageTestFitness goal) {
        return formatMethodName(goal.getClassName(), goal.getMethod());
    }

    /**
     * Get name for a single exception goal
     * @param goal
     * @return
     */
    private String getGoalName(ExceptionCoverageTestFitness goal) {
        Class<?> ex = goal.getExceptionClass();
        while (!Modifier.isPublic(ex.getModifiers()) || EvoSuiteMock.class.isAssignableFrom(ex) ||
                ex.getCanonicalName().startsWith("com.sun.")) {
            ex = ex.getSuperclass();
        }

        if(goal.getTargetMethod().startsWith("<init>")) {
            return STR_CREATE_EXCEPTION + capitalize(getUniqueConstructorName(goal.getTargetClass(), goal.getTargetMethod()))+ STR_THROWS + capitalize(ex.getSimpleName());
        }
        return formatMethodName(goal.getTargetClass(), goal.getTargetMethod()) + STR_THROWS + capitalize(ex.getSimpleName());
    }

    /**
     * Get name for a single input goal
     * @param goal
     * @return
     */
    private String getGoalName(InputCoverageTestFitness goal) {
        String descriptor = goal.getValueDescriptor();
        return formatMethodName(goal.getClassName(), goal.getMethod()) + STR_WITH + formatValueDescriptor(descriptor);
    }

    /**
     * Get name for a single output goal
     * @param goal
     * @return
     */
    private String getGoalName(OutputCoverageTestFitness goal) {
        String descriptor = goal.getValueDescriptor();
        return formatMethodName(goal.getClassName(), goal.getMethod()) + STR_RETURNS + formatValueDescriptor(descriptor);
    }

    /**
     * Format the value descriptors used by input and output goals
     *
     * @param descriptor
     * @return
     */
    private String formatValueDescriptor(String descriptor) {
        String[] components = descriptor.split(":");
        if(components.length == 1) {
            return capitalize(descriptor);
        } else if(components.length == 2) {
            // Ignore classname
            return capitalize(components[1]);
        } else if(components.length == 3) {
            // Second one is "non-null", we'll just ignore this
            return capitalize(components[2]);
        } else if(components.length == 5) {
            // Inspector
            return capitalize(getShortClassName(components[2])) + capitalize(components[3]) + capitalize(components[4]);
        } else {
            throw new RuntimeException("Unsupported value descriptor: "+descriptor);
        }
    }

    /**
     * Some goals require special treatment when combining two
     * @param goal1
     * @param goal2
     * @return
     */
    private String getGoalPairName(TestFitnessFunction goal1, TestFitnessFunction goal2) {
        if(goal1.getClass().equals(goal2.getClass())) {
            if(goal1 instanceof MethodCoverageTestFitness) {
                return getGoalPairName((MethodCoverageTestFitness) goal1, (MethodCoverageTestFitness) goal2);
            }
            if(goal1.getTargetClass().equals(goal2.getTargetClass()) && goal1.getTargetMethod().equals(goal2.getTargetMethod())) {
                if (goal1 instanceof InputCoverageTestFitness) {
                    return getGoalPairName((InputCoverageTestFitness) goal1, (InputCoverageTestFitness) goal2);
                } else if (goal1 instanceof OutputCoverageTestFitness) {
                    return getGoalPairName((OutputCoverageTestFitness) goal1, (OutputCoverageTestFitness) goal2);
                }
            }
        }
        return getGoalName(goal1) + "And" + getGoalName(goal2);
    }

    /**
     * Determine name for pair of method goals
     * @param goal1
     * @param goal2
     * @return
     */
    private String getGoalPairName(MethodCoverageTestFitness goal1, MethodCoverageTestFitness goal2) {
        boolean isConstructor1 = goal1.getTargetMethod().startsWith("<init>");
        boolean isConstructor2 = goal2.getTargetMethod().startsWith("<init>");

        if(isConstructor1 != isConstructor2) {
            if(isConstructor1)
                return getGoalName(goal1) + "AndCalls" + getGoalName(goal2);
            else
                return getGoalName(goal2) + "AndCalls" + getGoalName(goal1);
        } else {
            return getGoalName(goal1) + "And" + getGoalName(goal2);
        }
    }

    /**
     * Determine name for pair of input goals
     *
     * @param goal1
     * @param goal2
     * @return
     */
    private String getGoalPairName(InputCoverageTestFitness goal1, InputCoverageTestFitness goal2) {
        return formatMethodName(goal1.getClassName(), goal1.getMethod()) + STR_WITH + formatValueDescriptor(goal1.getValueDescriptor()) + "And" + formatValueDescriptor(goal2.getValueDescriptor());
    }

    /**
     * Determine name for pair of output goals
     *
     * @param goal1
     * @param goal2
     * @return
     */
    private String getGoalPairName(OutputCoverageTestFitness goal1, OutputCoverageTestFitness goal2 ) {
        return formatMethodName(goal1.getClassName(), goal1.getMethod()) + STR_RETURNS + formatValueDescriptor(goal1.getValueDescriptor()) + "And" + formatValueDescriptor(goal2.getValueDescriptor());
    }

    /**
     * Make sure package names are omitted and array brackets are not used in names
     * @param className
     * @return
     */
    private String getShortClassName(String className) {
        int pos = className.lastIndexOf(".");
        if(pos >= 0)
            return className.substring(pos+1).replace("[]", "Array");
        else
            return className.replace("[]", "Array");
    }

    /**
     * Distinguish between constructors and methods in creating call goals
     *
     * @param className
     * @param method
     * @return
     */
    private String formatMethodName(String className, String method) {
        if(method.startsWith("<init>")) {
            String methodWithoutDescriptor = getMethodNameWithoutDescriptor(method);
            if(methodCount.containsKey(methodWithoutDescriptor) && methodCount.get(methodWithoutDescriptor).size() > 1) {
                return STR_CREATE + capitalize(getUniqueConstructorName(getShortClassName(className), method));
            } else {
                return STR_CREATE + capitalize(getShortClassName(className));
            }
        }
        else {
            String methodWithoutDescriptor = getMethodNameWithoutDescriptor(method);
            if(methodCount.containsKey(methodWithoutDescriptor) && methodCount.get(methodWithoutDescriptor).size() > 1) {
                return capitalize(getUniqueMethodName(methodWithoutDescriptor, method));
            }
            else {
                return capitalize(methodWithoutDescriptor);
            }
        }
    }

    /**
     * Get a unique method name, depending on whether it is unique or overloaded
     * @param methodNameWithoutDescriptor
     * @param methodName
     * @return
     */
    private String getUniqueMethodName(String methodNameWithoutDescriptor, String methodName) {
        if(!methodCount.containsKey(methodNameWithoutDescriptor))
            return methodNameWithoutDescriptor;
        if(methodCount.get(methodNameWithoutDescriptor).size() == 1)
            return methodNameWithoutDescriptor;
        int pos = methodName.indexOf('(');
        if(pos < 0) {
            return methodName; // TODO: Should this really be possible?
        }
        String descriptor = methodName.substring(pos);
        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        // TODO: Dummy for now
        if(argumentTypes.length == 0)
            return methodNameWithoutDescriptor + STR_WITHOUT + STR_ARGUMENTS;
        else if(argumentTypes.length == 1) {
            return methodNameWithoutDescriptor + STR_WITH + capitalize(getShortClassName(argumentTypes[0].getClassName()));
        }
        else
            return methodNameWithoutDescriptor + STR_WITH + argumentTypes.length + STR_ARGUMENTS;
    }

    /**
     * Get a unique constructor name, depending on whether it is unique or overloaded
     * @param className
     * @param methodName
     * @return
     */
    private String getUniqueConstructorName(String className, String methodName) {
        if(!methodCount.containsKey("<init>"))
            return getShortClassName(className);
        if(methodCount.get("<init>").size() == 1)
            return getShortClassName(className);
        int pos = methodName.indexOf('(');
        if(pos < 0) {
            return getShortClassName(className); // TODO: Should this really be possible?
        }
        String descriptor = methodName.substring(pos);
        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        // TODO: Dummy for now
        if(argumentTypes.length == 0)
            return getShortClassName(className) + STR_WITHOUT + STR_ARGUMENTS;
        else if(argumentTypes.length == 1) {
            return getShortClassName(className) + STR_WITH + capitalize(getShortClassName(argumentTypes[0].getClassName()));
        }
        else
            return getShortClassName(className) + STR_WITH + argumentTypes.length + STR_ARGUMENTS;
    }

    /**
     * Cut off descriptor from method name
     * @param methodName
     * @return
     */
    private String getMethodNameWithoutDescriptor(String methodName) {
        // Should have a descriptor
        int pos = methodName.indexOf('(');
        if(pos > 0)
            return methodName.substring(0, pos);
        else
            return methodName;

    }

    @Override
    public String getName(TestCase test) {
        return testToName.get(test);
    }
}

