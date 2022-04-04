package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
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
 * 1 - Create an empty LinkedHashMap: key - position of the statement on which a constructor is invoked to create
 * 2 - Iterate over the statements of a test case
 * [2: Start loop]
 * 3 - Verify if the current statement is an instance of ConstructorStatement
 * 4 (3 is True):
 *    4.1 - If the return value of the current statement is equal to "File":
 * 5 (3 is False):
 *    5.1 -
 * [2: End loop]
 * 6 - Return the smell counter
 */
public class ResourceOptimism extends AbstractNormalizedTestCaseSmell {

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

                String methodName = ((MethodStatement) currentStatement).getMethodName();
                VariableReference callee = ((MethodStatement) currentStatement).getCallee();

                if (callee != null) {
                    position = callee.getStPosition();

                    if (filesUsedByTestCase.containsKey(position)) {
                        if (optimismMethods.contains(methodName)) {
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
            }
        }

        return count;
    }
}
