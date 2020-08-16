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

public class Text2Txt
{

	public String  Subject(String word1 , String word2 , String word3 )  
	{
		//CONVERT ENGLISH TEXT txt INTO MOBILE TELEPHONE TXT
		//BY SUBSTITUTING ABBREVIATIONS FOR COMMON WORDS
		word1 = word1.toLowerCase();
		word2 = word2.toLowerCase();
		word3 = word3.toLowerCase();
		String result  = "";
		if (word1.equals("two")) {
			result = "2";
		}
		if (word1.equals("for") || word1.equals("four")) {
			result = "4";
		}
		if (word1.equals("you")) {
			result = "u";
		}
		if (word1.equals("and")) {
			result = "n";
		}
		if (word1.equals("are")) {
			result = "r";
		}
		else if (word1.equals("see") && word2.equals("you")) {
			result = "cu";
		}
		else if (word1.equals("by") && word2.equals("the") && word3.equals("way")) {
			result = "btw";
		}
		return result;
	}

}