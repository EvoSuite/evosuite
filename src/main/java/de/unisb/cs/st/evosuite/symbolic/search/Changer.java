package de.unisb.cs.st.evosuite.symbolic.search;

import gov.nasa.jpf.JPF;

import java.util.logging.Logger;

/**
 * @author krusev
 *
 */
public class Changer {

	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.search.Changer");
	
	private int lastFitness = Integer.MIN_VALUE;

	private String lastVal = null;
	
	private Change change;
	
	public enum Change {DEL, ADD, REPLACE}
	
	public Changer () {
		//Chose a random operator
		Change allChngs[] = Change.values();
		int rnd = (int) (Math.random() * (allChngs.length));
		change = allChngs[rnd]; 
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
		
		//If the fitness got worse or the target is no longer reachable 
		//		revert to last value and don't use the same operator
		//think about using the same operator but on different position
		if (lastFitness > currFitness || !reachable) {
			result = lastVal;
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
		
		//If the string that we handle is empty we don't have any other choise
		if (result.isEmpty()) {
			change = Change.ADD;
		}
		
		
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
		
		int rnd = (int) (Math.random() * 52);
	    char base = (rnd < 26) ? 'A' : 'a';
	    return (char) (base + rnd % 26);
	} 
	
	
}
