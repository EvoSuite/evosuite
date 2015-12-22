package org.evosuite.idNaming;

import org.apache.commons.lang3.StringUtils;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gordon on 22/12/2015.
 */
public class NumberedTestNameGenerationStrategy implements TestNameGenerationStrategy {

    private Map<TestCase, String> testToName = new HashMap<>();

    public NumberedTestNameGenerationStrategy(List<TestCase> testCases, List<ExecutionResult> results) {
        generateNames(testCases);
    }

    public NumberedTestNameGenerationStrategy(TestSuiteChromosome suite) {
        generateNames(suite.getTests());
    }

    private void generateNames(List<TestCase> testCases) {
        int totalNumberOfTests = testCases.size();
        String totalNumberOfTestsString = String.valueOf(totalNumberOfTests - 1);

        int num = 0;
        for(TestCase test : testCases) {
            String testNumber = StringUtils.leftPad(String.valueOf(num),
                    totalNumberOfTestsString.length(), "0");
            String testName = "test" + testNumber;
            testToName.put(test, testName);
            num++;
        }
    }

    @Override
    public String getName(TestCase test) {
        return testToName.get(test);
    }
}
