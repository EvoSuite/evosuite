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

import java.net.URI;
import java.net.URISyntaxException;

public class URIClassifier {
    public URIClassifier() {
    }

    /*
     * public static String findShortestRelativeURI(String makeSmaller, String
     * fromBase) { boolean foundSmaller=false; int
     * spos=makeSmaller.indexOf('/'); int bpos=fromBase.indexOf('/'); String
     * sstr=makeSmaller; String bstr=fromBase;
     *
     * while ( spos==bpos && spos!=-1 &&
     * sstr.substring(0,spos).equals(bstr.substring(0,bpos)) ) {
     * foundSmaller=true; sstr=sstr.substring(spos+1);
     * bstr=bstr.substring(bpos+1); spos=sstr.indexOf('/');
     * bpos=bstr.indexOf('/'); } if (foundSmaller) { if
     * ((bpos=bstr.indexOf('/'))==-1) return sstr; else {
     * bstr=bstr.substring(bpos+1); if ((bpos=bstr.indexOf('/'))==-1) return
     * "../"+sstr; else { bstr=bstr.substring(bpos+1); if
     * ((bpos=bstr.indexOf('/'))==-1) return "../../"+sstr; } } } return
     * makeSmaller; }
     */

    /**
     * @param nuri
     * @return URI
     * @deprecated Use java.net.URI instead.
     */
    public static URI parseValidURI(String nuri) {
        return parseValidURI(nuri, null);
    }

    /**
     * @param nuri
     * @param baseuri
     * @return URI
     */
    public static URI parseValidURI(String nuri, String baseuri) {
        try {
            if (baseuri != null)
                return URIClassifier.parseURI(nuri, URIClassifier.parseURI(baseuri, null));
            else
                return URIClassifier.parseURI(nuri, null);
        } catch (URISyntaxException e) {
            Tracer.bug("Malformed URI '" + e + "': \n" + e.getMessage());
            return null; // Never reached
        }
    }

    /**
     * @param nuri
     * @return URI
     * @throws URISyntaxException
     * @deprecated Use java.net.URI instead.
     */
    public static URI parseURI(String nuri) throws URISyntaxException {
        return parseURI(nuri, null);
    }

    /**
     * Constructs an URI from a String
     *
     * @param nuri the URI
     * @deprecated Use java.net.URI instead.
     */
    public static URI parseURI(String nuri, URI baseuri) throws URISyntaxException {

        /* baseuri is not used in this method */

        int colonLocation = nuri.indexOf(':');

        int fragmentLocation = getFragmentLocation(nuri);

        if (colonLocation == -1 || colonLocation > fragmentLocation) {
            if (baseuri == null)
                throw new URISyntaxException("No ':' in \"" + nuri + "\" and no base URI given,"
                        + " so no relative URIs allowed", nuri);
        }

        String protocol = nuri.substring(0, colonLocation);

        if (protocol.equals("urn")) {
            return parseURN(nuri, colonLocation, fragmentLocation);
        } else if (protocol.equals("http")) {
            // Replaces all whitespaces with %20 since Javas URL class doesn't
            // do it.
//			StringBuffer buf = new StringBuffer(nuri);
//			int pos = buf.toString().indexOf(' ');
//			while (pos != -1) {
//				buf.replace(pos, pos + 1, "%20");
//				pos = buf.toString().indexOf(' ');
//			}
//			int pathLocation = buf.toString().indexOf('/', 8);
//
//			if (pathLocation == -1) {
//				return new URI(buf.toString() + "/");
//			} else {
//				return new URI(buf.toString());
//			}
            return new URI(nuri);
        } else if (protocol.equals("file")) {
            return new URI(nuri);
        } else if (protocol.equals("res")) {
            return new URI(nuri);
        } else {
            return parseGeneralURI(nuri, colonLocation, fragmentLocation);
        }
    }

    protected static URI parseURN(String nuri, int colonLocation, int fragmentLocation) throws URISyntaxException {
        int secondColonLocation = nuri.indexOf(':', colonLocation + 1);

        if (secondColonLocation == -1 || secondColonLocation > fragmentLocation
                || secondColonLocation == colonLocation + 1)
            throw new URISyntaxException("No protocol part in URN \"" + nuri + "\".", nuri);

        String urnprotocol = nuri.substring(colonLocation + 1, secondColonLocation);

        if (urnprotocol.equals("path"))
            return new URI(nuri);
        else
            return parseGeneralURN(nuri, colonLocation, secondColonLocation, fragmentLocation);
    }

    protected static URI parseGeneralURN(String nuri, int colonLocation, int secondColonLocation, int fragmentLocation)
            throws URISyntaxException {
        return parseGeneralURI(nuri, colonLocation, fragmentLocation);
    }

    protected static URI parseGeneralURI(String nuri, int colonLocation, int fragmentLocation)
            throws URISyntaxException {
        return new URI(nuri);
    }

    protected static int getFragmentLocation(String s) {
        int fragmentLocation = s.indexOf('#');
        if (fragmentLocation == -1)
            return s.length();

        return fragmentLocation;
    }
}