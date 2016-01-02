package org.evosuite.coverage.method;

import org.evosuite.coverage.io.input.InputCoverageGoal;
import org.evosuite.coverage.io.output.OutputCoverageGoal;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by gordon on 01/01/2016.
 */
public class JUnitObserver {

    private final static Logger logger = LoggerFactory.getLogger(JUnitObserver.class);

    private static JUnitObserver instance = null;

    private JUnitObserver() {}

    private boolean enabled = false;

    private Set<InputCoverageGoal> inputGoals = new LinkedHashSet<>();

    private Set<OutputCoverageGoal> outputGoals = new LinkedHashSet<>();

    private Set<MethodCoverageTestFitness> calledMethods = new LinkedHashSet<>();

    private Set<MethodNoExceptionCoverageTestFitness> calledMethodsNoException = new LinkedHashSet<>();

    public static JUnitObserver getInstance() {
        if(instance == null)
            instance = new JUnitObserver();

        return instance;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        enabled = value;
    }

    public void reset() {
        inputGoals.clear();
        outputGoals.clear();
        calledMethods.clear();
        calledMethodsNoException.clear();
    }

    public static void methodCalled(Object callee, int opcode, String className, String methodName, String methodDesc, Object[] arguments) {
        if(!getInstance().isEnabled())
            return;
        String classNameWithDots = className.replace('/', '.');
        logger.info("Calling method "+className+"."+methodName+" with callee "+callee+" and arguments "+ Arrays.asList(arguments));
        getInstance().calledMethods.add(new MethodCoverageTestFitness(classNameWithDots, methodName+methodDesc));
        getInstance().inputGoals.addAll(InputCoverageGoal.createCoveredGoalsFromParameters(classNameWithDots, methodName, methodDesc, Arrays.asList(arguments)));
    }

    public static void methodReturned(Object retVal, String className, String methodName, String methodDesc) {
        if(!getInstance().isEnabled())
            return;

        String classNameWithDots = className.replace('/', '.');

        logger.info("Method "+className+"."+methodName+" returned: "+retVal);
        getInstance().calledMethodsNoException.add(new MethodNoExceptionCoverageTestFitness(classNameWithDots, methodName+methodDesc));
        getInstance().outputGoals.addAll(OutputCoverageGoal.createGoalsFromObject(classNameWithDots, methodName, methodDesc, retVal));

    }

    public Set<OutputCoverageGoal> getOutputCoverageGoals() {
        return outputGoals;
    }

    public Set<InputCoverageGoal> getInputCoverageGoals() {
        return inputGoals;
    }

    public Set<MethodCoverageTestFitness> getCoveredMethodGoals() {
        return calledMethods;
    }

    public Set<MethodNoExceptionCoverageTestFitness> getCoveredMethodNoExceptionGoals() {
        return calledMethodsNoException;
    }
}
