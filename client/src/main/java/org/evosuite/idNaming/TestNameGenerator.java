/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.idNaming;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.evosuite.Properties;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.input.InputCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.output.OutputCoverageTestFitness;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

import java.util.*;

//import com.sun.codemodel.internal.util.Surrogate.Generator;

/**
 * This class implements a test method name generator.
 *
 * It provides two main public interfaces:
 *
 * Method {@code execute}: executes test name generation algorithm, including
 * a phase of optimization.
 * Method {@code getNameGeneratedFor}: returns the name generated for a given test case.
 */
public class TestNameGenerator extends DistinguishNames {

    private List<String> methodNames = new ArrayList<String>();
    private List<String> testCase = new ArrayList<String>();
    private List<Integer> methodPosition = new ArrayList<Integer>();
    
    /**
     * Mappings from test case to method, branch and output goal name
     */
    private Map<TestCase,String> testNames = new HashMap<TestCase, String>();
    private Map<TestCase,String> testOutputs = new HashMap<TestCase, String>();
	private Map<TestCase,String> testBranches = new HashMap<TestCase, String>();
	private Map<TestCase,String> testInputs = new HashMap<TestCase, String>();
	private Map<TestCase,String> testComparisons= new HashMap<TestCase, String>();
	private Map<TestCase,String> testExceptions = new HashMap<TestCase, String>();
    /**
     * Mapping from test case to test case name
     */
    private Map<TestCase, String> testCaseNames = new HashMap<TestCase, String>();

    /**
     * TestNameGenerator instance
     */
    private static TestNameGenerator instance = null;
    
    public String NAMING_TYPE = "";
    /**
     * Getter for the field {@code instance}
     *
     * @return a {@link org.evosuite.idNaming.TestNameGenerator}
     * object.
     */
    public static synchronized TestNameGenerator getInstance() {
        if (instance == null)
            instance = new TestNameGenerator();

        return instance;
    }

    /**
     * Generates test names for all the test cases in the list
     *
     * @param testCases list of test cases
     * @param results   list of execution results
     */
    public static void execute(List<TestCase> testCases, List<ExecutionResult> results) {
  //  public static void execute(List<TestCase> testCases) {
        TestNameGenerator generator = getInstance();

        // First, let's try to generate names for each test case individually
        for (int id = 0; id < testCases.size(); id++) {
            TestCase tc = testCases.get(id);
            ExecutionResult res = results.get(id);

            // find out target method
           String targetMethod = generator.getTargetMethod(tc, res);
         //   String targetMethod = generator.getTargetMethod(tc);

            // generate test name
           String testMethodName = generator.generateTestName(targetMethod, tc, res, id);
        //    String testMethodName = generator.generateTestName(targetMethod, tc, id);

            // save generated test name
            generator.setNameGeneratedFor(tc, testMethodName);
        }

        // Now, we may have conflicts between two (or more?) different tests.
        // We may even have opportunity to optimize the generated names further.
        // TODO: Should names be optimized only if all tests will be written in the same file? For now, yes.
        if (Properties.OUTPUT_GRANULARITY == Properties.OutputGranularity.MERGED) {
            generator.optimize(testCases, results);
        //	generator.optimize(testCases);
        }
    }

    /**
     * Returns the final name generated for a test case, or null, if no name was generated
     *
     * @param tc test case
     * @return a string containing the test name or null
     */
    public static String getNameGeneratedFor(TestCase tc) {
        TestNameGenerator generator = getInstance();
        return generator.testCaseNames.get(tc);
    }

    /**
     * Sets the final name generated for a test case
     *
     * @param tc   test case
     * @param name test method name
     */
    private void setNameGeneratedFor(TestCase tc, String name) {
        testCaseNames.put(tc, name);
    }
    
