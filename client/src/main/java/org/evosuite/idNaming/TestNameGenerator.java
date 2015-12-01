package org.evosuite.idNaming;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.evosuite.Properties;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.input.InputCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.output.OutputCoverageTestFitness;
import org.evosuite.runtime.mock.EvoSuiteMock;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

import java.lang.reflect.Modifier;
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
    
    /**
     * Mappings from test case to method, branch and output goal name
     */
    public Map<TestCase,String> testNames = new HashMap<TestCase, String>();
    private Map<TestCase,String> testOutputs = new HashMap<TestCase, String>();
	private Map<TestCase,String> testBranches = new HashMap<TestCase, String>();
	private Map<TestCase,String> testInputs = new HashMap<TestCase, String>();
	private Map<TestCase,String> testMethodArg= new HashMap<TestCase, String>();
	private Map<TestCase,String> testInputArg= new HashMap<TestCase, String>();
	private Map<TestCase,String> testOutputArg= new HashMap<TestCase, String>();
	private Map<TestCase,String> testBranchArg= new HashMap<TestCase, String>();
	private Map<TestCase,String> testMethodArgType= new HashMap<TestCase, String>();
	private Map<TestCase,String> testInputArgType= new HashMap<TestCase, String>();
	private Map<TestCase,String> testOutputArgType= new HashMap<TestCase, String>();
	private Map<TestCase,String> testBranchArgType= new HashMap<TestCase, String>();
	private Map<TestCase,String> testExceptions = new HashMap<TestCase, String>();
	private String className="";
    /**
     * Mapping from test case to test case name
     */
    private Map<TestCase, String> testCaseNames = new HashMap<TestCase, String>();
    public static Map<String, String> testCaseNames1 = new HashMap<String, String>();
    private Map<TestCase, TestFitnessFunction> goalType = new HashMap<TestCase,TestFitnessFunction>();

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
        testCaseNames1.put(tc.toCode().trim(), name);
    }

	public static Map<TestCase, String> getResults() {
		return getInstance().testCaseNames;
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
		String methodArguments="", inputArguments="", outputArguments="", branchArguments="", str="";
		String methodArgumentTypes="", inputArgumentTypes="", outputArgumentTypes="", branchArgumentTypes="";
		
		String exceptionName="";
		
		MethodName methods = new MethodName(tc);
		OutputName output = new OutputName(tc);
		InputName input = new InputName(tc);
		BranchName branch = new BranchName(tc);
		ExceptionName exceptions = new ExceptionName(tc);
		
		className = methods.className;
		methodName += methods.getMethodGoal();
		outputName = output.getOutputGoal();
		inputName = input.getInputGoal();
		branchName = branch.getBranchGoal();
		methodArguments = methods.getMethodArguments();
		inputArguments = input.getInputArguments();
		outputArguments = output.getOutputArguments();
		branchArguments = branch.getBranchArguments();
		methodArgumentTypes = methods.getMethodArgumentTypes();
		inputArgumentTypes = input.getInputArgumentTypes();
		outputArgumentTypes = output.getOutputArgumentTypes();
		branchArgumentTypes = branch.getBranchArgumentTypes();
		exceptionName = exceptions.getExceptionGoal();
		
		methodName = methodName.replace("<","").replace(">","").replace("(","").replace(")","");//+
		exceptionName =	exceptionName.replace("<","").replace(">","").replace("(","").replace(")","");
		outputName = outputName.replace("<","").replace(">","").replace("(","").replace(")","");
		branchName = branchName.replace("<","").replace(">","").replace("(","").replace(")","");
		inputName = inputName.replace("<","").replace(">","").replace("(","").replace(")","");
		methodArguments = methodArguments.replace("<","").replace(">","").replace("(","").replace(")","");
		branchArguments = branchArguments.replace("<","").replace(">","").replace("(","").replace(")","");
		inputArguments = inputArguments.replace("<","").replace(">","").replace("(","").replace(")","");
		outputArguments = outputArguments.replace("<","").replace(">","").replace("(","").replace(")","");
		methodArgumentTypes = methodArgumentTypes.replace("<","").replace(">","").replace("(","").replace(")","");
		branchArgumentTypes = branchArgumentTypes.replace("<","").replace(">","").replace("(","").replace(")","");
		inputArgumentTypes = inputArgumentTypes.replace("<","").replace(">","").replace("(","").replace(")","");
		outputArgumentTypes = outputArgumentTypes.replace("<","").replace(">","").replace("(","").replace(")","");
		
		
		methodName=methodName.replace("_init", "_CreateConstructor");
		exceptionName =	exceptionName.replace("_init", "_Constructor");
		outputName = outputName.replace("_init", "_Constructor");
		branchName = branchName.replace("_Invokesinit", "_InvokesConstructor");
		branchName = branchName.replace("_Callsinit", "_CallsConstructor");
		inputName = inputName.replace("_init", "_Constructor");
		methodArguments=methodArguments.replace("_init", "_Constructor");
		branchArguments = branchArguments.replace("_Invokesinit", "_InvokesConstructor");
		branchArguments = branchArguments.replace("_Callsinit", "_CallsConstructor");
		inputArguments = inputArguments.replace("_init", "_Constructor");
		outputArguments = outputArguments.replace("_init", "_Constructor");
		methodArgumentTypes = methodArgumentTypes.replace("_init", "_Constructor");
		branchArgumentTypes = branchArgumentTypes.replace("_Callsinit", "_CallsConstructor");
		branchArgumentTypes = branchArgumentTypes.replace("_Invokesinit", "_InvokesConstructor");
		inputArgumentTypes = inputArgumentTypes.replace("_init", "_Constructor");
		outputArgumentTypes = outputArgumentTypes.replace("_init", "_Constructor");
		
		if(methodArguments.split("_").length!=methodArgumentTypes.split("_").length){
			methodArgumentTypes="";
		}
		if(inputArguments.split("_").length!=inputArgumentTypes.split("_").length){
			inputArgumentTypes="";
		}
		if(outputArguments.split("_").length!=outputArgumentTypes.split("_").length){
			outputArgumentTypes="";
		}
		if(branchArguments.split("_").length!=branchArgumentTypes.split("_").length){
			branchArgumentTypes="";
		}
		System.out.println(methodArguments+"-"+inputArguments+"-"+outputArguments+"-" +branchArguments);
		testNames.put(tc, methodName); 
		testOutputs.put(tc, outputName);
		testBranches.put(tc, branchName);
		testInputs.put(tc, inputName);
	//	testComparisons.put(tc,comparisonName);
		testExceptions.put(tc, exceptionName);
		testMethodArg.put(tc, methodArguments);
		testInputArg.put(tc,inputArguments);
		testOutputArg.put(tc,outputArguments);
		testBranchArg.put(tc,branchArguments);
		testMethodArgType.put(tc, methodArgumentTypes);
		testInputArgType.put(tc,inputArgumentTypes);
		testOutputArgType.put(tc,outputArgumentTypes);
		testBranchArgType.put(tc,branchArgumentTypes);
		
		
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
	//	System.out.println(methodName);
	
		return methodName;
    }
    private String getGoalType(TestCase tc, String type){
    	 Set<? extends TestFitnessFunction> goals = tc.getCoveredGoals();
    	 String goalName="";
    	 for (TestFitnessFunction goal : goals) {
	   		 if(goal instanceof MethodCoverageTestFitness && type.equals("method")){
	    		 goalName = goal.toString();
	    		 goalType.put(tc, goal);
	   		 }
	   		 if(goal instanceof OutputCoverageTestFitness && type.equals("output")){
	    		 goalName = goal.toString();
	    		 goalType.put(tc, goal);
	   		 }
	   		 if(goal instanceof InputCoverageTestFitness && type.equals("input")){
	    		 goalName = goal.toString();
	    		 goalType.put(tc, goal);
	   		 }
	   		 if(goal instanceof BranchCoverageTestFitness && type.equals("branch")){
	    		 goalName = goal.toString();
	    		 goalType.put(tc, goal);
	   		 }
    	 }
    	 return goalName;
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
		String str1 ="";
		String str2 ="";
		String compareAgain="NO";
		int temp1=-1, temp2=-1, temp=-1;
    	for(int i=0; i<testCases.size(); i++){ 		
    		//check if we already optimized this name and break loops
    		temp=-1;
    		temp1=-1; temp2=-1;
    		for(int pos: testOptimized){
				if(pos==i){
					temp=10;
				break;
				}
			}    		
    		for(int j=i+1; j<testCases.size(); j++){    
    			if(temp==10){
        			break;
        		}    			
    			temp1=temp2=-1;
    			testMethodName1 = testCaseNames.get(testCases.get(i));
    			testMethodName2 = testCaseNames.get(testCases.get(j));
    			getGoalType(testCases.get(i),"method");
    			getGoalType(testCases.get(j),"method");
    			if(testMethodName1.equals(testMethodName2)){ 	
    			//	 compareAgain="YES";
    		//		System.out.println(testMethodName1);
					str1 = "test"+testMethodArg.get(testCases.get(i));
					str2 = "test"+testMethodArg.get(testCases.get(j));
					if(!str1.equals(str2)){
						//check if changed name is already in the list
						temp1=checkOtherNames(testCases,str1,i);
						temp2=checkOtherNames(testCases,str2,j);
						//if same names are not found 
	    				if(temp1!=0 && compareAgain!="Do not change"){
	    					testMethodNameOptimized1 = str1;		    					
	    					temp1=100;	    					
	    					compareAgain="Do not change";	    					
	    					getGoalType(testCases.get(i),"method");
	    				}
	    				if(temp2!=0){
	    					testMethodNameOptimized2 = str2;
	    					testOptimized.add(j);
			 				setNameGeneratedFor(testCases.get(j), testMethodNameOptimized2);
			 				getGoalType(testCases.get(j),"method");
	    					temp2=100;
	    				}
					}
					if(temp2!=100){
    				temp1=temp2=-1;
	 	    			str1 = "test"+testMethodArgType.get(testCases.get(i));
    					str2 = "test"+testMethodArgType.get(testCases.get(j));
    					if(!str1.equals(str2) && !str1.equals("test") && !str2.equals("test")){
    						temp1=checkOtherNames(testCases,str1,i);
    						temp2=checkOtherNames(testCases,str2,j);
		    				if(temp1!=0 && compareAgain!="Do not change"){
		    					testMethodNameOptimized1 = str1;		    					
		    					compareAgain="Do not change";
		    					temp1=100;	  				
		    				}
		    				if(temp2!=0){
		    					testMethodNameOptimized2 = str2;
		    					testOptimized.add(j);
					 			setNameGeneratedFor(testCases.get(j), testMethodNameOptimized2); 
		    					temp2=100;
		    				}
    					}
					}
					if(temp2!=100){
	    				temp1=temp2=-1;	 	    		
	    				str1 = "test"+testOutputs.get(testCases.get(i))+testExceptions.get(testCases.get(i));
    					str2 = "test"+testOutputs.get(testCases.get(j))+testExceptions.get(testCases.get(j));
    					if(!str1.equals(str2) && !str1.equals("test") && !str2.equals("test")){
    						temp1=checkOtherNames(testCases,str1,i);
    						temp2=checkOtherNames(testCases,str2,j);
		 	    			if(temp1!=0 && compareAgain!="Do not change"){
		    					testMethodNameOptimized1 = str1;
		    					temp1=100;	    					
	    						compareAgain="Do not change";
	    						getGoalType(testCases.get(i),"output");
		    				}
		    				if(temp2!=0){
			 	    			testMethodNameOptimized2 = str2;
			 	    			testOptimized.add(j);			 	    			
				 				setNameGeneratedFor(testCases.get(j), testMethodNameOptimized2); 
				 				getGoalType(testCases.get(j),"output");	    					
			 	    			temp2=100;
		    				}
    					}
					}
					if(temp2!=100){
	    				temp1=temp2=-1;	 	    		
	    				str1 = "test"+testOutputArgType.get(testCases.get(i));
    					str2 = "test"+testOutputArgType.get(testCases.get(j));
    					if(!str1.equals(str2) && !str1.equals("test") && !str2.equals("test")){
    						temp1=checkOtherNames(testCases,str1,i);
    						temp2=checkOtherNames(testCases,str2,j);
		 	    			if(temp1!=0 && compareAgain!="Do not change"){
		    					testMethodNameOptimized1 = str1;
		    					temp1=100;	    					
	    						compareAgain="Do not change";
		    				}
		    				if(temp2!=0){
			 	    			testMethodNameOptimized2 = str2;
			 	    			testOptimized.add(j);			 	    			
				 				setNameGeneratedFor(testCases.get(j), testMethodNameOptimized2);  					
			 	    			temp2=100;
		    				}
    					}
					}
					if(temp2!=100){
	    				temp1=temp2=-1;	 	    		
	 	    			str1 = "test"+testInputs.get(testCases.get(i));
    					str2 = "test"+testInputs.get(testCases.get(j));
    					if(!str1.equals(str2)){
    						temp1=checkOtherNames(testCases,str1,i);
    						temp2=checkOtherNames(testCases,str2,j);
		 	    			if(temp1!=0 && compareAgain!="Do not change"){
		    					testMethodNameOptimized1 = str1;
		    					temp1=100;	    					
	    						compareAgain="Do not change";
	    						getGoalType(testCases.get(i),"input");
		    				}
		    				if(temp2!=0){
			 	    			testMethodNameOptimized2 = str2;
			 	    			testOptimized.add(j);			 	    			
				 				setNameGeneratedFor(testCases.get(j), testMethodNameOptimized2); 
				 				getGoalType(testCases.get(j),"input");	    					
			 	    			temp2=100;
		    				}
    					}
					}
					if(temp2!=100){
	    				temp1=temp2=-1;	 	    		
	 	    			str1 = "test"+testInputArgType.get(testCases.get(i));
    					str2 = "test"+testInputArgType.get(testCases.get(j));
    					if(!str1.equals(str2) && !str1.equals("test") && !str2.equals("test")){
    						temp1=checkOtherNames(testCases,str1,i);
    						temp2=checkOtherNames(testCases,str2,j);
		 	    			if(temp1!=0 && compareAgain!="Do not change"){
		    					testMethodNameOptimized1 = str1;
		    					temp1=100;	    					
	    						compareAgain="Do not change";
		    				}
		    				if(temp2!=0){
			 	    			testMethodNameOptimized2 = str2;
			 	    			testOptimized.add(j);			 	    			
				 				setNameGeneratedFor(testCases.get(j), testMethodNameOptimized2);     					
			 	    			temp2=100;
		    				}
    					}
					}
					if(temp2!=100){
	    				temp1=temp2=-1;			    				
	 					str1= "test"+checkAssertions(testCases.get(i));
    					str2 = "test"+checkAssertions(testCases.get(j));
    					if(!str1.equals(str2)){
    						temp1=checkOtherNames(testCases,str1,i);
    						temp2=checkOtherNames(testCases,str2,j);
		 					if(temp1!=0 && compareAgain!="Do not change"){
		    					testMethodNameOptimized1 = str1;
		    					temp1=100;
		    					if(!str1.equals("test_checks")){
		    						compareAgain="Do not change";
		    					}
		    				}
		    				if(temp2!=0){
			 	    			testMethodNameOptimized2 = str2;
			 	    			if(!testMethodNameOptimized2.equals("test_checks")){
			 	    				testOptimized.add(j);
			 	    				setNameGeneratedFor(testCases.get(j), testMethodNameOptimized2); 			 	    			
			 	    			}
				 				temp2=100;
		    				}
    					}
					}
					if(temp2!=100){
	    				temp1=temp2=-1;	        
	 					str1 = "test"+testBranches.get(testCases.get(i));
    					str2 = "test"+testBranches.get(testCases.get(j));
    					if(!str1.equals(str2)){
    						temp1=checkOtherNames(testCases,str1,i);
    						temp2=checkOtherNames(testCases,str2,j);
		 					if(temp1!=0 && compareAgain!="Do not change"){
		    					testMethodNameOptimized1 = str1;
		    					temp1=100;
		    					compareAgain="Do not change";
		    					getGoalType(testCases.get(i),"branch");
		    				}
		    				if(temp2!=0){
			 	    			testMethodNameOptimized2 = str2;			 	    			
			 	    			testOptimized.add(j);
			 	    			setNameGeneratedFor(testCases.get(j), testMethodNameOptimized2); 
				 				getGoalType(testCases.get(j),"branch");
			 	    			temp2=100;
		    				}
    					}
					}
					if(temp2!=100){
	    				temp1=temp2=-1;	        
	 					str1 = "test"+testBranchArgType.get(testCases.get(i));
    					str2 = "test"+testBranchArgType.get(testCases.get(j));
    					if(!str1.equals(str2) && !str1.equals("test") && !str2.equals("test")){
    						temp1=checkOtherNames(testCases,str1,i);
    						temp2=checkOtherNames(testCases,str2,j);
		 					if(temp1!=0 && compareAgain!="Do not change"){
		    					testMethodNameOptimized1 = str1;
		    					temp1=100;
		    					compareAgain="Do not change";
		    				}
		    				if(temp2!=0){
			 	    			testMethodNameOptimized2 = str2;
			 	    			testOptimized.add(j);
				 				setNameGeneratedFor(testCases.get(j), testMethodNameOptimized2);
			 	    			temp2=100;
		    				}
    					}
					}
    			 }
    		}
		if(compareAgain=="Do not change" ){
 			    setNameGeneratedFor(testCases.get(i), testMethodNameOptimized1);
     	//    	System.out.println(testMethodNameOptimized1);
     	    	compareAgain="NO";	
 			}
 		}
    	String[] testName = new String[testCases.size()];
    	TestCase[] testCs = new TestCase[testCases.size()];
    	int count=0;
    	for(Map.Entry<TestCase,String> entry: testCaseNames.entrySet()){
    		testName[count]= entry.getValue();
    		testCs[count] = entry.getKey();
    		//System.out.println(testName[count]);
    		count++;
    	}
    	System.out.println(Arrays.toString(testName));
    	SimplifyMethodNames optimize = new SimplifyMethodNames();
    	testName = optimize.optimizeNames(Arrays.asList(testName));
    	for (int i=0; i<testName.length; i++) {        	 
    		if(!testName[i].contains("Exception") && !testName[i].contains("Throws")){
    			testName[i] = testName[i] + testExceptions.get(testCs[i]); // TODO
    		}          
        }    
    	testName = optimize.minimizeNames(testName);    	
    	testName = optimize.countSameNames(testName); 
    	
    	
    		
        for (int i=0; i<testName.length; i++) {        	           
            String testMethodNameOptimized = testName[i]; // TODO
            if(StringUtils.countMatches(testMethodNameOptimized, "_")>=2){
            	testMethodNameOptimized=
            			testMethodNameOptimized.substring(0, testMethodNameOptimized.lastIndexOf("_"))+
            			"And"+testMethodNameOptimized.substring(testMethodNameOptimized.lastIndexOf("_"),testMethodNameOptimized.length());
            }
         /*   if(testMethodNameOptimized.contains("Constructor") && testMethodNameOptimized.contains("Throwing") && testMethodNameOptimized.contains("Exception")){
            	testMethodNameOptimized=testMethodNameOptimized.replace("Constructor","FailsToCreate"+className);
            	
            	if(Character.isDigit(testMethodNameOptimized.charAt(testMethodNameOptimized.length()-1))){
            		testMethodNameOptimized=testMethodNameOptimized.replace(testMethodNameOptimized.substring(testMethodNameOptimized.indexOf("Throwing"), testMethodNameOptimized.length()-1), "");
            	}else
            	{
            		testMethodNameOptimized=testMethodNameOptimized.replace(testMethodNameOptimized.substring(testMethodNameOptimized.indexOf("Throwing"), testMethodNameOptimized.length()), "");
            	}
            }else{*/
	            if(testMethodNameOptimized.contains("Constructor")){
	            	testMethodNameOptimized=testMethodNameOptimized.replace("Constructor",className);
	            }
          //  }
	            testMethodNameOptimized = testMethodNameOptimized.replace("_", "");
	            
            setNameGeneratedFor(testCs[i], testMethodNameOptimized);         
            methodNames.add(testMethodNameOptimized);           
        }     
    		
    	
    }
    public static void main(String[] args){
    	String s= "test_ReadWith3ArgumentsOfTypeArrayByteIntInt_ReadThrowingNullPointerException";
    	s=s.replace("_", "");
    	
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
  
  public String checkAssertions(TestCase tc)  {
	  String code = tc.toCode();
	  AssertionExtraction assertions= new AssertionExtraction(code);
	  List<String> assertionStatements = new ArrayList<String>();
	  assertionStatements = assertions.getStatements();
	  String[] methodChecked = new String[3];
	  String name="_checks";
	  for(String str : assertionStatements){
		  methodChecked = str.split(",");
		  String idName="";
		  
		  
			  if(methodChecked.length==3){
				  str=methodChecked[1];
			  }else{
				  if(methodChecked.length==2){
					  str= methodChecked[1].substring(0,methodChecked[1].length()-2);
				  } else {
					  str= methodChecked[0].substring(methodChecked[0].indexOf("(")+1, methodChecked[0].length()-2);
				  }
			  }
			  
			  if(str.contains("(") && str.contains(")")){
				 if(str.contains(".")){
					 name+=WordUtils.capitalize(str.substring(str.indexOf(".")+1, str.indexOf("(")));
				 }else{
					 str = str.substring(str.indexOf(")"), str.lastIndexOf(")"));
				 }
			  } else{
					 //name= str;
					 IdentifierExtraction identiferes = new IdentifierExtraction(code);					 
					 Map<String,Integer> ids = identiferes.get_identifier_lines();
					 String [] codeLines = code.split("\n");
					 for(String id : identiferes.get_identifier_names()){
						 if(id.equals(str.trim())){
							 Integer s=ids.get(str.trim());
							 String line= codeLines[ids.get(str.trim())-2];
							 System.out.println(line);
							 MethodInvExtraction methods = new MethodInvExtraction(line);
							 String[] ids1 = new String[1];
							 if(line.contains(".") && line.contains("(") && line.contains(")")){
								 ids1 = methods.get_method_names();
								 name += WordUtils.capitalize(ids1[0]);
								 
							 }else{
								 name += "";								 
							 }
						 }
						 }
					 }
				 
			  
		  }
	  
	  return name;
  }
  
 
  private int checkOtherNames(List<TestCase> testCases,String str, int i){
	  int temp=-1;
	  for(int k=0; k<testCases.size(); k++){ 			    					
			//kontrollo nese emri qe po e ndron eshte i njete me emer tjeter ne liste
			if(testCaseNames.get(testCases.get(k)).equals(str) && k!=i){
				temp=0;
			}
				    					
		}
	  return temp;
  }
  private Class<?> getExceptionClassToUse(Throwable exception){
      /*
          we can only catch a public class.
          for "readability" of tests, it shouldn't be a mock one either
        */
      Class<?> ex = exception.getClass();
      while (!Modifier.isPublic(ex.getModifiers()) || EvoSuiteMock.class.isAssignableFrom(ex) ||
				ex.getCanonicalName().startsWith("com.sun.")) {
          ex = ex.getSuperclass();
      }
      return ex;
  }
 
  
}