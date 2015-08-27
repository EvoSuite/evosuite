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
import org.evosuite.Properties;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
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
	private Map<TestCase,String> testComparisons= new HashMap<TestCase, String>();

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
		String branchName="";
		String comparisonName="";
	
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
						outputName+="_"+goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("("))+"Returning"+
								WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(":")+1));						
					}
				}
		  	}
		}	
		
		methodName = methodName.replace("<","").replace(">","").replace("(","").replace(")","");
		outputName = outputName.replace("<","").replace(">","").replace("(","").replace(")","");
		branchName = branchName.replace("<","").replace(">","").replace("(","").replace(")","");
		testNames.put(tc, methodName); 
		testOutputs.put(tc, outputName);
		testBranches.put(tc, branchName);
		testComparisons.put(tc,comparisonName);
		if(methodName.equals("test")){			
		//	methodName = checkAssertions(tc, methodName);
			if(!outputName.equals("")){
				methodName ="test"+ outputName;
			}else {
				methodName ="test"+ branchName;
			}
		}
		
		/*else {
			if(NAMING_TYPE.equals("method_assertions")){
				methodName = checkAssertions(tc, methodName);
			} 
		}*/
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
		String compareAgain="NO";
    	for(int i=0; i<testCases.size(); i++){
    		for(int j=i+1; j<testCases.size(); j++){
    			 testMethodName1 = testCaseNames.get(testCases.get(i));
    			 testMethodName2 = testCaseNames.get(testCases.get(j));
    			if(testMethodName1.equals(testMethodName2)){
    				testMethodNameOptimized1 = testMethodName1 + testOutputs.get(testCases.get(i));
    				testMethodNameOptimized2 = testMethodName2 + testOutputs.get(testCases.get(j));
    				System.out.println(testMethodNameOptimized1+"-"+testMethodNameOptimized2);
    				setNameGeneratedFor(testCases.get(i), testMethodNameOptimized1);
    				setNameGeneratedFor(testCases.get(j), testMethodNameOptimized2);
    				compareAgain="YES";
    			}
    		}
    	}
    	if(compareAgain.equals("YES")){
	    	for(int i=0; i<testCases.size(); i++){
	    		for(int j=i+1; j<testCases.size(); j++){
	    			testMethodName1 = testCaseNames.get(testCases.get(i));
	    			 testMethodName2 = testCaseNames.get(testCases.get(j));
	    			if(testMethodName1.equals(testMethodName2)){
	    				testMethodNameOptimized1 = testMethodName1 + testBranches.get(testCases.get(i));
	    				testMethodNameOptimized2 = testMethodName2 + testBranches.get(testCases.get(j));
	    				System.out.println(testMethodNameOptimized1+"-"+testMethodNameOptimized2);
	    				setNameGeneratedFor(testCases.get(i), testMethodNameOptimized1);
	    				setNameGeneratedFor(testCases.get(j), testMethodNameOptimized2);
	    			}
	    		}
	    	}
    	}
    	String[] testName = new String[testCases.size()];
    	TestCase[] testCs = new TestCase[testCases.size()];
    	int count=0;
    	for (TestCase tc : testCaseNames.keySet()) {
    		testName[count] = testCaseNames.get(tc);
    		testCs[count] = tc;
    		count++;
    	} 	
    	SimplifyMethodNames optimize = new SimplifyMethodNames();
    	testName = optimize.optimizeNames(Arrays.asList(testName));  
    	int optimizeAgain = -1;
        for (int i=0; i<testName.length; i++) {        	           
            String testMethodNameOptimized = testName[i]; // TODO
            // to set the new, optimized test name:
            setNameGeneratedFor(testCs[i], testMethodNameOptimized);
         //   if(!testMethodNameOptimized.split("_")[0].equals("test")){
          //  	testMethodNameOptimized = "test"+testComparisons.get(testCs[i]);
           // 	optimizeAgain = 1;
           // 	setNameGeneratedFor(testCs[i], testMethodNameOptimized);
           // }           
        }
    /*    if(optimizeAgain == 1){
        	count=0;
	    	for (TestCase tc : testCaseNames.keySet()) {
	    		testName[count] = testCaseNames.get(tc);
	    		testCs[count] = tc;
	    		count++;
	    	} 
	        testName = optimize.optimizeNames(Arrays.asList(testName));  
	        for (int i=0; i<testName.length; i++) {        	           
	            String testMethodNameOptimized = testName[i]; // TODO
	            // to set the new, optimized test name:
	            setNameGeneratedFor(testCs[i], testMethodNameOptimized);
	        }
        }*/

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

    public String checkExeptionInTest(String tc, String testName) {
        String methodName = testName;
        String typeOfException = "";
        String[] tokens = testName.split("_");
        if (tokens.length == 1) {
            return testName;
        } else {
            ExceptionExtraction hasExceptions = new ExceptionExtraction(tc);
            if (hasExceptions.get_exceptions() > 0) {
                typeOfException = tc.substring(tc.lastIndexOf("fail(\"Expecting exception: "));
                typeOfException = typeOfException.substring(typeOfException.lastIndexOf(": ") + 2, typeOfException.indexOf("\");"));
                //methodName=tokens[0]+"_"+tokens[1]+"_"+typeOfException;
                methodName = testName + "_" + typeOfException;
            }
            return methodName;
        }
    }
  
}
