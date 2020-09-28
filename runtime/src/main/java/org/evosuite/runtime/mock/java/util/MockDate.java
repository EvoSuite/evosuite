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
package org.evosuite.runtime.mock.java.util;

import org.evosuite.runtime.mock.OverrideMock;

@SuppressWarnings("deprecation")
public class MockDate extends java.util.Date  implements OverrideMock{

	private static final long serialVersionUID = 6252798426594925071L;

	public MockDate() {
		super(org.evosuite.runtime.System.currentTimeMillis());
	}
	
	public MockDate(long time) {
		super(time);
	}
	
	public MockDate(int year, int month, int date) {
		super(year, month, date);
	}
	
	public MockDate(int year, int month, int date, int hrs, int min) {
		super(year, month, date, hrs, min);
	}
	
	public MockDate(int year, int month, int date, int hrs, int min, int sec) {
		super(year, month, date, hrs, min, sec);
	}
	
	public MockDate(String s) {
		super(s);
	}
	
	public static long parse(String s) {
		return java.util.Date.parse(s);
	}
	
	public static long UTC(int year,
            int month,
            int date,
            int hrs,
            int min,
            int sec) {
		return java.util.Date.UTC(year, month, date, hrs, min, sec);
	}
}
