package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestCodeVisitor;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsuite.TestSuiteChromosome;

public class ObscureInlineSetup extends AbstractTestSmell {

    public ObscureInlineSetup(String smellName) {
        super(smellName);
    }

    @Override
    public int obtainSmellCount(TestChromosome chromosome) {

        // Two alternatives (ask advisor for help!)

        /*
        First Alternative:
        The first alternative corresponds to do something similar to
        what the DefaultTestCase class does to convert a test case into
        a String:
        1 - create a TestCodeVisitor visitor
        2 - for each statement, do visitor.visitStatement(s)
        This way, it is possible to obtain the name for each
        variable in a test case
         */

        int size = chromosome.size();

        Statement currentStatement;
        TestCase testCase = chromosome.getTestCase();

        TestCodeVisitor visitor = new TestCodeVisitor();
        visitor.visitTestCase(testCase);

        //Visit each statement to add elements to variableNames
        for (int i = 0; i < size; i++){
            currentStatement = testCase.getStatement(i);
            visitor.visitStatement(currentStatement);
        }

        //Get the size of Collection of variable names
        return visitor.getVariableNames().size();


        //---------------------------------------------
        //---------------------------------------------
        //---------------------------------------------

        /*
        Second Alternative:
        The second alternative involves assuming that certain types
        of statements are always associated with the creation of a variable.
        This approach may be more efficient, but it also less stable
         */

        /*
        int size = chromosome.size();
        int count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            if(currentStatement instanceof PrimitiveStatement || currentStatement instanceof ConstructorStatement){
                count++;
            }
        }

        return count;
        */
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
