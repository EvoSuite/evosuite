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

/**
 * Definition:
 * Tests use external resources (such as files or databases that contain test data); hence, they are not self-contained.
 *
 * Metric:
 * The number of instances of file/database classes that a test case contains.
 *
 * Computation:
 * 1 - Create a smell counter and initialize the variable with the value 0
 * 2 - Let S = {S1,...,Sn} be the set of n statements in a test case
 * 3 - Iterate over S and, for each statement Si:
 * [3: Start loop]
 * 4 - Verify if Si is an instance of either MethodStatement or ConstructorStatement
 * 5 (4 is True):
 *    5.1 - If the return value of Si corresponds to an instance of a file/database class: increment the smell counter
 * [3: End loop]
 * 6 - Return the smell counter
 */
public class MysteryGuest extends AbstractNormalizedTestCaseSmell {

    private static final long serialVersionUID = -8598688416127627442L;

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
