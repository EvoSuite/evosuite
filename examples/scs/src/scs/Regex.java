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

import java.util.regex.*;

public class Regex
{
	public static void main(String[] args)
	{
		Subject(args[0]);
	}
	
	public static void Subject(String txt) 
	{
		//MATCH txt AGAINST VARIOUS REGULAR EXPRESSIONS
		//ALL OF txt MUST MATCH
		String digit  = "((0)|(1)|(2)|(3)|(4)|(5)|(6)|(7)|(8)|(9))";
		String fp  = digit + digit + "*\\." + digit + digit + "*";
		String fpe  = fp + "e((\\+)|(-))" + digit + digit;

		String alpha  = "((a)|(b)|(c)|(d)|(e)|(f)|(g)|(h)|(i)|(j)|(k)|(l)|(m)|(n)|(o)|(p)|(q)|(r)|(s)|(t)|(u)|(v)|(w)|(x)|(y)|(z)|(_)|(-))";
		String iden  = alpha + "(" + alpha + "|" + digit + ")*";
		String url  = "((http)|(ftp)|(afs)|(gopher))//:" + iden + "/"  + iden;
		String day  = "((mon)|(tue)|(wed)|(thur)|(fri)|(sat)|(sun))";
		String month  = "((jan)|(feb)|(mar)|(apr)|(may)|(jun)|(jul)|(aug)|(sep)|(oct)|(nov)|(dec))";
		String date  = day + digit + digit + month; 
		//var re : RegExp;

		//Pattern p = Pattern.compile(url);

		//Console.WriteLine("{0}  {1}", txt, iden); 
		//re = new RegExp(url);
		//re.regex.matchinexact.ParseFromRegExp();
		//print(StringUtils.PrettyPrint(re.regex.matchinexact.fsaexact));  
		//if (0 == re.regex.matchinexact.Match(txt)) {
		if (Pattern.matches(url, txt)) {
			; 
		}
		//print(StringUtils.PrettyPrint(re.regex.matchinexact));  

		//Console.WriteLine("{0}  {1}", txt, iden); 
		//re = new RegExp(date);
		//re.regex.matchinexact.ParseFromRegExp();
		//print(StringUtils.PrettyPrint(re.regex.matchinexact.fsaexact));  
		//  if (0 == re.regex.matchinexact.Match(txt)) {
		if (Pattern.matches(date, txt)) {
			; 
		}
		//print(StringUtils.PrettyPrint(re.regex.matchinexact));  

		//Console.WriteLine("{0}  {1}", txt, fpe); 
		//re = new RegExp(fpe);
		//re.regex.matchinexact.ParseFromRegExp();
		//print(StringUtils.PrettyPrint(re.regex.matchinexact.fsaexact));  
		// if (0 == re.regex.matchinexact.Match(txt)) {
		if (Pattern.matches(fpe, txt)) {
			; 
		}
		//print(StringUtils.PrettyPrint(re.regex.matchinexact));  
	}   
}