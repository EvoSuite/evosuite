package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
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

                // Class that declares the method
                String className = ((EntityWithParametersStatement) currentStatement).getDeclaringClassName();

                if(!className.equals(Properties.TARGET_CLASS)){
                    count += mysteryTypes.contains(currentStatement.getReturnClass().getSimpleName()) ? 1 : 0;
                }
            }
        }

        return count;
    }
}
