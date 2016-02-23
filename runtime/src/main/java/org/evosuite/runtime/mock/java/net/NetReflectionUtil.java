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

import java.lang.reflect.Method;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class used to call package level methods in java.net
 * 
 * TODO this will be needed for rollbacks
 * 
 * @author arcuri
 *
 */
public class NetReflectionUtil {

	private static final Logger logger = LoggerFactory.getLogger(NetReflectionUtil.class);
	
	/**
	 * Wrapper for {@code InetAddress.anyLocalAddress()} 
	 * @return
	 */
	public static InetAddress anyLocalAddress(){
				
		try {
			Method m = InetAddress.class.getDeclaredMethod("anyLocalAddress");
			m.setAccessible(true);
			return (InetAddress) m.invoke(null);
		} catch (Exception e) {
			//should never happen
			logger.error("Failed to use reflection on InetAddress.anyLocalAddress(): "+e.getMessage(),e);
		} 
		
		return null;
	}
}
