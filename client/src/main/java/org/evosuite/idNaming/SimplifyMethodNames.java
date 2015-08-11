package org.evosuite.idNaming;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimplifyMethodNames {

	public static String[] optimizeNames(List<String> nameList){
		String[] methodNames = nameList.toArray(new String[nameList.size()]) ;
		for(int i=0; i<nameList.size(); i++){
			String [] name1=methodNames[i].split("_");
			List<String> list1 = new ArrayList(Arrays.asList(name1));
			int prevInter=0;
			List<String> optName=null;
			List<String> intersection=null;
			List<String> bestInter = null;
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
				}				
			}
			if(prevInter!=0){
				String[] optNames=simpleNames(list1, optName, bestInter);
				methodNames[i] = optNames[0];
				methodNames[position] = optNames[1];
			}
		}
		return methodNames;
	}
	public static String[] simpleNames(List<String> list1, List<String> list2, List<String> intersection){	
		String  name1=list1.get(1);
		String  name2=list2.get(1);
		for(String str:intersection){
			if(list1.contains(str)){
				list1.remove(str);
			}
			if(list2.contains(str)){
				list2.remove(str);
			}			
		}
		String nameFirst = "test";
		String nameSecond = "test";
		if(list1.size()>0){
			for(String name: list1){
				nameFirst+="_"+name;
			}
		}else{
			nameFirst+="_"+name1;
		}
		if(list2.size()>0){
			for(String name: list2){
				nameSecond+="_"+name;
			}
		}else{
			nameSecond+="_"+name2;
		}
		String [] result= {nameFirst, nameSecond};
		return result;
	}
	

	
	public static  String[] minimizeNames (String[] names){		
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
	
	public static  String[] countSameNames(String[] nameList){
		int temp=0;
		for (int i=0; i<nameList.length; i++){
			temp=1;
			for (int j=i+1; j<nameList.length; j++){
				if(nameList[i].equals(nameList[j])){
					String[] tokens= nameList[j].split("_");
					nameList[j]=nameList[j].replace(tokens[0], tokens[0]+temp);
					temp++;
				}
			}
			if(temp>1){
				String[] tokens= nameList[i].split("_");
				nameList[i]=nameList[i].replace(tokens[0], tokens[0]+"0");
			}
		}
		return nameList;
	}	
}
