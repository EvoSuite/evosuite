/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

public class URI {

    /**
     * The URI in full.
     */
    protected String uri;

    /**
     * The location of the colon.
     */
    protected int colonLocation;

    /**
     * The location of the hash '#'.
     */
    protected int fragmentLocation;

    /**
     * Creates a generic URI from the given string.
     * 
     * @param nuri
     *            the string to parse.
     * @exception MalformedURIException
     *                if the string did not parse.
     */
    public URI(String nuri) throws MalformedURIException {
        uri = nuri;
        colonLocation = nuri.indexOf(':');

        fragmentLocation = URIClassifier.getFragmentLocation(nuri);

        if (colonLocation == -1 || colonLocation > fragmentLocation
                || colonLocation == 0)
            throw new MalformedURIException("No scheme in URI \"" + uri + "\"",
                    uri);
    }

    public String makeRelative(URI otherURI, boolean allowDotDot)
            throws MalformedURIException {
        return otherURI.toString();
    }

    protected String genericMakeRelative(URI otherURI, int otherPathLoc,
            int pathloc, boolean allowDotDot) {
        if (otherPathLoc != pathloc
                || !uri.regionMatches(0, otherURI.uri, 0, pathloc)) //Different
                                                                    // before
                                                                    // paths
                                                                    // begin,
                                                                    // cannot be
                                                                    // relative...
            return otherURI.toString();

        int commonPath = pathloc;

        int nextSlash = pathloc;

        // Find common path
        while ((nextSlash = uri.indexOf('/', nextSlash + 1)) != -1
                && nextSlash < fragmentLocation) {
            if (uri.regionMatches(commonPath, otherURI.uri, commonPath,
                    nextSlash - commonPath))
                commonPath = nextSlash;
            else
                break;
        }

        // Additional path to the other URI

        StringBuffer relPath = new StringBuffer();

        // For each subdir, prepend "../" to relative path. If dotdot is not
        // allowed, break and make absolute URI.
        nextSlash = commonPath;

        while ((nextSlash = uri.indexOf('/', nextSlash + 1)) != -1
                && nextSlash < fragmentLocation) {
            if (allowDotDot)
                relPath.append("../");
            else {
                relPath.append(otherURI.uri.substring(0, commonPath + 1));
                break;
            }
        }

        // If relative path is the same dir...
        if (relPath.length() == 0) {
            // Check to see if the file is the same. Could add getFile()
            // functions...
            String thisFile = uri.substring(commonPath, fragmentLocation);
            String otherFile = otherURI.uri.substring(commonPath,
                    otherURI.fragmentLocation);

            // If the files are the same, include only fragment information.
            // If there are no fragments, the URI will be "", which is OK!
            if (thisFile.equals(otherFile))
                commonPath = fragmentLocation - 1;
        }

        relPath.append(otherURI.uri.substring(commonPath + 1));

        return relPath.toString();
    }

    protected URI parseRelativeURI(String relstr) throws MalformedURIException {
        throw new MalformedURIException(
                "Generic URIs do not support relative URIs", uri);
    }

    protected String genericParseRelativeURI(String relStr, int pathLoc) {
        if (relStr.indexOf("#") == 0 || relStr.length() == 0)
            return uri + relStr;

        String res;

        if (relStr.length() > 0 && relStr.charAt(0) == '/')
            res = uri.substring(0, pathLoc) + relStr;
        else {
            int relPos = 0;
            int uriPos = uri.lastIndexOf('/', fragmentLocation - 1);

            while (relStr.regionMatches(relPos, "../", 0, 3)
                    && pathLoc < uriPos) {
                relPos += 3;
                uriPos = uri.lastIndexOf('/', uriPos - 1);
            }

            res = uri.substring(0, uriPos + 1) + relStr.substring(relPos);
        }

        return res;
    }

    /**
     * Returns the java.net.URL representation.
     * 
     * @return the java.net.URL representation.
     * @exception MalformedURLException
     *                if the URI did not parse into a java.net.URL.
     */
    public java.net.URL getJavaURL() throws java.net.MalformedURLException {
        return new java.net.URL(uri);
    }

    /**
     * Returns the <code>scheme</code> part of the URI.
     */
    public String getScheme() {
        return uri.substring(0, colonLocation);
    }

    /**
     * Returns the <code>scheme-specific</code> part of the URI.
     */
    public String getSchemeSpecific() {
        return uri.substring(colonLocation + 1, fragmentLocation);
    }

    /**
     * Returns the <code>fragment</code> part of the URI.
     */
    public String getFragment() {
        if (fragmentLocation == uri.length())
            return "";

        return uri.substring(fragmentLocation + 1);
    }

    /**
     * Returns a string without the <code>fragment</code> part of the URI.
     */
    public String getBase() {
        return uri.substring(0, fragmentLocation);
    }

    /**
     * @return a string which is the parent directory.
     */
    public String getParentDirectory() {
        if (uri.endsWith("/")) {
            int lastSlash = uri.substring(0,uri.length()).lastIndexOf('/');
            return uri.substring(0, lastSlash + 1);            
        } else {
            int lastSlash = uri.lastIndexOf('/');
            return uri.substring(0, lastSlash + 1);
        }
    }

    /**
     * Returns the URI in original form, i.e.
     * <code>scheme:scheme-specific</code>.
     */
    public String toString() {
        return uri;
    }

    public boolean equals(Object compURI) {
        if (compURI == null)
            return false;
        if (compURI instanceof URI) {
            URI nuri = (URI) compURI;
            return nuri.uri.equals(uri);
        }
        return false;
    }

    public int hashCode() {
        return uri.hashCode();
    }
}

