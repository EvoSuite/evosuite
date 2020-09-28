/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import java.util.HashMap;

public class NWAlgo {
	private final HashMap originalString;
	private final HashMap newString;
	private final HashMap originalType;
	private final HashMap newType;
	private final double[][] f;
	static int gap = -1;
	public static boolean finished;
	private String orgAlignment = "";
	private String newAlignment = "";
	int s = Integer.MIN_VALUE;//.NEGATIVE_INFINITY;

	public static void main(String[] args) {
		String originalString = "trate";
		String newString = "translation";
		//new NWAlgo(originalString, newString);
	}//main

	//construct the matrix 
	public NWAlgo(HashMap originalString, HashMap newString, HashMap originalType,
	        HashMap newType) {
		//forward, compute f matrix
		finished = false;
		this.originalString = originalString;
		this.newString = newString;
		this.originalType = originalType;
		this.newType = newType;
		f = new double[originalString.size() + 1][newString.size() + 1];
		double[][] GA = new double[originalString.size() + 1][newString.size() + 1];
		double[][] GB = new double[originalString.size() + 1][newString.size() + 1];
		double delete;
		double insert;
		double upLeft;
		int g = -5; //gap opening penalty
		int h = -3; //gap extension penalty 

		for (int i = 1; i <= originalString.size(); i++)
			f[i][0] = g + h * (i - 1); //gap*i;		
		for (int j = 1; j <= newString.size(); j++)
			f[0][j] = g + h * (j - 1); //gap*j;
		f[0][0] = 0;

		GB[0][0] = Integer.MIN_VALUE;
		for (int i = 1; i <= originalString.size(); i++) {
			GB[i][0] = g + h * (i - 1);
			GA[i][0] = Integer.MIN_VALUE;
		}
		GA[0][0] = Integer.MIN_VALUE;
		for (int j = 1; j <= newString.size(); j++) {
			GA[0][j] = g + h * (j - 1);
			GB[0][j] = Integer.MIN_VALUE;
		}

		for (int i = 1; i <= originalString.size(); i++) {
			for (int j = 1; j <= newString.size(); j++) {
				//System.out.print(originalString.charAt(i-1)+ ","+ newString.charAt(j-1));
				/*
				//ga
				GA[i][j]= Math.max(GA[i-1][j]+h, f[i-1][j]+g+h);
				GB[i][j]= Math.max(GB[i][j-1]+h, f[i][j-1]+g+h);
				//GC[i][j]= 
				
					//f
					upLeft= f[i-1][j-1] ;	//+getSimilarity(i-1, j-1);
					delete= GA[i-1][j-1]; //f[i-1][j] + gap;
					insert= GB[i-1][j-1]; //f[i][j-1] + gap;
					f[i][j]= Math.max(upLeft, Math.max(delete, insert))	+getSimilarity(i-1, j-1);
				*/

				GA[i][j] = Math.max(GA[i - 1][j] + h, f[i - 1][j] + g + h);
				GB[i][j] = Math.max(GB[i][j - 1] + h, f[i][j - 1] + g + h);

				upLeft = f[i - 1][j - 1] + getSimilarity(i - 1, j - 1);
				delete = GA[i][j]; //f[i-1][j] + gap;
				insert = GB[i][j]; //f[i][j-1] + gap;
				f[i][j] = Math.max(upLeft, Math.max(delete, insert));

			}
		}

		//illustrate matrix 
		/*
		for(int i=0; i<=originalString.size(); i++){
			for(int j=0; j<=newString.size(); j++){
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
		*/

		//backtrack to get the alignment 

		int i = originalString.size();
		int j = newString.size();

		double score;
		double diagScore;
		double upScore;
		double leftScore;
		while (i > 0 && j > 0) {
			score = f[i][j];
			diagScore = f[i - 1][j - 1];
			upScore = f[i][j - 1];
			leftScore = f[i - 1][j];

			if (score == diagScore + getSimilarity(i - 1, j - 1)) {
				orgAlignment = originalString.get(i - 1) + " " + orgAlignment;
				newAlignment = newString.get(j - 1) + " " + newAlignment;
				i -= 1;
				j -= 1;
			} else if (score == GA[i][j]) {//GA[i-1][j-1]+getSimilarity(i-1, j-1)){   //(leftScore+ gap)){
				orgAlignment = originalString.get(i - 1) + " " + orgAlignment;
				newAlignment = "-" + " " + newAlignment;
				i -= 1;
			} else if (score == GB[i][j]) { //GB[i-1][j-1]+getSimilarity(i-1, j-1)){	//(upScore+gap)){
				orgAlignment = "-" + " " + orgAlignment;
				newAlignment = newString.get(j - 1) + " " + newAlignment;
				j -= 1;
			} else {
				System.out.println("*error");
				System.err.println();
			}
		}

		while (i > 0) {
			orgAlignment = originalString.get(i - 1) + " " + orgAlignment;
			newAlignment = "-" + " " + newAlignment;
			i -= 1;
		}

		while (j > 0) {
			orgAlignment = "-" + " " + orgAlignment;
			newAlignment = newString.get(j - 1) + " " + newAlignment;
			j -= 1;
		}

		finished = true;
		// System.out.println(orgAlignment+"\n"+newAlignment);
		// System.out.println("nw distance: " +f[originalString.size()][newString.size()]);

	}

