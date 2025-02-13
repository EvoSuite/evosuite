package org.evosuite.basic;

import com.examples.with.different.packagename.TargetMethod;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class TargetMethodListSystemTest extends SystemTestBase {

    @Test
    public void testTargetMethodWithLineCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "foo(Ljava/lang/Integer;)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD_LIST = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH, Properties.Criterion.LINE};
        String[] command = new String[]{"-generateMOSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Assertions.assertFalse(best.toString().contains("bar"));
    }

    @Test
    public void testTargetMethodWithWeakMutation() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "foo(Ljava/lang/Integer;)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD_LIST = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH, Properties.Criterion.WEAKMUTATION};
        String[] command = new String[]{"-generateMOSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Assertions.assertFalse(best.toString().contains("bar"));
    }

    @Test
    public void testTargetMethodWithMethod() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "foo(Ljava/lang/Integer;)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD_LIST = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH, Properties.Criterion.METHOD};
        String[] command = new String[]{"-generateMOSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Assertions.assertFalse(best.toString().contains("bar"));
    }

    @Test
    public void testTargetMethodWithException() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "foo(Ljava/lang/Integer;)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD_LIST = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH, Properties.Criterion.EXCEPTION};
        String[] command = new String[]{"-generateMOSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Assertions.assertFalse(best.toString().contains("bar"));
    }

    @Test
    public void testTargetMethodWithMethodNoException() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "foo(Ljava/lang/Integer;)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD_LIST = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH, Properties.Criterion.METHODNOEXCEPTION};
        String[] command = new String[]{"-generateMOSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Assertions.assertFalse(best.toString().contains("bar"));
    }

    @Test
    public void testTargetMethodWithCBranch() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "foo(Ljava/lang/Integer;)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD_LIST = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH, Properties.Criterion.CBRANCH};
        String[] command = new String[]{"-generateMOSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Assertions.assertFalse(best.toString().contains("bar"));
    }

    @Test
    public void testTargetMethodWithOutput() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "foo(Ljava/lang/Integer;)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD_LIST = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH, Properties.Criterion.OUTPUT};
        String[] command = new String[]{"-generateMOSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Assertions.assertFalse(best.toString().contains("bar"));
    }
}
