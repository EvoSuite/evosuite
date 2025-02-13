package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

import java.util.List;

/**
 * Definition:
 * A test case has object comparisons that will never fail.
 *
 * Metric:
 * Number of times the "equals" method of a class other than the one under test is used to compare an object with itself.
 *
 * Computation:
 * 1 - Create a smell counter and initialize the variable with the value 0
 * 2 - Let S = {S1,...,Sn} be the set of n statements in a test case
 * 3 - Iterate over S and, for each statement Si:
 * [3: Start loop]
 * 4 - Verify if: (1) Si corresponds to a method statement named "equals" and (2) the class that declares this method
 *     is different from the class under test
 * 5 (4 is True):
 *    5.1 - If the "equals" method is used to compare two objects which were created in the same position of the
 *          test case (i.e., an object is being compared with itself): increment the smell counter
 * [3: End loop]
 * 6 - Return the smell counter
 */
public class LikelyIneffectiveObjectComparison extends AbstractNormalizedTestCaseSmell {

    private static final long serialVersionUID = 6458566710107014031L;

    public LikelyIneffectiveObjectComparison() {
        super("TestSmellLikelyIneffectiveObjectComparison");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        long count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            if(currentStatement instanceof MethodStatement){

                String curr = ((MethodStatement) currentStatement).getMethodName();
                String className = ((MethodStatement) currentStatement).getDeclaringClassName();

                if(curr.equals("equals") && !className.equals(Properties.TARGET_CLASS)){
                    VariableReference callee = ((MethodStatement) currentStatement).getCallee();
                    List<VariableReference> parameters = ((MethodStatement) currentStatement).getParameterReferences();

                    if(parameters.size() == 1 && callee != null && callee.getStPosition() == parameters.get(0).getStPosition()){
                        count++;
                    }
                }
            }
        }

        return count;
    }
}
