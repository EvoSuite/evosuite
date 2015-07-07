package org.evosuite.idNaming;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.evosuite.Properties;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;

public class TestNameGenerator {
	
	private String methodName;
	private String testName;
	private List<String> nameList;
	private List<String> testList;
	private static ExceptionExtraction hasExceptions;	
	private CheckTestNameUniqueness checkName;
	
	public TestNameGenerator(){
		methodName = "";
		testName = "";
	
		checkName = new CheckTestNameUniqueness();
	}	
	
/*public void testSuiteNaming(TestSuiteChromosome testSuite){
		
		for (TestChromosome test : testSuite.getTestChromosomes()) {
			setTestName(test.getTestCase().toCode());
			nameList.add(getTestName());
			testList.add(test.getTestCase().toCode());
		}
		checkName.check_name_uniqueness(nameList.toArray(new String[0]),testList.toArray(new String[0]));
	}*/
	
	
	
	
	public  static String generateTestName(String targetMethod, TestCase tc, ExecutionResult result, Integer num) {
		String testName = "";
		System.out.println(testName);
		String code =tc.toCode().toString();
		hasExceptions= new ExceptionExtraction(code);
		if (hasExceptions.get_exceptions()>0){
			//get method under test name
			//List<? extends TestFitnessFunction> goals=FitnessFunctions.getFitnessFactory(Properties.Criterion.BRANCH).getCoverageGoals();
			testName=targetMethod+"_throwsException" + num;
		} else{
			//get method under test name
			testName=targetMethod + num;
			//List<? extends TestFitnessFunction> goals=FitnessFunctions.getFitnessFactory(Properties.Criterion.BRANCH).getCoverageGoals();					                                    
			
		}	
		
		return testName;
	}
}
