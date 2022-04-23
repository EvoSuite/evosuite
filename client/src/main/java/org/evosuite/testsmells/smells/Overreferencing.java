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
 * Test cases that create unnecessary instances of classes from the application code.
 *
 * Metric:
 * Total number of unnecessary class instances (i.e., class instances that are created but never used).
 *
 * Computation:
 * 1 - Create an empty LinkedHashSet: elements - position of the statement on which a constructor is invoked to create
 *     a new object
 * 2 - Iterate over the statements of a test case
 * [2: Start loop]
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
 * 7 - Verify if the current statement has assertions
 * 8 (7 is True):
 *    8.1 - Iterate over the assertions of the current statement
 *    8.2 - If the current assertion is an instance of InspectorAssertion: get the object of the class on which the
 *          assertion is made and remove the respective element from the LinkedHashSet
 * [2: End loop]
 * 9 - Return the number of elements in the LinkedHashSet
 */
public class Overreferencing extends AbstractNormalizedTestCaseSmell {

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

    /*
    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        Statement currentStatement;

        Set<Integer> setOfInstances = new LinkedHashSet<>();

        LoggingUtils.getEvoLogger().info("--------------- Start ---------------");

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            if(currentStatement instanceof ConstructorStatement){
                setOfInstances.add(i);

                LoggingUtils.getEvoLogger().info("Constructor Statement - " + i + ": " + currentStatement.toString());

                List<VariableReference> parameters = ((ConstructorStatement) currentStatement).getParameterReferences();

                // Verify if an object is passed as an argument
                for(VariableReference parameter : parameters){
                    setOfInstances.remove(parameter.getStPosition());
                }
            }

            if (currentStatement instanceof MethodStatement) {
                List<VariableReference> parameters = ((MethodStatement) currentStatement).getParameterReferences();
                VariableReference callee = ((MethodStatement) currentStatement).getCallee();

                //LoggingUtils.getEvoLogger().info("Method Statement: " + currentStatement.toString());

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

                //LoggingUtils.getEvoLogger().info("Has Assertion: " + currentStatement.toString());

                // Verify if a method of an object is called
                for(Assertion assertion : assertions){
                    if(assertion instanceof InspectorAssertion){
                        setOfInstances.remove(assertion.getSource().getStPosition());
                    }
                }
            }
        }

        LoggingUtils.getEvoLogger().info("\n |---| |++++++++++++++++++++++++++++++| |---| \n");

        if(setOfInstances.size() > 0){

            TestCodeVisitor visitor = new TestCodeVisitor();

            TestCase testCase = chromosome.getTestCase();
            visitor.visitTestCase(testCase);

            for (int i = 0; i < size; i++){
                visitor.visitStatement(testCase.getStatement(i));
            }

            LoggingUtils.getEvoLogger().info(visitor.getCode());

            LoggingUtils.getEvoLogger().info("\n");

            LoggingUtils.getEvoLogger().info("Size = " + size);

            LoggingUtils.getEvoLogger().info("\n");

            for(Integer current : setOfInstances){
                LoggingUtils.getEvoLogger().info("Smelly Constructor Statement -> " + current + ": " + chromosome.getTestCase().getStatement(current));
            }
        }

        LoggingUtils.getEvoLogger().info("--------------- End ---------------");

        return setOfInstances.size();
    }
     */
}
