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

public class Cookie
{
	public int Subject(String name,  String val, String site) 
	{
		name = name.toLowerCase();
		val = val.toLowerCase();
		site = site.toLowerCase();
		int result = 0;
		if ("userid".equals(name)) {
			if (val.length() > 6) {
				if ("user".equals(val.substring(0, 4))) {
					result = 1;
				}
			}
		}
		else if ("session".equals(name)) {
			if ("am".equals(val) && "abc.com".equals(site)) {
				result = 1;
			}
			else {
				result = 2;
			}
		}
		return result;
	}
}