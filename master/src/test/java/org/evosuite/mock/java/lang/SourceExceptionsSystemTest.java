package org.evosuite.mock.java.lang;

import com.examples.with.different.packagename.mock.java.lang.ExtendingRuntimeException;
import com.examples.with.different.packagename.mock.java.lang.SourceExceptions;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 19/08/15.
 */
public class SourceExceptionsSystemTest extends SystemTest{

    @Test
    public void testRuntimeException() {
        String targetClass = SourceExceptions.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.REPLACE_CALLS = true;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.LINE, Properties.Criterion.EXCEPTION};

        EvoSuite evosuite = new EvoSuite();
        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        Assert.assertNotNull(best);

        String code = best.toString();
        Assert.assertTrue("Code:\n"+code, code.contains("assertThrownBy(\"SourceExceptions\","));
        Assert.assertTrue("Code:\n"+code, code.contains("assertThrownBy(\"SourceExceptions$Foo\","));
    }
}
