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

public class DateParse
{
	public void Subject(String dayname , String monthname)  
	{
		int result = 0;
		//int month = -1;
		dayname = dayname.toLowerCase();
		monthname = monthname.toLowerCase();

		if ("mon".equals(dayname) || 
				"tue".equals(dayname) || 
				"wed".equals(dayname) || 
				"thur".equals(dayname) || 
				"fri".equals(dayname) || 
				"sat".equals(dayname) || 
				"sun".equals(dayname)) {
			result = 1; 
		}       
		if ("jan".equals(monthname)) {
			result += 1;
		}
		if ("feb".equals(monthname)) {
			result += 2;
		}
		if ("mar".equals(monthname)) {
			result += 3;
		}
		if ("apr".equals(monthname)) {
			result += 4;
		}
		if ("may".equals(monthname)) {
			result += 5;
		}
		if ("jun".equals(monthname)) {
			result += 6;
		}
		if ("jul".equals(monthname)) {
			result += 7;
		}
		if ("aug".equals(monthname)) {
			result += 8;
		}
		if ("sep".equals(monthname)) {
			result += 9;
		}
		if ("oct".equals(monthname)) {
			result += 10;
		}
		if ("nov".equals(monthname)) {
			result += 11;
		}
		if ("dec".equals(monthname)) {
			result += 12;
		}
	}

}