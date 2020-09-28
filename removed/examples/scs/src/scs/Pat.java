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
//! futname = Subject      //NAME OF FUNCTION UNDER TEST
//! mutation = false        //SPECIFY MUTATION COVERAGE
//! textout = true        //WRITE INSTRUMENTED SUBJECT TO FILE
//! maxchildren = 500000  //MAX LENGTH OF SEARCH
//! totalpopsize = 100    //TOTAL SIZE OF POPULATIONS 
//! mutationpercent = 50  //REL FREQUENCY OF GENETIC MUTATION TO CROSSOVER
//! samefitcountmax = 100 //NUMBER OF CONSECUTIVE TESTS IN A POP 
//THAT MUST HAVE THE SAME COST FOR POP TO BE STAGNANT
//! verbose = false        //PRINT MESSAGES SHOWING PROGRESS OF SEARCH
//! showevery = 3000      //NUMBER OF CANDIDATE INPUTS GENERATED BETWEEN EACH SHOW
//! numbins = 0           //GRANULARITY OF CANDIDATE INPUT HISTOGRAM, SET TO 0 TO NOT COLLECT STATS
//! trialfirst = 1        //EACH TRIAL USES A DIFFERENT RANDOM SEED
//! triallast = 1         //NUMBER OF TRIALS = triallast - trialfirst + 1

package scs;

public class Pat
{
	public static String  Reverse(String s)
	{
		//RTN REVERSE OF s
		int slen  = s.length();
		if (slen < 2) {
			return s;
		}
		String result = "";
		//var result : System.Text.StringBuilder = new System.Text.StringBuilder(slen);
		//var i : int = slen - 1;
		for (int i = slen - 1; i >= 0; i--) { 
			//result.Append(s[i]);
			result += s.charAt(i);
		}
		//Console.WriteLine("s {0} revs {1}", s, result.ToString());
		return result;
	}

	public int Subject(String txt , String pat )  {
		//SEARCH txt FOR FIRST OCCURRENCE OF pat OR REVERSE OF pat
		//IF pat (STRING OF LENGTH AT LEAST 3) OCCURS IN txt, RTN 1
		//IF REVERSE OF pat OCCURS IN txt, RTN 2
		//IF pat AND REVERSE OF pat OCCURS IN txt, RTN 3
		//IF PALINDROME CONSISTING OF pat FOLLOWED BY REVERSE pat OCCURS IN txt, RTN 4
		//IF PALINDROME CONSISTING OF REVERSE pat FOLLOWED pat OCCURS IN txt, RTN 5
		int result = 0;
		int  i  = 0;
		int  j  = 0;
		int txtlen  = txt.length();
		int patlen  = pat.length();
		String  possmatch = null;

		if (patlen > 2) {
			String patrev  = Reverse(pat);
			for (i = 0; i <= txtlen - patlen; i++) { 
				if (txt.charAt(i) == pat.charAt(0)) {
					possmatch = txt.substring(i, patlen);
					if (possmatch.equals(pat)) {
						//FOUND pat
						result = 1;
						//CHECK IF txt CONTAINS REVERSE pat
						for (j = i + patlen; j <= txtlen - patlen; j++) { 
							if (txt.charAt(j) == patrev.charAt(0)) {
								possmatch = txt.substring(j, patlen);
								if (possmatch.equals(patrev)) {
									if (j == i + patlen) {
										return i;//4;
									}
									else {
										return i;//3;
									}
								}
							}
						}
					}
				}
				else if (txt.charAt(i) == patrev.charAt(0)) {
					possmatch = txt.substring(i, patlen);
					if (possmatch.equals(patrev)) {
						//FOUND pat REVERSE
						result = 2;
						//CHECK IF txt CONTAINS pat
						for (j = i + patlen; j <= txtlen - patlen; j++) { 
							if (txt.charAt(j) == pat.charAt(0)) {
								possmatch = txt.substring(j, patlen);
								if (possmatch.equals(pat)) {
									if (j == i + patlen) {
										return i;//5;
									}
									else {
										return i;//3;
									}
								}
							}
						}
					}
				}
			}  //pat NOR REVERSE FOUND
		}
		return result;
	}

}
