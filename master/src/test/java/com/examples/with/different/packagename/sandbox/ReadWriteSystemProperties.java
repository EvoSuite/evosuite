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
package com.examples.with.different.packagename.sandbox;

public class ReadWriteSystemProperties {

	public static final String A_PROPERTY = "a property with a ridiculosly long value and different characters $#%@!*$";
	public static final String USER_DIR = "user.dir";
	
	public boolean foo(String s){
		
		String dir = System.getProperty(USER_DIR);
		System.setProperty(USER_DIR, A_PROPERTY);//any value here would do
		System.setProperty(A_PROPERTY, dir);
		String readBack = System.getProperty(A_PROPERTY);
		
		if(readBack.equals(s)){
			return true;
		} else {
			return false;
		}
	}
}
