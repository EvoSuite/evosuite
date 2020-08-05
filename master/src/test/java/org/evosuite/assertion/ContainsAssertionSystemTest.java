package org.evosuite.assertion;

import com.examples.with.different.packagename.assertion.ContainerExample;
import org.evosuite.EvoSuite;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestCase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class ContainsAssertionSystemTest extends SystemTestBase {

    @Test
    public void testAssertionsIncludeContains() {

        //Properties.INLINE = false;
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ContainerExample.class.getCanonicalName();

        String[] command = new String[] {
                "-generateSuite", "-class", targetClass, "-Dassertion_strategy=all" };

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome suite = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println(suite.toString());

        Assert.assertTrue(suite.size() > 0);
        for (TestCase test : suite.getTests()) {
            boolean hasContainsAssertion = false;
            for(Assertion ass : test.getAssertions()) {
                if(ass instanceof ContainsAssertion) {
                    hasContainsAssertion = true;
                }
            }
            Assert.assertTrue("Test has no contains assertions: " + test.toCode(),
                    hasContainsAssertion);
        }
    }
}
