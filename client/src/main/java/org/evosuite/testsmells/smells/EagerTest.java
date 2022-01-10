package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.generic.GenericMethod;

import java.util.LinkedHashSet;
import java.util.Set;

public class EagerTest extends AbstractTestSmell {

    public EagerTest(String smellName) {
        super(smellName);
    }

    @Override
    public int obtainSmellCount(TestChromosome chromosome) {
        int size = chromosome.size();

        Statement currentStatement;
        GenericMethod method;

        Set<GenericMethod> listOfMethods = new LinkedHashSet<>();

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            if(currentStatement instanceof MethodStatement){
                method = ((MethodStatement) currentStatement).getMethod();
                listOfMethods.add(method);
            }
        }
        return listOfMethods.size();
    }

    @Override
    public int obtainSmellCount(TestSuiteChromosome chromosome) {
        int smellCount = 0;

        for(TestChromosome testcase : chromosome.getTestChromosomes()){
            smellCount += obtainSmellCount(testcase);
        }

        return smellCount;
    }
}
