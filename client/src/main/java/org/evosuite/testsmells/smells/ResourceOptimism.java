package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestCodeVisitor;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.AbstractTestCaseSmell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class ResourceOptimism extends AbstractTestCaseSmell {

    public ResourceOptimism() {
        super("TestSmellResourceOptimism");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

        LinkedHashMap<Integer, Boolean> filesUsedByTestCase = new LinkedHashMap<>();
        Statement currentStatement;

        TestCodeVisitor visitor = new TestCodeVisitor();

        List<String> optimismMethods = new ArrayList<>(
                Arrays.asList(
                        "exists",
                        "isFile",
                        "notExists"
                ));

        for (int i = 0; i < size; i++) {

            currentStatement = chromosome.getTestCase().getStatement(i);

            if (currentStatement instanceof ConstructorStatement) {
                if (visitor.getClassName(currentStatement.getReturnClass()).equals("File")) {
                    filesUsedByTestCase.put(i, false);
                }
            }

            if (currentStatement instanceof MethodStatement) {

                String methodName = ((MethodStatement) currentStatement).getMethodName();
                List<VariableReference> parameters = ((MethodStatement) currentStatement).getParameterReferences();
                VariableReference callee = ((MethodStatement) currentStatement).getCallee();

                int position;

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
            }
        }
        return count;
    }
}
