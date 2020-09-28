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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Class used to call methods on URLStreamHandler by reflection
 * @author arcuri
 *
 */
public class URLStreamHandlerUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(URLStreamHandlerUtil.class);
	
	private static Method openConnectionMethod;
	private static Method parseURLMethod;
	
	static{
		try {
			openConnectionMethod = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class, Proxy.class);
			openConnectionMethod.setAccessible(true);
			
			parseURLMethod = URLStreamHandler.class.getDeclaredMethod("parseURL", URL.class, String.class, int.class, int.class);
			parseURLMethod.setAccessible(true);
			
		} catch (NoSuchMethodException | SecurityException e) {
			logger.error("Failed to initialize due to reflection problems: "+e.toString());
		}
		
	}
	
	public static void parseURL(URLStreamHandler handler, URL url, String spec, int start, int limit) throws InvocationTargetException {
		try {
			parseURLMethod.invoke(handler, url, spec, start, limit);
		} catch (IllegalAccessException  e) {
			logger.error("Failed to call parseURL due to reflection problems: "+e.toString());
		}
	}
	
	public static URLConnection openConnection(URLStreamHandler handler, URL url, Proxy proxy) throws InvocationTargetException {
		try {
			return (URLConnection)openConnectionMethod.invoke(handler, url, proxy);
		} catch (IllegalAccessException  e) {
			logger.error("Failed to call openConnection due to reflection problems: "+e.toString());
			return null;
		}
	}
}
