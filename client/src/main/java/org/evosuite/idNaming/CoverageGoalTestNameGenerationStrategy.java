package org.evosuite.idNaming;

import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.input.InputCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.method.MethodNoExceptionCoverageTestFitness;
import org.evosuite.coverage.output.OutputCoverageTestFitness;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

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
        Map<TestCase, Set<TestFitnessFunction>> testToGoals = initializeCoverageMapFromResults(results);
        generateNames(testToGoals);
    }

    public CoverageGoalTestNameGenerationStrategy(List<TestCase> testCases) {
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
                testToName.put(entry.getKey(), getTestName(entry.getKey().getCoveredGoals()));

            } else {
                testToName.put(entry.getKey(), getTestName(entry.getValue()));
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

    private String getTestName(Set<TestFitnessFunction> uniqueGoals) {
        List<TestFitnessFunction> goalList = new ArrayList<>(uniqueGoals);
        Collections.sort(goalList, new GoalComparator());
        String name = PREFIX + capitalize(getGoalName(goalList.get(0)));
        return name;
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
        if(goal.getTargetMethod().startsWith("<init>")) {
            return "FailsToGenerate" + capitalize(goal.getTargetClass())+ "Throws" + capitalize(goal.getExceptionClass().getSimpleName());
        }
        return formatMethodName(goal.getTargetClass(), goal.getTargetMethod()) + "Throws" + capitalize(goal.getExceptionClass().getSimpleName());
    }

    private String getGoalName(InputCoverageTestFitness goal) {
        return formatMethodName(goal.getClassName(), goal.getMethod()) + "With" + capitalize(goal.getValueDescriptor());
    }

    private String getGoalName(OutputCoverageTestFitness goal) {
        return formatMethodName(goal.getClassName(), goal.getMethod()) + "Returning" + capitalize(goal.getValueDescriptor());
    }

    private String formatMethodName(String className, String method) {
        if(method.startsWith("<init>"))
            return "Generates"+capitalize(className);
        else {
            String methodWithoutDescriptor = getMethodNameWithoutDescriptor(method);
            if(methodCount.get(methodWithoutDescriptor).size() > 1) {
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
        String descriptor = methodName.substring(methodName.indexOf('('));
        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        // TODO: Dummy for now
        if(argumentTypes.length == 0)
            return methodNameWithoutDescriptor + "WithoutArguments";
        else if(argumentTypes.length == 1) {
            String className = argumentTypes[0].getClassName();
            int pos = className.lastIndexOf('.');
            if(pos > 0)
                className = className.substring(pos+1);
            return methodNameWithoutDescriptor + "With" + capitalize(className);
        }
        else
            return methodNameWithoutDescriptor + "With" + argumentTypes.length + "Arguments";
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

