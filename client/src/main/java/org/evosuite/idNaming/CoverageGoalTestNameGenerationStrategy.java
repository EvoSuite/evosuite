package org.evosuite.idNaming;

import org.evosuite.Properties;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.input.InputCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.method.MethodNoExceptionCoverageTestFitness;
import org.evosuite.coverage.output.OutputCoverageTestFitness;
import org.evosuite.runtime.mock.EvoSuiteMock;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
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

    public CoverageGoalTestNameGenerationStrategy(List<TestCase> testCases, List<ExecutionResult> results) {
        addGoalsNotIncludedInTargetCriteria(results);
        Map<TestCase, Set<TestFitnessFunction>> testToGoals = initializeCoverageMapFromResults(results);
        generateNames(testToGoals);
    }

    public CoverageGoalTestNameGenerationStrategy(List<TestCase> testCases) {
        // This assumes all goals are already saved in the tests
        Map<TestCase, Set<TestFitnessFunction>> testToGoals = initializeCoverageMapFromTests(testCases);
        generateNames(testToGoals);
    }


    private void generateNames(Map<TestCase, Set<TestFitnessFunction>> testToGoals ) {
        initializeMethodCoverageCount(testToGoals);
        findUniqueGoals(testToGoals);
        selectGoalName(testToGoals);
        fixAmbiguousTestNames();
    }

    private Map<TestCase, Set<TestFitnessFunction>> initializeCoverageMapFromTests(List<TestCase> tests) {
        Map<TestCase, Set<TestFitnessFunction>> testToGoals = new HashMap<>();
        for(TestCase test : tests) {
            testToGoals.put(test, filterSupportedGoals(new HashSet<>(test.getCoveredGoals())));
        }
        return testToGoals;
    }

    private Map<TestCase, Set<TestFitnessFunction>> initializeCoverageMapFromResults(List<ExecutionResult> results) {
        Map<TestCase, Set<TestFitnessFunction>> testToGoals = new HashMap<>();
        for(ExecutionResult result : results) {
            testToGoals.put(result.test, filterSupportedGoals(new HashSet<>(result.test.getCoveredGoals())));
        }
        return testToGoals;
    }

    private void addGoalsNotIncludedInTargetCriteria(List<ExecutionResult> results) {
        List<Properties.Criterion> requiredCriteria = new ArrayList<>(Arrays.asList(new Properties.Criterion[] { Properties.Criterion.OUTPUT, Properties.Criterion.INPUT, Properties.Criterion.METHOD, Properties.Criterion.METHODNOEXCEPTION, Properties.Criterion.EXCEPTION}));
        requiredCriteria.removeAll(Arrays.asList(Properties.CRITERION));
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

    private List<Class<?>> supportedClasses = Arrays.asList(new Class<?> [] { MethodCoverageTestFitness.class, MethodNoExceptionCoverageTestFitness.class,
            ExceptionCoverageTestFitness.class, OutputCoverageTestFitness.class, InputCoverageTestFitness.class});

    private Set<TestFitnessFunction> filterSupportedGoals(Set<TestFitnessFunction> goals) {
        return goals.stream().filter(c -> supportedClasses.contains(c.getClass())).collect(Collectors.toSet());
    }

    private void initializeMethodCoverageCount(Map<TestCase, Set<TestFitnessFunction>> testToGoals) {
        for(Set<TestFitnessFunction> goals : testToGoals.values()) {
            for(TestFitnessFunction goal : goals) {
                if(goal instanceof MethodCoverageTestFitness) {
                    String methodName = getMethodNameWithoutDescriptor(goal.getTargetMethod());
                    if(!methodCount.containsKey(methodName)) {
                        methodCount.put(methodName, new HashSet<String>());
                    }
                    methodCount.get(methodName).add(goal.getTargetMethod());
                }
            }
        }
    }

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

    private static String capitalize(String input) {
        final char[] buffer = input.toCharArray();
        buffer[0] = Character.toTitleCase(buffer[0]);
        return new String(buffer);
    }

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

    private String getGoalName(MethodCoverageTestFitness goal) {
        return formatMethodName(goal.getClassName(), goal.getMethod());
    }

    private String getGoalName(MethodNoExceptionCoverageTestFitness goal) {
        return formatMethodName(goal.getClassName(), goal.getMethod());
    }

    private String getGoalName(ExceptionCoverageTestFitness goal) {
        Class<?> ex = goal.getExceptionClass();
        while (!Modifier.isPublic(ex.getModifiers()) || EvoSuiteMock.class.isAssignableFrom(ex) ||
                ex.getCanonicalName().startsWith("com.sun.")) {
            ex = ex.getSuperclass();
        }

        if(goal.getTargetMethod().startsWith("<init>")) {
            return "FailsToGenerate" + capitalize(getShortClassName(goal.getTargetClass()))+ "Throws" + capitalize(ex.getSimpleName());
        }
        return formatMethodName(goal.getTargetClass(), goal.getTargetMethod()) + "Throws" + capitalize(ex.getSimpleName());
    }

    private String getGoalName(InputCoverageTestFitness goal) {
        return formatMethodName(goal.getClassName(), goal.getMethod()) + "With" + capitalize(getShortClassName(goal.getValueDescriptor()));
    }

    private String getGoalName(OutputCoverageTestFitness goal) {
        return formatMethodName(goal.getClassName(), goal.getMethod()) + "Returning" + capitalize(getShortClassName(goal.getValueDescriptor()));
    }

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

    private String getGoalPairName(InputCoverageTestFitness goal1, InputCoverageTestFitness goal2) {
        return formatMethodName(goal1.getClassName(), goal1.getMethod()) + "With" + capitalize(getShortClassName(goal1.getValueDescriptor())) + "And" + capitalize(getShortClassName(goal2.getValueDescriptor()));
    }

    private String getGoalPairName(OutputCoverageTestFitness goal1, OutputCoverageTestFitness goal2 ) {
        return formatMethodName(goal1.getClassName(), goal1.getMethod()) + "Returning" + capitalize(getShortClassName(goal1.getValueDescriptor())) + "And" + capitalize(getShortClassName(goal2.getValueDescriptor()));
    }

    private String getShortClassName(String className) {
        int pos = className.lastIndexOf(".");
        if(pos >= 0)
            return className.substring(pos+1).replace("[]", "Array");
        else
            return className.replace("[]", "Array");
    }

    private String formatMethodName(String className, String method) {
        if(method.startsWith("<init>")) {
            String methodWithoutDescriptor = getMethodNameWithoutDescriptor(method);
            if(methodCount.containsKey(methodWithoutDescriptor) && methodCount.get(methodWithoutDescriptor).size() > 1) {
                return "Generates" + capitalize(getUniqueConstructorName(getShortClassName(className), method));
            } else {
                return "Generates" + capitalize(getShortClassName(className));
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

    private String getUniqueMethodName(String methodNameWithoutDescriptor, String methodName) {
        if(!methodCount.containsKey(methodNameWithoutDescriptor))
            return methodNameWithoutDescriptor;
        int pos = methodName.indexOf('(');
        if(pos < 0) {
            return methodName; // TODO: Should this really be possible?
        }
        String descriptor = methodName.substring(pos);
        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        // TODO: Dummy for now
        if(argumentTypes.length == 0)
            return methodNameWithoutDescriptor + "WithoutArguments";
        else if(argumentTypes.length == 1) {
            return methodNameWithoutDescriptor + "With" + capitalize(getShortClassName(argumentTypes[0].getClassName()));
        }
        else
            return methodNameWithoutDescriptor + "With" + argumentTypes.length + "Arguments";
    }

    private String getUniqueConstructorName(String className, String methodName) {
        if(!methodCount.containsKey("<init>"))
            return className;
        int pos = methodName.indexOf('(');
        if(pos < 0) {
            return className; // TODO: Should this really be possible?
        }
        String descriptor = methodName.substring(pos);
        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        // TODO: Dummy for now
        if(argumentTypes.length == 0)
            return className + "WithoutArguments";
        else if(argumentTypes.length == 1) {
            return className + "With" + capitalize(getShortClassName(argumentTypes[0].getClassName()));
        }
        else
            return className + "With" + argumentTypes.length + "Arguments";
    }


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

