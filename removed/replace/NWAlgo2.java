/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.eclipse.replace;

import java.util.Collection;

public class NWAlgo2 {
	private String[] originalString;
	private String[] newString;
	private String[] originalType;
	private String[] newType;
	private int[][] f;
	static int gap= -1;  
	public static boolean finished;
	private String orgAlignment="";
	private String newAlignment="";
	
	public static void main(String[] args){
		String originalString="trate";
		String newString="translation";
		//new NWAlgo2(originalString, newString);
	}//main
	
	
	//construct the matrix 
	public NWAlgo2(String[] originalString, String[] newString, String[] originalType, String[] newType){
		//forward, compute f matrix
		finished= false;
		this.originalString=originalString;
		this.newString=newString;
		this.originalType= originalType;
		this.newType= newType; 
		f= new int[originalString.length+1][newString.length+1];
		int delete;
		int insert;
		int upLeft;
		
		for(int i=0; i<=originalString.length; i++)
			f[i][0]= gap*i;		
		for(int j=0; j<=newString.length; j++)
			f[0][j]= gap*j;
		
		
		for(int i=1; i<=originalString.length; i++){
			for(int j=1; j<=newString.length; j++){
				//System.out.print(originalString.charAt(i-1)+ ","+ newString.charAt(j-1));
					upLeft= f[i-1][j-1] +getSimilarity(i-1, j-1);
					delete= f[i-1][j] + gap;
					insert= f[i][j-1] + gap;
					f[i][j]= Math.max(upLeft, Math.max(delete, insert));
				//System.out.println("," + f[i][j]);
			}
		}
			
		//illustrate matrix 
		System.out.println("\n\n---------------");
		for(int i=0; i<=originalString.length; i++){
			for(int j=0; j<=newString.length; j++){
				String val= f[i][j]+"";
				int k=val.length();
				while(k<3){
					val=" "+val;
					k++;
				}
				System.out.print(val);
			}
			System.out.println();
		}
		
		
		//backtrack to get the alignment 
		
		int i= originalString.length; 
		int j= newString.length; 
		
		int score;
		int diagScore;
		int upScore;
		int leftScore;
		while(i>0 && j>0){
			score= f[i][j];
			diagScore= f[i-1][j-1];
			upScore= f[i][j-1];
			leftScore= f[i-1][j];
			
			if(score== diagScore +getSimilarity(i-1, j-1 )){
				orgAlignment= originalString[i-1] +" "+ orgAlignment; 
				newAlignment= newString[j-1] +" "+ newAlignment;
				i-=1;
				j-=1;
			}else if(score == (leftScore+ gap)){
				orgAlignment= originalString[i-1] +" "+ orgAlignment; 
				newAlignment= "-" +" "+ newAlignment;
				i-=1;
			}else if (score == (upScore+gap)){
				orgAlignment= "-"+" "+ orgAlignment; 
				newAlignment= newString[j-1] +" "+ newAlignment;
				j-=1;
			}else{
				System.out.println("*error"); System.err.println();
			}
		}
		
		while(i>0){	
			orgAlignment= originalString[i-1] +" "+ orgAlignment; 
			newAlignment= "-" +" "+ newAlignment;
			i-=1;
		}
			
		while(j>0){	
			orgAlignment= "-"+" "+ orgAlignment; 
			newAlignment= newString[j-1] +" "+ newAlignment;
			j-=1;
		}
			
		
		finished=true;
		System.out.println(orgAlignment+"\n"+newAlignment);
		System.out.println("nw distance: " +f[originalString.length][newString.length]);
	}
	
	public int getDistance(){
		return f[originalString.length][newString.length]; 
	}
	
	public String getOrgAlignment(){
		return orgAlignment; 
	}
	
	public String getNewAlignment(){
		return newAlignment;
	}
	public int getSimilarity(int n1, int n2){
		//System.out.println(new TypeMatcher().RewardChooser(originalType[n1], newType[n2]));
		int typeReward= new TypeMatcher().RewardChooser(originalType[n1], newType[n2]);
				
		//calculate difference 
		int stringRepresentation=0;
		stringRepresentation= calDifference(n1, n2);
		return typeReward + stringRepresentation;
	}
	
	
	public int calDifference(int n1, int n2){
		String oType = originalType[n1];
		String nType = newType[n2];
		int difference=0;
		if(!oType.equals(nType)){
			if((oType.equals("literal") && nType.equals("var")) ||
				(oType.equals("var") && nType.equals("literal")))
				difference= new LevenshteinAlgo().getLevenshteinDistance(originalString[n1], newString[n2]); 
			else 
				difference=1;
		}else{
			if (oType.equals("literal"))
				difference= new LevenshteinAlgo().getLevenshteinDistance(originalString[n1], newString[n2]); 
			if (oType.equals("var"))
				difference= new LevenshteinAlgo().getLevenshteinDistance(originalString[n1], newString[n2]); 
		}
		
		return 	difference;
	}
}
