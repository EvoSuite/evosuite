package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.List;
import java.util.Set;

/**
 * Definition:
 * Unrelated test cases are arranged into a test suite.
 *
 * Adaptation:
 * A test suite has a specific target class: the class under test. All test cases in a test suite are supposed to
 * perform tests on the class under test and, in that sense, should also be related. Therefore, instead of verifying
 * if all test cases perform tests on a common class, in this context, a test case is considered smelly if it does not
 * perform tests on the class under test.
 *
 * Metric:
 * Number of test cases in a test suite that do not perform tests on the class under test.
 *
 * Computation:
 * 1 - Initialize the smell counter with the number of test cases in the test suite (start by assuming that none of
 *     the test cases perform tests on the class under test)
 * 2 - Iterate over the test cases of a test suite
 * [2: Start loop]
 * 3 - Get the set of classes accessed by the current test case
 * 4 - Iterate over the accessed classes
 * [4: Start loop]
 * 5 - If the current accessed class has the same name as the target class: decrement the smell counter and stop the
 *     loop iterations (loop: 4)
 * [4: End loop]
 * [2: End loop]
 * 6 - Return the normalized value for the smell counter
 */
public class LackOfCohesionOfMethods extends AbstractTestSmell {

    private static final long serialVersionUID = -9172012714593111239L;

    public LackOfCohesionOfMethods() {
        super("TestSmellLackOfCohesionOfMethods");
    }

    @Override
    public double computeTestSmellMetric(TestSuiteChromosome chromosome) {
        List<TestChromosome> testChromosomes = chromosome.getTestChromosomes();
        int count = testChromosomes.size();
        String targetClass = Properties.TARGET_CLASS;

        for(TestChromosome testCase : testChromosomes){
            Set<Class<?>> accessedClasses = testCase.getTestCase().getAccessedClasses();

            for(Class<?> accessedClass : accessedClasses) {
                if(accessedClass.getCanonicalName().equals(targetClass)){
                    count--;
                    break;
                }
            }
        }

        return FitnessFunction.normalize(count);
    }
}