	public double getDistance() {
		return f[originalString.size()][newString.size()];
	}

	public String getOrgAlignment() {
		return orgAlignment;
	}

	public String getNewAlignment() {
		return newAlignment;
	}

	public double getSimilarity(int n1, int n2) {
		//System.out.println(new TypeMatcher().RewardChooser(originalType[n1], newType[n2]));
		//int typeReward= new TypeMatcher().RewardChooser((String) originalType.get(n1), (String) newType.get(n2));

		//calculate difference 
		double stringRepresentation = 0;
		stringRepresentation = calDifference(n1, n2);
		return stringRepresentation;
	}

	private static double normalize(double value) {
		return value / (value + 1.0);
	}

	public double calDifference(int n1, int n2) {
		String oType = (String) originalType.get(n1);
		String nType = (String) newType.get(n2);
		double difference = 0;
		if (!oType.equals(nType)) {
			//if((oType.equals("literal") && nType.equals("var")) ||
			//(oType.equals("var") && nType.equals("literal")))
			//difference= new LevenshteinAlgo().getLevenshteinDistance((String) originalString.get(n1), (String) newString.get(n2))*(-1); 
			//difference=-3;
			//else 
			difference = -2;//-3

		} else {
			if (oType.equals("literal"))
				difference = normalize(new LevenshteinAlgo().getLevenshteinDistance((String) originalString.get(n1),
				                                                                    (String) newString.get(n2)))
				        * (-1);
			if (oType.equals("var"))
				//if((new LevenshteinAlgo().getLevenshteinDistance((String) originalString.get(n1), (String) newString.get(n2)) )==0)
				//difference=3;
				//else
				difference = normalize(new LevenshteinAlgo().getLevenshteinDistance((String) originalString.get(n1),
				                                                                    (String) newString.get(n2)))
				        * (-1);
			if (oType.equals("class")) {
				//if((new LevenshteinAlgo().getLevenshteinDistance((String) originalString.get(n1), (String) newString.get(n2)) )==0)
				//difference=2;//2
				//else
				difference = normalize(new LevenshteinAlgo().getLevenshteinDistance((String) originalString.get(n1),
				                                                                    (String) newString.get(n2)))
				        * (-1);
			}
			if (oType.equals("spChar")) {
				//if((new LevenshteinAlgo().getLevenshteinDistance((String) originalString.get(n1), (String) newString.get(n2)) )==0)
				//difference=2;//2
				/*else
				difference=-1;//1
				*/
				difference = normalize(new LevenshteinAlgo().getLevenshteinDistance((String) originalString.get(n1),
				                                                                    (String) newString.get(n2)))
				        * (-2);

			}

			if (oType.equals("keyword")) {
				difference = normalize(new LevenshteinAlgo().getLevenshteinDistance((String) originalString.get(n1),
				                                                                    (String) newString.get(n2)))
				        * (-1);

			}

		}

		return difference;
	}
}
