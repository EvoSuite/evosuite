package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

import java.util.Set;

/**
 * Definition:
 * A test case performs tests on classes other than the one under test.
 *
 * Metric:
 * Total number of methods of other classes that are checked by a test case.
 *
 * Computation:
 * 1 - Create a smell counter and initialize the variable with the value 0
 * 2 - Let S = {S1,...,Sn} be the set of n statements in a test case
 * 3 - Iterate over S and, for each statement Si:
 * [3: Start loop]
 * 4 - Verify if Si corresponds to a method statement (instance of MethodStatement)
 * 5 (4 is True):
 *    5.1 - If the class that declares the method called in Si is different from the class under test: increment the smell counter
 * 6 - Verify if Si has assertions
 * 7 (6 is True):
 *    7.1 - Let A = {A1,...,Ak} be the set of k assertions in Si
 *    7.2 - Iterate over A of Si and, for each assertion Aj:
 *    [7.2: Start loop]
 *    7.3 - Verify if Aj corresponds to an inspector assertion (instance of InspectorAssertion)
 *    7.4 (6.3 is True):
 *       7.4.1 - If the class that declares the method on which the assertion is made is different from the class
 *               under test: increment the smell counter
 *    [7.2: End loop]
 * [3: End loop]
 * 8 - Return the smell counter
 */
public class IndirectTesting extends AbstractNormalizedTestCaseSmell {

    private static final long serialVersionUID = 803733954341223273L;

    public IndirectTesting() {
        super("TestSmellIndirectTesting");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        long count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            if(currentStatement instanceof MethodStatement){
                if(!((MethodStatement) currentStatement).getDeclaringClassName().equals(Properties.TARGET_CLASS)){
                    count++;
                }
            }

            if(currentStatement.hasAssertions()){

                Set<Assertion> assertions = currentStatement.getAssertions();

                for(Assertion assertion : assertions) {
                    if (assertion instanceof InspectorAssertion) {
                        if(!((InspectorAssertion) assertion).getInspector().getMethod().getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }
}
