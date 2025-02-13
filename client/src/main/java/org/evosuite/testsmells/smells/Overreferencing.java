package org.evosuite.testsmells.smells;

import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Definition:
 * Test cases that reference classes an excessive number of times.
 *
 * Adaptation:
 * Test cases that create unnecessary instances of classes (i.e., class instances that are created but never used).
 *
 * Metric:
 * Number of unnecessary class instances (i.e., class instances that are created but never used).
 *
 * Computation:
 * 1 - Create an empty LinkedHashSet: elements - position of the statement on which a constructor is invoked to create a new object
 * 2 - Let S = {S1,...,Sn} be the set of n statements in a test case
 * 3 - Iterate over S and, for each statement Si:
 * [3: Start loop]
 * 4 - Verify if Si corresponds to a constructor statement (instance of ConstructorStatement)
 * 5 (4 is True):
 *    5.1 - Add i to the LinkedHashSet
 *    5.2 - All the elements of the LinkedHashSet passed as arguments to the current constructor are removed from the LinkedHashSet
 * 6 - Verify if Si corresponds to a method statement (instance of MethodStatement)
 * 7 (6 is True):
 *    7.1 - If the LinkedHashSet contains the position of the object on which the current method is invoked: remove the
 *          respective position from the LinkedHashSet
 *    7.2 - All the elements of the LinkedHashSet passed as arguments to the current method are removed from the LinkedHashSet
 * 8 - Verify if Si corresponds to an assignment statement (instance of AssignmentStatement)
 * 9 (8 is True):
 *    9.1 - If the LinkedHashSet contains the object assigned to the array: remove the respective position from the LinkedHashSet
 * 10 - Verify if Si has assertions
 * 11 (10 is True):
 *    11.1 - Let A = {A1,...,Ak} be the set of k assertions in Si
 *    11.2 - Iterate over A of Si and, for each assertion Aj:
 *    [11.2: Start loop]
 *    11.3 - If Aj corresponds to an inspector assertion: get the object of the class on which the assertion is made
 *           and remove the respective position from the LinkedHashSet
 *    [11.2: End loop]
 * [3: End loop]
 * 12 - Return the number of elements in the LinkedHashSet
 */
public class Overreferencing extends AbstractNormalizedTestCaseSmell {

    private static final long serialVersionUID = -2704479762072190181L;

    public Overreferencing() {
        super("TestSmellOverreferencing");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        Statement currentStatement;

        Set<Integer> setOfInstances = new LinkedHashSet<>();

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            if(currentStatement instanceof ConstructorStatement){
                setOfInstances.add(i);

                List<VariableReference> parameters = ((ConstructorStatement) currentStatement).getParameterReferences();

                // Verify if an object is passed as an argument
                for(VariableReference parameter : parameters){
                    setOfInstances.remove(parameter.getStPosition());
                }
            }

            if (currentStatement instanceof MethodStatement) {
                List<VariableReference> parameters = ((MethodStatement) currentStatement).getParameterReferences();
                VariableReference callee = ((MethodStatement) currentStatement).getCallee();

                // Verify if a method of an object is called
                if(callee != null){
                    setOfInstances.remove(callee.getStPosition());
                }

                // Verify if an object is passed as an argument
                for(VariableReference parameter : parameters){
                    setOfInstances.remove(parameter.getStPosition());
                }
            }

            if(currentStatement instanceof AssignmentStatement) {
                setOfInstances.remove(((AssignmentStatement) currentStatement).getValue().getStPosition());
            }

            if(currentStatement.hasAssertions()){

                Set<Assertion> assertions = currentStatement.getAssertions();

                // Verify if a method of an object is called
                for(Assertion assertion : assertions){
                    if(assertion instanceof InspectorAssertion){
                        setOfInstances.remove(assertion.getSource().getStPosition());
                    }
                }
            }
        }

        return setOfInstances.size();
    }
}
