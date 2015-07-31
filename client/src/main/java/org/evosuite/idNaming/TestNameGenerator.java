package org.evosuite.idNaming;

import java.util.*;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.evosuite.Properties;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;

public class TestNameGenerator {
	
	public static List<String> methodNames = new ArrayList<String>();;
	public static List<String> testCase = new ArrayList<String>();
	public static List<TestCase> testCase1 = new ArrayList<TestCase>();

	// this could be a mapping from test case to *list* of candidate names
	public static Map<TestCase,String> testNames = new HashMap<TestCase,String>();

	public static String checkExeptionInTest(String tc, String testName){		
		String methodName=testName;
		String typeOfException="";
		String [] tokens= testName.split("_");
		if (tokens.length==1){			
				return testName;				
		} else {
			ExceptionExtraction hasExceptions= new ExceptionExtraction(tc);
			if(hasExceptions.get_exceptions()>0){
				typeOfException=tc.substring(tc.lastIndexOf("fail(\"Expecting exception: "));
				typeOfException=typeOfException.substring(typeOfException.lastIndexOf(": ")+2,typeOfException.indexOf("\");"));				
				//methodName=tokens[0]+"_"+tokens[1]+"_"+typeOfException;		
				methodName=testName+"_"+typeOfException;
			}
			return methodName;
		}	
	}	

	/*public  static String generateTestName(String targetMethod, TestCase tc, ExecutionResult result, Integer num) {
			String testName = "";
			//goal set 
			Set<? extends TestFitnessFunction> goals = tc.getCoveredGoals();
			
			testName =goals.toString();
			String goalNames[]=testName.split(", ");
			
			testName="test";
			for(String goal: goalNames){				
				if(goal.contains("root-Branch")){
					testName+="_"+goal.substring(goal.lastIndexOf(".")+1,goal.indexOf("("));					
				} else {					
					if(goal.contains("Branch") && goal.contains(" - true")){
						testName+="_With"+WordUtils.capitalize(goal.substring(goal.lastIndexOf(".")+1,goal.indexOf("(")))+"(True)";
					}
					if(goal.contains("Branch") && goal.contains(" - false")){
						testName+="_With"+WordUtils.capitalize(goal.substring(goal.lastIndexOf(".")+1,goal.indexOf("(")))+"(False)";
					}
				}
			}
			testName = testName.replace("<","").replace(">","").replace("(","").replace(")","");
		//	testName = testName + num;
			System.out.println(testName);
			System.out.println(tc.toCode());
		//check if the same name and test case is already traversed 
		int tempCount=-1;
		for(String test : testCase){
			if(test.equals(tc.toCode())){
				tempCount=200;
				break;
			}
		}		
		if(tempCount==-1){
			testCase.add(tc.toCode());
			methodNames.add(testName);
		}		
		return testName;
	}*/
	
    public  static String generateTestName1(String targetMethod, TestCase tc, ExecutionResult result, Integer num) {
        String testName = "";
        //goal set
        Set<? extends TestFitnessFunction> goals = tc.getCoveredGoals();

        testName =goals.toString();
		String goalNames[]=testName.split(", ");

		testName="test";

		for(String goal: goalNames){				
		/*	if(goal.contains("root-Branch")){
				testName+="_"+goal.substring(goal.lastIndexOf(".")+1,goal.indexOf("("));					
			} 				
			if(goal.contains("Branch") && goal.contains(" - true") && testName.split("_").length==1){
				testName+="_With"+WordUtils.capitalize(goal.substring(goal.lastIndexOf(".")+1,goal.indexOf("(")))+"(True)";
			}
			if(goal.contains("Branch") && goal.contains(" - false") && testName.split("_").length==1){
				testName+="_With"+WordUtils.capitalize(goal.substring(goal.lastIndexOf(".")+1,goal.indexOf("(")))+"(False)";
			}*/
			if(!goalNames[0].equals("[]")){
				testName+="_"+goal.substring(goal.lastIndexOf(".")+1,goal.indexOf("("));
			}
		}


		testName = testName.replace("<","").replace(">","").replace("(","").replace(")","");
		//	testName = testName + num;
		System.out.println(testName);
		System.out.println(tc.toCode());
		//check if the same name and test case is already traversed
		int tempCount=-1;
		for(String test : testCase){
			if(test.equals(tc.toCode())){
				tempCount=200;
				break;
			}
		}
		if(tempCount==-1){
			testCase.add(tc.toCode());
			testCase1.add(tc);
			methodNames.add(testName);
		}

		return testName;
	}
	public static String[] optimizeNames(){
		String[] testNames=new String[methodNames.size()];
		int i=0;		
		for(String name: OptimizeTestName.optimiseNames(methodNames)){
			testNames[i]=name;
			i++;
		}
		testNames=CheckTestNameUniqueness.renameMethods(testNames);
		return testNames;
	}
	public static int getPos(String name, List<Integer> posFound, String test){
		int pos=-1;
		int temp=0;
		for(int i=0; i<methodNames.size(); i++){
			temp=0;
			if(name.equals(methodNames.get(i)) && test.equals(testCase.get(i))){				
				for(int previousPos: posFound){
					if(previousPos==i){
						temp=-2;
						break;
					}
				}
				if(temp!=-2){
					pos=i;
					break;
				}
			}			
		}
		return pos;
	}

/*	public static String translateBranch(String option){
		String translate="";
		if(option.contains("IFGE")){
			translate = "BranchGE";
		} else {
			if(option.contains("IFLE")){
				translate = "BranchLE";
			} else {
				if(option.contains("IFGT")){
					translate = "BranchGT";
				} else{
					if(option.contains("IFLT")){
						translate = "BranchLT";
					} else{
						if(option.contains("IFEQ")){
							translate = "BranchEQ";
						} else {
							if(option.contains("ICMPGE")){
								translate = "BranchCompareGE";
							} else {
								if(option.contains("ICMPLE")){
									translate = "BranchCompareLE";
								} else {
									if(option.contains("ICMPGT")){
										translate = "BranchCompareGT";
									} else {
										if(option.contains("ICMPLT")){
											translate = "BranchCompareLT";
										} else {
											if(option.contains("ICMPEQ")){
												translate = "BranchCompareEQ";
											} else {
												if(option.contains("IFNONNULL")){
													translate = "BranchNoNull";
												}else {
													if(option.contains("IFNNULL")){
														translate = "BranchNull";
													}
												}
											}
										}	
									}
								}
							}
						}
					}
				}
			}
		}
		return translate;
	}*/


	/**
	 * Generates test names for all the test cases in the list
	 *
	 * @param testCases list of test cases
	 * @param results list of execution results
	 */
	public static void generateAllTestNames(List<TestCase> testCases, List<ExecutionResult> results) {
		for (int id = 0; id < testCases.size(); id++) {
			TestCase tc = testCases.get(id);
			ExecutionResult res = null; // results.get(id);
			// find out target method
			String targetMethod = getTargetMethod(tc,res);
			// add test case name to mapping
			testNames.put(tc,generateTestName1("targetM", tc, res, id)); // add new entry mapping tc to its name

		}
	}

	/**
	 * Infers the target Method Under Test
	 *
	 * @param tc test case
	 * @param res execution result
	 */
	private static String getTargetMethod(TestCase tc, ExecutionResult res) {
		// TODO
		return "";
	}
}
