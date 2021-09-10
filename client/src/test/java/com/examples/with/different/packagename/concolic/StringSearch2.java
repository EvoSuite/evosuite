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
package com.examples.with.different.packagename.concolic;


public class StringSearch2 {

    private static int getFragmentLocation(String s) {
        int fragmentLocation = s.indexOf('#');
        if (fragmentLocation == -1)
            return s.length();

        return fragmentLocation;
    }

    public static void checkPathURN(String nuri) {

        // URI code
        String uri = nuri;
        int colonLocation = nuri.indexOf(':');

        int fragmentLocation = getFragmentLocation(nuri);

        if (colonLocation == -1 || colonLocation > fragmentLocation
                || colonLocation == 0)
            throw new RuntimeException("No scheme in URI \"" + uri + "\"");

        // URN code
        String nurn = nuri;
        int secondColonLocation = nurn.indexOf(':', colonLocation + 1);

        if (secondColonLocation == -1 || secondColonLocation > fragmentLocation
                || secondColonLocation == colonLocation + 1)
            throw new RuntimeException("No protocol part in URN \"" + nurn
                    + "\".");

        if (!nurn.regionMatches(0, "urn", 0, colonLocation))
            throw new RuntimeException("The identifier was no URN \"" + nurn
                    + "\".");

        // PathURN code
        if (uri.length() == secondColonLocation + 1)
            throw new RuntimeException("Empty Path URN");

        if (uri.charAt(secondColonLocation + 1) != '/')
            throw new RuntimeException("Path URN has no '/': \"" + uri + "\"");

        if (!uri.regionMatches(colonLocation + 1, "path", 0,
                secondColonLocation - colonLocation - 1))
            throw new RuntimeException("The identifier was no Path URN \""
                    + uri + "\".");
    }


}
