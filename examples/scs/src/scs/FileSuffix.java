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

public class FileSuffix
{
	public int  Subject(String directory , String file ) 
	{
		//EG pathname = "...WORD/FILE.DOC";
		// files : Object[];
		String[] fileparts = null;
		//var lastfile : int = 0;
		int lastpart  = 0;
		String suffix = null;  
		fileparts = file.split(".");
		lastpart = fileparts.length - 1;
		if (lastpart > 0) {
			suffix = fileparts[lastpart];
			//Console.WriteLine("{0}, {1}", directory, suffix);
			if ("text".equals(directory)) {
				if ("txt".equals(suffix)) {
					//print("text");
				}
			}
			if ("acrobat".equals(directory)) {
				if ("pdf".equals(suffix)) {					 
					//print("acrobat");
				}
			}
			if ("word".equals(directory)) {
				if ("doc".equals(suffix)) {
					//print("word");
				}
			}
			if ("bin".equals(directory)) {
				if ("exe".equals(suffix)) {
					//print("bin");
				}
			}
			if ("lib".equals(directory)) {
				if ("dll".equals(suffix)) {
					//print("lib");
				}
			}
		}
		return 1;
	}
}