/**
 * Copyright (C) 2011,2012,2013,2014 Gordon Fraser, Andrea Arcuri, José Campos
 * and EvoSuite contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>
 * MD5 class.
 * </p>
 * 
 * @author José Campos based on Heshan Perera @ http://stackoverflow.com/a/10530959/998816
 */
public class MD5 {

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static String hash(String s) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(s.getBytes(), 0, s.length());

			BigInteger i = new BigInteger(1,m.digest());

			return String.format("%1$032x", i);
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}
}
