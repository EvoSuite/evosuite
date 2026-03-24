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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.io.MockIOException;


public class MockURL implements StaticReplacementMock{

	@Override
	public String getMockedClassName() {
		return URL.class.getName();
	}

	private static URLStreamHandlerFactory factory;
	private static Map<String, URLStreamHandler> handlers = new ConcurrentHashMap<>();
	private static final Object streamHandlerLock = new Object();

	public static void initStaticState(){
		factory = null;
		handlers.clear();
	}

    /**
     * Provide a valid URL example for the http protocol.
     */
    public static URL getHttpExample(){
        try {
            return URL("http://www.someFakeButWellFormedURL.org/fooExample");
        } catch (MalformedURLException e) {
            //should never happen
            throw new RuntimeException(e);
        }
    }

	public static URL getFtpExample(){
		try {
			return URL("ftp://ftp.someFakeButWellFormedURL.org/fooExample");
		} catch (MalformedURLException e) {
			//should never happen
			throw new RuntimeException(e);
		}
	}

	public static URL getFileExample(){
		try {
			return URL("file://some/fake/but/wellformed/url");
		} catch (MalformedURLException e) {
			//should never happen
			throw new RuntimeException(e);
		}
	}

	// -----  constructors ------------

	public static URL URL(String spec) throws MalformedURLException {
		return URL(null, spec);
	}

	public static URL URL(URL context, String spec) throws MalformedURLException {
		return URL(context, spec, null);
	}

	public static URL URL(String protocol, String host, String file)
			throws MalformedURLException {
		return URL(protocol, host, -1, file);
	}

	public static URL URL(String protocol, String host, int port, String file)
			throws MalformedURLException{
		return URL(protocol, host, port, file, null);
	}

	public static URL URL(String protocol, String host, int port, String file,
			URLStreamHandler handler) throws MalformedURLException {

		if(handler == null){
			handler = getMockedURLStreamHandler(protocol);
		}

		URL url = new URL(protocol,host,port,file,handler);

		return url;
	}


	public static URL URL(URL context, String spec, URLStreamHandler handler)
			throws MalformedURLException{

		if(handler == null){
			String protocol = null;
			if (context != null) {
				protocol = context.getProtocol();
			}
			if (protocol == null || protocol.trim().isEmpty()) {
				int protocolSeparator = spec.indexOf(':');
				if (protocolSeparator > 0) {
					protocol = spec.substring(0, protocolSeparator);
				}
			}

			if (protocol != null && !protocol.trim().isEmpty()) {
				handler = getMockedURLStreamHandler(protocol);
			}
		}

		URL url = new URL(context,spec,handler);

		return url;
	}

	// ---------------------



	public static String getQuery(URL url) {
		return url.getQuery();
	}

	public static String getPath(URL url) {
		return url.getPath();
	}

	public static String getUserInfo(URL url) {
		return url.getUserInfo();
	}

	public static String getAuthority(URL url) {
		return url.getAuthority();
	}

	public static int getPort(URL url) {
		return url.getPort();
	}

	public static int getDefaultPort(URL url) {
		return url.getDefaultPort();
	}

	public static String getProtocol(URL url) {
		return url.getProtocol();
	}

	public static String getHost(URL url) {
		return url.getHost();
	}

	public static String getFile(URL url) {
		return url.getFile();
	}

	public static String getRef(URL url) {
		return url.getRef();
	}


	public static boolean equals(URL url, Object obj) {
    	// URL equals is blocking and broken:
		// https://stackoverflow.com/questions/3771081/proper-way-to-check-for-url-equality
		if (!(obj instanceof URL))
			return false;
		URL u2 = (URL)obj;

		try {
			return url.toURI().equals(u2.toURI());
		} catch(URISyntaxException e) {
			return url.getPath().equals(u2.getPath());
		}
	}


	public static synchronized int hashCode(URL url) {
		try {
			return url.toURI().hashCode();
		} catch(URISyntaxException e) {
			return url.getPath().hashCode();
		}
	}


	public static boolean sameFile(URL url, URL other) {
		return url.sameFile(other);
	}


	public static String toString(URL url) {
		return url.toString();
	}


	public static String toExternalForm(URL url) {
		return url.toExternalForm();
	}


	public static URI toURI(URL url) throws URISyntaxException {
		return new URI (url.toString());
	}


	public static URLConnection openConnection(URL url) throws java.io.IOException {
		return url.openConnection();
	}


	public static URLConnection openConnection(URL url, Proxy proxy)
			throws java.io.IOException {
		if (proxy == null) {
			throw new IllegalArgumentException("proxy can not be null");
		}

		try {
			return url.openConnection(proxy);
		} catch (IOException e) {
			throw new MockIOException(e);
		}
    }


	public static InputStream openStream(URL url) throws java.io.IOException {
		return url.openStream();
	}

	public static Object getContent(URL url) throws java.io.IOException {
		return url.getContent();
	}

	public static Object getContent(URL url, Class[] classes)
			throws java.io.IOException {
		return url.getContent(classes);
	}

	public static void setURLStreamHandlerFactory(URLStreamHandlerFactory fac) {
		synchronized (streamHandlerLock) {
			if (factory != null) {
				throw new Error("factory already defined");
			}
			handlers.clear();
			factory = fac;
		}
	}

	protected static URLStreamHandler getMockedURLStreamHandler(String protocol) throws MalformedURLException {

		URLStreamHandler handler = handlers.get(protocol);
		if (handler == null) {

			// Use the factory (if any)
			if (factory != null) {
				handler = factory.createURLStreamHandler(protocol);
			}

			// create new instance
			if (handler == null){
				if(EvoURLStreamHandler.isValidProtocol(protocol)) {
					handler = new EvoURLStreamHandler(protocol);
				} else {
					throw new MalformedURLException("unknown protocol: "+protocol);
				}
			}

			handlers.put(protocol, handler);
		}

		return handler;
	}
}
