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