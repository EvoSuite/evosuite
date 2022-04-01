package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestCodeVisitor;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.EntityWithParametersStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MysteryGuest extends AbstractNormalizedTestCaseSmell {

    public MysteryGuest() {
        super("TestSmellMysteryGuest");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        long count = 0;

        Statement currentStatement;

        TestCodeVisitor visitor = new TestCodeVisitor();

        List<String> mysteryTypes = new ArrayList<>(
                Arrays.asList(
                        "File",
                        "FileOutputStream",
                        "SQLiteOpenHelper",
                        "SQLiteDatabase",
                        "Cursor",
                        "Context",
                        "HttpClient",
                        "HttpResponse",
                        "HttpPost",
                        "HttpGet",
                        "SoapObject"
                ));

        for (int i = 0; i < size; i++){

            currentStatement = chromosome.getTestCase().getStatement(i);

            if (currentStatement instanceof ConstructorStatement ||
                    currentStatement instanceof MethodStatement) {

                String className = ((EntityWithParametersStatement) currentStatement).getDeclaringClassName();

                if(!className.equals(Properties.TARGET_CLASS)){

                    //Verify if there is a more efficient way to do get the name of the class
                    String curr = visitor.getClassName(currentStatement.getReturnClass());

                    for (String variableType : mysteryTypes){
                        if(curr.equals(variableType)){
                            count++;
                            break;
                        }
                    }
                }
            }
        }

        return count;
    }
}
