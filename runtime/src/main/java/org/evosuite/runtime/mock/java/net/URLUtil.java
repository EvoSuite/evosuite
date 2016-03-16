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
package org.evosuite.runtime.mock.java.net;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLStreamHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Class used to operate on URL state by reflection
 * @author arcuri
 *
 */
public class URLUtil {

	private static final Logger logger = LoggerFactory.getLogger(URLUtil.class);

	private static Field hostAddressField;
	private static Field handlerField;
	private static Method setMethod;
	
	static{
		try {
			hostAddressField = URL.class.getDeclaredField("hostAddress");
			hostAddressField.setAccessible(true);
			handlerField = URL.class.getDeclaredField("handler");
			handlerField.setAccessible(true);
			
			setMethod = URL.class.getDeclaredMethod("set", 
					String.class, String.class, int.class, 
					String.class,String.class,String.class,String.class,String.class);
			setMethod.setAccessible(true);
			
		} catch (NoSuchFieldException | SecurityException | NoSuchMethodException e) {
			logger.error("Reflection error: "+e.getMessage());
		}
	}


	public static void set(URL url, String protocol, String host, int port,
			String authority, String userInfo, String path,
			String query, String ref) {

		try {
			setMethod.invoke(url, protocol,host,port,authority,userInfo,path,query,ref);
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error("Reflection error: "+e.getMessage());
		}
	}


	public static URLStreamHandler getHandler(URL url){
		try {
			return (URLStreamHandler) handlerField.get(url);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error("Reflection error: "+e.getMessage());
			return null;
		}
	}

	public static void setHandler(URL url, URLStreamHandler handler){
		try {
			handlerField.set(url, handler);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error("Reflection error: "+e.getMessage());
		}
	}
	
	public static void setHostAddress(URL url, InetAddress hostAddress){
		try {
			hostAddressField.set(url, hostAddress);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error("Reflection error: "+e.getMessage());
		}
	}

	public static InetAddress getHostAddress(URL url){
		try {
			return (InetAddress)hostAddressField.get(url);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error("Reflection error: "+e.getMessage());
			return null;
		}
	}
}
