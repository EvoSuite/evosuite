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
 * 1 - Create a smell counter and initialize the variable with the value 0
 * 2 - Create an empty LinkedHashMap: key - position of the statement on which a constructor is invoked to create a new
 *     File object; value - boolean initialized with "false" (the value is only set to "true" after verifying if the file exists)
 * 3 - Let S = {S1,...,Sn} be the set of n statements in a test case
 * 4 - Iterate over S and, for each statement Si:
 * [4: Start loop]
 * 5 - Verify if Si corresponds to a constructor statement (instance of ConstructorStatement)
 * 6 (5 is True):
 *    6.1 - If the return value of Si corresponds to an instance of the "File" class: add the element to the LinkedHashMap
 *    6.2 - Iterate over the respective parameters
 *    [6.2: Start loop]
 *    6.3 - If an element of the LinkedHashSet with value "false" is passed as an argument to Si: increment the smell counter
 *    [6.2: End loop]
 * 7 - Verify if Si corresponds to a method statement (instance of MethodStatement)
 * 8 (7 is True):
 *    8.1 - If the return value of Si corresponds to an instance of the "File" class: add the element to the LinkedHashMap
 *    8.2 - Verify if an instance of the "File" class is used:
 *    8.3 (8.2 is True):
 *       8.3.1 - If the current method checks whether this file exists, set the value of the respective key to
 *               "true"; otherwise, if the LinkedHashSet contains the respective key and the value is "false": increment the smell counter
 *    8.4 - Iterate over the respective parameters
 *    [8.4: Start loop]
 *    8.5 - If an element of the LinkedHashSet with value "false" is passed as an argument to the current method: increment the smell counter
 *    [8.4: End loop]
 * 9 - Verify if Si corresponds to an assignment statement (instance of AssignmentStatement)
 * 10 (9 is True):
 *    10.1 - If an element of the LinkedHashSet with value "false" is assigned to the array: increment the smell counter
 * [4: End loop]
 * 11 - Return the smell counter
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
