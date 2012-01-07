package de.unisb.cs.st.evosuite.symbolic.search;

import gov.nasa.jpf.JPF;

import java.util.List;
import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.RealVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.StringVariable;

/**
 * @author krusev
 *
 */
public class Changer {

	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.search.Changer");
	
	private int lastFitness = Integer.MAX_VALUE;

	private String lastVal = null;
	
	private Change change;
	
	public enum Change {DEL, ADD, REPLACE}
	
	public Changer () {
		//Chose a random operator
//		Change allChngs[] = Change.values();
//		int rnd = (int) (Math.random() * (allChngs.length));
//		change = allChngs[rnd]; 
		
		
		if ( (Math.random() > 0.5 ) ) {
			change = Change.ADD;
		} else {
			change = Change.DEL;
		}
	}
	
	/*
	 * maybe think of a better design here.
	 * The Idea now is that this can be easily extended to support 
	 * changing just the added or replaced char and not the whole 
	 * operator.
	 * 
	 * This can also be done using objects. 
	 * But will this improve the readability?
	 */
	/**
	 * Changes a String value using DEL, ADD or REPLACE char by char. 
	 * 
	 * Please define a new instance of the Changer class for each variable! 
	 * 
	 *  If the fitness given is worst that the previous one the String will be 
	 *  reverted to the previous condition. 
	 *
	 * @param currVal the String that we want to change 
	 * @param currFitness the fitness for that string
	 * @return the changed String
	 */
	public String changeVar (String currVal, int currFitness, boolean reachable) {
		String result = currVal;
		//log.warning("reachable"+reachable);
		//If the fitness got worse or the target is no longer reachable 
		//		revert to last value and don't use the same operator
		//think about using the same operator but on different position
		if (lastFitness > currFitness || !reachable) {
			if ( lastVal != null){
				result = lastVal;
			}
			Change allChngs[] = Change.values();
			int rnd = (int) (Math.random() * (allChngs.length));
			if (allChngs[rnd] == change) {
				rnd = (rnd+1)%(allChngs.length);
			}
			change = allChngs[rnd];
		} else {
			//We are either in the first execution or in a normal run
			lastVal = currVal;
			lastFitness = currFitness;
		}
		
		if (currFitness == -(Integer.MAX_VALUE) ) {
			if ( (Math.random() > 0.5 ) ) {
				change = Change.ADD;
			} else {
				change = Change.DEL;
			}
			
		} else {
			change = Change.REPLACE;
		}
		
		//If the string that we handle is empty we don't have any other choice
		if (result.isEmpty()) {
			change = Change.ADD;
		}
		
		//======================================
		

//		boolean length_diff = false;
//		if (currFitness < 0) {
//			currFitness = -currFitness;
//			length_diff = true;
//		}
//		
//		
//		if (length_diff) {
//			if (lastFitness < currFitness) {
//				if ( lastVal != null ){
//					result = lastVal;
//				}
//				if (change == Change.ADD) {
//					change = Change.DEL;
//				} else {
//					change = Change.ADD;
//				}
//			} else {
//				lastFitness = currFitness;
//			}
//			
//			
//			
//		} else {
//			change = Change.REPLACE;
//			lastFitness = currFitness;
//
//		}
		
		
		int rndIndx;
		switch (change) {
		case ADD:
			rndIndx = (int) (Math.random() * (result.length()+1));
			return result.substring(0, rndIndx) + getRandomChar() + result.substring(rndIndx, result.length());
		case DEL:
			rndIndx = (int) (Math.random() * (result.length()-1));
			return result.substring(0, rndIndx) + result.substring(rndIndx + 1, result.length());
		case REPLACE:
			rndIndx = (int) (Math.random() * (result.length()-1));
			return result.substring(0, rndIndx) + getRandomChar() + result.substring(rndIndx + 1, result.length());
		default:
			log.warning("de.unisb.cs.st.evosuite.symbolic.search.Changer: Forgot to implemt Operator!");
			return currVal;
		}		
	}

	/**
	 * 
	 * @return returns a random char 
	 */
	private char getRandomChar() {
		
//		int rnd = (int) (Math.random() * 52);
//	    char base = (rnd < 26) ? 'A' : 'a';i
//	    return (char) (base + rnd % 26);
		int rnd = (int) (Math.random() * 26);
	    return (char) ('a' + rnd);
	}

	public String strLocalSearch(StringVariable strVar, Constraint<?> target,
			List<Constraint<?>> cnstr) {
		// TODO Auto-generated method stub
		
		return null;
	} 
	
	public long intLocalSearch(IntegerVariable strVar, Constraint<?> target,
			List<Constraint<?>> cnstr) {
		// TODO Auto-generated method stub
	
		return 0;
	}

	public double realLocalSearch(RealVariable realVar, Constraint<?> target,
			List<Constraint<?>> cnstr) {
		// TODO Auto-generated method stub
		
		return 0;
	} 
	
	
}
