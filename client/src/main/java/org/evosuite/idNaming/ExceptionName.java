package org.evosuite.idNaming;

import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.input.InputCoverageTestFitness;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;

public class ExceptionName {
	private String exceptionName;
	public ExceptionName(TestCase tc){
		exceptionName = "";
		setExceptionGoal(tc);
	}
	
	
	public void setExceptionGoal(TestCase tc){
		Set<? extends TestFitnessFunction> goals = tc.getCoveredGoals();		
		for (TestFitnessFunction goal : goals) {
			String goalName = goal.toString();  		  	
			if (goal instanceof ExceptionCoverageTestFitness) {
				exceptionName+="_"+WordUtils.capitalize(goalName.substring(0,goalName.indexOf("(")))+
						"Throwing"+goalName.substring(goalName.lastIndexOf(".")+1, goalName.lastIndexOf("_"));
		  	}
		}
	}
	public String getExceptionGoal(){
		return exceptionName;
	}

}
