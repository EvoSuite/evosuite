package org.evosuite.symbolic.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.StringConstraint;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.expr.token.HasMoreTokensExpr;

public final class StringAVM {

	public StringAVM(StringVariable strVar, Collection<Constraint<?>> cnstr) {
		super();
		this.strVar = strVar;
		this.cnstr = cnstr;
	}

	static Logger log = LoggerFactory.getLogger(StringAVM.class);

	private double checkpointDistance = Double.MAX_VALUE;

	private String checkpointStringValue;

	private final StringVariable strVar;

	private final Collection<Constraint<?>> cnstr;

	/**
	 * <p>
	 * strLocalSearch
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean applyAVM() {

		// try to remove each
		log.debug("Trying to remove characters");
		boolean improvement = false;

		checkpointVar(DistanceEstimator.getDistance(cnstr));

		// First chop characters from the back until distance doesn't improve
		String oldString = strVar.getConcreteValue();
		boolean improved = true;
		while (improved && oldString.length() > 0) {
			String newStr = oldString.substring(0, oldString.length() - 1);
			strVar.setConcreteValue(newStr);
			log.debug("Current attempt: " + newStr);
			improved = false;

			double newDist = DistanceEstimator.getDistance(cnstr);

			if (distImpr(newDist)) {
				log.debug("Distance improved, keeping change");
				checkpointVar(newDist);
				improvement = true;
				improved = true;
				oldString = newStr;
				if (newDist == 0) {
					return true;
				}
			} else {
				log.debug("Distance did not improve, reverting change");
				restoreVar();
			}
		}

		// next try to replace each character using AVM
		log.debug("Trying to replace characters");
		// Backup is done internally
		if (doStringAVM(oldString)) {
			improvement = true;
			oldString = strVar.getConcreteValue();
		}

		if (checkpointDistance == 0.0)
			return true;

		// try to add at the end
		log.debug("Trying to add characters");

		checkpointVar(DistanceEstimator.getDistance(cnstr));

		// Finally add new characters at the end of the string
		improved = true;
		while (improved) {
			improved = false;
			char charToInsert = getRandomChar();
			String newStr = oldString + charToInsert;
			strVar.setConcreteValue(newStr);
			double newDist = DistanceEstimator.getDistance(cnstr);
			log.debug("Adding: " + newStr + ": " + newDist);
			if (distImpr(newDist)) {
				improvement = true;
				improved = true;
				checkpointVar(newDist);
				if (checkpointDistance == 0.0) {
					log.debug("Search seems successful, stopping at "
							+ checkpointDistance + "/" + newDist);
					return true;
				}

				doCharacterAVM(newStr.length() - 1);
				oldString = strVar.getConcreteValue();
			} else {
				restoreVar();
			}
		}

		// try to insert delimiters (if any)
		Set<StringValue> delimiters = getTokenDelimiters(cnstr);
		for (StringValue delimiter : delimiters) {
			improved = true;
			String delimiterStr = delimiter.execute();
			while (improved) {
				improved = false;
				char charToInsert = getRandomChar();
				String newStr = oldString + delimiterStr + charToInsert;
				strVar.setConcreteValue(newStr);
				double newDist = DistanceEstimator.getDistance(cnstr);
				log.debug("Adding: " + newStr + ": " + newDist);
				if (distImpr(newDist)) {
					improvement = true;
					improved = true;
					checkpointVar(newDist);
					if (checkpointDistance == 0.0) {
						log.debug("Search seems successful, stopping at "
								+ checkpointDistance + "/" + newDist);
						return true;
					}

					doCharacterAVM(newStr.length() - 1);
					oldString = strVar.getConcreteValue();
				} else {
					restoreVar();
				}
			}

		}

		return improvement;
	}

	private void checkpointVar(double newDist) {
		checkpointStringValue = strVar.getConcreteValue();
		checkpointDistance = newDist;
	}

	private boolean distImpr(double newDistance) {
		return newDistance < checkpointDistance;
	}

	private void restoreVar() {
		strVar.setConcreteValue(checkpointStringValue);
	}

	/**
	 * Apply AVM to an individual character within a string
	 * 
	 * @param position
	 * @param varsToChange
	 * 
	 * @return
	 */
	private boolean doCharacterAVM(int position) {
		checkpointVar(DistanceEstimator.getDistance(cnstr));
		boolean done = false;
		boolean hasImproved = false;

		while (!done) {
			done = true;
			String origString = strVar.getConcreteValue();
			char oldChar = origString.charAt(position);

			char[] characters = origString.toCharArray();

			char replacement = oldChar;
			replacement++;
			characters[position] = replacement;
			String newString = new String(characters);
			strVar.setConcreteValue(newString);
			double newDist = DistanceEstimator.getDistance(cnstr);
			log.debug("Probing increment " + position + ": " + newString + ": "
					+ newDist + " replacement = " + (int) replacement);
			if (distImpr(newDist)) {
				checkpointVar(newDist);

				if (newDist == 0.0)
					return true;
				done = false;
				hasImproved = true;
				iterateCharacterAVM(position, 2);
			} else {
				replacement -= 2;
				characters[position] = replacement;
				newString = new String(characters);
				strVar.setConcreteValue(newString);
				newDist = DistanceEstimator.getDistance(cnstr);
				log.debug("Probing decrement " + position + ": " + newString
						+ ": " + newDist + " replacement = "
						+ (int) replacement);
				if (distImpr(newDist)) {
					checkpointVar(newDist);

					if (newDist == 0.0)
						return true;

					done = false;
					hasImproved = true;
					iterateCharacterAVM(position, -2);
				} else {
					restoreVar();
					if (done)
						log.debug("Search finished " + position + ": "
								+ newString + ": " + newDist);
					else
						log.debug("Going for another iteration at position "
								+ position);

				}
			}
		}
		return hasImproved;
	}

