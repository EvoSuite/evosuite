package org.evosuite.idNaming;

import org.apache.commons.lang3.StringUtils;

public class CheckTestNameUniqueness {
	
	private MethodInvExtraction methodsInTest;
	
	public CheckTestNameUniqueness(){
		methodsInTest= new MethodInvExtraction();
	}
	public void check_name_uniqueness(String[] nameList, String[] testList){
		
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
}
 