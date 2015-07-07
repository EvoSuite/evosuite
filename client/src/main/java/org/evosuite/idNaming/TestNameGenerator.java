package org.evosuite.idNaming;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.evosuite.Properties;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.strategy.TestGenerationStrategy;
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
	
	
	
	
	public  static String generateTestName(String targetMethod, TestCase tc, ExecutionResult result) {
		String testName = "";
		System.out.println(testName);
		String code =tc.toCode().toString();
		hasExceptions= new ExceptionExtraction(code);
		if (hasExceptions.get_exceptions()>0){
			//get method under test name
			List<? extends TestFitnessFunction> goals=TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals();
			testName =goals.toString();
			String goalNames[]=testName.split(", ");
			//goalNames=goals.toArray(goalNames);
			int i=0;
			testName="test";
			for(String goal: goalNames){
				
				testName+="_"+goal.substring(goal.lastIndexOf(".")+1,goal.indexOf("("));
				if(goal.contains("root-Branch")){
					
				}
				if(goal.contains("Branch") && goal.contains(" - true")){
					testName+="(true)";
				}
				if(goal.contains("Branch") && goal.contains(" - false")){
					testName+="(false)";
				}
				i++;		
			}
			testName+="_throwsException";
		} else{
			//get method under test name
			testName=targetMethod;
			List<? extends TestFitnessFunction> goals=TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals();
			testName =goals.toString();
			String goalNames[]=testName.split(", ");
			//goalNames=goals.toArray(goalNames);
			int i=0;
			testName="test";
			for(String goal: goalNames){
				
				testName+="_"+goal.substring(goal.lastIndexOf(".")+1,goal.indexOf("("));
				if(goal.contains("root-Branch")){
					
				}
				if(goal.contains("Branch") && goal.contains(" - true")){
					testName+="(true)";
				}
				if(goal.contains("Branch") && goal.contains(" - false")){
					testName+="(false)";
				}
				i++;				
			}
			System.out.println(testName);
		//	testName=goals.toString();
			
			//List<? extends TestFitnessFunction> goals=FitnessFunctions.getFitnessFactory(Properties.Criterion.BRANCH).getCoverageGoals();					                                    
			
		}	
		
		return testName;
	}
}
