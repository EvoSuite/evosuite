/**
 * 
 */
package de.unisb.cs.st.evosuite.utils;

import org.apache.commons.lang.StringEscapeUtils;

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
			return value.getClass().getSimpleName() + "." + value;

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
