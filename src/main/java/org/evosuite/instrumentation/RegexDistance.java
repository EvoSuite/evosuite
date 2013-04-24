package org.evosuite.instrumentation;

import org.evosuite.utils.RegexDistanceUtils;

import dk.brics.automaton.Automaton;

/**
 * Should rather call RegexDistanceUtils directly.
 * Bytecode instrumentator would need to be updated
 *	
 */
@Deprecated
public class RegexDistance {

	public static Automaton getRegexAutomaton(String regex) {
		return RegexDistanceUtils.getRegexAutomaton(regex);
	}

	public static String getRegexInstance(String regex) {
		return RegexDistanceUtils.getRegexInstance(regex);
	}

	public static String getNonMatchingRegexInstance(String regex) {
		return RegexDistanceUtils.getNonMatchingRegexInstance(regex);
	}

	public static int getDistance(String arg, String regex) {
		return RegexDistanceUtils.getStandardDistance(arg, regex);
	}

}
