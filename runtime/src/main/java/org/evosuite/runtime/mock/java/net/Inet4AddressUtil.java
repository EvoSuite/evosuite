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
import java.lang.reflect.Constructor;
import java.net.Inet4Address;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class used to instantiate Inet4Address objects.
 * That class cannot be instantiated directly
 * 
 * @author arcuri
 *
 */
public class Inet4AddressUtil {

	private static final Logger logger = LoggerFactory.getLogger(Inet4AddressUtil.class);

	/*
	 * number of bytes in a IPv4 address
	 */
	public static final int INADDRSZ = 4;
	
	private static Constructor<Inet4Address> constructorStringByteArray;
	private static Constructor<Inet4Address> constructorStringInt;
	//private static Field holderField;
	
	static{
		try {
			constructorStringByteArray = Inet4Address.class.getDeclaredConstructor(String.class, byte[].class);
			constructorStringByteArray.setAccessible(true);
			
			constructorStringInt = Inet4Address.class.getDeclaredConstructor(String.class, int.class);
			constructorStringInt.setAccessible(true);
			
			//holderField = InetAddress.class.getDeclaredField("holder");
			//holderField.setAccessible(true);
			
		} catch (NoSuchMethodException | SecurityException e) { // | NoSuchFieldException e) {
			logger.error("Failed to initialize due to reflection problems: "+e.getMessage());
		}
	}
	
	public static Inet4Address createNewInstance(){
		try {
			return Inet4Address.class.newInstance();			
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("Failed to create instance: "+e.getMessage());
		}
		return null; 
	}
	
	public static Inet4Address createNewInstance(String hostName, byte[] addr){
		try {
			return constructorStringByteArray.newInstance(hostName,addr);
		} catch ( SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Failed to create instance: "+e.getMessage());
		}
		return null;
	}
	
	public static Inet4Address createNewInstance(String hostName, int address){
		try {
			return constructorStringInt.newInstance(hostName,address);
		} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Failed to create instance: "+e.getMessage());
		}
		return null;
	}
			
}
