/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.instrumentation;

import dk.brics.automaton.Automaton;
import org.evosuite.utils.RegexDistanceUtils;

/**
 * Should rather call RegexDistanceUtils directly.
 * Bytecode instrumentator would need to be updated
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
        try {
            return RegexDistanceUtils.getStandardDistance(arg, regex);
        } catch (IllegalArgumentException e) {
            // Make sure assertThrowBy has the right source
            return arg.matches(regex) ? 0 : 1;
        }
    }

}
