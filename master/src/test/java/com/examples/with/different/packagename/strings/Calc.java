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
package com.examples.with.different.packagename.strings;

/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
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
//! name = calc           //NAME OF EXPT, NOT COMPUTATIONALLY SIGNIFICANT


public class Calc
{
	public double  Subject(String op, double arg1 , double arg2 )  
	{
		op = op.toLowerCase();
		double result =  0.0;
		if ("pi".equals(op)) { //CONSTANT OPERATOR
			result = Math.PI;
		}
		else if ("e".equals(op)) {
			result = Math.E;
		}       //UNARY OPERATOR 
		else if ("sqrt".equals(op)) {
			result = Math.sqrt(arg1);
		}
		else if ("log".equals(op)) {
			result = Math.log(arg1);
		}
		else if ("sine".equals(op)) {
			result = Math.sin(arg1);
		}
		else if ("cosine".equals(op)) {
			result = Math.cos(arg1);
		}
		else if ("tangent".equals(op)) {
			result = Math.tan(arg1);
		}      //BINARY OPERATOR 
		else if ("plus".equals(op)) {
			result = arg1 + arg2;
		}
		else if ("subtract".equals(op)) {
			result = arg1 - arg2;
		}
		else if ("multiply".equals(op)) {
			result = arg1 * arg2;
		}
		else if ("divide".equals(op)) {
			result = arg1 / arg2;
		}
		return result;
	}
}
