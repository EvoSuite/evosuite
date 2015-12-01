package org.evosuite.idNaming;

import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.evosuite.coverage.input.InputCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;

public class InputName extends MethodArguments{
	private String inputName;
	private int noOfArguments;
	private String argTypes;
	private String inputArguments;
	private String inputArgumentTypes;
	public InputName(TestCase tc){
		inputName = "";
		noOfArguments = 0;
		argTypes = "";
		inputArguments = "";
		inputArgumentTypes = "";
		setInputGoal(tc);
	}
	
	
	public void setInputGoal(TestCase tc){
		Set<? extends TestFitnessFunction> goals = tc.getCoveredGoals();		
		for (TestFitnessFunction goal : goals) {
			String goalName = goal.toString();  		  	
			if (goal instanceof InputCoverageTestFitness ) {	
				String str = goalName.substring(goalName.lastIndexOf("(")+1,goalName.lastIndexOf(")"));
				if(StringUtils.countMatches(goalName, "(")==2){
					String[] inputN=goalName.split(":");
					inputName+="_"+WordUtils.capitalize(inputN[0].substring(inputN[0].lastIndexOf(".")+1,inputN[0].indexOf("(")))+"With"+
							WordUtils.capitalize(inputN[3].substring(0,inputN[3].indexOf("(")))+
							WordUtils.capitalize(inputN[4]);
					noOfArguments = countArguments(str);
					argTypes=getArgumentTypes(str);
	  			  	if(noOfArguments==0){
	  			  		inputArguments +="_"+WordUtils.capitalize(inputN[0].substring(inputN[0].lastIndexOf(".")+1,inputN[0].indexOf("(")))+
	  			  				"WithNoArgument"+"With"+
								WordUtils.capitalize(inputN[3].substring(0,inputN[3].indexOf("(")))+
								WordUtils.capitalize(inputN[4]);
	  				}else{
	  					if(noOfArguments==1){
	  						inputArguments +="_"+WordUtils.capitalize(inputN[0].substring(inputN[0].lastIndexOf(".")+1,inputN[0].indexOf("(")))+
	  				  				"With"+noOfArguments+"Argument"+"With"+
									WordUtils.capitalize(inputN[3].substring(0,inputN[3].indexOf("(")))+
									WordUtils.capitalize(inputN[4]);
	  						inputArgumentTypes +="_"+WordUtils.capitalize(inputN[0].substring(inputN[0].lastIndexOf(".")+1,inputN[0].indexOf("(")))+
	  				  				"With"+noOfArguments+"Argument"+"OfType"+argTypes.toString()+"With"+
									WordUtils.capitalize(inputN[3].substring(0,inputN[3].indexOf("(")))+
									WordUtils.capitalize(inputN[4]);
	  			  		}else{
	  			  			inputArguments +="_"+WordUtils.capitalize(inputN[0].substring(inputN[0].lastIndexOf(".")+1,inputN[0].indexOf("(")))+
		  			  				"With"+noOfArguments+"Arguments"+"With"+
									WordUtils.capitalize(inputN[3].substring(0,inputN[3].indexOf("(")))+
									WordUtils.capitalize(inputN[4]);
	  			  			inputArgumentTypes +="_"+WordUtils.capitalize(inputN[0].substring(inputN[0].lastIndexOf(".")+1,inputN[0].indexOf("(")))+
	  			  					"With"+noOfArguments+"Arguments"+"OfType"+argTypes.toString()+"With"+
	  			  					WordUtils.capitalize(inputN[3].substring(0,inputN[3].indexOf("(")))+
	  			  					WordUtils.capitalize(inputN[4]);
	  			  		}
	  				}
				} else{
					inputName+="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+"With"+
						WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(":")+1))+"Input";	
					noOfArguments = countArguments(str);
					argTypes=getArgumentTypes(str);
	  			  	if(noOfArguments==0){
	  			  		inputArguments +="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+
	  			  				"WithNoArgument"+"With"+
								WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(":")+1))+"Input";
	  				}else{
	  					if(noOfArguments==1){
	  						inputArguments +="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+
	  				  				"With"+noOfArguments+"Argument"+"With"+
									WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(":")+1))+"Input";
	  						inputArgumentTypes +="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+
	  				  				"With"+noOfArguments+"Argument"+"OfType"+argTypes.toString()+"With"+
									WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(":")+1))+"Input";
	  			  		}else{
	  			  			inputArguments +="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+"With"+noOfArguments+"Arguments"
	  			  					+"With"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(":")+1))+"Input";
	  			  			inputArgumentTypes +="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+
	  			  					"With"+noOfArguments+"Arguments"+"OfType"+argTypes.toString()+"With"+
	  			  					WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(":")+1))+"Input";
	  			  		}
	  				}
				}
			}
		}
	}
	public String getInputGoal(){
		return inputName;
	}
	public int getNoOfArguments(){
		return noOfArguments;
	}
	public String getInputArgumentTypes(){
		return inputArgumentTypes;
	}
	public String getInputArguments(){
		return inputArguments;
	}
}
