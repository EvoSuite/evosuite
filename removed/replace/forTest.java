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
import java.util.HashMap;
import java.util.HashSet;

public class forTest {

	public static int basicAlgo(String originalString, String newString){
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
		
		System.out.println();
		for(int i=0; i<=originalString.length(); i++){
			for(int j=0; j<=newString.length(); j++){
				String val= d[i][j]+"";
				int k=val.length();
				while(k<3){
					val=" "+val;
					k++;
				}
				System.out.print(val);
			}
			System.out.println();
		}
		
		return d[originalString.length()][newString.length()];

		
	}//getDistance
	
	public static void main(String[] args){
		//int basicDistance= getBasicDistance("atm", "cpu");System.out.println("basic :" +basicDistance );
		/*
		String[] string1 = {"int", "x","=","3",";", "\n","int", "y","=","5",";"};
		String[] string2 = {"int", "x","=","5",";","\n", "String"};
		String[] type1={"keyword", "var", "spChar", "num", "spChar", "spCharNewLine", "keyword", "var", "spChar", "num", "spChar",};
		String[] type2={"keyword", "var", "spChar", "num", "spChar", "spCharNewLine", "keyword"};
		
		String[] string1 = {"int", "x","=","3","+", "5",";"};
		String[] string2 = {"int", "y","=","5",";"};
		String[] type1={"keyword", "var", "spChar", "num", "spChar", "num", "spChar",};
		String[] type2={"keyword", "var", "spChar", "num", "spChar"};
		
		String[] string1 = {"String", "x","=","\"test\"","+", "x",";"};
		String[] string2 = {"String", "y","=","x",";"};
		String[] type1={"keyword", "var", "spChar", "literal", "spChar", "var", "spChar"};
		String[] type2={"keyword", "var", "spChar", "var", "spChar"};
		*/
		//NWAlgoType nwType= new NWAlgoType(type1, type2);
		//int dis= nwType.getDistance(); 
		//NWAlgo2 nwType2= new NWAlgo2(string1, string2, type1, type2);
		System.out.println(new LevenshteinAlgo().getLevenshteinDistance("ejectCard","withdraw")*(-1));
		HashMap case1;
		HashMap case2;
		Collection<String> varNames= new HashSet<String>();
		Collection<String> classNames= new HashSet<String>();
		
		String string1 = " Bank bank0 = new Bank();"+
					"ATM aTM0 = new ATM(bank0);"+
					"aTM0.ejectCard();";
		String string2 = " Bank bank0 = new Bank();"+
						"ATM aTM0 = new ATM(bank0);"+
						"aTM0.withdraw();";
		varNames.add("bank0");
		varNames.add("aTM0");
		classNames.add("Bank");
		classNames.add("ATM");
		TokenSlicer ts1= new TokenSlicer(string1, varNames,classNames );
		TokenSlicer ts2= new TokenSlicer(string2, varNames,classNames );
		System.out.println(new NWAlgo(ts1.getTokenRecord(), ts2.getTokenRecord(),ts1.getTypeRecord(),ts2.getTypeRecord()));
	
		//NWAlgo nwType1= new NWAlgo("ab", "abc");
	}
	
	private static int getBasicDistance(String originalToken, String newToken){
		
		return basicAlgo(originalToken, newToken);
	}
}
