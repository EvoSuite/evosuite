package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.AbstractTestCaseSmell;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Definition:
 * Test cases that create unnecessary instances of classes from the application code.
 *
 * Metric:
 * Count the total number of unnecessary class instances (i.e., class instances that are created but never used).
 *
 * Computation:
 * 1 - Create an empty LinkedHashSet: elements - position of the statement on which a constructor is invoked to create
 *     a new object
 * 2 - Iterate over the statements of a test case
 * 3 - Verify if the current statement is an instance of ConstructorStatement
 * 4 (3 is True):
 *    4.1 - Add the element to the LinkedHashSet
 *    4.2 - All the elements of the LinkedHashSet that are passed as arguments to the current constructor are removed
 *          from the LinkedHashSet
 * 5 - Verify if the current statement is an instance of MethodStatement
 * 6 (5 is True):
 *    6.1 - If the LinkedHashSet contains the object on which the current method is invoked: remove the element from
 *          the LinkedHashSet
 *    6.2 - All the elements of the LinkedHashSet that are passed as arguments to the current method are removed from
 *          the LinkedHashSet
 * 7 - Return the number of elements in the LinkedHashSet
 */
public class Overreferencing extends AbstractTestCaseSmell {

    public Overreferencing() {
        super("TestSmellOverreferencing");
    }

    @Override
    public double computeNumberOfTestSmells(TestChromosome chromosome) {
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
        }

        return setOfInstances.size();
    }
}
