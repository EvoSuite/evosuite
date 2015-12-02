package org.evosuite.idNaming;

import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;

public class BranchName extends MethodArguments{
	private String branchName;
	private int noOfArguments;
	private String argTypes;
	private String branchArguments;
	private String branchArgumentTypes;
	public BranchName(TestCase tc){
		noOfArguments = 0;
		argTypes = "";
		branchArguments = "";
		branchArgumentTypes = "";
		branchName="";
		setBranchGoal(tc);
	}	
	public void setBranchGoal(TestCase tc){
		Set<? extends TestFitnessFunction> goals = tc.getCoveredGoals();		
		for (TestFitnessFunction goal : goals) {
  		  	String goalName = goal.toString();  		  	
  			if (goal instanceof BranchCoverageTestFitness){
  				String str = goalName.substring(goalName.lastIndexOf("(")+1,goalName.lastIndexOf(")"));
	  		//	if(goalName.contains("root-Branch")){
	  				branchName+="_Invokes"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")));
	  				noOfArguments = countArguments(str);
	  			  	if(noOfArguments==0){
	  					branchArguments +=branchName+"WithNoArgument";
	  				}else{
	  					if(noOfArguments==1){
	  						branchArguments +=branchName+"With"+noOfArguments+"Argument";
	  			  		}else{
	  			  			branchArguments +=branchName+"With"+noOfArguments+"Arguments";
	  			  		}
	  				}
	  				argTypes=getArgumentTypes(str);			  	
				  	branchArgumentTypes += branchArguments+"OfType"+argTypes.toString();
			//	} 				
	  		/*	else{
	  				branchName+="_Calls"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")));//+
		  					//WordUtils.capitalize(goalName.substring(goalName.indexOf(" - ")+3))+"Branch";
	  				noOfArguments = countArguments(str);
	  			  	if(noOfArguments==0){
	  			  		branchArguments +="_Calls"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+
	  			  				"WithNoArgument";//+
			  					//WordUtils.capitalize(goalName.substring(goalName.indexOf(" - ")+3))+"Branch";
	  				}else{
	  					if(noOfArguments==1){
	  						branchArguments +="_Calls"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+
	  				  				"With"+noOfArguments+"Argument";//+
				  				//	WordUtils.capitalize(goalName.substring(goalName.indexOf(" - ")+3))+"Branch";
	  			  		}else{
	  			  		branchArguments +="_Calls"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+
	  			  				"With"+noOfArguments+"Arguments";//+
			  				//	WordUtils.capitalize(goalName.substring(goalName.indexOf(" - ")+3))+"Branch";
	  			  		}
	  				}
	  			  	//test_CallsAWithXArgumentsOfTypeYTrueBranch;
	  			 	argTypes=getArgumentTypes(str);			  	
				  	branchArgumentTypes +="_Calls"+WordUtils.capitalize(goalName.substring(goalName.lastIndexOf(".")+1,goalName.indexOf("(")))+
  			  				"With"+noOfArguments+"Arguments"+"OfType"+argTypes.toString();//+
		  				//	WordUtils.capitalize(goalName.substring(goalName.indexOf(" - ")+3))+"Branch";
				}*/
  			}
		}
	}
	public String getBranchGoal(){
		return branchName;
	}
	public int getNoOfArguments(){
		return noOfArguments;
	}
	public String getBranchArgumentTypes(){
		return branchArgumentTypes;
	}
	public String getBranchArguments(){
		return branchArguments;
	}
}
