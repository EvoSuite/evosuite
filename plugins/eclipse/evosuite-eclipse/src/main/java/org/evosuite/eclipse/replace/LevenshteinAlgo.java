package org.evosuite.eclipse.replace;

public class LevenshteinAlgo{
	public int getLevenshteinDistance(String originalString, String newString){
	//Levenshtein distance- simple (no weights, no recorded edit transcript)
	
	int[][] d= new int[originalString.length()+1][newString.length()+1]; 
	
	for(int i=1; i<originalString.length()+1; i++)
		d[i][0]=i;
	
	for(int j=1; j<newString.length()+1; j++)
		d[0][j]=j;
	
	for(int j=1; j<newString.length()+1; j++){
		for(int i=1; i<originalString.length()+1; i++){
			
			if(originalString.charAt(i-1)==newString.charAt(j-1)){
				d[i][j]= d[i-1][j-1];			//if match, cost=0
			}else{
				d[i][j]= Math.min(d[i][j-1]+1, 	//insertion,  cost=1
						Math.min(d[i-1][j-1]+1, 	//substitution
								d[i-1][j]+1));    //deletion
			}
		}
		
	}//for
	return d[originalString.length()][newString.length()];

	}//getDistance
}
