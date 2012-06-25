/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
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
/**
 * 
 */
package org.evosuite.utils;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * @author Gordon Fraser
 * 
 */
public class NumberFormatter {

	public static String getNumberString(Object value) {
		if (value == null)
			return "null";
		else if (value.getClass().equals(char.class)
		        || value.getClass().equals(Character.class))
			return "'" + StringEscapeUtils.escapeJava(value.toString()) + "'";
		else if (value.getClass().equals(String.class)) {
			return "\"" + StringEscapeUtils.escapeJava((String) value) + "\"";
		} else if (value.getClass().equals(float.class)
		        || value.getClass().equals(Float.class)) {
			if (value.toString().equals("NaN"))
				return "Float.NaN";
			else if (value.toString().equals("NEGATIVE_INFINITY"))
				return "Float.NEGATIVE_INFINITY";
			else if (value.toString().equals("POSITIVE_INFINITY"))
				return "Float.POSITIVE_INFINITY";
			else
				return value + "F";
		} else if (value.getClass().equals(double.class)
		        || value.getClass().equals(Double.class)) {
			if (value.toString().equals("NaN"))
				return "Double.NaN";
			else if (value.toString().equals("NEGATIVE_INFINITY"))
				return "Double.NEGATIVE_INFINITY";
			else if (value.toString().equals("POSITIVE_INFINITY"))
				return "Double.POSITIVE_INFINITY";
			else
				return value.toString();
		} else if (value.getClass().equals(long.class)
		        || value.getClass().equals(Long.class)) {
			return value + "L";
		} else if (value.getClass().equals(byte.class)
		        || value.getClass().equals(Byte.class)) {
			return "(byte)" + value;
		} else if (value.getClass().equals(short.class)
		        || value.getClass().equals(Short.class)) {
			return "(short)" + value;
		} else if (value.getClass().isEnum()) {
			Class<?> clazz = value.getClass();
			String className = clazz.getSimpleName();
			while (clazz.getEnclosingClass() != null) {
				className = clazz.getEnclosingClass().getSimpleName() + "." + className;
				clazz = clazz.getEnclosingClass();
			}
			try {
				if (value.getClass().getField(value.toString()) != null)
					return className + "." + value;
				else
					return className + ".valueOf(\"" + value + "\")";
			} catch (Exception e) {
				return className + ".valueOf(\"" + value + "\")";
			}

		} else
			return value.toString();
	}

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
