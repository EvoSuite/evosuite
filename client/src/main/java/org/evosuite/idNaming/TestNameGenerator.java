package org.evosuite.idNaming;

import org.evosuite.Properties;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.utils.LoggingUtils;

import java.util.*;

/**
 * This class implements a test method name generator.
 *
 * It provides two main public interfaces:
 *
 * Method {@code execute}: executes test name generation algorithm, including
 * a phase of optimization.
 * Method {@code getNameGeneratedFor}: returns the name generated for a given test case.
 */
public class TestNameGenerator {

    private List<String> methodNames = new ArrayList<String>();
    private List<String> testCase = new ArrayList<String>();
    private List<Integer> methodPosition = new ArrayList<Integer>();

    /**
     * Mapping from test case to test case name
     */
    private Map<TestCase, String> testCaseNames = new HashMap<TestCase, String>();

    /**
     * TestNameGenerator instance
     */
    private static TestNameGenerator instance = null;

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
        TestNameGenerator generator = getInstance();

        // First, let's try to generate names for each test case individually
        for (int id = 0; id < testCases.size(); id++) {
            TestCase tc = testCases.get(id);
            ExecutionResult res = results.get(id);

            // find out target method
            String targetMethod = generator.getTargetMethod(tc, res);

            // generate test name
            String testMethodName = generator.generateTestName(targetMethod, tc, res, id);

            // save generated test name
            generator.setNameGeneratedFor(tc, testMethodName);
        }

        // Now, we may have conflicts between two (or more?) different tests.
        // We may even have opportunity to optimize the generated names further.
        // TODO: Should names be optimized only if all tests will be written in the same file? For now, yes.
        if (Properties.OUTPUT_GRANULARITY == Properties.OutputGranularity.MERGED) {
            generator.optimize(testCases, results);
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
        String testName = "";
        //goal set
        Set<? extends TestFitnessFunction> goals = tc.getCoveredGoals();

        testName = goals.toString();
        String goalNames[] = testName.split(", ");

        testName = "test";

        for (String goal : goalNames) {
		/*	if(goal.contains("root-Branch")){
				testName+="_"+goal.substring(goal.lastIndexOf(".")+1,goal.indexOf("("));					
			} 				
			if(goal.contains("Branch") && goal.contains(" - true") && testName.split("_").length==1){
				testName+="_With"+WordUtils.capitalize(goal.substring(goal.lastIndexOf(".")+1,goal.indexOf("(")))+"(True)";
			}
			if(goal.contains("Branch") && goal.contains(" - false") && testName.split("_").length==1){
				testName+="_With"+WordUtils.capitalize(goal.substring(goal.lastIndexOf(".")+1,goal.indexOf("(")))+"(False)";
			}*/
            if (!goalNames[0].equals("[]")) {
                testName += "_" + goal.substring(goal.lastIndexOf(".") + 1, goal.indexOf("("));
            }
        }


        testName = testName.replace("<", "").replace(">", "").replace("(", "").replace(")", "");
        //	testName = testName + num;
        LoggingUtils.getEvoLogger().debug("Name for test case " + id + ": " + testName);
        LoggingUtils.getEvoLogger().debug("Code for test case " + id + ":\n" + tc.toCode());

        //check if the same name and test case is already traversed
        int tempCount = -1;
        for (String test : testCase) {
            if (test.equals(tc.toCode())) {
                tempCount = 200;
                break;
            }
        }
        if (tempCount == -1) {
            testCase.add(tc.toCode());
            methodNames.add(testName);
        }

        return testName;
    }

    /**
     * Once names have been generated for all tests, resolve conflicts and optimize names.
     */
    private void optimize(List<TestCase> testCases, List<ExecutionResult> results) {

        for (TestCase tc : testCaseNames.keySet()) {
            // to retrieve the current test name:
            String testMethodName = testCaseNames.get(tc);

            // TODO: Recover Ermira's code that uses positions?

            //int pos = getPos(testMethodName, methodPosition, tc.toCode());
            //methodPosition.add(pos);
            //String[] names = optimizeNames();
            //	String [] names=TestNameGenerator.methodNames.toArray(new String[0]);
            //	List<TestCase> testCase = TestNameGenerator.testCase1;
            //names = CheckTestNameUniqueness.checkNames(names, testCases);

            //String optimizedTestName = names[pos];

            String testMethodNameOptimized = testMethodName; // TODO
            // to set the new, optimized test name:
            setNameGeneratedFor(tc, testMethodNameOptimized);
        }
    }

    private String[] optimizeNames() {
        String[] testNames = new String[methodNames.size()];
        int i = 0;
        for (String name : OptimizeTestName.optimiseNames(methodNames)) {
            testNames[i] = name;
            i++;
        }
        testNames = CheckTestNameUniqueness.renameMethods(testNames);
        return testNames;
    }

    private int getPos(String name, List<Integer> posFound, String test) {
        int pos = -1;
        int temp = 0;
        for (int i = 0; i < methodNames.size(); i++) {
            temp = 0;
            if (name.equals(methodNames.get(i)) && test.equals(testCase.get(i))) {
                for (int previousPos : posFound) {
                    if (previousPos == i) {
                        temp = -2;
                        break;
                    }
                }
                if (temp != -2) {
                    pos = i;
                    break;
                }
            }
        }
        return pos;
    }
    /**
     * Infers the target Method Under Test
     *
     * @param tc  test case
     * @param res execution result
     */
    private String getTargetMethod(TestCase tc, ExecutionResult res) {
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

}
