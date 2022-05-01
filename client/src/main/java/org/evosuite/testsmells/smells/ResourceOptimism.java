package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

import java.util.*;

/**
 * Definition:
 * Tests make optimistic assumptions about the existence/state of external resources.
 *
 * Metric:
 * The number of times an instance of a File class is used without first calling one of the following
 * methods: exists(); notExists(); isFile().
 *
 * Computation:
 * 1 - Create an empty LinkedHashMap: key - position of the statement on which a constructor is invoked to create a new
 *     File object; value - boolean initialized with "false" (the value is only set to "true" after verifying if the file exists)
 * 2 - Iterate over the statements of a test case
 * [2: Start loop]
 * 3 - Verify if the current statement is an instance of ConstructorStatement
 * 4 (3 is True):
 *    4.1 - If the return value of the current statement corresponds to an instance of the "File" class: add the element to the LinkedHashMap
 *    4.2 - Iterate over the respective parameters
 *    [4.2: Start loop]
 *    4.3 - If an element of the LinkedHashSet with value "false" is passed as an argument to the current constructor: increment the smell counter
 *    [4.2: End loop]
 * 5 - Verify if the current statement is an instance of MethodStatement
 * 6 (5 is True):
 *    6.1 - If the return value of the current statement corresponds to an instance of the "File" class: add the element to the LinkedHashMap
 *    6.2 - Verify if an instance of the "File" class is used
 *    6.3 (6.2 is True):
 *       6.3.1 - Verify if the current method checks whether this file exists
 *       6.3.2 (6.3.1 is True):
 *          6.3.2.1 - Set value of the respective element to "true"
 *       6.3.3 (6.3.1 is False):
 *          6.3.3.1 - If the LinkedHashSet contains the respective element and the value is "false": increment the smell counter
 *    6.4 - Iterate over the respective parameters
 *    [6.4: Start loop]
 *    6.5 - If an element of the LinkedHashSet with value "false" is passed as an argument to the current method: increment the smell counter
 *    [6.4: End loop]
 * [2: End loop]
 * 6 - Return the smell counter
 */
public class ResourceOptimism extends AbstractNormalizedTestCaseSmell {

    private static final long serialVersionUID = -6278974287695553451L;

    public ResourceOptimism() {
        super("TestSmellResourceOptimism");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        long count = 0;

        Statement currentStatement;
        int position;

        LinkedHashMap<Integer, Boolean> filesUsedByTestCase = new LinkedHashMap<>();

        List<String> optimismMethods = new ArrayList<>(
                Arrays.asList(
                        "exists",
                        "isFile",
                        "notExists"
                ));

        for (int i = 0; i < size; i++) {
            currentStatement = chromosome.getTestCase().getStatement(i);

            if (currentStatement instanceof ConstructorStatement) {

                if (currentStatement.getReturnClass().getSimpleName().equals("File")) {
                    filesUsedByTestCase.put(i, false);
                }

                List<VariableReference> parameters = ((ConstructorStatement) currentStatement).getParameterReferences();

                // Verify if a file is passed as an argument
                for (VariableReference parameter : parameters) {
                    position = parameter.getStPosition();
                    if (filesUsedByTestCase.containsKey(position) && !filesUsedByTestCase.get(position) && position != i) {
                        count++;
                    }
                }

            } else if (currentStatement instanceof MethodStatement) {

                if (currentStatement.getReturnClass().getSimpleName().equals("File")) {
                    filesUsedByTestCase.put(i, false);
                }

                VariableReference callee = ((MethodStatement) currentStatement).getCallee();

                if (callee != null) {
                    position = callee.getStPosition();

                    if (filesUsedByTestCase.containsKey(position)) {
                        if (optimismMethods.contains(((MethodStatement) currentStatement).getMethodName())) {
                            filesUsedByTestCase.put(position, true);
                        } else if (!filesUsedByTestCase.get(position)) {
                            count++;
                        }
                    }
                }

                List<VariableReference> parameters = ((MethodStatement) currentStatement).getParameterReferences();

                for (VariableReference parameter : parameters) {
                    position = parameter.getStPosition();
                    if (filesUsedByTestCase.containsKey(position) && !filesUsedByTestCase.get(position) && position != i) {
                        count++;
                    }
                }

            } else if (currentStatement instanceof AssignmentStatement) {
                position = ((AssignmentStatement) currentStatement).getValue().getStPosition();
                if (filesUsedByTestCase.containsKey(position) && !filesUsedByTestCase.get(position) && position != i) {
                    count++;
                }
            }
        }

        return count;
    }
}
