/**
 * 
 */
package com.examples.with.different.packagename;

/**
 * This class leads to a compile error in an assertion on a Boolean:
 * assertEquals("8E8D%[::r&G}", (String)boolean0);
 * 
 * 
 */
public class JSONCastErrorExample {

	public static Object stringToValue(String string) {
		//Double d;

		if (string.equals("")) {
			return string;
		}
		if (string.equalsIgnoreCase("true")) {
			return Boolean.TRUE;
		}
		if (string.equalsIgnoreCase("false")) {
			return Boolean.FALSE;
		}
		if (string.equalsIgnoreCase("null")) {
			return null;
		}
		/*
		 * If it might be a number, try converting it. If a number cannot be
		 * produced, then the value will just be a string.
		 */

		char b = string.charAt(0);
		if ((b >= '0' && b <= '9') || b == '-') {
			try {
				if (string.indexOf('.') > -1 || string.indexOf('e') > -1
				        || string.indexOf('E') > -1) {
					/*
					d = Double.valueOf(string);
					if (!d.isInfinite() && !d.isNaN()) {
						return d;
					}
					*/
				} /*else {
				  Long myLong = new Long(string);
				  if (string.equals(myLong.toString())) {
				  	if (myLong.longValue() == myLong.intValue()) {
				  		return new Integer(myLong.intValue());
				  	} else {
				  		return myLong;
				  	}
				  }
				  }*/
			} catch (Exception ignore) {
			}
		}
		return string;
	}
}
