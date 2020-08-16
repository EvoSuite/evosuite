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

public class Title
{
  
  public int  Subject(String sex, String title) 
  {
  //CHECK PERSONAL TITLE CONSISTENT WITH SEX
    sex = sex.toLowerCase();
	title = title.toLowerCase();
    int result  = -1;
		if ("male".equals(sex)) {
			if ("mr".equals(title) || 
					"dr".equals(title) ||
					"sir".equals(title) ||
					"rev".equals(title) ||
					"rthon".equals(title) ||
					"prof".equals(title)) {
        result = 1;
      }
	  }
		else if ("female".equals(sex)) {
			if ("mrs".equals(title) || 
					"miss".equals(title) ||
					"ms".equals(title) ||
					"dr".equals(title) ||
					"lady".equals(title) ||
					"rev".equals(title) ||
					"rthon".equals(title) ||
					"prof".equals(title)){
        result = 0;
      }
	  }
		else if ("none".equals(sex)) {
			if ("dr".equals(title) ||
					"rev".equals(title) ||
					"rthon".equals(title) ||
					"prof".equals(title)){
        result = 2;
      }
	  }
    return result;
  }
}