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
//! name = costfuns       //NAME OF EXPT, NOT COMPUTATIONALLY SIGNIFICANT



package scs;

public class Costfuns
{
	public int  Subject(int i, String s)
	{
		//TEST COST FUNCTIONS
		String s1 = "ba";
		String s2 = "ab";
		if (i == 5) {     //i0
		}
		if (i  < -444) {   //i1
		}
		if (i  <= -333) {  //i2
		}
		if (i  > 666) {    //i3
		}
		if (i  >= 555) {   //i4
		}
		if (i  != -4) {    //i5
		}
		if (s.equals( s1 + s2)) {   //i6
		}
		//THOSE operations are not defined in Java...
		/*
  if (s  <= s1..Remove(0, 1)) { //i7
  }
  if (s  < s1.Remove(1, 1)) {  //i8
  }
  */
  if (s.compareTo(s2 + s2 + s1)>0) {     //i9
  }
  if (s.compareTo(s2 + s2 + s1)  >= 0) {    //i10
  }
  if (s  != s2 + s2) {         //i11
  }
		 
		return 0;
	}
}

