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
package org.evosuite.runtime.mock.java.net;

import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.lang.MockIllegalArgumentException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


public class MockURI implements StaticReplacementMock {

    @Override
    public String getMockedClassName() {
        return URI.class.getName();
    }

    public static final URI aHttpURI = create("http://foo.bar");

    public static final URI aFileURI= create("file:///tmp/foo.bar");

    public static final URI aFTPURI= create("ftp://foo.bar");

    /**
     * URI is a final class. It seems there is the
     * need to only mock one of its methods, in which URL is used.
     * Still we need a StaticReplacementMock, whereas StaticReplacementMethod
     * cannot be used :(
     */
    public static URL toURL(URI uri) throws MalformedURLException {
        if (!uri.isAbsolute())
            throw new MockIllegalArgumentException("URI is not absolute");
        return MockURL.URL(uri.toString());
    }

    // ----------- constructors -------------------------------------

    public static URI URI(String str) throws URISyntaxException {
        return new URI(str);
    }


    public static URI URI(String scheme,
                          String userInfo, String host, int port,
                          String path, String query, String fragment)
            throws URISyntaxException {
        return new URI(scheme, userInfo, host, port, path, query, fragment);
    }


    public static URI URI(String scheme,
                          String authority,
                          String path, String query, String fragment)
            throws URISyntaxException {
        return new URI(scheme, authority,
                path, query, fragment);
    }


    public static URI URI(String scheme, String host, String path, String fragment)
            throws URISyntaxException {
        return new URI(scheme, host, path, fragment);
    }


    public static URI URI(String scheme, String ssp, String fragment)
            throws URISyntaxException {
        return new URI(scheme, ssp, fragment);
    }


    // --------- static method(s) -------------

    public static URI create(String str) {
        return URI.create(str);
    }


    // ---------  instance methods --------------

    public static URI parseServerAuthority(URI uri)
            throws URISyntaxException {
        return uri.parseServerAuthority();
    }


    public static URI normalize(URI uri) {
        return uri.normalize();
    }


    public static URI resolve(URI instance, URI uri) {
        return instance.resolve(uri);
    }


    public static URI resolve(URI uri, String str) {
        return uri.resolve(str);
    }


    public static URI relativize(URI instance, URI uri) {
        return instance.relativize(uri);
    }

    public static String getScheme(URI instance) {
        return instance.getScheme();
    }

    public static boolean isAbsolute(URI instance) {
        return instance.isAbsolute();
    }

    public static boolean isOpaque(URI instance) {
        return instance.isOpaque();
    }


    public static String getRawSchemeSpecificPart(URI instance) {
        return instance.getRawSchemeSpecificPart();
    }


    public static String getSchemeSpecificPart(URI instance) {
        return instance.getSchemeSpecificPart();
    }


    public static String getRawAuthority(URI instance) {
        return instance.getRawAuthority();
    }


    public static String getAuthority(URI instance) {
        return instance.getAuthority();
    }


    public static String getRawUserInfo(URI instance) {
        return instance.getRawUserInfo();
    }

    public static String getUserInfo(URI instance) {
        return instance.getUserInfo();
    }


    public static String getHost(URI instance) {
        return instance.getHost();
    }


    public static int getPort(URI instance) {
        return instance.getPort();
    }


    public static String getRawPath(URI instance) {
        return instance.getRawPath();
    }


    public static String getPath(URI instance) {
        return instance.getPath();
    }


    public static String getRawQuery(URI instance) {
        return instance.getRawQuery();
    }


    public static String getQuery(URI instance) {
        return instance.getQuery();
    }

    public static String getRawFragment(URI instance) {
        return instance.getRawFragment();
    }


    public static String getFragment(URI instance) {
        return instance.getFragment();
    }


    public static int compareTo(URI instance, URI that) {
        return instance.compareTo(that);
    }


    public static String toASCIIString(URI instance) {
        return instance.toASCIIString();
    }

    public static String toString(URI instance) {
        return instance.toString();
    }
}
