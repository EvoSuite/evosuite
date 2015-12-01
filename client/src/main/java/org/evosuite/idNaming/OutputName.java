package org.evosuite.idNaming;

import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.output.OutputCoverageTestFitness;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;

public class OutputName extends MethodArguments{
	private String outputName;
	private int noOfArguments;
	private String argTypes;
	private String outputArguments;
	private String outputArgumentTypes;
	public OutputName(TestCase tc){		
		outputName = "";
		noOfArguments = 0;
		argTypes = "";
		outputArguments = "";
		outputArgumentTypes = "";
		setOutputGoal(tc);
	}
	
	public void setOutputGoal(TestCase tc){
		Set<? extends TestFitnessFunction> goals = tc.getCoveredGoals();		
		for (TestFitnessFunction goal : goals) {
  		  	String goalName = goal.toString();  		  	
	  		  if (goal instanceof OutputCoverageTestFitness ) {	
	  			String str = goalName.substring(goalName.lastIndexOf("(")+1,goalName.lastIndexOf(")"));
					if(StringUtils.countMatches(goalName, "(")==2){
						String[] outputN=goalName.split(":");
						outputName+="_"+WordUtils.capitalize(outputN[0].substring(outputN[0].lastIndexOf(".")+1,outputN[0].indexOf("(")))+"With"+
								WordUtils.capitalize(outputN[3].substring(0,outputN[3].indexOf("(")))+
								WordUtils.capitalize(outputN[4]);
						noOfArguments = countArguments(str);
						argTypes=getArgumentTypes(str);	
		  			  	if(noOfArguments==0){
		  			  		outputArguments +="_"+WordUtils.capitalize(outputN[0].substring(outputN[0].lastIndexOf(".")+1,outputN[0].indexOf("(")))+
		  			  				"WithNoArgument"+"With"+
									WordUtils.capitalize(outputN[3].substring(0,outputN[3].indexOf("(")))+
									WordUtils.capitalize(outputN[4]);
		  			  		
		  				}else{
		  					if(noOfArguments==1){
		  						outputArguments +="_"+WordUtils.capitalize(outputN[0].substring(outputN[0].lastIndexOf(".")+1,outputN[0].indexOf("(")))+
		  				  				"With"+noOfArguments+"Argument"+"With"+
										WordUtils.capitalize(outputN[3].substring(0,outputN[3].indexOf("(")))+
										WordUtils.capitalize(outputN[4]);
		  						outputArgumentTypes+="_"+WordUtils.capitalize(outputN[0].substring(outputN[0].lastIndexOf(".")+1,outputN[0].indexOf("(")))+
		  				  				"With"+noOfArguments+"Argument"+"OfType"+argTypes.toString()+"With"+
										WordUtils.capitalize(outputN[3].substring(0,outputN[3].indexOf("(")))+
										WordUtils.capitalize(outputN[4]);
		  			  		}else{
		  			  			outputArguments +="_"+WordUtils.capitalize(outputN[0].substring(outputN[0].lastIndexOf(".")+1,outputN[0].indexOf("(")))+
			  			  				"With"+noOfArguments+"Arguments"+"With"+
										WordUtils.capitalize(outputN[3].substring(0,outputN[3].indexOf("(")))+
										WordUtils.capitalize(outputN[4]);
		  			  			outputArgumentTypes+="_"+WordUtils.capitalize(outputN[0].substring(outputN[0].lastIndexOf(".")+1,outputN[0].indexOf("(")))+
		  			  					"With"+noOfArguments+"Arguments"+"OfType"+argTypes.toString()+"With"+
			  			  				WordUtils.capitalize(outputN[3].substring(0,outputN[3].indexOf("(")))+
										WordUtils.capitalize(outputN[4]);
		  			  		}
		  				}
					}else{
						outputName+="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+"Returning"+
							WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(":")+1));	
						noOfArguments = countArguments(str);
						argTypes=getArgumentTypes(str);	
		  			  	if(noOfArguments==0){
		  			  		outputArguments +="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+
		  			  				"WithNoArgument"+"Returning"+
									WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(":")+1));
		  				}else{
		  					if(noOfArguments==1){
		  						outputArguments +="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+
			  			  				"With"+noOfArguments+"Argument"+"Returning"+
										WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(":")+1));
		  						outputArgumentTypes+="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+
			  			  				"With"+noOfArguments+"Argument"+"OfType"+argTypes.toString()+"Returning"+
										WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(":")+1));
		  			  		}else{
		  			  			outputArguments +="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+
			  			  				"With"+noOfArguments+"Arguments"+"Returning"+
										WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(":")+1));
		  			  			outputArgumentTypes+="_"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+
		  			  					"With"+noOfArguments+"Arguments"+"OfType"+argTypes.toString()+"Returning"+
		  			  					WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(":")+1));
		  			  		}
		  				}
					}
				}
		}
	}
	public String getOutputGoal(){
		return outputName;
	}
	public int getNoOfArguments(){
		return noOfArguments;
	}
	public String getOutputArgumentTypes(){
		return outputArgumentTypes;
	}
	public String getOutputArguments(){
		return outputArguments;
	}

}
