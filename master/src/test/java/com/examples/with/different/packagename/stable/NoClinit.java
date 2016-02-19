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
package com.examples.with.different.packagename.stable;

import java.lang.reflect.Field;

public class NoClinit {

	private static final boolean SOME_TRUE_VALUE = true;
	private static final boolean SOME_BOOLEAN_VALUE = false;
	private static final byte SOME_BYTE_VALUE = 2;
	private static final short SOME_SHORT_VALUE = 22222;
	private static final char SOME_CHAR_VALUE = 'c';
	private static final int SOME_INT_VALUE = 22222;
	private static final long SOME_LONG_VALUE = 2;
	private static final float SOME_FLOAT_VALUE = 2f;
	private static final double SOME_DOUBLE_VALUE = 2f;
	private static final String SOME_STRING_VALUE = "Hello World!";


	
	public boolean reflecDoubleBranch() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = NoClinit.class.getDeclaredField("SOME_DOUBLE_VALUE");
		double doubleValue = field.getLong(null);
		if (doubleValue!=2f) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean reflecFloatBranch() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = NoClinit.class.getDeclaredField("SOME_FLOAT_VALUE");
		long floatValue = field.getLong(null);
		if (floatValue!=2f) {
			return false;
		} else {
			return true;
		}
	}
	
	
	public boolean reflecIntBranch() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = NoClinit.class.getDeclaredField("SOME_INT_VALUE");
		int intValue = field.getInt(null);
		if (intValue!=22222) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean reflecLongBranch() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = NoClinit.class.getDeclaredField("SOME_LONG_VALUE");
		long longValue = field.getLong(null);
		if (longValue!=2) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean reflecShortBranch() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = NoClinit.class.getDeclaredField("SOME_SHORT_VALUE");
		short shortValue = field.getShort(null);
		if (shortValue!=22222) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean reflecCharBranch() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = NoClinit.class.getDeclaredField("SOME_CHAR_VALUE");
		char charValue = field.getChar(null);
		if (charValue!='c') {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean reflectDoubleBranch() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = NoClinit.class.getDeclaredField("SOME_DOUBLE_VALUE");
		double doubleValue = field.getDouble(null);
		if (doubleValue!=2f) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean reflecBooleanBranch() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = NoClinit.class.getDeclaredField("SOME_TRUE_VALUE");
		boolean booleanValue = field.getBoolean(null);
		if (booleanValue!=true) {
			return false;
		} else {
			return true;
		}
	}

	public boolean reflecByteBranch() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = NoClinit.class.getDeclaredField("SOME_BYTE_VALUE");
		byte byteValue = field.getByte(null);
		if (byteValue!=2) {
			return false;
		} else {
			return true;
		}
	}

	public boolean reflecStringBranch() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = NoClinit.class.getDeclaredField("SOME_STRING_VALUE");
		Object object = field.get(null);
		if (object==null) {
			return false;
		} else {
			return true;
		}
	}	
}
