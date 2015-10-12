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

public class SimplifyMethodNames extends ShorterNames{
	
	private static SimplifyMethodNames instance=null;
	
    public static synchronized SimplifyMethodNames getInstance() {
        if (instance == null)
            instance = new SimplifyMethodNames();

        return instance;
    }

    public String[] optimizeNames(List<String> nameList){
		SimplifyMethodNames simple= new SimplifyMethodNames();
		String[] methodNames = nameList.toArray(new String[nameList.size()]) ;
		for(int i=0; i<nameList.size(); i++){
			String [] name1=methodNames[i].split("_");
			List<String> list1 = new ArrayList(Arrays.asList(name1));
			int prevInter=0;
			List<String> optName=null;
			List<String> intersection=null;
			List<String> bestInter = null;
			String secondName="";
			int position=0;
			for (int j=i+1; j<nameList.size(); j++){				
				String [] name2=methodNames[j].split("_");				
				List<String> list2 = new ArrayList(Arrays.asList(name2));
				List<String> union = new ArrayList<String>(list1);
				union.addAll(list2);
				// Prepare an intersection
				if(name1.length>name2.length){
					intersection = new ArrayList<String>(list2);	
					intersection.retainAll(list1);
				} else {
					intersection = new ArrayList<String>(list1);	
					intersection.retainAll(list2);
				}
				if(prevInter < intersection.size() && intersection.size()>1){
					prevInter=intersection.size();
					optName = list2;
					position = j;
					bestInter = intersection;
					secondName = methodNames[j];
				}				
			}
			if(prevInter!=0){
				String[] optNames=minimizePair(methodNames[i], secondName, nameList);				
					methodNames[i] = optNames[0];
					methodNames[position] = optNames[1];
			}
		}
	//methodNames = simple.minimizeNames(methodNames);
	//	methodNames = simple.countSameNames(methodNames); 
		return methodNames;
	}
	
  public static void main(String[] args){
	  String[] name={"test_removeWithPositiveInput_removeThrowingArrayIndexOutOfBoundsException",
			  "test_removeThrowingArrayIndexOutOfBoundsException",
			  "test_removeWithPositiveInput"};
	  minimizeFurther(name);
  }
	
    public static String[] minimizeNames (String[] names){		
		for(int i=0; i<names.length; i++){
			String [] tokens = names[i].split("_");
			String newName=names[i];
			int nameFound=-1;
			String newName2=names[i];
			if(tokens.length>2){
			//	outerloop:
				for(int k=tokens.length-1; k>=2; k--){
					newName=tokens[0];					
					if(tokens[k].contains("Exception") && tokens[k].contains("Throwing")){
						k--;
					}
					if(newName2.contains("_init_") ){
						newName2 = newName2.replace("_init_", "_");
						tokens = newName2.split("_");
						k--;
						
						newName = newName2.replace("_init_", ""); 
						if(newName2.split("_").length>2){
							
						}else {
							names[i] = newName;
							break;
						}
					}
				//	newName=newName2.substring(newName2.indexOf("_"+tokens[i]),newName2.lastIndexOf("_"+tokens[k]));
					if(k==tokens.length-1){
						newName = newName2.replace("_"+tokens[k], "");
					} else{
						newName = newName2.replaceFirst("_"+tokens[k]+"_", "_");
					}
					
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
		for(int i=0; i<names.length; i++){
			String newName=names[i];
			int nameFound=-1;
			if(names[i].split("_").length>2){
				String [] tokensInName = names[i].split("_");
				for(int l=1; l<tokensInName.length-1; l++){
					newName = names[i].replace("_"+tokensInName[l]+"_", "_");
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
					}
				}
			}
		}
		minimizeFurther(names);
	//	System.out.println(Arrays.toString(names));
		return names;
	}
    public static void minimizeFurther(String[] names){
    	String firstName="";
    	String nameInException="";
    	int found=-1;
    	int crash =-1;
    	String newName="";
    	for(int i=0; i<names.length; i++){
    		if(names[i].split("_").length>2){
    		found=-1;
    		crash =-1;
    		firstName="";
    		nameInException="";
    		newName= names[i];
    		System.out.println(names[i]);
			if(names[i].contains("Exception") && names[i].contains("Throwing")){
				nameInException = names[i].substring(names[i].lastIndexOf("_")+1, names[i].lastIndexOf("Throwing"));
				String[] tokens = names[i].split("_");
				for(int j = 1; j< tokens.length-1; j++ ){
					firstName = tokens[j].split("(?=\\p{Upper})")[0];
					if(firstName.equals(nameInException)){
						found=100;
						firstName=tokens[j];
						newName= names[i].replaceFirst("_"+nameInException+"Throwing", "Throwing"); 
						for(int k=0; k<names.length; k++){
							if (newName.equals(names[k])){
								crash = 100;
								break;
							}
						}
						if(crash !=100){
							names[i]=newName;
						}
						crash=-1;
					}
				}
			}
    		}
    	}
    	
    }
	
    public String[] countSameNames(String[] nameList){
		int temp=0;
		for (int i=0; i<nameList.length; i++){
			temp=1;
			for (int j=i+1; j<nameList.length; j++){
				if(nameList[i].equals(nameList[j])){
					String[] tokens= nameList[j].split("_");
					//nameList[j]=nameList[j].replace(tokens[0], tokens[0]+temp);
					nameList[j]=nameList[j]+temp;
					temp++;
				}
			}
			if(temp>1){
				String[] tokens= nameList[i].split("_");
				nameList[i]=nameList[i]+"0";
			}
		}
		return nameList;
	}	
}