    /**
     * Generates test name for one particular test case
     *
     * @param targetMethod inferred target method
     * @param tc           test case
     * @param result       test case execution result
     * @param id           test case id
     */
    private String generateTestName(String targetMethod, TestCase tc, ExecutionResult result, Integer id) {

    	Set<? extends TestFitnessFunction> goals = tc.getCoveredGoals();
		String methodName="test";
		String outputName="";
		String inputName="";
		String branchName="";
		String comparisonName="";
		String exceptionName="";
	
		for (TestFitnessFunction goal : goals) {
  		  	String goalName = goal.toString();
		  	if (goal instanceof MethodCoverageTestFitness) {
		  		methodName+="_"+goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("("));		  		
		  	}else {
		  		if (goal instanceof BranchCoverageTestFitness){
		  			if(goalName.contains("root-Branch")){
		  				branchName+="_"+goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("("))+ "RootBranch";
					} 				
		  			else{
		  				branchName+="_"+goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("("))+
			  					WordUtils.capitalize(goalName.substring(goalName.indexOf(" - ")+3))+"Branch";
		  				String [] branch=goalName.substring(goalName.lastIndexOf(":")+1,goalName.indexOf(" - ")).trim().split(" ");
		  				comparisonName+="_"+goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("("))+
			  					WordUtils.capitalize(goalName.substring(goalName.indexOf(" - ")+3))+"Branch"+ translateBranch(branch[3]);
					}			  			
				} else {
					if (goal instanceof OutputCoverageTestFitness ) {	
						if(StringUtils.countMatches(goalName, "(")==2){
							String[] outputN=goalName.split(":");
							outputName+="_"+outputN[0].substring(outputN[0].lastIndexOf(".")+1,outputN[0].indexOf("("))+"With"+
									WordUtils.capitalize(outputN[3].substring(0,outputN[3].indexOf("(")))+
									WordUtils.capitalize(outputN[4]);
						}else{
							outputName+="_"+goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("("))+"Returning"+
								WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(":")+1));	
						}
					}else{
						if (goal instanceof InputCoverageTestFitness ) {	
							if(StringUtils.countMatches(goalName, "(")==2){
								String[] inputN=goalName.split(":");
								inputName+="_"+inputN[0].substring(inputN[0].lastIndexOf(".")+1,inputN[0].indexOf("("))+"With"+
										WordUtils.capitalize(inputN[3].substring(0,inputN[3].indexOf("(")))+
										WordUtils.capitalize(inputN[4]);
							} else{
								inputName+="_"+goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("("))+"With"+
									WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(":")+1))+"Input";	
							}
						}else {
							if (goal instanceof ExceptionCoverageTestFitness) {
								//exceptionName+="_"+goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("("));
								exceptionName+="_"+goalName.substring(0,goalName.indexOf("("))+
										"Throwing"+goalName.substring(goalName.lastIndexOf(".")+1, goalName.lastIndexOf("_"));
						  	}
						}
					}
				}
		  	}
		}	
		
		methodName = methodName.replace("<","").replace(">","").replace("(","").replace(")","");//+
		exceptionName =	exceptionName.replace("<","").replace(">","").replace("(","").replace(")","");
		outputName = outputName.replace("<","").replace(">","").replace("(","").replace(")","");
		branchName = branchName.replace("<","").replace(">","").replace("(","").replace(")","");
		inputName = inputName.replace("<","").replace(">","").replace("(","").replace(")","");
		
		methodName=methodName.replace("_init", "_constructor");
		exceptionName =	exceptionName.replace("_init", "_constructor");
		outputName = outputName.replace("_init", "_constructor");
		branchName = branchName.replace("_init", "_constructor");
		inputName = inputName.replace("_init", "_constructor");
		
		testNames.put(tc, methodName); 
		testOutputs.put(tc, outputName);
		testBranches.put(tc, branchName);
		testInputs.put(tc, inputName);
		testComparisons.put(tc,comparisonName);
		testExceptions.put(tc, exceptionName);
		if(methodName.equals("test")){			
		//	methodName = checkAssertions(tc, methodName);
			if(!outputName.equals("")){
				methodName ="test"+ outputName;
			}else {
				if(!inputName.equals("")){
					methodName ="test"+ inputName;
				}else{
					methodName ="test"+ branchName;
				}
			}
		}
	
		System.out.println(methodName);
		return methodName;
    }

    /**
     * Once names have been generated for all tests, resolve conflicts and optimize names.
     */
    private void optimize(List<TestCase> testCases, List<ExecutionResult> results) {
    	String testMethodName1 = "";
		String testMethodName2 = "";
		String testMethodNameOptimized1 = "";
		String testMethodNameOptimized2 = "";
		List<Integer> testOptimized = new ArrayList<Integer>();

		List<String> output = new ArrayList<String>();
		List<String> input = new ArrayList<String>();
		List<String> branch = new ArrayList<String>();
		String compareAgain="NO";
		
    	for(int i=0; i<testCases.size(); i++){
    		compareAgain="NO";
    		for(int j=i+1; j<testCases.size(); j++){    			
	    			 testMethodName1 = testCaseNames.get(testCases.get(i));
	    			 testMethodName2 = testCaseNames.get(testCases.get(j));
	    			if(testMethodName1.equals(testMethodName2)){
	    				compareAgain="YES";
	    				testMethodNameOptimized1 = //testMethodName1 + 
	    						"test_"+testOutputs.get(testCases.get(i));
	    				testMethodNameOptimized2 = //testMethodName2 + 
	    						"test_"+testOutputs.get(testCases.get(j));
	    				if(testMethodNameOptimized1.equals(testMethodNameOptimized2)){
	    					testMethodNameOptimized1 = //testMethodNameOptimized1 + 
	    							"test_"+testInputs.get(testCases.get(i));
	        				testMethodNameOptimized2 = //testMethodNameOptimized2 + 
	        						"test_"+testInputs.get(testCases.get(j));
	        				if(testMethodNameOptimized1.equals(testMethodNameOptimized2)){
	        					testMethodNameOptimized1 = //testMethodNameOptimized1 + 
	        							"test_"+testBranches.get(testCases.get(i));
	            				testMethodNameOptimized2 = //testMethodNameOptimized2 + 
	            						"test_"+testBranches.get(testCases.get(j));  				
	        				}
	
	    				}
	    			//	System.out.println(testMethodNameOptimized1+"-"+testMethodNameOptimized2);    				
	    				setNameGeneratedFor(testCases.get(j), testMethodNameOptimized2);    				
	    			}    			
	    		} 
    		if(compareAgain=="YES"){
    	    	setNameGeneratedFor(testCases.get(i), testMethodNameOptimized1);
    		}
    	}
    	
    	String[] testName = new String[testCases.size()];
    	TestCase[] testCs = new TestCase[testCases.size()];
    	int count=0;
    	for(Map.Entry<TestCase,String> entry: testCaseNames.entrySet()){
    		testName[count]= entry.getValue();
    		testCs[count] = entry.getKey();
    		System.out.println(testName[count]);
    		count++;
    	}
    	
    /*	for (TestCase tc : testCaseNames.keySet()) {
    		testName[count] = testCaseNames.get(tc);
    		testCs[count] = tc;
    		count++;
    	} 	*/
    	SimplifyMethodNames optimize = new SimplifyMethodNames();
    	testName = optimize.optimizeNames(Arrays.asList(testName));
    	for (int i=0; i<testName.length; i++) {        	           
          testName[i] = testName[i] + testExceptions.get(testCs[i]); // TODO
          System.out.println(testName[i]);
        }    
    	testName = optimize.minimizeNames(testName);
    	testName = optimize.countSameNames(testName); 
    	int optimizeAgain = -1;
        for (int i=0; i<testName.length; i++) {        	           
            String testMethodNameOptimized = testName[i]; // TODO
            setNameGeneratedFor(testCs[i], testMethodNameOptimized);         
            methodNames.add(testMethodNameOptimized);
          //  System.out.println(testMethodNameOptimized);
           // System.out.println(testCs[i]);
           
        }     

    }
  
    /**
     * Infers the target Method Under Test
     *
     * @param tc  test case
     * @param res execution result
     */

    private String getTargetMethod(TestCase tc, ExecutionResult res) {
 //   private String getTargetMethod(TestCase tc) {
        // TODO
        return "test";
    }
    int count=0;
    public String checkExeptionInTest(String tc, String testName) {
        String methodName = testName;
      /*  String typeOfException = "";
        String[] tokens = testName.split("_");
        if (tokens.length == 1) {
            return testName;
        } else {
            ExceptionExtraction hasExceptions = new ExceptionExtraction(tc);
            if (hasExceptions.get_exceptions() > 0) {
                typeOfException = tc.substring(tc.lastIndexOf("fail(\"Expecting exception: "));
                typeOfException = typeOfException.substring(typeOfException.lastIndexOf(": ") + 2, typeOfException.indexOf("\");"));
             //   methodName=tokens[0]+"_"+tokens[1]+"_"+typeOfException;
               methodName = testName + "_" + typeOfException;
            }
            
            for(int i=0; i<testCase.size(); i++){        		
    			if(methodName.equals(methodNames.get(i))){
    				 methodName=tokens[0]+count+"_"+tokens[1]+"_"+typeOfException;
    				 count++;
    			}        		
            }*/
            return methodName;
        }
    
  
}
