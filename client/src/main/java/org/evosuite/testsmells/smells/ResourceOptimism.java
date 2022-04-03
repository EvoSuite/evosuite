package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

import java.util.*;

public class ResourceOptimism extends AbstractNormalizedTestCaseSmell {

    public ResourceOptimism() {
        super("TestSmellResourceOptimism");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        long count = 0;

        Statement currentStatement;

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

                if(currentStatement.getReturnClass().getSimpleName().equals("File")){
                    filesUsedByTestCase.put(i, false);
                }

            } else if (currentStatement instanceof MethodStatement) {

                String methodName = ((MethodStatement) currentStatement).getMethodName();
                VariableReference callee = ((MethodStatement) currentStatement).getCallee();

                int position;

                if(callee != null){
                    position = callee.getStPosition();

                    if (filesUsedByTestCase.containsKey(position)) {
                        if (optimismMethods.contains(methodName)) {
                            filesUsedByTestCase.put(position, true);
                        } else {
                            if(!filesUsedByTestCase.get(position)){
                                count++;
                            }
                        }
                    }
                }

                List<VariableReference> parameters = ((MethodStatement) currentStatement).getParameterReferences();

                for(VariableReference parameter : parameters){
                    position = parameter.getStPosition();
                    if (filesUsedByTestCase.containsKey(position)) {
                        if (optimismMethods.contains(methodName)) {
                            filesUsedByTestCase.put(position, true);
                        } else {
                            if(!filesUsedByTestCase.get(position)){
                                count++;
                            }
                        }
                    }
                }
            }
        }

        return count;
    }
}
