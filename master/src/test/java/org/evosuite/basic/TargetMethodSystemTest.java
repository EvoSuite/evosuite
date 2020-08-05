package org.evosuite.basic;

import com.examples.with.different.packagename.TargetMethod;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class TargetMethodSystemTest extends SystemTestBase {

    @Test
    public void testTargetMethodWithBranchCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertTrue(best.toString().contains("fooDefUse"));
        Assert.assertFalse(best.toString().contains("barDefUse"));
    }

    @Test
    public void testTargetMethodWithIBranchCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.IBRANCH};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println(best.toString());
        Assert.assertTrue(best.toString().contains("fooDefUse"));
        Assert.assertFalse(best.toString().contains("barDefUse"));
    }

    @Test
    public void testTargetMethodWithCBranchCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.CBRANCH};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertTrue(best.toString().contains("fooDefUse"));
        Assert.assertFalse(best.toString().contains("barDefUse"));
    }

    @Test
    public void testTargetMethodWithOutputCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.OUTPUT};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertTrue(best.toString().contains("fooDefUse"));
        Assert.assertFalse(best.toString().contains("barDefUse"));
    }

    @Test
    public void testTargetMethodWithExceptionCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooException()Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.EXCEPTION};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertTrue(best.toString().contains("fooException"));
        Assert.assertFalse(best.toString().contains("barException"));
    }

    @Test
    public void testTargetMethodWithStrongMutationCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.STRONGMUTATION};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertTrue(best.toString().contains("fooDefUse"));
        Assert.assertFalse(best.toString().contains("barDefUse"));
    }

    @Test
    public void testTargetMethodWithWeakMutationCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.WEAKMUTATION};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertTrue(best.toString().contains("fooDefUse"));
        Assert.assertFalse(best.toString().contains("barDefUse"));
    }

    @Test
    public void testTargetMethodWithMethodTraceCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.METHODTRACE};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertTrue(best.toString().contains("fooDefUse"));
        Assert.assertFalse(best.toString().contains("barDefUse"));
    }

    @Test
    public void testTargetMethodWithMethodCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.METHOD};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertTrue(best.toString().contains("fooDefUse"));
        Assert.assertFalse(best.toString().contains("barDefUse"));
    }

    @Test
    public void testTargetMethodWithMethodNoExceptionCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.METHODNOEXCEPTION};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertTrue(best.toString().contains("fooDefUse"));
        Assert.assertFalse(best.toString().contains("barDefUse"));
    }

    @Test
    public void testTargetMethodWithLineCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.LINE};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertTrue(best.toString().contains("fooDefUse"));
        Assert.assertFalse(best.toString().contains("barDefUse"));
    }

    @Test
    public void testTargetMethodWithOnlyLineCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.ONLYLINE};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertTrue(best.toString().contains("fooDefUse"));
        Assert.assertFalse(best.toString().contains("barDefUse"));
    }


    @Test
    public void testTargetMethodWithInputCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.INPUT};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertTrue(best.toString().contains("fooDefUse"));
        Assert.assertFalse(best.toString().contains("barDefUse"));
    }

    @Ignore
    @Test
    public void testTargetMethodWithALLDEFCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.ALLDEFS};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println(best.toString());
        System.out.println(best.toString().contains("fooDefUse"));
//        Assert.assertFalse(best.toString().contains("barDefUse"));
    }

    @Ignore
    @Test
    public void testTargetMethodWithDEFUSECoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.DEFUSE};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println(best.toString());
        System.out.println(best.toString().contains("fooDefUse"));
//        Assert.assertFalse(best.toString().contains("barDefUse"));
    }

    @Ignore
    @Test
    public void testTargetMethodWithOnlyBranchCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.ONLYBRANCH};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println(best.toString());
        System.out.println(best.toString().contains("fooDefUse"));
//        Assert.assertFalse(best.toString().contains("barDefUse"));
    }

    @Test // No goals generated
    public void testTargetMethodWithTryCatchCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "foo()Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.TRYCATCH};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println(best.toString());
//        Assert.assertTrue(best.toString().contains("foo"));
//        Assert.assertFalse(best.toString().contains("bar"));
    }

    @Ignore
    @Test //TODO: Needs to be fixed
    public void testTargetMethodWithStatementCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.STATEMENT};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println(best.toString());
//        Assert.assertTrue(best.toString().contains("fooDefUse"));
//        Assert.assertFalse(best.toString().contains("barDefUse"));
    }

    @Ignore
    @Test //TODO: Needs to be fixed
    public void testTargetMethodWithMutationCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.MUTATION};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println(best.toString());
//        Assert.assertTrue(best.toString().contains("fooDefUse"));
//        Assert.assertFalse(best.toString().contains("barDefUse"));
    }

    @Ignore
    @Test // Todo: Needs to be fixed
    public void testTargetMethodWithOnlyMutationCoverage() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = TargetMethod.class.getCanonicalName();
        String targetMethod = "fooDefUse(I)Z";
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = targetMethod;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.ONLYMUTATION};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println(best.toString());
//        Assert.assertTrue(best.toString().contains("fooDefUse"));
//        Assert.assertFalse(best.toString().contains("barDefUse"));
    }
}