	/**
	 * Apply AVM to all characters in a string
	 * 
	 * @param oldString
	 * @param varsToChange
	 * 
	 * @return
	 */
	private boolean doStringAVM(String oldString) {

		boolean improvement = false;

		for (int i = 0; i < oldString.length(); i++) {
			log.info("Current character: " + i);
			if (doCharacterAVM(i))
				improvement = true;
		}
		return improvement;
	}

	private boolean iterateCharacterAVM(int position, int delta) {

		boolean improvement = false;
		String oldString = strVar.getConcreteValue();

		log.debug("Trying increment " + delta + " of " + oldString);
		char oldChar = oldString.charAt(position);
		log.info(" -> Character " + position + ": " + oldChar);
		char[] characters = oldString.toCharArray();
		char replacement = oldChar;

		replacement += delta;
		characters[position] = replacement;
		String newString = new String(characters);
		strVar.setConcreteValue(newString);
		double newDist = DistanceEstimator.getDistance(cnstr);

		while (distImpr(newDist)) {
			checkpointVar(newDist);
			if (newDist == 0.0)
				return true;

			oldString = newString;
			improvement = true;
			delta = 2 * delta;
			replacement += delta;
			log.info("Current delta: " + delta + " -> " + replacement);
			characters[position] = replacement;
			newString = new String(characters);
			log.info(" " + position + " " + oldString + "/"
					+ oldString.length() + " -> " + newString + "/"
					+ newString.length());
			strVar.setConcreteValue(newString);
			newDist = DistanceEstimator.getDistance(cnstr);
		}
		log.debug("No improvement on " + oldString);
		restoreVar();
		// strVar.setMinValue(oldString);
		log.debug("Final value of this iteration: " + oldString);

		return improvement;
	}

	private static Set<StringValue> getTokenDelimiters(
			Collection<Constraint<?>> constraints) {

		Set<StringValue> delimiters = new HashSet<StringValue>();
		for (Constraint<?> constraint : constraints) {
			if (constraint instanceof StringConstraint) {
				StringConstraint stringConstraint = (StringConstraint) constraint;

				if (stringConstraint.getLeftOperand() instanceof HasMoreTokensExpr) {
					HasMoreTokensExpr hasMoreTokensExpr = (HasMoreTokensExpr) stringConstraint
							.getLeftOperand();
					StringValue delimiter = hasMoreTokensExpr
							.getTokenizerExpr().getDelimiter();
					delimiters.add(delimiter);
				}

			}
		}
		return delimiters;
	}

	private static char getRandomChar() {
		Random r = new Random();
		char randomChar = (char) r.nextInt();
		return randomChar;
	}
}
