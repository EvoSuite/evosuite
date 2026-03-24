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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;


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
	
	public static Inet4Address createNewInstance(){
		return createNewInstance(null, new byte[INADDRSZ]);
	}
	
	public static Inet4Address createNewInstance(String hostName, byte[] addr){
		try {
			return (Inet4Address) InetAddress.getByAddress(hostName, addr);
		} catch (UnknownHostException | ClassCastException e) {
			logger.error("Failed to create instance: {}", e.getMessage());
		}
		return null;
	}
	
	public static Inet4Address createNewInstance(String hostName, int address){
		byte[] addr = new byte[] {
				(byte) ((address >>> 24) & 0xFF),
				(byte) ((address >>> 16) & 0xFF),
				(byte) ((address >>> 8) & 0xFF),
				(byte) (address & 0xFF)
		};
		return createNewInstance(hostName, addr);
	}
			
}
