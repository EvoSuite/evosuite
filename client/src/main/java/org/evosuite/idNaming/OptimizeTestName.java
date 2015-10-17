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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptimizeTestName {
	static List<Integer> position = new ArrayList<Integer>();
	
	public static String[] simplifyNames(String nameFirst, String nameSecond){
		System.out.println(nameFirst+"-"+nameSecond);
				String [] name1=nameFirst.split("_");
				String [] name2=nameSecond.split("_");
				nameFirst="";
				nameSecond="";
				for (int k=1; k<name1.length; k++){
					for (int t=1; t<name2.length; t++){
						if(name1[k].equals(name2[t])){
							if (name2.length>2){
								name2[t]="";
							}
							if (name1.length>2){
								name1[k]="";
							}
						}						
					}
				}
				int count=1;
				for (String temp: name2){
					if(temp!="" ){					
						if (count!=name2.length){
							nameSecond+=temp+"_";
						}else{
							nameSecond+=temp;
						}
					}					
					count++;
				}				
				count=1;
				for (String temp: name1){
					if(temp!="" ){
						if (count!=name1.length){
							nameFirst+=temp+"_";
						}else{
							nameFirst+=temp;
						}
					}					
					count++;
				}
				if(nameFirst.endsWith("_")){
					nameFirst=nameFirst.substring(0, nameFirst.length()-1);
				}
				if(nameSecond.endsWith("_")){
					nameSecond=nameSecond.substring(0, nameSecond.length()-1);
				}
				String [] result= {nameFirst, nameSecond};
				return result;						
	}
	public static void main(String[] args){
		List<String> names= new ArrayList<String>();
		names.add("testWithComplexFalse");
		names.add("testWithComplexFalse");
		names.add("testWithComplexFalse");
		names.add("test_WithComplexTrue_WithComplexTrue_WithComplexTrue");
		names.add("test_WithSimpleFalse");
		names.add("test_Init_WithSimpleTalse");
		optimiseNames(names);
	}
	
	public static  String[] optimiseNames(List<String> nameList){
		int countSameTokens = 0;
		int prevSameTokens = 1;
		String nameToOptimize = "";
		String temp = "";
		String[] methodNames =  new String [nameList.size()];
		List<Integer> saveToken =  new ArrayList<Integer>();
		String []methodName = new String [nameList.size()];
		saveToken.add(-1);
		for(int i = 0; i<nameList.size(); i++){
			int t=0;
			prevSameTokens = 1;
			int k = 0, ki=0;
			// check whether nameList[i] is already optimised. If it is make t=100 just to notice later
			for(int c = 0; c<saveToken.size(); c++){
				if(i == saveToken.get(c)){
					t=100;								
				}
			} if(t!=100){					
					for(int j=i+1; j<nameList.size(); j++){
						k = 0;
						// check whether nameList[j] is already optimised. If it is make k=100 just to notice later
						for(int ci = 0; ci<saveToken.size(); ci++){
							if(j == saveToken.get(ci)){
								k=100;							
							}
						}if(k!=100) {			
							//divide names in tokens and check same tokens
							String [] name1=nameList.get(i).split("_");
							String [] name2=nameList.get(j).split("_");
							if(name1.length >1 && name2.length>2){
								countSameTokens=0;
								for(String token1 : name1){
									for(String token2 : name2){
										if(token1.equals(token2)){
											//count same tokens in first and second name
											countSameTokens++;
										}
									}					
								}
								temp =nameList.get(j);
								//check which name has more same tokens with nameList[i]
								if(countSameTokens > prevSameTokens){
									prevSameTokens = countSameTokens;
									nameToOptimize=temp;
									ki=j;
								}		
							}
							}							
						}						
					}
			//if nameList[i] and nameList[j] are not optimised and they have more than 1 tokens in common. 1 is always in common (test_)
			if(prevSameTokens > 1 && t!=100 && k!=100){
				methodName=simplifyNames(nameList.get(i), nameToOptimize);
				prevSameTokens = -1;
				saveToken.add(i);
				saveToken.add(ki);	
				
					methodNames[i]=methodName[0];
					methodNames[ki]=methodName[1];
					System.out.println(Arrays.toString(methodNames));
					
							
			}
			//if nameList[i] and nameList[j] are not optimised and nameList[i] is the last name in the list and do not have with whom to be compared
			if(t!=100 && k!=100 && i == nameList.size()-1){				
					methodNames[i]=nameList.get(i);
					saveToken.add(i);			
			} else{
				//if nameList[i] and nameList[j] are not optimised and they have 1 token in common just add nameList[i] in the list and continue
				if(prevSameTokens == 1 && t!=100 && k!=100){
					methodNames[i]=nameList.get(i);
					saveToken.add(i);
					
				}else{
					//if nameList[i] is not optimised put it in the list
					if(t!=100&&k==100){
						methodNames[i]=nameList.get(i);
						saveToken.add(i);
					}
				}
			}			
		}
		//save original name position how they are optimised. This is to have the right name in the right position in the end
		for (int i=1; i<saveToken.size(); i++){
			position.add(saveToken.get(i));
		}
		System.out.println(saveToken);
		minimizeNames(methodNames);
		return  methodNames;
	}
	public  static String[] minimizeNames (String[] names){		
		for(int i=0; i<names.length; i++){
			String [] tokens = names[i].split("_");
			String newName=names[i];
			int nameFound=-1;
			String newName2=names[i];
			if(tokens.length>2){
			//	outerloop:
				for(int k=tokens.length-1; k>=2; k--){
					newName=tokens[0];					
					newName=newName2.substring(0,newName2.lastIndexOf("_"+tokens[k]));
					for (int j=0; j<names.length; j++){
						if(i==j){							
						}else{
							if(newName.equals(names[j])){
								nameFound=1;
								break;
							} else{
								nameFound=0;
							}
						}						
					}					
					if(nameFound==1){
						break;
					}else{
						names[i] = newName;
						newName2= newName;
					}
				}
			}			
		}
		System.out.println(Arrays.toString(names));
		return names;
	}
	public static List<Integer> testPosition(){
		return position;
	}
	
}
