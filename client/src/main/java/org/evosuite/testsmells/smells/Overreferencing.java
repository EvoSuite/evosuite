package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.AbstractTestCaseSmell;

import java.util.LinkedHashMap;
import java.util.List;

public class Overreferencing extends AbstractTestCaseSmell {

    public Overreferencing() {
        super("TestSmellOverreferencing");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

        LinkedHashMap<Integer, Boolean> constructors = new LinkedHashMap<>();
        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            if(currentStatement instanceof ConstructorStatement){
                constructors.put(i, false);
            }

            if (currentStatement instanceof MethodStatement) {
                List<VariableReference> parameters = ((MethodStatement) currentStatement).getParameterReferences();
                VariableReference callee = ((MethodStatement) currentStatement).getCallee();

                int position;

                for(VariableReference parameter : parameters){
                    position = parameter.getStPosition();
                    if (constructors.containsKey(position)) {
                        constructors.put(position, true);
                    }
                }

                if(callee != null){
                    position = callee.getStPosition();

                    if (constructors.containsKey(position)) {
                        constructors.put(position, true);
                    }
                }
            }
        }

        for(Integer constructor : constructors.keySet()){
            if(!constructors.get(constructor)){
                count++;
            }
        }

        return count;
    }
}
