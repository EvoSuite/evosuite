package org.evosuite.idNaming;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.input.InputCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.output.OutputCoverageTestFitness;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

public class MethodName extends MethodArguments{
	private String methodName;
	private int noOfArguments;
	private String argTypes;
	private String methodArguments;
	private String methodArgumentTypes;
	public String className;;
	public MethodName(TestCase tc){
		methodName = "";
		noOfArguments = 0;
		argTypes = "";
		methodArguments = "";
		methodArgumentTypes = "";
		className = "";
		setMethodGoal(tc);
	}	
	public void setMethodGoal(TestCase tc){
		Set<? extends TestFitnessFunction> goals = tc.getCoveredGoals();		
		for (TestFitnessFunction goal : goals) {
  		  	String goalName = goal.toString();  		  	
  		  	if (goal instanceof MethodCoverageTestFitness) {
  		  	String str = goalName.substring(goalName.lastIndexOf("(")+1,goalName.lastIndexOf(")"));
  		    className=goalName.substring(0,goalName.lastIndexOf("."));
		  	className=className.substring(className.lastIndexOf(".")+1, className.length());
	  		
  		  		methodName+="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")));	
		  		noOfArguments = countArguments(str);
		  		argTypes=getArgumentTypes(str);
			  	if(noOfArguments==0){
					methodArguments +="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+"WithNoArgument";
				}else{
					if(noOfArguments==1){
						methodArguments +="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+"With"+noOfArguments+"Argument";
						methodArgumentTypes +="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+"With"+noOfArguments+"ArgumentOfType"+argTypes;	
			  		}else{
			  			methodArguments +="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+"With"+noOfArguments+"Arguments";
			  			methodArgumentTypes +="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+"With"+noOfArguments+"ArgumentsOfType"+argTypes;	
			  		}
				}
  		  	}
		}
	}
	public String getMethodGoal(){
		return methodName;
	}
	public int getNoOfArguments(){
		return noOfArguments;
	}
	public String getMethodArgumentTypes(){
		return methodArgumentTypes;
	}
	public String getMethodArguments(){
		return methodArguments;
	}

	 
}
