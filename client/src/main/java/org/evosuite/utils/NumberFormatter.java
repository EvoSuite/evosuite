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
/**
 * 
 */
package org.evosuite.utils;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * <p>
 * NumberFormatter class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class NumberFormatter {

	/**
	 * <p>
	 * getNumberString
	 * </p>
	 * 
	 * @param value
	 *            a {@link java.lang.Object} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getNumberString(Object value) {
		if (value == null)
			return "null";
		else if (value.getClass().equals(char.class)
		        || value.getClass().equals(Character.class)) {
			// StringEscapeUtils fails to escape a single quote char
			if (Character.valueOf('\'').equals(value)) {
				return "'\\\''";
			} else {
				return "'"
				        + StringEscapeUtils.escapeJava(Character.toString((Character) value))
				        + "'";
			}
		} else if (value.getClass().equals(String.class)) {
			return "\"" + StringEscapeUtils.escapeJava((String) value) + "\"";
		} else if (value.getClass().equals(float.class)
		        || value.getClass().equals(Float.class)) {
			if (value.toString().equals("" + Float.NaN))
				return "Float.NaN";
			else if (value.toString().equals("" + Float.NEGATIVE_INFINITY))
				return "Float.NEGATIVE_INFINITY";
			else if (value.toString().equals("" + Float.POSITIVE_INFINITY))
				return "Float.POSITIVE_INFINITY";
			else if (((Float) value) < 0F)
				return "(" + value + "F)";
			else
				return value + "F";
		} else if (value.getClass().equals(double.class)
		        || value.getClass().equals(Double.class)) {
			if (value.toString().equals("" + Double.NaN))
				return "Double.NaN";
			else if (value.toString().equals("" + Double.NEGATIVE_INFINITY))
				return "Double.NEGATIVE_INFINITY";
			else if (value.toString().equals("" + Double.POSITIVE_INFINITY))
				return "Double.POSITIVE_INFINITY";
			else if (((Double) value) < 0.0)
				return "(" + value + ")";
			else
				return value.toString();
		} else if (value.getClass().equals(long.class)
		        || value.getClass().equals(Long.class)) {
			if (((Long) value) < 0)
				return "(" + value + "L)";
			else
				return value + "L";
		} else if (value.getClass().equals(byte.class)
		        || value.getClass().equals(Byte.class)) {
			if (((Byte) value) < 0)
				return "(byte) (" + value + ")";
			else
				return "(byte)" + value;
		} else if (value.getClass().equals(short.class)
		        || value.getClass().equals(Short.class)) {
			if (((Short) value) < 0)
				return "(short) (" + value + ")";
			else
				return "(short)" + value;
		} else if (value.getClass().equals(int.class)
		        || value.getClass().equals(Integer.class)) {
			int val = ((Integer) value).intValue();
			if (val == Integer.MAX_VALUE)
				return "Integer.MAX_VALUE";
			else if (val == Integer.MIN_VALUE)
				return "Integer.MIN_VALUE";
			else if (((Integer) value) < 0)
				return "(" + value + ")";
			else
				return "" + val;
		} else if (value.getClass().isEnum() || value instanceof Enum) {
			// java.util.concurrent.TimeUnit is an example where the enum
			// elements are anonymous inner classes, and then isEnum does
			// not return true apparently? So we check using instanceof as well.
			
			Class<?> clazz = value.getClass();
			String className = clazz.getSimpleName();
			while (clazz.getEnclosingClass() != null) {
				String enclosingName = clazz.getEnclosingClass().getSimpleName();
				className = enclosingName + "." + className;
				clazz = clazz.getEnclosingClass();
			}
			
			// We have to do this here to avoid a double colon in the TimeUnit example
			if(!className.endsWith("."))
				className += ".";
			try {
				if (value.getClass().getField(value.toString()) != null)
					return className  + value;
				else if (((Enum<?>)value).name() != null)
					return className  + ((Enum<?>)value).name();
				else
					return "Enum.valueOf("+className + "class, \"" + value + "\")";
			} catch (Exception e) {
				if (((Enum<?>)value).name() != null)
					return className  + ((Enum<?>)value).name();
				else
					return "Enum.valueOf("+className + "class /* "+e+" */, \"" + value + "\")";
				// return className + "valueOf(\"" + value + "\")";
			}
		} else if(value.getClass().equals(Boolean.class)) {
			return value.toString();
		} else {
			// This should not happen
			assert(false);
			return value.toString();
		}
	}

	/**
	 * <p>
	 * getBoxedClassName
	 * </p>
	 * 
	 * @param value
	 *            a {@link java.lang.Object} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getBoxedClassName(Object value) {
		if (value.getClass().equals(Double.class))
			return "double";
		else if (value.getClass().equals(Float.class))
			return "float";
		else if (value.getClass().equals(Long.class))
			return "long";
		else if (value.getClass().equals(Boolean.class))
			return "boolean";
		else if (value.getClass().equals(Short.class))
			return "short";
		else if (value.getClass().equals(Integer.class))
			return "int";
		else if (value.getClass().equals(Byte.class))
			return "byte";
		else if (value.getClass().equals(Character.class))
			return "char";
		else
			return value.getClass().getSimpleName();

	}

}
