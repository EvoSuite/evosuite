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

package scs;

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
