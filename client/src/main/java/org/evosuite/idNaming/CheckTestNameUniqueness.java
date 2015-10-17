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
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;

import java.util.List;
import java.util.Set;

public class CheckTestNameUniqueness {
	
	
	public CheckTestNameUniqueness(){
	}
	public static void checkNameUniqueness(String[] nameList, String[] testList){
		
		for(int i = 0; i<nameList.length; i++){
			for(int j=i+1; j<nameList.length; j++){
				if(nameList[i] == nameList[j]){
					String[] codeLines = testList[i].split("\n");
					for(String codeLine:codeLines){
						if(codeLine.contains(nameList[i]) && codeLine.contains("=")) {
							//take method parameters
							
							String parameters= codeLine.substring(codeLine.indexOf("="), codeLine.lastIndexOf(")"));
							parameters=parameters.substring(parameters.indexOf("."));
							parameters=parameters.substring(parameters.indexOf("("));
							String[] parameter=parameters.split(",");
							int k=0;
							for(String var :parameter){
								var = var.trim();
								if(var.contains(" ")){
									parameter[k]=var.substring(var.indexOf(" "));
									k++;
								}
							}
							k=0;
						//	nameList[i] =methodInLine[0];
							for(String lineForVar: codeLines){
								if(k<parameter.length){
									if (lineForVar.contains(parameter[k]) && lineForVar.contains("=")){
										lineForVar=lineForVar.trim();
										parameter[k]=lineForVar.substring(0, lineForVar.indexOf(" "));
										
										nameList[i] +=parameter[k];
										k++;
									}
								}
							}
							
						}
					}
				}
			}
		}
				
	}

	public static String[] renameMethods(String[] nameList){
	//	Set<Integer> savePos= new HashSet<Integer>();
		
		int temp=0;
		for (int i=0; i<nameList.length; i++){
			temp=1;
			for (int j=i+1; j<nameList.length; j++){
				if(nameList[i].equals(nameList[j])){
					//savePos.add(i);
					//savePos.add(j);
					String[] tokens= nameList[j].split("_");
					nameList[j]=nameList[j].replace(tokens[0], tokens[0]+temp);
					temp++;
				}
			}
			if(temp>1){
				String[] tokens= nameList[i].split("_");
				nameList[i]=nameList[i].replace(tokens[0], tokens[0]+"0");
				//nameList[i]=nameList[i]+"0";
			}
		}
		return nameList;
		
	}
	public static void main(String[] args){
		String[] names={"eri","ermi", "eri","ermi", "eri"};
		renameMethods(names);
	}
	
	public static String[] checkNames(String[] names, List<TestCase> tc){
		for(int i=0; i<names.length; i++){
			for(int j=i+1; j<names.length; j++){
				if(names[i].equals(names[j])){
					String currentName=names[i];
					names[i]=getTestName(tc.get(i));
					if(names[i].equals(currentName)){
						names[j]=getTestName(tc.get(j));
					}
				
					
				}
			}
		}
		names=renameMethods(names);
		return names;
	}
	public static String getTestName(TestCase tc){
		String testName = "";
		//goal set 
		Set<? extends TestFitnessFunction> goals = tc.getCoveredGoals();
		
		testName =goals.toString();
		String goalNames[]=testName.split(", ");
		
		testName="test";
	
		for(String goal: goalNames){				
			if(goal.contains("root-Branch")){
				testName+="_"+goal.substring(goal.lastIndexOf(".")+1,goal.indexOf("("));					
			} 				
			if(goal.contains("Branch") && goal.contains(" - true")){
				testName+="_With"+WordUtils.capitalize(goal.substring(goal.lastIndexOf(".")+1,goal.indexOf("(")))+"(True)";
			}
			if(goal.contains("Branch") && goal.contains(" - false")){
				testName+="_With"+WordUtils.capitalize(goal.substring(goal.lastIndexOf(".")+1,goal.indexOf("(")))+"(False)";
			}
			
		}
		testName = testName.replace("<","").replace(">","").replace("(","").replace(")","");
		return testName;
	}
}
 