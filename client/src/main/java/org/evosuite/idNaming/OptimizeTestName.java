package org.evosuite.idNaming;

import java.util.ArrayList;
import java.util.List;

public class OptimizeTestName {

	public static void optimize_names(String nameFirst, String nameSecond){
		System.out.println(nameFirst+"-"+nameSecond);
				String [] name1=nameFirst.split("_");
				String [] name2=nameSecond.split("_");
				nameFirst="";
				nameSecond="";
				for (int k=0; k<name1.length; k++){
					for (int t=0; t<name2.length; t++){
						if(name1[k].equals(name2[t])){
							name2[t]="";
							name1[k]="";
						}
						
					}
				}
				int count=1;
				for (String temp: name2){
					if(temp=="" ){
						
					}else{
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
					if(temp=="" ){
						
					}else{
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
				
				System.out.println(nameFirst+"-"+nameSecond);
						
	}
	public static void main(String[] args){
		String[] names={"a1_a2_a3","a1_a2_a4","a7_a5_a3","a10_a6_a7","a7_a5_a4","ai_aj+ak"};
		optNames(names);
	}
	
	public static void optNames(String[] nameList){
		String []tempName = new String [nameList.length];
		int countSameTokens = 0;
		int prevSameTokens = 0;
		String nameToOptimize = "";
		String temp = "";
		
		List<Integer> saveToken =  new ArrayList<Integer>()  ;
		saveToken.add(-1);
		for(int i = 0; i<nameList.length; i++){
			int t=0;
			int k = 0, ki=0;
			for(int c = 0; c<saveToken.size(); c++){
				if(i == saveToken.get(c)){
					t=100;
								
				}
			} if(t!=100){
					
					for(int j=i+1; j<nameList.length; j++){
						k = 0;
						for(int ci = 0; ci<saveToken.size(); ci++){
							if(j == saveToken.get(ci)){
								k=100;
							
							}
						}if(k!=100) {
							
								String [] name1=nameList[i].split("_");
								String [] name2=nameList[j].split("_");
								countSameTokens=0;
								for(String token1 : name1){
									for(String token2 : name2){
										if(token1.equals(token2)){
											countSameTokens++;
										}
									}					
									}
								temp =nameList[j];
								if(countSameTokens > prevSameTokens){
									prevSameTokens = countSameTokens;
									nameToOptimize=temp;
									ki=j;
								}					
							}							
						}						
					}
			if(prevSameTokens > 0 && t!=100 && k!=100){
				optimize_names(nameList[i], nameToOptimize);
				prevSameTokens = 0;
				saveToken.add(i);
				saveToken.add(ki);
				
			
			}
		}
		System.out.println(saveToken);
	}
	
}
