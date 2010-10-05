//! futname = Subject      //NAME OF FUNCTION UNDER TEST
//! mutation = true        //SPECIFY MUTATION COVERAGE
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
//! name = costfuns       //NAME OF EXPT, NOT COMPUTATIONALLY SIGNIFICANT

package scs;

public class NotyPevar
{
	//SHOW USE OF UNTYPED VARIABLES
	public int  Subject(int i,  String s )
	{
		int x;
		int y;
		int result  = 0;
		x = i;
		y = x;
		if (x + y == 56) {     //i0
			result = x;
		}
		String xs = "hello";
		if ((xs + y).equals("hello7")) {   //i1
			result = 1;
		}
		if (xs.compareTo(s)   < 0) {  //i2
			result = 2;
		}
		x = 5;
		if (y > x) {    //i3
			result = 3;
		}
		return result;
	}
}

